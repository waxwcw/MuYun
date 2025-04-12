package net.ximatai.muyun.controller;

import io.quarkus.runtime.Startup;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import net.ximatai.muyun.ability.IReferenceAbility;
import net.ximatai.muyun.ability.curd.std.ICreateAbility;
import net.ximatai.muyun.ability.curd.std.IQueryAbility;
import net.ximatai.muyun.database.IDatabaseOperations;
import net.ximatai.muyun.model.QueryItem;

import java.time.LocalDateTime;
import java.util.*;

import net.ximatai.muyun.model.ReferenceInfo;
import org.eclipse.microprofile.openapi.annotations.Operation;

/**
 * 用户答题进度控制器
 * 负责记录用户答题情况和统计（总答题统计包括总答题数，正确率，某个题库答题统计）
 */
@Startup
@Path("progress")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProgressController implements ICreateAbility, IQueryAbility, IReferenceAbility {
    @Inject
    IDatabaseOperations databaseOperations;

    @Inject
    QuestionController questionController;

    @Inject
    CategoryController categoryController;

    @Override
    public IDatabaseOperations getDatabaseOperations() {
        return databaseOperations;
    }

    @Override
    public String getMainTable() {
        return "progress";
    }
    
    @Override
    public List<QueryItem> queryItemList() {
        return Arrays.asList(
            QueryItem.of("user_id").setSymbolType(QueryItem.SymbolType.EQUAL),
            QueryItem.of("wrong_count").setSymbolType(QueryItem.SymbolType.GREATER_THAN),
            QueryItem.of("is_favorite").setSymbolType(QueryItem.SymbolType.EQUAL)
        );
    }
    
    /**
     * 批量提交多个题目的答题结果
     * @param resultsList 答题结果列表
     * @return 响应结果
     */
    @POST
    @Path("/submit-batch")
    @Transactional
    @Operation(summary = "批量提交答题结果", description = "记录用户对多个题目的答题情况")
    public Response submitBatchResults(List<Map<String, Object>> resultsList) {
        if (resultsList == null || resultsList.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("message", "提交的数据不能为空"))
                    .build();
        }
        
        // 存储需要执行的操作
        List<String> insertQueries = new ArrayList<>();
        List<List<Object>> insertParams = new ArrayList<>();
        List<String> updateQueries = new ArrayList<>();
        List<List<Object>> updateParams = new ArrayList<>();
        
        // 预编译的SQL语句
        final String queryRecordSql = "SELECT * FROM " + getSchemaName() + "." + getMainTable() + 
                                      " WHERE user_id = ? AND question_id = ? AND category_id = ?";
        final String insertSql = "INSERT INTO " + getSchemaName() + "." + getMainTable() + 
                                " (user_id, question_id, category_id, category_name, correct_count, wrong_count, is_completed, last_practiced_at) " + 
                                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        final String updateSql = "UPDATE " + getSchemaName() + "." + getMainTable() + 
                                " SET correct_count = ?, wrong_count = ?, is_completed = ?, last_practiced_at = ?, category_name = ? " +
                                "WHERE user_id = ? AND question_id = ? AND category_id = ?";
        
        int processedCount = 0;
        int skipCount = 0;
                
        try {
            // 为每个题目更新或创建记录
            for (Map<String, Object> result : resultsList) {
                if (!result.containsKey("user_id") || !result.containsKey("question_id") 
                        || !result.containsKey("category_id") || !result.containsKey("is_correct")
                        || !result.containsKey("category_name")) {
                    skipCount++;
                    continue; // 跳过不完整的记录
                }
                
                String userId = (String) result.get("user_id");
                String questionId = (String) result.get("question_id");
                String categoryId = (String) result.get("category_id");
                String categoryName = (String) result.get("category_name");
                boolean isCorrect = (boolean) result.get("is_correct");
                LocalDateTime now = LocalDateTime.now();
                
                // 查询现有记录
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> existingRecords = getDB().query(queryRecordSql, userId, questionId, categoryId);
                
                if (existingRecords.isEmpty()) {
                    // 准备插入参数
                    List<Object> params = new ArrayList<>();
                    params.add(userId);
                    params.add(questionId);
                    params.add(categoryId);
                    params.add(categoryName);      // 新增的分类名称
                    params.add(isCorrect ? 1 : 0); // correct_count
                    params.add(isCorrect ? 0 : 1); // wrong_count
                    params.add(true);              // is_completed
                    params.add(now);               // last_practiced_at
                    
                    insertQueries.add(insertSql);
                    insertParams.add(params);
                } else {
                    // 准备更新参数
                    Map<String, Object> existingRecord = existingRecords.get(0);
                    
                    int correctCount = ((Number) existingRecord.getOrDefault("correct_count", 0)).intValue();
                    int wrongCount = ((Number) existingRecord.getOrDefault("wrong_count", 0)).intValue();
                    
                    if (isCorrect) {
                        correctCount++;
                    } else {
                        wrongCount++;
                    }
                    
                    List<Object> params = new ArrayList<>();
                    params.add(correctCount);           // 更新后的正确次数
                    params.add(wrongCount);             // 更新后的错误次数
                    params.add(true);                   // is_completed
                    params.add(now);                    // last_practiced_at
                    params.add(categoryName);           // 更新分类名称
                    params.add(userId);                 // WHERE 条件
                    params.add(questionId);             // WHERE 条件
                    params.add(categoryId);             // WHERE 条件
                    
                    updateQueries.add(updateSql);
                    updateParams.add(params);
                }
                
                processedCount++;
            }
            
            // 批量执行插入操作
            for (int i = 0; i < insertQueries.size(); i++) {
                getDB().execute(insertQueries.get(i), insertParams.get(i));
            }
            
            // 批量执行更新操作
            for (int i = 0; i < updateQueries.size(); i++) {
                getDB().execute(updateQueries.get(i), updateParams.get(i));
            }
            
            // 构建响应
            Map<String, Object> response = new HashMap<>();
            response.put("message", "批量答题记录已更新");
            response.put("processed", processedCount);
            if (skipCount > 0) {
                response.put("skipped", skipCount);
            }
            
            return Response.ok(response).build();
            
        } catch (Exception e) {
            // 记录异常并返回错误响应
            e.printStackTrace(); // 实际项目中应使用日志记录
            return Response.serverError()
                   .entity(Map.of("message", "处理答题记录时发生错误: " + e.getMessage()))
                   .build();
        }
    }
    
    /**
     * 将题目标记为收藏/取消收藏
     * @param data 包含用户ID、题目ID、分类ID和收藏状态的数据
     * @return 响应结果
     */
    @POST
    @Path("/toggle-favorite")
    @Transactional
    @Operation(summary = "切换收藏状态", description = "将用户的题目标记为收藏或取消收藏")
    public Response toggleFavorite(Map<String, Object> data) {
        if (!data.containsKey("user_id") || !data.containsKey("question_id") 
                || !data.containsKey("category_id") || !data.containsKey("is_favorite")
                || !data.containsKey("category_name")) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("message", "缺少必要字段：user_id, question_id, category_id, category_name, is_favorite"))
                    .build();
        }
        
        try {
            String userId = (String) data.get("user_id");
            String questionId = (String) data.get("question_id");
            String categoryId = (String) data.get("category_id");
            String categoryName = (String) data.get("category_name");
            boolean isFavorite = (boolean) data.get("is_favorite");
            LocalDateTime now = LocalDateTime.now();
            
            // 预编译SQL语句
            final String queryRecordSql = "SELECT * FROM " + getSchemaName() + "." + getMainTable() + 
                                         " WHERE user_id = ? AND question_id = ? AND category_id = ?";
            
            List<Map<String, Object>> existingRecords = getDB().query(queryRecordSql, userId, questionId, categoryId);
            
            if (existingRecords.isEmpty()) {
                // 如果记录不存在，则创建一个新记录
                final String insertSql = "INSERT INTO " + getSchemaName() + "." + getMainTable() + 
                                       " (user_id, question_id, category_id, category_name, is_completed, correct_count, wrong_count, last_practiced_at, is_favorite) " + 
                                       "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                
                getDB().execute(insertSql, 
                               userId, questionId, categoryId, categoryName, false, 0, 0, now, isFavorite);
            } else {
                // 更新收藏状态和分类名称
                final String updateSql = "UPDATE " + getSchemaName() + "." + getMainTable() + 
                                       " SET is_favorite = ?, category_name = ? WHERE user_id = ? AND question_id = ? AND category_id = ?";
                                       
                getDB().execute(updateSql, isFavorite, categoryName, userId, questionId, categoryId);
            }
            
            return Response.ok(Map.of(
                "message", isFavorite ? "题目已收藏" : "题目已取消收藏",
                "user_id", userId,
                "question_id", questionId,
                "category_id", categoryId,
                "category_name", categoryName,
                "is_favorite", isFavorite
            )).build();
            
        } catch (Exception e) {
            // 记录异常并返回错误响应
            e.printStackTrace(); // 实际项目中应使用日志记录
            return Response.serverError()
                   .entity(Map.of("message", "处理收藏操作时发生错误: " + e.getMessage()))
                   .build();
        }
    }
    
    /**
     * 获取用户答题统计
     * @param userId 用户ID
     * @param categoryId 可选的分类ID
     * @return 用户答题统计信息
     */
    @GET
    @Path("/user-stats/{userId}")
    @Operation(summary = "获取用户答题统计", description = "获取用户的整体答题统计数据，可选按分类筛选")
    public Response getUserStats(
            @PathParam("userId") String userId,
            @QueryParam("category_id") String categoryId) {
        
        try {
            // 基础统计SQL
            StringBuilder statsSqlBuilder = new StringBuilder(
                "SELECT " +
                "COUNT(*) as total_questions, " +
                "SUM(correct_count) as total_correct, " +
                "SUM(wrong_count) as total_wrong, " +
                "SUM(CASE WHEN is_completed THEN 1 ELSE 0 END) as completed_count, " +
                "SUM(CASE WHEN is_favorite THEN 1 ELSE 0 END) as favorite_count " +
                "FROM " + getSchemaName() + "." + getMainTable() + 
                " WHERE user_id = ?");
            
            List<Object> params = new ArrayList<>();
            params.add(userId);
            
            // 如果有分类ID，添加分类筛选
            if (categoryId != null && !categoryId.isEmpty()) {
                statsSqlBuilder.append(" AND category_id = ?");
                params.add(categoryId);
            }
            
            // 执行查询
            List<Map<String, Object>> statsResults = getDB().query(statsSqlBuilder.toString(), params);
            
            if (statsResults.isEmpty() || statsResults.getFirst().get("total_questions") == null) {
                return Response.ok(Map.of(
                    "user_id", userId,
                    "category_id", categoryId != null ? categoryId : "all",
                    "total_questions", 0,
                    "total_correct", 0,
                    "total_wrong", 0,
                    "completed_count", 0,
                    "favorite_count", 0,
                    "accuracy_rate", 0.0
                )).build();
            }
            
            Map<String, Object> stats = new HashMap<>(statsResults.getFirst());
            
            // 计算正确率
            long totalCorrect = ((Number) stats.getOrDefault("total_correct", 0)).longValue();
            long totalWrong = ((Number) stats.getOrDefault("total_wrong", 0)).longValue();
            double accuracyRate = 0.0;
            
            if (totalCorrect + totalWrong > 0) {
                accuracyRate = (double) totalCorrect / (totalCorrect + totalWrong) * 100;
                // 保留两位小数
                accuracyRate = Math.round(accuracyRate * 100.0) / 100.0;
            }
            
            stats.put("accuracy_rate", accuracyRate);
            stats.put("user_id", userId);
            
            if (categoryId != null && !categoryId.isEmpty()) {
                stats.put("category_id", categoryId);
            } else {
                stats.put("category_id", "all");
                
                // 如果没有指定分类，额外获取各分类的统计信息
                String categoriesSql = 
                    "SELECT " +
                    "category_id, " +
                    "category_name, " +
                    "COUNT(*) as total_questions, " +
                    "SUM(correct_count) as total_correct, " +
                    "SUM(wrong_count) as total_wrong " +
                    "FROM " + getSchemaName() + "." + getMainTable() + 
                    " WHERE user_id = ? " +
                    "GROUP BY category_id, category_name";
                
                List<Map<String, Object>> categoryStats = getDB().query(categoriesSql, userId);
                
                if (!categoryStats.isEmpty()) {
                    // 计算每个分类的正确率
                    for (Map<String, Object> category : categoryStats) {
                        long catCorrect = ((Number) category.getOrDefault("total_correct", 0)).longValue();
                        long catWrong = ((Number) category.getOrDefault("total_wrong", 0)).longValue();
                        double catAccuracy = 0.0;
                        
                        if (catCorrect + catWrong > 0) {
                            catAccuracy = (double) catCorrect / (catCorrect + catWrong) * 100;
                            // 保留两位小数
                            catAccuracy = Math.round(catAccuracy * 100.0) / 100.0;
                        }
                        
                        category.put("accuracy_rate", catAccuracy);
                    }
                    
                    stats.put("categories", categoryStats);
                }
            }
            
            return Response.ok(stats).build();
            
        } catch (Exception e) {
            // 记录异常并返回错误响应
            e.printStackTrace(); // 实际项目中应使用日志记录
            return Response.serverError()
                   .entity(Map.of("message", "获取用户统计数据时发生错误: " + e.getMessage()))
                   .build();
        }
    }

    /**
     * 获取用户在某个分类下的题目完成状态
     * @param userId 用户ID
     * @param categoryId 分类ID
     * @return 分类下已完成和未完成的题目列表
     */
    @GET
    @Path("/category-progress/{userId}/{categoryId}")
    @Operation(summary = "获取分类进度", description = "获取用户在特定分类下的题目完成情况")
    public Response getCategoryProgress(
            @PathParam("userId") String userId,
            @PathParam("categoryId") String categoryId) {
        
        try {
            if (userId == null || userId.isEmpty() || categoryId == null || categoryId.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("message", "用户ID和分类ID不能为空"))
                        .build();
            }
            
            // 获取分类名称
            final String categoryNameSql = 
                "SELECT DISTINCT category_name FROM " + getSchemaName() + "." + getMainTable() + 
                " WHERE user_id = ? AND category_id = ? LIMIT 1";
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> categoryResults = getDB().query(categoryNameSql, userId, categoryId);
            
            String categoryName = "";
            if (!categoryResults.isEmpty() && categoryResults.get(0).get("category_name") != null) {
                categoryName = (String) categoryResults.get(0).get("category_name");
            }
            
            // 查询已完成的题目
            final String completedSql = 
                "SELECT question_id, correct_count, wrong_count, last_practiced_at, is_favorite " +
                "FROM " + getSchemaName() + "." + getMainTable() + 
                " WHERE user_id = ? AND category_id = ? AND is_completed = true " +
                "ORDER BY last_practiced_at DESC";

            List<Map<String, Object>> completedQuestions = getDB().query(completedSql, userId, categoryId);
            
            // 查询收藏的题目
            final String favoriteSql = 
                "SELECT question_id, correct_count, wrong_count, last_practiced_at, is_favorite " +
                "FROM " + getSchemaName() + "." + getMainTable() + 
                " WHERE user_id = ? AND category_id = ? AND is_favorite = true " +
                "ORDER BY last_practiced_at DESC";

            List<Map<String, Object>> favoriteQuestions = getDB().query(favoriteSql, userId, categoryId);
            
            // 查询所有该分类下的题目数量
            final String totalQuestionsSql = 
                "SELECT COUNT(*) as total FROM question WHERE category_id = ?";

            List<Map<String, Object>> totalResults = getDB().query(totalQuestionsSql, categoryId);
            
            long totalQuestions = 0;
            if (!totalResults.isEmpty() && totalResults.get(0).get("total") != null) {
                totalQuestions = ((Number) totalResults.get(0).get("total")).longValue();
            }
            
            // 构建响应
            Map<String, Object> result = new HashMap<>();
            result.put("user_id", userId);
            result.put("category_id", categoryId);
            result.put("category_name", categoryName);
            result.put("completed_count", completedQuestions.size());
            result.put("favorite_count", favoriteQuestions.size());
            result.put("total_questions", totalQuestions);
            result.put("completion_percentage", totalQuestions > 0 ? 
                    Math.round((completedQuestions.size() * 100.0 / totalQuestions) * 100.0) / 100.0 : 0);
            
            result.put("completed_questions", completedQuestions);
            result.put("favorite_questions", favoriteQuestions);
            
            return Response.ok(result).build();
            
        } catch (Exception e) {
            // 记录异常并返回错误响应
            e.printStackTrace(); // 实际项目中应使用日志记录
            return Response.serverError()
                   .entity(Map.of("message", "获取分类进度时发生错误: " + e.getMessage()))
                   .build();
        }
    }

    @Override
    public List<ReferenceInfo> getReferenceList() {
        return List.of(
            questionController.toReferenceInfo("question_id")
                .add("content","content")
                .add("type","type")
        );
    }
}
