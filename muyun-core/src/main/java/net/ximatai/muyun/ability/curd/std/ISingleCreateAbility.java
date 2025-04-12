package net.ximatai.muyun.ability.curd.std;

import jakarta.annotation.security.RolesAllowed;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import net.ximatai.muyun.ability.*;
import net.ximatai.muyun.database.builder.Column;
import net.ximatai.muyun.util.StringUtil;
import org.eclipse.microprofile.openapi.annotations.Operation;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface ISingleCreateAbility extends IDatabaseAbilityStd, IMetadataAbility {


    default void beforeCreate(Map body) {

    }

    default void afterCreate(String id) {

    }

    @POST
    @Path("/create")
    @Transactional
    @Operation(summary = "新增数据", description = "返回新增数据ID")
//    @RolesAllowed("user")
    default String create(Map body) {
        beforeCreate(body);
        HashMap map = new HashMap<>(body);
        fitOutDefaultValue(map);

        String main = getDB().insertItem(getSchemaName(), getMainTable(), map);

        afterCreate(main);
        return main;
    }

    default void fitOutDefaultValue(Map body) {
        if (!body.containsKey("t_create")) {
            LocalDateTime now = LocalDateTime.now();
            body.put("t_create", now);
            body.put("t_update", now);
        }
    }
}
