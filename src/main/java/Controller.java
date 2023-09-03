import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Controller class is used as part of the mvc pattern and is a program operation controller
 * @author Anton Chigir
 */
public class Controller implements Runnable {
    Yaml yaml = new Yaml();
    /**
     * scanner that get all info from user
     */
    Scanner scanner = new Scanner(System.in);
    /**
     * flag that tell 2-nd thread to stop working after 1-st ends all needed functions
     */
    boolean flag;
    /**
     * lock to make db requests synchronized
     */
    Lock lock = new ReentrantLock();
    Condition condition = lock.newCondition();
    int MAX_PASSWORD_LENGTH = 40;
    Account currentUserAccount = new Account();
    /**
     * objects of classes view and dao for mvc and dao patterns correct work
     */
    View view;
    Dao dao;

    /**
     * calls function from class View that print program's start message
     */
    public void printHello(){
        view.printHello();
    }

    /**
     * get username from console
     * @return String username of current program user
     */
    public String getUsername(){
        return scanner.nextLine();
    }

    /**
     * Character-by-character reads from the console the password from the account of the current Clever-bank user
     * @return password of Clever-bank account from current user
     */
    public char[] getPassword() {
        char[] password = new char[MAX_PASSWORD_LENGTH];
        int i = 0;
        try {
            while (scanner.hasNext()) {
                password[i] = scanner.next().charAt(0);
                i++;
            }
        } catch (ArrayIndexOutOfBoundsException e){
            view.printPasswordException();
        }
        return password;
    }

    /**
     * Accesses the dao class to verify user data
     */
    public void checkUser(){
        String username = getUsername();
        char[] password = getPassword();
        if(!dao.checkAccess(currentUserAccount, username, password)){
            Arrays.fill(password, ' ');
            currentUserAccount.setMUser(new User(username));
            return;
        }
        Arrays.fill(password, ' ');
    }

    /**
     * Responsible for the further behavior of the program after the user selects the desired operation
     */
    public void menu(){
        view.printSwitch();
        int option = scanner.nextInt();
        Transaction transaction = new Transaction(null, null, null, null);
        switch (option){
            case 1:
                transaction = withdrawMoney();
            case 2:
                transaction = addMoney();
            case 3:
                transaction = cleverBankTransfer();
            case 4:
                transaction = otherBankTransfer();
            default:
                printNoSuchOption();
                menu();
        }
        try {
            HashMap<String, Object> map = yaml.load(new FileInputStream("src/main/resources/config.yml"));
            printCheck(transaction, map);
        } catch (IOException e){
            System.out.println(e.getMessage());
        }
        printContinueAsk();
        if(getLikeToContinue()){
            menu();
        }
    }

    /**
     * This function responsible for withdrawing money from the account with the help of dao
     * @return Transaction object that have information about transaction
     */
    public Transaction withdrawMoney(){
        BigDecimal value = getOperationValue();
        dao.withdraw(currentUserAccount, value);
        return new Transaction(currentUserAccount, null, value, "Снятие со счета");
    }

    /**
     * This function responsible for adding money on the account with the help of dao
     * @return Transaction object that have information about transaction
     */
    public Transaction addMoney(){
        BigDecimal value = getOperationValue();
        dao.add(currentUserAccount, value);
        return new Transaction(currentUserAccount, null, value, "Пополнение счета");
    }

    /**
     * This function responsible for transfer money to other Clever-bank account with the help of dao
     * @return Transaction object that have information about transaction
     */
    public Transaction cleverBankTransfer(){
        BigDecimal value = getOperationValue();
        Account transferUserAccount = getCleverBankTransfer();
        String thisBank = "Clever-bank";
        transferUserAccount.setBank(new Bank(thisBank));
        Transaction transaction = new Transaction(currentUserAccount, transferUserAccount, value, "Перевод");
        dao.Transfer(transaction);
        return transaction;
    }

