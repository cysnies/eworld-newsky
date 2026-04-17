package org.hibernate.metamodel.source.annotations.global;

import java.util.HashMap;
import java.util.Map;
import org.hibernate.AnnotationException;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.metamodel.binding.TypeDef;
import org.hibernate.metamodel.source.MetadataImplementor;
import org.hibernate.metamodel.source.annotations.AnnotationBindingContext;
import org.hibernate.metamodel.source.annotations.HibernateDotNames;
import org.hibernate.metamodel.source.annotations.JandexHelper;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.logging.Logger;

public class TypeDefBinder {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, TypeDefBinder.class.getName());

   public static void bind(AnnotationBindingContext bindingContext) {
      for(AnnotationInstance typeDef : bindingContext.getIndex().getAnnotations(HibernateDotNames.TYPE_DEF)) {
         bind(bindingContext.getMetadataImplementor(), typeDef);
      }

      for(AnnotationInstance typeDefs : bindingContext.getIndex().getAnnotations(HibernateDotNames.TYPE_DEFS)) {
         AnnotationInstance[] typeDefAnnotations = (AnnotationInstance[])JandexHelper.getValue(typeDefs, "value", AnnotationInstance[].class);

         for(AnnotationInstance typeDef : typeDefAnnotations) {
            bind(bindingContext.getMetadataImplementor(), typeDef);
         }
      }

   }

   private static void bind(MetadataImplementor metadata, AnnotationInstance typeDefAnnotation) {
      String name = (String)JandexHelper.getValue(typeDefAnnotation, "name", String.class);
      String defaultForType = (String)JandexHelper.getValue(typeDefAnnotation, "defaultForType", String.class);
      String typeClass = (String)JandexHelper.getValue(typeDefAnnotation, "typeClass", String.class);
      boolean noName = StringHelper.isEmpty(name);
      boolean noDefaultForType = defaultForType == null || defaultForType.equals(Void.TYPE.getName());
      if (noName && noDefaultForType) {
         throw new AnnotationException("Either name or defaultForType (or both) attribute should be set in TypeDef having typeClass " + typeClass);
      } else {
         Map<String, String> parameterMaps = new HashMap();
         AnnotationInstance[] parameterAnnotations = (AnnotationInstance[])JandexHelper.getValue(typeDefAnnotation, "parameters", AnnotationInstance[].class);

         for(AnnotationInstance parameterAnnotation : parameterAnnotations) {
            parameterMaps.put(JandexHelper.getValue(parameterAnnotation, "name", String.class), JandexHelper.getValue(parameterAnnotation, "value", String.class));
         }

         if (!noName) {
            bind(name, typeClass, parameterMaps, metadata);
         }

         if (!noDefaultForType) {
            bind(defaultForType, typeClass, parameterMaps, metadata);
         }

      }
   }

   private static void bind(String name, String typeClass, Map prms, MetadataImplementor metadata) {
      LOG.debugf("Binding type definition: %s", name);
      metadata.addTypeDefinition(new TypeDef(name, typeClass, prms));
   }

   private TypeDefBinder() {
   }
}
