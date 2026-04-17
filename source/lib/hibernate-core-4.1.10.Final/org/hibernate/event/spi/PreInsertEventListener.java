package org.hibernate.event.spi;

import java.io.Serializable;

public interface PreInsertEventListener extends Serializable {
   boolean onPreInsert(PreInsertEvent var1);
}
