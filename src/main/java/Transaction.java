import lombok.Data;

import java.math.BigDecimal;

@Data
public class Transaction {
    private final Account currentUserAccount;
    private final Account transferUserAccount;
    private final BigDecimal value;
}
