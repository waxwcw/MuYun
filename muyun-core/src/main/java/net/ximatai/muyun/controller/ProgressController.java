package net.ximatai.muyun.controller;

import io.quarkus.runtime.Startup;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import net.ximatai.muyun.ability.curd.std.ICreateAbility;
import net.ximatai.muyun.ability.curd.std.ISingleSelectAbility;
import net.ximatai.muyun.database.IDatabaseOperations;

@Startup
@Path("progress")
public class ProgressController implements ICreateAbility , ISingleSelectAbility {
    @Inject
    IDatabaseOperations databaseOperations;

    @Override
    public IDatabaseOperations getDatabaseOperations() {
        return databaseOperations;
    }

    @Override
    public String getMainTable() {
        return "progress";
    }
}
