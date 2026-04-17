package org.hibernate.internal.jaxb.mapping.orm;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
   name = "persistence-unit-defaults",
   propOrder = {"description", "schema", "catalog", "delimitedIdentifiers", "access", "cascadePersist", "entityListeners"}
)
public class JaxbPersistenceUnitDefaults {
   protected String description;
   protected String schema;
   protected String catalog;
   @XmlElement(
      name = "delimited-identifiers"
   )
   protected JaxbEmptyType delimitedIdentifiers;
   protected JaxbAccessType access;
   @XmlElement(
      name = "cascade-persist"
   )
   protected JaxbEmptyType cascadePersist;
   @XmlElement(
      name = "entity-listeners"
   )
   protected JaxbEntityListeners entityListeners;

   public String getDescription() {
      return this.description;
   }

   public void setDescription(String value) {
      this.description = value;
   }

   public String getSchema() {
      return this.schema;
   }

   public void setSchema(String value) {
      this.schema = value;
   }

   public String getCatalog() {
      return this.catalog;
   }

   public void setCatalog(String value) {
      this.catalog = value;
   }

   public JaxbEmptyType getDelimitedIdentifiers() {
      return this.delimitedIdentifiers;
   }

   public void setDelimitedIdentifiers(JaxbEmptyType value) {
      this.delimitedIdentifiers = value;
   }

   public JaxbAccessType getAccess() {
      return this.access;
   }

   public void setAccess(JaxbAccessType value) {
      this.access = value;
   }

   public JaxbEmptyType getCascadePersist() {
      return this.cascadePersist;
   }

   public void setCascadePersist(JaxbEmptyType value) {
      this.cascadePersist = value;
   }

   public JaxbEntityListeners getEntityListeners() {
      return this.entityListeners;
   }

   public void setEntityListeners(JaxbEntityListeners value) {
      this.entityListeners = value;
   }
}
