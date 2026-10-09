package com.ancientcharmoffujianstyle.service;

import com.ancientcharmoffujianstyle.domain.entity.SysUser;
import com.ancientcharmoffujianstyle.domain.entity.UserPreference;
import com.ancientcharmoffujianstyle.mapper.SysUserMapper;
import com.ancientcharmoffujianstyle.mapper.UserPreferenceMapper;
import com.ancientcharmoffujianstyle.service.Impl.UserPreferenceServiceImpl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserPreferenceServiceImplTest {

    @Test
    void savePreferenceValidatesUserAndPersistsCityAndCategory() {
        SysUserMapper userMapper = mock(SysUserMapper.class);
        UserPreferenceMapper preferenceMapper = mock(UserPreferenceMapper.class);
        UserPreferenceServiceImpl service = new UserPreferenceServiceImpl(userMapper, preferenceMapper);
        UserPreference preference = new UserPreference();
        preference.setUserId(1L);
        preference.setCity(3L);
        preference.setCategory("传统技艺");
        when(userMapper.selectById(1L)).thenReturn(new SysUser());
        when(preferenceMapper.insert(any())).thenReturn(1);

        service.savePreference(preference);

        verify(preferenceMapper).insert(preference);
    }

    @Test
    void getPreferenceRejectsMissingUser() {
        SysUserMapper userMapper = mock(SysUserMapper.class);
        UserPreferenceMapper preferenceMapper = mock(UserPreferenceMapper.class);
        UserPreferenceServiceImpl service = new UserPreferenceServiceImpl(userMapper, preferenceMapper);
        when(userMapper.selectById(1L)).thenReturn(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.getPreference(1L));

        assertEquals("用户不存在", exception.getMessage());
    }

    @Test
    void getPreferenceReturnsExistingPreference() {
        SysUserMapper userMapper = mock(SysUserMapper.class);
        UserPreferenceMapper preferenceMapper = mock(UserPreferenceMapper.class);
        UserPreferenceServiceImpl service = new UserPreferenceServiceImpl(userMapper, preferenceMapper);
        UserPreference preference = new UserPreference();
        preference.setUserId(1L);
        preference.setCity(3L);
        preference.setCategory("传统技艺");
        when(userMapper.selectById(1L)).thenReturn(new SysUser());
        when(preferenceMapper.selectById(1L)).thenReturn(preference);

        assertEquals(preference, service.getPreference(1L));
    }

    @Test
    void savePreferenceUpdatesExistingPreference() {
        SysUserMapper userMapper = mock(SysUserMapper.class);
        UserPreferenceMapper preferenceMapper = mock(UserPreferenceMapper.class);
        UserPreferenceServiceImpl service = new UserPreferenceServiceImpl(userMapper, preferenceMapper);
        UserPreference preference = new UserPreference();
        preference.setUserId(1L);
        preference.setCity(3L);
        preference.setCategory("传统技艺");
        when(userMapper.selectById(1L)).thenReturn(new SysUser());
        when(preferenceMapper.selectById(1L)).thenReturn(new UserPreference());
        when(preferenceMapper.updateById(preference)).thenReturn(1);

        service.savePreference(preference);

        verify(preferenceMapper).updateById(preference);
    }

    @Test
    void savePreferenceRejectsMissingUserWithoutWriting() {
        SysUserMapper userMapper = mock(SysUserMapper.class);
        UserPreferenceMapper preferenceMapper = mock(UserPreferenceMapper.class);
        UserPreferenceServiceImpl service = new UserPreferenceServiceImpl(userMapper, preferenceMapper);
        UserPreference preference = new UserPreference();
        preference.setUserId(1L);
        when(userMapper.selectById(1L)).thenReturn(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.savePreference(preference));

        assertEquals("用户不存在", exception.getMessage());
        verify(preferenceMapper, org.mockito.Mockito.never()).insert(any());
        verify(preferenceMapper, org.mockito.Mockito.never()).updateById(any());
    }

    @Test
    void savePreferenceRejectsEmptyPreference() {
        SysUserMapper userMapper = mock(SysUserMapper.class);
        UserPreferenceMapper preferenceMapper = mock(UserPreferenceMapper.class);
        UserPreferenceServiceImpl service = new UserPreferenceServiceImpl(userMapper, preferenceMapper);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.savePreference(null));

        assertEquals("偏好数据不能为空", exception.getMessage());
        verify(preferenceMapper, org.mockito.Mockito.never()).insert(any());
        verify(preferenceMapper, org.mockito.Mockito.never()).updateById(any());
    }
}
