package net.ximatai.muyun.core.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

import java.util.Objects;

@ConfigMapping(prefix = "muyun")
public interface MuYunConfig {

    @WithDefault("1")
    String superUserId();

    default boolean isSuperUser(String userID) {
        Objects.requireNonNull(userID, "请提供测试用户ID");
        return userID.equals(superUserId());
    }
}
