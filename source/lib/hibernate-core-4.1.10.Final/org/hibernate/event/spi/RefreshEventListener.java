package org.hibernate.event.spi;

import java.io.Serializable;
import java.util.Map;
import org.hibernate.HibernateException;

public interface RefreshEventListener extends Serializable {
   void onRefresh(RefreshEvent var1) throws HibernateException;

   void onRefresh(RefreshEvent var1, Map var2) throws HibernateException;
}
