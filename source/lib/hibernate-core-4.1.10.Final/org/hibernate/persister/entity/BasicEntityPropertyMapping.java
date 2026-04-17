package org.hibernate.persister.entity;

import org.hibernate.QueryException;
import org.hibernate.type.Type;

public class BasicEntityPropertyMapping extends AbstractPropertyMapping {
   private final AbstractEntityPersister persister;

   public BasicEntityPropertyMapping(AbstractEntityPersister persister) {
      this.persister = persister;
   }

   public String[] getIdentifierColumnNames() {
      return this.persister.getIdentifierColumnNames();
   }

   public String[] getIdentifierColumnReaders() {
      return this.persister.getIdentifierColumnReaders();
   }

   public String[] getIdentifierColumnReaderTemplates() {
      return this.persister.getIdentifierColumnReaderTemplates();
   }

   protected String getEntityName() {
      return this.persister.getEntityName();
   }

   public Type getType() {
      return this.persister.getType();
   }

   public String[] toColumns(String alias, String propertyName) throws QueryException {
      AbstractEntityPersister var10001 = this.persister;
      return super.toColumns(AbstractEntityPersister.generateTableAlias(alias, this.persister.getSubclassPropertyTableNumber(propertyName)), propertyName);
   }
}
