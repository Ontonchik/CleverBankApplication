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
    Scanner scanner = new Scanner(System.in);
    boolean flag;
    Lock lock = new ReentrantLock();
    Condition condition = lock.newCondition();
    int MAX_PASSWORD_LENGTH = 40;
    Account currentUserAccount = new Account();
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
            currentUserAccount.setUser(new User(username));
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

    public Transaction withdrawMoney(){
        BigDecimal value = getOperationValue();
        dao.withdraw(currentUserAccount, value);
        return new Transaction(currentUserAccount, null, value, "Снятие со счета");
    }

    public Transaction addMoney(){
        BigDecimal value = getOperationValue();
        dao.add(currentUserAccount, value);
        return new Transaction(currentUserAccount, null, value, "Пополнение счета");
    }

    public Transaction cleverBankTransfer(){
        BigDecimal value = getOperationValue();
        Account transferUserAccount = getCleverBankTransfer();
        String thisBank = "Clever-bank";
        transferUserAccount.setBank(new Bank(thisBank));
        Transaction transaction = new Transaction(currentUserAccount, transferUserAccount, value, "Перевод");
        dao.Transfer(transaction);
        return transaction;
    }

    public Transaction otherBankTransfer(){
        BigDecimal value = getOperationValue();
        String bankName = getOtherBankName();
        Account transferUserAccount = getOtherBankTransfer();
        transferUserAccount.setBank(new Bank(bankName));
        Transaction transaction = new Transaction(currentUserAccount, transferUserAccount, value, "Перевод");
        dao.Transfer(transaction);
        return transaction;
    }

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

    public Account getCleverBankTransfer(){
        view.printCleverBankTransfer();
        int transferId = scanner.nextInt();
        return new Account(transferId);
    }

    public String getOtherBankName(){
        view.printOtherBankTransfer();
        return scanner.nextLine();
    }

    public Account getOtherBankTransfer(){
        return new Account(scanner.nextInt());
    }

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

    public void printContinueAsk(){
        view.printContinue();
    }

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
                    }
                }
            }
            lock.unlock();
        }catch (InterruptedException e){
            System.out.println(e.getMessage());
        }
    }

    public void throwMoneyException(){
        view.printMoneyException();
    }

    public void printCheck(Transaction transaction, HashMap<String, Object> map){
        view.printCheck(transaction, map, yaml);
    }

    public void printNoSuchOption(){
        view.printNoSuchOption();
    }

    public void begin(){
        view = new View();
        dao = new Dao(lock);
        printHello();
        checkUser();
        menu();
        dao.destroy();
    }

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