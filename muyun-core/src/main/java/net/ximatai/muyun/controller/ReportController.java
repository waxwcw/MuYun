package net.ximatai.muyun.controller;

import io.quarkus.runtime.Startup;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import net.ximatai.muyun.method.curd.std.SingleCreateMethod;
import net.ximatai.muyun.database.DBOperations;

@Startup
@Path("report")
public class ReportController implements SingleCreateMethod {

    @Inject
    DBOperations databaseOperations;

    @Override
    public DBOperations getDatabaseOperations() {
        return databaseOperations;
    }

    @Override
    public String getMainTable() {
        return "report";
    }

}
