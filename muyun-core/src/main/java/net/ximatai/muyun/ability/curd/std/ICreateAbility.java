package net.ximatai.muyun.ability.curd.std;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import net.ximatai.muyun.ability.*;
import net.ximatai.muyun.core.exception.MuYunException;
import net.ximatai.muyun.database.builder.Column;
import net.ximatai.muyun.util.StringUtil;
import org.eclipse.microprofile.openapi.annotations.Operation;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 创建数据的能力
 */
public interface ICreateAbility extends IDatabaseAbilityStd, IMetadataAbility {

    default void beforeCreate(Map body) {

    }

    default void afterCreate(String id) {

    }

    @POST
    @Path("/create")
    @Transactional
    @Operation(summary = "新增数据", description = "返回新增数据ID")
    default String create(Map body) {
        beforeCreate(body);
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

        afterCreate(main);
        return main;
    }

    @POST
    @Path("/batchCreate")
    @Transactional
    @Operation(summary = "批量新增数据", description = "返回新增数据ID数组")
    default List<String> batchCreate(List<Map> list) {

        List<Map<String, ?>> dataList = new ArrayList<>();

        List<String> codeList = null;

        if (this instanceof ICodeGenerateAbility ability) {
            codeList = ability.generate(list);
        }

        for (int i = 0; i < list.size(); i++) {
            Map map = new HashMap<>(list.get(i));
            fitOutDefaultValue(map);

            if (codeList != null) {
                ICodeGenerateAbility ability = (ICodeGenerateAbility) this;
                String codeColumn = ability.getCodeColumn();
                String code = (String) map.get(codeColumn);
                if (code == null || code.isBlank()) {
                    map.put(codeColumn, codeList.get(i));
                }
            }

            dataList.add(map);
        }

        List<String> idList = getDB().insertList(getSchemaName(), getMainTable(), dataList);

        if (idList.size() != list.size()) {
            throw new MuYunException("数据插入不成功");
        }

        idList.forEach(id -> {
            afterCreate(id);
        });

        if (this instanceof IChildrenAbility childrenAbility) {
            int i = 0;
            for (Map body : list) {
                int finalI = i;
                childrenAbility.getChildren().forEach(childTableInfo -> {
                    String childAlias = childTableInfo.getChildAlias();
                    if (body.containsKey(childAlias) && body.get(childAlias) instanceof List<?> childrenList) {
                        childrenAbility.putChildTableList(idList.get(finalI), childAlias, childrenList);
                    }
                });
                i++;
            }
        }

        return idList;
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
