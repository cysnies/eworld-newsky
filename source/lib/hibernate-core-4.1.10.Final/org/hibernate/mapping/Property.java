package org.hibernate.mapping;

import java.io.Serializable;
import java.util.Iterator;
import java.util.StringTokenizer;
import org.hibernate.EntityMode;
import org.hibernate.MappingException;
import org.hibernate.PropertyNotFoundException;
import org.hibernate.engine.spi.CascadeStyle;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.internal.util.collections.ArrayHelper;
import org.hibernate.property.Getter;
import org.hibernate.property.PropertyAccessor;
import org.hibernate.property.PropertyAccessorFactory;
import org.hibernate.property.Setter;
import org.hibernate.type.CompositeType;
import org.hibernate.type.Type;

public class Property implements Serializable, MetaAttributable {
   private String name;
   private Value value;
   private String cascade;
   private boolean updateable = true;
   private boolean insertable = true;
   private boolean selectable = true;
   private boolean optimisticLocked = true;
   private PropertyGeneration generation;
   private String propertyAccessorName;
   private boolean lazy;
   private boolean optional;
   private String nodeName;
   private java.util.Map metaAttributes;
   private PersistentClass persistentClass;
   private boolean naturalIdentifier;
   private boolean lob;

   public Property() {
      this.generation = PropertyGeneration.NEVER;
   }

   public boolean isBackRef() {
      return false;
   }

   public boolean isSynthetic() {
      return false;
   }

   public Type getType() throws MappingException {
      return this.value.getType();
   }

   public int getColumnSpan() {
      return this.value.getColumnSpan();
   }

   public Iterator getColumnIterator() {
      return this.value.getColumnIterator();
   }

   public String getName() {
      return this.name;
   }

   public boolean isComposite() {
      return this.value instanceof Component;
   }

   public Value getValue() {
      return this.value;
   }

   public boolean isPrimitive(Class clazz) {
      return this.getGetter(clazz).getReturnType().isPrimitive();
   }

   public CascadeStyle getCascadeStyle() throws MappingException {
      Type type = this.value.getType();
      if (type.isComponentType()) {
         return getCompositeCascadeStyle((CompositeType)type, this.cascade);
      } else {
         return type.isCollectionType() ? getCollectionCascadeStyle(((Collection)this.value).getElement().getType(), this.cascade) : getCascadeStyle(this.cascade);
      }
   }

   private static CascadeStyle getCompositeCascadeStyle(CompositeType compositeType, String cascade) {
      if (compositeType.isAnyType()) {
         return getCascadeStyle(cascade);
      } else {
         int length = compositeType.getSubtypes().length;

         for(int i = 0; i < length; ++i) {
            if (compositeType.getCascadeStyle(i) != CascadeStyle.NONE) {
               return CascadeStyle.ALL;
            }
         }

         return getCascadeStyle(cascade);
      }
   }

   private static CascadeStyle getCollectionCascadeStyle(Type elementType, String cascade) {
      return elementType.isComponentType() ? getCompositeCascadeStyle((CompositeType)elementType, cascade) : getCascadeStyle(cascade);
   }

   private static CascadeStyle getCascadeStyle(String cascade) {
      if (cascade != null && !cascade.equals("none")) {
         StringTokenizer tokens = new StringTokenizer(cascade, ", ");
         CascadeStyle[] styles = new CascadeStyle[tokens.countTokens()];

         for(int i = 0; tokens.hasMoreTokens(); styles[i++] = CascadeStyle.getCascadeStyle(tokens.nextToken())) {
         }

         return new CascadeStyle.MultipleCascadeStyle(styles);
      } else {
         return CascadeStyle.NONE;
      }
   }

   public String getCascade() {
      return this.cascade;
   }

   public void setCascade(String cascade) {
      this.cascade = cascade;
   }

   public void setName(String name) {
      this.name = name == null ? null : name.intern();
   }

