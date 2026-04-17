package org.hibernate.internal.jaxb.mapping.hbm;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
   name = "column-element",
   propOrder = {"comment"}
)
public class JaxbColumnElement {
   protected String comment;
   @XmlAttribute
   protected String check;
   @XmlAttribute(
      name = "default"
   )
   protected String _default;
   @XmlAttribute
   protected String index;
   @XmlAttribute
   protected String length;
   @XmlAttribute(
      required = true
   )
   protected String name;
   @XmlAttribute(
      name = "not-null"
   )
   protected Boolean notNull;
   @XmlAttribute
   protected String precision;
   @XmlAttribute
   protected String read;
   @XmlAttribute
   protected String scale;
   @XmlAttribute(
      name = "sql-type"
   )
   protected String sqlType;
   @XmlAttribute
   protected Boolean unique;
   @XmlAttribute(
      name = "unique-key"
   )
   protected String uniqueKey;
   @XmlAttribute
   protected String write;

   public String getComment() {
      return this.comment;
   }

   public void setComment(String value) {
      this.comment = value;
   }

   public String getCheck() {
      return this.check;
   }

   public void setCheck(String value) {
      this.check = value;
   }

   public String getDefault() {
      return this._default;
   }

   public void setDefault(String value) {
      this._default = value;
   }

   public String getIndex() {
      return this.index;
   }

   public void setIndex(String value) {
      this.index = value;
   }

   public String getLength() {
      return this.length;
   }

   public void setLength(String value) {
      this.length = value;
   }

   public String getName() {
      return this.name;
   }

   public void setName(String value) {
      this.name = value;
   }

   public Boolean isNotNull() {
      return this.notNull;
   }

   public void setNotNull(Boolean value) {
      this.notNull = value;
   }

   public String getPrecision() {
      return this.precision;
   }

   public void setPrecision(String value) {
      this.precision = value;
   }

   public String getRead() {
      return this.read;
   }

   public void setRead(String value) {
      this.read = value;
   }

   public String getScale() {
      return this.scale;
   }

   public void setScale(String value) {
      this.scale = value;
   }

   public String getSqlType() {
      return this.sqlType;
   }

   public void setSqlType(String value) {
      this.sqlType = value;
   }

   public Boolean isUnique() {
      return this.unique;
   }

   public void setUnique(Boolean value) {
      this.unique = value;
   }

   public String getUniqueKey() {
      return this.uniqueKey;
   }

   public void setUniqueKey(String value) {
      this.uniqueKey = value;
   }

   public String getWrite() {
      return this.write;
   }

   public void setWrite(String value) {
      this.write = value;
   }
}
