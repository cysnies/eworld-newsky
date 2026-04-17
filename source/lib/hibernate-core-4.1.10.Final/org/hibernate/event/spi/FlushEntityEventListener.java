package org.hibernate.event.spi;

import java.io.Serializable;
import org.hibernate.HibernateException;

public interface FlushEntityEventListener extends Serializable {
   void onFlushEntity(FlushEntityEvent var1) throws HibernateException;
}
