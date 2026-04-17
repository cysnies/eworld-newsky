package org.hibernate.event.spi;

import java.io.Serializable;
import org.hibernate.HibernateException;

public interface AutoFlushEventListener extends Serializable {
   void onAutoFlush(AutoFlushEvent var1) throws HibernateException;
}
