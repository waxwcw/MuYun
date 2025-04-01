package net.ximatai.muyun.platform.security;

import io.quarkus.runtime.Startup;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import net.ximatai.muyun.ability.IRuntimeAbility;
import net.ximatai.muyun.core.exception.MuYunException;
import net.ximatai.muyun.model.ApiRequest;
import net.ximatai.muyun.model.IRuntimeUser;
import net.ximatai.muyun.model.PageResult;
import net.ximatai.muyun.platform.user.UserController;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.jboss.logging.Logger;


import java.util.Map;

@Startup
@Path("security")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SecurityController implements IRuntimeAbility {

    @Inject
    RoutingContext routingContext;

    @Inject
    UserController userController;

    private static final Logger LOG = Logger.getLogger(SecurityController.class);

    @POST
    @Path("/isValidToken")
    @Operation(
            summary = "校验Token是否过期"
    )
    @APIResponse(
            responseCode = "200",
            description = "Token验证结果",
            content = @Content(mediaType = MediaType.APPLICATION_JSON)
    )
    public Response isValidToken() {
        try {
            // 获取当前用户信息
            IRuntimeUser user = getUser();
            boolean isValid = user != null && !IRuntimeUser.WHITE.getId().equals(user.getId());
            
            JsonObject result = new JsonObject()
                    .put("valid", isValid);

            if (isValid) {
                result.put("user", new JsonObject()
                        .put("id", user.getId())
                        .put("name", user.getName())
                        .put("username", user.getUsername())
                );
            }

            return Response.ok(result.encode()).build();
        } catch (Exception e) {
            LOG.error("Token validation error", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new JsonObject().put("message", "Internal server error").encode())
                    .build();
        }
    }

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
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new JsonObject().put("message", "Missing required parameters").encode())
                        .build();
            }

            // 查询用户信息
            Map<String, Object> userInfo = userController.check(String.valueOf(userMap.get("account")));

            // 验证用户是否存在
            if (userInfo == null) {
                apiRequest.setError(new RuntimeException("该用户不存在：%s".formatted(userMap.get("account"))));
                LOG.warn("该用户不存在：" + userMap.get("account"));
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(new JsonObject().put("message", "用户名或密码错误").encode())
                        .build();
            }
            
            // 验证密码
            String password = userInfo.get("password").toString();
            if (!password.equals(userMap.get("password"))) {
                LOG.warn("Login failed: Invalid password for user - " + userMap.get("account"));
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(new JsonObject().put("message", "用户名或密码错误").encode())
                        .build();
            }

            // 构建用户对象
            IRuntimeUser user = IRuntimeUser.build(new JsonObject()
                    .put("id", userInfo.get("id"))
                    .put("name", userInfo.get("nickname"))
                    .put("username", userInfo.get("account"))
            );
            
            // 生成JWT令牌
            String token = JwtUtils.generateToken(user);
            
            // 返回令牌和用户信息
            JsonObject result = new JsonObject()
                    .put("token", token)
                    .put("user", new JsonObject()
                            .put("id", user.getId())
                            .put("name", user.getName())
                            .put("username", user.getUsername())
                    );
            
            LOG.info("User logged in successfully: " + user.getUsername());
            return Response.ok(result.encode()).build();
            
        } catch (Exception e) {
            LOG.error("Login error", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new JsonObject().put("message", "Internal server error").encode())
                    .build();
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
                return Response.ok(new JsonObject().put("message", "已登出").encode()).build();
            }
            
            // 记录登出日志
            LOG.info("User logged out: " + user.getUsername());
            
            // 返回成功响应
            return Response.ok(new JsonObject().put("message", "登出成功").encode()).build();
            
        } catch (Exception e) {
            LOG.error("Logout error", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new JsonObject().put("message", "Internal server error").encode())
                    .build();
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
