package net.ximatai.muyun.platform.exception;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import net.ximatai.muyun.utils.ResponseUtil;
import org.jboss.logging.Logger;

/**
 * 全局异常处理器
 * 处理所有未被其他异常处理器捕获的异常，返回统一的响应格式
 */
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {
    
    private static final Logger LOG = Logger.getLogger(GlobalExceptionMapper.class);
    
    @Override
    public Response toResponse(Throwable exception) {
        // 如果是WebApplicationException，保留其状态码
        if (exception instanceof WebApplicationException webEx) {
            Response originalResponse = webEx.getResponse();
            int statusCode = originalResponse.getStatus();
            
            LOG.error("WebApplicationException: " + exception.getMessage(), exception);
            
            return switch (statusCode) {
                case 404 -> ResponseUtil.notFound();
                case 401 -> ResponseUtil.unauthorized("未授权访问");
                case 403 -> ResponseUtil.error(Response.Status.FORBIDDEN, 403, "禁止访问");
                case 400 -> ResponseUtil.badRequest("请求参数错误");
                default -> ResponseUtil.serverError("服务器内部错误");
            };
        }
        
        // 其他未知异常，统一返回500错误
        LOG.error("Unhandled exception: " + exception.getMessage(), exception);
        return ResponseUtil.serverError("服务器内部错误");
    }
} 