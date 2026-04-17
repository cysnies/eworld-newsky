package org.hibernate.metamodel.source.annotations.attribute.type;

import java.util.HashMap;
import java.util.Map;
import org.hibernate.metamodel.source.annotations.HibernateDotNames;
import org.hibernate.metamodel.source.annotations.JandexHelper;
import org.hibernate.metamodel.source.annotations.attribute.MappedAttribute;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;

public class AttributeTypeResolverImpl extends AbstractAttributeTypeResolver {
   private final MappedAttribute mappedAttribute;

   public AttributeTypeResolverImpl(MappedAttribute mappedAttribute) {
      this.mappedAttribute = mappedAttribute;
   }

   protected String resolveHibernateTypeName(AnnotationInstance typeAnnotation) {
      String typeName = null;
      if (typeAnnotation != null) {
         typeName = (String)JandexHelper.getValue(typeAnnotation, "type", String.class);
      }

      return typeName;
   }

   protected Map resolveHibernateTypeParameters(AnnotationInstance typeAnnotation) {
      HashMap<String, String> typeParameters = new HashMap();
      AnnotationValue parameterAnnotationValue = typeAnnotation.value("parameters");
      if (parameterAnnotationValue != null) {
         AnnotationInstance[] parameterAnnotations = parameterAnnotationValue.asNestedArray();

         for(AnnotationInstance parameterAnnotationInstance : parameterAnnotations) {
            typeParameters.put(JandexHelper.getValue(parameterAnnotationInstance, "name", String.class), JandexHelper.getValue(parameterAnnotationInstance, "value", String.class));
         }
      }

      return typeParameters;
   }

   protected AnnotationInstance getTypeDeterminingAnnotationInstance() {
      return JandexHelper.getSingleAnnotation(this.mappedAttribute.annotations(), HibernateDotNames.TYPE);
   }
}