    /**
     * This function responsible for transfer money on other bank account with the help of dao
     * @return Transaction object that have information about transaction
     */
    public Transaction otherBankTransfer(){
        BigDecimal value = getOperationValue();
        String bankName = getOtherBankName();
        Account transferUserAccount = getOtherBankTransfer();
        transferUserAccount.setBank(new Bank(bankName));
        Transaction transaction = new Transaction(currentUserAccount, transferUserAccount, value, "Перевод");
        dao.Transfer(transaction);
        return transaction;
    }

    /**
     * This function finds out from the user how much money should be involved in the operation
     * @return BigDecimal value
     */
    public BigDecimal getOperationValue(){
        view.printMoneyValue();
        BigDecimal value = scanner.nextBigDecimal();
        while (value.compareTo(new BigDecimal(0)) < 0) {
            throwMoneyException();
            view.printMoneyValue();
            value = scanner.nextBigDecimal();
        }
        return value;
    }

    /**
     * the function is responsible for getting the address of the clever-bank user account to which the current user wants to send money
     * @return Account object that is implementation of Clever-bank account on which user want to transfer money
     */
    public Account getCleverBankTransfer(){
        view.printCleverBankTransfer();
        int transferId = scanner.nextInt();
        return new Account(transferId);
    }

    /**
     * Receives from the user the name of the bank to whose account the user wants to send money
     * @return String other bank name
     */
    public String getOtherBankName(){
        view.printOtherBankTransfer();
        return scanner.nextLine();
    }

    /**
     * Receives from the user the id of the account from other bank on which the user wants to send money
     * @return Account from other bank
     */
    public Account getOtherBankTransfer(){
        return new Account(scanner.nextInt());
    }

    /**
     * With the help of view asks user if he wants to continue after transaction, get his answer and return it in boolean way
     * @return boolean flag that means user wants to continue or not
     */
    public boolean getLikeToContinue(){
        switch (scanner.nextInt()){
            case 1:
                return true;
            case 2:
                return false;
            default:
                printNoSuchOption();
                return getLikeToContinue();
        }
    }

    /**
     * Asks user if he wants to continue
     */
    public void printContinueAsk(){
        view.printContinue();
    }

    /**
     * Function that checks if now is the end of the month 23:30 once per 30 seconds. If it is function add money on every Clever-bank account
     * @param val is taken from config.yml and means how many percents of money we should give in the end of the month
     */
    public void monthCheck(BigDecimal val){
        try {
            lock.lock();
            flag = true;
            while(flag){
                condition.wait(30000);
                if(flag){
                    if(LocalDate.now().getDayOfMonth() == LocalDate.now().lengthOfMonth() &&
                            LocalTime.now().getHour() == 23 && LocalTime.now().getMinute() == 30){
                        dao.monthAdd(val);
                        condition.wait(60000);
                    }
                }
            }
            lock.unlock();
        }catch (InterruptedException e){
            System.out.println(e.getMessage());
        }
    }

    /**
     * Function that is throwing exception because of lack of money on account while checking transfer
     */
    public void throwMoneyException(){
        view.printMoneyException();
    }

    /**
     * Ask other function from View to print check on current successful transaction
     * @param transaction is object that gives us info about current transaction, user and transfer
     * @param map is information from config.yml
     */
    public void printCheck(Transaction transaction, HashMap<String, Object> map){
        view.printCheck(transaction, map, yaml);
    }

    /**
     * Ask other function from View to send message to user and help him to choose correct option
     */
    public void printNoSuchOption(){
        view.printNoSuchOption();
    }

    /**
     * Function that controls 1-st thread work. It calls all functions needed for correct work
     */
    public void begin(){
        view = new View();
        dao = new Dao(lock, this);
        printHello();
        checkUser();
        menu();
        flag = false;
        dao.destroy();
    }

    /**
     * this function is controlling 2-nd thread and sends it to monthCheck()
     */
    @Override
    public void run() {
        try {
            HashMap<String, Object> map = yaml.load(new FileInputStream("src/main/resources/config.yml"));
            BigDecimal val = new BigDecimal(map.get("monthValue").toString());
            monthCheck(val);
        }catch (IOException e){
            System.out.println(e.getMessage());
        }
    }
}