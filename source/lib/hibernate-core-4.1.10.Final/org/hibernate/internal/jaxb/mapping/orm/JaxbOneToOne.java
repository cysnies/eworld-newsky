package org.hibernate.internal.jaxb.mapping.orm;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
   name = "one-to-one",
   propOrder = {"primaryKeyJoinColumn", "joinColumn", "joinTable", "cascade"}
)
public class JaxbOneToOne {
   @XmlElement(
      name = "primary-key-join-column"
   )
   protected List primaryKeyJoinColumn;
   @XmlElement(
      name = "join-column"
   )
   protected List joinColumn;
   @XmlElement(
      name = "join-table"
   )
   protected JaxbJoinTable joinTable;
   protected JaxbCascadeType cascade;
   @XmlAttribute(
      required = true
   )
   protected String name;
   @XmlAttribute(
      name = "target-entity"
   )
   protected String targetEntity;
   @XmlAttribute
   protected JaxbFetchType fetch;
   @XmlAttribute
   protected Boolean optional;
   @XmlAttribute
   protected JaxbAccessType access;
   @XmlAttribute(
      name = "mapped-by"
   )
   protected String mappedBy;
   @XmlAttribute(
      name = "orphan-removal"
   )
   protected Boolean orphanRemoval;
   @XmlAttribute(
      name = "maps-id"
   )
   protected String mapsId;
   @XmlAttribute
   protected Boolean id;

   public List getPrimaryKeyJoinColumn() {
      if (this.primaryKeyJoinColumn == null) {
         this.primaryKeyJoinColumn = new ArrayList();
      }

      return this.primaryKeyJoinColumn;
   }

   public List getJoinColumn() {
      if (this.joinColumn == null) {
         this.joinColumn = new ArrayList();
      }

      return this.joinColumn;
   }

   public JaxbJoinTable getJoinTable() {
      return this.joinTable;
   }

   public void setJoinTable(JaxbJoinTable value) {
      this.joinTable = value;
   }

   public JaxbCascadeType getCascade() {
      return this.cascade;
   }

   public void setCascade(JaxbCascadeType value) {
      this.cascade = value;
   }

   public String getName() {
      return this.name;
   }

   public void setName(String value) {
      this.name = value;
   }

   public String getTargetEntity() {
      return this.targetEntity;
   }

   public void setTargetEntity(String value) {
      this.targetEntity = value;
   }

   public JaxbFetchType getFetch() {
      return this.fetch;
   }

   public void setFetch(JaxbFetchType value) {
      this.fetch = value;
   }

   public Boolean isOptional() {
      return this.optional;
   }

   public void setOptional(Boolean value) {
      this.optional = value;
   }

   public JaxbAccessType getAccess() {
      return this.access;
   }

   public void setAccess(JaxbAccessType value) {
      this.access = value;
   }

   public String getMappedBy() {
      return this.mappedBy;
   }

   public void setMappedBy(String value) {
      this.mappedBy = value;
   }

   public Boolean isOrphanRemoval() {
      return this.orphanRemoval;
   }

   public void setOrphanRemoval(Boolean value) {
      this.orphanRemoval = value;
   }

   public String getMapsId() {
      return this.mapsId;
   }

   public void setMapsId(String value) {
      this.mapsId = value;
   }

   public Boolean isId() {
      return this.id;
   }

   public void setId(Boolean value) {
      this.id = value;
   }
}
