package net.ximatai.muyun.util;

import io.smallrye.jwt.build.Jwt;
import io.vertx.core.json.JsonObject;
import net.ximatai.muyun.model.IRuntimeUser;
import org.eclipse.microprofile.jwt.Claims;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JWT工具类，用于生成和验证JWT令牌
 */
public class JwtUtil {
    
    private static final Logger LOG = Logger.getLogger(JwtUtil.class.getName());
    
    private static final String PRIVATE_KEY_PATH = "privateKey.pem";
    private static final String PUBLIC_KEY_PATH = "publicKey.pem";
    private static final String KEY_STORE_DIR = "keys";

    private static final List<String> ROLES = List.of("User");
    
    private static PrivateKey privateKey;
    private static PublicKey publicKey;

    // 令牌有效期（秒），从配置文件中读取，默认为24小时
    private static long getTokenValidity() {
        try {
            return org.eclipse.microprofile.config.ConfigProvider.getConfig()
                    .getValue("jwt.token.expiration", Long.class);
        } catch (Exception e) {
            return 24 * 60 * 60; // 默认24小时
        }
    }

    /**
     * 加载RSA密钥
     */
    static {
        try {
            loadOrGenerateKeys();
            LOG.info("RSA keys loaded successfully");
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to load RSA keys", e);
        }
    }

    /**
     * 加载或生成RSA密钥对
     */
    private static void loadOrGenerateKeys() throws Exception {
        Path keyStoreDir = Paths.get(KEY_STORE_DIR);
        Path privateKeyPath = keyStoreDir.resolve(PRIVATE_KEY_PATH);
        Path publicKeyPath = keyStoreDir.resolve(PUBLIC_KEY_PATH);

        // 确保密钥存储目录存在
        if (!Files.exists(keyStoreDir)) {
            Files.createDirectories(keyStoreDir);
        }

        // 如果密钥文件不存在，从资源文件复制
        if (!Files.exists(privateKeyPath) || !Files.exists(publicKeyPath)) {
            String privateKeyPEM = readResourceFile(PRIVATE_KEY_PATH);
            String publicKeyPEM = readResourceFile(PUBLIC_KEY_PATH);
            
            Files.writeString(privateKeyPath, privateKeyPEM);
            Files.writeString(publicKeyPath, publicKeyPEM);
        }

        // 加载密钥
        privateKey = loadPrivateKeyFromFile(privateKeyPath);
        publicKey = loadPublicKeyFromFile(publicKeyPath);
    }
    
    /**
     * 从文件加载私钥
     */
    private static PrivateKey loadPrivateKeyFromFile(Path privateKeyPath) throws Exception {
        String privateKeyPEM = Files.readString(privateKeyPath);
        privateKeyPEM = privateKeyPEM
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                .replace("-----END RSA PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        
        byte[] encoded = Base64.getDecoder().decode(privateKeyPEM);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
        return keyFactory.generatePrivate(keySpec);
    }
    
    /**
     * 从文件加载公钥
     */
    private static PublicKey loadPublicKeyFromFile(Path publicKeyPath) throws Exception {
        String publicKeyPEM = Files.readString(publicKeyPath);
        publicKeyPEM = publicKeyPEM
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        
        byte[] encoded = Base64.getDecoder().decode(publicKeyPEM);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);
        return keyFactory.generatePublic(keySpec);
    }
    
