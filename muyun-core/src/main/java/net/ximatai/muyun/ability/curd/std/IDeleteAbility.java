package net.ximatai.muyun.ability.curd.std;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import net.ximatai.muyun.ability.IChildrenAbility;
import net.ximatai.muyun.ability.IDatabaseAbilityStd;
import net.ximatai.muyun.ability.IMetadataAbility;
import net.ximatai.muyun.model.ChildTableInfo;
import net.ximatai.muyun.model.DataChangeChannel;
import org.eclipse.microprofile.openapi.annotations.Operation;

import java.util.List;

/**
 * 删除数据的能力
 */
public interface IDeleteAbility extends IDatabaseAbilityStd, IMetadataAbility {

    default void beforeDelete(String id) {

    }

    default void afterDelete(String id) {

    }

    @GET
    @Path("/delete/{id}")
    @Transactional
    @Operation(summary = "删除数据", description = "返回被删除数据的数量，正常为1")
    default Integer delete(@PathParam("id") String id) {
        this.beforeDelete(id);
        int result;

        if (this instanceof IChildrenAbility ability) { // 如果带子表，考虑级联删除
            ability.getChildren().stream().filter(ChildTableInfo::isAutoDelete).forEach(childTableInfo -> {
                ability.putChildTableList(id, childTableInfo.getChildAlias(), List.of()); //对应即可清空子表
            });
        }


        result = getDB().deleteItem(getSchemaName(), getMainTable(), id);

        afterDelete(id);

        return result;
    }
}
