package com.ancientcharmoffujianstyle.controller;


import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.ancientcharmoffujianstyle.Mapping.CheckInMapping;
import com.ancientcharmoffujianstyle.controller.base.WebController;
import com.ancientcharmoffujianstyle.domain.entity.CheckIn;
import com.ancientcharmoffujianstyle.domain.query.CheckInQuery;
import com.ancientcharmoffujianstyle.domain.query.PageQuery;
import com.ancientcharmoffujianstyle.domain.vo.CheckInDtlVo;
import com.ancientcharmoffujianstyle.domain.vo.CheckInListVo;
import com.ancientcharmoffujianstyle.service.ICheckInService;
import com.ancientcharmoffujianstyle.utils.ApiResponse;
import com.ancientcharmoffujianstyle.utils.PageDataset;
import com.ancientcharmoffujianstyle.utils.UploadUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/check-in")
@Api(tags = "打卡模块")
public class CheckInController extends WebController {

    @Autowired
    ICheckInService checkInService;

    @Autowired
    UploadUtil uploadUtil;

    @GetMapping("/byuser")
    @ApiOperation("查询打卡信息列表(通过发布者id)")
    public PageDataset<List<CheckInListVo>> checkInList(@Validated PageQuery pageQuery, Integer userId) {
        Page<CheckInListVo> page = PageHelper.startPage(pageQuery.getPageNum(), pageQuery.getPageSize());

        List<CheckInListVo> list = checkInService.selectList(userId);

        return wrapPageResult(page.getResult(),page.getTotal());
    }

    @GetMapping("/byfy")
    @ApiOperation("查询打卡信息列表(通过非遗id)")
    public PageDataset<List<CheckInListVo>> checkInListByFyId(@Validated PageQuery pageQuery, Integer fyId) {
        Page<CheckInListVo> page = PageHelper.startPage(pageQuery.getPageNum(), pageQuery.getPageSize());

        List<CheckInListVo> list = checkInService.selectListByFy(fyId);

        return wrapPageResult(page.getResult(),page.getTotal());
    }

    @GetMapping("/dtl/{id}")
    @ApiOperation("通过id查询打卡记录")
    public ApiResponse<CheckInDtlVo> dtl(@PathVariable @Validated Long id){
        if(id==null || id==0){
            return ApiResponse.error("id不能为空或者0",null);
        }
        return ApiResponse.success(checkInService.dtl(id));
    }


    @PostMapping("/upload")
    @ApiOperation("上传打卡")
    public ApiResponse upload(@RequestParam("image") MultipartFile imageFile
            ,CheckInQuery checkInQuery) throws IOException {
        String url= null;
        try {
            url = uploadUtil.uploadImage(imageFile,checkInQuery);
        }
       catch (Exception e){
            e.printStackTrace();
       }

        CheckIn create = CheckInMapping.INSTANCE.toCreate(checkInQuery);
        create.setPictureUrl(url);
        checkInService.save(create);

        return ApiResponse.success();
    }

    @DeleteMapping
    @ApiOperation("删除打卡记录(因为他修改打卡还要重新上传图片好麻烦，直接不让改想改就删了重发)")
    public ApiResponse delete(Integer id){
        checkInService.removeById(id);

        return ApiResponse.success();
    }


}
