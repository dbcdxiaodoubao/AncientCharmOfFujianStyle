package com.ancientcharmoffujianstyle.controller;


import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.ancientcharmoffujianstyle.Mapping.CheckInMapping;
import com.ancientcharmoffujianstyle.controller.base.WebController;
import com.ancientcharmoffujianstyle.domain.entity.CheckIn;
import com.ancientcharmoffujianstyle.domain.query.CheckInQuery;
import com.ancientcharmoffujianstyle.domain.query.CommentQuery;
import com.ancientcharmoffujianstyle.domain.query.PageQuery;
import com.ancientcharmoffujianstyle.domain.vo.CheckInCommentVo;
import com.ancientcharmoffujianstyle.domain.vo.CheckInDtlVo;
import com.ancientcharmoffujianstyle.domain.vo.CheckInListVo;
import com.ancientcharmoffujianstyle.service.ICheckInService;
import com.ancientcharmoffujianstyle.service.Impl.CheckInInteractionService;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/check-in")
@Api(tags = "打卡模块")
public class CheckInController extends WebController {

    @Autowired
    ICheckInService checkInService;

    @Autowired
    UploadUtil uploadUtil;

    @Autowired
    CheckInInteractionService interactionService;

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

    @PostMapping("/like")
    @ApiOperation("点赞/取消点赞")
    public ApiResponse<Map<String, Object>> toggleLike(@RequestParam Long checkinId, @RequestParam Long userId) {
        try {
            return ApiResponse.success(interactionService.toggleLike(checkinId, userId));
        } catch (IllegalArgumentException exception) {
            return ApiResponse.error(exception.getMessage(), null);
        }
    }

    @GetMapping("/liked")
    @ApiOperation("查询当前用户在指定打卡范围内点赞过的记录id")
    public ApiResponse<List<Long>> liked(@RequestParam Long userId,
                                         @RequestParam(required = false) String checkinIds) {
        return ApiResponse.success(interactionService.likedCheckinIds(userId, parseIds(checkinIds)));
    }

    @GetMapping("/comments")
    @ApiOperation("查询打卡评论")
    public ApiResponse<List<CheckInCommentVo>> comments(@RequestParam Long checkinId) {
        return ApiResponse.success(interactionService.listComments(checkinId));
    }

    @PostMapping("/comment")
    @ApiOperation("发表评论")
    public ApiResponse<CheckInCommentVo> comment(@RequestBody CommentQuery query) {
        try {
            return ApiResponse.success(interactionService.addComment(
                    query.getCheckinId(), query.getUserId(), query.getContent()));
        } catch (IllegalArgumentException exception) {
            return ApiResponse.error(exception.getMessage(), null);
        }
    }

    private List<Long> parseIds(String checkinIds) {
        List<Long> ids = new ArrayList<>();
        if (checkinIds == null || checkinIds.trim().isEmpty()) {
            return ids;
        }
        for (String part : checkinIds.split(",")) {
            String value = part.trim();
            if (value.isEmpty()) {
                continue;
            }
            try {
                ids.add(Long.valueOf(value));
            } catch (NumberFormatException ignored) {
                // 忽略非法id
            }
        }
        return ids;
    }
}
