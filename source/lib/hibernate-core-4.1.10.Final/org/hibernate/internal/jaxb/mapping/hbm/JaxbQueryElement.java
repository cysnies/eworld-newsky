package org.hibernate.internal.jaxb.mapping.hbm;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlMixed;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
   name = "query-element",
   propOrder = {"content"}
)
public class JaxbQueryElement {
   @XmlElementRef(
      name = "query-param",
      namespace = "http://www.hibernate.org/xsd/hibernate-mapping",
      type = JAXBElement.class
   )
   @XmlMixed
   protected List content;
   @XmlAttribute(
      name = "cache-mode"
   )
   protected JaxbCacheModeAttribute cacheMode;
   @XmlAttribute(
      name = "cache-region"
   )
   protected String cacheRegion;
   @XmlAttribute
   protected Boolean cacheable;
   @XmlAttribute
   protected String comment;
   @XmlAttribute(
      name = "fetch-size"
   )
   protected String fetchSize;
   @XmlAttribute(
      name = "flush-mode"
   )
   protected JaxbFlushModeAttribute flushMode;
   @XmlAttribute(
      required = true
   )
   protected String name;
   @XmlAttribute(
      name = "read-only"
   )
   protected Boolean readOnly;
   @XmlAttribute
   protected String timeout;

   public List getContent() {
      if (this.content == null) {
         this.content = new ArrayList();
      }

      return this.content;
   }

   public JaxbCacheModeAttribute getCacheMode() {
      return this.cacheMode;
   }

   public void setCacheMode(JaxbCacheModeAttribute value) {
      this.cacheMode = value;
   }

   public String getCacheRegion() {
      return this.cacheRegion;
   }

   public void setCacheRegion(String value) {
      this.cacheRegion = value;
   }

   public boolean isCacheable() {
      return this.cacheable == null ? false : this.cacheable;
   }

   public void setCacheable(Boolean value) {
      this.cacheable = value;
   }

   public String getComment() {
      return this.comment;
   }

   public void setComment(String value) {
      this.comment = value;
   }

   public String getFetchSize() {
      return this.fetchSize;
   }

   public void setFetchSize(String value) {
      this.fetchSize = value;
   }

   public JaxbFlushModeAttribute getFlushMode() {
      return this.flushMode;
   }

   public void setFlushMode(JaxbFlushModeAttribute value) {
      this.flushMode = value;
   }

   public String getName() {
      return this.name;
   }

   public void setName(String value) {
      this.name = value;
   }

   public Boolean isReadOnly() {
      return this.readOnly;
   }

   public void setReadOnly(Boolean value) {
      this.readOnly = value;
   }

   public String getTimeout() {
      return this.timeout;
   }

   public void setTimeout(String value) {
      this.timeout = value;
   }
}
