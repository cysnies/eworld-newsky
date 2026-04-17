package org.hibernate.type;

import java.util.Map;
import org.hibernate.MappingException;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.persister.entity.Joinable;

public interface AssociationType extends Type {
   ForeignKeyDirection getForeignKeyDirection();

   boolean useLHSPrimaryKey();

   String getLHSPropertyName();

   String getRHSUniqueKeyPropertyName();

   Joinable getAssociatedJoinable(SessionFactoryImplementor var1) throws MappingException;

   String getAssociatedEntityName(SessionFactoryImplementor var1) throws MappingException;

   String getOnCondition(String var1, SessionFactoryImplementor var2, Map var3) throws MappingException;

   boolean isAlwaysDirtyChecked();

   /** @deprecated */
   @Deprecated
   boolean isEmbeddedInXML();
}
