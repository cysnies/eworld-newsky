package org.hibernate.event.spi;

import java.io.Serializable;

public interface PostLoadEventListener extends Serializable {
   void onPostLoad(PostLoadEvent var1);
}
