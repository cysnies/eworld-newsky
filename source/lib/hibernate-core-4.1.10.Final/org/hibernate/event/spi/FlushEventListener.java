package org.hibernate.event.spi;

import java.io.Serializable;
import org.hibernate.HibernateException;

public interface FlushEventListener extends Serializable {
   void onFlush(FlushEvent var1) throws HibernateException;
}
