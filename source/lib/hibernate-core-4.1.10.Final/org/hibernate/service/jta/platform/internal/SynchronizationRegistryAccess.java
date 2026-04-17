package org.hibernate.service.jta.platform.internal;

import java.io.Serializable;
import javax.transaction.TransactionSynchronizationRegistry;

public interface SynchronizationRegistryAccess extends Serializable {
   TransactionSynchronizationRegistry getSynchronizationRegistry();
}
