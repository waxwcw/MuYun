package net.ximatai.muyun.core.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;


@ConfigMapping(prefix = "muyun")
public interface MuYunConfig extends IProfile {
    
    /**
     * 是否启用JWT认证
     * 设置为false时，所有API请求都视为白名单用户，无需认证
     * 默认为true，启用JWT认证
     */
    @WithDefault("true")
    boolean enableJwtAuth();
}
