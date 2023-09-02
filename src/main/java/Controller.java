import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Controller {
    Scanner scanner;
    boolean flag;
    Lock lock = new ReentrantLock();
    Condition condition = lock.newCondition();
    int MAX_PASSWORD_LENGTH = 40;
    Account currentUserAccount = new Account();
    View view;
    Dao dao;

    public void printHello(){
        view.printHello();
    }

    public String getUsername(){
        return scanner.nextLine();
    }

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

    public void checkUser(){
        String username = getUsername();
        char[] password = getPassword();
        if(!dao.checkAccess(currentUserAccount, username, password)){
            Arrays.fill(password, ' ');
            return;
        }
        currentUserAccount.setUser(new User(username));
        Arrays.fill(password, ' ');
    }

    public void Options(){
        view.printSwitch();
        int option = scanner.nextInt();
        switch (option){
            case 1:
                withdrawMoney();
            case 2:
                addMoney();
            case 3:
                cleverBankTransfer();
            case 4:
                otherBankTransfer();
        }
    }

    public void withdrawMoney(){
        BigDecimal value = getOperationValue();
        dao.withdraw(currentUserAccount, value);
    }

    public void addMoney(){
        BigDecimal value = getOperationValue();
        dao.add(currentUserAccount, value);
    }

    public void cleverBankTransfer(){
        BigDecimal value = getOperationValue();
        Account transferUserAccount = getCleverBankTransfer();
        String thisBank = "Clever-bank";
        transferUserAccount.setBank(new Bank(thisBank));
        Transaction transaction = new Transaction(currentUserAccount, transferUserAccount, value);
        dao.Transfer(transaction);
    }

    public void otherBankTransfer(){
        BigDecimal value = getOperationValue();
        String bankName = getOtherBankName();
        Account transferUserAccount = getOtherBankTransfer();
        transferUserAccount.setBank(new Bank(bankName));
        Transaction transaction = new Transaction(currentUserAccount, transferUserAccount, value);
        dao.Transfer(transaction);
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

    public void monthCheck() throws InterruptedException {
        lock.lock();
        flag = true;
        while(flag){
            condition.wait(30000);
            if(flag){

            }
        }
        lock.unlock();
    }

    public void throwMoneyException(){
        view.printMoneyException();
    }
}