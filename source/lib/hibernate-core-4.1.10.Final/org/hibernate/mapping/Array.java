package org.hibernate.mapping;

import org.hibernate.MappingException;
import org.hibernate.cfg.Mappings;
import org.hibernate.internal.util.ReflectHelper;
import org.hibernate.type.CollectionType;
import org.hibernate.type.PrimitiveType;
import org.hibernate.type.Type;

public class Array extends List {
   private String elementClassName;

   public Array(Mappings mappings, PersistentClass owner) {
      super(mappings, owner);
   }

   public Class getElementClass() throws MappingException {
      if (this.elementClassName == null) {
         Type elementType = this.getElement().getType();
         return this.isPrimitiveArray() ? ((PrimitiveType)elementType).getPrimitiveClass() : elementType.getReturnedClass();
      } else {
         try {
            return ReflectHelper.classForName(this.elementClassName);
         } catch (ClassNotFoundException cnfe) {
            throw new MappingException(cnfe);
         }
      }
   }

   public CollectionType getDefaultCollectionType() throws MappingException {
      return this.getMappings().getTypeResolver().getTypeFactory().array(this.getRole(), this.getReferencedPropertyName(), this.getElementClass());
   }

   public boolean isArray() {
      return true;
   }

   public String getElementClassName() {
      return this.elementClassName;
   }

   public void setElementClassName(String elementClassName) {
      this.elementClassName = elementClassName;
   }

   public Object accept(ValueVisitor visitor) {
      return visitor.accept(this);
   }
}
