package net.ximatai.muyun.mapper;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import net.ximatai.muyun.util.ResponseUtil;
import org.jboss.logging.Logger;

/**
 * 资源不存在异常处理器
 * 处理所有接口的404错误，返回统一的响应格式
 */
@Provider
public class NotFoundExceptionMapper implements ExceptionMapper<NotFoundException> {
    
    private static final Logger LOG = Logger.getLogger(NotFoundExceptionMapper.class);
    
    @Override
    public Response toResponse(NotFoundException exception) {
        LOG.info("Resource not found: " + exception.getMessage());
        return ResponseUtil.notFound();
    }
} 
