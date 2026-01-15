package at.albin.VELLSCAFFOLDING.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {

    static {
        try {
            Class.forName("org.mariadb.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static final String HOST = "localhost";
    private static final String DATABASE = "users_db";
    private static final String USER = "db_vellscaffolding";
    private static final String PASSWORD = "<SECRET>";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                "jdbc:mariadb://" + HOST + ":3306/" + DATABASE,
                USER,
                PASSWORD
        );
    }
}
