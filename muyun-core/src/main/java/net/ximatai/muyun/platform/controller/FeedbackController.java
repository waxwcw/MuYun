package net.ximatai.muyun.platform.controller;

import io.quarkus.runtime.Startup;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import net.ximatai.muyun.ability.curd.std.ISingleCreateAbility;
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
