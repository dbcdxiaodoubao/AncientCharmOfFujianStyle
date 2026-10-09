package com.ancientcharmoffujianstyle.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("非遗推荐结果")
public class RecommendationVo extends FyinfoListVo {

    @ApiModelProperty("推荐评分")
    private Double score;

    @ApiModelProperty("推荐原因")
    private List<String> reasons;
}
