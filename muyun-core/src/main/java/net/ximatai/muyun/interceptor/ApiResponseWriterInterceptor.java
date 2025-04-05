package net.ximatai.muyun.interceptor;

import jakarta.annotation.Priority;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.ext.WriterInterceptor;
import jakarta.ws.rs.ext.WriterInterceptorContext;
import net.ximatai.muyun.model.ApiResponse;

import java.io.IOException;

/**
 * 响应拦截器
 * 确保 ApiResponse 类型的返回值正确输出
 */
@Provider
@Priority(Integer.MAX_VALUE - 30)
public class ApiResponseWriterInterceptor implements WriterInterceptor {

    @Override
    public void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {
        // 设置正确的媒体类型
        if (context.getEntity() instanceof ApiResponse) {
            context.setMediaType(MediaType.APPLICATION_JSON_TYPE);
        }
        
        // 继续处理
        context.proceed();
    }
} 
