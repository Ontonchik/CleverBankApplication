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
}