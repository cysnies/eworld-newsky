package org.hibernate.event.spi;

import java.io.Serializable;
import java.util.Map;
import org.hibernate.HibernateException;

public interface PersistEventListener extends Serializable {
   void onPersist(PersistEvent var1) throws HibernateException;

   void onPersist(PersistEvent var1, Map var2) throws HibernateException;
}
