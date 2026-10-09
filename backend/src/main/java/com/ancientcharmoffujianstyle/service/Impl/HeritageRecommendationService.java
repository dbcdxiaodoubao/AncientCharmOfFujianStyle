package com.ancientcharmoffujianstyle.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ancientcharmoffujianstyle.domain.entity.Fyinfo;
import com.ancientcharmoffujianstyle.domain.entity.UserBrowseHistory;
import com.ancientcharmoffujianstyle.domain.entity.UserFavorite;
import com.ancientcharmoffujianstyle.domain.entity.UserPreference;
import com.ancientcharmoffujianstyle.domain.vo.RecommendationVo;
import com.ancientcharmoffujianstyle.mapper.FyinfoMapper;
import com.ancientcharmoffujianstyle.mapper.SysUserMapper;
import com.ancientcharmoffujianstyle.mapper.UserBrowseHistoryMapper;
import com.ancientcharmoffujianstyle.mapper.UserFavoriteMapper;
import com.ancientcharmoffujianstyle.mapper.UserPreferenceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class HeritageRecommendationService {

    private static final double DIVERSITY_PENALTY = 65D;
    private final FyinfoMapper fyinfoMapper;
    private final SysUserMapper userMapper;
    private final UserPreferenceMapper preferenceMapper;
    private final UserFavoriteMapper favoriteMapper;
    private final UserBrowseHistoryMapper browseHistoryMapper;
    private final Clock clock;

    @Autowired
    public HeritageRecommendationService(FyinfoMapper fyinfoMapper, SysUserMapper userMapper,
                                         UserPreferenceMapper preferenceMapper, UserFavoriteMapper favoriteMapper,
                                         UserBrowseHistoryMapper browseHistoryMapper) {
        this(fyinfoMapper, userMapper, preferenceMapper, favoriteMapper, browseHistoryMapper,
                Clock.systemDefaultZone());
    }

    public HeritageRecommendationService(FyinfoMapper fyinfoMapper, SysUserMapper userMapper,
                                         UserPreferenceMapper preferenceMapper, UserFavoriteMapper favoriteMapper,
                                         UserBrowseHistoryMapper browseHistoryMapper, Clock clock) {
        this.fyinfoMapper = fyinfoMapper;
        this.userMapper = userMapper;
        this.preferenceMapper = preferenceMapper;
        this.favoriteMapper = favoriteMapper;
        this.browseHistoryMapper = browseHistoryMapper;
        this.clock = clock;
    }

    public List<RecommendationVo> recommend(Long userId) {
        if (userId != null && userId <= 0) {
            throw new IllegalArgumentException("用户id不能为空");
        }
        if (userId != null && userMapper.selectById(userId) == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        List<Fyinfo> candidates = fyinfoMapper.selectList(new LambdaQueryWrapper<Fyinfo>());
        if (candidates == null || candidates.isEmpty()) {
            return Collections.emptyList();
        }
        UserPreference preference = null;
        List<UserFavorite> favorites = Collections.emptyList();
        List<UserBrowseHistory> histories = Collections.emptyList();
        if (userId != null) {
            preference = preferenceMapper.selectById(userId);
            favorites = favoriteMapper.selectList(new LambdaQueryWrapper<UserFavorite>()
                    .eq(UserFavorite::getUserId, userId));
            histories = browseHistoryMapper.selectRecent30DaysByUserId(userId);
        }
        Set<Long> favoriteIds = favoriteIds(favorites);
        Set<String> favoriteTypes = typesFor(favoriteIds, candidates);
        Map<String, Double> browseTypeScores = browseTypeScores(histories, candidates);
        List<ScoredRecommendation> scored = new ArrayList<>();
        for (Fyinfo candidate : candidates) {
            scored.add(score(candidate, preference, favoriteIds, favoriteTypes, browseTypeScores));
        }
        scored.sort(Comparator.comparingDouble(ScoredRecommendation::getBaseScore).reversed()
                .thenComparing(item -> item.recommendation.getId()));
        return diversify(scored);
    }

    public void recordBrowse(Long userId, Long fyId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("用户id不能为空");
        }
        if (userMapper.selectById(userId) == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        if (fyId == null || fyId <= 0 || fyinfoMapper.selectById(fyId) == null) {
            throw new IllegalArgumentException("非遗项目不存在");
        }
        UserBrowseHistory history = new UserBrowseHistory();
        history.setUserId(userId);
        history.setFyId(fyId);
        browseHistoryMapper.insert(history);
    }

    private ScoredRecommendation score(Fyinfo candidate, UserPreference preference, Set<Long> favoriteIds,
                                       Set<String> favoriteTypes, Map<String, Double> browseTypeScores) {
        RecommendationVo result = toVo(candidate);
        List<String> reasons = new ArrayList<>();
        double qualityScore = qualityScore(candidate);
        double score = qualityScore;
        if (preference != null && candidate.getCity() != null && candidate.getCity().equals(preference.getCity())) {
            score += 30D;
            reasons.add("匹配偏好城市");
        }
        if (preference != null && same(candidate.getType(), preference.getCategory())) {
            score += 30D;
            reasons.add("匹配偏好类别");
        }
        if (favoriteTypes.contains(candidate.getType())) {
            score += 25D;
            reasons.add("收藏同类别项目");
        }
        Double browseScore = browseTypeScores.get(candidate.getType());
        if (browseScore != null && browseScore > 0D) {
            score += browseScore;
            reasons.add("近期浏览同类别项目");
        }
        if (favoriteIds.contains(candidate.getId())) {
            reasons.add("已收藏项目关联");
        }
        if (qualityScore > 0D) {
            reasons.add("非遗等级与资料完整度");
        }
        result.setScore(score);
        result.setReasons(reasons);
        return new ScoredRecommendation(result);
    }

    private List<RecommendationVo> diversify(List<ScoredRecommendation> scored) {
        List<ScoredRecommendation> remaining = new ArrayList<>(scored);
        List<RecommendationVo> results = new ArrayList<>();
        RecommendationVo previous = null;
        while (!remaining.isEmpty()) {
            ScoredRecommendation next = null;
            double highest = -Double.MAX_VALUE;
            for (ScoredRecommendation item : remaining) {
                double adjusted = item.getBaseScore();
                if (previous != null && repeatsDiversityDimension(previous, item.recommendation)) {
                    adjusted -= DIVERSITY_PENALTY;
                }
                if (next == null || adjusted > highest || (adjusted == highest
                        && item.recommendation.getId() < next.recommendation.getId())) {
                    next = item;
                    highest = adjusted;
                }
            }
            if (previous != null && repeatsDiversityDimension(previous, next.recommendation)) {
                next.recommendation.setScore(highest);
                addDiversityReason(next.recommendation);
            }
            results.add(next.recommendation);
            previous = next.recommendation;
            remaining.remove(next);
        }
        return results;
    }

    private Map<String, Double> browseTypeScores(List<UserBrowseHistory> histories, List<Fyinfo> candidates) {
        Map<Long, String> typesById = new HashMap<>();
        for (Fyinfo candidate : candidates) {
            typesById.put(candidate.getId(), candidate.getType());
        }
        Map<String, Double> scores = new HashMap<>();
        if (histories == null) {
            return scores;
        }
        LocalDate today = LocalDate.now(clock);
        for (UserBrowseHistory history : histories) {
            String type = typesById.get(history.getFyId());
            if (type == null || history.getBrowseTime() == null) {
                continue;
            }
            long days = ChronoUnit.DAYS.between(history.getBrowseTime().toLocalDate(), today);
            if (days < 0 || days > 30) {
                continue;
            }
            double score = 20D * (30D - days) / 30D;
            Double existing = scores.get(type);
            if (existing == null || score > existing) {
                scores.put(type, score);
            }
        }
        return scores;
    }

    private Set<Long> favoriteIds(List<UserFavorite> favorites) {
        Set<Long> ids = new HashSet<>();
        if (favorites != null) {
            for (UserFavorite favorite : favorites) {
                if (favorite.getFyId() != null) {
                    ids.add(favorite.getFyId());
                }
            }
        }
        return ids;
    }

    private Set<String> typesFor(Set<Long> ids, List<Fyinfo> candidates) {
        Set<String> types = new HashSet<>();
        for (Fyinfo candidate : candidates) {
            if (ids.contains(candidate.getId()) && candidate.getType() != null) {
                types.add(candidate.getType());
            }
        }
        return types;
    }

    private double qualityScore(Fyinfo candidate) {
        double score;
        if (candidate.getLevel() == null) {
            score = 0D;
        } else if (candidate.getLevel() == 1L) {
            score = 10D;
        } else if (candidate.getLevel() == 2L) {
            score = 8D;
        } else if (candidate.getLevel() == 3L) {
            score = 6D;
        } else if (candidate.getLevel() == 4L) {
            score = 4D;
        } else if (candidate.getLevel() == 5L) {
            score = 1D;
        } else {
            score = 0D;
        }
        if (hasText(candidate.getDtl())) {
            score += 3D;
        }
        if (hasText(candidate.getPictureUrl())) {
            score += 2D;
        }
        return Math.min(score, 15D);
    }

    private RecommendationVo toVo(Fyinfo candidate) {
        RecommendationVo result = new RecommendationVo();
        result.setId(candidate.getId());
        result.setName(candidate.getName());
        result.setPictureUrl(candidate.getPictureUrl());
        result.setCity(candidate.getCity());
        result.setLevel(candidate.getLevel());
        result.setType(candidate.getType());
        return result;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private boolean same(Object left, Object right) {
        return left != null && left.equals(right);
    }

    private void addDiversityReason(RecommendationVo recommendation) {
        String reason = "与上一推荐同城或同类，已应用多样性降权";
        if (!recommendation.getReasons().contains(reason)) {
            recommendation.getReasons().add(reason);
        }
    }

    private boolean repeatsDiversityDimension(RecommendationVo previous, RecommendationVo current) {
        return same(previous.getCity(), current.getCity()) || same(previous.getType(), current.getType());
    }

    private static class ScoredRecommendation {
        private final RecommendationVo recommendation;
        private final double baseScore;

        private ScoredRecommendation(RecommendationVo recommendation) {
            this.recommendation = recommendation;
            this.baseScore = recommendation.getScore();
        }

        private double getBaseScore() {
            return baseScore;
        }
    }
}
