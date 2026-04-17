package org.hibernate.cfg;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import org.hibernate.annotations.common.reflection.XClass;
import org.hibernate.annotations.common.reflection.XProperty;
import org.hibernate.mapping.Join;
import org.hibernate.mapping.KeyValue;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.Table;

public interface PropertyHolder {
   String getClassName();

   String getEntityOwnerClassName();

   Table getTable();

   void addProperty(Property var1, XClass var2);

   void addProperty(Property var1, Ejb3Column[] var2, XClass var3);

   KeyValue getIdentifier();

   boolean isOrWithinEmbeddedId();

   PersistentClass getPersistentClass();

   boolean isComponent();

   boolean isEntity();

   void setParentProperty(String var1);

   String getPath();

   Column[] getOverriddenColumn(String var1);

   JoinColumn[] getOverriddenJoinColumn(String var1);

   JoinTable getJoinTable(XProperty var1);

   String getEntityName();

   Join addJoin(JoinTable var1, boolean var2);

   boolean isInIdClass();

   void setInIdClass(Boolean var1);
}
