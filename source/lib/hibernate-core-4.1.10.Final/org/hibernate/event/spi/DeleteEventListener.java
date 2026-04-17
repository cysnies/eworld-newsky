package org.hibernate.event.spi;

import java.io.Serializable;
import java.util.Set;
import org.hibernate.HibernateException;

public interface DeleteEventListener extends Serializable {
   void onDelete(DeleteEvent var1) throws HibernateException;

   void onDelete(DeleteEvent var1, Set var2) throws HibernateException;
}
