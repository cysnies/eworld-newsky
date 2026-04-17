package org.hibernate.internal.jaxb.mapping.orm;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
   name = "",
   propOrder = {"description", "persistenceUnitMetadata", "_package", "schema", "catalog", "access", "sequenceGenerator", "tableGenerator", "namedQuery", "namedNativeQuery", "sqlResultSetMapping", "mappedSuperclass", "entity", "embeddable"}
)
@XmlRootElement(
   name = "entity-mappings"
)
public class JaxbEntityMappings {
   protected String description;
   @XmlElement(
      name = "persistence-unit-metadata"
   )
   protected JaxbPersistenceUnitMetadata persistenceUnitMetadata;
   @XmlElement(
      name = "package"
   )
   protected String _package;
   protected String schema;
   protected String catalog;
   protected JaxbAccessType access;
   @XmlElement(
      name = "sequence-generator"
   )
   protected List sequenceGenerator;
   @XmlElement(
      name = "table-generator"
   )
   protected List tableGenerator;
   @XmlElement(
      name = "named-query"
   )
   protected List namedQuery;
   @XmlElement(
      name = "named-native-query"
   )
   protected List namedNativeQuery;
   @XmlElement(
      name = "sql-result-set-mapping"
   )
   protected List sqlResultSetMapping;
   @XmlElement(
      name = "mapped-superclass"
   )
   protected List mappedSuperclass;
   protected List entity;
   protected List embeddable;
   @XmlAttribute(
      required = true
   )
   @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
   protected String version;

   public String getDescription() {
      return this.description;
   }

   public void setDescription(String value) {
      this.description = value;
   }

   public JaxbPersistenceUnitMetadata getPersistenceUnitMetadata() {
      return this.persistenceUnitMetadata;
   }

   public void setPersistenceUnitMetadata(JaxbPersistenceUnitMetadata value) {
      this.persistenceUnitMetadata = value;
   }

   public String getPackage() {
      return this._package;
   }

   public void setPackage(String value) {
      this._package = value;
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

   public JaxbAccessType getAccess() {
      return this.access;
   }

   public void setAccess(JaxbAccessType value) {
      this.access = value;
   }

   public List getSequenceGenerator() {
      if (this.sequenceGenerator == null) {
         this.sequenceGenerator = new ArrayList();
      }

      return this.sequenceGenerator;
   }

   public List getTableGenerator() {
      if (this.tableGenerator == null) {
         this.tableGenerator = new ArrayList();
      }

      return this.tableGenerator;
   }

   public List getNamedQuery() {
      if (this.namedQuery == null) {
         this.namedQuery = new ArrayList();
      }

      return this.namedQuery;
   }

   public List getNamedNativeQuery() {
      if (this.namedNativeQuery == null) {
         this.namedNativeQuery = new ArrayList();
      }

      return this.namedNativeQuery;
   }

   public List getSqlResultSetMapping() {
      if (this.sqlResultSetMapping == null) {
         this.sqlResultSetMapping = new ArrayList();
      }

      return this.sqlResultSetMapping;
   }

   public List getMappedSuperclass() {
      if (this.mappedSuperclass == null) {
         this.mappedSuperclass = new ArrayList();
      }

      return this.mappedSuperclass;
   }

   public List getEntity() {
      if (this.entity == null) {
         this.entity = new ArrayList();
      }

      return this.entity;
   }

   public List getEmbeddable() {
      if (this.embeddable == null) {
         this.embeddable = new ArrayList();
      }

      return this.embeddable;
   }

   public String getVersion() {
      return this.version == null ? "2.0" : this.version;
   }

   public void setVersion(String value) {
      this.version = value;
   }
}
