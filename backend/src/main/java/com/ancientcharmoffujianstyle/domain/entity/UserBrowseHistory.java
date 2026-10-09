package com.ancientcharmoffujianstyle.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@ApiModel("用户浏览足迹实体")
public class UserBrowseHistory {

    @ApiModelProperty("浏览记录id")
    @TableId(type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("用户id")
    private Long userId;

    @ApiModelProperty("非遗项目id")
    private Long fyId;

    @ApiModelProperty("浏览时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime browseTime;
}
