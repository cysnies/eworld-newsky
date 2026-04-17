package org.hibernate.event.spi;

import java.io.Serializable;

public interface PreUpdateEventListener extends Serializable {
   boolean onPreUpdate(PreUpdateEvent var1);
}
