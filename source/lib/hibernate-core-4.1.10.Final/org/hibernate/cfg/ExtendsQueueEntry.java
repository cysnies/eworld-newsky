package org.hibernate.cfg;

import java.util.Set;
import org.hibernate.internal.util.xml.XmlDocument;

public class ExtendsQueueEntry {
   private final String explicitName;
   private final String mappingPackage;
   private final XmlDocument metadataXml;
   private final Set entityNames;

   public ExtendsQueueEntry(String explicitName, String mappingPackage, XmlDocument metadataXml, Set entityNames) {
      this.explicitName = explicitName;
      this.mappingPackage = mappingPackage;
      this.metadataXml = metadataXml;
      this.entityNames = entityNames;
   }

   public String getExplicitName() {
      return this.explicitName;
   }

   public String getMappingPackage() {
      return this.mappingPackage;
   }

   public XmlDocument getMetadataXml() {
      return this.metadataXml;
   }

   public Set getEntityNames() {
      return this.entityNames;
   }
}
