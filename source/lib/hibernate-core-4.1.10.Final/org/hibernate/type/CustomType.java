package org.hibernate.type;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import org.dom4j.Node;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.internal.util.collections.ArrayHelper;
import org.hibernate.metamodel.relational.Size;
import org.hibernate.usertype.EnhancedUserType;
import org.hibernate.usertype.LoggableUserType;
import org.hibernate.usertype.Sized;
import org.hibernate.usertype.UserType;
import org.hibernate.usertype.UserVersionType;

public class CustomType extends AbstractType implements IdentifierType, DiscriminatorType, VersionType, BasicType, StringRepresentableType {
   private final UserType userType;
   private final String name;
   private final int[] types;
   private final Size[] dictatedSizes;
   private final Size[] defaultSizes;
   private final boolean customLogging;
   private final String[] registrationKeys;

   public CustomType(UserType userType) throws MappingException {
      this(userType, ArrayHelper.EMPTY_STRING_ARRAY);
   }

   public CustomType(UserType userType, String[] registrationKeys) throws MappingException {
      this.userType = userType;
      this.name = userType.getClass().getName();
      this.types = userType.sqlTypes();
      this.dictatedSizes = Sized.class.isInstance(userType) ? ((Sized)userType).dictatedSizes() : new Size[this.types.length];
      this.defaultSizes = Sized.class.isInstance(userType) ? ((Sized)userType).defaultSizes() : new Size[this.types.length];
      this.customLogging = LoggableUserType.class.isInstance(userType);
      this.registrationKeys = registrationKeys;
   }

   public UserType getUserType() {
      return this.userType;
   }

   public String[] getRegistrationKeys() {
      return this.registrationKeys;
   }

   public int[] sqlTypes(Mapping pi) {
      return this.types;
   }

   public Size[] dictatedSizes(Mapping mapping) throws MappingException {
      return this.dictatedSizes;
   }

   public Size[] defaultSizes(Mapping mapping) throws MappingException {
      return this.defaultSizes;
   }

   public int getColumnSpan(Mapping session) {
      return this.types.length;
   }

   public Class getReturnedClass() {
      return this.userType.returnedClass();
   }

   public boolean isEqual(Object x, Object y) throws HibernateException {
      return this.userType.equals(x, y);
   }

   public int getHashCode(Object x) {
      return this.userType.hashCode(x);
   }

   public Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor session, Object owner) throws HibernateException, SQLException {
      return this.userType.nullSafeGet(rs, names, session, owner);
   }

   public Object nullSafeGet(ResultSet rs, String columnName, SessionImplementor session, Object owner) throws HibernateException, SQLException {
      return this.nullSafeGet(rs, new String[]{columnName}, session, owner);
   }

   public Object assemble(Serializable cached, SessionImplementor session, Object owner) throws HibernateException {
      return this.userType.assemble(cached, owner);
   }

   public Serializable disassemble(Object value, SessionImplementor session, Object owner) throws HibernateException {
      return this.userType.disassemble(value);
   }

   public Object replace(Object original, Object target, SessionImplementor session, Object owner, Map copyCache) throws HibernateException {
      return this.userType.replace(original, target, owner);
   }

   public void nullSafeSet(PreparedStatement st, Object value, int index, boolean[] settable, SessionImplementor session) throws HibernateException, SQLException {
      if (settable[0]) {
         this.userType.nullSafeSet(st, value, index, session);
      }

   }

   public void nullSafeSet(PreparedStatement st, Object value, int index, SessionImplementor session) throws HibernateException, SQLException {
      this.userType.nullSafeSet(st, value, index, session);
   }

   public String toXMLString(Object value, SessionFactoryImplementor factory) {
      return this.toString(value);
   }

   public Object fromXMLString(String xml, Mapping factory) {
      return this.fromStringValue(xml);
   }

   public String getName() {
      return this.name;
   }

   public Object deepCopy(Object value, SessionFactoryImplementor factory) throws HibernateException {
      return this.userType.deepCopy(value);
   }

   public boolean isMutable() {
      return this.userType.isMutable();
   }

   public Object stringToObject(String xml) {
      return this.fromStringValue(xml);
   }

   public String objectToSQLString(Object value, Dialect dialect) throws Exception {
      return ((EnhancedUserType)this.userType).objectToSQLString(value);
   }

   public Comparator getComparator() {
      return (Comparator)this.userType;
   }

   public Object next(Object current, SessionImplementor session) {
      return ((UserVersionType)this.userType).next(current, session);
   }

   public Object seed(SessionImplementor session) {
      return ((UserVersionType)this.userType).seed(session);
   }

   public Object fromXMLNode(Node xml, Mapping factory) throws HibernateException {
      return this.fromXMLString(xml.getText(), factory);
   }

   public void setToXMLNode(Node node, Object value, SessionFactoryImplementor factory) throws HibernateException {
      node.setText(this.toXMLString(value, factory));
   }

   public String toLoggableString(Object value, SessionFactoryImplementor factory) throws HibernateException {
      if (value == null) {
         return "null";
      } else {
         return this.customLogging ? ((LoggableUserType)this.userType).toLoggableString(value, factory) : this.toXMLString(value, factory);
      }
   }

   public boolean[] toColumnNullness(Object value, Mapping mapping) {
      boolean[] result = new boolean[this.getColumnSpan(mapping)];
      if (value != null) {
         Arrays.fill(result, true);
      }

      return result;
   }

   public boolean isDirty(Object old, Object current, boolean[] checkable, SessionImplementor session) throws HibernateException {
      return checkable[0] && this.isDirty(old, current, session);
   }

   public String toString(Object value) throws HibernateException {
      if (StringRepresentableType.class.isInstance(this.userType)) {
         return ((StringRepresentableType)this.userType).toString(value);
      } else if (value == null) {
         return null;
      } else {
         return EnhancedUserType.class.isInstance(this.userType) ? ((EnhancedUserType)this.userType).toXMLString(value) : value.toString();
      }
   }

   public Object fromStringValue(String string) throws HibernateException {
      if (StringRepresentableType.class.isInstance(this.userType)) {
         return ((StringRepresentableType)this.userType).fromStringValue(string);
      } else if (EnhancedUserType.class.isInstance(this.userType)) {
         return ((EnhancedUserType)this.userType).fromXMLString(string);
      } else {
         throw new HibernateException(String.format("Could not process #fromStringValue, UserType class [%s] did not implement %s or %s", this.name, StringRepresentableType.class.getName(), EnhancedUserType.class.getName()));
      }
   }
}
