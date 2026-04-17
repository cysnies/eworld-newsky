package org.hibernate.persister.entity;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.type.Type;

public interface Loadable extends EntityPersister {
   String ROWID_ALIAS = "rowid_";

   boolean hasSubclasses();

   Type getDiscriminatorType();

   Object getDiscriminatorValue();

   String getSubclassForDiscriminatorValue(Object var1);

   String[] getIdentifierColumnNames();

   String[] getIdentifierAliases(String var1);

   String[] getPropertyAliases(String var1, int var2);

   String[] getPropertyColumnNames(int var1);

   String getDiscriminatorAlias(String var1);

   String getDiscriminatorColumnName();

   boolean hasRowId();

   Object[] hydrate(ResultSet var1, Serializable var2, Object var3, Loadable var4, String[][] var5, boolean var6, SessionImplementor var7) throws SQLException, HibernateException;

   boolean isAbstract();

   void registerAffectingFetchProfile(String var1);

   String getTableAliasForColumn(String var1, String var2);
}
