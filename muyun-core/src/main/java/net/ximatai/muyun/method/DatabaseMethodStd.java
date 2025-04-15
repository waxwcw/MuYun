package net.ximatai.muyun.method;

import net.ximatai.muyun.database.DBOperations;
import net.ximatai.muyun.database.DBOperationsStd;

/**
 * 数据库操作能力（标准JDBC同步版）
 */
public interface DatabaseMethodStd extends DatabaseMethod {

    DBOperations getDatabaseOperations();

    default DBOperationsStd getDB() {
        return (DBOperationsStd) getDatabaseOperations();
    }
}
