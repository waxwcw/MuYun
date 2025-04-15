package net.ximatai.muyun.controller;

import io.quarkus.runtime.Startup;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import net.ximatai.muyun.method.RuntimeMethod;
import net.ximatai.muyun.model.ApiRequest;
import net.ximatai.muyun.model.IRuntimeUser;
import net.ximatai.muyun.util.JwtUtil;
import net.ximatai.muyun.util.ResponseUtil;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.jboss.logging.Logger;

import java.util.Map;

@Startup
@Path("/security")
@PermitAll
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SecurityController implements RuntimeMethod {

    @Inject
    RoutingContext routingContext;

    @Inject
    UserController userController;

    @Inject
    JsonWebToken jwt;

    private static final Logger LOG = Logger.getLogger(SecurityController.class);

    @POST
    @Path("/oauth")
    @Operation(
            summary = "用户登陆"
    )
    @APIResponse(
            responseCode = "200",
            description = "登录成功，返回JWT令牌",
            content = @Content(mediaType = MediaType.APPLICATION_JSON)
    )
    @APIResponse(
            responseCode = "401",
            description = "登录失败，用户名或密码错误"
    )
    public Response oauth(Map userMap) {
        try {
            ApiRequest apiRequest = getApiRequest();
            
            // 验证请求参数
            if (userMap == null || userMap.get("account") == null || userMap.get("password") == null) {
                LOG.warn("Login failed: Missing required parameters");
                return ResponseUtil.badRequest("缺少必要参数");
            }

            // 查询用户信息
            Map<String, Object> userInfo = userController.check(String.valueOf(userMap.get("account")));

            // 验证用户是否存在
            if (userInfo == null) {
                apiRequest.setError(new RuntimeException("该用户不存在：%s".formatted(userMap.get("account"))));
                LOG.warn("该用户不存在：" + userMap.get("account"));
                return ResponseUtil.unauthorized("用户名或密码错误");
            }
            
            // 验证密码
            String password = userInfo.get("password").toString();
            if (!password.equals(userMap.get("password"))) {
                LOG.warn("Login failed: Invalid password for user - " + userMap.get("account"));
                return ResponseUtil.unauthorized("用户名或密码错误");
            }

            // 构建用户对象
            IRuntimeUser user = IRuntimeUser.build(new JsonObject()
                    .put("id", userInfo.get("id"))
                    .put("name", userInfo.get("account"))
                    .put("username", userInfo.get("nickname"))
            );
            
            // 生成JWT令牌
            String token = JwtUtil.generateToken(user);
            
            // 构建返回数据
            JsonObject result = new JsonObject()
                    .put("token", token)
                    .put("UserInfo", new JsonObject()
                            .put("userId", userInfo.get("id"))
                            .put("avatar_url", userInfo.get("avatar_url"))
                            .put("nickname", userInfo.get("nickname"))
                    );
            
            LOG.info("User logged in successfully: " + user.getUsername());
            return ResponseUtil.success(result, "登录成功");
            
        } catch (Exception e) {
            LOG.error("Login error", e);
            return ResponseUtil.serverError("服务器内部错误");
        }
    }

    @POST
    @Path("/logout")
    @Operation(
            summary = "用户登出"
    )
    @APIResponse(
            responseCode = "200",
            description = "登出成功",
            content = @Content(mediaType = MediaType.APPLICATION_JSON)
    )
    public Response logout() {
        try {
            // 获取当前用户信息
            IRuntimeUser user = getUser();
            
            if (user == null || IRuntimeUser.WHITE.getId().equals(user.getId())) {
                // 用户未登录或已是匿名用户
                return ResponseUtil.success(null, "已登出");
            }
            
            // 记录登出日志
            LOG.info("User logged out: " + user.getUsername());
            
            // 返回成功响应
            return ResponseUtil.success(null, "登出成功");
            
        } catch (Exception e) {
            LOG.error("Logout error", e);
            return ResponseUtil.serverError("服务器内部错误");
        }
    }

    @Override
    public RoutingContext getRoutingContext() {
        return routingContext;
    }


    // 登录请求模型
    public static class LoginRequest {
        private String account;
        
        public String getAccount() {
            return account;
        }
        
        public void setAccount(String account) {
            this.account = account;
        }
    }
}
