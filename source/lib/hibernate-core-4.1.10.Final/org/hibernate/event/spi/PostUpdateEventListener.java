package org.hibernate.event.spi;

import java.io.Serializable;

public interface PostUpdateEventListener extends Serializable {
   void onPostUpdate(PostUpdateEvent var1);
}
