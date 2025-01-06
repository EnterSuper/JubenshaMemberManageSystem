/**
 * 功能：
 * 作者：Olivia
 * 日期：2024/5/30 20:33
 */

package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("takes")
public class Takes {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String year;
    private String semester;
    private Integer start_week;
    private Integer end_week;
    private Integer course_id;
    private String title;
    private Float credits;
    private String type;
    private Integer grade;
    private String time;
    private String Monday;
    private String Tuesday;
    private String Wednesday;
    private String Thursday;
    private String Friday;
    private String Saturday;
    private String Sunday;
    private Integer time_id;
}
