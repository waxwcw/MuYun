package net.ximatai.muyun.utils;

import io.vertx.core.json.JsonObject;
import jakarta.ws.rs.core.Response;
import net.ximatai.muyun.model.ApiResponse;

/**
 * 响应工具类
 * 用于构建统一格式的响应对象
 */
public class ResponseUtil {

    /**
     * 创建成功响应
     * 
     * @param data 响应数据
     * @return Response对象
     */
    public static Response success(Object data) {
        return Response.ok(ApiResponse.success(data)).build();
    }

    /**
     * 创建成功响应
     * 
     * @param data 响应数据
     * @param msg 响应消息
     * @return Response对象
     */
    public static Response success(Object data, String msg) {
        return Response.ok(ApiResponse.success(data, msg)).build();
    }

    /**
     * 创建错误响应
     * 
     * @param status HTTP状态码
     * @param code 业务错误码
     * @param msg 错误消息
     * @return Response对象
     */
    public static Response error(Response.Status status, int code, String msg) {
        return Response.status(status).entity(ApiResponse.error(code, msg)).build();
    }

    /**
     * 创建服务器内部错误响应
     * 
     * @param msg 错误消息
     * @return Response对象
     */
    public static Response serverError(String msg) {
        return error(Response.Status.INTERNAL_SERVER_ERROR, 500, msg);
    }

    /**
     * 创建未授权错误响应
     * 
     * @param msg 错误消息
     * @return Response对象
     */
    public static Response unauthorized(String msg) {
        return error(Response.Status.UNAUTHORIZED, 401, msg);
    }

    /**
     * 创建请求参数错误响应
     * 
     * @param msg 错误消息
     * @return Response对象
     */
    public static Response badRequest(String msg) {
        return error(Response.Status.BAD_REQUEST, 400, msg);
    }

    /**
     * 创建资源不存在错误响应
     * 
     * @param msg 错误消息
     * @return Response对象
     */
    public static Response notFound(String msg) {
        return error(Response.Status.NOT_FOUND, 404, "资源不存在: " + msg);
    }
    
    /**
     * 创建默认404错误响应
     * 
     * @return Response对象
     */
    public static Response notFound() {
        return error(Response.Status.NOT_FOUND, 404, "资源不存在: HTTP 404 Not Found");
    }
} 