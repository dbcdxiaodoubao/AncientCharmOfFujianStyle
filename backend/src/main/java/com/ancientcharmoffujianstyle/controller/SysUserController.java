package com.ancientcharmoffujianstyle.controller;

import com.ancientcharmoffujianstyle.Mapping.SysUserMapping;
import com.ancientcharmoffujianstyle.controller.base.WebController;
import com.ancientcharmoffujianstyle.domain.entity.SysUser;
import com.ancientcharmoffujianstyle.domain.query.LoginQuery;
import com.ancientcharmoffujianstyle.domain.query.RegisterQuery;
import com.ancientcharmoffujianstyle.domain.vo.LoginVo;
import com.ancientcharmoffujianstyle.service.ISysUserService;
import com.ancientcharmoffujianstyle.utils.ApiResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sysuser")
@Api(tags = "用户管理")
public class SysUserController extends WebController {

    @Autowired
    ISysUserService iSysUserService;

    @GetMapping("/list")
    @ApiOperation("查询用户信息列表")
    public ApiResponse<List<SysUser>> list() {

        List<SysUser> list = iSysUserService.list();

        return ApiResponse.success(list);
    }

    @PostMapping("/login")
    @ApiOperation("登录接口")
    public ApiResponse<LoginVo> login(@Validated @RequestBody LoginQuery loginQuery){
        if (!iSysUserService.login(loginQuery)){
            return ApiResponse.error("账号或密码错误",null);
        }

        return ApiResponse.success(iSysUserService.getLoginVo(loginQuery.getUserName()));
    }

    @PostMapping("/register")
    @ApiOperation("注册")
    public ApiResponse<LoginVo> register(@Validated @RequestBody RegisterQuery registerQuery){
        if (iSysUserService.haveOner(registerQuery.getUserName())!=0){
            return ApiResponse.error("用户名重复",null);
        }

        iSysUserService.register(registerQuery);

        return ApiResponse.success(iSysUserService.getLoginVo(registerQuery.getUserName()));
    }

    @GetMapping("/{id}")
    @ApiOperation("用户详情")
    public ApiResponse<SysUser> dtl(@PathVariable @Validated Long id){
        if (id<=0){
            return ApiResponse.error("id错误",null);
        }
        SysUser byId = iSysUserService.getById(id);
        SysUserMapping.INSTANCE.toDtlVo(byId);

        return ApiResponse.success(byId);
    }

}
