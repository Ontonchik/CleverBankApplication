import java.math.BigDecimal;
import java.sql.*;
import java.util.Arrays;

public class Dao {
    Connection connection;

    public void sqlExceptionHandler(){
        System.out.println("Ошибка при обращении в базу данных");
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

    public void withdraw(User user, BigDecimal value){
        try {
            PreparedStatement statement = connection.prepareStatement("Update newtable set cash = cash - ?  where username = ? and bank = Clever-bank;");
            statement.setBigDecimal(1, value);
            statement.setString(2, user.getUsername());
            ResultSet resultSet = statement.executeQuery();
        }catch (SQLException e){
            sqlExceptionHandler();
        }
    }



}
