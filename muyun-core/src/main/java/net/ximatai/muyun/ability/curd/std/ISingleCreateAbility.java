package net.ximatai.muyun.ability.curd.std;

import jakarta.annotation.security.RolesAllowed;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import net.ximatai.muyun.ability.*;
import net.ximatai.muyun.database.builder.Column;
import net.ximatai.muyun.util.StringUtil;
import org.eclipse.microprofile.openapi.annotations.Operation;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface ISingleCreateAbility extends IDatabaseAbilityStd, IMetadataAbility {
    @POST
    @Path("/create")
    @Transactional
    @Operation(summary = "新增数据", description = "返回新增数据ID")
    @RolesAllowed("user")
    default String create(Map body) {

        HashMap map = new HashMap<>(body);
        fitOutDefaultValue(map);

        if (this instanceof ICodeGenerateAbility ability) {
            String codeColumn = ability.getCodeColumn();
            String code = (String) map.get(codeColumn);
            if (code == null || code.isBlank()) {
                map.put(codeColumn, ability.generateCode(body));
            }
        }


        String main = getDB().insertItem(getSchemaName(), getMainTable(), map);

        if (this instanceof IChildrenAbility childrenAbility) {
            childrenAbility.getChildren().forEach(childTableInfo -> {
                String childAlias = childTableInfo.getChildAlias();
                if (body.containsKey(childAlias) && body.get(childAlias) instanceof List<?> list) {
                    childrenAbility.putChildTableList(main, childAlias, list);
                }
            });
        }

        return main;
    }

    default void fitOutDefaultValue(Map body) {
        if (!body.containsKey("t_create")) {
            LocalDateTime now = LocalDateTime.now();
            body.put("t_create", now);
            body.put("t_update", now);
        }

        if (this instanceof ITreeAbility ability) {
            Column pidColumn = ability.getParentKeyColumn();
            if (StringUtil.isBlank(body.get(pidColumn.getName()))) {
                body.put(pidColumn.getName(), pidColumn.getDefaultValue());
            }
        }

    }
}
