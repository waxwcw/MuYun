package net.ximatai.muyun.platform.report;

import io.quarkus.runtime.Startup;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import net.ximatai.muyun.ability.curd.std.ISingleCreateAbility;
import net.ximatai.muyun.core.Scaffold;
import net.ximatai.muyun.database.IDatabaseOperations;
import org.eclipse.microprofile.openapi.annotations.Operation;

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
