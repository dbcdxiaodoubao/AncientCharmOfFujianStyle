package com.ancientcharmoffujianstyle.service;

import com.ancientcharmoffujianstyle.domain.entity.Fyinfo;
import com.ancientcharmoffujianstyle.domain.entity.SysUser;
import com.ancientcharmoffujianstyle.domain.entity.UserBrowseHistory;
import com.ancientcharmoffujianstyle.domain.entity.UserFavorite;
import com.ancientcharmoffujianstyle.domain.entity.UserPreference;
import com.ancientcharmoffujianstyle.domain.vo.RecommendationVo;
import com.ancientcharmoffujianstyle.mapper.FyinfoMapper;
import com.ancientcharmoffujianstyle.mapper.SysUserMapper;
import com.ancientcharmoffujianstyle.mapper.UserBrowseHistoryMapper;
import com.ancientcharmoffujianstyle.mapper.UserFavoriteMapper;
import com.ancientcharmoffujianstyle.mapper.UserPreferenceMapper;
import com.ancientcharmoffujianstyle.service.Impl.HeritageRecommendationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HeritageRecommendationServiceTest {

    private FyinfoMapper fyinfoMapper;
    private SysUserMapper userMapper;
    private UserPreferenceMapper preferenceMapper;
    private UserFavoriteMapper favoriteMapper;
    private UserBrowseHistoryMapper browseHistoryMapper;
    private HeritageRecommendationService service;

    @BeforeEach
    void setUp() {
        fyinfoMapper = mock(FyinfoMapper.class);
        userMapper = mock(SysUserMapper.class);
        preferenceMapper = mock(UserPreferenceMapper.class);
        favoriteMapper = mock(UserFavoriteMapper.class);
        browseHistoryMapper = mock(UserBrowseHistoryMapper.class);
        service = new HeritageRecommendationService(fyinfoMapper, userMapper, preferenceMapper, favoriteMapper,
                browseHistoryMapper, Clock.fixed(Instant.parse("2026-08-12T00:00:00Z"), ZoneOffset.UTC));
        when(userMapper.selectById(1L)).thenReturn(new SysUser());
        when(favoriteMapper.selectList(any())).thenReturn(Collections.<UserFavorite>emptyList());
        when(browseHistoryMapper.selectRecent30DaysByUserId(1L))
                .thenReturn(Collections.<UserBrowseHistory>emptyList());
    }

    @Test
    void recommendsCityPreferenceWithThirtyPointsAndReason() {
        UserPreference preference = preference(1L, 3L, null);
        when(preferenceMapper.selectById(1L)).thenReturn(preference);
        when(fyinfoMapper.selectList(any())).thenReturn(Collections.singletonList(fy(1L, 3L, "传统技艺", 5L, "", "")));

        RecommendationVo recommendation = service.recommend(1L).get(0);

        assertEquals(31D, recommendation.getScore());
        assertTrue(recommendation.getReasons().contains("匹配偏好城市"));
    }

    @Test
    void recommendsCategoryPreferenceWithThirtyPointsAndReason() {
        when(preferenceMapper.selectById(1L)).thenReturn(preference(1L, null, "传统技艺"));
        when(fyinfoMapper.selectList(any())).thenReturn(Collections.singletonList(fy(1L, 2L, "传统技艺", 5L, "", "")));

        RecommendationVo recommendation = service.recommend(1L).get(0);

        assertEquals(31D, recommendation.getScore());
        assertTrue(recommendation.getReasons().contains("匹配偏好类别"));
    }

    @Test
    void appliesLinearBrowseDecayAndExplainsSameCategoryInterest() {
        Fyinfo browsed = fy(10L, 1L, "传统舞蹈", 5L, "", "");
        Fyinfo candidate = fy(11L, 2L, "传统舞蹈", 5L, "", "");
        when(fyinfoMapper.selectList(any())).thenReturn(Arrays.asList(browsed, candidate));
        when(browseHistoryMapper.selectRecent30DaysByUserId(1L))
                .thenReturn(Collections.singletonList(history(10L, LocalDateTime.of(2026, 8, 2, 0, 0))));

        RecommendationVo recommendation = findById(service.recommend(1L), 11L);

        assertEquals(-50.67D, recommendation.getScore(), 0.01D);
        assertTrue(recommendation.getReasons().contains("近期浏览同类别项目"));
    }

    @Test
    void coldStartRanksByLevelAndCompletenessWithoutBehavior() {
        Fyinfo completeHighLevel = fy(1L, 1L, "民俗", 1L, "详细介绍", "image.png");
        Fyinfo incompleteLowLevel = fy(2L, 2L, "传统技艺", 5L, "", "");
        when(fyinfoMapper.selectList(any())).thenReturn(Arrays.asList(incompleteLowLevel, completeHighLevel));

        List<RecommendationVo> recommendations = service.recommend(1L);

        assertEquals(1L, recommendations.get(0).getId());
        assertEquals(15D, recommendations.get(0).getScore());
        assertTrue(recommendations.get(0).getReasons().contains("非遗等级与资料完整度"));
    }

    @Test
    void omitsQualityReasonWhenLevelAndMaterialsProvideNoQualityScore() {
        Fyinfo noQuality = fy(1L, 1L, "民俗", 0L, "", "");
        when(fyinfoMapper.selectList(any())).thenReturn(Collections.singletonList(noQuality));

        RecommendationVo recommendation = service.recommend(null).get(0);

        assertEquals(0D, recommendation.getScore());
        assertTrue(!recommendation.getReasons().contains("非遗等级与资料完整度"));
    }

    @Test
    void recommendsColdStartWhenUserIdIsMissingWithoutReadingBehavior() {
        Fyinfo highLevel = fy(1L, 1L, "民俗", 1L, "详细介绍", "image.png");
        Fyinfo lowLevel = fy(2L, 2L, "传统技艺", 5L, "", "");
        when(fyinfoMapper.selectList(any())).thenReturn(Arrays.asList(lowLevel, highLevel));

        List<RecommendationVo> recommendations = service.recommend(null);

        assertEquals(1L, recommendations.get(0).getId());
        verify(preferenceMapper, never()).selectById(any());
        verify(favoriteMapper, never()).selectList(any());
        verify(browseHistoryMapper, never()).selectRecent30DaysByUserId(any());
    }

    @Test
    void rejectsExplicitInvalidUserId() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.recommend(0L));

        assertEquals("用户id不能为空", exception.getMessage());
        verify(fyinfoMapper, never()).selectList(any());
    }

    @Test
    void rejectsPositiveUserIdThatDoesNotExistForRecommendations() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.recommend(99L));

        assertEquals("用户不存在", exception.getMessage());
        verify(fyinfoMapper, never()).selectList(any());
    }

    @Test
    void reordersConsecutiveSameCityAndCategoryUsingDiversityPenalty() {
        when(preferenceMapper.selectById(1L)).thenReturn(preference(1L, 1L, "民俗"));
        Fyinfo first = fy(1L, 1L, "民俗", 5L, "", "");
        Fyinfo repeated = fy(2L, 1L, "民俗", 5L, "", "");
        Fyinfo diverse = fy(3L, 2L, "传统技艺", 5L, "", "");
        when(fyinfoMapper.selectList(any())).thenReturn(Arrays.asList(first, repeated, diverse));

        List<RecommendationVo> recommendations = service.recommend(1L);

        assertEquals(1L, recommendations.get(0).getId());
        assertEquals(3L, recommendations.get(1).getId());
        assertEquals(2L, recommendations.get(2).getId());
        assertTrue(!recommendations.get(2).getReasons().contains("为保证多样性降低连续同城同类项目排序"));
    }

    @Test
    void reordersConsecutiveSameCityWithDifferentCategories() {
        when(preferenceMapper.selectById(1L)).thenReturn(preference(1L, 1L, null));
        Fyinfo first = fy(1L, 1L, "民俗", 5L, "", "");
        Fyinfo sameCity = fy(2L, 1L, "传统技艺", 5L, "", "");
        Fyinfo otherCity = fy(3L, 2L, "传统舞蹈", 1L, "详细介绍", "image.png");
        when(fyinfoMapper.selectList(any())).thenReturn(Arrays.asList(first, sameCity, otherCity));

        List<RecommendationVo> recommendations = service.recommend(1L);

        assertEquals(1L, recommendations.get(0).getId());
        assertEquals(3L, recommendations.get(1).getId());
        assertEquals(2L, recommendations.get(2).getId());
    }

    @Test
    void reordersConsecutiveSameCategoryWithDifferentCities() {
        when(preferenceMapper.selectById(1L)).thenReturn(preference(1L, null, "民俗"));
        Fyinfo first = fy(1L, 1L, "民俗", 5L, "", "");
        Fyinfo sameCategory = fy(2L, 2L, "民俗", 5L, "", "");
        Fyinfo otherCategory = fy(3L, 3L, "传统舞蹈", 1L, "详细介绍", "image.png");
        when(fyinfoMapper.selectList(any())).thenReturn(Arrays.asList(first, sameCategory, otherCategory));

        List<RecommendationVo> recommendations = service.recommend(1L);

        assertEquals(1L, recommendations.get(0).getId());
        assertEquals(3L, recommendations.get(1).getId());
        assertEquals(2L, recommendations.get(2).getId());
    }

    @Test
    void doesNotLeaveDiversityReasonOnCandidateThatIsNotFinallyConsecutive() {
        when(preferenceMapper.selectById(1L)).thenReturn(preference(1L, 1L, null));
        Fyinfo first = fy(1L, 1L, "民俗", 5L, "", "");
        Fyinfo initiallyPenalized = fy(2L, 1L, "传统技艺", 5L, "", "");
        Fyinfo separator = fy(3L, 2L, "传统舞蹈", 1L, "详细介绍", "image.png");
        when(fyinfoMapper.selectList(any())).thenReturn(Arrays.asList(first, initiallyPenalized, separator));

        List<RecommendationVo> recommendations = service.recommend(1L);

        assertEquals(3L, recommendations.get(1).getId());
        RecommendationVo delayed = findById(recommendations, 2L);
        assertTrue(!delayed.getReasons().contains("与上一推荐同城或同类，已应用多样性降权"));
    }

    @Test
    void returnsAdjustedScoreAndReasonWhenFinalChoiceIsPenalizedForDiversity() {
        when(preferenceMapper.selectById(1L)).thenReturn(preference(1L, 1L, "民俗"));
        Fyinfo first = fy(1L, 1L, "民俗", 5L, "", "");
        Fyinfo repeated = fy(2L, 1L, "民俗", 5L, "", "");
        when(fyinfoMapper.selectList(any())).thenReturn(Arrays.asList(first, repeated));

        RecommendationVo penalized = service.recommend(1L).get(1);

        assertEquals(-4D, penalized.getScore());
        assertTrue(penalized.getReasons().contains("与上一推荐同城或同类，已应用多样性降权"));
    }

    @Test
    void addsFavoriteCategoryInterestAndFavoriteRelationshipReasons() {
        Fyinfo favorite = fy(1L, 1L, "传统技艺", 5L, "", "");
        Fyinfo candidate = fy(2L, 2L, "传统技艺", 5L, "", "");
        when(fyinfoMapper.selectList(any())).thenReturn(Arrays.asList(favorite, candidate));
        when(favoriteMapper.selectList(any())).thenReturn(Collections.singletonList(favoriteRecord(1L)));

        List<RecommendationVo> recommendations = service.recommend(1L);
        RecommendationVo favoriteRecommendation = findById(recommendations, 1L);
        RecommendationVo candidateRecommendation = findById(recommendations, 2L);

        assertEquals(-39D, candidateRecommendation.getScore());
        assertTrue(candidateRecommendation.getReasons().contains("收藏同类别项目"));
        assertTrue(favoriteRecommendation.getReasons().contains("已收藏项目关联"));
    }

    @Test
    void recordsBrowseHistoryForExistingFyinfo() {
        when(fyinfoMapper.selectById(2L)).thenReturn(new Fyinfo());
        when(browseHistoryMapper.insert(any())).thenReturn(1);

        service.recordBrowse(1L, 2L);

        verify(browseHistoryMapper).insert(any(UserBrowseHistory.class));
    }

    @Test
    void rejectsBrowseHistoryForPositiveUserIdThatDoesNotExistWithoutWriting() {
        when(fyinfoMapper.selectById(2L)).thenReturn(new Fyinfo());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.recordBrowse(99L, 2L));

        assertEquals("用户不存在", exception.getMessage());
        verify(browseHistoryMapper, never()).insert(any());
    }

    private RecommendationVo findById(List<RecommendationVo> recommendations, Long id) {
        for (RecommendationVo recommendation : recommendations) {
            if (id.equals(recommendation.getId())) {
                return recommendation;
            }
        }
        throw new AssertionError("未找到推荐项目");
    }

    private UserPreference preference(Long userId, Long city, String category) {
        UserPreference preference = new UserPreference();
        preference.setUserId(userId);
        preference.setCity(city);
        preference.setCategory(category);
        return preference;
    }

    private UserFavorite favoriteRecord(Long fyId) {
        UserFavorite favorite = new UserFavorite();
        favorite.setUserId(1L);
        favorite.setFyId(fyId);
        return favorite;
    }

    private UserBrowseHistory history(Long fyId, LocalDateTime browseTime) {
        UserBrowseHistory history = new UserBrowseHistory();
        history.setUserId(1L);
        history.setFyId(fyId);
        history.setBrowseTime(browseTime);
        return history;
    }

    private Fyinfo fy(Long id, Long city, String type, Long level, String dtl, String pictureUrl) {
        Fyinfo fyinfo = new Fyinfo();
        fyinfo.setId(id);
        fyinfo.setName("项目" + id);
        fyinfo.setCity(city);
        fyinfo.setType(type);
        fyinfo.setLevel(level);
        fyinfo.setDtl(dtl);
        fyinfo.setPictureUrl(pictureUrl);
        return fyinfo;
    }
}
