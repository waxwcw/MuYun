package net.ximatai.muyun.method;

import io.vertx.ext.web.RoutingContext;
import net.ximatai.muyun.MuYunConst;
import net.ximatai.muyun.core.config.IProfile;
import net.ximatai.muyun.model.ApiRequest;
import net.ximatai.muyun.model.IRuntimeUser;

/**
 * 获取运行时上下文的能力
 */
public interface IRuntimeAbility extends IProfile {
    RoutingContext getRoutingContext();

    default ApiRequest getApiRequest() {
        try {
            return getRoutingContext().get(MuYunConst.CONTEXT_KEY_API_REQUEST);
        } catch (Exception e) {
            return ApiRequest.BLANK;
        }
    }

    default IRuntimeUser getUser() {
        try {
            return getRoutingContext().get(MuYunConst.CONTEXT_KEY_RUNTIME_USER);
        } catch (Exception e) {
            return IRuntimeUser.WHITE;
        }
    }

}
