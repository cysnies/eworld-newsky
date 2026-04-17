package org.hibernate.persister.collection;

import org.hibernate.MappingException;
import org.hibernate.QueryException;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.persister.entity.PropertyMapping;
import org.hibernate.type.Type;

public class ElementPropertyMapping implements PropertyMapping {
   private final String[] elementColumns;
   private final Type type;

   public ElementPropertyMapping(String[] elementColumns, Type type) throws MappingException {
      this.elementColumns = elementColumns;
      this.type = type;
   }

   public Type toType(String propertyName) throws QueryException {
      if (propertyName != null && !"id".equals(propertyName)) {
         throw new QueryException("cannot dereference scalar collection element: " + propertyName);
      } else {
         return this.type;
      }
   }

   public String[] toColumns(String alias, String propertyName) throws QueryException {
      if (propertyName != null && !"id".equals(propertyName)) {
         throw new QueryException("cannot dereference scalar collection element: " + propertyName);
      } else {
         return StringHelper.qualify(alias, this.elementColumns);
      }
   }

   public String[] toColumns(String propertyName) throws QueryException, UnsupportedOperationException {
      throw new UnsupportedOperationException("References to collections must be define a SQL alias");
   }

   public Type getType() {
      return this.type;
   }
}
