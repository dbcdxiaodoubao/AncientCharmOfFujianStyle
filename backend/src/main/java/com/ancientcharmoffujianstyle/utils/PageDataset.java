package com.ancientcharmoffujianstyle.utils;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

/**
 * 表格分页数据对象
 *
 *
 */
@ApiModel("表格分页数据对象")
public class PageDataset<T> implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 总记录数 */
    @ApiModelProperty("总记录数")
    private long total;

    /** 列表数据 */
    @ApiModelProperty("列表数据")
    private T rows;

    /** 消息状态码 */
    @ApiModelProperty("消息状态码")
    private int code;

    /** 消息内容 */
    @ApiModelProperty("消息内容")
    private String msg;

    /**
     * 表格数据对象
     */
    public PageDataset()
    {
    }

    /**
     * 分页
     *
     * @param list 列表数据
     * @param total 总记录数
     */
    public PageDataset(T list, long total)
    {
        this.rows = list;
        this.total = total;
        this.code = StatusCode.SUCCESS;
        this.msg = "查询成功";
    }

    public long getTotal()
    {
        return total;
    }

    public void setTotal(long total)
    {
        this.total = total;
    }

    public T getRows()
    {
        return rows;
    }

    public void setRows(T rows)
    {
        this.rows = rows;
    }

    public int getCode()
    {
        return code;
    }

    public void setCode(int code)
    {
        this.code = code;
    }

    public String getMsg()
    {
        return msg;
    }

    public void setMsg(String msg)
    {
        this.msg = msg;
    }
}
