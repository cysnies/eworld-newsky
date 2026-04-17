package org.hibernate.type;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.dom4j.Element;
import org.dom4j.Node;
import org.hibernate.EntityMode;
import org.hibernate.FetchMode;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.PropertyNotFoundException;
import org.hibernate.engine.spi.CascadeStyle;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.internal.util.collections.ArrayHelper;
import org.hibernate.metamodel.relational.Size;
import org.hibernate.tuple.StandardProperty;
import org.hibernate.tuple.component.ComponentMetamodel;
import org.hibernate.tuple.component.ComponentTuplizer;

public class ComponentType extends AbstractType implements CompositeType {
   private final TypeFactory.TypeScope typeScope;
   private final String[] propertyNames;
   private final Type[] propertyTypes;
   private final boolean[] propertyNullability;
   protected final int propertySpan;
   private final CascadeStyle[] cascade;
   private final FetchMode[] joinedFetch;
   private final boolean isKey;
   protected final EntityMode entityMode;
   protected final ComponentTuplizer componentTuplizer;

   public ComponentType(TypeFactory.TypeScope typeScope, ComponentMetamodel metamodel) {
      this.typeScope = typeScope;
      this.isKey = metamodel.isKey();
      this.propertySpan = metamodel.getPropertySpan();
      this.propertyNames = new String[this.propertySpan];
      this.propertyTypes = new Type[this.propertySpan];
      this.propertyNullability = new boolean[this.propertySpan];
      this.cascade = new CascadeStyle[this.propertySpan];
      this.joinedFetch = new FetchMode[this.propertySpan];

      for(int i = 0; i < this.propertySpan; ++i) {
         StandardProperty prop = metamodel.getProperty(i);
         this.propertyNames[i] = prop.getName();
         this.propertyTypes[i] = prop.getType();
         this.propertyNullability[i] = prop.isNullable();
         this.cascade[i] = prop.getCascadeStyle();
         this.joinedFetch[i] = prop.getFetchMode();
      }

      this.entityMode = metamodel.getEntityMode();
      this.componentTuplizer = metamodel.getComponentTuplizer();
   }

   public boolean isKey() {
      return this.isKey;
   }

   public EntityMode getEntityMode() {
      return this.entityMode;
   }

   public ComponentTuplizer getComponentTuplizer() {
      return this.componentTuplizer;
   }

   public int getColumnSpan(Mapping mapping) throws MappingException {
      int span = 0;

      for(int i = 0; i < this.propertySpan; ++i) {
         span += this.propertyTypes[i].getColumnSpan(mapping);
      }

      return span;
   }

   public int[] sqlTypes(Mapping mapping) throws MappingException {
      int[] sqlTypes = new int[this.getColumnSpan(mapping)];
      int n = 0;

      for(int i = 0; i < this.propertySpan; ++i) {
         int[] subtypes = this.propertyTypes[i].sqlTypes(mapping);

         for(int j = 0; j < subtypes.length; ++j) {
            sqlTypes[n++] = subtypes[j];
         }
      }

      return sqlTypes;
   }

   public Size[] dictatedSizes(Mapping mapping) throws MappingException {
      Size[] sizes = new Size[this.getColumnSpan(mapping)];
      int soFar = 0;

      for(Type propertyType : this.propertyTypes) {
         Size[] propertySizes = propertyType.dictatedSizes(mapping);
         System.arraycopy(propertySizes, 0, sizes, soFar, propertySizes.length);
         soFar += propertySizes.length;
      }

      return sizes;
   }

   public Size[] defaultSizes(Mapping mapping) throws MappingException {
      Size[] sizes = new Size[this.getColumnSpan(mapping)];
      int soFar = 0;

      for(Type propertyType : this.propertyTypes) {
         Size[] propertySizes = propertyType.defaultSizes(mapping);
         System.arraycopy(propertySizes, 0, sizes, soFar, propertySizes.length);
         soFar += propertySizes.length;
      }

      return sizes;
   }

   public final boolean isComponentType() {
      return true;
   }

   public Class getReturnedClass() {
      return this.componentTuplizer.getMappedClass();
   }

