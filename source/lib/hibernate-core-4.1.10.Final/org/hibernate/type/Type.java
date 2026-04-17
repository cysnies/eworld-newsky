package org.hibernate.type;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import org.dom4j.Node;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.metamodel.relational.Size;

public interface Type extends Serializable {
   boolean isAssociationType();

   boolean isCollectionType();

   boolean isEntityType();

   boolean isAnyType();

   boolean isComponentType();

   int getColumnSpan(Mapping var1) throws MappingException;

   int[] sqlTypes(Mapping var1) throws MappingException;

   Size[] dictatedSizes(Mapping var1) throws MappingException;

   Size[] defaultSizes(Mapping var1) throws MappingException;

   Class getReturnedClass();

   /** @deprecated */
   @Deprecated
   boolean isXMLElement();

   boolean isSame(Object var1, Object var2) throws HibernateException;

   boolean isEqual(Object var1, Object var2) throws HibernateException;

   boolean isEqual(Object var1, Object var2, SessionFactoryImplementor var3) throws HibernateException;

   int getHashCode(Object var1) throws HibernateException;

   int getHashCode(Object var1, SessionFactoryImplementor var2) throws HibernateException;

   int compare(Object var1, Object var2);

   boolean isDirty(Object var1, Object var2, SessionImplementor var3) throws HibernateException;

   boolean isDirty(Object var1, Object var2, boolean[] var3, SessionImplementor var4) throws HibernateException;

   boolean isModified(Object var1, Object var2, boolean[] var3, SessionImplementor var4) throws HibernateException;

   Object nullSafeGet(ResultSet var1, String[] var2, SessionImplementor var3, Object var4) throws HibernateException, SQLException;

   Object nullSafeGet(ResultSet var1, String var2, SessionImplementor var3, Object var4) throws HibernateException, SQLException;

   void nullSafeSet(PreparedStatement var1, Object var2, int var3, boolean[] var4, SessionImplementor var5) throws HibernateException, SQLException;

   void nullSafeSet(PreparedStatement var1, Object var2, int var3, SessionImplementor var4) throws HibernateException, SQLException;

   String toLoggableString(Object var1, SessionFactoryImplementor var2) throws HibernateException;

   /** @deprecated */
   @Deprecated
   void setToXMLNode(Node var1, Object var2, SessionFactoryImplementor var3) throws HibernateException;

   /** @deprecated */
   @Deprecated
   Object fromXMLNode(Node var1, Mapping var2) throws HibernateException;

   String getName();

   Object deepCopy(Object var1, SessionFactoryImplementor var2) throws HibernateException;

   boolean isMutable();

   Serializable disassemble(Object var1, SessionImplementor var2, Object var3) throws HibernateException;

   Object assemble(Serializable var1, SessionImplementor var2, Object var3) throws HibernateException;

   void beforeAssemble(Serializable var1, SessionImplementor var2);

   Object hydrate(ResultSet var1, String[] var2, SessionImplementor var3, Object var4) throws HibernateException, SQLException;

   Object resolve(Object var1, SessionImplementor var2, Object var3) throws HibernateException;

   Object semiResolve(Object var1, SessionImplementor var2, Object var3) throws HibernateException;

   Type getSemiResolvedType(SessionFactoryImplementor var1);

   Object replace(Object var1, Object var2, SessionImplementor var3, Object var4, Map var5) throws HibernateException;

   Object replace(Object var1, Object var2, SessionImplementor var3, Object var4, Map var5, ForeignKeyDirection var6) throws HibernateException;

   boolean[] toColumnNullness(Object var1, Mapping var2);
}
