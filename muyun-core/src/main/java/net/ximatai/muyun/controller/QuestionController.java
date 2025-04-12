package net.ximatai.muyun.controller;

import io.quarkus.runtime.Startup;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import net.ximatai.muyun.ability.IReferableAbility;
import net.ximatai.muyun.ability.IReferenceAbility;
import net.ximatai.muyun.ability.curd.std.ICreateAbility;
import net.ximatai.muyun.ability.curd.std.IQueryAbility;
import net.ximatai.muyun.database.IDatabaseOperations;
import net.ximatai.muyun.model.QueryItem;
import net.ximatai.muyun.model.ReferenceInfo;

import java.util.List;

@Startup
@Path("question")
public class QuestionController implements IQueryAbility, ICreateAbility, IReferenceAbility, IReferableAbility {

    @Inject
    IDatabaseOperations databaseOperations;

    @Inject
    CategoryController categoryController;

    @Override
    public IDatabaseOperations getDatabaseOperations() {
        return databaseOperations;
    }

    @Override
    public String getMainTable() {
        return "question";
    }

    @Override
    public List<QueryItem> queryItemList() {
        return List.of(
            QueryItem.of("category_id").setSymbolType(QueryItem.SymbolType.EQUAL)
        );
    }

    @Override
    public List<ReferenceInfo> getReferenceList() {
        return List.of(
            categoryController.toReferenceInfo("category_id")
                .add("name","category_name")
        );
    }

    @Override
    public List<String> getOpenColumns() {
        return List.of("content","type");
    }
}
