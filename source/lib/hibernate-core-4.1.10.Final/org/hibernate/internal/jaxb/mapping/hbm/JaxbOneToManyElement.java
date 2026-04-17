package org.hibernate.internal.jaxb.mapping.hbm;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
   name = "one-to-many-element"
)
public class JaxbOneToManyElement {
   @XmlAttribute(
      name = "class"
   )
   protected String clazz;
   @XmlAttribute(
      name = "embed-xml"
   )
   protected Boolean embedXml;
   @XmlAttribute(
      name = "entity-name"
   )
   protected String entityName;
   @XmlAttribute
   protected String node;
   @XmlAttribute(
      name = "not-found"
   )
   protected JaxbNotFoundAttribute notFound;

   public String getClazz() {
      return this.clazz;
   }

   public void setClazz(String value) {
      this.clazz = value;
   }

   public boolean isEmbedXml() {
      return this.embedXml == null ? true : this.embedXml;
   }

   public void setEmbedXml(Boolean value) {
      this.embedXml = value;
   }

   public String getEntityName() {
      return this.entityName;
   }

   public void setEntityName(String value) {
      this.entityName = value;
   }

   public String getNode() {
      return this.node;
   }

   public void setNode(String value) {
      this.node = value;
   }

   public JaxbNotFoundAttribute getNotFound() {
      return this.notFound == null ? JaxbNotFoundAttribute.EXCEPTION : this.notFound;
   }

   public void setNotFound(JaxbNotFoundAttribute value) {
      this.notFound = value;
   }
}
