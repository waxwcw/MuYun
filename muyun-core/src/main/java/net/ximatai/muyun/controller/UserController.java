package net.ximatai.muyun.controller;

import io.quarkus.runtime.Startup;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import net.ximatai.muyun.method.IReferableAbility;
import net.ximatai.muyun.method.curd.std.*;
import net.ximatai.muyun.database.IDatabaseOperations;
import org.eclipse.microprofile.openapi.annotations.Operation;

import java.util.List;
import java.util.Map;


@Startup
@Path("user")
@Produces(MediaType.APPLICATION_JSON)
public class UserController implements ISingleCreateAbility, IUpdateAbility , IDeleteAbility, IReferableAbility{


    @Inject
    IDatabaseOperations databaseOperations;

    @Override
    public IDatabaseOperations getDatabaseOperations() {
        return databaseOperations;
    }

    @Override
    @POST
    @Path("/create")
    @Transactional
    @Operation(summary = "创建用户", description = "返回新增数据ID")
    public String create(Map body) {
        // 调用默认实现
        return ISingleCreateAbility.super.create(body);
    }

    @Override
    public String getMainTable() {
        return "user";
    }
    
    @Override
    public List<String> getOpenColumns() {
        return List.of("nickname", "avatar_url","t_create");
    }
    
    public Map<String, Object> check(String username) {
        String sql = "select * from %s.%s where account = :account".formatted(getSchemaName(), getMainTable());
        Map<String, Object> row = getDB().row(sql, Map.of("account", username));
        return row;
    }
}
