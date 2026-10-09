package com.ancientcharmoffujianstyle.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@ApiModel("用户偏好实体")
public class UserPreference {

    @ApiModelProperty("用户id")
    @TableId
    private Long userId;

    @ApiModelProperty("偏好城市")
    private Long city;

    @ApiModelProperty("偏好非遗类别")
    private String category;

    @ApiModelProperty("更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
