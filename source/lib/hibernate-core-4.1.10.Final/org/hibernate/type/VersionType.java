package org.hibernate.type;

import java.util.Comparator;
import org.hibernate.engine.spi.SessionImplementor;

public interface VersionType extends Type {
   Object seed(SessionImplementor var1);

   Object next(Object var1, SessionImplementor var2);

   Comparator getComparator();
}
