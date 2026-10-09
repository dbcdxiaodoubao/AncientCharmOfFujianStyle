package com.ancientcharmoffujianstyle.utils;

/**
 * 古韵闽风 - 接口响应状态码
 * 为非遗文旅平台前后端交互提供统一的语义化状态标识。
 *
 * @author ancientcharm
 */
public final class StatusCode {

    private StatusCode() { }

    // ---- 成功 ----
    /** 请求处理成功 */
    public static final int SUCCESS = 200;
    /** 资源新建成功（如打卡记录上传、用户注册） */
    public static final int CREATED = 201;
    /** 请求已受理，异步处理中 */
    public static final int ACCEPTED = 202;
    /** 操作成功但无返回内容（如删除打卡记录） */
    public static final int NO_CONTENT = 204;

    // ---- 客户端错误 ----
    /** 请求参数格式错误或缺失必填字段 */
    public static final int BAD_REQUEST = 400;
    /** 未登录或登录凭证已过期 */
    public static final int UNAUTHORIZED = 401;
    /** 无权限访问该资源 */
    public static final int FORBIDDEN = 403;
    /** 请求的非遗项目或打卡记录不存在 */
    public static final int NOT_FOUND = 404;
    /** 用户名已被注册 */
    public static final int CONFLICT = 409;
    /** 不支持的请求数据类型 */
    public static final int UNSUPPORTED_MEDIA = 415;

    // ---- 服务端错误 ----
    /** 服务器内部错误 */
    public static final int INTERNAL_ERROR = 500;
    /** 功能尚未实现 */
    public static final int NOT_IMPLEMENTED = 501;

    /**
     * 判断状态码是否表示成功
     */
    public static boolean isSuccess(int code) {
        return code >= 200 && code < 300;
    }
}
