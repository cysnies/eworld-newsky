package org.hibernate.event.spi;

import java.io.Serializable;

public interface PostCollectionRecreateEventListener extends Serializable {
   void onPostRecreateCollection(PostCollectionRecreateEvent var1);
}