    /**
     * 读取资源文件内容
     */
    private static String readResourceFile(String resourcePath) throws IOException {
        ClassLoader classLoader = JwtUtil.class.getClassLoader();
        try (InputStream inputStream = classLoader.getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IOException("Resource not found: " + resourcePath);
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
    
    /**
     * 生成JWT令牌
     *
     * @param user 用户信息
     * @return JWT令牌字符串
     */
    public static String generateToken(IRuntimeUser user) {
        if (privateKey == null) {
            LOG.severe("Private key not loaded, using default signing method");
            return generateTokenWithDefaultSigning(user);
        }
        
        Set<String> groups = new HashSet<>();
        groups.add("user");
        groups.add("muyun-platform");
        
        long tokenValidity = getTokenValidity();
        
        // 如果tokenValidity小于等于0，则不设置过期时间（令牌永不过期）
        if (tokenValidity <= 0) {
            return Jwt.claims()
                    .subject(user.getId())
                    .issuer("muyun-platform")
                    .issuedAt(Instant.now())
                    .groups(groups)
                    .claim(Claims.full_name.name(), user.getName())
                    .claim("username", user.getUsername())
                    .sign(privateKey);
        }
        
        return Jwt.claims()
                .subject(user.getId())
                .issuer("muyun-platform")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plus(Duration.ofSeconds(tokenValidity)))
                .groups(groups)
                .claim(Claims.full_name.name(), user.getName())
                .claim("username", user.getUsername())
                .sign(privateKey);
    }
    
    /**
     * 使用默认签名方式生成JWT令牌（备用方法）
     *
     * @param user 用户信息
     * @return JWT令牌字符串
     */
    private static String generateTokenWithDefaultSigning(IRuntimeUser user) {
        Set<String> groups = new HashSet<>();
        groups.add("user");
        
        long tokenValidity = getTokenValidity();
        
        // 如果tokenValidity小于等于0，则不设置过期时间（令牌永不过期）
        if (tokenValidity <= 0) {
            return Jwt.claims()
                    .subject(user.getId())
                    .issuer("muyun-platform")
                    .issuedAt(Instant.now())
                    .groups(groups)
                    .claim(Claims.full_name.name(), user.getName())
                    .claim("username", user.getUsername())
                    .sign();
        }
        
        return Jwt.claims()
                .subject(user.getId())
                .issuer("muyun-platform")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plus(Duration.ofSeconds(tokenValidity)))
                .groups(groups)
                .claim(Claims.full_name.name(), user.getName())
                .claim("username", user.getUsername())
                .sign();
    }

    /**
     * 验证JWT令牌是否有效
     *
     * @param token JWT令牌
     * @return 如果令牌有效返回true，否则返回false
     */
    public static boolean validateToken(JsonWebToken token) {
        if (token == null) {
            return false;
        }

        try {
            // 检查令牌是否过期
            Instant expirationTime = Instant.ofEpochSecond(token.getExpirationTime());
            return expirationTime == null || Instant.now().isBefore(expirationTime);
            // 注意：使用MicroProfile JWT，令牌验证是在框架层自动完成的
            // 公钥验证是通过配置文件中的mp.jwt.verify.publickey或mp.jwt.verify.publickey.location属性完成的
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Token validation failed", e);
            return false;
        }
    }
    
    /**
     * 检查令牌是否即将过期（默认5分钟内）
     *
     * @param token JWT令牌
     * @return 如果令牌即将过期返回true，否则返回false
     */
    public static boolean isTokenAboutToExpire(JsonWebToken token) {
        if (token == null) {
            return true;
        }

        try {
            // 获取过期时间
            Instant expirationTime = Instant.ofEpochSecond(token.getExpirationTime());
            if (expirationTime == null) {
                return false; // 永不过期的令牌
            }
            
            // 如果过期时间在5分钟内，认为即将过期
            return Instant.now().plus(Duration.ofMinutes(5)).isAfter(expirationTime);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Token expiration check failed", e);
            return true; // 如果检查失败，建议刷新令牌
        }
    }

    /**
     * 从JWT令牌中提取用户信息
     *
     * @param token JWT令牌
     * @return 用户信息
     */
    public static IRuntimeUser extractUser(JsonWebToken token) {
        if (token == null) {
            return IRuntimeUser.WHITE;
        }
        
        JsonObject userJson = new JsonObject()
                .put("id", token.getSubject())
                .put("name", token.getClaim(Claims.full_name.name()))
                .put("username", token.getClaim("username"));
        
        return IRuntimeUser.build(userJson);
    }
}
