package net.ximatai.muyun.platform.filter;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.ext.Provider;
import net.ximatai.muyun.MuYunConst;
import net.ximatai.muyun.core.config.MuYunConfig;
import net.ximatai.muyun.model.IRuntimeUser;
import net.ximatai.muyun.util.JwtUtil;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.logging.Logger;

import java.util.Arrays;
import java.util.List;

/**
 * JWT认证过滤器，用于拦截API请求并验证JWT令牌
 */
@Provider
@Priority(Priorities.AUTHENTICATION)
@ApplicationScoped
public class JwtAuthFilter implements ContainerRequestFilter {

    private static final Logger LOG = Logger.getLogger(JwtAuthFilter.class);
    
    // 不需要认证的路径
    private static final List<String> WHITELIST_PATHS = Arrays.asList(
            "/api/security/oauth",
            "/api/security/isValidToken",
            "/api/security/refreshToken",
            "/api/security/checkTokenExpiration",
            "/api/user/create"
    );

    @Inject
    JsonWebToken jwt;

    @Inject
    MuYunConfig config;

    @Context
    RoutingContext routingContext;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        HttpServerRequest request = routingContext.request();
        String path = request.path();
        
        // 全局JWT认证开关：如果禁用了JWT认证，所有请求都视为白名单用户
        if (!config.enableJwtAuth()) {
            LOG.debug("JWT authentication is disabled globally");
            routingContext.put(MuYunConst.CONTEXT_KEY_RUNTIME_USER, IRuntimeUser.WHITE);
            return;
        }
        
        // 如果是白名单路径，不需要认证
        if (WHITELIST_PATHS.stream().anyMatch(path::startsWith)) {
            return;
        }
        
        // 从请求头中获取令牌
        String authHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // 如果没有令牌，设置为白名单用户
            routingContext.put(MuYunConst.CONTEXT_KEY_RUNTIME_USER, IRuntimeUser.WHITE);
            return;
        }
        
        // 从令牌中提取用户信息并设置到上下文中
        IRuntimeUser user = JwtUtil.extractUser(jwt);
        routingContext.put(MuYunConst.CONTEXT_KEY_RUNTIME_USER, user);
        
        LOG.debug("User authenticated: " + user.getUsername());
    }
}
