package org.hibernate.engine.transaction.synchronization.spi;

import java.io.Serializable;
import javax.transaction.SystemException;

public interface ExceptionMapper extends Serializable {
   RuntimeException mapStatusCheckFailure(String var1, SystemException var2);

   RuntimeException mapManagedFlushFailure(String var1, RuntimeException var2);
}
