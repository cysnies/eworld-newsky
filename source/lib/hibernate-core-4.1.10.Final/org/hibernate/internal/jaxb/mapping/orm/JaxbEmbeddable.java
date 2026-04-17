package org.hibernate.internal.jaxb.mapping.orm;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
   name = "embeddable",
   propOrder = {"description", "attributes"}
)
public class JaxbEmbeddable {
   protected String description;
   protected JaxbEmbeddableAttributes attributes;
   @XmlAttribute(
      name = "class",
      required = true
   )
   protected String clazz;
   @XmlAttribute
   protected JaxbAccessType access;
   @XmlAttribute(
      name = "metadata-complete"
   )
   protected Boolean metadataComplete;

   public String getDescription() {
      return this.description;
   }

   public void setDescription(String value) {
      this.description = value;
   }

   public JaxbEmbeddableAttributes getAttributes() {
      return this.attributes;
   }

   public void setAttributes(JaxbEmbeddableAttributes value) {
      this.attributes = value;
   }

   public String getClazz() {
      return this.clazz;
   }

   public void setClazz(String value) {
      this.clazz = value;
   }

   public JaxbAccessType getAccess() {
      return this.access;
   }

   public void setAccess(JaxbAccessType value) {
      this.access = value;
   }

   public Boolean isMetadataComplete() {
      return this.metadataComplete;
   }

   public void setMetadataComplete(Boolean value) {
      this.metadataComplete = value;
   }
}
