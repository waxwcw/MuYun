package net.ximatai.muyun.method.curd.std;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import net.ximatai.muyun.method.*;
import org.eclipse.microprofile.openapi.annotations.Operation;

import java.util.*;

public interface ISingleSelectAbility extends IDatabaseAbilityStd, IMetadataAbility {

    default boolean showCurrentDate() {
        return false;
    }

    default String getSelectOneRowSql() {
        return "select * from (%s) %s where 1=1 %s and %s = :id ".formatted(getSelectSql(), getMainTable(), "", getPK());
    }

    default String getSelectSql() {
        StringBuilder starSql = new StringBuilder("%s.*".formatted(getMainTable()));
        StringBuilder joinSql = new StringBuilder();
        String softDeleteSql = "";

        if (this instanceof IReferenceAbility referenceAbility) {
            referenceAbility.getReferenceList().forEach(info -> {
                String referenceTableTempName = "%s_%s".formatted(info.getReferenceTableName(), UUID.randomUUID().toString().substring(25));
                info.getTranslates().forEach((column, alias) -> {
                    starSql.append(",%s.%s as %s ".formatted(referenceTableTempName, column, alias));
                });
                StringBuilder other = new StringBuilder();
                info.getOtherConditions().forEach((column, value) -> {
                    other.append(" and %s.%s = %s ".formatted(referenceTableTempName, column, value));
                });

                if (info.isDeep()) {
                    joinSql.append("\n left join (%s) as %s on %s.%s = %s.%s %s "
                        .formatted(info.getDeepSelectSql(),
                            referenceTableTempName, getMainTable(), info.getRelationColumn(),
                            referenceTableTempName, info.getHitField(), other));
                } else {
                    String table = info.getReferenceFullTableName();
                    if (info.isReferenceCustomSelectSqlAbility()) { //自定义sql
                        table = info.getReferenceCustomSql();
                    }
                    joinSql.append("\n left join %s as %s on %s.%s = %s.%s %s "
                        .formatted(table, referenceTableTempName,
                            getMainTable(), info.getRelationColumn(),
                            referenceTableTempName, info.getHitField(), other));
                }
            });
        }

        String mainTable = getSchemaDotTable();
        if (this instanceof ICustomSelectSqlAbility ability) {
            mainTable = "(%s) as %s ".formatted(ability.getCustomSql(), getMainTable());
        }

        if (showCurrentDate()) {
            starSql.append(",current_date ");
        }

        return "select %s from %s %s where 1=1 %s ".formatted(starSql, mainTable, joinSql, softDeleteSql);
    }

    @GET
    @Path("/view/{id}")
    @Operation(summary = "查看指定的数据")
    default Map<String, Object> view(@PathParam("id") String id) {
        Map<String, Object> row = getDB().row(getSelectOneRowSql(), Map.of("id", id));

        if (row == null) {
            return null;
        }

        return row;
    }
}
