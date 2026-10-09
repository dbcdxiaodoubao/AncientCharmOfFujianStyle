package com.ancientcharmoffujianstyle.utils;

import java.lang.management.ManagementFactory;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * 古韵闽风 - 日期时间工具
 * 为非遗打卡记录的时间戳处理、路线规划的时间计算提供统一方法。
 * 不依赖第三方库，仅使用Java标准时间API。
 */
public class DateUtils {

    public static final String FMT_YEAR = "yyyy";
    public static final String FMT_YEAR_MONTH = "yyyy-MM";
    public static final String FMT_DATE = "yyyy-MM-dd";
    public static final String FMT_DATETIME_COMPACT = "yyyyMMddHHmmss";
    public static final String FMT_DATETIME = "yyyy-MM-dd HH:mm:ss";

    private static final String[] PARSE_FORMATS = {
        "yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy-MM",
        "yyyy/MM/dd", "yyyy/MM/dd HH:mm:ss", "yyyy/MM/dd HH:mm", "yyyy/MM",
        "yyyy.MM.dd", "yyyy.MM.dd HH:mm:ss", "yyyy.MM.dd HH:mm", "yyyy.MM"
    };

    /** 一天毫秒数 */
    private static final long MS_PER_DAY = 24L * 60 * 60 * 1000;
    /** 一小时毫秒数 */
    private static final long MS_PER_HOUR = 60L * 60 * 1000;
    /** 一分钟毫秒数 */
    private static final long MS_PER_MINUTE = 60L * 1000;

    public static Date now() {
        return new Date();
    }

    public static String today() {
        return format(now(), FMT_DATE);
    }

    public static String nowStr() {
        return format(now(), FMT_DATETIME);
    }

    public static String format(Date date, String pattern) {
        if (date == null) return "";
        return new SimpleDateFormat(pattern).format(date);
    }

    public static Date parse(String text, String pattern) {
        if (text == null || text.isEmpty()) return null;
        try {
            return new SimpleDateFormat(pattern).parse(text);
        } catch (Exception e) {
            throw new IllegalArgumentException("日期解析失败: " + text + " (格式:" + pattern + ")", e);
        }
    }

    /**
     * 自动尝试多种格式解析日期字符串
     * 用于处理前端传入的各类日期格式
     */
    public static Date parseAuto(String text) {
        if (text == null || text.isEmpty()) return null;
        for (String fmt : PARSE_FORMATS) {
            try {
                return new SimpleDateFormat(fmt).parse(text);
            } catch (Exception ignored) { }
        }
        return null;
    }

    /** 生成日期路径，如 2024/03/15，用于图片按日期分目录存储 */
    public static String datePath() {
        Date now = now();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        return sdf.format(now);
    }

    /** 生成紧凑日期，如 20240315 */
    public static String dateCompact() {
        return new SimpleDateFormat("yyyyMMdd").format(now());
    }

    /** 获取服务器启动时间 */
    public static Date serverStartTime() {
        return new Date(ManagementFactory.getRuntimeMXBean().getStartTime());
    }

    /** 计算两个日期相差的天数 */
    public static int daysBetween(Date from, Date to) {
        long diff = Math.abs(to.getTime() - from.getTime());
        return (int) (diff / MS_PER_DAY);
    }

    /**
     * 计算时间间隔的人类可读表示
     * 用于展示打卡时间与当前时间的间隔，如"3天2小时15分钟"
     */
    public static String timeSpan(Date from, Date to) {
        long diff = Math.abs(to.getTime() - from.getTime());
        long days = diff / MS_PER_DAY;
        long hours = (diff % MS_PER_DAY) / MS_PER_HOUR;
        long minutes = (diff % MS_PER_HOUR) / MS_PER_MINUTE;

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("天");
        if (hours > 0) sb.append(hours).append("小时");
        sb.append(minutes).append("分钟");
        return sb.toString();
    }

    /** LocalDateTime 转 Date */
    public static Date fromLocalDateTime(LocalDateTime ldt) {
        return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
    }

    /** LocalDate 转 Date（当天00:00:00） */
    public static Date fromLocalDate(LocalDate ld) {
        return Date.from(ld.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /** Date 转 LocalDateTime */
    public static LocalDateTime toLocalDateTime(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
}
