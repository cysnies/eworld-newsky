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
   name = "element-collection",
   propOrder = {"orderBy", "orderColumn", "mapKey", "mapKeyClass", "mapKeyTemporal", "mapKeyEnumerated", "mapKeyAttributeOverride", "mapKeyColumn", "mapKeyJoinColumn", "column", "temporal", "enumerated", "lob", "attributeOverride", "associationOverride", "collectionTable"}
)
public class JaxbElementCollection {
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
   protected JaxbColumn column;
   protected JaxbTemporalType temporal;
   protected JaxbEnumType enumerated;
   protected JaxbLob lob;
   @XmlElement(
      name = "attribute-override"
   )
   protected List attributeOverride;
   @XmlElement(
      name = "association-override"
   )
   protected List associationOverride;
   @XmlElement(
      name = "collection-table"
   )
   protected JaxbCollectionTable collectionTable;
   @XmlAttribute(
      required = true
   )
   protected String name;
   @XmlAttribute(
      name = "target-class"
   )
   protected String targetClass;
   @XmlAttribute
   protected JaxbFetchType fetch;
   @XmlAttribute
   protected JaxbAccessType access;

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

   public JaxbColumn getColumn() {
      return this.column;
   }

   public void setColumn(JaxbColumn value) {
      this.column = value;
   }

   public JaxbTemporalType getTemporal() {
      return this.temporal;
   }

   public void setTemporal(JaxbTemporalType value) {
      this.temporal = value;
   }

   public JaxbEnumType getEnumerated() {
      return this.enumerated;
   }

   public void setEnumerated(JaxbEnumType value) {
      this.enumerated = value;
   }

   public JaxbLob getLob() {
      return this.lob;
   }

   public void setLob(JaxbLob value) {
      this.lob = value;
   }

   public List getAttributeOverride() {
      if (this.attributeOverride == null) {
         this.attributeOverride = new ArrayList();
      }

      return this.attributeOverride;
   }

   public List getAssociationOverride() {
      if (this.associationOverride == null) {
         this.associationOverride = new ArrayList();
      }

      return this.associationOverride;
   }

   public JaxbCollectionTable getCollectionTable() {
      return this.collectionTable;
   }

   public void setCollectionTable(JaxbCollectionTable value) {
      this.collectionTable = value;
   }

   public String getName() {
      return this.name;
   }

   public void setName(String value) {
      this.name = value;
   }

   public String getTargetClass() {
      return this.targetClass;
   }

   public void setTargetClass(String value) {
      this.targetClass = value;
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
}
