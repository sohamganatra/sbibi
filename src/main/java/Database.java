import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

class Database {
    private static String USERNAME;
    private static String PASSWORD;
    private static String DBNAME;
    private static boolean CREDENTIALS_READ = false;
    private static Connection CONNECTION = null;
    private static Logger LOGGER = Logger.getLogger(Database.class);

    Database() {
    }

    private static void connectoToDatabase() {
        LOGGER.info("Connecting to database");

        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (ClassNotFoundException var2) {
            LOGGER.error("Error: Oracle JDBC/ODBC driver not found. Please Add the jar in libraries: ", var2);
            return;
        }

        try {
            CONNECTION = DriverManager.getConnection("jdbc:oracle:thin:@10.0.247.132:1521:" + DBNAME, USERNAME, PASSWORD);
        } catch (SQLException var1) {
            LOGGER.error("Error: Connection Failed! check output console: ", var1);
            return;
        }

        if (CONNECTION == null) {
            LOGGER.error("Failed to make connection!");
        }

    }

    private static void dropDatabaseConnection() {
        LOGGER.info("Closing database connection");

        try {
            if (CONNECTION != null) {
                CONNECTION.close();
            }
        } catch (SQLException var1) {
            LOGGER.error("Error: Exception in closing connection", var1);
        }

    }
    static HashMap<String,String> convertSQLToMAP(String sql){
        LOGGER.info("Executing SQL: " + sql);

        try {
            readCredentials();
            connectoToDatabase();
            PreparedStatement preparedStatement = CONNECTION.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();
            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
            HashMap<String, String> mapData = new HashMap<>();
            resultSet.next();
            for(Integer columnIndex = 1;columnIndex <= resultSetMetaData.getColumnCount();columnIndex++){
                LOGGER.info("columnIndex: " + columnIndex);
                mapData.put(resultSetMetaData.getColumnName(columnIndex).split("__")[0],resultSet.getString(columnIndex));
            }
            return mapData;
        }  catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    static String runSQLWithoutTable(String sql) {
        LOGGER.info("Executing SQL: " + sql);

        try {
            readCredentials();
            connectoToDatabase();
            PreparedStatement preparedStatement = CONNECTION.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();
            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
            StringBuilder result = new StringBuilder();

            byte i;
            do {
                if (!resultSet.next()) {
                    String var12 = "0";
                    return var12;
                }

                i = 1;
            } while(i > resultSetMetaData.getColumnCount());

            result.append(resultSet.getString(i));
            String var6 = result.toString();
            return var6;
        } catch (Exception var10) {
            LOGGER.info(var10.toString());
        } finally {
            dropDatabaseConnection();
        }

        return "0";
    }

    private static void readCredentials() {
        if (!CREDENTIALS_READ) {
            File DBCredentialsFile = new File(Database.class.getResource(Config.DBCredentialsFile).getFile());

            try {
                List<String> credentials = FileUtils.readLines(DBCredentialsFile, "UTF-8");

                assert credentials.size() >= 2;

                USERNAME = credentials.get(0);
                PASSWORD = credentials.get(1);
                DBNAME = credentials.get(2);
                CREDENTIALS_READ = true;
                LOGGER.info("Username: " + USERNAME);
                LOGGER.info("Password: " + PASSWORD);
                LOGGER.info("DBName: " + DBNAME);
            } catch (IOException var2) {
                var2.printStackTrace();
            }

        }
    }
}
