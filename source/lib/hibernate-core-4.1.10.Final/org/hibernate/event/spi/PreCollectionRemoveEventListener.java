package org.hibernate.event.spi;

import java.io.Serializable;

public interface PreCollectionRemoveEventListener extends Serializable {
   void onPreRemoveCollection(PreCollectionRemoveEvent var1);
}
