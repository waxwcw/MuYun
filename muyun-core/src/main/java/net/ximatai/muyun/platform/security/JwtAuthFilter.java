package net.ximatai.muyun.platform.security;

import io.quarkus.security.UnauthorizedException;
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
import net.ximatai.muyun.model.IRuntimeUser;
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
            "/api/user/create"
    );

    @Inject
    JsonWebToken jwt;

    @Context
    RoutingContext routingContext;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        HttpServerRequest request = routingContext.request();
        String path = request.path();
        
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
        IRuntimeUser user = JwtUtils.extractUser(jwt);
        routingContext.put(MuYunConst.CONTEXT_KEY_RUNTIME_USER, user);
        
        LOG.debug("User authenticated: " + user.getUsername());
    }
}
