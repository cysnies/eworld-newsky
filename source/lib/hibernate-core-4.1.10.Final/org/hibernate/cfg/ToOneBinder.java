package org.hibernate.cfg;

import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import org.hibernate.AssertionFailure;
import org.hibernate.annotations.common.reflection.XClass;
import org.hibernate.annotations.common.reflection.XProperty;

public class ToOneBinder {
   public static String getReferenceEntityName(PropertyData propertyData, XClass targetEntity, Mappings mappings) {
      return AnnotationBinder.isDefault(targetEntity, mappings) ? propertyData.getClassOrElementName() : targetEntity.getName();
   }

   public static String getReferenceEntityName(PropertyData propertyData, Mappings mappings) {
      XClass targetEntity = getTargetEntity(propertyData, mappings);
      return AnnotationBinder.isDefault(targetEntity, mappings) ? propertyData.getClassOrElementName() : targetEntity.getName();
   }

   public static XClass getTargetEntity(PropertyData propertyData, Mappings mappings) {
      XProperty property = propertyData.getProperty();
      return mappings.getReflectionManager().toXClass(getTargetEntityClass(property));
   }

   private static Class getTargetEntityClass(XProperty property) {
      ManyToOne mTo = (ManyToOne)property.getAnnotation(ManyToOne.class);
      if (mTo != null) {
         return mTo.targetEntity();
      } else {
         OneToOne oTo = (OneToOne)property.getAnnotation(OneToOne.class);
         if (oTo != null) {
            return oTo.targetEntity();
         } else {
            throw new AssertionFailure("Unexpected discovery of a targetEntity: " + property.getName());
         }
      }
   }
}
