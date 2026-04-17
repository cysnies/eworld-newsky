package org.hibernate.engine.spi;

import org.hibernate.SessionBuilder;

public interface SessionBuilderImplementor extends SessionBuilder {
   SessionBuilder owner(SessionOwner var1);
}
