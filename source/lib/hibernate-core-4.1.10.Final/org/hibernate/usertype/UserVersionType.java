package org.hibernate.usertype;

import java.util.Comparator;
import org.hibernate.engine.spi.SessionImplementor;

public interface UserVersionType extends UserType, Comparator {
   Object seed(SessionImplementor var1);

   Object next(Object var1, SessionImplementor var2);
}
