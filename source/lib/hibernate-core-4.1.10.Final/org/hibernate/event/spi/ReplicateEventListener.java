package org.hibernate.event.spi;

import java.io.Serializable;
import org.hibernate.HibernateException;

public interface ReplicateEventListener extends Serializable {
   void onReplicate(ReplicateEvent var1) throws HibernateException;
}
