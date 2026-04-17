package org.hibernate.event.spi;

import java.io.Serializable;
import org.hibernate.HibernateException;

public interface SaveOrUpdateEventListener extends Serializable {
   void onSaveOrUpdate(SaveOrUpdateEvent var1) throws HibernateException;
}
