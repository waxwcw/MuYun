package net.ximatai.muyun.method.curd.std;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import net.ximatai.muyun.method.IDatabaseAbilityStd;
import net.ximatai.muyun.method.IMetadataAbility;
import org.eclipse.microprofile.openapi.annotations.Operation;

import java.util.List;

/**
 * 删除数据的能力
 */
public interface IDeleteAbility extends IDatabaseAbilityStd, IMetadataAbility {

    default void beforeDelete(String id) {}

    default void afterDelete(String id) {}

    /**
     * 级联删除关联表中的数据
     * @param parentId         父表ID
     * @param childTable       子表名称
     * @param foreignKeyColumn 外键列名
     */
    default void cascadeDeleteRelated(String parentId, String childTable, String foreignKeyColumn) {
        getDB().delete(
            String.format("DELETE FROM %s.%s WHERE %s = ?",
                getSchemaName(), childTable, foreignKeyColumn),
            List.of(parentId)
        );
    }

    @GET
    @Path("/delete/{id}")
    @Transactional
    @Operation(summary = "根据id删除")
    default Integer delete(@PathParam("id") String id) {
        this.beforeDelete(id);
        int result;
        result = getDB().deleteItem(getSchemaName(), getMainTable(), id);
        afterDelete(id);
        return result;
    }
}
