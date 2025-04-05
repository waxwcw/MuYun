package net.ximatai.muyun.platform.flow;


import io.quarkus.runtime.Startup;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import net.ximatai.muyun.ability.IReferenceAbility;
import net.ximatai.muyun.ability.curd.std.*;
import net.ximatai.muyun.database.IDatabaseOperations;
import net.ximatai.muyun.model.QueryItem;
import net.ximatai.muyun.model.ReferenceInfo;
import net.ximatai.muyun.platform.user.UserController;


import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Startup
@Path("infor-flow")
public class FlowController  implements  ISingleCreateAbility, IDeleteAbility, ICustomSelectSqlAbility ,IQueryAbility, IReferenceAbility {


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
        return "post";
    }

    @Override
    public String getCustomSql() {
        return """
        SELECT
            p.*,
            COALESCE(
                (SELECT json_agg(pi.image_url)
                 FROM post_image pi
                 WHERE pi.post_id = p.id),
                '[]'::json
            ) AS images
        FROM post p
        """;
    }
    
    /**
     * 创建动态并保存相关图片
     * @param body 包含user_id、content和images列表的请求体
     * @return 新创建的动态ID
     */
    @POST
    @Path("/create")
    @Transactional
    @Override
    public String create(Map body) {
        // 调用父类的create方法前的准备
        beforeCreate(body);
        
        // 提取图片列表
        List<String> imageUrls = (List<String>) body.get("images");
        
        // 移除images字段，避免直接存入主表
        Map<String, Object> postData = new HashMap<>(body);
        postData.remove("images");
        
        // 创建post记录
        fitOutDefaultValue(postData);
        String postId = getDB().insertItem(getSchemaName(), getMainTable(), postData);
        
        // 如果有图片，创建post_image记录
        if (imageUrls != null && !imageUrls.isEmpty()) {
            for (int i = 0; i < imageUrls.size(); i++) {
                Map<String, Object> imageData = new HashMap<>();
                imageData.put("post_id", postId);
                imageData.put("image_url", imageUrls.get(i));
                imageData.put("sort_order", i); // 设置排序顺序
                
                // 直接插入数据库
                getDB().insertItem(getSchemaName(), "post_image", imageData);
            }
        }
        
        // 调用afterCreate钩子
        afterCreate(postId);
        
        return postId;
    }

    @Override
    public List<QueryItem> queryItemList() {
        return List.of(
            QueryItem.of("content").setSymbolType(QueryItem.SymbolType.LIKE),
            QueryItem.of("type").setSymbolType(QueryItem.SymbolType.EQUAL),
            QueryItem.of("user_id").setSymbolType(QueryItem.SymbolType.EQUAL)
        );
    }

    @Override
    public List<ReferenceInfo> getReferenceList() {
        return List.of(
            userController.toReferenceInfo("user_id")
                .add("nickname","nickname")
                .add("avatar_url","avatar_url")
        );
    }
}
