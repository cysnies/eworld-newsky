package org.hibernate.event.spi;

import java.io.Serializable;

public interface PreLoadEventListener extends Serializable {
   void onPreLoad(PreLoadEvent var1);
}
