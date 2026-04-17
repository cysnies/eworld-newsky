package org.hibernate.internal.jaxb.mapping.hbm;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
   name = "any-element",
   propOrder = {"meta", "metaValue", "column"}
)
public class JaxbAnyElement {
   protected List meta;
   @XmlElement(
      name = "meta-value"
   )
   protected List metaValue;
   @XmlElement(
      required = true
   )
   protected JaxbColumnElement column;
   @XmlAttribute
   protected String access;
   @XmlAttribute
   protected String cascade;
   @XmlAttribute(
      name = "id-type",
      required = true
   )
   protected String idType;
   @XmlAttribute
   protected String index;
   @XmlAttribute
   protected Boolean insert;
   @XmlAttribute
   protected Boolean lazy;
   @XmlAttribute(
      name = "meta-type"
   )
   protected String metaType;
   @XmlAttribute(
      required = true
   )
   protected String name;
   @XmlAttribute
   protected String node;
   @XmlAttribute(
      name = "optimistic-lock"
   )
   protected Boolean optimisticLock;
   @XmlAttribute
   protected Boolean update;

   public List getMeta() {
      if (this.meta == null) {
         this.meta = new ArrayList();
      }

      return this.meta;
   }

   public List getMetaValue() {
      if (this.metaValue == null) {
         this.metaValue = new ArrayList();
      }

      return this.metaValue;
   }

   public JaxbColumnElement getColumn() {
      return this.column;
   }

   public void setColumn(JaxbColumnElement value) {
      this.column = value;
   }

   public String getAccess() {
      return this.access;
   }

   public void setAccess(String value) {
      this.access = value;
   }

   public String getCascade() {
      return this.cascade;
   }

   public void setCascade(String value) {
      this.cascade = value;
   }

   public String getIdType() {
      return this.idType;
   }

   public void setIdType(String value) {
      this.idType = value;
   }

   public String getIndex() {
      return this.index;
   }

   public void setIndex(String value) {
      this.index = value;
   }

   public boolean isInsert() {
      return this.insert == null ? true : this.insert;
   }

   public void setInsert(Boolean value) {
      this.insert = value;
   }

   public boolean isLazy() {
      return this.lazy == null ? false : this.lazy;
   }

   public void setLazy(Boolean value) {
      this.lazy = value;
   }

   public String getMetaType() {
      return this.metaType;
   }

   public void setMetaType(String value) {
      this.metaType = value;
   }

   public String getName() {
      return this.name;
   }

   public void setName(String value) {
      this.name = value;
   }

   public String getNode() {
      return this.node;
   }

   public void setNode(String value) {
      this.node = value;
   }

   public boolean isOptimisticLock() {
      return this.optimisticLock == null ? true : this.optimisticLock;
   }

   public void setOptimisticLock(Boolean value) {
      this.optimisticLock = value;
   }

   public boolean isUpdate() {
      return this.update == null ? true : this.update;
   }

   public void setUpdate(Boolean value) {
      this.update = value;
   }
}
