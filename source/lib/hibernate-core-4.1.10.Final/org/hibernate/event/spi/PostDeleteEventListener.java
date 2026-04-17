package org.hibernate.event.spi;

import java.io.Serializable;

public interface PostDeleteEventListener extends Serializable {
   void onPostDelete(PostDeleteEvent var1);
}
