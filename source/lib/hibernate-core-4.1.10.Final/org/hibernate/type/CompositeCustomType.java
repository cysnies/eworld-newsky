package org.hibernate.type;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import org.dom4j.Element;
import org.dom4j.Node;
import org.hibernate.EntityMode;
import org.hibernate.FetchMode;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.engine.spi.CascadeStyle;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.internal.util.collections.ArrayHelper;
import org.hibernate.metamodel.relational.Size;
import org.hibernate.usertype.CompositeUserType;
import org.hibernate.usertype.LoggableUserType;

public class CompositeCustomType extends AbstractType implements CompositeType, BasicType {
   private final CompositeUserType userType;
   private final String[] registrationKeys;
   private final String name;
   private final boolean customLogging;

   public CompositeCustomType(CompositeUserType userType) {
      this(userType, ArrayHelper.EMPTY_STRING_ARRAY);
   }

   public CompositeCustomType(CompositeUserType userType, String[] registrationKeys) {
      this.userType = userType;
      this.name = userType.getClass().getName();
      this.customLogging = LoggableUserType.class.isInstance(userType);
      this.registrationKeys = registrationKeys;
   }

   public String[] getRegistrationKeys() {
      return this.registrationKeys;
   }

   public CompositeUserType getUserType() {
      return this.userType;
   }

   public boolean isMethodOf(Method method) {
      return false;
   }

   public Type[] getSubtypes() {
      return this.userType.getPropertyTypes();
   }

   public String[] getPropertyNames() {
      return this.userType.getPropertyNames();
   }

   public Object[] getPropertyValues(Object component, SessionImplementor session) throws HibernateException {
      return this.getPropertyValues(component, EntityMode.POJO);
   }

   public Object[] getPropertyValues(Object component, EntityMode entityMode) throws HibernateException {
      int len = this.getSubtypes().length;
      Object[] result = new Object[len];

      for(int i = 0; i < len; ++i) {
         result[i] = this.getPropertyValue(component, i);
      }

      return result;
   }

   public void setPropertyValues(Object component, Object[] values, EntityMode entityMode) throws HibernateException {
      for(int i = 0; i < values.length; ++i) {
         this.userType.setPropertyValue(component, i, values[i]);
      }

   }

   public Object getPropertyValue(Object component, int i, SessionImplementor session) throws HibernateException {
      return this.getPropertyValue(component, i);
   }

   public Object getPropertyValue(Object component, int i) throws HibernateException {
      return this.userType.getPropertyValue(component, i);
   }

   public CascadeStyle getCascadeStyle(int i) {
      return CascadeStyle.NONE;
   }

   public FetchMode getFetchMode(int i) {
      return FetchMode.DEFAULT;
   }

   public boolean isComponentType() {
      return true;
   }

   public Object deepCopy(Object value, SessionFactoryImplementor factory) throws HibernateException {
      return this.userType.deepCopy(value);
   }

   public Object assemble(Serializable cached, SessionImplementor session, Object owner) throws HibernateException {
      return this.userType.assemble(cached, session, owner);
   }

   public Serializable disassemble(Object value, SessionImplementor session, Object owner) throws HibernateException {
      return this.userType.disassemble(value, session);
   }

   public Object replace(Object original, Object target, SessionImplementor session, Object owner, Map copyCache) throws HibernateException {
      return this.userType.replace(original, target, session, owner);
   }

   public boolean isEqual(Object x, Object y) throws HibernateException {
      return this.userType.equals(x, y);
   }

   public int getHashCode(Object x) {
      return this.userType.hashCode(x);
   }

   public int getColumnSpan(Mapping mapping) throws MappingException {
      Type[] types = this.userType.getPropertyTypes();
      int n = 0;

      for(Type type : types) {
         n += type.getColumnSpan(mapping);
      }

      return n;
   }

   public String getName() {
      return this.name;
   }

   public Class getReturnedClass() {
      return this.userType.returnedClass();
   }

   public boolean isMutable() {
      return this.userType.isMutable();
   }

   public Object nullSafeGet(ResultSet rs, String columnName, SessionImplementor session, Object owner) throws HibernateException, SQLException {
      return this.userType.nullSafeGet(rs, new String[]{columnName}, session, owner);
   }

   public Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor session, Object owner) throws HibernateException, SQLException {
      return this.userType.nullSafeGet(rs, names, session, owner);
   }

   public void nullSafeSet(PreparedStatement st, Object value, int index, SessionImplementor session) throws HibernateException, SQLException {
      this.userType.nullSafeSet(st, value, index, session);
   }

   public void nullSafeSet(PreparedStatement st, Object value, int index, boolean[] settable, SessionImplementor session) throws HibernateException, SQLException {
      this.userType.nullSafeSet(st, value, index, session);
   }

   public int[] sqlTypes(Mapping mapping) throws MappingException {
      int[] result = new int[this.getColumnSpan(mapping)];
      int n = 0;

      for(Type type : this.userType.getPropertyTypes()) {
         for(int sqlType : type.sqlTypes(mapping)) {
            result[n++] = sqlType;
         }
      }

      return result;
   }

   public Size[] dictatedSizes(Mapping mapping) throws MappingException {
      Size[] sizes = new Size[this.getColumnSpan(mapping)];
      int soFar = 0;

      for(Type propertyType : this.userType.getPropertyTypes()) {
         Size[] propertySizes = propertyType.dictatedSizes(mapping);
         System.arraycopy(propertySizes, 0, sizes, soFar, propertySizes.length);
         soFar += propertySizes.length;
      }

      return sizes;
   }

   public Size[] defaultSizes(Mapping mapping) throws MappingException {
      Size[] sizes = new Size[this.getColumnSpan(mapping)];
      int soFar = 0;

      for(Type propertyType : this.userType.getPropertyTypes()) {
         Size[] propertySizes = propertyType.defaultSizes(mapping);
         System.arraycopy(propertySizes, 0, sizes, soFar, propertySizes.length);
         soFar += propertySizes.length;
      }

      return sizes;
   }

   public String toLoggableString(Object value, SessionFactoryImplementor factory) throws HibernateException {
      if (value == null) {
         return "null";
      } else {
         return this.customLogging ? ((LoggableUserType)this.userType).toLoggableString(value, factory) : value.toString();
      }
   }

   public boolean[] getPropertyNullability() {
      return null;
   }

   public Object fromXMLNode(Node xml, Mapping factory) throws HibernateException {
      return xml;
   }

   public void setToXMLNode(Node node, Object value, SessionFactoryImplementor factory) throws HibernateException {
      replaceNode(node, (Element)value);
   }

   public boolean[] toColumnNullness(Object value, Mapping mapping) {
      boolean[] result = new boolean[this.getColumnSpan(mapping)];
      if (value == null) {
         return result;
      } else {
         Object[] values = this.getPropertyValues(value, EntityMode.POJO);
         int loc = 0;
         Type[] propertyTypes = this.getSubtypes();

         for(int i = 0; i < propertyTypes.length; ++i) {
            boolean[] propertyNullness = propertyTypes[i].toColumnNullness(values[i], mapping);
            System.arraycopy(propertyNullness, 0, result, loc, propertyNullness.length);
            loc += propertyNullness.length;
         }

         return result;
      }
   }

   public boolean isDirty(Object old, Object current, boolean[] checkable, SessionImplementor session) throws HibernateException {
      return this.isDirty(old, current, session);
   }

   public boolean isEmbedded() {
      return false;
   }
}
