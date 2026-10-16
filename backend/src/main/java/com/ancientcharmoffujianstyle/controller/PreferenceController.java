package com.ancientcharmoffujianstyle.controller;

import com.ancientcharmoffujianstyle.domain.entity.UserPreference;
import com.ancientcharmoffujianstyle.service.IUserPreferenceService;
import com.ancientcharmoffujianstyle.utils.ApiResponse;
import com.ancientcharmoffujianstyle.security.CurrentUser;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/preference")
@Api(tags = "用户偏好")
public class PreferenceController {

    private final IUserPreferenceService preferenceService;

    public PreferenceController(IUserPreferenceService preferenceService) {
        this.preferenceService = preferenceService;
    }

    @GetMapping("/{userId}")
    @ApiOperation("读取用户偏好")
    public ApiResponse<UserPreference> get(@PathVariable Long userId) {
        CurrentUser.requireSelf(userId);
        try {
            return ApiResponse.success(preferenceService.getPreference(userId));
        } catch (IllegalArgumentException exception) {
            return ApiResponse.error(exception.getMessage(), null);
        }
    }

    @PostMapping
    @ApiOperation("保存用户偏好")
    public ApiResponse<Void> save(@RequestBody UserPreference preference) {
        preference.setUserId(CurrentUser.requireSelf(preference.getUserId()));
        try {
            preferenceService.savePreference(preference);
            return ApiResponse.success();
        } catch (IllegalArgumentException exception) {
            return ApiResponse.error(exception.getMessage(), null);
        }
    }
}
