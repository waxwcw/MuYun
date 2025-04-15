package net.ximatai.muyun.controller;

import io.quarkus.runtime.Startup;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import net.ximatai.muyun.method.IReferableAbility;
import net.ximatai.muyun.method.curd.std.ICustomSelectSqlAbility;
import net.ximatai.muyun.method.curd.std.ISelectAbility;
import net.ximatai.muyun.database.IDatabaseOperations;

import java.util.List;


@Startup
@Path("category")
public class CategoryController implements ISelectAbility, IReferableAbility, ICustomSelectSqlAbility{

    @Inject
    IDatabaseOperations databaseOperations;

    @Override
    public IDatabaseOperations getDatabaseOperations() {
        return databaseOperations;
    }

    @Override
    public String getMainTable() {
        return "category";
    }

    @Override
    public List<String> getOpenColumns() {
        return List.of("name");
    }

    @Override
    public String getCustomSql() {
        return "SELECT c.*, COUNT(q.id) as total FROM category c " +
               "LEFT JOIN question q ON c.id = q.category_id " +
               "GROUP BY c.id";
    }
}
