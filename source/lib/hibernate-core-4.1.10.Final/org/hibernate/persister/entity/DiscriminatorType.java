package org.hibernate.persister.entity;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import org.dom4j.Node;
import org.hibernate.EntityMode;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.internal.util.collections.ArrayHelper;
import org.hibernate.internal.util.compare.EqualsHelper;
import org.hibernate.metamodel.relational.Size;
import org.hibernate.type.AbstractType;
import org.hibernate.type.Type;

public class DiscriminatorType extends AbstractType {
   private final Type underlyingType;
   private final Loadable persister;

   public DiscriminatorType(Type underlyingType, Loadable persister) {
      this.underlyingType = underlyingType;
      this.persister = persister;
   }

   public Class getReturnedClass() {
      return Class.class;
   }

   public String getName() {
      return this.getClass().getName();
   }

   public boolean isMutable() {
      return false;
   }

   public Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor session, Object owner) throws HibernateException, SQLException {
      return this.nullSafeGet(rs, names[0], session, owner);
   }

   public Object nullSafeGet(ResultSet rs, String name, SessionImplementor session, Object owner) throws HibernateException, SQLException {
      Object discriminatorValue = this.underlyingType.nullSafeGet(rs, name, session, owner);
      String entityName = this.persister.getSubclassForDiscriminatorValue(discriminatorValue);
      if (entityName == null) {
         throw new HibernateException("Unable to resolve discriminator value [" + discriminatorValue + "] to entity name");
      } else {
         EntityPersister entityPersister = session.getEntityPersister(entityName, (Object)null);
         return EntityMode.POJO == entityPersister.getEntityMode() ? entityPersister.getMappedClass() : entityName;
      }
   }

   public void nullSafeSet(PreparedStatement st, Object value, int index, boolean[] settable, SessionImplementor session) throws HibernateException, SQLException {
      this.nullSafeSet(st, value, index, session);
   }

   public void nullSafeSet(PreparedStatement st, Object value, int index, SessionImplementor session) throws HibernateException, SQLException {
      String entityName = session.getFactory().getClassMetadata((Class)value).getEntityName();
      Loadable entityPersister = (Loadable)session.getFactory().getEntityPersister(entityName);
      this.underlyingType.nullSafeSet(st, entityPersister.getDiscriminatorValue(), index, session);
   }

   public String toLoggableString(Object value, SessionFactoryImplementor factory) throws HibernateException {
      return value == null ? "[null]" : value.toString();
   }

   public Object deepCopy(Object value, SessionFactoryImplementor factory) throws HibernateException {
      return value;
   }

   public Object replace(Object original, Object target, SessionImplementor session, Object owner, Map copyCache) throws HibernateException {
      return original;
   }

   public boolean[] toColumnNullness(Object value, Mapping mapping) {
      return value == null ? ArrayHelper.FALSE : ArrayHelper.TRUE;
   }

   public boolean isDirty(Object old, Object current, boolean[] checkable, SessionImplementor session) throws HibernateException {
      return EqualsHelper.equals(old, current);
   }

   public int[] sqlTypes(Mapping mapping) throws MappingException {
      return this.underlyingType.sqlTypes(mapping);
   }

   public Size[] dictatedSizes(Mapping mapping) throws MappingException {
      return this.underlyingType.dictatedSizes(mapping);
   }

   public Size[] defaultSizes(Mapping mapping) throws MappingException {
      return this.underlyingType.defaultSizes(mapping);
   }

   public int getColumnSpan(Mapping mapping) throws MappingException {
      return this.underlyingType.getColumnSpan(mapping);
   }

   public void setToXMLNode(Node node, Object value, SessionFactoryImplementor factory) throws HibernateException {
   }

   public Object fromXMLNode(Node xml, Mapping factory) throws HibernateException {
      return null;
   }
}
