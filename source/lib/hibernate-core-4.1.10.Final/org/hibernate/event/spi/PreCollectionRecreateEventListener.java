package org.hibernate.event.spi;

import java.io.Serializable;

public interface PreCollectionRecreateEventListener extends Serializable {
   void onPreRecreateCollection(PreCollectionRecreateEvent var1);
}
