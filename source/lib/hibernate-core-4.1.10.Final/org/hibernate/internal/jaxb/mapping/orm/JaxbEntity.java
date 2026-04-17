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
   name = "entity",
   propOrder = {"description", "table", "secondaryTable", "primaryKeyJoinColumn", "idClass", "inheritance", "discriminatorValue", "discriminatorColumn", "sequenceGenerator", "tableGenerator", "namedQuery", "namedNativeQuery", "sqlResultSetMapping", "excludeDefaultListeners", "excludeSuperclassListeners", "entityListeners", "prePersist", "postPersist", "preRemove", "postRemove", "preUpdate", "postUpdate", "postLoad", "attributeOverride", "associationOverride", "attributes"}
)
public class JaxbEntity {
   protected String description;
   protected JaxbTable table;
   @XmlElement(
      name = "secondary-table"
   )
   protected List secondaryTable;
   @XmlElement(
      name = "primary-key-join-column"
   )
   protected List primaryKeyJoinColumn;
   @XmlElement(
      name = "id-class"
   )
   protected JaxbIdClass idClass;
   protected JaxbInheritance inheritance;
   @XmlElement(
      name = "discriminator-value"
   )
   protected String discriminatorValue;
   @XmlElement(
      name = "discriminator-column"
   )
   protected JaxbDiscriminatorColumn discriminatorColumn;
   @XmlElement(
      name = "sequence-generator"
   )
   protected JaxbSequenceGenerator sequenceGenerator;
   @XmlElement(
      name = "table-generator"
   )
   protected JaxbTableGenerator tableGenerator;
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
      name = "exclude-default-listeners"
   )
   protected JaxbEmptyType excludeDefaultListeners;
   @XmlElement(
      name = "exclude-superclass-listeners"
   )
   protected JaxbEmptyType excludeSuperclassListeners;
   @XmlElement(
      name = "entity-listeners"
   )
   protected JaxbEntityListeners entityListeners;
   @XmlElement(
      name = "pre-persist"
   )
   protected JaxbPrePersist prePersist;
   @XmlElement(
      name = "post-persist"
   )
   protected JaxbPostPersist postPersist;
   @XmlElement(
      name = "pre-remove"
   )
   protected JaxbPreRemove preRemove;
   @XmlElement(
      name = "post-remove"
   )
   protected JaxbPostRemove postRemove;
   @XmlElement(
      name = "pre-update"
   )
   protected JaxbPreUpdate preUpdate;
   @XmlElement(
      name = "post-update"
   )
   protected JaxbPostUpdate postUpdate;
   @XmlElement(
      name = "post-load"
   )
   protected JaxbPostLoad postLoad;
   @XmlElement(
      name = "attribute-override"
   )
   protected List attributeOverride;
   @XmlElement(
      name = "association-override"
   )
   protected List associationOverride;
   protected JaxbAttributes attributes;
   @XmlAttribute
   protected String name;
   @XmlAttribute(
      name = "class",
      required = true
   )
   protected String clazz;
   @XmlAttribute
   protected JaxbAccessType access;
   @XmlAttribute
   protected Boolean cacheable;
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

   public JaxbTable getTable() {
      return this.table;
   }

   public void setTable(JaxbTable value) {
      this.table = value;
   }

   public List getSecondaryTable() {
      if (this.secondaryTable == null) {
         this.secondaryTable = new ArrayList();
      }

      return this.secondaryTable;
   }

   public List getPrimaryKeyJoinColumn() {
      if (this.primaryKeyJoinColumn == null) {
         this.primaryKeyJoinColumn = new ArrayList();
      }

      return this.primaryKeyJoinColumn;
   }

   public JaxbIdClass getIdClass() {
      return this.idClass;
   }

   public void setIdClass(JaxbIdClass value) {
      this.idClass = value;
   }

   public JaxbInheritance getInheritance() {
      return this.inheritance;
   }

   public void setInheritance(JaxbInheritance value) {
      this.inheritance = value;
   }

   public String getDiscriminatorValue() {
      return this.discriminatorValue;
   }

   public void setDiscriminatorValue(String value) {
      this.discriminatorValue = value;
   }

   public JaxbDiscriminatorColumn getDiscriminatorColumn() {
      return this.discriminatorColumn;
   }

   public void setDiscriminatorColumn(JaxbDiscriminatorColumn value) {
      this.discriminatorColumn = value;
   }

   public JaxbSequenceGenerator getSequenceGenerator() {
      return this.sequenceGenerator;
   }

   public void setSequenceGenerator(JaxbSequenceGenerator value) {
      this.sequenceGenerator = value;
   }

   public JaxbTableGenerator getTableGenerator() {
      return this.tableGenerator;
   }

   public void setTableGenerator(JaxbTableGenerator value) {
      this.tableGenerator = value;
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

   public JaxbEmptyType getExcludeDefaultListeners() {
      return this.excludeDefaultListeners;
   }

   public void setExcludeDefaultListeners(JaxbEmptyType value) {
      this.excludeDefaultListeners = value;
   }

   public JaxbEmptyType getExcludeSuperclassListeners() {
      return this.excludeSuperclassListeners;
   }

   public void setExcludeSuperclassListeners(JaxbEmptyType value) {
      this.excludeSuperclassListeners = value;
   }

   public JaxbEntityListeners getEntityListeners() {
      return this.entityListeners;
   }

   public void setEntityListeners(JaxbEntityListeners value) {
      this.entityListeners = value;
   }

   public JaxbPrePersist getPrePersist() {
      return this.prePersist;
   }

   public void setPrePersist(JaxbPrePersist value) {
      this.prePersist = value;
   }

   public JaxbPostPersist getPostPersist() {
      return this.postPersist;
   }

   public void setPostPersist(JaxbPostPersist value) {
      this.postPersist = value;
   }

   public JaxbPreRemove getPreRemove() {
      return this.preRemove;
   }

   public void setPreRemove(JaxbPreRemove value) {
      this.preRemove = value;
   }

   public JaxbPostRemove getPostRemove() {
      return this.postRemove;
   }

   public void setPostRemove(JaxbPostRemove value) {
      this.postRemove = value;
   }

   public JaxbPreUpdate getPreUpdate() {
      return this.preUpdate;
   }

   public void setPreUpdate(JaxbPreUpdate value) {
      this.preUpdate = value;
   }

   public JaxbPostUpdate getPostUpdate() {
      return this.postUpdate;
   }

   public void setPostUpdate(JaxbPostUpdate value) {
      this.postUpdate = value;
   }

   public JaxbPostLoad getPostLoad() {
      return this.postLoad;
   }

   public void setPostLoad(JaxbPostLoad value) {
      this.postLoad = value;
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

   public JaxbAttributes getAttributes() {
      return this.attributes;
   }

   public void setAttributes(JaxbAttributes value) {
      this.attributes = value;
   }

   public String getName() {
      return this.name;
   }

   public void setName(String value) {
      this.name = value;
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

   public Boolean isCacheable() {
      return this.cacheable;
   }

   public void setCacheable(Boolean value) {
      this.cacheable = value;
   }

   public Boolean isMetadataComplete() {
      return this.metadataComplete;
   }

   public void setMetadataComplete(Boolean value) {
      this.metadataComplete = value;
   }
}
