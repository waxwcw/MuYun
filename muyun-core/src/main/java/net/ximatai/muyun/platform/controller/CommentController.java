package net.ximatai.muyun.platform.controller;


import io.quarkus.runtime.Startup;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import net.ximatai.muyun.ability.IReferenceAbility;
import net.ximatai.muyun.ability.curd.std.*;
import net.ximatai.muyun.database.IDatabaseOperations;
import net.ximatai.muyun.model.QueryItem;
import net.ximatai.muyun.model.ReferenceInfo;
import net.ximatai.muyun.platform.user.UserController;

import java.util.List;



@Startup
@Path("comment")
public class CommentController implements  IDeleteAbility, ISingleCreateAbility, IQueryAbility, IReferenceAbility {


    @Inject
    UserController userController;

    @Inject
    IDatabaseOperations databaseOperations;

    @Override
    public IDatabaseOperations getDatabaseOperations() {
        return databaseOperations;
    }

    @Override
    public String getMainTable() {
        return "comment";
    }


    @Override
    public List<QueryItem> queryItemList() {
        return List.of(
            QueryItem.of("post_id").setSymbolType(QueryItem.SymbolType.EQUAL)
        );
    }

    @Override
    public List<ReferenceInfo> getReferenceList() {
        return List.of(
            userController.toReferenceInfo("user_id").add("nickname","nickname").add("avatar_url","avatar_url")
        );
    }
}

