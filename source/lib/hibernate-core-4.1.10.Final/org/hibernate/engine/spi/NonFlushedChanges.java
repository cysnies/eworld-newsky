package org.hibernate.engine.spi;

import java.io.Serializable;

public interface NonFlushedChanges extends Serializable {
   void clear();
}
