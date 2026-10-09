package com.ancientcharmoffujianstyle.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ancientcharmoffujianstyle.domain.entity.Fyinfo;
import com.ancientcharmoffujianstyle.domain.vo.FyinfoListVo;
import org.springframework.stereotype.Service;

import java.util.List;


public interface IFyinfoService extends IService<Fyinfo> {

    /**
     * 通过所属城市查询非遗信息列表
     * @param city
     * @return
     */
    List<FyinfoListVo> listByCity(Long city);
}
