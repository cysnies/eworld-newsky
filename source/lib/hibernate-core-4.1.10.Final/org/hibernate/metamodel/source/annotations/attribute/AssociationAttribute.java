package org.hibernate.metamodel.source.annotations.attribute;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import org.hibernate.FetchMode;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.mapping.PropertyGeneration;
import org.hibernate.metamodel.source.MappingException;
import org.hibernate.metamodel.source.annotations.EnumConversionHelper;
import org.hibernate.metamodel.source.annotations.HibernateDotNames;
import org.hibernate.metamodel.source.annotations.JPADotNames;
import org.hibernate.metamodel.source.annotations.JandexHelper;
import org.hibernate.metamodel.source.annotations.attribute.type.AttributeTypeResolver;
import org.hibernate.metamodel.source.annotations.attribute.type.AttributeTypeResolverImpl;
import org.hibernate.metamodel.source.annotations.attribute.type.CompositeAttributeTypeResolver;
import org.hibernate.metamodel.source.annotations.entity.EntityBindingContext;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.DotName;

public class AssociationAttribute extends MappedAttribute {
   private final AttributeNature associationNature;
   private final boolean ignoreNotFound;
   private final String referencedEntityType;
   private final String mappedBy;
   private final Set cascadeTypes;
   private final boolean isOptional;
   private final boolean isLazy;
   private final boolean isOrphanRemoval;
   private final FetchMode fetchMode;
   private final boolean mapsId;
   private final String referencedIdAttributeName;
   private boolean isInsertable = true;
   private boolean isUpdatable = true;
   private AttributeTypeResolver resolver;

   public static AssociationAttribute createAssociationAttribute(String name, Class attributeType, AttributeNature attributeNature, String accessType, Map annotations, EntityBindingContext context) {
      return new AssociationAttribute(name, attributeType, attributeNature, accessType, annotations, context);
   }

   private AssociationAttribute(String name, Class javaType, AttributeNature associationType, String accessType, Map annotations, EntityBindingContext context) {
      super(name, javaType, accessType, annotations, context);
      this.associationNature = associationType;
      this.ignoreNotFound = this.ignoreNotFound();
      AnnotationInstance associationAnnotation = JandexHelper.getSingleAnnotation(annotations, associationType.getAnnotationDotName());
      this.referencedEntityType = this.determineReferencedEntityType(associationAnnotation);
      this.mappedBy = this.determineMappedByAttributeName(associationAnnotation);
      this.isOptional = this.determineOptionality(associationAnnotation);
      this.isLazy = this.determineFetchType(associationAnnotation);
      this.isOrphanRemoval = this.determineOrphanRemoval(associationAnnotation);
      this.cascadeTypes = this.determineCascadeTypes(associationAnnotation);
      this.fetchMode = this.determineFetchMode();
      this.referencedIdAttributeName = this.determineMapsId();
      this.mapsId = this.referencedIdAttributeName != null;
   }

   public boolean isIgnoreNotFound() {
      return this.ignoreNotFound;
   }

   public String getReferencedEntityType() {
      return this.referencedEntityType;
   }

   public String getMappedBy() {
      return this.mappedBy;
   }

   public AttributeNature getAssociationNature() {
      return this.associationNature;
   }

   public Set getCascadeTypes() {
      return this.cascadeTypes;
   }

   public boolean isOrphanRemoval() {
      return this.isOrphanRemoval;
   }

   public FetchMode getFetchMode() {
      return this.fetchMode;
   }

   public String getReferencedIdAttributeName() {
      return this.referencedIdAttributeName;
   }

   public boolean mapsId() {
      return this.mapsId;
   }

   public AttributeTypeResolver getHibernateTypeResolver() {
      if (this.resolver == null) {
         this.resolver = this.getDefaultHibernateTypeResolver();
      }

      return this.resolver;
   }

   public boolean isLazy() {
      return this.isLazy;
   }

   public boolean isOptional() {
      return this.isOptional;
   }

   public boolean isInsertable() {
      return this.isInsertable;
   }

   public boolean isUpdatable() {
      return this.isUpdatable;
   }

   public PropertyGeneration getPropertyGeneration() {
      return PropertyGeneration.NEVER;
   }

   private AttributeTypeResolver getDefaultHibernateTypeResolver() {
      return new CompositeAttributeTypeResolver(new AttributeTypeResolverImpl(this));
   }

