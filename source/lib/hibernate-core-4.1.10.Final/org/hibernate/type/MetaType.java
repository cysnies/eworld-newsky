package org.hibernate.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.dom4j.Node;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.metamodel.relational.Size;

public class MetaType extends AbstractType {
   public static final String[] REGISTRATION_KEYS = new String[0];
   private final Map values;
   private final Map keys;
   private final Type baseType;

   public MetaType(Map values, Type baseType) {
      this.baseType = baseType;
      this.values = values;
      this.keys = new HashMap();

      for(Map.Entry me : values.entrySet()) {
         this.keys.put(me.getValue(), me.getKey());
      }

   }

   public String[] getRegistrationKeys() {
      return REGISTRATION_KEYS;
   }

   public int[] sqlTypes(Mapping mapping) throws MappingException {
      return this.baseType.sqlTypes(mapping);
   }

   public Size[] dictatedSizes(Mapping mapping) throws MappingException {
      return this.baseType.dictatedSizes(mapping);
   }

   public Size[] defaultSizes(Mapping mapping) throws MappingException {
      return this.baseType.defaultSizes(mapping);
   }

   public int getColumnSpan(Mapping mapping) throws MappingException {
      return this.baseType.getColumnSpan(mapping);
   }

   public Class getReturnedClass() {
      return String.class;
   }

   public Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor session, Object owner) throws HibernateException, SQLException {
      Object key = this.baseType.nullSafeGet(rs, names, session, owner);
      return key == null ? null : this.values.get(key);
   }

   public Object nullSafeGet(ResultSet rs, String name, SessionImplementor session, Object owner) throws HibernateException, SQLException {
      Object key = this.baseType.nullSafeGet(rs, name, session, owner);
      return key == null ? null : this.values.get(key);
   }

   public void nullSafeSet(PreparedStatement st, Object value, int index, SessionImplementor session) throws HibernateException, SQLException {
      this.baseType.nullSafeSet(st, value == null ? null : this.keys.get(value), index, session);
   }

   public void nullSafeSet(PreparedStatement st, Object value, int index, boolean[] settable, SessionImplementor session) throws HibernateException, SQLException {
      if (settable[0]) {
         this.nullSafeSet(st, value, index, session);
      }

   }

   public String toLoggableString(Object value, SessionFactoryImplementor factory) throws HibernateException {
      return this.toXMLString(value, factory);
   }

   public String toXMLString(Object value, SessionFactoryImplementor factory) throws HibernateException {
      return (String)value;
   }

   public Object fromXMLString(String xml, Mapping factory) throws HibernateException {
      return xml;
   }

   public String getName() {
      return this.baseType.getName();
   }

   public Object deepCopy(Object value, SessionFactoryImplementor factory) throws HibernateException {
      return value;
   }

   public Object replace(Object original, Object target, SessionImplementor session, Object owner, Map copyCache) {
      return original;
   }

   public boolean isMutable() {
      return false;
   }

   public Object fromXMLNode(Node xml, Mapping factory) throws HibernateException {
      return this.fromXMLString(xml.getText(), factory);
   }

   public void setToXMLNode(Node node, Object value, SessionFactoryImplementor factory) throws HibernateException {
      node.setText(this.toXMLString(value, factory));
   }

   public boolean[] toColumnNullness(Object value, Mapping mapping) {
      throw new UnsupportedOperationException();
   }

   public boolean isDirty(Object old, Object current, boolean[] checkable, SessionImplementor session) throws HibernateException {
      return checkable[0] && this.isDirty(old, current, session);
   }
}
