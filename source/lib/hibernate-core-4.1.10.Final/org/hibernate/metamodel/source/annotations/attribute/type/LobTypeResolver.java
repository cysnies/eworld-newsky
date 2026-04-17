package org.hibernate.metamodel.source.annotations.attribute.type;

import java.io.Serializable;
import java.sql.Blob;
import java.sql.Clob;
import java.util.HashMap;
import java.util.Map;
import org.hibernate.AssertionFailure;
import org.hibernate.metamodel.source.annotations.JPADotNames;
import org.hibernate.metamodel.source.annotations.JandexHelper;
import org.hibernate.metamodel.source.annotations.attribute.MappedAttribute;
import org.hibernate.type.CharacterArrayClobType;
import org.hibernate.type.PrimitiveCharacterArrayClobType;
import org.hibernate.type.SerializableToBlobType;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.WrappedMaterializedBlobType;
import org.jboss.jandex.AnnotationInstance;

public class LobTypeResolver extends AbstractAttributeTypeResolver {
   private final MappedAttribute mappedAttribute;

   public LobTypeResolver(MappedAttribute mappedAttribute) {
      if (mappedAttribute == null) {
         throw new AssertionFailure("MappedAttribute is null");
      } else {
         this.mappedAttribute = mappedAttribute;
      }
   }

   protected AnnotationInstance getTypeDeterminingAnnotationInstance() {
      return JandexHelper.getSingleAnnotation(this.mappedAttribute.annotations(), JPADotNames.LOB);
   }

   public String resolveHibernateTypeName(AnnotationInstance annotationInstance) {
      if (annotationInstance == null) {
         return null;
      } else {
         String type = null;
         if (Clob.class.isAssignableFrom(this.mappedAttribute.getAttributeType())) {
            type = StandardBasicTypes.CLOB.getName();
         } else if (Blob.class.isAssignableFrom(this.mappedAttribute.getAttributeType())) {
            type = StandardBasicTypes.BLOB.getName();
         } else if (String.class.isAssignableFrom(this.mappedAttribute.getAttributeType())) {
            type = StandardBasicTypes.MATERIALIZED_CLOB.getName();
         } else if (Character[].class.isAssignableFrom(this.mappedAttribute.getAttributeType())) {
            type = CharacterArrayClobType.class.getName();
         } else if (char[].class.isAssignableFrom(this.mappedAttribute.getAttributeType())) {
            type = PrimitiveCharacterArrayClobType.class.getName();
         } else if (Byte[].class.isAssignableFrom(this.mappedAttribute.getAttributeType())) {
            type = WrappedMaterializedBlobType.class.getName();
         } else if (byte[].class.isAssignableFrom(this.mappedAttribute.getAttributeType())) {
            type = StandardBasicTypes.MATERIALIZED_BLOB.getName();
         } else if (Serializable.class.isAssignableFrom(this.mappedAttribute.getAttributeType())) {
            type = SerializableToBlobType.class.getName();
         } else {
            type = "blob";
         }

         return type;
      }
   }

   protected Map resolveHibernateTypeParameters(AnnotationInstance annotationInstance) {
      if (this.getExplicitHibernateTypeName().equals(SerializableToBlobType.class.getName())) {
         HashMap<String, String> typeParameters = new HashMap();
         typeParameters.put("classname", this.mappedAttribute.getAttributeType().getName());
         return typeParameters;
      } else {
         return null;
      }
   }
}
