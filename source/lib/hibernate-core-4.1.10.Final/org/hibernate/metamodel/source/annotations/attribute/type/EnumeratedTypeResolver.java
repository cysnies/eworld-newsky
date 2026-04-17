package org.hibernate.metamodel.source.annotations.attribute.type;

import java.util.HashMap;
import java.util.Map;
import org.hibernate.AnnotationException;
import org.hibernate.AssertionFailure;
import org.hibernate.metamodel.source.annotations.JPADotNames;
import org.hibernate.metamodel.source.annotations.JandexHelper;
import org.hibernate.metamodel.source.annotations.attribute.MappedAttribute;
import org.hibernate.type.EnumType;
import org.jboss.jandex.AnnotationInstance;

public class EnumeratedTypeResolver extends AbstractAttributeTypeResolver {
   private final MappedAttribute mappedAttribute;
   private final boolean isMapKey;

   public EnumeratedTypeResolver(MappedAttribute mappedAttribute) {
      if (mappedAttribute == null) {
         throw new AssertionFailure("MappedAttribute is null");
      } else {
         this.mappedAttribute = mappedAttribute;
         this.isMapKey = false;
      }
   }

   protected AnnotationInstance getTypeDeterminingAnnotationInstance() {
      return JandexHelper.getSingleAnnotation(this.mappedAttribute.annotations(), JPADotNames.ENUMERATED);
   }

   public String resolveHibernateTypeName(AnnotationInstance enumeratedAnnotation) {
      boolean isEnum = this.mappedAttribute.getAttributeType().isEnum();
      if (!isEnum) {
         if (enumeratedAnnotation != null) {
            throw new AnnotationException("Attribute " + this.mappedAttribute.getName() + " is not a Enumerated type, but has a @Enumerated annotation.");
         } else {
            return null;
         }
      } else {
         return EnumType.class.getName();
      }
   }

   protected Map resolveHibernateTypeParameters(AnnotationInstance annotationInstance) {
      HashMap<String, String> typeParameters = new HashMap();
      typeParameters.put("enumClass", this.mappedAttribute.getAttributeType().getName());
      if (annotationInstance != null) {
         javax.persistence.EnumType enumType = (javax.persistence.EnumType)JandexHelper.getEnumValue(annotationInstance, "value", javax.persistence.EnumType.class);
         if (javax.persistence.EnumType.ORDINAL.equals(enumType)) {
            typeParameters.put("type", String.valueOf(4));
         } else {
            if (!javax.persistence.EnumType.STRING.equals(enumType)) {
               throw new AssertionFailure("Unknown EnumType: " + enumType);
            }

            typeParameters.put("type", String.valueOf(12));
         }
      } else {
         typeParameters.put("type", String.valueOf(4));
      }

      return typeParameters;
   }
}
