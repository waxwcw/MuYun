package net.ximatai.muyun.controller;


import io.quarkus.runtime.Startup;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import net.ximatai.muyun.method.ReferenceMethod;
import net.ximatai.muyun.method.curd.std.*;
import net.ximatai.muyun.database.DBOperations;
import net.ximatai.muyun.model.QueryItem;
import net.ximatai.muyun.model.ReferenceInfo;

import java.util.List;



@Startup
@Path("comment")
public class CommentController implements DeleteMethod, SingleCreateMethod, QueryMethod, ReferenceMethod {


    @Inject
    UserController userController;

    @Inject
    DBOperations databaseOperations;

    @Override
    public DBOperations getDatabaseOperations() {
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

