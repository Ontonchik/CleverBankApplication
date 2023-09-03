import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
public class Account {
    private int accountId;
    private User user;
    private char[] password;
    Bank bank;

    public Account(int account_Id){
        accountId = account_Id;
    }

    public Account(int account_Id, Bank accountBank){
        accountId = account_Id;
        bank = accountBank;
    }
}
