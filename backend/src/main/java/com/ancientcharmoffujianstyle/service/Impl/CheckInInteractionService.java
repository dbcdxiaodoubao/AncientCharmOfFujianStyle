package com.ancientcharmoffujianstyle.service.Impl;

import com.ancientcharmoffujianstyle.domain.entity.CheckInComment;
import com.ancientcharmoffujianstyle.domain.entity.CheckInLike;
import com.ancientcharmoffujianstyle.domain.entity.SysUser;
import com.ancientcharmoffujianstyle.domain.vo.CheckInCommentVo;
import com.ancientcharmoffujianstyle.mapper.CheckInCommentMapper;
import com.ancientcharmoffujianstyle.mapper.CheckInLikeMapper;
import com.ancientcharmoffujianstyle.mapper.CheckInMapper;
import com.ancientcharmoffujianstyle.mapper.SysUserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 古韵闽风 - 打卡互动服务
 * 为非遗打卡动态提供点赞与评论能力。
 */
@Service
public class CheckInInteractionService {

    private static final int MAX_COMMENT_LENGTH = 500;

    @Autowired
    private CheckInLikeMapper likeMapper;

    @Autowired
    private CheckInCommentMapper commentMapper;

    @Autowired
    private CheckInMapper checkInMapper;

    @Autowired
    private SysUserMapper userMapper;

    /**
     * 点赞/取消点赞，返回最新点赞状态与点赞数
     */
    public Map<String, Object> toggleLike(Long checkinId, Long userId) {
        validateCheckInAndUser(checkinId, userId);
        CheckInLike existing = likeMapper.selectOne(new LambdaQueryWrapper<CheckInLike>()
                .eq(CheckInLike::getCheckinId, checkinId)
                .eq(CheckInLike::getUserId, userId));
        boolean liked;
        if (existing != null) {
            likeMapper.deleteById(existing.getId());
            liked = false;
        } else {
            CheckInLike like = new CheckInLike();
            like.setCheckinId(checkinId);
            like.setUserId(userId);
            likeMapper.insert(like);
            liked = true;
        }
        Map<String, Object> result = new HashMap<>();
        result.put("liked", liked);
        result.put("likeCount", countLikes(checkinId));
        return result;
    }

    public long countLikes(Long checkinId) {
        if (checkinId == null || checkinId <= 0) {
            return 0L;
        }
        Long count = likeMapper.selectCount(new LambdaQueryWrapper<CheckInLike>()
                .eq(CheckInLike::getCheckinId, checkinId));
        return count == null ? 0L : count;
    }

    /**
     * 查询某个用户在给定打卡列表范围内点赞过的打卡id
     */
    public List<Long> likedCheckinIds(Long userId, List<Long> checkinIds) {
        if (userId == null || userId <= 0 || checkinIds == null || checkinIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<CheckInLike> likes = likeMapper.selectList(new LambdaQueryWrapper<CheckInLike>()
                .eq(CheckInLike::getUserId, userId)
                .in(CheckInLike::getCheckinId, checkinIds));
        if (likes == null || likes.isEmpty()) {
            return Collections.emptyList();
        }
        return likes.stream().map(CheckInLike::getCheckinId).collect(Collectors.toList());
    }

    public List<CheckInCommentVo> listComments(Long checkinId) {
        if (checkinId == null || checkinId <= 0) {
            return Collections.emptyList();
        }
        List<CheckInComment> comments = commentMapper.selectList(new LambdaQueryWrapper<CheckInComment>()
                .eq(CheckInComment::getCheckinId, checkinId)
                .orderByAsc(CheckInComment::getCreateTime)
                .orderByAsc(CheckInComment::getId));
        return toVoList(comments);
    }

    public CheckInCommentVo addComment(Long checkinId, Long userId, String content) {
        validateCheckInAndUser(checkinId, userId);
        String text = content == null ? null : content.trim();
        if (text == null || text.isEmpty()) {
            throw new IllegalArgumentException("评论内容不能为空");
        }
        if (text.length() > MAX_COMMENT_LENGTH) {
            text = text.substring(0, MAX_COMMENT_LENGTH);
        }
        CheckInComment comment = new CheckInComment();
        comment.setCheckinId(checkinId);
        comment.setUserId(userId);
        comment.setContent(text);
        commentMapper.insert(comment);
        CheckInComment saved = commentMapper.selectById(comment.getId());
        List<CheckInCommentVo> voList = toVoList(Collections.singletonList(saved));
        return voList.isEmpty() ? null : voList.get(0);
    }

    private List<CheckInCommentVo> toVoList(List<CheckInComment> comments) {
        if (comments == null || comments.isEmpty()) {
            return Collections.emptyList();
        }
        Set<Long> userIds = new LinkedHashSet<>();
        for (CheckInComment comment : comments) {
            if (comment != null && comment.getUserId() != null) {
                userIds.add(comment.getUserId());
            }
        }
        Map<Long, String> nameById = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<SysUser> users = userMapper.selectBatchIds(userIds);
            if (users != null) {
                for (SysUser user : users) {
                    nameById.put(user.getUserId(), user.getUserName());
                }
            }
        }
        List<CheckInCommentVo> result = new ArrayList<>();
        for (CheckInComment comment : comments) {
            if (comment == null) {
                continue;
            }
            CheckInCommentVo vo = new CheckInCommentVo();
            vo.setId(comment.getId());
            vo.setCheckinId(comment.getCheckinId());
            vo.setUserId(comment.getUserId());
            vo.setContent(comment.getContent());
            vo.setCreateTime(comment.getCreateTime());
            String name = nameById.get(comment.getUserId());
            vo.setUserName(name == null ? "用户" + comment.getUserId() : name);
            result.add(vo);
        }
        return result;
    }

    private void validateCheckInAndUser(Long checkinId, Long userId) {
        if (checkinId == null || checkinId <= 0 || checkInMapper.selectById(checkinId) == null) {
            throw new IllegalArgumentException("打卡记录不存在");
        }
        if (userId == null || userId <= 0 || userMapper.selectById(userId) == null) {
            throw new IllegalArgumentException("用户不存在");
        }
    }
}
