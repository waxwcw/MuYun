package net.ximatai.muyun.method.curd.std;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import net.ximatai.muyun.method.*;
import org.eclipse.microprofile.openapi.annotations.Operation;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public interface SingleCreateMethod extends DatabaseMethodStd, MetadataMethod {


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
