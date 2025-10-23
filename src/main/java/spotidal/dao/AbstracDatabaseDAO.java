package spotidal.dao;

import java.sql.Connection;

public abstract class AbstracDatabaseDAO {
    protected final Connection conn;

    public AbstracDatabaseDAO(Connection conn) {
        this.conn = conn;
    }
}
