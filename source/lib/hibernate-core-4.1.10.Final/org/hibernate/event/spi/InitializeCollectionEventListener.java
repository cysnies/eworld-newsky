package org.hibernate.event.spi;

import java.io.Serializable;
import org.hibernate.HibernateException;

public interface InitializeCollectionEventListener extends Serializable {
   void onInitializeCollection(InitializeCollectionEvent var1) throws HibernateException;
}
