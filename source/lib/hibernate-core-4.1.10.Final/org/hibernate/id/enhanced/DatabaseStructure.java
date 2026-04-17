package org.hibernate.id.enhanced;

import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.SessionImplementor;

public interface DatabaseStructure {
   String getName();

   int getTimesAccessed();

   int getInitialValue();

   int getIncrementSize();

   AccessCallback buildCallback(SessionImplementor var1);

   void prepare(Optimizer var1);

   String[] sqlCreateStrings(Dialect var1);

   String[] sqlDropStrings(Dialect var1);

   boolean isPhysicalSequence();
}
