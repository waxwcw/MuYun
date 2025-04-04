package net.ximatai.muyun.model;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * 统一API响应体
 * 用于包装所有API接口的返回数据，符合RESTful API规范
 */
@Schema(description = "API响应体")
public class ApiResponse<T> {
    @Schema(description = "状态码，200表示成功，其他表示失败")
    private int code;
    
    @Schema(description = "响应数据")
    private T data;
    
    @Schema(description = "响应消息")
    private String msg;

    /**
     * 无参构造函数
     */
    public ApiResponse() {
    }

    /**
     * 全参构造函数
     * 
     * @param code 状态码
     * @param data 响应数据
     * @param msg 响应消息
     */
    public ApiResponse(int code, T data, String msg) {
        this.code = code;
        this.data = data;
        this.msg = msg;
    }

    /**
     * 创建成功响应
     * 
     * @param data 响应数据
     * @param <T> 数据类型
     * @return 成功响应对象
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, data, "操作成功");
    }

    /**
     * 创建成功响应
     * 
     * @param data 响应数据
     * @param msg 响应消息
     * @param <T> 数据类型
     * @return 成功响应对象
     */
    public static <T> ApiResponse<T> success(T data, String msg) {
        return new ApiResponse<>(200, data, msg);
    }

    /**
     * 创建失败响应
     * 
     * @param code 错误状态码
     * @param msg 错误消息
     * @param <T> 数据类型
     * @return 失败响应对象
     */
    public static <T> ApiResponse<T> error(int code, String msg) {
        return new ApiResponse<>(code, null, msg);
    }

    /**
     * 创建自定义响应
     * 
     * @param code 状态码
     * @param data 响应数据
     * @param msg 响应消息
     * @param <T> 数据类型
     * @return 自定义响应对象
     */
    public static <T> ApiResponse<T> build(int code, T data, String msg) {
        return new ApiResponse<>(code, data, msg);
    }

    // Getter 和 Setter 方法
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
} 