package net.ximatai.muyun.method;

import net.ximatai.muyun.method.curd.std.CustomSelectSqlMethod;
import net.ximatai.muyun.method.curd.std.SelectMethod;
import net.ximatai.muyun.database.exception.MyDatabaseException;
import net.ximatai.muyun.model.ReferenceInfo;

import java.util.List;

/**
 * 可以被关联的能力
 */
public interface ReferableMethod extends MetadataMethod, LabelMethod, SelectMethod {

    default String getKeyColumn() {
        return getPK();
    }

    default List<String> getOpenColumns() {
        return List.of();
    }

    default boolean checkColumnExist(String column) {
        if (!(this instanceof CustomSelectSqlMethod) && !checkColumn(column)) {
            throw new MyDatabaseException("根据引用关系，要求 %s 必须含有 %s 字段".formatted(getMainTable(), column));
        }
        return true;
    }

    default ReferenceInfo toReferenceInfo(String foreignKey) {
        return new ReferenceInfo(foreignKey, this).autoPackage();
    }

}
