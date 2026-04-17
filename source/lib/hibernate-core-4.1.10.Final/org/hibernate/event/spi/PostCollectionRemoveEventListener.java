package org.hibernate.event.spi;

import java.io.Serializable;

public interface PostCollectionRemoveEventListener extends Serializable {
   void onPostRemoveCollection(PostCollectionRemoveEvent var1);
}
