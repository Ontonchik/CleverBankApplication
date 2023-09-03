import lombok.Data;

import java.math.BigDecimal;

/**
 * Implementation of transaction object
 */
@Data
public class Transaction {
    private final Account currentUserAccount;
    private final Account transferUserAccount;
    private final BigDecimal value;
    private final String transactionType;
}
