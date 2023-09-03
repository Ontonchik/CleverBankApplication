import java.math.BigDecimal;
import java.sql.*;
import java.util.Arrays;
import java.util.concurrent.locks.Lock;

/**
 * Only class that works with db and operations to db
 */
public class Dao {

    /**
     * lock to make db requests synchronized
     */
    Controller controller;
    Connection connection;
    Lock lock;

    Dao(Lock l, Controller c){
        controller = c;
        lock = l;
        init();
    }

    /**
     * Asks View to print info about lack of money
     */
    public void printNoMoneyException(){
        controller.view.printLackOfMoney();
    }

    /**
     * Asks View to print info about sql exception
     * @param e is sql exception that shows smth wrong with db
     */
    public void sqlExceptionHandler(SQLException e){
        controller.view.printSqlException(e);
    }

    /**
     * Function that starts connection with db
     */
    public void init() {
        try {
            connection = DriverManager.getConnection("jdbc:postgresql://localhost/CleverBank", "postgres", "*Anton5615");
        }catch (SQLException e){
            sqlExceptionHandler(e);
        }
    }

    /**
     * Function that close connection with db
     */
    public void destroy() {
        try {
            connection.close();
        } catch (SQLException e){
            sqlExceptionHandler(e);
        }
    }

    /**
     * Function that validates user data
     * @param currentUserAccount is null but if data is correct it will be uploaded with this data
     * @param username is String user's name of the account
     * @param password is user's password to the account
     * @return is user data is correct
     */
    public boolean checkAccess(Account currentUserAccount, String username, char[] password) {
        lock.lock();
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
        finally {
            lock.unlock();
        }
        return false;
    }

    /**
     * This function removes money from users account in db
     * @param currentUserAccount is implementation of current user Clever-bank account
     * @param value is value for transaction
     */
    public void withdraw(Account currentUserAccount, BigDecimal value){
        lock.lock();
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
        finally {
            lock.unlock();
        }
    }

    /**
     * This function adds money from users account in db
     * @param currentUserAccount is implementation of current user Clever-bank account
     * @param value is value for transaction
     */
    public void add(Account currentUserAccount, BigDecimal value){
        lock.lock();
        try {
            PreparedStatement statement = connection.prepareStatement("Update newtable set cash = cash + ?  where account_id = ? and bank = Clever-bank;");
            statement.setBigDecimal(1, value);
            statement.setInt(2, currentUserAccount.getAccountId());
            statement.executeQuery();
        }catch (SQLException e){
            sqlExceptionHandler(e);
        }
        finally {
            lock.unlock();
        }
    }

    /**
     * Function that sends money to other account in db
     * @param transaction is current transaction object with information about it
     */
    public void Transfer(Transaction transaction){
        lock.lock();
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
        finally {
            lock.unlock();
        }
    }

    /**
     * Function that check if value of money on user account is enough to conduct the transaction
     * @param currentUserAccount is implementation of user's account i Clever-bank
     * @param value is transaction value
     * @return is whether money on user account enough or no
     * @throws SQLException if db connection have errors
     */
    public boolean enoughMoneyCheck(Account currentUserAccount, BigDecimal value) throws SQLException {
        lock.lock();
        connection.setAutoCommit(false);
        PreparedStatement statement = connection.prepareStatement("Select cash from newtable where account_id = ?;");
        statement.setInt(1, currentUserAccount.getAccountId());
        ResultSet resultSet = statement.executeQuery();
        lock.unlock();
        return resultSet.next() && resultSet.getBigDecimal("cash").compareTo(value) >= 0;
    }

    /**
     * Check if there is such bank, and such account in this bank to which Clever-bank user wants to send money
     * @param transaction is current transaction object with information about it
     * @return whether there is such account and bank or no
     * @throws SQLException if there is db errors
     */
    public boolean isValidTransaction(Transaction transaction) throws SQLException {
        lock.lock();
        if(enoughMoneyCheck(transaction.getCurrentUserAccount(), transaction.getValue())){
            PreparedStatement statement = connection.prepareStatement("Select * from newtable where account_id = ? and bank = ?;");
            statement.setInt(1, transaction.getTransferUserAccount().getAccountId());
            statement.setString(2, transaction.getTransferUserAccount().getBank().getName());
            ResultSet resultSet = statement.executeQuery();
            lock.unlock();
            return resultSet.next() && resultSet.getString("bank").equals(transaction.getTransferUserAccount().bank.getName());
        }
        lock.unlock();
        return false;
    }

    /**
     * Adds money on every Clever-bank account at the end of the month
     * @param monthValue shows how much money we should add (default 1.01 because cash is multiplied by it)
     */
    public void monthAdd(BigDecimal monthValue){
        lock.lock();
        try {
            PreparedStatement statement = connection.prepareStatement("Update newtable set cash = cash * ?  where bank = Clever-bank;");
            statement.setBigDecimal(1, monthValue);
            statement.executeQuery();
        }catch (SQLException e){
            sqlExceptionHandler(e);
        }
        finally {
            lock.unlock();
        }
    }
}
