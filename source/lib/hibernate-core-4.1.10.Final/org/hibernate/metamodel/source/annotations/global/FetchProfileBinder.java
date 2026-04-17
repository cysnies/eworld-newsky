package org.hibernate.metamodel.source.annotations.global;

import java.util.HashSet;
import java.util.Set;
import org.hibernate.MappingException;
import org.hibernate.annotations.FetchMode;
import org.hibernate.metamodel.binding.FetchProfile;
import org.hibernate.metamodel.source.MetadataImplementor;
import org.hibernate.metamodel.source.annotations.AnnotationBindingContext;
import org.hibernate.metamodel.source.annotations.HibernateDotNames;
import org.hibernate.metamodel.source.annotations.JandexHelper;
import org.jboss.jandex.AnnotationInstance;

public class FetchProfileBinder {
   private FetchProfileBinder() {
   }

   public static void bind(AnnotationBindingContext bindingContext) {
      for(AnnotationInstance fetchProfile : bindingContext.getIndex().getAnnotations(HibernateDotNames.FETCH_PROFILE)) {
         bind(bindingContext.getMetadataImplementor(), fetchProfile);
      }

      for(AnnotationInstance fetchProfiles : bindingContext.getIndex().getAnnotations(HibernateDotNames.FETCH_PROFILES)) {
         AnnotationInstance[] fetchProfileAnnotations = (AnnotationInstance[])JandexHelper.getValue(fetchProfiles, "value", AnnotationInstance[].class);

         for(AnnotationInstance fetchProfile : fetchProfileAnnotations) {
            bind(bindingContext.getMetadataImplementor(), fetchProfile);
         }
      }

   }

   private static void bind(MetadataImplementor metadata, AnnotationInstance fetchProfile) {
      String name = (String)JandexHelper.getValue(fetchProfile, "name", String.class);
      Set<FetchProfile.Fetch> fetches = new HashSet();
      AnnotationInstance[] overrideAnnotations = (AnnotationInstance[])JandexHelper.getValue(fetchProfile, "fetchOverrides", AnnotationInstance[].class);

      for(AnnotationInstance override : overrideAnnotations) {
         FetchMode fetchMode = (FetchMode)JandexHelper.getEnumValue(override, "mode", FetchMode.class);
         if (!fetchMode.equals(FetchMode.JOIN)) {
            throw new MappingException("Only FetchMode.JOIN is currently supported");
         }

         String entityName = (String)JandexHelper.getValue(override, "entity", String.class);
         String associationName = (String)JandexHelper.getValue(override, "association", String.class);
         fetches.add(new FetchProfile.Fetch(entityName, associationName, fetchMode.toString().toLowerCase()));
      }

      metadata.addFetchProfile(new FetchProfile(name, fetches));
   }
}
