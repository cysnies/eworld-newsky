package org.hibernate.id;

import org.hibernate.persister.entity.EntityPersister;

public interface PostInsertIdentityPersister extends EntityPersister {
   String getSelectByUniqueKeyString(String var1);

   String getIdentitySelectString();

   String[] getIdentifierColumnNames();

   String[] getRootTableKeyColumnNames();
}
