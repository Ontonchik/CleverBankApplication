import lombok.Data;

import java.math.BigDecimal;

/**
 * Implementation of transaction object
 */
@Data
public class Transaction {
    private final Account mCurrentUserAccount;
    private final Account mTransferUserAccount;
    private final BigDecimal mValue;
    private final String mTransactionType;
}
