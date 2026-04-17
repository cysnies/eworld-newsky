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
   name = "one-to-many",
   propOrder = {"orderBy", "orderColumn", "mapKey", "mapKeyClass", "mapKeyTemporal", "mapKeyEnumerated", "mapKeyAttributeOverride", "mapKeyColumn", "mapKeyJoinColumn", "joinTable", "joinColumn", "cascade"}
)
public class JaxbOneToMany {
   @XmlElement(
      name = "order-by"
   )
   protected String orderBy;
   @XmlElement(
      name = "order-column"
   )
   protected JaxbOrderColumn orderColumn;
   @XmlElement(
      name = "map-key"
   )
   protected JaxbMapKey mapKey;
   @XmlElement(
      name = "map-key-class"
   )
   protected JaxbMapKeyClass mapKeyClass;
   @XmlElement(
      name = "map-key-temporal"
   )
   protected JaxbTemporalType mapKeyTemporal;
   @XmlElement(
      name = "map-key-enumerated"
   )
   protected JaxbEnumType mapKeyEnumerated;
   @XmlElement(
      name = "map-key-attribute-override"
   )
   protected List mapKeyAttributeOverride;
   @XmlElement(
      name = "map-key-column"
   )
   protected JaxbMapKeyColumn mapKeyColumn;
   @XmlElement(
      name = "map-key-join-column"
   )
   protected List mapKeyJoinColumn;
   @XmlElement(
      name = "join-table"
   )
   protected JaxbJoinTable joinTable;
   @XmlElement(
      name = "join-column"
   )
   protected List joinColumn;
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
   protected JaxbAccessType access;
   @XmlAttribute(
      name = "mapped-by"
   )
   protected String mappedBy;
   @XmlAttribute(
      name = "orphan-removal"
   )
   protected Boolean orphanRemoval;

   public String getOrderBy() {
      return this.orderBy;
   }

   public void setOrderBy(String value) {
      this.orderBy = value;
   }

   public JaxbOrderColumn getOrderColumn() {
      return this.orderColumn;
   }

   public void setOrderColumn(JaxbOrderColumn value) {
      this.orderColumn = value;
   }

   public JaxbMapKey getMapKey() {
      return this.mapKey;
   }

   public void setMapKey(JaxbMapKey value) {
      this.mapKey = value;
   }

   public JaxbMapKeyClass getMapKeyClass() {
      return this.mapKeyClass;
   }

   public void setMapKeyClass(JaxbMapKeyClass value) {
      this.mapKeyClass = value;
   }

   public JaxbTemporalType getMapKeyTemporal() {
      return this.mapKeyTemporal;
   }

   public void setMapKeyTemporal(JaxbTemporalType value) {
      this.mapKeyTemporal = value;
   }

   public JaxbEnumType getMapKeyEnumerated() {
      return this.mapKeyEnumerated;
   }

   public void setMapKeyEnumerated(JaxbEnumType value) {
      this.mapKeyEnumerated = value;
   }

   public List getMapKeyAttributeOverride() {
      if (this.mapKeyAttributeOverride == null) {
         this.mapKeyAttributeOverride = new ArrayList();
      }

      return this.mapKeyAttributeOverride;
   }

   public JaxbMapKeyColumn getMapKeyColumn() {
      return this.mapKeyColumn;
   }

   public void setMapKeyColumn(JaxbMapKeyColumn value) {
      this.mapKeyColumn = value;
   }

   public List getMapKeyJoinColumn() {
      if (this.mapKeyJoinColumn == null) {
         this.mapKeyJoinColumn = new ArrayList();
      }

      return this.mapKeyJoinColumn;
   }

   public JaxbJoinTable getJoinTable() {
      return this.joinTable;
   }

   public void setJoinTable(JaxbJoinTable value) {
      this.joinTable = value;
   }

   public List getJoinColumn() {
      if (this.joinColumn == null) {
         this.joinColumn = new ArrayList();
      }

      return this.joinColumn;
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
}
