import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Controller {
    Lock lock = new ReentrantLock();
    Condition condition = lock.newCondition();
    int MAX_PASSWORD_LENGTH = 40;
    User currentUser = new User();
    View view;
    Dao dao;

    public void printHello(){
        view.printHello();
    }

    public String getUsername(Scanner scanner){
        return scanner.nextLine();
    }

    public char[] getPassword(Scanner scanner) {
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

    public void checkUser(Scanner scanner){
        String username = getUsername(scanner);
        char[] password = getPassword(scanner);
        if(!dao.checkAccess(username, password)){
            Arrays.fill(password, ' ');
            return;
        }
        currentUser.setUsername(username);
        currentUser.setPassword(password);
        Arrays.fill(password, ' ');
    }

    public void Options(Scanner scanner){
        view.printSwitch();
        int option = scanner.nextInt();
        switch (option){
            case 1:
                withdrawMoney(scanner);
            case 2:
                addMoney(scanner);
            case 3:
                cleverBankTransfer(scanner);
        }
    }

    public void withdrawMoney(Scanner scanner){
        BigDecimal value = getOperationValue(scanner);
        dao.withdraw(currentUser, value);
    }

    public void addMoney(Scanner scanner){
        BigDecimal value = getOperationValue(scanner);
        dao.add(currentUser, value);
    }

    public void cleverBankTransfer(Scanner scanner){
        BigDecimal value = getOperationValue(scanner);
        String transferUsername = getCleverBankTransfer(scanner);
        String thisBank = "Clever-bank";
        dao.Transfer(currentUser, value, thisBank , transferUsername);
    }

    public void otherBankTransfer(Scanner scanner){
        BigDecimal value = getOperationValue(scanner);
        String bankName = getOtherBankName(scanner);
        String transferUsername = getOtherBankTransfer(scanner);
        dao.Transfer(currentUser, value, bankName, transferUsername);
    }

    public BigDecimal getOperationValue(Scanner scanner){
        view.printMoneyValue();
        return scanner.nextBigDecimal();
    }

    public String getCleverBankTransfer(Scanner scanner){
        view.printCleverBankTransfer();
        return scanner.nextLine();
    }

    public String getOtherBankName(Scanner scanner){
        view.printOtherBankTransfer();
        return scanner.nextLine();
    }

    public String getOtherBankTransfer(Scanner scanner){
        return scanner.nextLine();
    }
}