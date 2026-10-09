package com.ancientcharmoffujianstyle.domain;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ancientcharmoffujianstyle.domain.entity.UserBrowseHistory;
import com.ancientcharmoffujianstyle.domain.entity.UserFavorite;
import com.ancientcharmoffujianstyle.domain.entity.UserPreference;
import com.ancientcharmoffujianstyle.mapper.UserBrowseHistoryMapper;
import com.ancientcharmoffujianstyle.mapper.UserFavoriteMapper;
import com.ancientcharmoffujianstyle.mapper.UserPreferenceMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserActivityModelStructureTest {

    @Test
    void userActivityEntitiesExposeRequiredFields() throws NoSuchFieldException {
        assertEquals(Long.class, UserFavorite.class.getDeclaredField("userId").getType());
        assertEquals(Long.class, UserFavorite.class.getDeclaredField("fyId").getType());
        assertEquals(LocalDateTime.class, UserFavorite.class.getDeclaredField("createTime").getType());
        assertEquals(Long.class, UserPreference.class.getDeclaredField("userId").getType());
        assertEquals(Long.class, UserPreference.class.getDeclaredField("city").getType());
        assertEquals(String.class, UserPreference.class.getDeclaredField("category").getType());
        assertEquals(LocalDateTime.class, UserPreference.class.getDeclaredField("updateTime").getType());
        assertEquals(Long.class, UserBrowseHistory.class.getDeclaredField("userId").getType());
        assertEquals(Long.class, UserBrowseHistory.class.getDeclaredField("fyId").getType());
        assertEquals(LocalDateTime.class, UserBrowseHistory.class.getDeclaredField("browseTime").getType());
    }

    @Test
    void activityMappersUseBaseMapperAndExposeRecentHistoryQuery() throws NoSuchMethodException {
        assertTrue(BaseMapper.class.isAssignableFrom(UserFavoriteMapper.class));
        assertTrue(BaseMapper.class.isAssignableFrom(UserPreferenceMapper.class));
        assertTrue(BaseMapper.class.isAssignableFrom(UserBrowseHistoryMapper.class));
        assertEquals(List.class, UserBrowseHistoryMapper.class
                .getMethod("selectRecent30DaysByUserId", Long.class).getReturnType());
    }
}
