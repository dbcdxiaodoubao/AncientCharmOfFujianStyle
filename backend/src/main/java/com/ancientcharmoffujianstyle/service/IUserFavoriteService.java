package com.ancientcharmoffujianstyle.service;

import com.ancientcharmoffujianstyle.domain.entity.UserFavorite;

import java.util.List;

public interface IUserFavoriteService {

    boolean addFavorite(Long userId, Long fyId);

    void removeFavorite(Long userId, Long fyId);

    List<UserFavorite> listByUserId(Long userId);
}
