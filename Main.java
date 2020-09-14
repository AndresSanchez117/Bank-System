package banking;

import java.sql.*;

public class Main {
    public static void main(String[] args) {
        // TODO: check that args[1] is what is expected
        //String db = "bank.db";
        createNewDatabase(args[1]);

        BankSystem bankSystem = new BankSystem(args[1]);
        bankSystem.menu();
    }

    public static void createNewDatabase(String filename) {
        String url = "jdbc:sqlite:" + filename;

        String createTableSQL = "CREATE TABLE IF NOT EXISTS card (\n"
                + "                 id INTEGER,\n"
                + "                 number TEXT,\n"
                + "                 pin TEXT,\n"
                + "                 balance INTEGER DEFAULT 0\n"
                + ");";

        try (Connection connection = DriverManager.getConnection(url)) {
            if (connection != null) {
                Statement statement = connection.createStatement();
                statement.execute(createTableSQL);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
