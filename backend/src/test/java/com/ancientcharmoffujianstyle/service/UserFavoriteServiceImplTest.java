package com.ancientcharmoffujianstyle.service;

import com.ancientcharmoffujianstyle.domain.entity.Fyinfo;
import com.ancientcharmoffujianstyle.domain.entity.SysUser;
import com.ancientcharmoffujianstyle.domain.entity.UserFavorite;
import com.ancientcharmoffujianstyle.mapper.FyinfoMapper;
import com.ancientcharmoffujianstyle.mapper.SysUserMapper;
import com.ancientcharmoffujianstyle.mapper.UserFavoriteMapper;
import com.ancientcharmoffujianstyle.service.Impl.UserFavoriteServiceImpl;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserFavoriteServiceImplTest {

    @Test
    void addFavoriteInsertsOnlyWhenUserProjectAndFavoriteAreValid() {
        SysUserMapper userMapper = mock(SysUserMapper.class);
        FyinfoMapper fyinfoMapper = mock(FyinfoMapper.class);
        UserFavoriteMapper favoriteMapper = mock(UserFavoriteMapper.class);
        UserFavoriteServiceImpl service = new UserFavoriteServiceImpl(userMapper, fyinfoMapper, favoriteMapper);
        when(userMapper.selectById(1L)).thenReturn(new SysUser());
        when(fyinfoMapper.selectById(2L)).thenReturn(new Fyinfo());
        when(favoriteMapper.selectCount(any())).thenReturn(0L);
        when(favoriteMapper.insert(any())).thenReturn(1);

        assertTrue(service.addFavorite(1L, 2L));
        verify(favoriteMapper).insert(any());
    }

    @Test
    void addFavoriteDoesNotInsertDuplicate() {
        SysUserMapper userMapper = mock(SysUserMapper.class);
        FyinfoMapper fyinfoMapper = mock(FyinfoMapper.class);
        UserFavoriteMapper favoriteMapper = mock(UserFavoriteMapper.class);
        UserFavoriteServiceImpl service = new UserFavoriteServiceImpl(userMapper, fyinfoMapper, favoriteMapper);
        when(userMapper.selectById(1L)).thenReturn(new SysUser());
        when(fyinfoMapper.selectById(2L)).thenReturn(new Fyinfo());
        when(favoriteMapper.selectCount(any())).thenReturn(1L);

        assertFalse(service.addFavorite(1L, 2L));
        verify(favoriteMapper, never()).insert(any());
    }

    @Test
    void addFavoriteRejectsMissingUserWithoutWriting() {
        SysUserMapper userMapper = mock(SysUserMapper.class);
        FyinfoMapper fyinfoMapper = mock(FyinfoMapper.class);
        UserFavoriteMapper favoriteMapper = mock(UserFavoriteMapper.class);
        UserFavoriteServiceImpl service = new UserFavoriteServiceImpl(userMapper, fyinfoMapper, favoriteMapper);
        when(userMapper.selectById(1L)).thenReturn(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.addFavorite(1L, 2L));

        assertTrue(exception.getMessage().contains("用户不存在"));
        verify(favoriteMapper, never()).insert(any());
    }

    @Test
    void addFavoriteRejectsMissingFyinfoWithoutWriting() {
        SysUserMapper userMapper = mock(SysUserMapper.class);
        FyinfoMapper fyinfoMapper = mock(FyinfoMapper.class);
        UserFavoriteMapper favoriteMapper = mock(UserFavoriteMapper.class);
        UserFavoriteServiceImpl service = new UserFavoriteServiceImpl(userMapper, fyinfoMapper, favoriteMapper);
        when(userMapper.selectById(1L)).thenReturn(new SysUser());
        when(fyinfoMapper.selectById(2L)).thenReturn(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.addFavorite(1L, 2L));

        assertEquals("非遗项目不存在", exception.getMessage());
        verify(favoriteMapper, never()).insert(any());
    }

    @Test
    void removeFavoriteDeletesExistingFavorite() {
        SysUserMapper userMapper = mock(SysUserMapper.class);
        FyinfoMapper fyinfoMapper = mock(FyinfoMapper.class);
        UserFavoriteMapper favoriteMapper = mock(UserFavoriteMapper.class);
        UserFavoriteServiceImpl service = new UserFavoriteServiceImpl(userMapper, fyinfoMapper, favoriteMapper);
        when(userMapper.selectById(1L)).thenReturn(new SysUser());
        when(fyinfoMapper.selectById(2L)).thenReturn(new Fyinfo());
        when(favoriteMapper.delete(any())).thenReturn(1);

        service.removeFavorite(1L, 2L);

        verify(favoriteMapper).delete(any());
    }

    @Test
    void removeFavoriteRejectsMissingFavorite() {
        SysUserMapper userMapper = mock(SysUserMapper.class);
        FyinfoMapper fyinfoMapper = mock(FyinfoMapper.class);
        UserFavoriteMapper favoriteMapper = mock(UserFavoriteMapper.class);
        UserFavoriteServiceImpl service = new UserFavoriteServiceImpl(userMapper, fyinfoMapper, favoriteMapper);
        when(userMapper.selectById(1L)).thenReturn(new SysUser());
        when(fyinfoMapper.selectById(2L)).thenReturn(new Fyinfo());
        when(favoriteMapper.delete(any())).thenReturn(0);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.removeFavorite(1L, 2L));

        assertEquals("收藏记录不存在", exception.getMessage());
    }

    @Test
    void listByUserIdReturnsFavoritesForExistingUser() {
        SysUserMapper userMapper = mock(SysUserMapper.class);
        FyinfoMapper fyinfoMapper = mock(FyinfoMapper.class);
        UserFavoriteMapper favoriteMapper = mock(UserFavoriteMapper.class);
        UserFavoriteServiceImpl service = new UserFavoriteServiceImpl(userMapper, fyinfoMapper, favoriteMapper);
        UserFavorite first = new UserFavorite();
        UserFavorite second = new UserFavorite();
        List<UserFavorite> favorites = Arrays.asList(first, second);
        when(userMapper.selectById(1L)).thenReturn(new SysUser());
        when(favoriteMapper.selectList(any())).thenReturn(favorites);

        assertEquals(favorites, service.listByUserId(1L));
        verify(favoriteMapper).selectList(any());
    }

    @Test
    void listByUserIdRejectsInvalidUserId() {
        SysUserMapper userMapper = mock(SysUserMapper.class);
        FyinfoMapper fyinfoMapper = mock(FyinfoMapper.class);
        UserFavoriteMapper favoriteMapper = mock(UserFavoriteMapper.class);
        UserFavoriteServiceImpl service = new UserFavoriteServiceImpl(userMapper, fyinfoMapper, favoriteMapper);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.listByUserId(0L));

        assertEquals("用户不存在", exception.getMessage());
        verify(favoriteMapper, never()).selectList(any());
    }

    @Test
    void listByUserIdRejectsMissingUser() {
        SysUserMapper userMapper = mock(SysUserMapper.class);
        FyinfoMapper fyinfoMapper = mock(FyinfoMapper.class);
        UserFavoriteMapper favoriteMapper = mock(UserFavoriteMapper.class);
        UserFavoriteServiceImpl service = new UserFavoriteServiceImpl(userMapper, fyinfoMapper, favoriteMapper);
        when(userMapper.selectById(1L)).thenReturn(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.listByUserId(1L));

        assertEquals("用户不存在", exception.getMessage());
        verify(favoriteMapper, never()).selectList(any());
    }
}
