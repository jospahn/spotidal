package spotidal.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spotidal.dao.DaoException;

import java.sql.Connection;

public abstract class AbstractDatabaseDAO {
    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected final Connection conn;

    protected abstract void createTable() throws DaoException;

    public AbstractDatabaseDAO(Connection conn) {
        this.conn = conn;
        createTable();
    }
}
