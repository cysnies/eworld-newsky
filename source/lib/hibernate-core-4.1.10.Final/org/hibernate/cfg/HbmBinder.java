package org.hibernate.cfg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.hibernate.CacheMode;
import org.hibernate.EntityMode;
import org.hibernate.FetchMode;
import org.hibernate.FlushMode;
import org.hibernate.MappingException;
import org.hibernate.engine.spi.ExecuteUpdateResultCheckStyle;
import org.hibernate.engine.spi.FilterDefinition;
import org.hibernate.engine.spi.NamedQueryDefinition;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.ReflectHelper;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.internal.util.collections.JoinedIterator;
import org.hibernate.internal.util.xml.XmlDocument;
import org.hibernate.mapping.Any;
import org.hibernate.mapping.Array;
import org.hibernate.mapping.AuxiliaryDatabaseObject;
import org.hibernate.mapping.Backref;
import org.hibernate.mapping.Bag;
import org.hibernate.mapping.Collection;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Component;
import org.hibernate.mapping.DependantValue;
import org.hibernate.mapping.FetchProfile;
import org.hibernate.mapping.Fetchable;
import org.hibernate.mapping.Filterable;
import org.hibernate.mapping.Formula;
import org.hibernate.mapping.IdentifierBag;
import org.hibernate.mapping.IdentifierCollection;
import org.hibernate.mapping.IndexBackref;
import org.hibernate.mapping.Join;
import org.hibernate.mapping.JoinedSubclass;
import org.hibernate.mapping.KeyValue;
import org.hibernate.mapping.ManyToOne;
import org.hibernate.mapping.MetaAttribute;
import org.hibernate.mapping.MetadataSource;
import org.hibernate.mapping.OneToMany;
import org.hibernate.mapping.OneToOne;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.PrimitiveArray;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.PropertyGeneration;
import org.hibernate.mapping.RootClass;
import org.hibernate.mapping.Selectable;
import org.hibernate.mapping.SimpleAuxiliaryDatabaseObject;
import org.hibernate.mapping.SimpleValue;
import org.hibernate.mapping.SingleTableSubclass;
import org.hibernate.mapping.Subclass;
import org.hibernate.mapping.Table;
import org.hibernate.mapping.ToOne;
import org.hibernate.mapping.TypeDef;
import org.hibernate.mapping.UnionSubclass;
import org.hibernate.mapping.UniqueKey;
import org.hibernate.mapping.Value;
import org.hibernate.type.BasicType;
import org.hibernate.type.DiscriminatorType;
import org.hibernate.type.ForeignKeyDirection;
import org.hibernate.type.Type;
import org.jboss.logging.Logger;

