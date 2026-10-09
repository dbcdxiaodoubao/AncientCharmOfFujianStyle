package com.ancientcharmoffujianstyle.controller;

import com.ancientcharmoffujianstyle.domain.entity.UserFavorite;
import com.ancientcharmoffujianstyle.service.IUserFavoriteService;
import com.ancientcharmoffujianstyle.utils.ApiResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/favorite")
@Api(tags = "用户收藏")
public class FavoriteController {

    private final IUserFavoriteService favoriteService;

    public FavoriteController(IUserFavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @PostMapping
    @ApiOperation("收藏非遗项目")
    public ApiResponse<Void> add(@RequestParam Long userId, @RequestParam Long fyId) {
        try {
            if (!favoriteService.addFavorite(userId, fyId)) {
                return ApiResponse.error("已收藏该非遗项目", null);
            }
            return ApiResponse.success();
        } catch (IllegalArgumentException exception) {
            return ApiResponse.error(exception.getMessage(), null);
        }
    }

    @DeleteMapping
    @ApiOperation("取消收藏非遗项目")
    public ApiResponse<Void> remove(@RequestParam Long userId, @RequestParam Long fyId) {
        try {
            favoriteService.removeFavorite(userId, fyId);
            return ApiResponse.success();
        } catch (IllegalArgumentException exception) {
            return ApiResponse.error(exception.getMessage(), null);
        }
    }

    @GetMapping("/{userId}")
    @ApiOperation("查询用户收藏")
    public ApiResponse<List<UserFavorite>> list(@PathVariable Long userId) {
        try {
            return ApiResponse.success(favoriteService.listByUserId(userId));
        } catch (IllegalArgumentException exception) {
            return ApiResponse.error(exception.getMessage(), null);
        }
    }
}
