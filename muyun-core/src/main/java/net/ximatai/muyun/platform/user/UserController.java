package net.ximatai.muyun.platform.user;

import io.quarkus.runtime.Startup;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import net.ximatai.muyun.ability.IChildrenAbility;
import net.ximatai.muyun.ability.ICodeGenerateAbility;
import net.ximatai.muyun.ability.curd.std.*;
import net.ximatai.muyun.database.IDatabaseOperations;
import org.eclipse.microprofile.openapi.annotations.Operation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Startup
@Path("user")
public class UserController implements ISingleCreateAbility, IUpdateAbility , ISingleSelectAbility, IDeleteAbility {


    @Inject
    IDatabaseOperations databaseOperations;

    @Override
    public IDatabaseOperations getDatabaseOperations() {
        return databaseOperations;
    }

    @POST
    @Path("/create")
    @Transactional
    @Operation(summary = "创建用户", description = "返回新增数据ID")
    public String create(Map body) {

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

    @Override
    public String getMainTable() {
        return "user";
    }
    public Map<String, Object> check(String username) {
        String sql = "select * from %s.%s where account = :account".formatted(getSchemaName(), getMainTable());
        Map<String, Object> row = getDB().row(sql, Map.of("account", username));
        return row;
    }

}
