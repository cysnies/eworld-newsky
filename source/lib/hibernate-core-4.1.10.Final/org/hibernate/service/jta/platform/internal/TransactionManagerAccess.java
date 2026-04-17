package org.hibernate.service.jta.platform.internal;

import java.io.Serializable;
import javax.transaction.TransactionManager;

public interface TransactionManagerAccess extends Serializable {
   TransactionManager getTransactionManager();
}
