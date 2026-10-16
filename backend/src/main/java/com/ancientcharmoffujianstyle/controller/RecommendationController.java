package com.ancientcharmoffujianstyle.controller;

import com.ancientcharmoffujianstyle.domain.vo.RecommendationVo;
import com.ancientcharmoffujianstyle.service.Impl.HeritageRecommendationService;
import com.ancientcharmoffujianstyle.utils.ApiResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import com.ancientcharmoffujianstyle.security.CurrentUser;

@RestController
@RequestMapping("/recommendation")
@Api(tags = "非遗推荐")
public class RecommendationController {

    private final HeritageRecommendationService recommendationService;

    public RecommendationController(HeritageRecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping
    @ApiOperation("获取个性化非遗推荐")
    public ApiResponse<List<RecommendationVo>> recommend(@RequestParam(required = false) Long userId) {
        Long verifiedUserId = CurrentUser.optionalSelf(userId);
        try {
            return ApiResponse.success(recommendationService.recommend(verifiedUserId));
        } catch (IllegalArgumentException exception) {
            return ApiResponse.error(exception.getMessage(), null);
        }
    }

    @PostMapping("/browse")
    @ApiOperation("记录非遗浏览足迹")
    public ApiResponse<Void> recordBrowse(@RequestParam Long userId, @RequestParam Long fyId) {
        CurrentUser.requireSelf(userId);
        try {
            recommendationService.recordBrowse(userId, fyId);
            return ApiResponse.success();
        } catch (IllegalArgumentException exception) {
            return ApiResponse.error(exception.getMessage(), null);
        }
    }
}
