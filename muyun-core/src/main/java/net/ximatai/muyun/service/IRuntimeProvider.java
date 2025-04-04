package net.ximatai.muyun.service;

import io.vertx.ext.web.RoutingContext;
import net.ximatai.muyun.model.ApiRequest;
import net.ximatai.muyun.model.IRuntimeUser;

import java.util.Optional;

public interface IRuntimeProvider {

    Optional<IRuntimeUser> getUser(RoutingContext context);

    default ApiRequest apiRequest(RoutingContext context) {
        return new ApiRequest(context.request().path());
    }

}
