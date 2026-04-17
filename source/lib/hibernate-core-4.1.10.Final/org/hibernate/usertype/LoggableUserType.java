package org.hibernate.usertype;

import org.hibernate.engine.spi.SessionFactoryImplementor;

public interface LoggableUserType {
   String toLoggableString(Object var1, SessionFactoryImplementor var2);
}
