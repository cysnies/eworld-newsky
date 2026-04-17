package org.hibernate.cfg;

import java.util.Map;
import org.hibernate.MappingException;
import org.hibernate.annotations.FetchProfile;
import org.hibernate.mapping.MetadataSource;
import org.hibernate.mapping.PersistentClass;

public class VerifyFetchProfileReferenceSecondPass implements SecondPass {
   private String fetchProfileName;
   private FetchProfile.FetchOverride fetch;
   private Mappings mappings;

   public VerifyFetchProfileReferenceSecondPass(String fetchProfileName, FetchProfile.FetchOverride fetch, Mappings mappings) {
      this.fetchProfileName = fetchProfileName;
      this.fetch = fetch;
      this.mappings = mappings;
   }

   public void doSecondPass(Map persistentClasses) throws MappingException {
      org.hibernate.mapping.FetchProfile profile = this.mappings.findOrCreateFetchProfile(this.fetchProfileName, MetadataSource.ANNOTATIONS);
      if (MetadataSource.ANNOTATIONS == profile.getSource()) {
         PersistentClass clazz = this.mappings.getClass(this.fetch.entity().getName());
         clazz.getProperty(this.fetch.association());
         profile.addFetch(this.fetch.entity().getName(), this.fetch.association(), this.fetch.mode().toString().toLowerCase());
      }
   }
}
