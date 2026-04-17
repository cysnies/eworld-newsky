package org.hibernate.event.spi;

import java.io.Serializable;

public interface PreCollectionUpdateEventListener extends Serializable {
   void onPreUpdateCollection(PreCollectionUpdateEvent var1);
}
