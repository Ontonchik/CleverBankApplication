import java.sql.*;
import java.util.Arrays;

public class Dao {
    Connection connection;

    public void sqlExceptionHandler(){
        System.out.println("Неполадки в работе базы данных");
    }

    public void init() {
        try {
            connection = DriverManager.getConnection("jdbc:postgresql://localhost/CleverBank", "postgres", "*Anton5615");
        }catch (SQLException e){
            sqlExceptionHandler();
        }
    }

    public void destroy() {
        try {
            connection.close();
        } catch (SQLException e){
            sqlExceptionHandler();
        }
    }

    public boolean checkAccess(String username, char[] password) {
        try {
            PreparedStatement statement = connection.prepareStatement("Select password from newtable where username = ?;");
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next() && Arrays.equals(resultSet.getString("password").toCharArray(), password);
        } catch (SQLException e){
            sqlExceptionHandler();
            return false;
        }
    }


}
