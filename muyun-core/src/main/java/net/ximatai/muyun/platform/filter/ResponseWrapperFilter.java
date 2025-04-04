package net.ximatai.muyun.platform.filter;

import jakarta.annotation.Priority;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import net.ximatai.muyun.model.ApiResponse;

import java.io.IOException;

/**
 * 全局响应包装过滤器
 * 将所有非 Response 类型的响应包装为统一的 ApiResponse 格式
 */
@Provider
@Priority(Integer.MAX_VALUE - 10)
public class ResponseWrapperFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        // 跳过已经是 ApiResponse 或者 Response 类型的响应
        if (responseContext.getEntity() instanceof ApiResponse) {
            return;
        }
        
        // 处理返回值
        Object entity = responseContext.getEntity();
        if (entity != null && !(entity instanceof ApiResponse)) {
            // 将普通返回值包装为 ApiResponse
            ApiResponse<?> apiResponse = ApiResponse.success(entity, "success");
            
            // 替换响应实体
            responseContext.setEntity(
                    apiResponse,
                    responseContext.getEntityAnnotations(),
                    responseContext.getMediaType()
            );
        }
        
        // 确保响应类型为 JSON
        if (responseContext.getMediaType() == null) {
            responseContext.getHeaders().putSingle("Content-Type", MediaType.APPLICATION_JSON);
        }
    }
} 
