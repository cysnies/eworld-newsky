package org.hibernate.metamodel.source.annotations.global;

import java.util.HashMap;
import java.util.Map;
import javax.persistence.GenerationType;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.metamodel.binding.IdGenerator;
import org.hibernate.metamodel.source.MetadataImplementor;
import org.hibernate.metamodel.source.annotations.AnnotationBindingContext;
import org.hibernate.metamodel.source.annotations.EnumConversionHelper;
import org.hibernate.metamodel.source.annotations.HibernateDotNames;
import org.hibernate.metamodel.source.annotations.JPADotNames;
import org.hibernate.metamodel.source.annotations.JandexHelper;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.logging.Logger;

public class IdGeneratorBinder {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, IdGeneratorBinder.class.getName());

   private IdGeneratorBinder() {
   }

   public static void bind(AnnotationBindingContext bindingContext) {
      for(AnnotationInstance generator : bindingContext.getIndex().getAnnotations(JPADotNames.SEQUENCE_GENERATOR)) {
         bindSequenceGenerator(bindingContext.getMetadataImplementor(), generator);
      }

      for(AnnotationInstance generator : bindingContext.getIndex().getAnnotations(JPADotNames.TABLE_GENERATOR)) {
         bindTableGenerator(bindingContext.getMetadataImplementor(), generator);
      }

      for(AnnotationInstance generator : bindingContext.getIndex().getAnnotations(HibernateDotNames.GENERIC_GENERATOR)) {
         bindGenericGenerator(bindingContext.getMetadataImplementor(), generator);
      }

      for(AnnotationInstance generators : bindingContext.getIndex().getAnnotations(HibernateDotNames.GENERIC_GENERATORS)) {
         for(AnnotationInstance generator : (AnnotationInstance[])JandexHelper.getValue(generators, "value", AnnotationInstance[].class)) {
            bindGenericGenerator(bindingContext.getMetadataImplementor(), generator);
         }
      }

   }

   private static void addStringParameter(AnnotationInstance annotation, String element, Map parameters, String parameter) {
      String string = (String)JandexHelper.getValue(annotation, element, String.class);
      if (StringHelper.isNotEmpty(string)) {
         parameters.put(parameter, string);
      }

   }

   private static void bindGenericGenerator(MetadataImplementor metadata, AnnotationInstance generator) {
      String name = (String)JandexHelper.getValue(generator, "name", String.class);
      Map<String, String> parameterMap = new HashMap();
      AnnotationInstance[] parameterAnnotations = (AnnotationInstance[])JandexHelper.getValue(generator, "parameters", AnnotationInstance[].class);

      for(AnnotationInstance parameterAnnotation : parameterAnnotations) {
         parameterMap.put(JandexHelper.getValue(parameterAnnotation, "name", String.class), JandexHelper.getValue(parameterAnnotation, "value", String.class));
      }

      metadata.addIdGenerator(new IdGenerator(name, (String)JandexHelper.getValue(generator, "strategy", String.class), parameterMap));
      LOG.tracef("Add generic generator with name: %s", name);
   }

   private static void bindSequenceGenerator(MetadataImplementor metadata, AnnotationInstance generator) {
      String name = (String)JandexHelper.getValue(generator, "name", String.class);
      Map<String, String> parameterMap = new HashMap();
      addStringParameter(generator, "sequenceName", parameterMap, "sequence_name");
      boolean useNewIdentifierGenerators = metadata.getOptions().useNewIdentifierGenerators();
      String strategy = EnumConversionHelper.generationTypeToGeneratorStrategyName(GenerationType.SEQUENCE, useNewIdentifierGenerators);
      if (useNewIdentifierGenerators) {
         addStringParameter(generator, "catalog", parameterMap, "catalog");
         addStringParameter(generator, "schema", parameterMap, "schema");
         parameterMap.put("increment_size", String.valueOf(JandexHelper.getValue(generator, "allocationSize", Integer.class)));
         parameterMap.put("initial_value", String.valueOf(JandexHelper.getValue(generator, "initialValue", Integer.class)));
      } else {
         if ((Integer)JandexHelper.getValue(generator, "initialValue", Integer.class) != 1) {
            LOG.unsupportedInitialValue("hibernate.id.new_generator_mappings");
         }

         parameterMap.put("max_lo", String.valueOf((Integer)JandexHelper.getValue(generator, "allocationSize", Integer.class) - 1));
      }

      metadata.addIdGenerator(new IdGenerator(name, strategy, parameterMap));
      LOG.tracef("Add sequence generator with name: %s", name);
   }

   private static void bindTableGenerator(MetadataImplementor metadata, AnnotationInstance generator) {
      String name = (String)JandexHelper.getValue(generator, "name", String.class);
      Map<String, String> parameterMap = new HashMap();
      addStringParameter(generator, "catalog", parameterMap, "catalog");
      addStringParameter(generator, "schema", parameterMap, "schema");
      boolean useNewIdentifierGenerators = metadata.getOptions().useNewIdentifierGenerators();
      String strategy = EnumConversionHelper.generationTypeToGeneratorStrategyName(GenerationType.TABLE, useNewIdentifierGenerators);
      if (useNewIdentifierGenerators) {
         parameterMap.put("prefer_entity_table_as_segment_value", "true");
         addStringParameter(generator, "table", parameterMap, "table_name");
         addStringParameter(generator, "pkColumnName", parameterMap, "segment_column_name");
         addStringParameter(generator, "pkColumnValue", parameterMap, "segment_value");
         addStringParameter(generator, "valueColumnName", parameterMap, "value_column_name");
         parameterMap.put("increment_size", String.valueOf(JandexHelper.getValue(generator, "allocationSize", String.class)));
         parameterMap.put("initial_value", String.valueOf((String)JandexHelper.getValue(generator, "initialValue", String.class) + 1));
      } else {
         addStringParameter(generator, "table", parameterMap, "table");
         addStringParameter(generator, "pkColumnName", parameterMap, "primary_key_column");
         addStringParameter(generator, "pkColumnValue", parameterMap, "primary_key_value");
         addStringParameter(generator, "valueColumnName", parameterMap, "value_column");
         parameterMap.put("max_lo", String.valueOf((Integer)JandexHelper.getValue(generator, "allocationSize", Integer.class) - 1));
      }

      if (((AnnotationInstance[])JandexHelper.getValue(generator, "uniqueConstraints", AnnotationInstance[].class)).length > 0) {
         LOG.ignoringTableGeneratorConstraints(name);
      }

      metadata.addIdGenerator(new IdGenerator(name, strategy, parameterMap));
      LOG.tracef("Add table generator with name: %s", name);
   }
}
