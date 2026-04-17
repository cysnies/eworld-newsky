package org.hibernate.event.internal;

import org.hibernate.engine.spi.CascadingAction;

public class DefaultPersistOnFlushEventListener extends DefaultPersistEventListener {
   protected CascadingAction getCascadeAction() {
      return CascadingAction.PERSIST_ON_FLUSH;
   }
}
