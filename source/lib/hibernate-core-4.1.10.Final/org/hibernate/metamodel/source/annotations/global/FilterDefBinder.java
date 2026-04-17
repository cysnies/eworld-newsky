package org.hibernate.metamodel.source.annotations.global;

import java.util.HashMap;
import java.util.Map;
import org.hibernate.engine.spi.FilterDefinition;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.metamodel.source.MetadataImplementor;
import org.hibernate.metamodel.source.annotations.AnnotationBindingContext;
import org.hibernate.metamodel.source.annotations.HibernateDotNames;
import org.hibernate.metamodel.source.annotations.JandexHelper;
import org.hibernate.type.Type;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.logging.Logger;

public class FilterDefBinder {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, FilterDefBinder.class.getName());

   public static void bind(AnnotationBindingContext bindingContext) {
      for(AnnotationInstance filterDef : bindingContext.getIndex().getAnnotations(HibernateDotNames.FILTER_DEF)) {
         bind(bindingContext.getMetadataImplementor(), filterDef);
      }

      for(AnnotationInstance filterDefs : bindingContext.getIndex().getAnnotations(HibernateDotNames.FILTER_DEFS)) {
         AnnotationInstance[] filterDefAnnotations = (AnnotationInstance[])JandexHelper.getValue(filterDefs, "value", AnnotationInstance[].class);

         for(AnnotationInstance filterDef : filterDefAnnotations) {
            bind(bindingContext.getMetadataImplementor(), filterDef);
         }
      }

   }

   private static void bind(MetadataImplementor metadata, AnnotationInstance filterDef) {
      String name = (String)JandexHelper.getValue(filterDef, "name", String.class);
      Map<String, Type> prms = new HashMap();

      for(AnnotationInstance prm : (AnnotationInstance[])JandexHelper.getValue(filterDef, "parameters", AnnotationInstance[].class)) {
         prms.put(JandexHelper.getValue(prm, "name", String.class), metadata.getTypeResolver().heuristicType((String)JandexHelper.getValue(prm, "type", String.class)));
      }

      metadata.addFilterDefinition(new FilterDefinition(name, (String)JandexHelper.getValue(filterDef, "defaultCondition", String.class), prms));
      LOG.debugf("Binding filter definition: %s", name);
   }

   private FilterDefBinder() {
   }
}
