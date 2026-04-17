package org.hibernate.criterion;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.hibernate.Criteria;
import org.hibernate.EntityMode;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.TypedValue;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.type.CompositeType;
import org.hibernate.type.Type;

public class Example implements Criterion {
   private final Object entity;
   private final Set excludedProperties = new HashSet();
   private PropertySelector selector;
   private boolean isLikeEnabled;
   private Character escapeCharacter;
   private boolean isIgnoreCaseEnabled;
   private MatchMode matchMode;
   private static final PropertySelector NOT_NULL = new NotNullPropertySelector();
   private static final PropertySelector ALL = new AllPropertySelector();
   private static final PropertySelector NOT_NULL_OR_ZERO = new NotNullOrZeroPropertySelector();
   private static final Object[] TYPED_VALUES = new TypedValue[0];

   public Example setEscapeCharacter(Character escapeCharacter) {
      this.escapeCharacter = escapeCharacter;
      return this;
   }

   public Example setPropertySelector(PropertySelector selector) {
      this.selector = selector;
      return this;
   }

   public Example excludeZeroes() {
      this.setPropertySelector(NOT_NULL_OR_ZERO);
      return this;
   }

   public Example excludeNone() {
      this.setPropertySelector(ALL);
      return this;
   }

   public Example enableLike(MatchMode matchMode) {
      this.isLikeEnabled = true;
      this.matchMode = matchMode;
      return this;
   }

   public Example enableLike() {
      return this.enableLike(MatchMode.EXACT);
   }

   public Example ignoreCase() {
      this.isIgnoreCaseEnabled = true;
      return this;
   }

   public Example excludeProperty(String name) {
      this.excludedProperties.add(name);
      return this;
   }

   public static Example create(Object entity) {
      if (entity == null) {
         throw new NullPointerException("null example");
      } else {
         return new Example(entity, NOT_NULL);
      }
   }

   protected Example(Object entity, PropertySelector selector) {
      this.entity = entity;
      this.selector = selector;
   }

   public String toString() {
      return "example (" + this.entity + ')';
   }

   private boolean isPropertyIncluded(Object value, String name, Type type) {
      return !this.excludedProperties.contains(name) && !type.isAssociationType() && this.selector.include(value, name, type);
   }

   public String toSqlString(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
      StringBuilder buf = (new StringBuilder()).append('(');
      EntityPersister meta = criteriaQuery.getFactory().getEntityPersister(criteriaQuery.getEntityName(criteria));
      String[] propertyNames = meta.getPropertyNames();
      Type[] propertyTypes = meta.getPropertyTypes();
      Object[] propertyValues = meta.getPropertyValues(this.entity);

      for(int i = 0; i < propertyNames.length; ++i) {
         Object propertyValue = propertyValues[i];
         String propertyName = propertyNames[i];
         boolean isPropertyIncluded = i != meta.getVersionProperty() && this.isPropertyIncluded(propertyValue, propertyName, propertyTypes[i]);
         if (isPropertyIncluded) {
            if (propertyTypes[i].isComponentType()) {
               this.appendComponentCondition(propertyName, propertyValue, (CompositeType)propertyTypes[i], criteria, criteriaQuery, buf);
            } else {
               this.appendPropertyCondition(propertyName, propertyValue, criteria, criteriaQuery, buf);
            }
         }
      }

      if (buf.length() == 1) {
         buf.append("1=1");
      }

      return buf.append(')').toString();
   }

   public TypedValue[] getTypedValues(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
      EntityPersister meta = criteriaQuery.getFactory().getEntityPersister(criteriaQuery.getEntityName(criteria));
      String[] propertyNames = meta.getPropertyNames();
      Type[] propertyTypes = meta.getPropertyTypes();
      Object[] values = meta.getPropertyValues(this.entity);
      List list = new ArrayList();

      for(int i = 0; i < propertyNames.length; ++i) {
         Object value = values[i];
         Type type = propertyTypes[i];
         String name = propertyNames[i];
         boolean isPropertyIncluded = i != meta.getVersionProperty() && this.isPropertyIncluded(value, name, type);
         if (isPropertyIncluded) {
            if (propertyTypes[i].isComponentType()) {
               this.addComponentTypedValues(name, value, (CompositeType)type, list, criteria, criteriaQuery);
            } else {
               this.addPropertyTypedValue(value, type, list);
            }
         }
      }

      return (TypedValue[])list.toArray(TYPED_VALUES);
   }

