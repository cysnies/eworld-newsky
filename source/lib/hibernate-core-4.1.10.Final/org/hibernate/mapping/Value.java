package org.hibernate.mapping;

import java.io.Serializable;
import java.util.Iterator;
import org.hibernate.FetchMode;
import org.hibernate.MappingException;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.type.Type;

public interface Value extends Serializable {
   int getColumnSpan();

   Iterator getColumnIterator();

   Type getType() throws MappingException;

   FetchMode getFetchMode();

   Table getTable();

   boolean hasFormula();

   boolean isAlternateUniqueKey();

   boolean isNullable();

   boolean[] getColumnUpdateability();

   boolean[] getColumnInsertability();

   void createForeignKey() throws MappingException;

   boolean isSimpleValue();

   boolean isValid(Mapping var1) throws MappingException;

   void setTypeUsingReflection(String var1, String var2) throws MappingException;

   Object accept(ValueVisitor var1);
}
