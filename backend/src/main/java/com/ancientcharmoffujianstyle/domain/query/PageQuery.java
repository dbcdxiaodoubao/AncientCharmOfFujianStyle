package com.ancientcharmoffujianstyle.domain.query;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@ApiModel("分页实体")
public class PageQuery {

    @ApiModelProperty(value = "分页大小",required = true)
    @NotNull(message = "分页大小不能为空")
    private Integer pageSize;

    @ApiModelProperty(value = "第几页",required = true)
    @NotNull(message = "第几页不能为空")
    private Integer pageNum;
}
