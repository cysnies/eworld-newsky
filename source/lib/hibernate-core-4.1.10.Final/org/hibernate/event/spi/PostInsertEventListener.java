package org.hibernate.event.spi;

import java.io.Serializable;

public interface PostInsertEventListener extends Serializable {
   void onPostInsert(PostInsertEvent var1);
}
