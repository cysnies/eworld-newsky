package org.hibernate.type;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;
import org.dom4j.Node;
import org.hibernate.EntityMode;
import org.hibernate.FetchMode;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.TransientObjectException;
import org.hibernate.engine.internal.ForeignKeys;
import org.hibernate.engine.spi.CascadeStyle;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.internal.util.collections.ArrayHelper;
import org.hibernate.metamodel.relational.Size;
import org.hibernate.persister.entity.Joinable;
import org.hibernate.proxy.HibernateProxyHelper;

public class AnyType extends AbstractType implements CompositeType, AssociationType {
   private final Type identifierType;
   private final Type metaType;
   private static final String[] PROPERTY_NAMES = new String[]{"class", "id"};

   public AnyType(Type metaType, Type identifierType) {
      this.identifierType = identifierType;
      this.metaType = metaType;
   }

   public Object deepCopy(Object value, SessionFactoryImplementor factory) throws HibernateException {
      return value;
   }

   public boolean isMethodOf(Method method) {
      return false;
   }

   public boolean isSame(Object x, Object y) throws HibernateException {
      return x == y;
   }

   public int compare(Object x, Object y) {
      return 0;
   }

   public int getColumnSpan(Mapping session) throws MappingException {
      return 2;
   }

   public String getName() {
      return "object";
   }

   public boolean isMutable() {
      return false;
   }

   public Object nullSafeGet(ResultSet rs, String name, SessionImplementor session, Object owner) throws HibernateException, SQLException {
      throw new UnsupportedOperationException("object is a multicolumn type");
   }

