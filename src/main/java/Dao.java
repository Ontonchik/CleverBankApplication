import java.math.BigDecimal;
import java.sql.*;
import java.util.Arrays;

public class Dao {
    Connection connection;

    public void printNoMoneyException(){
        System.out.println("Недостаточно средств на счете для проведения операции");
    }

    public void sqlExceptionHandler(SQLException e){
        System.out.println("Ошибка при обращении в базу данных. Подробнее об ошибке:");
        System.out.println(e.getMessage());
    }

    public void init() {
        try {
            connection = DriverManager.getConnection("jdbc:postgresql://localhost/CleverBank", "postgres", "*Anton5615");
        }catch (SQLException e){
            sqlExceptionHandler(e);
        }
    }

    public void destroy() {
        try {
            connection.close();
        } catch (SQLException e){
            sqlExceptionHandler(e);
        }
    }

    public boolean checkAccess(Account currentUserAccount, String username, char[] password) {
        try {
            PreparedStatement statement = connection.prepareStatement("Select password, account_id from newtable where username = ?;");
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            boolean isCorrectPassword = resultSet.next() && Arrays.equals(resultSet.getString("password").toCharArray(), password);
            if(isCorrectPassword){
                currentUserAccount.setAccountId(resultSet.getInt("account_id"));
                return true;
            }
        } catch (SQLException e){
            sqlExceptionHandler(e);
        }
        return false;
    }

    public void withdraw(Account currentUserAccount, BigDecimal value){
        try {
            if(enoughMoneyCheck(currentUserAccount, value)) {
                PreparedStatement statement = connection.prepareStatement("Update newtable set cash = cash - ?  where account_id = ? and bank = Clever-bank;");
                statement.setBigDecimal(1, value);
                statement.setInt(2, currentUserAccount.getAccountId());
                statement.executeQuery();
            }
            else{
                printNoMoneyException();
            }
        }catch (SQLException e){
            sqlExceptionHandler(e);
        }
    }

    public void add(Account currentUserAccount, BigDecimal value){
        try {
            PreparedStatement statement = connection.prepareStatement("Update newtable set cash = cash + ?  where account_id = ? and bank = Clever-bank;");
            statement.setBigDecimal(1, value);
            statement.setInt(2, currentUserAccount.getAccountId());
            statement.executeQuery();
        }catch (SQLException e){
            sqlExceptionHandler(e);
        }
    }

    public void Transfer(Transaction transaction){
        try {
            if(isValidTransaction(transaction)) {
                PreparedStatement statement = connection.prepareStatement("Update newtable set cash = cash" +
                        " - ?  where account_id = ? and bank = Clever-bank, cash = cash + ?  where account_id = ? " +
                        "and bank = ?;");
                BigDecimal value = transaction.getValue();
                statement.setBigDecimal(1, value);
                statement.setInt(2, transaction.getCurrentUserAccount().getAccountId());
                statement.setBigDecimal(3, value);
                statement.setInt(4, transaction.getTransferUserAccount().getAccountId());
                statement.setString(5, transaction.getTransferUserAccount().bank.getName());
                statement.executeQuery();
                connection.commit();
            }
            connection.setAutoCommit(true);
        }catch (SQLException e){
            sqlExceptionHandler(e);
        }
    }

    public boolean enoughMoneyCheck(Account currentUserAccount, BigDecimal value) throws SQLException {
        connection.setAutoCommit(false);
        PreparedStatement statement = connection.prepareStatement("Select cash from newtable where account_id = ?;");
        statement.setInt(1, currentUserAccount.getAccountId());
        ResultSet resultSet = statement.executeQuery();
        return resultSet.next() && resultSet.getBigDecimal("cash").compareTo(value) >= 0;
    }

    public boolean isValidTransaction(Transaction transaction) throws SQLException {
        if(enoughMoneyCheck(transaction.getCurrentUserAccount(), transaction.getValue())){
            PreparedStatement statement = connection.prepareStatement("Select * from newtable where account_id = ? and bank = ?;");
            statement.setInt(1, transaction.getTransferUserAccount().getAccountId());
            statement.setString(1, transaction.getTransferUserAccount().getBank().getName());
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next() && resultSet.getString("bank").equals(transaction.getTransferUserAccount().bank.getName());
        }
        return false;
    }
}
