package net.ximatai.muyun.controller;


import io.quarkus.runtime.Startup;
import io.vertx.core.json.JsonObject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import net.ximatai.muyun.util.ResponseUtil;
import org.eclipse.microprofile.openapi.annotations.Operation;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

@Startup
@Path("config")
public class ConfigController {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ConfigController.class);
    @POST
    @Path("/getRongCloudToken")
    @Operation(
            summary = "获取融云Token"
    )
    public Response getRongCloudToken(Map body) {
        try {
            // 验证请求参数
            if (body == null || body.get("userId") == null || body.get("name") == null || body.get("avatar_url") == null) {
                return ResponseUtil.badRequest("缺少必要参数");
            }
            
            String userId = body.get("userId").toString();
            String name = body.get("name").toString();
            String avatar_url = body.get("avatar_url").toString();
            
            // 设置请求URL和参数
            String apiUrl = "https://api.sg-light-api.com/user/getToken.json";
            String requestBody = "userId=" + userId + "&name=" + name + "&portraitUri=" + avatar_url;
            
            // 创建HTTP客户端
            HttpClient client = HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_1_1)
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();
            
            // 生成签名所需的参数
            String appKey = "c9kqb3rdcfnzj";
            String appSecret = "9CCciqyOSbA";
            String nonce = String.valueOf((int)(Math.random() * 100000000)); // 生成随机数
            String timestamp = String.valueOf(System.currentTimeMillis()); // 获取当前时间戳（毫秒）
            
            // 计算签名：SHA1(AppSecret + Nonce + Timestamp)
            String signatureSource = appSecret + nonce + timestamp;
            String signature = calculateSHA1(signatureSource);
            
            // 构建HTTP请求
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("App-Key", appKey)
                    .header("Nonce", nonce)
                    .header("Timestamp", timestamp)
                    .header("Signature", signature)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();
            
            // 发送请求并获取响应
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            // 解析响应JSON
            if (response.statusCode() == 200) {
                JsonObject jsonResponse = new JsonObject(response.body());
                if (jsonResponse.getInteger("code") == 200) {
                    // 成功获取Token，返回成功响应
                    JsonObject data = new JsonObject().put("token", jsonResponse.getString("token"));
                    return ResponseUtil.success(data, "获取融云Token成功");
                }
            }
            
            // 如果请求失败，返回错误响应
            JsonObject data = new JsonObject().put("response", response.body());
            return ResponseUtil.error(Response.Status.INTERNAL_SERVER_ERROR, 500, "获取融云Token失败");
            
        } catch (Exception e) {
            logger.error("获取融云Token异常", e);
            JsonObject data = new JsonObject().put("error", e.getMessage());
            return ResponseUtil.serverError("获取融云Token异常");
        }
    }

    /**
     * 计算SHA1哈希值
     * @param input 输入字符串
     * @return SHA1哈希值
     */
    private String calculateSHA1(String input) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-1");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            
            // 将字节数组转换为十六进制字符串
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            logger.error("计算SHA1哈希值异常", e);
            return "";
        }
    }

}
