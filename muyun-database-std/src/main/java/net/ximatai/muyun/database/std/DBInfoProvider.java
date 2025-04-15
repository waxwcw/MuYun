package net.ximatai.muyun.database.std;

import jakarta.inject.Inject;
import net.ximatai.muyun.database.metadata.DBInfo;
import org.jdbi.v3.core.Jdbi;

public class DBInfoProvider implements net.ximatai.muyun.database.DBInfoProvider {

    private Jdbi jdbi;
    private DBInfo dbInfo;

    @Override
    public Jdbi getJdbi() {
        return jdbi;
    }

    @Inject
    @Override
    public void setJdbi(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    @Override
    public void resetDBInfo() {
        dbInfo = null;
    }

    @Override
    public DBInfo getDBInfo() {
        if (dbInfo == null) {
            dbInfo = net.ximatai.muyun.database.DBInfoProvider.super.getDBInfo();
        }
        return dbInfo;
    }

}
