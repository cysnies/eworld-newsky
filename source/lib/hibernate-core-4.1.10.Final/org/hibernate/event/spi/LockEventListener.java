package org.hibernate.event.spi;

import java.io.Serializable;
import org.hibernate.HibernateException;

public interface LockEventListener extends Serializable {
   void onLock(LockEvent var1) throws HibernateException;
}
