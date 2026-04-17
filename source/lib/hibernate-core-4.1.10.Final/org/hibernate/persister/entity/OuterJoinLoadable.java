package org.hibernate.persister.entity;

import org.hibernate.FetchMode;
import org.hibernate.engine.spi.CascadeStyle;
import org.hibernate.type.EntityType;
import org.hibernate.type.Type;

public interface OuterJoinLoadable extends Loadable, Joinable {
   String selectFragment(String var1, String var2);

   int countSubclassProperties();

   FetchMode getFetchMode(int var1);

   CascadeStyle getCascadeStyle(int var1);

   boolean isDefinedOnSubclass(int var1);

   Type getSubclassPropertyType(int var1);

   String getSubclassPropertyName(int var1);

   boolean isSubclassPropertyNullable(int var1);

   String[] getSubclassPropertyColumnNames(int var1);

   String getSubclassPropertyTableName(int var1);

   String[] toColumns(String var1, int var2);

   String fromTableFragment(String var1);

   String[] getPropertyColumnNames(String var1);

   String getPropertyTableName(String var1);

   EntityType getEntityType();
}
