import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class View {
    public int checkNumber;

    View(){
        checkNumber = 0;
    }

    public void printHello(){
        System.out.println("Добро пожаловать в приложение Clever-Bank.");
        System.out.println("Требуется авторизация: введите имя пользователя и пароль");
    }

    public void printSwitch(){
        System.out.println("Выберите желаемую операцию:");
        System.out.println("1 - Снятие денег со счета");
        System.out.println("2 - Пополнение счета");
        System.out.println("3 - Перевод средств клиенту Clever-Bank");
        System.out.println("4 - Перевод средств клиенту другого банка");
    }

    public void printMoneyValue(){
        System.out.println("Введите сумму операции");
    }

    public void printPasswordException(){
        System.out.println("Длина пароля не должна быть больше 40-ка символов");
    }

    public void printCleverBankTransfer(){
        System.out.println("Введите номер счета Clever-bank, на который хотите перевести деньги");
    }

    public void printOtherBankTransfer(){
        System.out.println("Введите название банка и номер счета этого банка" +
                ", на который хотите перевести деньги(Сначала название банка, затем номер счета)");
    }

    public void printMoneyException(){
        System.out.println("Сумма перевода должна быть положительным числом");
    }

    public void printNoSuchOption(){
        System.out.println("Такой опции нет, введите число от 1 до 4");
    }

    public void printCheck(Transaction transaction){
        try {
            checkNumber += 1;
            PrintWriter wr = new PrintWriter(new FileWriter("output.txt"));
            wr.println("------------------------------------");
            wr.println("|          Банковский чек          |");
            wr.println("| Чек:                           " + checkNumber + " |");
            wr.println("| " + LocalDate.now() + "              " + LocalTime.now().truncatedTo(ChronoUnit.SECONDS) + " |");
            wr.println("| Тип транзакции:          " + transaction.getTransactionType() + " |");
            switch (transaction.getTransactionType()){
                case "Перевод":
                    wr.println("| Банк отправителя     " + transaction.getCurrentUserAccount().getBank().getName() + " |");
                    wr.println("| Банк получателя       " + transaction.getTransferUserAccount().getBank().getName() + " |");
                    wr.println("| Счет отправителя             " + transaction.getCurrentUserAccount().getAccountId() + " |");
                    wr.println("| Счет получателя              " + transaction.getTransferUserAccount().getAccountId() + " |");
                case "Пополнение счета":
                    wr.println("| Банк пользователя    " + transaction.getCurrentUserAccount().getBank().getName() + " |");
                    wr.println("| Счет пользователя            " + transaction.getCurrentUserAccount().getAccountId() + " |");
                case "Снятие со счета":
                    wr.println("| Банк пользователя    " + transaction.getCurrentUserAccount().getBank().getName() + " |");
                    wr.println("| Счет пользователя            " + transaction.getCurrentUserAccount().getAccountId() + " |");
            }
            wr.println("| Сумма:                       " + transaction.getValue() + " |");
            wr.println("------------------------------------");
            wr.close();
        } catch (IOException e){
            System.out.println("Ошибка записи в файл");
            System.out.println(e.getMessage());
        }
    }
}