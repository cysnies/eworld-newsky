package org.hibernate.service.jta.platform.internal;

import java.io.Serializable;
import javax.transaction.Synchronization;

public interface JtaSynchronizationStrategy extends Serializable {
   void registerSynchronization(Synchronization var1);

   boolean canRegisterSynchronization();
}
