package net.ximatai.muyun.method.curd.std;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import net.ximatai.muyun.method.*;
import net.ximatai.muyun.core.exception.MuYunException;
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
        String main = getDB().insertItem(getSchemaName(), getMainTable(), map);
        afterCreate(main);
        return main;
    }

    @POST
    @Path("/batchCreate")
    @Transactional
    @Operation(summary = "批量新增数据", description = "返回新增数据ID数组")
    default List<String> batchCreate(List<Map> list) {

        List<Map<String, ?>> dataList = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            Map map = new HashMap<>(list.get(i));
            fitOutDefaultValue(map);

            dataList.add(map);
        }

        List<String> idList = getDB().insertList(getSchemaName(), getMainTable(), dataList);

        if (idList.size() != list.size()) {
            throw new MuYunException("数据插入不成功");
        }

        idList.forEach(id -> {
            afterCreate(id);
        });

        return idList;
    }

    default void fitOutDefaultValue(Map body) {
        if (!body.containsKey("t_create")) {
            LocalDateTime now = LocalDateTime.now();
            body.put("t_create", now);
            body.put("t_update", now);
        }
    }

}
