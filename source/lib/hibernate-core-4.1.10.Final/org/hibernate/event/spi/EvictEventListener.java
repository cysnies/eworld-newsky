package org.hibernate.event.spi;

import java.io.Serializable;
import org.hibernate.HibernateException;

public interface EvictEventListener extends Serializable {
   void onEvict(EvictEvent var1) throws HibernateException;
}
