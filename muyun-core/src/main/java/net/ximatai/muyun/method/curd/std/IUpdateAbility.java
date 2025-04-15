package net.ximatai.muyun.method.curd.std;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import net.ximatai.muyun.method.IDatabaseAbilityStd;
import net.ximatai.muyun.method.IMetadataAbility;
import org.eclipse.microprofile.openapi.annotations.Operation;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 修改数据的能力
 */
public interface IUpdateAbility extends IDatabaseAbilityStd, IMetadataAbility {

    /**
     * @deprecated 请使用带 Map 参数的 beforeUpdate 方法，这个方法将在未来的版本中移除
     */
    @Deprecated(forRemoval = true)
    default void beforeUpdate(String id) {

    }

    default void beforeUpdate(String id, Optional<Map> body) {

    }

    default void afterUpdate(String id) {

    }

    @POST
    @Path("/update/{id}")
    @Transactional
    @Operation(summary = "修改数据", description = "返回被修改数据的数量，正常为1")
    default Integer update(@PathParam("id") String id, Map body) {
        beforeUpdate(id);
        beforeUpdate(id, Optional.of(body));
        HashMap map = new HashMap(body);
        map.put(getPK(), id);
        map.put("t_update", LocalDateTime.now());

        int result = getDB().updateItem(getSchemaName(), getMainTable(), map);

        afterUpdate(id);

        return result;
    }
}
