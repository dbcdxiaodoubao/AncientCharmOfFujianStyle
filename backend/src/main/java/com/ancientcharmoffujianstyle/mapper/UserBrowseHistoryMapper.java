package com.ancientcharmoffujianstyle.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ancientcharmoffujianstyle.domain.entity.UserBrowseHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserBrowseHistoryMapper extends BaseMapper<UserBrowseHistory> {

    /**
     * 查询用户最近30天的浏览记录，按浏览时间倒序排列。
     *
     * @param userId 用户id
     * @return 浏览记录列表
     */
    default List<UserBrowseHistory> selectRecent30DaysByUserId(Long userId) {
        return selectSince(userId, java.time.LocalDateTime.now().minusDays(30));
    }

    List<UserBrowseHistory> selectSince(@Param("userId") Long userId,
                                      @Param("since") java.time.LocalDateTime since);
}
