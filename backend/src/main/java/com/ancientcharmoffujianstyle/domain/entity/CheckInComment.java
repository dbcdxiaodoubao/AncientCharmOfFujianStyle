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
@ApiModel("打卡评论实体")
public class CheckInComment {

    @ApiModelProperty("评论id")
    @TableId(type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("打卡记录id")
    private Long checkinId;

    @ApiModelProperty("评论用户id")
    private Long userId;

    @ApiModelProperty("评论内容")
    private String content;

    @ApiModelProperty("评论时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
