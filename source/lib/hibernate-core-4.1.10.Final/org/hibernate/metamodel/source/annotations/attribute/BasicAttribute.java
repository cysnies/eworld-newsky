package org.hibernate.metamodel.source.annotations.attribute;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.persistence.FetchType;
import javax.persistence.GenerationType;
import org.hibernate.AnnotationException;
import org.hibernate.annotations.GenerationTime;
import org.hibernate.internal.jaxb.Origin;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.mapping.PropertyGeneration;
import org.hibernate.metamodel.binding.IdGenerator;
import org.hibernate.metamodel.source.MappingException;
import org.hibernate.metamodel.source.annotations.EnumConversionHelper;
import org.hibernate.metamodel.source.annotations.HibernateDotNames;
import org.hibernate.metamodel.source.annotations.JPADotNames;
import org.hibernate.metamodel.source.annotations.JandexHelper;
import org.hibernate.metamodel.source.annotations.attribute.type.AttributeTypeResolver;
import org.hibernate.metamodel.source.annotations.attribute.type.AttributeTypeResolverImpl;
import org.hibernate.metamodel.source.annotations.attribute.type.CompositeAttributeTypeResolver;
import org.hibernate.metamodel.source.annotations.attribute.type.EnumeratedTypeResolver;
import org.hibernate.metamodel.source.annotations.attribute.type.LobTypeResolver;
import org.hibernate.metamodel.source.annotations.attribute.type.TemporalTypeResolver;
import org.hibernate.metamodel.source.annotations.entity.EntityBindingContext;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.DotName;

public class BasicAttribute extends MappedAttribute {
   private final IdGenerator idGenerator;
   private final boolean isVersioned;
   private boolean isLazy = false;
   private boolean isOptional = true;
   private PropertyGeneration propertyGeneration;
   private boolean isInsertable = true;
   private boolean isUpdatable = true;
   private final String customWriteFragment;
   private final String customReadFragment;
   private final String checkCondition;
   private AttributeTypeResolver resolver;

   public static BasicAttribute createSimpleAttribute(String name, Class attributeType, Map annotations, String accessType, EntityBindingContext context) {
      return new BasicAttribute(name, attributeType, accessType, annotations, context);
   }

   BasicAttribute(String name, Class attributeType, String accessType, Map annotations, EntityBindingContext context) {
      super(name, attributeType, accessType, annotations, context);
      AnnotationInstance versionAnnotation = JandexHelper.getSingleAnnotation(annotations, JPADotNames.VERSION);
      this.isVersioned = versionAnnotation != null;
      if (this.isId()) {
         this.getColumnValues().setUnique(true);
         this.getColumnValues().setNullable(false);
         this.idGenerator = this.checkGeneratedValueAnnotation();
      } else {
         this.idGenerator = null;
      }

      this.checkBasicAnnotation();
      this.checkGeneratedAnnotation();
      List<AnnotationInstance> columnTransformerAnnotations = this.getAllColumnTransformerAnnotations();
      String[] readWrite = this.createCustomReadWrite(columnTransformerAnnotations);
      this.customReadFragment = readWrite[0];
      this.customWriteFragment = readWrite[1];
      this.checkCondition = this.parseCheckAnnotation();
   }

   public boolean isVersioned() {
      return this.isVersioned;
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
      return this.propertyGeneration;
   }

   public String getCustomWriteFragment() {
      return this.customWriteFragment;
   }

   public String getCustomReadFragment() {
      return this.customReadFragment;
   }

   public String getCheckCondition() {
      return this.checkCondition;
   }

   public IdGenerator getIdGenerator() {
      return this.idGenerator;
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("SimpleAttribute");
      sb.append("{name=").append(this.getName());
      return sb.toString();
   }

   private void checkBasicAnnotation() {
      AnnotationInstance basicAnnotation = JandexHelper.getSingleAnnotation(this.annotations(), JPADotNames.BASIC);
      if (basicAnnotation != null) {
         FetchType fetchType = FetchType.LAZY;
         AnnotationValue fetchValue = basicAnnotation.value("fetch");
         if (fetchValue != null) {
            fetchType = (FetchType)Enum.valueOf(FetchType.class, fetchValue.asEnum());
         }

         this.isLazy = fetchType == FetchType.LAZY;
         AnnotationValue optionalValue = basicAnnotation.value("optional");
         if (optionalValue != null) {
            this.isOptional = optionalValue.asBoolean();
         }
      }

   }