   private EntityMode getEntityMode(Criteria criteria, CriteriaQuery criteriaQuery) {
      EntityPersister meta = criteriaQuery.getFactory().getEntityPersister(criteriaQuery.getEntityName(criteria));
      EntityMode result = meta.getEntityMode();
      if (!meta.getEntityMetamodel().getTuplizer().isInstance(this.entity)) {
         throw new ClassCastException(this.entity.getClass().getName());
      } else {
         return result;
      }
   }

   protected void addPropertyTypedValue(Object value, Type type, List list) {
      if (value != null) {
         if (value instanceof String) {
            String string = (String)value;
            if (this.isIgnoreCaseEnabled) {
               string = string.toLowerCase();
            }

            if (this.isLikeEnabled) {
               string = this.matchMode.toMatchString(string);
            }

            value = string;
         }

         list.add(new TypedValue(type, value, (EntityMode)null));
      }

   }

   protected void addComponentTypedValues(String path, Object component, CompositeType type, List list, Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
      if (component != null) {
         String[] propertyNames = type.getPropertyNames();
         Type[] subtypes = type.getSubtypes();
         Object[] values = type.getPropertyValues(component, this.getEntityMode(criteria, criteriaQuery));

         for(int i = 0; i < propertyNames.length; ++i) {
            Object value = values[i];
            Type subtype = subtypes[i];
            String subpath = StringHelper.qualify(path, propertyNames[i]);
            if (this.isPropertyIncluded(value, subpath, subtype)) {
               if (subtype.isComponentType()) {
                  this.addComponentTypedValues(subpath, value, (CompositeType)subtype, list, criteria, criteriaQuery);
               } else {
                  this.addPropertyTypedValue(value, subtype, list);
               }
            }
         }
      }

   }

   protected void appendPropertyCondition(String propertyName, Object propertyValue, Criteria criteria, CriteriaQuery cq, StringBuilder buf) throws HibernateException {
      Criterion crit;
      if (propertyValue != null) {
         boolean isString = propertyValue instanceof String;
         if (this.isLikeEnabled && isString) {
            crit = new LikeExpression(propertyName, (String)propertyValue, this.matchMode, this.escapeCharacter, this.isIgnoreCaseEnabled);
         } else {
            crit = new SimpleExpression(propertyName, propertyValue, "=", this.isIgnoreCaseEnabled && isString);
         }
      } else {
         crit = new NullExpression(propertyName);
      }

      String critCondition = crit.toSqlString(criteria, cq);
      if (buf.length() > 1 && critCondition.trim().length() > 0) {
         buf.append(" and ");
      }

      buf.append(critCondition);
   }

   protected void appendComponentCondition(String path, Object component, CompositeType type, Criteria criteria, CriteriaQuery criteriaQuery, StringBuilder buf) throws HibernateException {
      if (component != null) {
         String[] propertyNames = type.getPropertyNames();
         Object[] values = type.getPropertyValues(component, this.getEntityMode(criteria, criteriaQuery));
         Type[] subtypes = type.getSubtypes();

         for(int i = 0; i < propertyNames.length; ++i) {
            String subpath = StringHelper.qualify(path, propertyNames[i]);
            Object value = values[i];
            if (this.isPropertyIncluded(value, subpath, subtypes[i])) {
               Type subtype = subtypes[i];
               if (subtype.isComponentType()) {
                  this.appendComponentCondition(subpath, value, (CompositeType)subtype, criteria, criteriaQuery, buf);
               } else {
                  this.appendPropertyCondition(subpath, value, criteria, criteriaQuery, buf);
               }
            }
         }
      }

   }

   static final class AllPropertySelector implements PropertySelector {
      public boolean include(Object object, String propertyName, Type type) {
         return true;
      }

      private Object readResolve() {
         return Example.ALL;
      }
   }

   static final class NotNullPropertySelector implements PropertySelector {
      public boolean include(Object object, String propertyName, Type type) {
         return object != null;
      }

      private Object readResolve() {
         return Example.NOT_NULL;
      }
   }

   static final class NotNullOrZeroPropertySelector implements PropertySelector {
      public boolean include(Object object, String propertyName, Type type) {
         return object != null && (!(object instanceof Number) || ((Number)object).longValue() != 0L);
      }

      private Object readResolve() {
         return Example.NOT_NULL_OR_ZERO;
      }
   }

   public interface PropertySelector extends Serializable {
      boolean include(Object var1, String var2, Type var3);
   }
}