public final class HbmBinder {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, HbmBinder.class.getName());

   private HbmBinder() {
   }

   public static void bindRoot(XmlDocument metadataXml, Mappings mappings, Map inheritedMetas, Set entityNames) throws MappingException {
      Document doc = metadataXml.getDocumentTree();
      Element hibernateMappingElement = doc.getRootElement();
      List<String> names = getExtendsNeeded(metadataXml, mappings);
      if (names.isEmpty()) {
         inheritedMetas = getMetas(hibernateMappingElement, inheritedMetas, true);
         extractRootAttributes(hibernateMappingElement, mappings);
         Iterator rootChildren = hibernateMappingElement.elementIterator();

         while(rootChildren.hasNext()) {
            Element element = (Element)rootChildren.next();
            String elementName = element.getName();
            if ("filter-def".equals(elementName)) {
               parseFilterDef(element, mappings);
            } else if ("fetch-profile".equals(elementName)) {
               parseFetchProfile(element, mappings, (String)null);
            } else if ("identifier-generator".equals(elementName)) {
               parseIdentifierGeneratorRegistration(element, mappings);
            } else if ("typedef".equals(elementName)) {
               bindTypeDef(element, mappings);
            } else if ("class".equals(elementName)) {
               RootClass rootclass = new RootClass();
               bindRootClass(element, rootclass, mappings, inheritedMetas);
               mappings.addClass(rootclass);
            } else if ("subclass".equals(elementName)) {
               PersistentClass superModel = getSuperclass(mappings, element);
               handleSubclass(superModel, mappings, element, inheritedMetas);
            } else if ("joined-subclass".equals(elementName)) {
               PersistentClass superModel = getSuperclass(mappings, element);
               handleJoinedSubclass(superModel, mappings, element, inheritedMetas);
            } else if ("union-subclass".equals(elementName)) {
               PersistentClass superModel = getSuperclass(mappings, element);
               handleUnionSubclass(superModel, mappings, element, inheritedMetas);
            } else if ("query".equals(elementName)) {
               bindNamedQuery(element, (String)null, mappings);
            } else if ("sql-query".equals(elementName)) {
               bindNamedSQLQuery(element, (String)null, mappings);
            } else if ("resultset".equals(elementName)) {
               bindResultSetMappingDefinition(element, (String)null, mappings);
            } else if ("import".equals(elementName)) {
               bindImport(element, mappings);
            } else if ("database-object".equals(elementName)) {
               bindAuxiliaryDatabaseObject(element, mappings);
            }
         }

      } else {
         Attribute packageAttribute = hibernateMappingElement.attribute("package");
         String packageName = packageAttribute == null ? null : packageAttribute.getValue();

         for(String name : names) {
            mappings.addToExtendsQueue(new ExtendsQueueEntry(name, packageName, metadataXml, entityNames));
         }

      }
   }

   private static void parseIdentifierGeneratorRegistration(Element element, Mappings mappings) {
      String strategy = element.attributeValue("name");
      if (StringHelper.isEmpty(strategy)) {
         throw new MappingException("'name' attribute expected for identifier-generator elements");
      } else {
         String generatorClassName = element.attributeValue("class");
         if (StringHelper.isEmpty(generatorClassName)) {
            throw new MappingException("'class' attribute expected for identifier-generator [identifier-generator@name=" + strategy + "]");
         } else {
            try {
               Class generatorClass = ReflectHelper.classForName(generatorClassName);
               mappings.getIdentifierGeneratorFactory().register(strategy, generatorClass);
            } catch (ClassNotFoundException var5) {
               throw new MappingException("Unable to locate identifier-generator class [name=" + strategy + ", class=" + generatorClassName + "]");
            }
         }
      }
   }

   private static void bindImport(Element importNode, Mappings mappings) {
      String className = getClassName(importNode.attribute("class"), mappings);
      Attribute renameNode = importNode.attribute("rename");
      String rename = renameNode == null ? StringHelper.unqualify(className) : renameNode.getValue();
      LOG.debugf("Import: %s -> %s", rename, className);
      mappings.addImport(className, rename);
   }

   private static void bindTypeDef(Element typedefNode, Mappings mappings) {
      String typeClass = typedefNode.attributeValue("class");
      String typeName = typedefNode.attributeValue("name");
      Iterator paramIter = typedefNode.elementIterator("param");
      Properties parameters = new Properties();

      while(paramIter.hasNext()) {
         Element param = (Element)paramIter.next();
         parameters.setProperty(param.attributeValue("name"), param.getTextTrim());
      }

      mappings.addTypeDef(typeName, typeClass, parameters);
   }

   private static void bindAuxiliaryDatabaseObject(Element auxDbObjectNode, Mappings mappings) {
      AuxiliaryDatabaseObject auxDbObject = null;
      Element definitionNode = auxDbObjectNode.element("definition");
      if (definitionNode != null) {
         try {
            auxDbObject = (AuxiliaryDatabaseObject)ReflectHelper.classForName(definitionNode.attributeValue("class")).newInstance();
         } catch (ClassNotFoundException var6) {
            throw new MappingException("could not locate custom database object class [" + definitionNode.attributeValue("class") + "]");
         } catch (Throwable var7) {
            throw new MappingException("could not instantiate custom database object class [" + definitionNode.attributeValue("class") + "]");
         }
      } else {
         auxDbObject = new SimpleAuxiliaryDatabaseObject(auxDbObjectNode.elementTextTrim("create"), auxDbObjectNode.elementTextTrim("drop"));
      }

      Iterator dialectScopings = auxDbObjectNode.elementIterator("dialect-scope");

      while(dialectScopings.hasNext()) {
         Element dialectScoping = (Element)dialectScopings.next();
         auxDbObject.addDialectScope(dialectScoping.attributeValue("name"));
      }

      mappings.addAuxiliaryDatabaseObject(auxDbObject);
   }

   private static void extractRootAttributes(Element hmNode, Mappings mappings) {
      Attribute schemaNode = hmNode.attribute("schema");
      mappings.setSchemaName(schemaNode == null ? null : schemaNode.getValue());
      Attribute catalogNode = hmNode.attribute("catalog");
      mappings.setCatalogName(catalogNode == null ? null : catalogNode.getValue());
      Attribute dcNode = hmNode.attribute("default-cascade");
      mappings.setDefaultCascade(dcNode == null ? "none" : dcNode.getValue());
      Attribute daNode = hmNode.attribute("default-access");
      mappings.setDefaultAccess(daNode == null ? "property" : daNode.getValue());
      Attribute dlNode = hmNode.attribute("default-lazy");
      mappings.setDefaultLazy(dlNode == null || dlNode.getValue().equals("true"));
      Attribute aiNode = hmNode.attribute("auto-import");
      mappings.setAutoImport(aiNode == null || "true".equals(aiNode.getValue()));
      Attribute packNode = hmNode.attribute("package");
      if (packNode != null) {
         mappings.setDefaultPackage(packNode.getValue());
      }

   }

   public static void bindRootClass(Element node, RootClass rootClass, Mappings mappings, Map inheritedMetas) throws MappingException {
      bindClass(node, rootClass, mappings, inheritedMetas);
      inheritedMetas = getMetas(node, inheritedMetas, true);
      bindRootPersistentClassCommonValues(node, inheritedMetas, mappings, rootClass);
   }

   private static void bindRootPersistentClassCommonValues(Element node, Map inheritedMetas, Mappings mappings, RootClass entity) throws MappingException {
      Attribute schemaNode = node.attribute("schema");
      String schema = schemaNode == null ? mappings.getSchemaName() : schemaNode.getValue();
      Attribute catalogNode = node.attribute("catalog");
      String catalog = catalogNode == null ? mappings.getCatalogName() : catalogNode.getValue();
      Table table = mappings.addTable(schema, catalog, getClassTableName(entity, node, schema, catalog, (Table)null, mappings), getSubselect(node), entity.isAbstract() != null && entity.isAbstract());
      entity.setTable(table);
      bindComment(table, node);
      if (LOG.isDebugEnabled()) {
         LOG.debugf("Mapping class: %s -> %s", entity.getEntityName(), entity.getTable().getName());
      }

      Attribute mutableNode = node.attribute("mutable");
      entity.setMutable(mutableNode == null || mutableNode.getValue().equals("true"));
      Attribute whereNode = node.attribute("where");
      if (whereNode != null) {
         entity.setWhere(whereNode.getValue());
      }

      Attribute chNode = node.attribute("check");
      if (chNode != null) {
         table.addCheckConstraint(chNode.getValue());
      }

      Attribute polyNode = node.attribute("polymorphism");
      entity.setExplicitPolymorphism(polyNode != null && polyNode.getValue().equals("explicit"));
      Attribute rowidNode = node.attribute("rowid");
      if (rowidNode != null) {
         table.setRowId(rowidNode.getValue());
      }

      Iterator subnodes = node.elementIterator();

      while(subnodes.hasNext()) {
         Element subnode = (Element)subnodes.next();
         String name = subnode.getName();
         if ("id".equals(name)) {
            bindSimpleId(subnode, entity, mappings, inheritedMetas);
         } else if ("composite-id".equals(name)) {
            bindCompositeId(subnode, entity, mappings, inheritedMetas);
         } else if (!"version".equals(name) && !"timestamp".equals(name)) {
            if ("discriminator".equals(name)) {
               bindDiscriminatorProperty(table, entity, subnode, mappings);
            } else if ("cache".equals(name)) {
               entity.setCacheConcurrencyStrategy(subnode.attributeValue("usage"));
               entity.setCacheRegionName(subnode.attributeValue("region"));
               entity.setLazyPropertiesCacheable(!"non-lazy".equals(subnode.attributeValue("include")));
            }
         } else {
            bindVersioningProperty(table, subnode, mappings, name, entity, inheritedMetas);
         }
      }

      entity.createPrimaryKey();
      createClassProperties(node, entity, mappings, inheritedMetas);
   }

   private static void bindSimpleId(Element idNode, RootClass entity, Mappings mappings, Map inheritedMetas) throws MappingException {
      String propertyName = idNode.attributeValue("name");
      SimpleValue id = new SimpleValue(mappings, entity.getTable());
      entity.setIdentifier(id);
      if (propertyName == null) {
         bindSimpleValue(idNode, id, false, "id", mappings);
      } else {
         bindSimpleValue(idNode, id, false, propertyName, mappings);
      }

      if (propertyName != null && entity.hasPojoRepresentation()) {
         id.setTypeUsingReflection(entity.getClassName(), propertyName);
      } else if (!id.isTypeSpecified()) {
         throw new MappingException("must specify an identifier type: " + entity.getEntityName());
      }

      if (propertyName != null) {
         Property prop = new Property();
         prop.setValue(id);
         bindProperty(idNode, prop, mappings, inheritedMetas);
         entity.setIdentifierProperty(prop);
         entity.setDeclaredIdentifierProperty(prop);
      }

      makeIdentifier(idNode, id, mappings);
   }

   private static void bindCompositeId(Element idNode, RootClass entity, Mappings mappings, Map inheritedMetas) throws MappingException {
      String propertyName = idNode.attributeValue("name");
      Component id = new Component(mappings, entity);
      entity.setIdentifier(id);
      bindCompositeId(idNode, id, entity, propertyName, mappings, inheritedMetas);
      if (propertyName == null) {
         entity.setEmbeddedIdentifier(id.isEmbedded());
         if (id.isEmbedded()) {
            id.setDynamic(!entity.hasPojoRepresentation());
         }
      } else {
         Property prop = new Property();
         prop.setValue(id);
         bindProperty(idNode, prop, mappings, inheritedMetas);
         entity.setIdentifierProperty(prop);
         entity.setDeclaredIdentifierProperty(prop);
      }

      makeIdentifier(idNode, id, mappings);
   }

   private static void bindVersioningProperty(Table table, Element subnode, Mappings mappings, String name, RootClass entity, Map inheritedMetas) {
      String propertyName = subnode.attributeValue("name");
      SimpleValue val = new SimpleValue(mappings, table);
      bindSimpleValue(subnode, val, false, propertyName, mappings);
      if (!val.isTypeSpecified()) {
         if ("version".equals(name)) {
            val.setTypeName("integer");
         } else if ("db".equals(subnode.attributeValue("source"))) {
            val.setTypeName("dbtimestamp");
         } else {
            val.setTypeName("timestamp");
         }
      }

      Property prop = new Property();
      prop.setValue(val);
      bindProperty(subnode, prop, mappings, inheritedMetas);
      if (prop.getGeneration() == PropertyGeneration.INSERT) {
         throw new MappingException("'generated' attribute cannot be 'insert' for versioning property");
      } else {
         makeVersion(subnode, val);
         entity.setVersion(prop);
         entity.addProperty(prop);
      }
   }

   private static void bindDiscriminatorProperty(Table table, RootClass entity, Element subnode, Mappings mappings) {
      SimpleValue discrim = new SimpleValue(mappings, table);
      entity.setDiscriminator(discrim);
      bindSimpleValue(subnode, discrim, false, "class", mappings);
      if (!discrim.isTypeSpecified()) {
         discrim.setTypeName("string");
      }

      entity.setPolymorphic(true);
      String explicitForceValue = subnode.attributeValue("force");
      boolean forceDiscriminatorInSelects = explicitForceValue == null ? mappings.forceDiscriminatorInSelectsByDefault() : "true".equals(explicitForceValue);
      entity.setForceDiscriminator(forceDiscriminatorInSelects);
      if ("false".equals(subnode.attributeValue("insert"))) {
         entity.setDiscriminatorInsertable(false);
      }

   }

   public static void bindClass(Element node, PersistentClass persistentClass, Mappings mappings, Map inheritedMetas) throws MappingException {
      Attribute lazyNode = node.attribute("lazy");
      boolean lazy = lazyNode == null ? mappings.isDefaultLazy() : "true".equals(lazyNode.getValue());
      persistentClass.setLazy(lazy);
      String entityName = node.attributeValue("entity-name");
      if (entityName == null) {
         entityName = getClassName(node.attribute("name"), mappings);
      }

      if (entityName == null) {
         throw new MappingException("Unable to determine entity name");
      } else {
         persistentClass.setEntityName(entityName);
         persistentClass.setJpaEntityName(StringHelper.unqualify(entityName));
         bindPojoRepresentation(node, persistentClass, mappings, inheritedMetas);
         bindDom4jRepresentation(node, persistentClass, mappings, inheritedMetas);
         bindMapRepresentation(node, persistentClass, mappings, inheritedMetas);
         Iterator itr = node.elementIterator("fetch-profile");

         while(itr.hasNext()) {
            Element profileElement = (Element)itr.next();
            parseFetchProfile(profileElement, mappings, entityName);
         }

         bindPersistentClassCommonValues(node, persistentClass, mappings, inheritedMetas);
      }
   }

   private static void bindPojoRepresentation(Element node, PersistentClass entity, Mappings mappings, Map metaTags) {
      String className = getClassName(node.attribute("name"), mappings);
      String proxyName = getClassName(node.attribute("proxy"), mappings);
      entity.setClassName(className);
      if (proxyName != null) {
         entity.setProxyInterfaceName(proxyName);
         entity.setLazy(true);
      } else if (entity.isLazy()) {
         entity.setProxyInterfaceName(className);
      }

      Element tuplizer = locateTuplizerDefinition(node, EntityMode.POJO);
      if (tuplizer != null) {
         entity.addTuplizer(EntityMode.POJO, tuplizer.attributeValue("class"));
      }

   }

   private static void bindDom4jRepresentation(Element node, PersistentClass entity, Mappings mappings, Map inheritedMetas) {
      String nodeName = node.attributeValue("node");
      if (nodeName == null) {
         nodeName = StringHelper.unqualify(entity.getEntityName());
      }

      entity.setNodeName(nodeName);
   }

   private static void bindMapRepresentation(Element node, PersistentClass entity, Mappings mappings, Map inheritedMetas) {
      Element tuplizer = locateTuplizerDefinition(node, EntityMode.MAP);
      if (tuplizer != null) {
         entity.addTuplizer(EntityMode.MAP, tuplizer.attributeValue("class"));
      }

   }

   private static Element locateTuplizerDefinition(Element container, EntityMode entityMode) {
      for(Element tuplizerElem : container.elements("tuplizer")) {
         if (entityMode.toString().equals(tuplizerElem.attributeValue("entity-mode"))) {
            return tuplizerElem;
         }
      }

      return null;
   }

   private static void bindPersistentClassCommonValues(Element node, PersistentClass entity, Mappings mappings, Map inheritedMetas) throws MappingException {
      Attribute discriminatorNode = node.attribute("discriminator-value");
      entity.setDiscriminatorValue(discriminatorNode == null ? entity.getEntityName() : discriminatorNode.getValue());
      Attribute dynamicNode = node.attribute("dynamic-update");
      entity.setDynamicUpdate(dynamicNode != null && "true".equals(dynamicNode.getValue()));
      Attribute insertNode = node.attribute("dynamic-insert");
      entity.setDynamicInsert(insertNode != null && "true".equals(insertNode.getValue()));
      mappings.addImport(entity.getEntityName(), entity.getEntityName());
      if (mappings.isAutoImport() && entity.getEntityName().indexOf(46) > 0) {
         mappings.addImport(entity.getEntityName(), StringHelper.unqualify(entity.getEntityName()));
      }

      Attribute batchNode = node.attribute("batch-size");
      if (batchNode != null) {
         entity.setBatchSize(Integer.parseInt(batchNode.getValue()));
      }

      Attribute sbuNode = node.attribute("select-before-update");
      if (sbuNode != null) {
         entity.setSelectBeforeUpdate("true".equals(sbuNode.getValue()));
      }

      Attribute olNode = node.attribute("optimistic-lock");
      entity.setOptimisticLockMode(getOptimisticLockMode(olNode));
      entity.setMetaAttributes(getMetas(node, inheritedMetas));
      Attribute persisterNode = node.attribute("persister");
      if (persisterNode != null) {
         try {
            entity.setEntityPersisterClass(ReflectHelper.classForName(persisterNode.getValue()));
         } catch (ClassNotFoundException var14) {
            throw new MappingException("Could not find persister class: " + persisterNode.getValue());
         }
      }

      handleCustomSQL(node, entity);
      Iterator tables = node.elementIterator("synchronize");

      while(tables.hasNext()) {
         entity.addSynchronizedTable(((Element)tables.next()).attributeValue("table"));
      }

      Attribute abstractNode = node.attribute("abstract");
      Boolean isAbstract = abstractNode == null ? null : ("true".equals(abstractNode.getValue()) ? Boolean.TRUE : ("false".equals(abstractNode.getValue()) ? Boolean.FALSE : null));
      entity.setAbstract(isAbstract);
   }

   private static void handleCustomSQL(Element node, PersistentClass model) throws MappingException {
      Element element = node.element("sql-insert");
      if (element != null) {
         boolean callable = isCallable(element);
         model.setCustomSQLInsert(element.getTextTrim(), callable, getResultCheckStyle(element, callable));
      }

      element = node.element("sql-delete");
      if (element != null) {
         boolean callable = isCallable(element);
         model.setCustomSQLDelete(element.getTextTrim(), callable, getResultCheckStyle(element, callable));
      }

      element = node.element("sql-update");
      if (element != null) {
         boolean callable = isCallable(element);
         model.setCustomSQLUpdate(element.getTextTrim(), callable, getResultCheckStyle(element, callable));
      }

      element = node.element("loader");
      if (element != null) {
         model.setLoaderName(element.attributeValue("query-ref"));
      }

   }

   private static void handleCustomSQL(Element node, Join model) throws MappingException {
      Element element = node.element("sql-insert");
      if (element != null) {
         boolean callable = isCallable(element);
         model.setCustomSQLInsert(element.getTextTrim(), callable, getResultCheckStyle(element, callable));
      }

      element = node.element("sql-delete");
      if (element != null) {
         boolean callable = isCallable(element);
         model.setCustomSQLDelete(element.getTextTrim(), callable, getResultCheckStyle(element, callable));
      }

      element = node.element("sql-update");
      if (element != null) {
         boolean callable = isCallable(element);
         model.setCustomSQLUpdate(element.getTextTrim(), callable, getResultCheckStyle(element, callable));
      }

   }

   private static void handleCustomSQL(Element node, Collection model) throws MappingException {
      Element element = node.element("sql-insert");
      if (element != null) {
         boolean callable = isCallable(element, true);
         model.setCustomSQLInsert(element.getTextTrim(), callable, getResultCheckStyle(element, callable));
      }

      element = node.element("sql-delete");
      if (element != null) {
         boolean callable = isCallable(element, true);
         model.setCustomSQLDelete(element.getTextTrim(), callable, getResultCheckStyle(element, callable));
      }

      element = node.element("sql-update");
      if (element != null) {
         boolean callable = isCallable(element, true);
         model.setCustomSQLUpdate(element.getTextTrim(), callable, getResultCheckStyle(element, callable));
      }

      element = node.element("sql-delete-all");
      if (element != null) {
         boolean callable = isCallable(element, true);
         model.setCustomSQLDeleteAll(element.getTextTrim(), callable, getResultCheckStyle(element, callable));
      }

   }

   private static boolean isCallable(Element e) throws MappingException {
      return isCallable(e, true);
   }

   private static boolean isCallable(Element element, boolean supportsCallable) throws MappingException {
      Attribute attrib = element.attribute("callable");
      if (attrib != null && "true".equals(attrib.getValue())) {
         if (!supportsCallable) {
            throw new MappingException("callable attribute not supported yet!");
         } else {
            return true;
         }
      } else {
         return false;
      }
   }

   private static ExecuteUpdateResultCheckStyle getResultCheckStyle(Element element, boolean callable) throws MappingException {
      Attribute attr = element.attribute("check");
      return attr == null ? ExecuteUpdateResultCheckStyle.COUNT : ExecuteUpdateResultCheckStyle.fromExternalName(attr.getValue());
   }

   public static void bindUnionSubclass(Element node, UnionSubclass unionSubclass, Mappings mappings, Map inheritedMetas) throws MappingException {
      bindClass(node, unionSubclass, mappings, inheritedMetas);
      inheritedMetas = getMetas(node, inheritedMetas, true);
      Attribute schemaNode = node.attribute("schema");
      String schema = schemaNode == null ? mappings.getSchemaName() : schemaNode.getValue();
      Attribute catalogNode = node.attribute("catalog");
      String catalog = catalogNode == null ? mappings.getCatalogName() : catalogNode.getValue();
      Table denormalizedSuperTable = unionSubclass.getSuperclass().getTable();
      Table mytable = mappings.addDenormalizedTable(schema, catalog, getClassTableName(unionSubclass, node, schema, catalog, denormalizedSuperTable, mappings), unionSubclass.isAbstract() != null && unionSubclass.isAbstract(), getSubselect(node), denormalizedSuperTable);
      unionSubclass.setTable(mytable);
      if (LOG.isDebugEnabled()) {
         LOG.debugf("Mapping union-subclass: %s -> %s", unionSubclass.getEntityName(), unionSubclass.getTable().getName());
      }

      createClassProperties(node, unionSubclass, mappings, inheritedMetas);
   }

   public static void bindSubclass(Element node, Subclass subclass, Mappings mappings, Map inheritedMetas) throws MappingException {
      bindClass(node, subclass, mappings, inheritedMetas);
      inheritedMetas = getMetas(node, inheritedMetas, true);
      if (LOG.isDebugEnabled()) {
         LOG.debugf("Mapping subclass: %s -> %s", subclass.getEntityName(), subclass.getTable().getName());
      }

      createClassProperties(node, subclass, mappings, inheritedMetas);
   }

   private static String getClassTableName(PersistentClass model, Element node, String schema, String catalog, Table denormalizedSuperTable, Mappings mappings) {
      Attribute tableNameNode = node.attribute("table");
      String logicalTableName;
      String physicalTableName;
      if (tableNameNode == null) {
         logicalTableName = StringHelper.unqualify(model.getEntityName());
         physicalTableName = mappings.getNamingStrategy().classToTableName(model.getEntityName());
      } else {
         logicalTableName = tableNameNode.getValue();
         physicalTableName = mappings.getNamingStrategy().tableName(logicalTableName);
      }

      mappings.addTableBinding(schema, catalog, logicalTableName, physicalTableName, denormalizedSuperTable);
      return physicalTableName;
   }

   public static void bindJoinedSubclass(Element node, JoinedSubclass joinedSubclass, Mappings mappings, Map inheritedMetas) throws MappingException {
      bindClass(node, joinedSubclass, mappings, inheritedMetas);
      inheritedMetas = getMetas(node, inheritedMetas, true);
      Attribute schemaNode = node.attribute("schema");
      String schema = schemaNode == null ? mappings.getSchemaName() : schemaNode.getValue();
      Attribute catalogNode = node.attribute("catalog");
      String catalog = catalogNode == null ? mappings.getCatalogName() : catalogNode.getValue();
      Table mytable = mappings.addTable(schema, catalog, getClassTableName(joinedSubclass, node, schema, catalog, (Table)null, mappings), getSubselect(node), false);
      joinedSubclass.setTable(mytable);
      bindComment(mytable, node);
      if (LOG.isDebugEnabled()) {
         LOG.debugf("Mapping joined-subclass: %s -> %s", joinedSubclass.getEntityName(), joinedSubclass.getTable().getName());
      }

      Element keyNode = node.element("key");
      SimpleValue key = new DependantValue(mappings, mytable, joinedSubclass.getIdentifier());
      joinedSubclass.setKey(key);
      key.setCascadeDeleteEnabled("cascade".equals(keyNode.attributeValue("on-delete")));
      bindSimpleValue(keyNode, key, false, joinedSubclass.getEntityName(), mappings);
      joinedSubclass.createPrimaryKey();
      joinedSubclass.createForeignKey();
      Attribute chNode = node.attribute("check");
      if (chNode != null) {
         mytable.addCheckConstraint(chNode.getValue());
      }

      createClassProperties(node, joinedSubclass, mappings, inheritedMetas);
   }

   private static void bindJoin(Element node, Join join, Mappings mappings, Map inheritedMetas) throws MappingException {
      PersistentClass persistentClass = join.getPersistentClass();
      String path = persistentClass.getEntityName();
      Attribute schemaNode = node.attribute("schema");
      String schema = schemaNode == null ? mappings.getSchemaName() : schemaNode.getValue();
      Attribute catalogNode = node.attribute("catalog");
      String catalog = catalogNode == null ? mappings.getCatalogName() : catalogNode.getValue();
      Table primaryTable = persistentClass.getTable();
      Table table = mappings.addTable(schema, catalog, getClassTableName(persistentClass, node, schema, catalog, primaryTable, mappings), getSubselect(node), false);
      join.setTable(table);
      bindComment(table, node);
      Attribute fetchNode = node.attribute("fetch");
      if (fetchNode != null) {
         join.setSequentialSelect("select".equals(fetchNode.getValue()));
      }

      Attribute invNode = node.attribute("inverse");
      if (invNode != null) {
         join.setInverse("true".equals(invNode.getValue()));
      }

      Attribute nullNode = node.attribute("optional");
      if (nullNode != null) {
         join.setOptional("true".equals(nullNode.getValue()));
      }

      if (LOG.isDebugEnabled()) {
         LOG.debugf("Mapping class join: %s -> %s", persistentClass.getEntityName(), join.getTable().getName());
      }

      Element keyNode = node.element("key");
      SimpleValue key = new DependantValue(mappings, table, persistentClass.getIdentifier());
      join.setKey(key);
      key.setCascadeDeleteEnabled("cascade".equals(keyNode.attributeValue("on-delete")));
      bindSimpleValue(keyNode, key, false, persistentClass.getEntityName(), mappings);
      join.createPrimaryKey();
      join.createForeignKey();
      Iterator iter = node.elementIterator();

      while(iter.hasNext()) {
         Element subnode = (Element)iter.next();
         String name = subnode.getName();
         String propertyName = subnode.attributeValue("name");
         Value value = null;
         if ("many-to-one".equals(name)) {
            value = new ManyToOne(mappings, table);
            bindManyToOne(subnode, (ManyToOne)value, propertyName, true, mappings);
         } else if ("any".equals(name)) {
            value = new Any(mappings, table);
            bindAny(subnode, (Any)value, true, mappings);
         } else if ("property".equals(name)) {
            value = new SimpleValue(mappings, table);
            bindSimpleValue(subnode, (SimpleValue)value, true, propertyName, mappings);
         } else if ("component".equals(name) || "dynamic-component".equals(name)) {
            String subpath = StringHelper.qualify(path, propertyName);
            value = new Component(mappings, join);
            bindComponent(subnode, (Component)value, join.getPersistentClass().getClassName(), propertyName, subpath, true, false, mappings, inheritedMetas, false);
         }

         if (value != null) {
            Property prop = createProperty(value, propertyName, persistentClass.getEntityName(), subnode, mappings, inheritedMetas);
            prop.setOptional(join.isOptional());
            join.addProperty(prop);
         }
      }

      handleCustomSQL(node, join);
   }

   public static void bindColumns(Element node, SimpleValue simpleValue, boolean isNullable, boolean autoColumn, String propertyPath, Mappings mappings) throws MappingException {
      Table table = simpleValue.getTable();
      Attribute columnAttribute = node.attribute("column");
      if (columnAttribute == null) {
         Iterator itr = node.elementIterator();
         int count = 0;

         while(itr.hasNext()) {
            Element columnElement = (Element)itr.next();
            if (columnElement.getName().equals("column")) {
               Column column = new Column();
               column.setValue(simpleValue);
               column.setTypeIndex(count++);
               bindColumn(columnElement, column, isNullable);
               String columnName = columnElement.attributeValue("name");
               String logicalColumnName = mappings.getNamingStrategy().logicalColumnName(columnName, propertyPath);
               column.setName(mappings.getNamingStrategy().columnName(columnName));
               if (table != null) {
                  table.addColumn(column);
                  mappings.addColumnBinding(logicalColumnName, column, table);
               }

               simpleValue.addColumn(column);
               bindIndex(columnElement.attribute("index"), table, column, mappings);
               bindIndex(node.attribute("index"), table, column, mappings);
               bindUniqueKey(columnElement.attribute("unique-key"), table, column, mappings);
               bindUniqueKey(node.attribute("unique-key"), table, column, mappings);
            } else if (columnElement.getName().equals("formula")) {
               Formula formula = new Formula();
               formula.setFormula(columnElement.getText());
               simpleValue.addFormula(formula);
            }
         }

         Attribute uniqueAttribute = node.attribute("unique");
         if (uniqueAttribute != null && "true".equals(uniqueAttribute.getValue()) && ManyToOne.class.isInstance(simpleValue)) {
            ((ManyToOne)simpleValue).markAsLogicalOneToOne();
         }
      } else {
         if (node.elementIterator("column").hasNext()) {
            throw new MappingException("column attribute may not be used together with <column> subelement");
         }

         if (node.elementIterator("formula").hasNext()) {
            throw new MappingException("column attribute may not be used together with <formula> subelement");
         }

         Column column = new Column();
         column.setValue(simpleValue);
         bindColumn(node, column, isNullable);
         if (column.isUnique() && ManyToOne.class.isInstance(simpleValue)) {
            ((ManyToOne)simpleValue).markAsLogicalOneToOne();
         }

         String columnName = columnAttribute.getValue();
         String logicalColumnName = mappings.getNamingStrategy().logicalColumnName(columnName, propertyPath);
         column.setName(mappings.getNamingStrategy().columnName(columnName));
         if (table != null) {
            table.addColumn(column);
            mappings.addColumnBinding(logicalColumnName, column, table);
         }

         simpleValue.addColumn(column);
         bindIndex(node.attribute("index"), table, column, mappings);
         bindUniqueKey(node.attribute("unique-key"), table, column, mappings);
      }

      if (autoColumn && simpleValue.getColumnSpan() == 0) {
         Column column = new Column();
         column.setValue(simpleValue);
         bindColumn(node, column, isNullable);
         column.setName(mappings.getNamingStrategy().propertyToColumnName(propertyPath));
         String logicalName = mappings.getNamingStrategy().logicalColumnName((String)null, propertyPath);
         mappings.addColumnBinding(logicalName, column, table);
         simpleValue.getTable().addColumn(column);
         simpleValue.addColumn(column);
         bindIndex(node.attribute("index"), table, column, mappings);
         bindUniqueKey(node.attribute("unique-key"), table, column, mappings);
      }

   }

   private static void bindIndex(Attribute indexAttribute, Table table, Column column, Mappings mappings) {
      if (indexAttribute != null && table != null) {
         StringTokenizer tokens = new StringTokenizer(indexAttribute.getValue(), ", ");

         while(tokens.hasMoreTokens()) {
            table.getOrCreateIndex(tokens.nextToken()).addColumn(column);
         }
      }

   }

   private static void bindUniqueKey(Attribute uniqueKeyAttribute, Table table, Column column, Mappings mappings) {
      if (uniqueKeyAttribute != null && table != null) {
         StringTokenizer tokens = new StringTokenizer(uniqueKeyAttribute.getValue(), ", ");

         while(tokens.hasMoreTokens()) {
            table.getOrCreateUniqueKey(tokens.nextToken()).addColumn(column);
         }
      }

   }

   public static void bindSimpleValue(Element node, SimpleValue simpleValue, boolean isNullable, String path, Mappings mappings) throws MappingException {
      bindSimpleValueType(node, simpleValue, mappings);
      bindColumnsOrFormula(node, simpleValue, path, isNullable, mappings);
      Attribute fkNode = node.attribute("foreign-key");
      if (fkNode != null) {
         simpleValue.setForeignKeyName(fkNode.getValue());
      }

   }

   private static void bindSimpleValueType(Element node, SimpleValue simpleValue, Mappings mappings) throws MappingException {
      String typeName = null;
      Properties parameters = new Properties();
      Attribute typeNode = node.attribute("type");
      if (typeNode == null) {
         typeNode = node.attribute("id-type");
      } else {
         typeName = typeNode.getValue();
      }

      Element typeChild = node.element("type");
      if (typeName == null && typeChild != null) {
         typeName = typeChild.attribute("name").getValue();
         Iterator typeParameters = typeChild.elementIterator("param");

         while(typeParameters.hasNext()) {
            Element paramElement = (Element)typeParameters.next();
            parameters.setProperty(paramElement.attributeValue("name"), paramElement.getTextTrim());
         }
      }

      resolveAndBindTypeDef(simpleValue, mappings, typeName, parameters);
   }

   private static void resolveAndBindTypeDef(SimpleValue simpleValue, Mappings mappings, String typeName, Properties parameters) {
      TypeDef typeDef = mappings.getTypeDef(typeName);
      if (typeDef != null) {
         typeName = typeDef.getTypeClass();
         Properties allParameters = new Properties();
         allParameters.putAll(typeDef.getParameters());
         allParameters.putAll(parameters);
         parameters = allParameters;
      } else if (typeName != null && !mappings.isInSecondPass()) {
         BasicType basicType = mappings.getTypeResolver().basic(typeName);
         if (basicType == null) {
            SecondPass resolveUserTypeMappingSecondPass = new ResolveUserTypeMappingSecondPass(simpleValue, typeName, mappings, parameters);
            mappings.addSecondPass(resolveUserTypeMappingSecondPass);
         }
      }

      if (!parameters.isEmpty()) {
         simpleValue.setTypeParameters(parameters);
      }

      if (typeName != null) {
         simpleValue.setTypeName(typeName);
      }

   }

   public static void bindProperty(Element node, Property property, Mappings mappings, Map inheritedMetas) throws MappingException {
      String propName = node.attributeValue("name");
      property.setName(propName);
      String nodeName = node.attributeValue("node");
      if (nodeName == null) {
         nodeName = propName;
      }

      property.setNodeName(nodeName);
      Attribute accessNode = node.attribute("access");
      if (accessNode != null) {
         property.setPropertyAccessorName(accessNode.getValue());
      } else if (node.getName().equals("properties")) {
         property.setPropertyAccessorName("embedded");
      } else {
         property.setPropertyAccessorName(mappings.getDefaultAccess());
      }

      Attribute cascadeNode = node.attribute("cascade");
      property.setCascade(cascadeNode == null ? mappings.getDefaultCascade() : cascadeNode.getValue());
      Attribute updateNode = node.attribute("update");
      property.setUpdateable(updateNode == null || "true".equals(updateNode.getValue()));
      Attribute insertNode = node.attribute("insert");
      property.setInsertable(insertNode == null || "true".equals(insertNode.getValue()));
      Attribute lockNode = node.attribute("optimistic-lock");
      property.setOptimisticLocked(lockNode == null || "true".equals(lockNode.getValue()));
      Attribute generatedNode = node.attribute("generated");
      String generationName = generatedNode == null ? null : generatedNode.getValue();
      PropertyGeneration generation = PropertyGeneration.parse(generationName);
      property.setGeneration(generation);
      if (generation == PropertyGeneration.ALWAYS || generation == PropertyGeneration.INSERT) {
         if (property.isInsertable()) {
            if (insertNode != null) {
               throw new MappingException("cannot specify both insert=\"true\" and generated=\"" + generation.getName() + "\" for property: " + propName);
            }

            property.setInsertable(false);
         }

         if (property.isUpdateable() && generation == PropertyGeneration.ALWAYS) {
            if (updateNode != null) {
               throw new MappingException("cannot specify both update=\"true\" and generated=\"" + generation.getName() + "\" for property: " + propName);
            }

            property.setUpdateable(false);
         }
      }

      boolean isLazyable = "property".equals(node.getName()) || "component".equals(node.getName()) || "many-to-one".equals(node.getName()) || "one-to-one".equals(node.getName()) || "any".equals(node.getName());
      if (isLazyable) {
         Attribute lazyNode = node.attribute("lazy");
         property.setLazy(lazyNode != null && "true".equals(lazyNode.getValue()));
      }

      if (LOG.isDebugEnabled()) {
         String msg = "Mapped property: " + property.getName();
         String columns = columns(property.getValue());
         if (columns.length() > 0) {
            msg = msg + " -> " + columns;
         }

         LOG.debug(msg);
      }

      property.setMetaAttributes(getMetas(node, inheritedMetas));
   }

   private static String columns(Value val) {
      StringBuilder columns = new StringBuilder();
      Iterator iter = val.getColumnIterator();

      while(iter.hasNext()) {
         columns.append(((Selectable)iter.next()).getText());
         if (iter.hasNext()) {
            columns.append(", ");
         }
      }

      return columns.toString();
   }

   public static void bindCollection(Element node, Collection collection, String className, String path, Mappings mappings, Map inheritedMetas) throws MappingException {
      collection.setRole(path);
      Attribute inverseNode = node.attribute("inverse");
      if (inverseNode != null) {
         collection.setInverse("true".equals(inverseNode.getValue()));
      }

      Attribute mutableNode = node.attribute("mutable");
      if (mutableNode != null) {
         collection.setMutable(!"false".equals(mutableNode.getValue()));
      }

      Attribute olNode = node.attribute("optimistic-lock");
      collection.setOptimisticLocked(olNode == null || "true".equals(olNode.getValue()));
      Attribute orderNode = node.attribute("order-by");
      if (orderNode != null) {
         collection.setOrderBy(orderNode.getValue());
      }

      Attribute whereNode = node.attribute("where");
      if (whereNode != null) {
         collection.setWhere(whereNode.getValue());
      }

      Attribute batchNode = node.attribute("batch-size");
      if (batchNode != null) {
         collection.setBatchSize(Integer.parseInt(batchNode.getValue()));
      }

      String nodeName = node.attributeValue("node");
      if (nodeName == null) {
         nodeName = node.attributeValue("name");
      }

      collection.setNodeName(nodeName);
      String embed = node.attributeValue("embed-xml");
      if (!StringHelper.isEmpty(embed) && !"true".equals(embed)) {
         LOG.embedXmlAttributesNoLongerSupported();
      }

      collection.setEmbedded(embed == null || "true".equals(embed));
      Attribute persisterNode = node.attribute("persister");
      if (persisterNode != null) {
         try {
            collection.setCollectionPersisterClass(ReflectHelper.classForName(persisterNode.getValue()));
         } catch (ClassNotFoundException var24) {
            throw new MappingException("Could not find collection persister class: " + persisterNode.getValue());
         }
      }

      Attribute typeNode = node.attribute("collection-type");
      if (typeNode != null) {
         String typeName = typeNode.getValue();
         TypeDef typeDef = mappings.getTypeDef(typeName);
         if (typeDef != null) {
            collection.setTypeName(typeDef.getTypeClass());
            collection.setTypeParameters(typeDef.getParameters());
         } else {
            collection.setTypeName(typeName);
         }
      }

      initOuterJoinFetchSetting(node, collection);
      if ("subselect".equals(node.attributeValue("fetch"))) {
         collection.setSubselectLoadable(true);
         collection.getOwner().setSubselectLoadableCollections(true);
      }

      initLaziness(node, collection, mappings, "true", mappings.isDefaultLazy());
      if ("extra".equals(node.attributeValue("lazy"))) {
         collection.setLazy(true);
         collection.setExtraLazy(true);
      }

      Element oneToManyNode = node.element("one-to-many");
      if (oneToManyNode != null) {
         OneToMany oneToMany = new OneToMany(mappings, collection.getOwner());
         collection.setElement(oneToMany);
         bindOneToMany(oneToManyNode, oneToMany, mappings);
      } else {
         Attribute tableNode = node.attribute("table");
         String tableName;
         if (tableNode != null) {
            tableName = mappings.getNamingStrategy().tableName(tableNode.getValue());
         } else {
            Table ownerTable = collection.getOwner().getTable();
            String logicalOwnerTableName = ownerTable.getName();
            tableName = mappings.getNamingStrategy().collectionTableName(collection.getOwner().getEntityName(), logicalOwnerTableName, (String)null, (String)null, path);
            if (ownerTable.isQuoted()) {
               tableName = StringHelper.quote(tableName);
            }
         }

         Attribute schemaNode = node.attribute("schema");
         String schema = schemaNode == null ? mappings.getSchemaName() : schemaNode.getValue();
         Attribute catalogNode = node.attribute("catalog");
         String catalog = catalogNode == null ? mappings.getCatalogName() : catalogNode.getValue();
         Table table = mappings.addTable(schema, catalog, tableName, getSubselect(node), false);
         collection.setCollectionTable(table);
         bindComment(table, node);
         if (LOG.isDebugEnabled()) {
            LOG.debugf("Mapping collection: %s -> %s", collection.getRole(), collection.getCollectionTable().getName());
         }
      }

      Attribute sortedAtt = node.attribute("sort");
      if (sortedAtt != null && !sortedAtt.getValue().equals("unsorted")) {
         collection.setSorted(true);
         String comparatorClassName = sortedAtt.getValue();
         if (!comparatorClassName.equals("natural")) {
            collection.setComparatorClassName(comparatorClassName);
         }
      } else {
         collection.setSorted(false);
      }

      Attribute cascadeAtt = node.attribute("cascade");
      if (cascadeAtt != null && cascadeAtt.getValue().indexOf("delete-orphan") >= 0) {
         collection.setOrphanDelete(true);
      }

      handleCustomSQL(node, collection);
      if (collection instanceof org.hibernate.mapping.List) {
         mappings.addSecondPass(new ListSecondPass(node, mappings, (org.hibernate.mapping.List)collection, inheritedMetas));
      } else if (collection instanceof org.hibernate.mapping.Map) {
         mappings.addSecondPass(new MapSecondPass(node, mappings, (org.hibernate.mapping.Map)collection, inheritedMetas));
      } else if (collection instanceof IdentifierCollection) {
         mappings.addSecondPass(new IdentifierCollectionSecondPass(node, mappings, collection, inheritedMetas));
      } else {
         mappings.addSecondPass(new CollectionSecondPass(node, mappings, collection, inheritedMetas));
      }

      Iterator iter = node.elementIterator("filter");

      while(iter.hasNext()) {
         Element filter = (Element)iter.next();
         parseFilter(filter, collection, mappings);
      }

      Iterator tables = node.elementIterator("synchronize");

      while(tables.hasNext()) {
         collection.getSynchronizedTables().add(((Element)tables.next()).attributeValue("table"));
      }

      Element element = node.element("loader");
      if (element != null) {
         collection.setLoaderName(element.attributeValue("query-ref"));
      }

      collection.setReferencedPropertyName(node.element("key").attributeValue("property-ref"));
   }

   private static void initLaziness(Element node, Fetchable fetchable, Mappings mappings, String proxyVal, boolean defaultLazy) {
      Attribute lazyNode = node.attribute("lazy");
      boolean isLazyTrue = lazyNode == null ? defaultLazy && fetchable.isLazy() : lazyNode.getValue().equals(proxyVal);
      fetchable.setLazy(isLazyTrue);
   }

   private static void initLaziness(Element node, ToOne fetchable, Mappings mappings, boolean defaultLazy) {
      if ("no-proxy".equals(node.attributeValue("lazy"))) {
         fetchable.setUnwrapProxy(true);
         fetchable.setLazy(true);
      } else {
         initLaziness(node, fetchable, mappings, "proxy", defaultLazy);
      }

   }

   private static void bindColumnsOrFormula(Element node, SimpleValue simpleValue, String path, boolean isNullable, Mappings mappings) {
      Attribute formulaNode = node.attribute("formula");
      if (formulaNode != null) {
         Formula f = new Formula();
         f.setFormula(formulaNode.getText());
         simpleValue.addFormula(f);
      } else {
         bindColumns(node, simpleValue, isNullable, true, path, mappings);
      }

   }

   private static void bindComment(Table table, Element node) {
      Element comment = node.element("comment");
      if (comment != null) {
         table.setComment(comment.getTextTrim());
      }

   }

   public static void bindManyToOne(Element node, ManyToOne manyToOne, String path, boolean isNullable, Mappings mappings) throws MappingException {
      bindColumnsOrFormula(node, manyToOne, path, isNullable, mappings);
      initOuterJoinFetchSetting(node, manyToOne);
      initLaziness(node, manyToOne, mappings, true);
      Attribute ukName = node.attribute("property-ref");
      if (ukName != null) {
         manyToOne.setReferencedPropertyName(ukName.getValue());
      }

      manyToOne.setReferencedEntityName(getEntityName(node, mappings));
      String embed = node.attributeValue("embed-xml");
      if (!StringHelper.isEmpty(embed) && !"true".equals(embed)) {
         LOG.embedXmlAttributesNoLongerSupported();
      }

      manyToOne.setEmbedded(embed == null || "true".equals(embed));
      String notFound = node.attributeValue("not-found");
      manyToOne.setIgnoreNotFound("ignore".equals(notFound));
      if (ukName != null && !manyToOne.isIgnoreNotFound() && !node.getName().equals("many-to-many")) {
         mappings.addSecondPass(new ManyToOneSecondPass(manyToOne));
      }

      Attribute fkNode = node.attribute("foreign-key");
      if (fkNode != null) {
         manyToOne.setForeignKeyName(fkNode.getValue());
      }

      String cascade = node.attributeValue("cascade");
      if (cascade != null && cascade.indexOf("delete-orphan") >= 0 && !manyToOne.isLogicalOneToOne()) {
         throw new MappingException("many-to-one attribute [" + path + "] does not support orphan delete as it is not unique");
      }
   }

   public static void bindAny(Element node, Any any, boolean isNullable, Mappings mappings) throws MappingException {
      any.setIdentifierType(getTypeFromXML(node));
      Attribute metaAttribute = node.attribute("meta-type");
      if (metaAttribute != null) {
         any.setMetaType(metaAttribute.getValue());
         Iterator iter = node.elementIterator("meta-value");
         if (iter.hasNext()) {
            HashMap values = new HashMap();
            Type metaType = mappings.getTypeResolver().heuristicType(any.getMetaType());

            while(iter.hasNext()) {
               Element metaValue = (Element)iter.next();

               try {
                  Object value = ((DiscriminatorType)metaType).stringToObject(metaValue.attributeValue("value"));
                  String entityName = getClassName(metaValue.attribute("class"), mappings);
                  values.put(value, entityName);
               } catch (ClassCastException var11) {
                  throw new MappingException("meta-type was not a DiscriminatorType: " + metaType.getName());
               } catch (Exception e) {
                  throw new MappingException("could not interpret meta-value", e);
               }
            }

            any.setMetaValues(values);
         }
      }

      bindColumns(node, any, isNullable, false, (String)null, mappings);
   }

   public static void bindOneToOne(Element node, OneToOne oneToOne, String path, boolean isNullable, Mappings mappings) throws MappingException {
      bindColumns(node, oneToOne, isNullable, false, (String)null, mappings);
      Attribute constrNode = node.attribute("constrained");
      boolean constrained = constrNode != null && constrNode.getValue().equals("true");
      oneToOne.setConstrained(constrained);
      oneToOne.setForeignKeyType(constrained ? ForeignKeyDirection.FOREIGN_KEY_FROM_PARENT : ForeignKeyDirection.FOREIGN_KEY_TO_PARENT);
      initOuterJoinFetchSetting(node, oneToOne);
      initLaziness(node, oneToOne, mappings, true);
      String embed = node.attributeValue("embed-xml");
      if (!StringHelper.isEmpty(embed) && !"true".equals(embed)) {
         LOG.embedXmlAttributesNoLongerSupported();
      }

      oneToOne.setEmbedded("true".equals(embed));
      Attribute fkNode = node.attribute("foreign-key");
      if (fkNode != null) {
         oneToOne.setForeignKeyName(fkNode.getValue());
      }

      Attribute ukName = node.attribute("property-ref");
      if (ukName != null) {
         oneToOne.setReferencedPropertyName(ukName.getValue());
      }

      oneToOne.setPropertyName(node.attributeValue("name"));
      oneToOne.setReferencedEntityName(getEntityName(node, mappings));
      String cascade = node.attributeValue("cascade");
      if (cascade != null && cascade.indexOf("delete-orphan") >= 0 && oneToOne.isConstrained()) {
         throw new MappingException("one-to-one attribute [" + path + "] does not support orphan delete as it is constrained");
      }
   }

   public static void bindOneToMany(Element node, OneToMany oneToMany, Mappings mappings) throws MappingException {
      oneToMany.setReferencedEntityName(getEntityName(node, mappings));
      String embed = node.attributeValue("embed-xml");
      if (!StringHelper.isEmpty(embed) && !"true".equals(embed)) {
         LOG.embedXmlAttributesNoLongerSupported();
      }

      oneToMany.setEmbedded(embed == null || "true".equals(embed));
      String notFound = node.attributeValue("not-found");
      oneToMany.setIgnoreNotFound("ignore".equals(notFound));
   }

   public static void bindColumn(Element node, Column column, boolean isNullable) throws MappingException {
      Attribute lengthNode = node.attribute("length");
      if (lengthNode != null) {
         column.setLength(Integer.parseInt(lengthNode.getValue()));
      }

      Attribute scalNode = node.attribute("scale");
      if (scalNode != null) {
         column.setScale(Integer.parseInt(scalNode.getValue()));
      }

      Attribute precNode = node.attribute("precision");
      if (precNode != null) {
         column.setPrecision(Integer.parseInt(precNode.getValue()));
      }

      Attribute nullNode = node.attribute("not-null");
      column.setNullable(nullNode == null ? isNullable : nullNode.getValue().equals("false"));
      Attribute unqNode = node.attribute("unique");
      if (unqNode != null) {
         column.setUnique(unqNode.getValue().equals("true"));
      }

      column.setCheckConstraint(node.attributeValue("check"));
      column.setDefaultValue(node.attributeValue("default"));
      Attribute typeNode = node.attribute("sql-type");
      if (typeNode != null) {
         column.setSqlType(typeNode.getValue());
      }

      String customWrite = node.attributeValue("write");
      if (customWrite != null && !customWrite.matches("[^?]*\\?[^?]*")) {
         throw new MappingException("write expression must contain exactly one value placeholder ('?') character");
      } else {
         column.setCustomWrite(customWrite);
         column.setCustomRead(node.attributeValue("read"));
         Element comment = node.element("comment");
         if (comment != null) {
            column.setComment(comment.getTextTrim());
         }

      }
   }

   public static void bindArray(Element node, Array array, String prefix, String path, Mappings mappings, Map inheritedMetas) throws MappingException {
      bindCollection(node, array, prefix, path, mappings, inheritedMetas);
      Attribute att = node.attribute("element-class");
      if (att != null) {
         array.setElementClassName(getClassName(att, mappings));
      }

   }

   private static Class reflectedPropertyClass(String className, String propertyName) throws MappingException {
      return className == null ? null : ReflectHelper.reflectedPropertyClass(className, propertyName);
   }

   public static void bindComposite(Element node, Component component, String path, boolean isNullable, Mappings mappings, Map inheritedMetas) throws MappingException {
      bindComponent(node, component, (String)null, (String)null, path, isNullable, false, mappings, inheritedMetas, false);
   }

   public static void bindCompositeId(Element node, Component component, PersistentClass persistentClass, String propertyName, Mappings mappings, Map inheritedMetas) throws MappingException {
      component.setKey(true);
      String path = StringHelper.qualify(persistentClass.getEntityName(), propertyName == null ? "id" : propertyName);
      bindComponent(node, component, persistentClass.getClassName(), propertyName, path, false, node.attribute("class") == null && propertyName == null, mappings, inheritedMetas, false);
      if ("true".equals(node.attributeValue("mapped"))) {
         if (propertyName != null) {
            throw new MappingException("cannot combine mapped=\"true\" with specified name");
         }

         Component mapper = new Component(mappings, persistentClass);
         bindComponent(node, mapper, persistentClass.getClassName(), (String)null, path, false, true, mappings, inheritedMetas, true);
         persistentClass.setIdentifierMapper(mapper);
         Property property = new Property();
         property.setName("_identifierMapper");
         property.setNodeName("id");
         property.setUpdateable(false);
         property.setInsertable(false);
         property.setValue(mapper);
         property.setPropertyAccessorName("embedded");
         persistentClass.addProperty(property);
      }

   }

   public static void bindComponent(Element node, Component component, String ownerClassName, String parentProperty, String path, boolean isNullable, boolean isEmbedded, Mappings mappings, Map inheritedMetas, boolean isIdentifierMapper) throws MappingException {
      component.setEmbedded(isEmbedded);
      component.setRoleName(path);
      inheritedMetas = getMetas(node, inheritedMetas);
      component.setMetaAttributes(inheritedMetas);
      Attribute classNode = isIdentifierMapper ? null : node.attribute("class");
      if (classNode != null) {
         component.setComponentClassName(getClassName(classNode, mappings));
      } else if ("dynamic-component".equals(node.getName())) {
         component.setDynamic(true);
      } else if (isEmbedded) {
         if (component.getOwner().hasPojoRepresentation()) {
            component.setComponentClassName(component.getOwner().getClassName());
         } else {
            component.setDynamic(true);
         }
      } else if (component.getOwner().hasPojoRepresentation()) {
         Class reflectedClass = reflectedPropertyClass(ownerClassName, parentProperty);
         if (reflectedClass != null) {
            component.setComponentClassName(reflectedClass.getName());
         }
      } else {
         component.setDynamic(true);
      }

      String nodeName = node.attributeValue("node");
      if (nodeName == null) {
         nodeName = node.attributeValue("name");
      }

      if (nodeName == null) {
         nodeName = component.getOwner().getNodeName();
      }

      component.setNodeName(nodeName);
      Iterator iter = node.elementIterator();

      while(iter.hasNext()) {
         Element subnode = (Element)iter.next();
         String name = subnode.getName();
         String propertyName = getPropertyName(subnode);
         String subpath = propertyName == null ? null : StringHelper.qualify(path, propertyName);
         CollectionType collectType = HbmBinder.CollectionType.collectionTypeFromString(name);
         Value value = null;
         if (collectType != null) {
            Collection collection = collectType.create(subnode, subpath, component.getOwner(), mappings, inheritedMetas);
            mappings.addCollection(collection);
            value = collection;
         } else if (!"many-to-one".equals(name) && !"key-many-to-one".equals(name)) {
            if ("one-to-one".equals(name)) {
               value = new OneToOne(mappings, component.getTable(), component.getOwner());
               String relativePath;
               if (isEmbedded) {
                  relativePath = propertyName;
               } else {
                  relativePath = subpath.substring(component.getOwner().getEntityName().length() + 1);
               }

               bindOneToOne(subnode, (OneToOne)value, relativePath, isNullable, mappings);
            } else if ("any".equals(name)) {
               value = new Any(mappings, component.getTable());
               bindAny(subnode, (Any)value, isNullable, mappings);
            } else if (!"property".equals(name) && !"key-property".equals(name)) {
               if (!"component".equals(name) && !"dynamic-component".equals(name) && !"nested-composite-element".equals(name)) {
                  if ("parent".equals(name)) {
                     component.setParentProperty(propertyName);
                  }
               } else {
                  value = new Component(mappings, component);
                  bindComponent(subnode, (Component)value, component.getComponentClassName(), propertyName, subpath, isNullable, isEmbedded, mappings, inheritedMetas, isIdentifierMapper);
               }
            } else {
               value = new SimpleValue(mappings, component.getTable());
               String relativePath;
               if (isEmbedded) {
                  relativePath = propertyName;
               } else {
                  relativePath = subpath.substring(component.getOwner().getEntityName().length() + 1);
               }

               bindSimpleValue(subnode, (SimpleValue)value, isNullable, relativePath, mappings);
            }
         } else {
            value = new ManyToOne(mappings, component.getTable());
            String relativePath;
            if (isEmbedded) {
               relativePath = propertyName;
            } else {
               relativePath = subpath.substring(component.getOwner().getEntityName().length() + 1);
            }

            bindManyToOne(subnode, (ManyToOne)value, relativePath, isNullable, mappings);
         }

         if (value != null) {
            Property property = createProperty(value, propertyName, component.getComponentClassName(), subnode, mappings, inheritedMetas);
            if (isIdentifierMapper) {
               property.setInsertable(false);
               property.setUpdateable(false);
            }

            component.addProperty(property);
         }
      }

      if ("true".equals(node.attributeValue("unique"))) {
         iter = component.getColumnIterator();
         ArrayList cols = new ArrayList();

         while(iter.hasNext()) {
            cols.add(iter.next());
         }

         component.getOwner().getTable().createUniqueKey(cols);
      }

      iter = node.elementIterator("tuplizer");

      while(iter.hasNext()) {
         Element tuplizerElem = (Element)iter.next();
         EntityMode mode = EntityMode.parse(tuplizerElem.attributeValue("entity-mode"));
         component.addTuplizer(mode, tuplizerElem.attributeValue("class"));
      }

   }

   public static String getTypeFromXML(Element node) throws MappingException {
      Attribute typeNode = node.attribute("type");
      if (typeNode == null) {
         typeNode = node.attribute("id-type");
      }

      return typeNode == null ? null : typeNode.getValue();
   }

   private static void initOuterJoinFetchSetting(Element node, Fetchable model) {
      Attribute fetchNode = node.attribute("fetch");
      boolean lazy = true;
      FetchMode fetchStyle;
      if (fetchNode == null) {
         Attribute jfNode = node.attribute("outer-join");
         if (jfNode == null) {
            if ("many-to-many".equals(node.getName())) {
               lazy = false;
               fetchStyle = FetchMode.JOIN;
            } else if ("one-to-one".equals(node.getName())) {
               lazy = ((OneToOne)model).isConstrained();
               fetchStyle = lazy ? FetchMode.DEFAULT : FetchMode.JOIN;
            } else {
               fetchStyle = FetchMode.DEFAULT;
            }
         } else {
            String eoj = jfNode.getValue();
            if ("auto".equals(eoj)) {
               fetchStyle = FetchMode.DEFAULT;
            } else {
               boolean join = "true".equals(eoj);
               fetchStyle = join ? FetchMode.JOIN : FetchMode.SELECT;
            }
         }
      } else {
         boolean join = "join".equals(fetchNode.getValue());
         fetchStyle = join ? FetchMode.JOIN : FetchMode.SELECT;
      }

      model.setFetchMode(fetchStyle);
      model.setLazy(lazy);
   }

   private static void makeIdentifier(Element node, SimpleValue model, Mappings mappings) {
      Element subnode = node.element("generator");
      if (subnode != null) {
         String generatorClass = subnode.attributeValue("class");
         model.setIdentifierGeneratorStrategy(generatorClass);
         Properties params = new Properties();
         params.put("identifier_normalizer", mappings.getObjectNameNormalizer());
         if (mappings.getSchemaName() != null) {
            params.setProperty("schema", mappings.getObjectNameNormalizer().normalizeIdentifierQuoting(mappings.getSchemaName()));
         }

         if (mappings.getCatalogName() != null) {
            params.setProperty("catalog", mappings.getObjectNameNormalizer().normalizeIdentifierQuoting(mappings.getCatalogName()));
         }

         Iterator iter = subnode.elementIterator("param");

         while(iter.hasNext()) {
            Element childNode = (Element)iter.next();
            params.setProperty(childNode.attributeValue("name"), childNode.getTextTrim());
         }

         model.setIdentifierGeneratorProperties(params);
      }

      model.getTable().setIdentifierValue(model);
      Attribute nullValueNode = node.attribute("unsaved-value");
      if (nullValueNode != null) {
         model.setNullValue(nullValueNode.getValue());
      } else if ("assigned".equals(model.getIdentifierGeneratorStrategy())) {
         model.setNullValue("undefined");
      } else {
         model.setNullValue((String)null);
      }

   }

   private static final void makeVersion(Element node, SimpleValue model) {
      Attribute nullValueNode = node.attribute("unsaved-value");
      if (nullValueNode != null) {
         model.setNullValue(nullValueNode.getValue());
      } else {
         model.setNullValue("undefined");
      }

   }

   protected static void createClassProperties(Element node, PersistentClass persistentClass, Mappings mappings, Map inheritedMetas) throws MappingException {
      createClassProperties(node, persistentClass, mappings, inheritedMetas, (UniqueKey)null, true, true, false);
   }

   protected static void createClassProperties(Element node, PersistentClass persistentClass, Mappings mappings, Map inheritedMetas, UniqueKey uniqueKey, boolean mutable, boolean nullable, boolean naturalId) throws MappingException {
      String entityName = persistentClass.getEntityName();
      Table table = persistentClass.getTable();
      Iterator iter = node.elementIterator();

      while(iter.hasNext()) {
         Element subnode = (Element)iter.next();
         String name = subnode.getName();
         String propertyName = subnode.attributeValue("name");
         CollectionType collectType = HbmBinder.CollectionType.collectionTypeFromString(name);
         Value value = null;
         if (collectType != null) {
            Collection collection = collectType.create(subnode, StringHelper.qualify(entityName, propertyName), persistentClass, mappings, inheritedMetas);
            mappings.addCollection(collection);
            value = collection;
         } else if ("many-to-one".equals(name)) {
            value = new ManyToOne(mappings, table);
            bindManyToOne(subnode, (ManyToOne)value, propertyName, nullable, mappings);
         } else if ("any".equals(name)) {
            value = new Any(mappings, table);
            bindAny(subnode, (Any)value, nullable, mappings);
         } else if ("one-to-one".equals(name)) {
            value = new OneToOne(mappings, table, persistentClass);
            bindOneToOne(subnode, (OneToOne)value, propertyName, true, mappings);
         } else if ("property".equals(name)) {
            value = new SimpleValue(mappings, table);
            bindSimpleValue(subnode, (SimpleValue)value, nullable, propertyName, mappings);
         } else if (!"component".equals(name) && !"dynamic-component".equals(name) && !"properties".equals(name)) {
            if ("join".equals(name)) {
               Join join = new Join();
               join.setPersistentClass(persistentClass);
               bindJoin(subnode, join, mappings, inheritedMetas);
               persistentClass.addJoin(join);
            } else if ("subclass".equals(name)) {
               handleSubclass(persistentClass, mappings, subnode, inheritedMetas);
            } else if ("joined-subclass".equals(name)) {
               handleJoinedSubclass(persistentClass, mappings, subnode, inheritedMetas);
            } else if ("union-subclass".equals(name)) {
               handleUnionSubclass(persistentClass, mappings, subnode, inheritedMetas);
            } else if ("filter".equals(name)) {
               parseFilter(subnode, persistentClass, mappings);
            } else if ("natural-id".equals(name)) {
               UniqueKey uk = new UniqueKey();
               uk.setName("_UniqueKey");
               uk.setTable(table);
               boolean mutableId = "true".equals(subnode.attributeValue("mutable"));
               createClassProperties(subnode, persistentClass, mappings, inheritedMetas, uk, mutableId, false, true);
               table.addUniqueKey(uk);
            } else if ("query".equals(name)) {
               bindNamedQuery(subnode, persistentClass.getEntityName(), mappings);
            } else if ("sql-query".equals(name)) {
               bindNamedSQLQuery(subnode, persistentClass.getEntityName(), mappings);
            } else if ("resultset".equals(name)) {
               bindResultSetMappingDefinition(subnode, persistentClass.getEntityName(), mappings);
            }
         } else {
            String subpath = StringHelper.qualify(entityName, propertyName);
            value = new Component(mappings, persistentClass);
            bindComponent(subnode, (Component)value, persistentClass.getClassName(), propertyName, subpath, true, "properties".equals(name), mappings, inheritedMetas, false);
         }

         if (value != null) {
            Property property = createProperty(value, propertyName, persistentClass.getClassName(), subnode, mappings, inheritedMetas);
            if (!mutable) {
               property.setUpdateable(false);
            }

            if (naturalId) {
               property.setNaturalIdentifier(true);
            }

            persistentClass.addProperty(property);
            if (uniqueKey != null) {
               uniqueKey.addColumns(property.getColumnIterator());
            }
         }
      }

   }

   private static Property createProperty(Value value, String propertyName, String className, Element subnode, Mappings mappings, Map inheritedMetas) throws MappingException {
      if (StringHelper.isEmpty(propertyName)) {
         throw new MappingException(subnode.getName() + " mapping must defined a name attribute [" + className + "]");
      } else {
         value.setTypeUsingReflection(className, propertyName);
         if (value instanceof ToOne) {
            ToOne toOne = (ToOne)value;
            String propertyRef = toOne.getReferencedPropertyName();
            if (propertyRef != null) {
               mappings.addUniquePropertyReference(toOne.getReferencedEntityName(), propertyRef);
            }
         } else if (value instanceof Collection) {
            Collection coll = (Collection)value;
            String propertyRef = coll.getReferencedPropertyName();
            if (propertyRef != null) {
               mappings.addPropertyReference(coll.getOwnerEntityName(), propertyRef);
            }
         }

         value.createForeignKey();
         Property prop = new Property();
         prop.setValue(value);
         bindProperty(subnode, prop, mappings, inheritedMetas);
         return prop;
      }
   }

   private static void handleUnionSubclass(PersistentClass model, Mappings mappings, Element subnode, Map inheritedMetas) throws MappingException {
      UnionSubclass subclass = new UnionSubclass(model);
      bindUnionSubclass(subnode, subclass, mappings, inheritedMetas);
      model.addSubclass(subclass);
      mappings.addClass(subclass);
   }

   private static void handleJoinedSubclass(PersistentClass model, Mappings mappings, Element subnode, Map inheritedMetas) throws MappingException {
      JoinedSubclass subclass = new JoinedSubclass(model);
      bindJoinedSubclass(subnode, subclass, mappings, inheritedMetas);
      model.addSubclass(subclass);
      mappings.addClass(subclass);
   }

   private static void handleSubclass(PersistentClass model, Mappings mappings, Element subnode, Map inheritedMetas) throws MappingException {
      Subclass subclass = new SingleTableSubclass(model);
      bindSubclass(subnode, subclass, mappings, inheritedMetas);
      model.addSubclass(subclass);
      mappings.addClass(subclass);
   }

   public static void bindListSecondPass(Element node, org.hibernate.mapping.List list, Map classes, Mappings mappings, Map inheritedMetas) throws MappingException {
      bindCollectionSecondPass(node, list, classes, mappings, inheritedMetas);
      Element subnode = node.element("list-index");
      if (subnode == null) {
         subnode = node.element("index");
      }

      SimpleValue iv = new SimpleValue(mappings, list.getCollectionTable());
      bindSimpleValue(subnode, iv, list.isOneToMany(), "idx", mappings);
      iv.setTypeName("integer");
      list.setIndex(iv);
      String baseIndex = subnode.attributeValue("base");
      if (baseIndex != null) {
         list.setBaseIndex(Integer.parseInt(baseIndex));
      }

      list.setIndexNodeName(subnode.attributeValue("node"));
      if (list.isOneToMany() && !list.getKey().isNullable() && !list.isInverse()) {
         String entityName = ((OneToMany)list.getElement()).getReferencedEntityName();
         PersistentClass referenced = mappings.getClass(entityName);
         IndexBackref ib = new IndexBackref();
         ib.setName('_' + list.getOwnerEntityName() + "." + node.attributeValue("name") + "IndexBackref");
         ib.setUpdateable(false);
         ib.setSelectable(false);
         ib.setCollectionRole(list.getRole());
         ib.setEntityName(list.getOwner().getEntityName());
         ib.setValue(list.getIndex());
         referenced.addProperty(ib);
      }

   }

   public static void bindIdentifierCollectionSecondPass(Element node, IdentifierCollection collection, Map persistentClasses, Mappings mappings, Map inheritedMetas) throws MappingException {
      bindCollectionSecondPass(node, collection, persistentClasses, mappings, inheritedMetas);
      Element subnode = node.element("collection-id");
      SimpleValue id = new SimpleValue(mappings, collection.getCollectionTable());
      bindSimpleValue(subnode, id, false, "id", mappings);
      collection.setIdentifier(id);
      makeIdentifier(subnode, id, mappings);
   }

   public static void bindMapSecondPass(Element node, org.hibernate.mapping.Map map, Map classes, Mappings mappings, Map inheritedMetas) throws MappingException {
      bindCollectionSecondPass(node, map, classes, mappings, inheritedMetas);
      Iterator iter = node.elementIterator();

      while(iter.hasNext()) {
         Element subnode = (Element)iter.next();
         String name = subnode.getName();
         if (!"index".equals(name) && !"map-key".equals(name)) {
            if (!"index-many-to-many".equals(name) && !"map-key-many-to-many".equals(name)) {
               if (!"composite-index".equals(name) && !"composite-map-key".equals(name)) {
                  if ("index-many-to-any".equals(name)) {
                     Any any = new Any(mappings, map.getCollectionTable());
                     bindAny(subnode, any, map.isOneToMany(), mappings);
                     map.setIndex(any);
                  }
               } else {
                  Component component = new Component(mappings, map);
                  bindComposite(subnode, component, map.getRole() + ".index", map.isOneToMany(), mappings, inheritedMetas);
                  map.setIndex(component);
               }
            } else {
               ManyToOne mto = new ManyToOne(mappings, map.getCollectionTable());
               bindManyToOne(subnode, mto, "idx", map.isOneToMany(), mappings);
               map.setIndex(mto);
            }
         } else {
            SimpleValue value = new SimpleValue(mappings, map.getCollectionTable());
            bindSimpleValue(subnode, value, map.isOneToMany(), "idx", mappings);
            if (!value.isTypeSpecified()) {
               throw new MappingException("map index element must specify a type: " + map.getRole());
            }

            map.setIndex(value);
            map.setIndexNodeName(subnode.attributeValue("node"));
         }
      }

      boolean indexIsFormula = false;
      Iterator colIter = map.getIndex().getColumnIterator();

      while(colIter.hasNext()) {
         if (((Selectable)colIter.next()).isFormula()) {
            indexIsFormula = true;
         }
      }

      if (map.isOneToMany() && !map.getKey().isNullable() && !map.isInverse() && !indexIsFormula) {
         String entityName = ((OneToMany)map.getElement()).getReferencedEntityName();
         PersistentClass referenced = mappings.getClass(entityName);
         IndexBackref ib = new IndexBackref();
         ib.setName('_' + map.getOwnerEntityName() + "." + node.attributeValue("name") + "IndexBackref");
         ib.setUpdateable(false);
         ib.setSelectable(false);
         ib.setCollectionRole(map.getRole());
         ib.setEntityName(map.getOwner().getEntityName());
         ib.setValue(map.getIndex());
         referenced.addProperty(ib);
      }

   }

   public static void bindCollectionSecondPass(Element node, Collection collection, Map persistentClasses, Mappings mappings, Map inheritedMetas) throws MappingException {
      if (collection.isOneToMany()) {
         OneToMany oneToMany = (OneToMany)collection.getElement();
         String assocClass = oneToMany.getReferencedEntityName();
         PersistentClass persistentClass = (PersistentClass)persistentClasses.get(assocClass);
         if (persistentClass == null) {
            throw new MappingException("Association references unmapped class: " + assocClass);
         }

         oneToMany.setAssociatedClass(persistentClass);
         collection.setCollectionTable(persistentClass.getTable());
         if (LOG.isDebugEnabled()) {
            LOG.debugf("Mapping collection: %s -> %s", collection.getRole(), collection.getCollectionTable().getName());
         }
      }

      Attribute chNode = node.attribute("check");
      if (chNode != null) {
         collection.getCollectionTable().addCheckConstraint(chNode.getValue());
      }

      Iterator iter = node.elementIterator();

      while(iter.hasNext()) {
         Element subnode = (Element)iter.next();
         String name = subnode.getName();
         if ("key".equals(name)) {
            String propRef = collection.getReferencedPropertyName();
            KeyValue keyVal;
            if (propRef == null) {
               keyVal = collection.getOwner().getIdentifier();
            } else {
               keyVal = (KeyValue)collection.getOwner().getRecursiveProperty(propRef).getValue();
            }

            SimpleValue key = new DependantValue(mappings, collection.getCollectionTable(), keyVal);
            key.setCascadeDeleteEnabled("cascade".equals(subnode.attributeValue("on-delete")));
            bindSimpleValue(subnode, key, collection.isOneToMany(), "id", mappings);
            collection.setKey(key);
            Attribute notNull = subnode.attribute("not-null");
            ((DependantValue)key).setNullable(notNull == null || notNull.getValue().equals("false"));
            Attribute updateable = subnode.attribute("update");
            ((DependantValue)key).setUpdateable(updateable == null || updateable.getValue().equals("true"));
         } else if ("element".equals(name)) {
            SimpleValue elt = new SimpleValue(mappings, collection.getCollectionTable());
            collection.setElement(elt);
            bindSimpleValue(subnode, elt, true, "elt", mappings);
         } else if ("many-to-many".equals(name)) {
            ManyToOne element = new ManyToOne(mappings, collection.getCollectionTable());
            collection.setElement(element);
            bindManyToOne(subnode, element, "elt", false, mappings);
            bindManyToManySubelements(collection, subnode, mappings);
         } else if ("composite-element".equals(name)) {
            Component element = new Component(mappings, collection);
            collection.setElement(element);
            bindComposite(subnode, element, collection.getRole() + ".element", true, mappings, inheritedMetas);
         } else if ("many-to-any".equals(name)) {
            Any element = new Any(mappings, collection.getCollectionTable());
            collection.setElement(element);
            bindAny(subnode, element, true, mappings);
         } else if ("cache".equals(name)) {
            collection.setCacheConcurrencyStrategy(subnode.attributeValue("usage"));
            collection.setCacheRegionName(subnode.attributeValue("region"));
         }

         String nodeName = subnode.attributeValue("node");
         if (nodeName != null) {
            collection.setElementNodeName(nodeName);
         }
      }

      if (collection.isOneToMany() && !collection.isInverse() && !collection.getKey().isNullable()) {
         String entityName = ((OneToMany)collection.getElement()).getReferencedEntityName();
         PersistentClass referenced = mappings.getClass(entityName);
         Backref prop = new Backref();
         prop.setName('_' + collection.getOwnerEntityName() + "." + node.attributeValue("name") + "Backref");
         prop.setUpdateable(false);
         prop.setSelectable(false);
         prop.setCollectionRole(collection.getRole());
         prop.setEntityName(collection.getOwner().getEntityName());
         prop.setValue(collection.getKey());
         referenced.addProperty(prop);
      }

   }

   private static void bindManyToManySubelements(Collection collection, Element manyToManyNode, Mappings model) throws MappingException {
      Attribute where = manyToManyNode.attribute("where");
      String whereCondition = where == null ? null : where.getValue();
      collection.setManyToManyWhere(whereCondition);
      Attribute order = manyToManyNode.attribute("order-by");
      String orderFragment = order == null ? null : order.getValue();
      collection.setManyToManyOrdering(orderFragment);
      Iterator filters = manyToManyNode.elementIterator("filter");
      if ((filters.hasNext() || whereCondition != null) && collection.getFetchMode() == FetchMode.JOIN && collection.getElement().getFetchMode() != FetchMode.JOIN) {
         throw new MappingException("many-to-many defining filter or where without join fetching not valid within collection using join fetching [" + collection.getRole() + "]");
      } else {
         while(filters.hasNext()) {
            Element filterElement = (Element)filters.next();
            String name = filterElement.attributeValue("name");
            String condition = filterElement.getTextTrim();
            if (StringHelper.isEmpty(condition)) {
               condition = filterElement.attributeValue("condition");
            }

            if (StringHelper.isEmpty(condition)) {
               condition = model.getFilterDefinition(name).getDefaultFilterCondition();
            }

            if (condition == null) {
               throw new MappingException("no filter condition found for filter: " + name);
            }

            Iterator aliasesIterator = filterElement.elementIterator("aliases");
            Map<String, String> aliasTables = new HashMap();

            while(aliasesIterator.hasNext()) {
               Element alias = (Element)aliasesIterator.next();
               aliasTables.put(alias.attributeValue("alias"), alias.attributeValue("table"));
            }

            if (LOG.isDebugEnabled()) {
               LOG.debugf("Applying many-to-many filter [%s] as [%s] to role [%s]", name, condition, collection.getRole());
            }

            String autoAliasInjectionText = filterElement.attributeValue("autoAliasInjection");
            boolean autoAliasInjection = StringHelper.isEmpty(autoAliasInjectionText) ? true : Boolean.parseBoolean(autoAliasInjectionText);
            collection.addManyToManyFilter(name, condition, autoAliasInjection, aliasTables, (Map)null);
         }

      }
   }

   public static final FlushMode getFlushMode(String flushMode) {
      if (flushMode == null) {
         return null;
      } else if ("auto".equals(flushMode)) {
         return FlushMode.AUTO;
      } else if ("commit".equals(flushMode)) {
         return FlushMode.COMMIT;
      } else if ("never".equals(flushMode)) {
         return FlushMode.NEVER;
      } else if ("manual".equals(flushMode)) {
         return FlushMode.MANUAL;
      } else if ("always".equals(flushMode)) {
         return FlushMode.ALWAYS;
      } else {
         throw new MappingException("unknown flushmode");
      }
   }

   private static void bindNamedQuery(Element queryElem, String path, Mappings mappings) {
      String queryName = queryElem.attributeValue("name");
      if (path != null) {
         queryName = path + '.' + queryName;
      }

      String query = queryElem.getText();
      LOG.debugf("Named query: %s -> %s", queryName, query);
      boolean cacheable = "true".equals(queryElem.attributeValue("cacheable"));
      String region = queryElem.attributeValue("cache-region");
      Attribute tAtt = queryElem.attribute("timeout");
      Integer timeout = tAtt == null ? null : Integer.valueOf(tAtt.getValue());
      Attribute fsAtt = queryElem.attribute("fetch-size");
      Integer fetchSize = fsAtt == null ? null : Integer.valueOf(fsAtt.getValue());
      Attribute roAttr = queryElem.attribute("read-only");
      boolean readOnly = roAttr != null && "true".equals(roAttr.getValue());
      Attribute cacheModeAtt = queryElem.attribute("cache-mode");
      String cacheMode = cacheModeAtt == null ? null : cacheModeAtt.getValue();
      Attribute cmAtt = queryElem.attribute("comment");
      String comment = cmAtt == null ? null : cmAtt.getValue();
      NamedQueryDefinition namedQuery = new NamedQueryDefinition(queryName, query, cacheable, region, timeout, fetchSize, getFlushMode(queryElem.attributeValue("flush-mode")), getCacheMode(cacheMode), readOnly, comment, getParameterTypes(queryElem));
      mappings.addQuery(namedQuery.getName(), namedQuery);
   }

   public static CacheMode getCacheMode(String cacheMode) {
      if (cacheMode == null) {
         return null;
      } else if ("get".equals(cacheMode)) {
         return CacheMode.GET;
      } else if ("ignore".equals(cacheMode)) {
         return CacheMode.IGNORE;
      } else if ("normal".equals(cacheMode)) {
         return CacheMode.NORMAL;
      } else if ("put".equals(cacheMode)) {
         return CacheMode.PUT;
      } else if ("refresh".equals(cacheMode)) {
         return CacheMode.REFRESH;
      } else {
         throw new MappingException("Unknown Cache Mode: " + cacheMode);
      }
   }

   public static Map getParameterTypes(Element queryElem) {
      Map result = new LinkedHashMap();
      Iterator iter = queryElem.elementIterator("query-param");

      while(iter.hasNext()) {
         Element element = (Element)iter.next();
         result.put(element.attributeValue("name"), element.attributeValue("type"));
      }

      return result;
   }

   private static void bindResultSetMappingDefinition(Element resultSetElem, String path, Mappings mappings) {
      mappings.addSecondPass(new ResultSetMappingSecondPass(resultSetElem, path, mappings));
   }

   private static void bindNamedSQLQuery(Element queryElem, String path, Mappings mappings) {
      mappings.addSecondPass(new NamedSQLQuerySecondPass(queryElem, path, mappings));
   }

   private static String getPropertyName(Element node) {
      return node.attributeValue("name");
   }

   private static PersistentClass getSuperclass(Mappings mappings, Element subnode) throws MappingException {
      String extendsName = subnode.attributeValue("extends");
      PersistentClass superModel = mappings.getClass(extendsName);
      if (superModel == null) {
         String qualifiedExtendsName = getClassName(extendsName, mappings);
         superModel = mappings.getClass(qualifiedExtendsName);
      }

      if (superModel == null) {
         throw new MappingException("Cannot extend unmapped class " + extendsName);
      } else {
         return superModel;
      }
   }

   private static int getOptimisticLockMode(Attribute olAtt) throws MappingException {
      if (olAtt == null) {
         return 0;
      } else {
         String olMode = olAtt.getValue();
         if (olMode != null && !"version".equals(olMode)) {
            if ("dirty".equals(olMode)) {
               return 1;
            } else if ("all".equals(olMode)) {
               return 2;
            } else if ("none".equals(olMode)) {
               return -1;
            } else {
               throw new MappingException("Unsupported optimistic-lock style: " + olMode);
            }
         } else {
            return 0;
         }
      }
   }

   private static final Map getMetas(Element node, Map inheritedMeta) {
      return getMetas(node, inheritedMeta, false);
   }

   public static final Map getMetas(Element node, Map inheritedMeta, boolean onlyInheritable) {
      Map map = new HashMap();
      map.putAll(inheritedMeta);
      Iterator iter = node.elementIterator("meta");

      while(iter.hasNext()) {
         Element metaNode = (Element)iter.next();
         boolean inheritable = Boolean.valueOf(metaNode.attributeValue("inherit"));
         if (!(onlyInheritable & !inheritable)) {
            String name = metaNode.attributeValue("attribute");
            MetaAttribute meta = (MetaAttribute)map.get(name);
            MetaAttribute inheritedAttribute = (MetaAttribute)inheritedMeta.get(name);
            if (meta == null) {
               meta = new MetaAttribute(name);
               map.put(name, meta);
            } else if (meta == inheritedAttribute) {
               meta = new MetaAttribute(name);
               map.put(name, meta);
            }

            meta.addValue(metaNode.getText());
         }
      }

      return map;
   }

   public static String getEntityName(Element elem, Mappings model) {
      String entityName = elem.attributeValue("entity-name");
      return entityName == null ? getClassName(elem.attribute("class"), model) : entityName;
   }

   private static String getClassName(Attribute att, Mappings model) {
      return att == null ? null : getClassName(att.getValue(), model);
   }

   public static String getClassName(String unqualifiedName, Mappings model) {
      return getClassName(unqualifiedName, model.getDefaultPackage());
   }

   public static String getClassName(String unqualifiedName, String defaultPackage) {
      if (unqualifiedName == null) {
         return null;
      } else {
         return unqualifiedName.indexOf(46) < 0 && defaultPackage != null ? defaultPackage + '.' + unqualifiedName : unqualifiedName;
      }
   }

   private static void parseFilterDef(Element element, Mappings mappings) {
      String name = element.attributeValue("name");
      LOG.debugf("Parsing filter-def [%s]", name);
      String defaultCondition = element.getTextTrim();
      if (StringHelper.isEmpty(defaultCondition)) {
         defaultCondition = element.attributeValue("condition");
      }

      HashMap paramMappings = new HashMap();
      Iterator params = element.elementIterator("filter-param");

      while(params.hasNext()) {
         Element param = (Element)params.next();
         String paramName = param.attributeValue("name");
         String paramType = param.attributeValue("type");
         LOG.debugf("Adding filter parameter : %s -> %s", paramName, paramType);
         Type heuristicType = mappings.getTypeResolver().heuristicType(paramType);
         LOG.debugf("Parameter heuristic type : %s", heuristicType);
         paramMappings.put(paramName, heuristicType);
      }

      LOG.debugf("Parsed filter-def [%s]", name);
      FilterDefinition def = new FilterDefinition(name, defaultCondition, paramMappings);
      mappings.addFilterDefinition(def);
   }

   private static void parseFilter(Element filterElement, Filterable filterable, Mappings model) {
      String name = filterElement.attributeValue("name");
      String condition = filterElement.getTextTrim();
      if (StringHelper.isEmpty(condition)) {
         condition = filterElement.attributeValue("condition");
      }

      if (StringHelper.isEmpty(condition)) {
         condition = model.getFilterDefinition(name).getDefaultFilterCondition();
      }

      if (condition == null) {
         throw new MappingException("no filter condition found for filter: " + name);
      } else {
         Iterator aliasesIterator = filterElement.elementIterator("aliases");
         Map<String, String> aliasTables = new HashMap();

         while(aliasesIterator.hasNext()) {
            Element alias = (Element)aliasesIterator.next();
            aliasTables.put(alias.attributeValue("alias"), alias.attributeValue("table"));
         }

         LOG.debugf("Applying filter [%s] as [%s]", name, condition);
         String autoAliasInjectionText = filterElement.attributeValue("autoAliasInjection");
         boolean autoAliasInjection = StringHelper.isEmpty(autoAliasInjectionText) ? true : Boolean.parseBoolean(autoAliasInjectionText);
         filterable.addFilter(name, condition, autoAliasInjection, aliasTables, (Map)null);
      }
   }

   private static void parseFetchProfile(Element element, Mappings mappings, String containingEntityName) {
      String profileName = element.attributeValue("name");
      FetchProfile profile = mappings.findOrCreateFetchProfile(profileName, MetadataSource.HBM);
      Iterator itr = element.elementIterator("fetch");

      while(itr.hasNext()) {
         Element fetchElement = (Element)itr.next();
         String association = fetchElement.attributeValue("association");
         String style = fetchElement.attributeValue("style");
         String entityName = fetchElement.attributeValue("entity");
         if (entityName == null) {
            entityName = containingEntityName;
         }

         if (entityName == null) {
            throw new MappingException("could not determine entity for fetch-profile fetch [" + profileName + "]:[" + association + "]");
         }

         profile.addFetch(entityName, association, style);
      }

   }

   private static String getSubselect(Element element) {
      String subselect = element.attributeValue("subselect");
      if (subselect != null) {
         return subselect;
      } else {
         Element subselectElement = element.element("subselect");
         return subselectElement == null ? null : subselectElement.getText();
      }
   }

   public static List getExtendsNeeded(XmlDocument metadataXml, Mappings mappings) {
      List<String> extendz = new ArrayList();
      Iterator[] subclasses = new Iterator[3];
      Element hmNode = metadataXml.getDocumentTree().getRootElement();
      Attribute packNode = hmNode.attribute("package");
      final String packageName = packNode == null ? null : packNode.getValue();
      if (packageName != null) {
         mappings.setDefaultPackage(packageName);
      }

      subclasses[0] = hmNode.elementIterator("subclass");
      subclasses[1] = hmNode.elementIterator("joined-subclass");
      subclasses[2] = hmNode.elementIterator("union-subclass");
      Iterator iterator = new JoinedIterator(subclasses);

      while(iterator.hasNext()) {
         Element element = (Element)iterator.next();
         String extendsName = element.attributeValue("extends");
         if (mappings.getClass(extendsName) == null && mappings.getClass(getClassName(extendsName, mappings)) == null) {
            extendz.add(extendsName);
         }
      }

      if (!extendz.isEmpty()) {
         final Set<String> set = new HashSet(extendz);
         EntityElementHandler handler = new EntityElementHandler() {
            public void handleEntity(String entityName, String className, Mappings mappings) {
               if (entityName != null) {
                  set.remove(entityName);
               } else {
                  String fqn = HbmBinder.getClassName(className, packageName);
                  set.remove(fqn);
                  if (packageName != null) {
                     set.remove(StringHelper.unqualify(fqn));
                  }
               }

            }
         };
         recognizeEntities(mappings, hmNode, handler);
         extendz.clear();
         extendz.addAll(set);
      }

      return extendz;
   }

   private static void recognizeEntities(Mappings mappings, Element startNode, EntityElementHandler handler) {
      Iterator[] classes = new Iterator[4];
      classes[0] = startNode.elementIterator("class");
      classes[1] = startNode.elementIterator("subclass");
      classes[2] = startNode.elementIterator("joined-subclass");
      classes[3] = startNode.elementIterator("union-subclass");
      Iterator classIterator = new JoinedIterator(classes);

      while(classIterator.hasNext()) {
         Element element = (Element)classIterator.next();
         handler.handleEntity(element.attributeValue("entity-name"), element.attributeValue("name"), mappings);
         recognizeEntities(mappings, element, handler);
      }

   }

   static class CollectionSecondPass extends org.hibernate.cfg.CollectionSecondPass {
      Element node;

      CollectionSecondPass(Element node, Mappings mappings, Collection collection, Map inheritedMetas) {
         super(mappings, collection, inheritedMetas);
         this.node = node;
      }

      public void secondPass(Map persistentClasses, Map inheritedMetas) throws MappingException {
         HbmBinder.bindCollectionSecondPass(this.node, this.collection, persistentClasses, this.mappings, inheritedMetas);
      }
   }

   static class IdentifierCollectionSecondPass extends CollectionSecondPass {
      IdentifierCollectionSecondPass(Element node, Mappings mappings, Collection collection, Map inheritedMetas) {
         super(node, mappings, collection, inheritedMetas);
      }

      public void secondPass(Map persistentClasses, Map inheritedMetas) throws MappingException {
         HbmBinder.bindIdentifierCollectionSecondPass(this.node, (IdentifierCollection)this.collection, persistentClasses, this.mappings, inheritedMetas);
      }
   }

   static class MapSecondPass extends CollectionSecondPass {
      MapSecondPass(Element node, Mappings mappings, org.hibernate.mapping.Map collection, Map inheritedMetas) {
         super(node, mappings, collection, inheritedMetas);
      }

      public void secondPass(Map persistentClasses, Map inheritedMetas) throws MappingException {
         HbmBinder.bindMapSecondPass(this.node, (org.hibernate.mapping.Map)this.collection, persistentClasses, this.mappings, inheritedMetas);
      }
   }

   static class ManyToOneSecondPass implements SecondPass {
      private final ManyToOne manyToOne;

      ManyToOneSecondPass(ManyToOne manyToOne) {
         this.manyToOne = manyToOne;
      }

      public void doSecondPass(Map persistentClasses) throws MappingException {
         this.manyToOne.createPropertyRefConstraints(persistentClasses);
      }
   }

   static class ListSecondPass extends CollectionSecondPass {
      ListSecondPass(Element node, Mappings mappings, org.hibernate.mapping.List collection, Map inheritedMetas) {
         super(node, mappings, collection, inheritedMetas);
      }

      public void secondPass(Map persistentClasses, Map inheritedMetas) throws MappingException {
         HbmBinder.bindListSecondPass(this.node, (org.hibernate.mapping.List)this.collection, persistentClasses, this.mappings, inheritedMetas);
      }
   }

   abstract static class CollectionType {
      private String xmlTag;
      private static final CollectionType MAP = new CollectionType("map") {
         public Collection create(Element node, String path, PersistentClass owner, Mappings mappings, Map inheritedMetas) throws MappingException {
            org.hibernate.mapping.Map map = new org.hibernate.mapping.Map(mappings, owner);
            HbmBinder.bindCollection(node, map, owner.getEntityName(), path, mappings, inheritedMetas);
            return map;
         }
      };
      private static final CollectionType SET = new CollectionType("set") {
         public Collection create(Element node, String path, PersistentClass owner, Mappings mappings, Map inheritedMetas) throws MappingException {
            org.hibernate.mapping.Set set = new org.hibernate.mapping.Set(mappings, owner);
            HbmBinder.bindCollection(node, set, owner.getEntityName(), path, mappings, inheritedMetas);
            return set;
         }
      };
      private static final CollectionType LIST = new CollectionType("list") {
         public Collection create(Element node, String path, PersistentClass owner, Mappings mappings, Map inheritedMetas) throws MappingException {
            org.hibernate.mapping.List list = new org.hibernate.mapping.List(mappings, owner);
            HbmBinder.bindCollection(node, list, owner.getEntityName(), path, mappings, inheritedMetas);
            return list;
         }
      };
      private static final CollectionType BAG = new CollectionType("bag") {
         public Collection create(Element node, String path, PersistentClass owner, Mappings mappings, Map inheritedMetas) throws MappingException {
            Bag bag = new Bag(mappings, owner);
            HbmBinder.bindCollection(node, bag, owner.getEntityName(), path, mappings, inheritedMetas);
            return bag;
         }
      };
      private static final CollectionType IDBAG = new CollectionType("idbag") {
         public Collection create(Element node, String path, PersistentClass owner, Mappings mappings, Map inheritedMetas) throws MappingException {
            IdentifierBag bag = new IdentifierBag(mappings, owner);
            HbmBinder.bindCollection(node, bag, owner.getEntityName(), path, mappings, inheritedMetas);
            return bag;
         }
      };
      private static final CollectionType ARRAY = new CollectionType("array") {
         public Collection create(Element node, String path, PersistentClass owner, Mappings mappings, Map inheritedMetas) throws MappingException {
            Array array = new Array(mappings, owner);
            HbmBinder.bindArray(node, array, owner.getEntityName(), path, mappings, inheritedMetas);
            return array;
         }
      };
      private static final CollectionType PRIMITIVE_ARRAY = new CollectionType("primitive-array") {
         public Collection create(Element node, String path, PersistentClass owner, Mappings mappings, Map inheritedMetas) throws MappingException {
            PrimitiveArray array = new PrimitiveArray(mappings, owner);
            HbmBinder.bindArray(node, array, owner.getEntityName(), path, mappings, inheritedMetas);
            return array;
         }
      };
      private static final HashMap INSTANCES = new HashMap();

      public abstract Collection create(Element var1, String var2, PersistentClass var3, Mappings var4, Map var5) throws MappingException;

      CollectionType(String xmlTag) {
         this.xmlTag = xmlTag;
      }

      public String toString() {
         return this.xmlTag;
      }

      public static CollectionType collectionTypeFromString(String xmlTagName) {
         return (CollectionType)INSTANCES.get(xmlTagName);
      }

      static {
         INSTANCES.put(MAP.toString(), MAP);
         INSTANCES.put(BAG.toString(), BAG);
         INSTANCES.put(IDBAG.toString(), IDBAG);
         INSTANCES.put(SET.toString(), SET);
         INSTANCES.put(LIST.toString(), LIST);
         INSTANCES.put(ARRAY.toString(), ARRAY);
         INSTANCES.put(PRIMITIVE_ARRAY.toString(), PRIMITIVE_ARRAY);
      }
   }

   private static class ResolveUserTypeMappingSecondPass implements SecondPass {
      private SimpleValue simpleValue;
      private String typeName;
      private Mappings mappings;
      private Properties parameters;

      public ResolveUserTypeMappingSecondPass(SimpleValue simpleValue, String typeName, Mappings mappings, Properties parameters) {
         this.simpleValue = simpleValue;
         this.typeName = typeName;
         this.parameters = parameters;
         this.mappings = mappings;
      }

      public void doSecondPass(Map persistentClasses) throws MappingException {
         HbmBinder.resolveAndBindTypeDef(this.simpleValue, this.mappings, this.typeName, this.parameters);
      }
   }

   private interface EntityElementHandler {
      void handleEntity(String var1, String var2, Mappings var3);
   }
}
