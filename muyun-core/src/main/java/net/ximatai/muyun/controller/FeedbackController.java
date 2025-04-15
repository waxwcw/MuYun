package net.ximatai.muyun.controller;

import io.quarkus.runtime.Startup;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import net.ximatai.muyun.method.curd.std.ISingleCreateAbility;
import net.ximatai.muyun.database.IDatabaseOperations;

@Startup
@Path("feedback")
public class FeedbackController implements ISingleCreateAbility {

    @Inject
    IDatabaseOperations databaseOperations;

    @Override
    public IDatabaseOperations getDatabaseOperations() {
        return databaseOperations;
    }

    @Override
    public String getMainTable() {
        return "feedback";
    }

}
