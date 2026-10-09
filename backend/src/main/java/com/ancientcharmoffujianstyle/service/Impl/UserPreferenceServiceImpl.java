package com.ancientcharmoffujianstyle.service.Impl;

import com.ancientcharmoffujianstyle.domain.entity.UserPreference;
import com.ancientcharmoffujianstyle.mapper.SysUserMapper;
import com.ancientcharmoffujianstyle.mapper.UserPreferenceMapper;
import com.ancientcharmoffujianstyle.service.IUserPreferenceService;
import org.springframework.stereotype.Service;

@Service
public class UserPreferenceServiceImpl implements IUserPreferenceService {

    private final SysUserMapper userMapper;
    private final UserPreferenceMapper preferenceMapper;

    public UserPreferenceServiceImpl(SysUserMapper userMapper, UserPreferenceMapper preferenceMapper) {
        this.userMapper = userMapper;
        this.preferenceMapper = preferenceMapper;
    }

    @Override
    public UserPreference getPreference(Long userId) {
        validateUser(userId);
        return preferenceMapper.selectById(userId);
    }

    @Override
    public void savePreference(UserPreference preference) {
        if (preference == null) {
            throw new IllegalArgumentException("偏好数据不能为空");
        }
        validateUser(preference.getUserId());
        if (preferenceMapper.selectById(preference.getUserId()) == null) {
            preferenceMapper.insert(preference);
        } else {
            preferenceMapper.updateById(preference);
        }
    }

    private void validateUser(Long userId) {
        if (userId == null || userId <= 0 || userMapper.selectById(userId) == null) {
            throw new IllegalArgumentException("用户不存在");
        }
    }
}