   public void setValue(Value value) {
      this.value = value;
   }

   public boolean isUpdateable() {
      return this.updateable && !ArrayHelper.isAllFalse(this.value.getColumnUpdateability());
   }

   public boolean isInsertable() {
      boolean[] columnInsertability = this.value.getColumnInsertability();
      return this.insertable && (columnInsertability.length == 0 || !ArrayHelper.isAllFalse(columnInsertability));
   }

   public PropertyGeneration getGeneration() {
      return this.generation;
   }

   public void setGeneration(PropertyGeneration generation) {
      this.generation = generation;
   }

   public void setUpdateable(boolean mutable) {
      this.updateable = mutable;
   }

   public void setInsertable(boolean insertable) {
      this.insertable = insertable;
   }

   public String getPropertyAccessorName() {
      return this.propertyAccessorName;
   }

   public void setPropertyAccessorName(String string) {
      this.propertyAccessorName = string;
   }

   boolean isNullable() {
      return this.value == null || this.value.isNullable();
   }

   public boolean isBasicPropertyAccessor() {
      return this.propertyAccessorName == null || "property".equals(this.propertyAccessorName);
   }

   public java.util.Map getMetaAttributes() {
      return this.metaAttributes;
   }

   public MetaAttribute getMetaAttribute(String attributeName) {
      return this.metaAttributes == null ? null : (MetaAttribute)this.metaAttributes.get(attributeName);
   }

   public void setMetaAttributes(java.util.Map metas) {
      this.metaAttributes = metas;
   }

   public boolean isValid(Mapping mapping) throws MappingException {
      return this.getValue().isValid(mapping);
   }

   public String toString() {
      return this.getClass().getName() + '(' + this.name + ')';
   }

   public void setLazy(boolean lazy) {
      this.lazy = lazy;
   }

   public boolean isLazy() {
      if (!(this.value instanceof ToOne)) {
         return this.lazy;
      } else {
         ToOne toOneValue = (ToOne)this.value;
         return toOneValue.isLazy() && toOneValue.isUnwrapProxy();
      }
   }

   public boolean isOptimisticLocked() {
      return this.optimisticLocked;
   }

   public void setOptimisticLocked(boolean optimisticLocked) {
      this.optimisticLocked = optimisticLocked;
   }

   public boolean isOptional() {
      return this.optional || this.isNullable();
   }

   public void setOptional(boolean optional) {
      this.optional = optional;
   }

   public PersistentClass getPersistentClass() {
      return this.persistentClass;
   }

   public void setPersistentClass(PersistentClass persistentClass) {
      this.persistentClass = persistentClass;
   }

   public boolean isSelectable() {
      return this.selectable;
   }

   public void setSelectable(boolean selectable) {
      this.selectable = selectable;
   }

   public String getNodeName() {
      return this.nodeName;
   }

   public void setNodeName(String nodeName) {
      this.nodeName = nodeName;
   }

   public String getAccessorPropertyName(EntityMode mode) {
      return this.getName();
   }

   public Getter getGetter(Class clazz) throws PropertyNotFoundException, MappingException {
      return this.getPropertyAccessor(clazz).getGetter(clazz, this.name);
   }

   public Setter getSetter(Class clazz) throws PropertyNotFoundException, MappingException {
      return this.getPropertyAccessor(clazz).getSetter(clazz, this.name);
   }

   public PropertyAccessor getPropertyAccessor(Class clazz) throws MappingException {
      return PropertyAccessorFactory.getPropertyAccessor(clazz, this.getPropertyAccessorName());
   }

   public boolean isNaturalIdentifier() {
      return this.naturalIdentifier;
   }

   public void setNaturalIdentifier(boolean naturalIdentifier) {
      this.naturalIdentifier = naturalIdentifier;
   }

   public boolean isLob() {
      return this.lob;
   }

   public void setLob(boolean lob) {
      this.lob = lob;
   }
}
