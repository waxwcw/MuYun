package net.ximatai.muyun.controller;

import io.quarkus.runtime.Startup;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import net.ximatai.muyun.method.ReferableMethod;
import net.ximatai.muyun.method.curd.std.CustomSelectSqlMethod;
import net.ximatai.muyun.method.curd.std.SelectMethod;
import net.ximatai.muyun.database.DBOperations;

import java.util.List;


@Startup
@Path("category")
public class CategoryController implements SelectMethod, ReferableMethod, CustomSelectSqlMethod {

    @Inject
    DBOperations databaseOperations;

    @Override
    public DBOperations getDatabaseOperations() {
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
