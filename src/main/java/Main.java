import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public class Main {
    public static void main(String[] args) {
        Controller controller = new Controller();
        controller.start();

//        View view = new View();
//        Transaction testTransaction = new Transaction(new Account(123, new Bank("Clever-bank")),
//                new Account(516, new Bank("Bebra-bank")),new BigDecimal(518),"Перевод");
//        view.printCheck(testTransaction);
//        System.out.println(LocalTime.now().getHour());
    }
}
