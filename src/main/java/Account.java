import lombok.Getter;
import lombok.Setter;

/**
 * Implementation of user account in bank
 */
@Getter
@Setter
public class Account {
    private int mAccountId;
    private User mUser;
    private char[] mPassword;
    Bank bank;

    public Account(int account_Id){
        mAccountId = account_Id;
    }

    public Account(int account_Id, Bank accountBank){
        mAccountId = account_Id;
        bank = accountBank;
    }

    public Account() {
        bank = new Bank("Clever-bank");
    }
}
