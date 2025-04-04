package net.ximatai.muyun.platform.controller;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import net.ximatai.muyun.utils.ResponseUtil;
import org.eclipse.microprofile.openapi.annotations.Operation;

/**
 * 兜底控制器
 * 处理所有未匹配到的路由请求
 */
@Path("/{path:.*}")
public class FallbackController {

    @GET
    @Operation(summary = "处理所有未匹配的GET请求")
    public Response handleNotFound() {
        return ResponseUtil.notFound();
    }
} 