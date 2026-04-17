package org.hibernate;

import java.util.Collection;
import org.hibernate.engine.spi.FilterDefinition;

public interface Filter {
   String getName();

   FilterDefinition getFilterDefinition();

   Filter setParameter(String var1, Object var2);

   Filter setParameterList(String var1, Collection var2);

   Filter setParameterList(String var1, Object[] var2);

   void validate() throws HibernateException;
}
