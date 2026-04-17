package org.hibernate.internal.jaxb.mapping.orm;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
   name = "persistence-unit-metadata",
   propOrder = {"description", "xmlMappingMetadataComplete", "persistenceUnitDefaults"}
)
public class JaxbPersistenceUnitMetadata {
   protected String description;
   @XmlElement(
      name = "xml-mapping-metadata-complete"
   )
   protected JaxbEmptyType xmlMappingMetadataComplete;
   @XmlElement(
      name = "persistence-unit-defaults"
   )
   protected JaxbPersistenceUnitDefaults persistenceUnitDefaults;

   public String getDescription() {
      return this.description;
   }

   public void setDescription(String value) {
      this.description = value;
   }

   public JaxbEmptyType getXmlMappingMetadataComplete() {
      return this.xmlMappingMetadataComplete;
   }

   public void setXmlMappingMetadataComplete(JaxbEmptyType value) {
      this.xmlMappingMetadataComplete = value;
   }

   public JaxbPersistenceUnitDefaults getPersistenceUnitDefaults() {
      return this.persistenceUnitDefaults;
   }

   public void setPersistenceUnitDefaults(JaxbPersistenceUnitDefaults value) {
      this.persistenceUnitDefaults = value;
   }
}
