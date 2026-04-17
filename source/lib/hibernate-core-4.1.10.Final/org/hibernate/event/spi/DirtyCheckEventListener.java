package org.hibernate.event.spi;

import java.io.Serializable;
import org.hibernate.HibernateException;

public interface DirtyCheckEventListener extends Serializable {
   void onDirtyCheck(DirtyCheckEvent var1) throws HibernateException;
}
