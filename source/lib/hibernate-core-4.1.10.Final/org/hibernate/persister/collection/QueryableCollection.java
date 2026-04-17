package org.hibernate.persister.collection;

import org.hibernate.FetchMode;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.persister.entity.Joinable;
import org.hibernate.persister.entity.PropertyMapping;

public interface QueryableCollection extends PropertyMapping, Joinable, CollectionPersister {
   String selectFragment(String var1, String var2);

   String[] getIndexColumnNames();

   String[] getIndexFormulas();

   String[] getIndexColumnNames(String var1);

   String[] getElementColumnNames(String var1);

   String[] getElementColumnNames();

   String getSQLOrderByString(String var1);

   String getManyToManyOrderByString(String var1);

   boolean hasWhere();

   EntityPersister getElementPersister();

   FetchMode getFetchMode();
}
