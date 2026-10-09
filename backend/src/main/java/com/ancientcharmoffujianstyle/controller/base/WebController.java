package com.ancientcharmoffujianstyle.controller.base;

import java.beans.PropertyEditorSupport;
import java.util.Date;
import java.util.List;

import com.github.pagehelper.PageInfo;
import com.ancientcharmoffujianstyle.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;

/**
 * 古韵闽风 - Web基础控制器
 * 为非遗地图文旅平台的所有业务Controller提供：
 * 统一分页封装、接口响应格式化和日期参数自动转换等通用能力。
 */
public class WebController {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 将查询结果封装为分页数据集
     * 适配非遗列表、打卡记录等需要分页展示的场景
     */
protected PageDataset wrapPageResult(List<?> list) {
        PageDataset dataset = new PageDataset();
        dataset.setCode(StatusCode.SUCCESS);
        dataset.setMsg("查询成功");
        dataset.setRows(list);
        dataset.setTotal(new PageInfo(list).getTotal());
        return dataset;
    }

    /**
     * 将查询结果封装为分页数据集（指定总条数）
     * 当分页插件无法自动获取total时使用此方法手动传入
     */
protected PageDataset wrapPageResult(List<?> list, long total) {
        PageDataset dataset = new PageDataset();
        dataset.setCode(StatusCode.SUCCESS);
        dataset.setMsg("查询成功");
        dataset.setRows(list);
        dataset.setTotal(total);
        return dataset;
    }

    /** 返回接口处理成功 */
    public ApiResponse replyOk() {
        return ApiResponse.success();
    }

    /** 返回接口处理失败 */
    public ApiResponse replyFail() {
        return ApiResponse.error();
    }

    /** 返回成功并附带消息 */
    public ApiResponse replyOk(String message) {
        return ApiResponse.success(message);
    }

    /** 返回成功并附带数据 */
    public ApiResponse replyOk(Object data) {
        return ApiResponse.success(data);
    }

    /** 返回失败并附带消息 */
    public ApiResponse replyFail(String message) {
        return ApiResponse.error(message);
    }

    /**
     * 根据数据库操作影响行数判断成功/失败
     * @param affectedRows INSERT/UPDATE/DELETE 影响的行数
     * @return 影响行数>0返回成功，否则返回失败
     */
    protected ApiResponse judgeByRows(int affectedRows) {
        return affectedRows > 0 ? ApiResponse.success() : ApiResponse.error();
    }

    /**
     * 根据布尔值判断操作结果
     * @param succeeded 操作是否成功
     * @return 对应的ApiResponse
     */
    protected ApiResponse judgeByBool(boolean succeeded) {
        return succeeded ? replyOk() : replyFail();
    }
}