   private boolean ignoreNotFound() {
      NotFoundAction action = NotFoundAction.EXCEPTION;
      AnnotationInstance notFoundAnnotation = JandexHelper.getSingleAnnotation(this.annotations(), HibernateDotNames.NOT_FOUND);
      if (notFoundAnnotation != null) {
         AnnotationValue actionValue = notFoundAnnotation.value("action");
         if (actionValue != null) {
            action = (NotFoundAction)Enum.valueOf(NotFoundAction.class, actionValue.asEnum());
         }
      }

      return NotFoundAction.IGNORE.equals(action);
   }

   private boolean determineOptionality(AnnotationInstance associationAnnotation) {
      boolean optional = true;
      AnnotationValue optionalValue = associationAnnotation.value("optional");
      if (optionalValue != null) {
         optional = optionalValue.asBoolean();
      }

      return optional;
   }

   private boolean determineOrphanRemoval(AnnotationInstance associationAnnotation) {
      boolean orphanRemoval = false;
      AnnotationValue orphanRemovalValue = associationAnnotation.value("orphanRemoval");
      if (orphanRemovalValue != null) {
         orphanRemoval = orphanRemovalValue.asBoolean();
      }

      return orphanRemoval;
   }

   private boolean determineFetchType(AnnotationInstance associationAnnotation) {
      boolean lazy = false;
      AnnotationValue fetchValue = associationAnnotation.value("fetch");
      if (fetchValue != null) {
         FetchType fetchType = (FetchType)Enum.valueOf(FetchType.class, fetchValue.asEnum());
         if (FetchType.LAZY.equals(fetchType)) {
            lazy = true;
         }
      }

      return lazy;
   }

   private String determineReferencedEntityType(AnnotationInstance associationAnnotation) {
      String targetTypeName = this.getAttributeType().getName();
      AnnotationInstance targetAnnotation = JandexHelper.getSingleAnnotation(this.annotations(), HibernateDotNames.TARGET);
      if (targetAnnotation != null) {
         targetTypeName = targetAnnotation.value().asClass().name().toString();
      }

      AnnotationValue targetEntityValue = associationAnnotation.value("targetEntity");
      if (targetEntityValue != null) {
         targetTypeName = targetEntityValue.asClass().name().toString();
      }

      return targetTypeName;
   }

   private String determineMappedByAttributeName(AnnotationInstance associationAnnotation) {
      String mappedBy = null;
      AnnotationValue mappedByAnnotationValue = associationAnnotation.value("mappedBy");
      if (mappedByAnnotationValue != null) {
         mappedBy = mappedByAnnotationValue.asString();
      }

      return mappedBy;
   }

   private Set determineCascadeTypes(AnnotationInstance associationAnnotation) {
      Set<CascadeType> cascadeTypes = new HashSet();
      AnnotationValue cascadeValue = associationAnnotation.value("cascade");
      if (cascadeValue != null) {
         String[] cascades = cascadeValue.asEnumArray();

         for(String s : cascades) {
            cascadeTypes.add(Enum.valueOf(CascadeType.class, s));
         }
      }

      return cascadeTypes;
   }

   private FetchMode determineFetchMode() {
      FetchMode mode = FetchMode.DEFAULT;
      AnnotationInstance fetchAnnotation = JandexHelper.getSingleAnnotation(this.annotations(), HibernateDotNames.FETCH);
      if (fetchAnnotation != null) {
         org.hibernate.annotations.FetchMode annotationFetchMode = (org.hibernate.annotations.FetchMode)JandexHelper.getEnumValue(fetchAnnotation, "value", org.hibernate.annotations.FetchMode.class);
         mode = EnumConversionHelper.annotationFetchModeToHibernateFetchMode(annotationFetchMode);
      }

      return mode;
   }

   private String determineMapsId() {
      AnnotationInstance mapsIdAnnotation = JandexHelper.getSingleAnnotation(this.annotations(), JPADotNames.MAPS_ID);
      if (mapsIdAnnotation == null) {
         return null;
      } else if (!AttributeNature.MANY_TO_ONE.equals(this.getAssociationNature()) && !AttributeNature.MANY_TO_ONE.equals(this.getAssociationNature())) {
         throw new MappingException("@MapsId can only be specified on a many-to-one or one-to-one associations", this.getContext().getOrigin());
      } else {
         String referencedIdAttributeName = (String)JandexHelper.getValue(mapsIdAnnotation, "value", String.class);
         return referencedIdAttributeName;
      }
   }
}
