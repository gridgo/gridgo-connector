package io.gridgo.connector.mysql;

import io.gridgo.connector.mysql.support.Helper;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import snaq.db.ConnectionPool;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class MainTest {
    static String url =  getUrl("localhost", "3306", "root", "ManhCuong22293", "test");

    private static String getUrl(String host, String port, String user, String password, String schema){
        return "jdbc:mysql://" + host +
                ":" + port +
                "/" + schema + "?" +
                "user=" + user + "&" +
                "password=" + password;
    }
    public static void main(String[] args) throws SQLException {
        testJDBI();
    }

    public static void testJDBI() throws SQLException {
        ConnectionPool pool = new ConnectionPool("local", 5, 10, 30, 180, url, "root", "ManhCuong22293");
        Jdbi jdbi = Jdbi.create(pool::getConnection);
        Handle handle = jdbi.open();
        ResultSet rs = handle.createQuery("select name, age from test_table")
                .execute((supplier, context) -> supplier.get().executeQuery());
        List<Map<String, Object>> rows = Helper.resultSetAsList(rs);
        System.out.println(rows);
    }
}
