package org.hibernate.mapping;

import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.function.SQLFunctionRegistry;

public interface Selectable {
   String getAlias(Dialect var1);

   String getAlias(Dialect var1, Table var2);

   boolean isFormula();

   String getTemplate(Dialect var1, SQLFunctionRegistry var2);

   String getText(Dialect var1);

   String getText();
}
