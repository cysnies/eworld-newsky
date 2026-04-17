package org.hibernate.event.spi;

import java.io.Serializable;
import java.util.Map;
import org.hibernate.HibernateException;

public interface MergeEventListener extends Serializable {
   void onMerge(MergeEvent var1) throws HibernateException;

   void onMerge(MergeEvent var1, Map var2) throws HibernateException;
}
