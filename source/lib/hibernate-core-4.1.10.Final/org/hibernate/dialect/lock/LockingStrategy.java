package org.hibernate.dialect.lock;

import java.io.Serializable;
import org.hibernate.StaleObjectStateException;
import org.hibernate.engine.spi.SessionImplementor;

public interface LockingStrategy {
   void lock(Serializable var1, Object var2, Object var3, int var4, SessionImplementor var5) throws StaleObjectStateException, LockingStrategyException;
}
