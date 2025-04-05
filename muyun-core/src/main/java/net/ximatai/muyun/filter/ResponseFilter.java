package net.ximatai.muyun.filter;

import jakarta.annotation.Priority;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.Provider;
import net.ximatai.muyun.model.ApiResponse;

import java.io.IOException;

/**
 * 响应过滤器
 * 确保所有响应都使用统一的格式
 */
@Provider
@Priority(Integer.MAX_VALUE - 20)
public class ResponseFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        // 如果响应实体不是ApiResponse类型，且状态码是404，包装为统一格式
        if (responseContext.getStatus() == 404 && !(responseContext.getEntity() instanceof ApiResponse)) {
            responseContext.setEntity(
                    ApiResponse.error(404, "资源不存在: HTTP 404 Not Found"),
                    responseContext.getEntityAnnotations(),
                    responseContext.getMediaType()
            );
        }
        
        // 确保响应类型为JSON
        if (responseContext.getMediaType() == null) {
            responseContext.getHeaders().putSingle("Content-Type", MediaType.APPLICATION_JSON);
        }
    }
} 
