package org.hibernate.event.spi;

import java.io.Serializable;

public interface PostCollectionUpdateEventListener extends Serializable {
   void onPostUpdateCollection(PostCollectionUpdateEvent var1);
}