   public boolean isSame(Object x, Object y) throws HibernateException {
      if (x == y) {
         return true;
      } else if (x != null && y != null) {
         Object[] xvalues = this.getPropertyValues(x, this.entityMode);
         Object[] yvalues = this.getPropertyValues(y, this.entityMode);

         for(int i = 0; i < this.propertySpan; ++i) {
            if (!this.propertyTypes[i].isSame(xvalues[i], yvalues[i])) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   public boolean isEqual(Object x, Object y) throws HibernateException {
      if (x == y) {
         return true;
      } else if (x != null && y != null) {
         Object[] xvalues = this.getPropertyValues(x, this.entityMode);
         Object[] yvalues = this.getPropertyValues(y, this.entityMode);

         for(int i = 0; i < this.propertySpan; ++i) {
            if (!this.propertyTypes[i].isEqual(xvalues[i], yvalues[i])) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   public boolean isEqual(Object x, Object y, SessionFactoryImplementor factory) throws HibernateException {
      if (x == y) {
         return true;
      } else if (x != null && y != null) {
         Object[] xvalues = this.getPropertyValues(x, this.entityMode);
         Object[] yvalues = this.getPropertyValues(y, this.entityMode);

         for(int i = 0; i < this.propertySpan; ++i) {
            if (!this.propertyTypes[i].isEqual(xvalues[i], yvalues[i], factory)) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   public int compare(Object x, Object y) {
      if (x == y) {
         return 0;
      } else {
         Object[] xvalues = this.getPropertyValues(x, this.entityMode);
         Object[] yvalues = this.getPropertyValues(y, this.entityMode);

         for(int i = 0; i < this.propertySpan; ++i) {
            int propertyCompare = this.propertyTypes[i].compare(xvalues[i], yvalues[i]);
            if (propertyCompare != 0) {
               return propertyCompare;
            }
         }

         return 0;
      }
   }

   public boolean isMethodOf(Method method) {
      return false;
   }

   public int getHashCode(Object x) {
      int result = 17;
      Object[] values = this.getPropertyValues(x, this.entityMode);

      for(int i = 0; i < this.propertySpan; ++i) {
         Object y = values[i];
         result *= 37;
         if (y != null) {
            result += this.propertyTypes[i].getHashCode(y);
         }
      }

      return result;
   }

   public int getHashCode(Object x, SessionFactoryImplementor factory) {
      int result = 17;
      Object[] values = this.getPropertyValues(x, this.entityMode);

      for(int i = 0; i < this.propertySpan; ++i) {
         Object y = values[i];
         result *= 37;
         if (y != null) {
            result += this.propertyTypes[i].getHashCode(y, factory);
         }
      }

      return result;
   }

   public boolean isDirty(Object x, Object y, SessionImplementor session) throws HibernateException {
      if (x == y) {
         return false;
      } else if (x != null && y != null) {
         Object[] xvalues = this.getPropertyValues(x, this.entityMode);
         Object[] yvalues = this.getPropertyValues(y, this.entityMode);

         for(int i = 0; i < xvalues.length; ++i) {
            if (this.propertyTypes[i].isDirty(xvalues[i], yvalues[i], session)) {
               return true;
            }
         }

         return false;
      } else {
         return true;
      }
   }

   public boolean isDirty(Object x, Object y, boolean[] checkable, SessionImplementor session) throws HibernateException {
      if (x == y) {
         return false;
      } else if (x != null && y != null) {
         Object[] xvalues = this.getPropertyValues(x, this.entityMode);
         Object[] yvalues = this.getPropertyValues(y, this.entityMode);
         int loc = 0;

         for(int i = 0; i < xvalues.length; ++i) {
            int len = this.propertyTypes[i].getColumnSpan(session.getFactory());
            if (len <= 1) {
               boolean dirty = (len == 0 || checkable[loc]) && this.propertyTypes[i].isDirty(xvalues[i], yvalues[i], session);
               if (dirty) {
                  return true;
               }
            } else {
               boolean[] subcheckable = new boolean[len];
               System.arraycopy(checkable, loc, subcheckable, 0, len);
               boolean dirty = this.propertyTypes[i].isDirty(xvalues[i], yvalues[i], subcheckable, session);
               if (dirty) {
                  return true;
               }
            }

            loc += len;
         }

         return false;
      } else {
         return true;
      }
   }

   public boolean isModified(Object old, Object current, boolean[] checkable, SessionImplementor session) throws HibernateException {
      if (current == null) {
         return old != null;
      } else if (old == null) {
         return current != null;
      } else {
         Object[] currentValues = this.getPropertyValues(current, session);
         Object[] oldValues = old;
         int loc = 0;

         for(int i = 0; i < currentValues.length; ++i) {
            int len = this.propertyTypes[i].getColumnSpan(session.getFactory());
            boolean[] subcheckable = new boolean[len];
            System.arraycopy(checkable, loc, subcheckable, 0, len);
            if (this.propertyTypes[i].isModified(oldValues[i], currentValues[i], subcheckable, session)) {
               return true;
            }

            loc += len;
         }

         return false;
      }
   }

   public Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor session, Object owner) throws HibernateException, SQLException {
      return this.resolve(this.hydrate(rs, names, session, owner), session, owner);
   }

   public void nullSafeSet(PreparedStatement st, Object value, int begin, SessionImplementor session) throws HibernateException, SQLException {
      Object[] subvalues = this.nullSafeGetValues(value, this.entityMode);

      for(int i = 0; i < this.propertySpan; ++i) {
         this.propertyTypes[i].nullSafeSet(st, subvalues[i], begin, session);
         begin += this.propertyTypes[i].getColumnSpan(session.getFactory());
      }

   }

   public void nullSafeSet(PreparedStatement st, Object value, int begin, boolean[] settable, SessionImplementor session) throws HibernateException, SQLException {
      Object[] subvalues = this.nullSafeGetValues(value, this.entityMode);
      int loc = 0;

      for(int i = 0; i < this.propertySpan; ++i) {
         int len = this.propertyTypes[i].getColumnSpan(session.getFactory());
         if (len != 0) {
            if (len == 1) {
               if (settable[loc]) {
                  this.propertyTypes[i].nullSafeSet(st, subvalues[i], begin, session);
                  ++begin;
               }
            } else {
               boolean[] subsettable = new boolean[len];
               System.arraycopy(settable, loc, subsettable, 0, len);
               this.propertyTypes[i].nullSafeSet(st, subvalues[i], begin, subsettable, session);
               begin += ArrayHelper.countTrue(subsettable);
            }
         }

         loc += len;
      }

   }

   private Object[] nullSafeGetValues(Object value, EntityMode entityMode) throws HibernateException {
      return value == null ? new Object[this.propertySpan] : this.getPropertyValues(value, entityMode);
   }

   public Object nullSafeGet(ResultSet rs, String name, SessionImplementor session, Object owner) throws HibernateException, SQLException {
      return this.nullSafeGet(rs, new String[]{name}, session, owner);
   }

   public Object getPropertyValue(Object component, int i, SessionImplementor session) throws HibernateException {
      return this.getPropertyValue(component, i, this.entityMode);
   }

   public Object getPropertyValue(Object component, int i, EntityMode entityMode) throws HibernateException {
      return this.componentTuplizer.getPropertyValue(component, i);
   }

   public Object[] getPropertyValues(Object component, SessionImplementor session) throws HibernateException {
      return this.getPropertyValues(component, this.entityMode);
   }

   public Object[] getPropertyValues(Object component, EntityMode entityMode) throws HibernateException {
      return component instanceof Object[] ? (Object[])((Object[])component) : this.componentTuplizer.getPropertyValues(component);
   }

   public void setPropertyValues(Object component, Object[] values, EntityMode entityMode) throws HibernateException {
      this.componentTuplizer.setPropertyValues(component, values);
   }

   public Type[] getSubtypes() {
      return this.propertyTypes;
   }

   public String getName() {
      return "component" + ArrayHelper.toString(this.propertyNames);
   }

   public String toLoggableString(Object value, SessionFactoryImplementor factory) throws HibernateException {
      if (value == null) {
         return "null";
      } else {
         Map result = new HashMap();
         if (this.entityMode == null) {
            throw new ClassCastException(value.getClass().getName());
         } else {
            Object[] values = this.getPropertyValues(value, this.entityMode);

            for(int i = 0; i < this.propertyTypes.length; ++i) {
               result.put(this.propertyNames[i], this.propertyTypes[i].toLoggableString(values[i], factory));
            }

            return StringHelper.unqualify(this.getName()) + result.toString();
         }
      }
   }

   public String[] getPropertyNames() {
      return this.propertyNames;
   }

   public Object deepCopy(Object component, SessionFactoryImplementor factory) throws HibernateException {
      if (component == null) {
         return null;
      } else {
         Object[] values = this.getPropertyValues(component, this.entityMode);

         for(int i = 0; i < this.propertySpan; ++i) {
            values[i] = this.propertyTypes[i].deepCopy(values[i], factory);
         }

         Object result = this.instantiate(this.entityMode);
         this.setPropertyValues(result, values, this.entityMode);
         if (this.componentTuplizer.hasParentProperty()) {
            this.componentTuplizer.setParent(result, this.componentTuplizer.getParent(component), factory);
         }

         return result;
      }
   }

   public Object replace(Object original, Object target, SessionImplementor session, Object owner, Map copyCache) throws HibernateException {
      if (original == null) {
         return null;
      } else {
         Object result = target == null ? this.instantiate(owner, session) : target;
         Object[] values = TypeHelper.replace(this.getPropertyValues(original, this.entityMode), this.getPropertyValues(result, this.entityMode), this.propertyTypes, session, owner, copyCache);
         this.setPropertyValues(result, values, this.entityMode);
         return result;
      }
   }

   public Object replace(Object original, Object target, SessionImplementor session, Object owner, Map copyCache, ForeignKeyDirection foreignKeyDirection) throws HibernateException {
      if (original == null) {
         return null;
      } else {
         Object result = target == null ? this.instantiate(owner, session) : target;
         Object[] values = TypeHelper.replace(this.getPropertyValues(original, this.entityMode), this.getPropertyValues(result, this.entityMode), this.propertyTypes, session, owner, copyCache, foreignKeyDirection);
         this.setPropertyValues(result, values, this.entityMode);
         return result;
      }
   }

   public Object instantiate(EntityMode entityMode) throws HibernateException {
      return this.componentTuplizer.instantiate();
   }

   public Object instantiate(Object parent, SessionImplementor session) throws HibernateException {
      Object result = this.instantiate(this.entityMode);
      if (this.componentTuplizer.hasParentProperty() && parent != null) {
         this.componentTuplizer.setParent(result, session.getPersistenceContext().proxyFor(parent), session.getFactory());
      }

      return result;
   }

   public CascadeStyle getCascadeStyle(int i) {
      return this.cascade[i];
   }

   public boolean isMutable() {
      return true;
   }

   public Serializable disassemble(Object value, SessionImplementor session, Object owner) throws HibernateException {
      if (value == null) {
         return null;
      } else {
         Object[] values = this.getPropertyValues(value, this.entityMode);

         for(int i = 0; i < this.propertyTypes.length; ++i) {
            values[i] = this.propertyTypes[i].disassemble(values[i], session, owner);
         }

         return values;
      }
   }

   public Object assemble(Serializable object, SessionImplementor session, Object owner) throws HibernateException {
      if (object == null) {
         return null;
      } else {
         Object[] values = (Object[])object;
         Object[] assembled = new Object[values.length];

         for(int i = 0; i < this.propertyTypes.length; ++i) {
            assembled[i] = this.propertyTypes[i].assemble((Serializable)values[i], session, owner);
         }

         Object result = this.instantiate(owner, session);
         this.setPropertyValues(result, assembled, this.entityMode);
         return result;
      }
   }

   public FetchMode getFetchMode(int i) {
      return this.joinedFetch[i];
   }

   public Object hydrate(ResultSet rs, String[] names, SessionImplementor session, Object owner) throws HibernateException, SQLException {
      int begin = 0;
      boolean notNull = false;
      Object[] values = new Object[this.propertySpan];

      for(int i = 0; i < this.propertySpan; ++i) {
         int length = this.propertyTypes[i].getColumnSpan(session.getFactory());
         String[] range = ArrayHelper.slice(names, begin, length);
         Object val = this.propertyTypes[i].hydrate(rs, range, session, owner);
         if (val == null) {
            if (this.isKey) {
               return null;
            }
         } else {
            notNull = true;
         }

         values[i] = val;
         begin += length;
      }

      return notNull ? values : null;
   }

   public Object resolve(Object value, SessionImplementor session, Object owner) throws HibernateException {
      if (value == null) {
         return null;
      } else {
         Object result = this.instantiate(owner, session);
         Object[] values = value;
         Object[] resolvedValues = new Object[values.length];

         for(int i = 0; i < values.length; ++i) {
            resolvedValues[i] = this.propertyTypes[i].resolve(values[i], session, owner);
         }

         this.setPropertyValues(result, resolvedValues, this.entityMode);
         return result;
      }
   }

   public Object semiResolve(Object value, SessionImplementor session, Object owner) throws HibernateException {
      return this.resolve(value, session, owner);
   }

   public boolean[] getPropertyNullability() {
      return this.propertyNullability;
   }

   public boolean isXMLElement() {
      return true;
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

         for(int i = 0; i < this.propertyTypes.length; ++i) {
            boolean[] propertyNullness = this.propertyTypes[i].toColumnNullness(values[i], mapping);
            System.arraycopy(propertyNullness, 0, result, loc, propertyNullness.length);
            loc += propertyNullness.length;
         }

         return result;
      }
   }

   public boolean isEmbedded() {
      return false;
   }

   public int getPropertyIndex(String name) {
      String[] names = this.getPropertyNames();
      int i = 0;

      for(int max = names.length; i < max; ++i) {
         if (names[i].equals(name)) {
            return i;
         }
      }

      throw new PropertyNotFoundException("Unable to locate property named " + name + " on " + this.getReturnedClass().getName());
   }
}