   public Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor session, Object owner) throws HibernateException, SQLException {
      return this.resolveAny((String)this.metaType.nullSafeGet(rs, names[0], session, owner), (Serializable)this.identifierType.nullSafeGet(rs, names[1], session, owner), session);
   }

   public Object hydrate(ResultSet rs, String[] names, SessionImplementor session, Object owner) throws HibernateException, SQLException {
      String entityName = (String)this.metaType.nullSafeGet(rs, names[0], session, owner);
      Serializable id = (Serializable)this.identifierType.nullSafeGet(rs, names[1], session, owner);
      return new ObjectTypeCacheEntry(entityName, id);
   }

   public Object resolve(Object value, SessionImplementor session, Object owner) throws HibernateException {
      ObjectTypeCacheEntry holder = (ObjectTypeCacheEntry)value;
      return this.resolveAny(holder.entityName, holder.id, session);
   }

   public Object semiResolve(Object value, SessionImplementor session, Object owner) throws HibernateException {
      throw new UnsupportedOperationException("any mappings may not form part of a property-ref");
   }

   private Object resolveAny(String entityName, Serializable id, SessionImplementor session) throws HibernateException {
      return entityName != null && id != null ? session.internalLoad(entityName, id, false, false) : null;
   }

   public void nullSafeSet(PreparedStatement st, Object value, int index, SessionImplementor session) throws HibernateException, SQLException {
      this.nullSafeSet(st, value, index, (boolean[])null, session);
   }

   public void nullSafeSet(PreparedStatement st, Object value, int index, boolean[] settable, SessionImplementor session) throws HibernateException, SQLException {
      Serializable id;
      String entityName;
      if (value == null) {
         id = null;
         entityName = null;
      } else {
         entityName = session.bestGuessEntityName(value);
         id = ForeignKeys.getEntityIdentifierIfNotUnsaved(entityName, value, session);
      }

      if (settable == null || settable[0]) {
         this.metaType.nullSafeSet(st, entityName, index, session);
      }

      if (settable == null) {
         this.identifierType.nullSafeSet(st, id, index + 1, session);
      } else {
         boolean[] idsettable = new boolean[settable.length - 1];
         System.arraycopy(settable, 1, idsettable, 0, idsettable.length);
         this.identifierType.nullSafeSet(st, id, index + 1, idsettable, session);
      }

   }

   public Class getReturnedClass() {
      return Object.class;
   }

   public int[] sqlTypes(Mapping mapping) throws MappingException {
      return ArrayHelper.join(this.metaType.sqlTypes(mapping), this.identifierType.sqlTypes(mapping));
   }

   public Size[] dictatedSizes(Mapping mapping) throws MappingException {
      return (Size[])ArrayHelper.join((Object[])this.metaType.dictatedSizes(mapping), (Object[])this.identifierType.dictatedSizes(mapping));
   }

   public Size[] defaultSizes(Mapping mapping) throws MappingException {
      return (Size[])ArrayHelper.join((Object[])this.metaType.defaultSizes(mapping), (Object[])this.identifierType.defaultSizes(mapping));
   }

   public void setToXMLNode(Node xml, Object value, SessionFactoryImplementor factory) {
      throw new UnsupportedOperationException("any types cannot be stringified");
   }

   public String toLoggableString(Object value, SessionFactoryImplementor factory) throws HibernateException {
      return value == null ? "null" : factory.getTypeHelper().entity(HibernateProxyHelper.getClassWithoutInitializingProxy(value)).toLoggableString(value, factory);
   }

   public Object fromXMLNode(Node xml, Mapping factory) throws HibernateException {
      throw new UnsupportedOperationException();
   }

   public Object assemble(Serializable cached, SessionImplementor session, Object owner) throws HibernateException {
      ObjectTypeCacheEntry e = (ObjectTypeCacheEntry)cached;
      return e == null ? null : session.internalLoad(e.entityName, e.id, false, false);
   }

   public Serializable disassemble(Object value, SessionImplementor session, Object owner) throws HibernateException {
      return value == null ? null : new ObjectTypeCacheEntry(session.bestGuessEntityName(value), ForeignKeys.getEntityIdentifierIfNotUnsaved(session.bestGuessEntityName(value), value, session));
   }

   public boolean isAnyType() {
      return true;
   }

   public Object replace(Object original, Object target, SessionImplementor session, Object owner, Map copyCache) throws HibernateException {
      if (original == null) {
         return null;
      } else {
         String entityName = session.bestGuessEntityName(original);
         Serializable id = ForeignKeys.getEntityIdentifierIfNotUnsaved(entityName, original, session);
         return session.internalLoad(entityName, id, false, false);
      }
   }

   public CascadeStyle getCascadeStyle(int i) {
      return CascadeStyle.NONE;
   }

   public FetchMode getFetchMode(int i) {
      return FetchMode.SELECT;
   }

   public String[] getPropertyNames() {
      return PROPERTY_NAMES;
   }

   public Object getPropertyValue(Object component, int i, SessionImplementor session) throws HibernateException {
      return i == 0 ? session.bestGuessEntityName(component) : this.getIdentifier(component, session);
   }

   public Object[] getPropertyValues(Object component, SessionImplementor session) throws HibernateException {
      return new Object[]{session.bestGuessEntityName(component), this.getIdentifier(component, session)};
   }

   private Serializable getIdentifier(Object value, SessionImplementor session) throws HibernateException {
      try {
         return ForeignKeys.getEntityIdentifierIfNotUnsaved(session.bestGuessEntityName(value), value, session);
      } catch (TransientObjectException var4) {
         return null;
      }
   }

   public Type[] getSubtypes() {
      return new Type[]{this.metaType, this.identifierType};
   }

   public void setPropertyValues(Object component, Object[] values, EntityMode entityMode) throws HibernateException {
      throw new UnsupportedOperationException();
   }

   public Object[] getPropertyValues(Object component, EntityMode entityMode) {
      throw new UnsupportedOperationException();
   }

   public boolean isComponentType() {
      return true;
   }

   public ForeignKeyDirection getForeignKeyDirection() {
      return ForeignKeyDirection.FOREIGN_KEY_FROM_PARENT;
   }

   public boolean isAssociationType() {
      return true;
   }

   public boolean useLHSPrimaryKey() {
      return false;
   }

   public Joinable getAssociatedJoinable(SessionFactoryImplementor factory) {
      throw new UnsupportedOperationException("any types do not have a unique referenced persister");
   }

   public boolean isModified(Object old, Object current, boolean[] checkable, SessionImplementor session) throws HibernateException {
      if (current == null) {
         return old != null;
      } else if (old == null) {
         return current != null;
      } else {
         ObjectTypeCacheEntry holder = (ObjectTypeCacheEntry)old;
         boolean[] idcheckable = new boolean[checkable.length - 1];
         System.arraycopy(checkable, 1, idcheckable, 0, idcheckable.length);
         return checkable[0] && !holder.entityName.equals(session.bestGuessEntityName(current)) || this.identifierType.isModified(holder.id, this.getIdentifier(current, session), idcheckable, session);
      }
   }

   public String getAssociatedEntityName(SessionFactoryImplementor factory) throws MappingException {
      throw new UnsupportedOperationException("any types do not have a unique referenced persister");
   }

   public boolean[] getPropertyNullability() {
      return null;
   }

   public String getOnCondition(String alias, SessionFactoryImplementor factory, Map enabledFilters) throws MappingException {
      throw new UnsupportedOperationException();
   }

   public boolean isReferenceToPrimaryKey() {
      return true;
   }

   public String getRHSUniqueKeyPropertyName() {
      return null;
   }

   public String getLHSPropertyName() {
      return null;
   }

   public boolean isAlwaysDirtyChecked() {
      return false;
   }

   public boolean isEmbeddedInXML() {
      return false;
   }

   public boolean[] toColumnNullness(Object value, Mapping mapping) {
      boolean[] result = new boolean[this.getColumnSpan(mapping)];
      if (value != null) {
         Arrays.fill(result, true);
      }

      return result;
   }

   public boolean isDirty(Object old, Object current, boolean[] checkable, SessionImplementor session) throws HibernateException {
      return this.isDirty(old, current, session);
   }

   public boolean isEmbedded() {
      return false;
   }

   public static final class ObjectTypeCacheEntry implements Serializable {
      String entityName;
      Serializable id;

      ObjectTypeCacheEntry(String entityName, Serializable id) {
         this.entityName = entityName;
         this.id = id;
      }
   }
}
