package net.ximatai.muyun.controller;

import io.quarkus.runtime.Startup;
import io.vertx.ext.web.RoutingContext;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import net.ximatai.muyun.method.IReferableAbility;
import net.ximatai.muyun.method.IReferenceAbility;
import net.ximatai.muyun.method.IRuntimeAbility;
import net.ximatai.muyun.method.curd.std.ICreateAbility;
import net.ximatai.muyun.method.curd.std.ICustomSelectSqlAbility;
import net.ximatai.muyun.method.curd.std.IQueryAbility;
import net.ximatai.muyun.database.IDatabaseOperations;
import net.ximatai.muyun.model.IRuntimeUser;
import net.ximatai.muyun.model.PageResult;
import net.ximatai.muyun.model.QueryGroup;
import net.ximatai.muyun.model.QueryItem;
import net.ximatai.muyun.model.ReferenceInfo;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Startup
@Path("question")
public class QuestionController implements IQueryAbility, ICreateAbility, IReferenceAbility, IReferableAbility, IRuntimeAbility, ICustomSelectSqlAbility {

    @Inject
    IDatabaseOperations databaseOperations;

    @Inject
    CategoryController categoryController;
    
    @Inject
    ProgressController progressController;
    
    @Inject
    RoutingContext routingContext;

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
        List<QueryItem> items = new ArrayList<>();
        items.add(QueryItem.of("category_id").setSymbolType(QueryItem.SymbolType.EQUAL));
        return items;
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
    
    @Override
    public String getCustomSql() {
        // Get the current user
        IRuntimeUser user = getUser();
        String userId = user.getId();
        
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT q.*, ");
        sql.append("(CASE WHEN p.user_id IS NOT NULL THEN p.is_favorite ELSE false END) as is_favorite ");
        sql.append("FROM ").append(getSchemaName()).append(".question q ");
        sql.append("LEFT JOIN ").append(getSchemaName()).append(".progress p ");
        sql.append("ON q.id = p.question_id AND p.user_id = '").append(userId).append("' ");
        
        return sql.toString();
    }
    
    @Override
    public String getSelectOneRowSql() {
        // Customize the SQL for single row selection to include is_favorite
        IRuntimeUser user = getUser();
        String userId = user.getId();
        
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT q.*, ");
        sql.append("(CASE WHEN p.user_id IS NOT NULL THEN p.is_favorite ELSE false END) as is_favorite ");
        sql.append("FROM ").append(getSchemaName()).append(".question q ");
        sql.append("LEFT JOIN ").append(getSchemaName()).append(".progress p ");
        sql.append("ON q.id = p.question_id AND p.user_id = '").append(userId).append("' ");
        sql.append("WHERE q.id = :id");
        
        return sql.toString();
    }
    
    @POST
    @Path("/view")
    @Operation(summary = "分页查询（带查询条件）")
    @Override
    public PageResult view(
            @Parameter(description = "页码") @QueryParam("page") Integer page,
            @Parameter(description = "分页大小") @QueryParam("size") Long size,
            @Parameter(description = "是否分页") @QueryParam("noPage") Boolean noPage,
            @Parameter(description = "排序", example = "t_create,desc") @QueryParam("sort") List<String> sort,
            @RequestBody(description = "查询条件信息") Map<String, Object> queryBody) {
        
        IRuntimeUser user = getUser();
        String userId = user.getId();
        
        // Check if we need to exclude completed questions
        Boolean excludeCompleted = false;
        if (queryBody != null && queryBody.containsKey("exclude_completed")) {
            excludeCompleted = Boolean.valueOf(queryBody.get("exclude_completed").toString());
            // Remove from query body as we'll handle it manually
            queryBody.remove("exclude_completed");
        }
        
        // Get the base result with standard filtering
        PageResult result = IQueryAbility.super.view(page, size, noPage, sort, queryBody);
        
        // If we need to filter out completed questions and the user is authenticated
        if (excludeCompleted && !userId.equals(IRuntimeUser.WHITE.getId())) {
            // Get the question IDs from the progress table where is_completed=true
            List<String> completedIds = getDB().query(
                "SELECT question_id FROM " + getSchemaName() + ".progress " +
                "WHERE user_id = ? AND is_completed = true", 
                userId
            ).stream()
             .map(row -> (String)row.get("question_id"))
             .collect(Collectors.toList());
            
            if (!completedIds.isEmpty()) {
                // Filter out the completed questions from the result
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> resultList = (List<Map<String, Object>>) result.getList();
                
                List<Map<String, Object>> filteredList = resultList.stream()
                    .filter(row -> !completedIds.contains(row.get("id")))
                    .collect(Collectors.toList());
                
                // Create a new PageResult with the filtered data
                result = new PageResult<>(
                    filteredList, 
                    filteredList.size(), 
                    result.getSize(), 
                    result.getPage()
                );
            }
        }
        
        return result;
    }
    
    @Override
    public RoutingContext getRoutingContext() {
        return routingContext;
    }
}
