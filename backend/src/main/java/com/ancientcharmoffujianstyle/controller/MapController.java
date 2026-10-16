package com.ancientcharmoffujianstyle.controller;

import com.ancientcharmoffujianstyle.Mapping.FyinfoMapping;
import com.ancientcharmoffujianstyle.controller.base.WebController;
import com.ancientcharmoffujianstyle.domain.entity.Fyinfo;
import com.ancientcharmoffujianstyle.domain.vo.FyinfoDtlVo;
import com.ancientcharmoffujianstyle.domain.vo.FyinfoListVo;
import com.ancientcharmoffujianstyle.service.IFyinfoService;
import com.ancientcharmoffujianstyle.utils.ApiResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/map")
@Api(tags = "地图管理")
public class MapController extends WebController {

    @Autowired
    IFyinfoService fyinfoService;

    @GetMapping
    @ApiOperation("获取非遗信息列表")
    public ApiResponse<List<FyinfoListVo>> list(){
        List<FyinfoListVo> list = fyinfoService.listByCity(0L);

        return ApiResponse.success(list);
    }

    @GetMapping("/{city}")
    @ApiOperation("通过所属城市获取非遗信息列表")
    public ApiResponse<List<FyinfoListVo>> listByCity(@PathVariable Long city){
        List<FyinfoListVo> list = fyinfoService.listByCity(city);

        return ApiResponse.success(list);
    }

    @GetMapping("/dtl/{id}")
    @ApiOperation("查询非遗详情")
    public ApiResponse<FyinfoDtlVo> dtl(@PathVariable @Validated Long id){
        Fyinfo byId = fyinfoService.getById(id);
        if (byId == null) return ApiResponse.error("非遗项目不存在", null);
        FyinfoDtlVo dtlVo = new FyinfoDtlVo();
        dtlVo.setId(id);
        dtlVo.setCity(byId.getCity());
        dtlVo.setName(byId.getName());
        dtlVo.setDtl(byId.getDtl());
        dtlVo.setType(byId.getType());
        dtlVo.setLevel(byId.getLevel());
        dtlVo.setPictureUrl(byId.getPictureUrl());
        return ApiResponse.success(dtlVo);
    }

}
