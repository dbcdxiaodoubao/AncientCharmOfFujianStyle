package com.ancientcharmoffujianstyle.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ancientcharmoffujianstyle.domain.entity.UserFavorite;
import com.ancientcharmoffujianstyle.mapper.FyinfoMapper;
import com.ancientcharmoffujianstyle.mapper.SysUserMapper;
import com.ancientcharmoffujianstyle.mapper.UserFavoriteMapper;
import com.ancientcharmoffujianstyle.service.IUserFavoriteService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserFavoriteServiceImpl implements IUserFavoriteService {

    private final SysUserMapper userMapper;
    private final FyinfoMapper fyinfoMapper;
    private final UserFavoriteMapper favoriteMapper;

    public UserFavoriteServiceImpl(SysUserMapper userMapper, FyinfoMapper fyinfoMapper,
                                   UserFavoriteMapper favoriteMapper) {
        this.userMapper = userMapper;
        this.fyinfoMapper = fyinfoMapper;
        this.favoriteMapper = favoriteMapper;
    }

    @Override
    public boolean addFavorite(Long userId, Long fyId) {
        validateUserAndFyinfo(userId, fyId);
        LambdaQueryWrapper<UserFavorite> query = favoriteQuery(userId, fyId);
        if (favoriteMapper.selectCount(query) > 0) {
            return false;
        }
        UserFavorite favorite = new UserFavorite();
        favorite.setUserId(userId);
        favorite.setFyId(fyId);
        return favoriteMapper.insert(favorite) > 0;
    }

    @Override
    public void removeFavorite(Long userId, Long fyId) {
        validateUserAndFyinfo(userId, fyId);
        if (favoriteMapper.delete(favoriteQuery(userId, fyId)) == 0) {
            throw new IllegalArgumentException("收藏记录不存在");
        }
    }

    @Override
    public List<UserFavorite> listByUserId(Long userId) {
        validateUserId(userId);
        return favoriteMapper.selectList(new LambdaQueryWrapper<UserFavorite>()
                .eq(UserFavorite::getUserId, userId)
                .orderByDesc(UserFavorite::getCreateTime));
    }

    private void validateUserAndFyinfo(Long userId, Long fyId) {
        validateUserId(userId);
        if (fyId == null || fyId <= 0 || fyinfoMapper.selectById(fyId) == null) {
            throw new IllegalArgumentException("非遗项目不存在");
        }
    }

    private void validateUserId(Long userId) {
        if (userId == null || userId <= 0 || userMapper.selectById(userId) == null) {
            throw new IllegalArgumentException("用户不存在");
        }
    }

    private LambdaQueryWrapper<UserFavorite> favoriteQuery(Long userId, Long fyId) {
        return new LambdaQueryWrapper<UserFavorite>()
                .eq(UserFavorite::getUserId, userId)
                .eq(UserFavorite::getFyId, fyId);
    }
}