   private void checkGeneratedAnnotation() {
      AnnotationInstance generatedAnnotation = JandexHelper.getSingleAnnotation(this.annotations(), HibernateDotNames.GENERATED);
      if (generatedAnnotation != null) {
         this.isInsertable = false;
         AnnotationValue generationTimeValue = generatedAnnotation.value();
         if (generationTimeValue != null) {
            GenerationTime genTime = (GenerationTime)Enum.valueOf(GenerationTime.class, generationTimeValue.asEnum());
            if (GenerationTime.ALWAYS.equals(genTime)) {
               this.isUpdatable = false;
               this.propertyGeneration = PropertyGeneration.parse(genTime.toString().toLowerCase());
            }
         }
      }

   }

   private List getAllColumnTransformerAnnotations() {
      List<AnnotationInstance> allColumnTransformerAnnotations = new ArrayList();
      AnnotationInstance columnTransformersAnnotations = JandexHelper.getSingleAnnotation(this.annotations(), HibernateDotNames.COLUMN_TRANSFORMERS);
      if (columnTransformersAnnotations != null) {
         AnnotationInstance[] annotationInstances = ((AnnotationInstance)allColumnTransformerAnnotations.get(0)).value().asNestedArray();
         allColumnTransformerAnnotations.addAll(Arrays.asList(annotationInstances));
      }

      AnnotationInstance columnTransformerAnnotation = JandexHelper.getSingleAnnotation(this.annotations(), HibernateDotNames.COLUMN_TRANSFORMER);
      if (columnTransformerAnnotation != null) {
         allColumnTransformerAnnotations.add(columnTransformerAnnotation);
      }

      return allColumnTransformerAnnotations;
   }

   private String[] createCustomReadWrite(List columnTransformerAnnotations) {
      String[] readWrite = new String[2];
      boolean alreadyProcessedForColumn = false;

      for(AnnotationInstance annotationInstance : columnTransformerAnnotations) {
         String forColumn = annotationInstance.value("forColumn") == null ? null : annotationInstance.value("forColumn").asString();
         if (forColumn == null || forColumn.equals(this.getName())) {
            if (alreadyProcessedForColumn) {
               throw new AnnotationException("Multiple definition of read/write conditions for column " + this.getName());
            }

            readWrite[0] = annotationInstance.value("read") == null ? null : annotationInstance.value("read").asString();
            readWrite[1] = annotationInstance.value("write") == null ? null : annotationInstance.value("write").asString();
            alreadyProcessedForColumn = true;
         }
      }

      return readWrite;
   }

   private String parseCheckAnnotation() {
      String checkCondition = null;
      AnnotationInstance checkAnnotation = JandexHelper.getSingleAnnotation(this.annotations(), HibernateDotNames.CHECK);
      if (checkAnnotation != null) {
         checkCondition = checkAnnotation.value("constraints").toString();
      }

      return checkCondition;
   }

   private IdGenerator checkGeneratedValueAnnotation() {
      IdGenerator generator = null;
      AnnotationInstance generatedValueAnnotation = JandexHelper.getSingleAnnotation(this.annotations(), JPADotNames.GENERATED_VALUE);
      if (generatedValueAnnotation != null) {
         String name = (String)JandexHelper.getValue(generatedValueAnnotation, "generator", String.class);
         if (StringHelper.isNotEmpty(name)) {
            generator = this.getContext().getMetadataImplementor().getIdGenerator(name);
            if (generator == null) {
               throw new MappingException(String.format("Unable to find named generator %s", name), (Origin)null);
            }
         } else {
            GenerationType genType = (GenerationType)JandexHelper.getEnumValue(generatedValueAnnotation, "strategy", GenerationType.class);
            String strategy = EnumConversionHelper.generationTypeToGeneratorStrategyName(genType, this.getContext().getMetadataImplementor().getOptions().useNewIdentifierGenerators());
            generator = new IdGenerator((String)null, strategy, (Map)null);
         }
      }

      return generator;
   }

   public AttributeTypeResolver getHibernateTypeResolver() {
      if (this.resolver == null) {
         this.resolver = this.getDefaultHibernateTypeResolver();
      }

      return this.resolver;
   }

   private AttributeTypeResolver getDefaultHibernateTypeResolver() {
      CompositeAttributeTypeResolver resolver = new CompositeAttributeTypeResolver(new AttributeTypeResolverImpl(this));
      resolver.addHibernateTypeResolver(new TemporalTypeResolver(this));
      resolver.addHibernateTypeResolver(new LobTypeResolver(this));
      resolver.addHibernateTypeResolver(new EnumeratedTypeResolver(this));
      return resolver;
   }
}
