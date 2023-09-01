public class View {

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
        System.out.println("Введите имя пользователя Clever-bank, которому хотите перевести деньги");
    }

    public void printOtherBankTransfer(){
        System.out.println("Введите название банка и имя пользователя этого банка" +
                ", которому хотите перевести деньги(Сначала название банка, затем имя пользователя)");
    }
}