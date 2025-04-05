package net.ximatai.muyun.core.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

import java.util.Objects;

@ConfigMapping(prefix = "muyun")
public interface MuYunConfig extends IProfile {

    @WithDefault("1")
    String superUserId();

    @WithDefault("24")
    int sessionTimeoutHour();

    @WithDefault("false")
    boolean useSession();
    
    /**
     * 是否启用JWT认证
     * 设置为false时，所有API请求都视为白名单用户，无需认证
     * 默认为true，启用JWT认证
     */
    @WithDefault("true")
    boolean enableJwtAuth();

    default boolean isSuperUser(String userID) {
        Objects.requireNonNull(userID, "请提供测试用户ID");
        return userID.equals(superUserId());
    }
}
