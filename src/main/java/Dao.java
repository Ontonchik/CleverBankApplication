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
            PreparedStatement statement = connection.prepareStatement("Select password, id from newtable where username = ?;");
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            boolean isCorrectPassword = resultSet.next() && Arrays.equals(resultSet.getString("password").toCharArray(), password);
            if(isCorrectPassword){
                currentUserAccount.setMAccountId(resultSet.getInt("id"));
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
            connection.setAutoCommit(false);
            if(enoughMoneyCheck(currentUserAccount, value)) {
                PreparedStatement statement = connection.prepareStatement("Update newtable set cash = cash - ?  where id = ? and bank_name = 'Clever-bank';");
                statement.setBigDecimal(1, value);
                statement.setInt(2, currentUserAccount.getMAccountId());
                statement.executeUpdate();
            }
            else{
                printNoMoneyException();
            }
            connection.commit();
            connection.setAutoCommit(true);
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
            connection.setAutoCommit(false);
            PreparedStatement statement = connection.prepareStatement("Update newtable set cash = cash + ?  where id = ? and bank_name = 'Clever-bank';");
            statement.setBigDecimal(1, value);
            statement.setInt(2, currentUserAccount.getMAccountId());
            statement.executeUpdate();
            connection.commit();
            connection.setAutoCommit(true);
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
            connection.setAutoCommit(false);
            if(isValidTransaction(transaction)) {
                PreparedStatement statement1 = connection.prepareStatement("Update newtable set cash = cash" +
                        " - ?  where id = ? and bank_name = 'Clever-bank';");
                BigDecimal value = transaction.getMValue();
                statement1.setBigDecimal(1, value);
                statement1.setInt(2, transaction.getMCurrentUserAccount().getMAccountId());
                PreparedStatement statement2 = connection.prepareStatement("Update newtable set cash = cash + ?  where id = ? " +
                        "and bank_name = ?;");
                statement2.setBigDecimal(1, value);
                statement2.setInt(2, transaction.getMTransferUserAccount().getMAccountId());
                statement2.setString(3, transaction.getMTransferUserAccount().bank.getMName());
                statement1.executeUpdate();
                statement2.executeUpdate();
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
        PreparedStatement statement = connection.prepareStatement("Select cash from newtable where id = ?;");
        statement.setInt(1, currentUserAccount.getMAccountId());
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
        if(enoughMoneyCheck(transaction.getMCurrentUserAccount(), transaction.getMValue())){
            PreparedStatement statement = connection.prepareStatement("Select * from newtable where id = ? and bank_name = ?;");
            statement.setInt(1, transaction.getMTransferUserAccount().getMAccountId());
            statement.setString(2, transaction.getMTransferUserAccount().getBank().getMName());
            ResultSet resultSet = statement.executeQuery();
            lock.unlock();
            return resultSet.next() && resultSet.getString("bank_name").equals(transaction.getMTransferUserAccount().bank.getMName());
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
            connection.setAutoCommit(false);
            PreparedStatement statement = connection.prepareStatement("Update newtable set cash = cash * ?  where bank_name = 'Clever-bank';");
            statement.setBigDecimal(1, monthValue);
            statement.executeUpdate();
            connection.commit();
            connection.setAutoCommit(true);
        }catch (SQLException e){
            sqlExceptionHandler(e);
        }
        finally {
            lock.unlock();
        }
    }
}
