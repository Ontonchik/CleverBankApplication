import org.yaml.snakeyaml.Yaml;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;

/**
 * Class from mvc pattern that works with application interface and connect user with model with the help of controller
 */
public class View {

    View(){}

    /**
     * Function that creates starting window
     */
    public void printHello(){
        System.out.println("Добро пожаловать в приложение Clever-Bank.");
        System.out.println("Требуется авторизация: введите имя пользователя и пароль");
    }

    /**
     * Function that show application options to user
     */
    public void printSwitch(){
        System.out.println("Выберите желаемую операцию:");
        System.out.println("1 - Снятие денег со счета");
        System.out.println("2 - Пополнение счета");
        System.out.println("3 - Перевод средств клиенту Clever-Bank");
        System.out.println("4 - Перевод средств клиенту другого банка");
    }

    /**
     * Function that asks user to assign value of transaction
     */
    public void printMoneyValue(){
        System.out.println("Введите сумму операции");
    }

    /**
     * Prints incorrect password length exception
     */
    public void printPasswordException(){
        System.out.println("Длина пароля не должна быть больше 40-ка символов");
    }

    /**
     * Ask user to assign account on which he wants to send money
     */
    public void printCleverBankTransfer(){
        System.out.println("Введите номер счета Clever-bank, на который хотите перевести деньги");
    }

    /**
     * Ask user to assign account on which he wants to send money
     */
    public void printOtherBankTransfer(){
        System.out.println("Введите название банка и номер счета этого банка" +
                ", на который хотите перевести деньги(Сначала название банка, затем номер счета)");
    }

    /**
     * Print incorrect money value exception
     */
    public void printMoneyException(){
        System.out.println("Сумма перевода должна быть положительным числом");
    }

    /**
     * Print invalid option exception
     */
    public void printNoSuchOption(){
        System.out.println("Такой опции нет, попробуйте еще раз");
    }

    /**
     * Function that prints check after successful operation in file check/output + number of check
     * @param transaction is object that have information about current transaction
     * @param map is information from config.yml that needed to now number of current check
     * @param yaml is Yaml object with the help of which we work with .yml file
     */
    public void printCheck(Transaction transaction, HashMap<String, Object> map, Yaml yaml){
        try {
            int checkNumber = Integer.parseInt(map.get("checkNumber").toString());
            PrintWriter wr = new PrintWriter(new FileWriter("check/output" + checkNumber + ".txt"));
            PrintWriter writer = new PrintWriter("src/main/resources/config.yml");
            map.replace("checkNumber", checkNumber + 1);
            yaml.dump(map, writer);
            wr.println("------------------------------------");
            wr.println("|          Банковский чек          |");
            wr.println("| Чек:                           " + checkNumber + " |");
            wr.println("| " + LocalDate.now() + "              " + LocalTime.now().truncatedTo(ChronoUnit.SECONDS) + " |");
            wr.println("| Тип транзакции:          " + transaction.getMTransactionType() + " |");
            switch (transaction.getMTransactionType()){
                case "Перевод":
                    wr.println("| Банк отправителя     " + transaction.getMCurrentUserAccount().getBank().getMName() + " |");
                    wr.println("| Банк получателя       " + transaction.getMTransferUserAccount().getBank().getMName() + " |");
                    wr.println("| Счет отправителя             " + transaction.getMCurrentUserAccount().getMAccountId() + " |");
                    wr.println("| Счет получателя              " + transaction.getMTransferUserAccount().getMAccountId() + " |");
                case "Пополнение счета":
                    wr.println("| Банк пользователя    " + transaction.getMCurrentUserAccount().getBank().getMName() + " |");
                    wr.println("| Счет пользователя            " + transaction.getMCurrentUserAccount().getMAccountId() + " |");
                case "Снятие со счета":
                    wr.println("| Банк пользователя    " + transaction.getMCurrentUserAccount().getBank().getMName() + " |");
                    wr.println("| Счет пользователя            " + transaction.getMCurrentUserAccount().getMAccountId() + " |");
            }
            wr.println("| Сумма:                       " + transaction.getMValue() + " |");
            wr.println("------------------------------------");
            wr.close();
        } catch (IOException e){
            System.out.println("Ошибка записи в файл");
            System.out.println(e.getMessage());
        }
    }

    /**
     * Prints a request to the user whether he wants to continue or no
     */
    public void printContinue(){
        System.out.println("Операция проведена успешно, желаете продолжить?");
        System.out.println("1 - да, 2 - нет");
    }

    /**
     * Function that give info about lack of money to conduct a transaction
     */
    public void printLackOfMoney(){
        System.out.println("Недостаточно средств на счете для проведения операции");
    }

    /**
     * Show to user that db isn't working well
     * @param e is sql exception thrown in dao
     */
    public void printSqlException(SQLException e){
        System.out.println("Ошибка при обращении в базу данных. Подробнее об ошибке:");
        System.out.println(e.getMessage());
    }
}