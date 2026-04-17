package org.hibernate.engine.spi;

public interface SessionOwner {
   boolean shouldAutoCloseSession();
}
