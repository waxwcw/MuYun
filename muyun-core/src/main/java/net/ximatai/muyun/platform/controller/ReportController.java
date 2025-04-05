package net.ximatai.muyun.platform.controller;

import io.quarkus.runtime.Startup;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import net.ximatai.muyun.ability.curd.std.ISingleCreateAbility;
import net.ximatai.muyun.database.IDatabaseOperations;

@Startup
@Path("report")
public class ReportController implements ISingleCreateAbility {

    @Inject
    IDatabaseOperations databaseOperations;

    @Override
    public IDatabaseOperations getDatabaseOperations() {
        return databaseOperations;
    }

    @Override
    public String getMainTable() {
        return "report";
    }

//    @POST
//    @Path("/feedback")
//    @Transactional
//    @Operation(
//            summary = "题目反馈"
//    )
//    public String feedback() {
//        return null;
//    }

}
