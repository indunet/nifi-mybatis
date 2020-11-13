package org.indunet.nifi;

import org.apache.nifi.dbcp.DBCPService;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.logging.Logger;

public class DbcpServiceAdapter implements DataSource {
    protected DBCPService dbcpService;

    public DbcpServiceAdapter(DBCPService dbcpService) {
        this.dbcpService = dbcpService;
    }

    @Override
    public Connection getConnection() {
        return this.dbcpService.getConnection();
    }

    @Override
    public Connection getConnection(String username, String password) {
        return this.dbcpService.getConnection();
    }

    @Override
    public <T> T unwrap(Class<T> iface) {
        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) {
        return false;
    }

    @Override
    public PrintWriter getLogWriter() {
        return null;
    }

    @Override
    public void setLogWriter(PrintWriter out) {

    }

    @Override
    public void setLoginTimeout(int seconds) {

    }

    @Override
    public int getLoginTimeout() {
        return 0;
    }

    @Override
    public Logger getParentLogger() {
        return null;
    }
}
