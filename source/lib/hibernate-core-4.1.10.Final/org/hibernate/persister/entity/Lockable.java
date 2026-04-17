package org.hibernate.persister.entity;

public interface Lockable extends EntityPersister {
   String getRootTableName();

   String getRootTableAlias(String var1);

   String[] getRootTableIdentifierColumnNames();

   String getVersionColumnName();
}
