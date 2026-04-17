package org.hibernate.event.spi;

import java.io.Serializable;

public interface PreDeleteEventListener extends Serializable {
   boolean onPreDelete(PreDeleteEvent var1);
}
