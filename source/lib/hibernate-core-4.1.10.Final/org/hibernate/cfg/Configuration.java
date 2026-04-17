package org.hibernate.cfg;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.MapsId;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.hibernate.AnnotationException;
import org.hibernate.DuplicateMappingException;
import org.hibernate.EmptyInterceptor;
import org.hibernate.HibernateException;
import org.hibernate.Interceptor;
import org.hibernate.InvalidMappingException;
import org.hibernate.MappingException;
import org.hibernate.MappingNotFoundException;
import org.hibernate.SessionFactory;
import org.hibernate.SessionFactoryObserver;
import org.hibernate.annotations.AnyMetaDef;
import org.hibernate.annotations.common.reflection.MetadataProvider;
import org.hibernate.annotations.common.reflection.MetadataProviderInjector;
import org.hibernate.annotations.common.reflection.ReflectionManager;
import org.hibernate.annotations.common.reflection.XClass;
import org.hibernate.annotations.common.reflection.java.JavaReflectionManager;
import org.hibernate.cfg.annotations.reflection.JPAMetadataProvider;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.MySQLDialect;
import org.hibernate.dialect.function.SQLFunction;
import org.hibernate.engine.ResultSetMappingDefinition;
import org.hibernate.engine.spi.FilterDefinition;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.engine.spi.NamedQueryDefinition;
import org.hibernate.engine.spi.NamedSQLQueryDefinition;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.id.IdentifierGeneratorAggregator;
import org.hibernate.id.PersistentIdentifierGenerator;
import org.hibernate.id.factory.IdentifierGeneratorFactory;
import org.hibernate.id.factory.internal.DefaultIdentifierGeneratorFactory;
import org.hibernate.id.factory.spi.MutableIdentifierGeneratorFactory;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.SessionFactoryImpl;
import org.hibernate.internal.util.ConfigHelper;
import org.hibernate.internal.util.ReflectHelper;
import org.hibernate.internal.util.SerializationHelper;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.internal.util.collections.ArrayHelper;
import org.hibernate.internal.util.collections.CollectionHelper;
import org.hibernate.internal.util.collections.JoinedIterator;
import org.hibernate.internal.util.config.ConfigurationHelper;
import org.hibernate.internal.util.xml.ErrorLogger;
import org.hibernate.internal.util.xml.MappingReader;
import org.hibernate.internal.util.xml.Origin;
import org.hibernate.internal.util.xml.OriginImpl;
import org.hibernate.internal.util.xml.XMLHelper;
import org.hibernate.internal.util.xml.XmlDocument;
import org.hibernate.internal.util.xml.XmlDocumentImpl;
import org.hibernate.mapping.AuxiliaryDatabaseObject;
import org.hibernate.mapping.Collection;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.DenormalizedTable;
import org.hibernate.mapping.FetchProfile;
import org.hibernate.mapping.ForeignKey;
import org.hibernate.mapping.IdGenerator;
import org.hibernate.mapping.IdentifierCollection;
import org.hibernate.mapping.Index;
import org.hibernate.mapping.Join;
import org.hibernate.mapping.MappedSuperclass;
import org.hibernate.mapping.MetadataSource;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.RootClass;
import org.hibernate.mapping.SimpleValue;
import org.hibernate.mapping.Table;
import org.hibernate.mapping.TypeDef;
import org.hibernate.mapping.UniqueKey;
import org.hibernate.proxy.EntityNotFoundDelegate;
import org.hibernate.secure.internal.JACCConfiguration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;
import org.hibernate.service.internal.StandardServiceRegistryImpl;
import org.hibernate.tool.hbm2ddl.DatabaseMetadata;
import org.hibernate.tool.hbm2ddl.IndexMetadata;
import org.hibernate.tool.hbm2ddl.TableMetadata;
import org.hibernate.tuple.entity.EntityTuplizerFactory;
import org.hibernate.type.BasicType;
import org.hibernate.type.SerializationException;
import org.hibernate.type.Type;
import org.hibernate.type.TypeResolver;
import org.hibernate.usertype.CompositeUserType;
import org.hibernate.usertype.UserType;
import org.jboss.logging.Logger;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

public class Configuration implements Serializable {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, Configuration.class.getName());
   public static final String DEFAULT_CACHE_CONCURRENCY_STRATEGY = "hibernate.cache.default_cache_concurrency_strategy";
   public static final String USE_NEW_ID_GENERATOR_MAPPINGS = "hibernate.id.new_generator_mappings";
   public static final String ARTEFACT_PROCESSING_ORDER = "hibernate.mapping.precedence";
   private static final String SEARCH_STARTUP_CLASS = "org.hibernate.search.event.EventListenerRegister";
   private static final String SEARCH_STARTUP_METHOD = "enableHibernateSearch";
   protected MetadataSourceQueue metadataSourceQueue;
   private transient ReflectionManager reflectionManager;
   protected Map classes;
   protected Map imports;
   protected Map collections;
   protected Map tables;
   protected List auxiliaryDatabaseObjects;
   protected Map namedQueries;
   protected Map namedSqlQueries;
   protected Map sqlResultSetMappings;
   protected Map typeDefs;
   protected Map filterDefinitions;
   protected Map fetchProfiles;
   protected Map tableNameBinding;
   protected Map columnNameBindingPerTable;
   protected List secondPasses;
   protected List propertyReferences;
   protected Map extendsQueue;
   protected Map sqlFunctions;
   private TypeResolver typeResolver;
   private EntityTuplizerFactory entityTuplizerFactory;
   private Interceptor interceptor;
   private Properties properties;
   private EntityResolver entityResolver;
   private EntityNotFoundDelegate entityNotFoundDelegate;
   protected transient XMLHelper xmlHelper;
   protected NamingStrategy namingStrategy;
   private SessionFactoryObserver sessionFactoryObserver;
   protected final SettingsFactory settingsFactory;
   private transient Mapping mapping;
   private MutableIdentifierGeneratorFactory identifierGeneratorFactory;
   private Map mappedSuperClasses;
   private Map namedGenerators;
   private Map joins;
   private Map classTypes;
   private Set defaultNamedQueryNames;
   private Set defaultNamedNativeQueryNames;
   private Set defaultSqlResultSetMappingNames;
   private Set defaultNamedGenerators;
   private Map generatorTables;
   private Map uniqueConstraintHoldersByTable;
   private Map mappedByResolver;
   private Map propertyRefResolver;
   private Map anyMetaDefs;
   private List caches;
   private boolean inSecondPass;
   private boolean isDefaultProcessed;
   private boolean isValidatorNotPresentLogged;
   private Map propertiesAnnotatedWithMapsId;
   private Map propertiesAnnotatedWithIdAndToOne;
   private CurrentTenantIdentifierResolver currentTenantIdentifierResolver;
   private boolean specjProprietarySyntaxEnabled;
   final ObjectNameNormalizer normalizer;
   public static final MetadataSourceType[] DEFAULT_ARTEFACT_PROCESSING_ORDER;
   private List metadataSourcePrecedence;

   protected Configuration(SettingsFactory settingsFactory) {
      this.typeResolver = new TypeResolver();
      this.mapping = this.buildMapping();
      this.inSecondPass = false;
      this.isDefaultProcessed = false;
      this.normalizer = new ObjectNameNormalizerImpl();
      this.settingsFactory = settingsFactory;
      this.reset();
   }

   public Configuration() {
      this(new SettingsFactory());
   }

   protected void reset() {
      this.metadataSourceQueue = new MetadataSourceQueue();
      this.createReflectionManager();
      this.classes = new HashMap();
      this.imports = new HashMap();
      this.collections = new HashMap();
      this.tables = new TreeMap();
      this.namedQueries = new HashMap();
      this.namedSqlQueries = new HashMap();
      this.sqlResultSetMappings = new HashMap();
      this.typeDefs = new HashMap();
      this.filterDefinitions = new HashMap();
      this.fetchProfiles = new HashMap();
      this.auxiliaryDatabaseObjects = new ArrayList();
      this.tableNameBinding = new HashMap();
      this.columnNameBindingPerTable = new HashMap();
      this.secondPasses = new ArrayList();
      this.propertyReferences = new ArrayList();
      this.extendsQueue = new HashMap();
      this.xmlHelper = new XMLHelper();
      this.interceptor = EmptyInterceptor.INSTANCE;
      this.properties = Environment.getProperties();
      this.entityResolver = XMLHelper.DEFAULT_DTD_RESOLVER;
      this.sqlFunctions = new HashMap();
      this.entityTuplizerFactory = new EntityTuplizerFactory();
      this.identifierGeneratorFactory = new DefaultIdentifierGeneratorFactory();
      this.mappedSuperClasses = new HashMap();
      this.metadataSourcePrecedence = Collections.emptyList();
      this.namedGenerators = new HashMap();
      this.joins = new HashMap();
      this.classTypes = new HashMap();
      this.generatorTables = new HashMap();
      this.defaultNamedQueryNames = new HashSet();
      this.defaultNamedNativeQueryNames = new HashSet();
      this.defaultSqlResultSetMappingNames = new HashSet();
      this.defaultNamedGenerators = new HashSet();
      this.uniqueConstraintHoldersByTable = new HashMap();
      this.mappedByResolver = new HashMap();
      this.propertyRefResolver = new HashMap();
      this.caches = new ArrayList();
      this.namingStrategy = EJB3NamingStrategy.INSTANCE;
      this.setEntityResolver(new EJB3DTDEntityResolver());
      this.anyMetaDefs = new HashMap();
      this.propertiesAnnotatedWithMapsId = new HashMap();
      this.propertiesAnnotatedWithIdAndToOne = new HashMap();
      this.specjProprietarySyntaxEnabled = System.getProperty("hibernate.enable_specj_proprietary_syntax") != null;
   }

   public EntityTuplizerFactory getEntityTuplizerFactory() {
      return this.entityTuplizerFactory;
   }

   public ReflectionManager getReflectionManager() {
      return this.reflectionManager;
   }

   public Iterator getClassMappings() {
      return this.classes.values().iterator();
   }

   public Iterator getCollectionMappings() {
      return this.collections.values().iterator();
   }

   public Iterator getTableMappings() {
      return this.tables.values().iterator();
   }

   public Iterator getMappedSuperclassMappings() {
      return this.mappedSuperClasses.values().iterator();
   }

   public PersistentClass getClassMapping(String entityName) {
      return (PersistentClass)this.classes.get(entityName);
   }

   public Collection getCollectionMapping(String role) {
      return (Collection)this.collections.get(role);
   }

   public void setEntityResolver(EntityResolver entityResolver) {
      this.entityResolver = entityResolver;
   }

   public EntityResolver getEntityResolver() {
      return this.entityResolver;
   }

   public EntityNotFoundDelegate getEntityNotFoundDelegate() {
      return this.entityNotFoundDelegate;
   }

   public void setEntityNotFoundDelegate(EntityNotFoundDelegate entityNotFoundDelegate) {
      this.entityNotFoundDelegate = entityNotFoundDelegate;
   }

   public Configuration addFile(String xmlFile) throws MappingException {
      return this.addFile(new File(xmlFile));
   }

   public Configuration addFile(File xmlFile) throws MappingException {
      LOG.readingMappingsFromFile(xmlFile.getPath());
      String name = xmlFile.getAbsolutePath();

      InputSource inputSource;
      try {
         inputSource = new InputSource(new FileInputStream(xmlFile));
      } catch (FileNotFoundException var5) {
         throw new MappingNotFoundException("file", xmlFile.toString());
      }

      this.add(inputSource, "file", name);
      return this;
   }

   private XmlDocument add(InputSource inputSource, String originType, String originName) {
      return this.add(inputSource, new OriginImpl(originType, originName));
   }

   private XmlDocument add(InputSource inputSource, Origin origin) {
      XmlDocument metadataXml = MappingReader.INSTANCE.readMappingDocument(this.entityResolver, inputSource, origin);
      this.add(metadataXml);
      return metadataXml;
   }

   public void add(XmlDocument metadataXml) {
      if (!this.inSecondPass && isOrmXml(metadataXml)) {
         MetadataProvider metadataProvider = ((MetadataProviderInjector)this.reflectionManager).getMetadataProvider();
         JPAMetadataProvider jpaMetadataProvider = (JPAMetadataProvider)metadataProvider;

         for(String className : jpaMetadataProvider.getXMLContext().addDocument(metadataXml.getDocumentTree())) {
            try {
               this.metadataSourceQueue.add(this.reflectionManager.classForName(className, this.getClass()));
            } catch (ClassNotFoundException e) {
               throw new AnnotationException("Unable to load class defined in XML: " + className, e);
            }
         }
      } else {
         this.metadataSourceQueue.add(metadataXml);
      }

   }

   private static boolean isOrmXml(XmlDocument xmlDocument) {
      return "entity-mappings".equals(xmlDocument.getDocumentTree().getRootElement().getName());
   }

   public Configuration addCacheableFile(File xmlFile) throws MappingException {
      File cachedFile = this.determineCachedDomFile(xmlFile);

      try {
         return this.addCacheableFileStrictly(xmlFile);
      } catch (SerializationException e) {
         LOG.unableToDeserializeCache(cachedFile.getPath(), e);
      } catch (FileNotFoundException e) {
         LOG.cachedFileNotFound(cachedFile.getPath(), e);
      }

      String name = xmlFile.getAbsolutePath();

      InputSource inputSource;
      try {
         inputSource = new InputSource(new FileInputStream(xmlFile));
      } catch (FileNotFoundException var8) {
         throw new MappingNotFoundException("file", xmlFile.toString());
      }

      LOG.readingMappingsFromFile(xmlFile.getPath());
      XmlDocument metadataXml = this.add(inputSource, "file", name);

      try {
         LOG.debugf("Writing cache file for: %s to: %s", xmlFile, cachedFile);
         SerializationHelper.serialize((Serializable)metadataXml.getDocumentTree(), new FileOutputStream(cachedFile));
      } catch (Exception e) {
         LOG.unableToWriteCachedFile(cachedFile.getPath(), e.getMessage());
      }

      return this;
   }

   private File determineCachedDomFile(File xmlFile) {
      return new File(xmlFile.getAbsolutePath() + ".bin");
   }

   public Configuration addCacheableFileStrictly(File xmlFile) throws SerializationException, FileNotFoundException {
      File cachedFile = this.determineCachedDomFile(xmlFile);
      boolean useCachedFile = xmlFile.exists() && cachedFile.exists() && xmlFile.lastModified() < cachedFile.lastModified();
      if (!useCachedFile) {
         throw new FileNotFoundException("Cached file could not be found or could not be used");
      } else {
         LOG.readingCachedMappings(cachedFile);
         Document document = (Document)SerializationHelper.deserialize((InputStream)(new FileInputStream(cachedFile)));
         this.add(new XmlDocumentImpl(document, "file", xmlFile.getAbsolutePath()));
         return this;
      }
   }

   public Configuration addCacheableFile(String xmlFile) throws MappingException {
      return this.addCacheableFile(new File(xmlFile));
   }

   public Configuration addXML(String xml) throws MappingException {
      LOG.debugf("Mapping XML:\n%s", xml);
      InputSource inputSource = new InputSource(new StringReader(xml));
      this.add(inputSource, "string", "XML String");
      return this;
   }

   public Configuration addURL(URL url) throws MappingException {
      String urlExternalForm = url.toExternalForm();
      LOG.debugf("Reading mapping document from URL : %s", urlExternalForm);

      try {
         this.add(url.openStream(), "URL", urlExternalForm);
         return this;
      } catch (IOException e) {
         throw new InvalidMappingException("Unable to open url stream [" + urlExternalForm + "]", "URL", urlExternalForm, e);
      }
   }

   private XmlDocument add(InputStream inputStream, String type, String name) {
      InputSource inputSource = new InputSource(inputStream);

      XmlDocument var5;
      try {
         var5 = this.add(inputSource, type, name);
      } finally {
         try {
            inputStream.close();
         } catch (IOException var12) {
            LOG.trace("Was unable to close input stream");
         }

      }

      return var5;
   }

   public Configuration addDocument(org.w3c.dom.Document doc) throws MappingException {
      LOG.debugf("Mapping Document:\n%s", doc);
      Document document = this.xmlHelper.createDOMReader().read(doc);
      this.add(new XmlDocumentImpl(document, "unknown", (String)null));
      return this;
   }

   public Configuration addInputStream(InputStream xmlInputStream) throws MappingException {
      this.add((InputStream)xmlInputStream, "input stream", (String)null);
      return this;
   }

   public Configuration addResource(String resourceName, ClassLoader classLoader) throws MappingException {
      LOG.readingMappingsFromResource(resourceName);
      InputStream resourceInputStream = classLoader.getResourceAsStream(resourceName);
      if (resourceInputStream == null) {
         throw new MappingNotFoundException("resource", resourceName);
      } else {
         this.add(resourceInputStream, "resource", resourceName);
         return this;
      }
   }

   public Configuration addResource(String resourceName) throws MappingException {
      LOG.readingMappingsFromResource(resourceName);
      ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
      InputStream resourceInputStream = null;
      if (contextClassLoader != null) {
         resourceInputStream = contextClassLoader.getResourceAsStream(resourceName);
      }

      if (resourceInputStream == null) {
         resourceInputStream = Environment.class.getClassLoader().getResourceAsStream(resourceName);
      }

      if (resourceInputStream == null) {
         throw new MappingNotFoundException("resource", resourceName);
      } else {
         this.add(resourceInputStream, "resource", resourceName);
         return this;
      }
   }

   public Configuration addClass(Class persistentClass) throws MappingException {
      String mappingResourceName = persistentClass.getName().replace('.', '/') + ".hbm.xml";
      LOG.readingMappingsFromResource(mappingResourceName);
      return this.addResource(mappingResourceName, persistentClass.getClassLoader());
   }

   public Configuration addAnnotatedClass(Class annotatedClass) {
      XClass xClass = this.reflectionManager.toXClass(annotatedClass);
      this.metadataSourceQueue.add(xClass);
      return this;
   }

   public Configuration addPackage(String packageName) throws MappingException {
      LOG.debugf("Mapping Package %s", packageName);

      try {
         AnnotationBinder.bindPackage(packageName, this.createMappings());
         return this;
      } catch (MappingException me) {
         LOG.unableToParseMetadata(packageName);
         throw me;
      }
   }

   public Configuration addJar(File jar) throws MappingException {
      LOG.searchingForMappingDocuments(jar.getName());
      JarFile jarFile = null;

      try {
         try {
            jarFile = new JarFile(jar);
         } catch (IOException ioe) {
            throw new InvalidMappingException("Could not read mapping documents from jar: " + jar.getName(), "jar", jar.getName(), ioe);
         }

         Enumeration jarEntries = jarFile.entries();

         while(jarEntries.hasMoreElements()) {
            ZipEntry ze = (ZipEntry)jarEntries.nextElement();
            if (ze.getName().endsWith(".hbm.xml")) {
               LOG.foundMappingDocument(ze.getName());

               try {
                  this.addInputStream(jarFile.getInputStream(ze));
               } catch (Exception e) {
                  throw new InvalidMappingException("Could not read mapping documents from jar: " + jar.getName(), "jar", jar.getName(), e);
               }
            }
         }
      } finally {
         try {
            if (jarFile != null) {
               jarFile.close();
            }
         } catch (IOException ioe) {
            LOG.unableToCloseJar(ioe.getMessage());
         }

      }

      return this;
   }

   public Configuration addDirectory(File dir) throws MappingException {
      File[] files = dir.listFiles();

      for(File file : files) {
         if (file.isDirectory()) {
            this.addDirectory(file);
         } else if (file.getName().endsWith(".hbm.xml")) {
            this.addFile(file);
         }
      }

      return this;
   }

   public Mappings createMappings() {
      return new MappingsImpl();
   }

   private Iterator iterateGenerators(Dialect dialect) throws MappingException {
      TreeMap generators = new TreeMap();
      String defaultCatalog = this.properties.getProperty("hibernate.default_catalog");
      String defaultSchema = this.properties.getProperty("hibernate.default_schema");

      for(PersistentClass pc : this.classes.values()) {
         if (!pc.isInherited()) {
            IdentifierGenerator ig = pc.getIdentifier().createIdentifierGenerator(this.getIdentifierGeneratorFactory(), dialect, defaultCatalog, defaultSchema, (RootClass)pc);
            if (ig instanceof PersistentIdentifierGenerator) {
               generators.put(((PersistentIdentifierGenerator)ig).generatorKey(), ig);
            } else if (ig instanceof IdentifierGeneratorAggregator) {
               ((IdentifierGeneratorAggregator)ig).registerPersistentGenerators(generators);
            }
         }
      }

      for(Collection collection : this.collections.values()) {
         if (collection.isIdentified()) {
            IdentifierGenerator ig = ((IdentifierCollection)collection).getIdentifier().createIdentifierGenerator(this.getIdentifierGeneratorFactory(), dialect, defaultCatalog, defaultSchema, (RootClass)null);
            if (ig instanceof PersistentIdentifierGenerator) {
               generators.put(((PersistentIdentifierGenerator)ig).generatorKey(), ig);
            }
         }
      }

      return generators.values().iterator();
   }

   public String[] generateDropSchemaScript(Dialect dialect) throws HibernateException {
      this.secondPassCompile();
      String defaultCatalog = this.properties.getProperty("hibernate.default_catalog");
      String defaultSchema = this.properties.getProperty("hibernate.default_schema");
      ArrayList<String> script = new ArrayList(50);
      ListIterator itr = this.auxiliaryDatabaseObjects.listIterator(this.auxiliaryDatabaseObjects.size());

      while(itr.hasPrevious()) {
         AuxiliaryDatabaseObject object = (AuxiliaryDatabaseObject)itr.previous();
         if (object.appliesToDialect(dialect)) {
            script.add(object.sqlDropString(dialect, defaultCatalog, defaultSchema));
         }
      }

      if (dialect.dropConstraints()) {
         Iterator itr = this.getTableMappings();

         while(itr.hasNext()) {
            Table table = (Table)itr.next();
            if (table.isPhysicalTable()) {
               Iterator subItr = table.getForeignKeyIterator();

               while(subItr.hasNext()) {
                  ForeignKey fk = (ForeignKey)subItr.next();
                  if (fk.isPhysicalConstraint()) {
                     script.add(fk.sqlDropString(dialect, defaultCatalog, defaultSchema));
                  }
               }
            }
         }
      }

      Iterator itr = this.getTableMappings();

      while(itr.hasNext()) {
         Table table = (Table)itr.next();
         if (table.isPhysicalTable()) {
            script.add(table.sqlDropString(dialect, defaultCatalog, defaultSchema));
         }
      }

      itr = this.iterateGenerators(dialect);

      while(itr.hasNext()) {
         String[] lines = ((PersistentIdentifierGenerator)itr.next()).sqlDropStrings(dialect);
         script.addAll(Arrays.asList(lines));
      }

      return ArrayHelper.toStringArray((java.util.Collection)script);
   }

   public String[] generateSchemaCreationScript(Dialect dialect) throws HibernateException {
      this.secondPassCompile();
      ArrayList<String> script = new ArrayList(50);
      String defaultCatalog = this.properties.getProperty("hibernate.default_catalog");
      String defaultSchema = this.properties.getProperty("hibernate.default_schema");
      Iterator iter = this.getTableMappings();

      while(iter.hasNext()) {
         Table table = (Table)iter.next();
         if (table.isPhysicalTable()) {
            script.add(table.sqlCreateString(dialect, this.mapping, defaultCatalog, defaultSchema));
            Iterator<String> comments = table.sqlCommentStrings(dialect, defaultCatalog, defaultSchema);

            while(comments.hasNext()) {
               script.add(comments.next());
            }
         }
      }

      iter = this.getTableMappings();

      while(iter.hasNext()) {
         Table table = (Table)iter.next();
         if (table.isPhysicalTable()) {
            Iterator subIter = table.getUniqueKeyIterator();

            while(subIter.hasNext()) {
               UniqueKey uk = (UniqueKey)subIter.next();
               String constraintString = uk.sqlCreateString(dialect, this.mapping, defaultCatalog, defaultSchema);
               if (constraintString != null) {
                  script.add(constraintString);
               }
            }

            subIter = table.getIndexIterator();

            while(subIter.hasNext()) {
               Index index = (Index)subIter.next();
               script.add(index.sqlCreateString(dialect, this.mapping, defaultCatalog, defaultSchema));
            }

            if (dialect.hasAlterTable()) {
               subIter = table.getForeignKeyIterator();

               while(subIter.hasNext()) {
                  ForeignKey fk = (ForeignKey)subIter.next();
                  if (fk.isPhysicalConstraint()) {
                     script.add(fk.sqlCreateString(dialect, this.mapping, defaultCatalog, defaultSchema));
                  }
               }
            }
         }
      }

      iter = this.iterateGenerators(dialect);

      while(iter.hasNext()) {
         String[] lines = ((PersistentIdentifierGenerator)iter.next()).sqlCreateStrings(dialect);
         script.addAll(Arrays.asList(lines));
      }

      for(AuxiliaryDatabaseObject auxiliaryDatabaseObject : this.auxiliaryDatabaseObjects) {
         if (auxiliaryDatabaseObject.appliesToDialect(dialect)) {
            script.add(auxiliaryDatabaseObject.sqlCreateString(dialect, this.mapping, defaultCatalog, defaultSchema));
         }
      }

      return ArrayHelper.toStringArray((java.util.Collection)script);
   }

   public String[] generateSchemaUpdateScript(Dialect dialect, DatabaseMetadata databaseMetadata) throws HibernateException {
      this.secondPassCompile();
      String defaultCatalog = this.properties.getProperty("hibernate.default_catalog");
      String defaultSchema = this.properties.getProperty("hibernate.default_schema");
      ArrayList<String> script = new ArrayList(50);
      Iterator iter = this.getTableMappings();

      while(iter.hasNext()) {
         Table table = (Table)iter.next();
         String tableSchema = table.getSchema() == null ? defaultSchema : table.getSchema();
         String tableCatalog = table.getCatalog() == null ? defaultCatalog : table.getCatalog();
         if (table.isPhysicalTable()) {
            TableMetadata tableInfo = databaseMetadata.getTableMetadata(table.getName(), tableSchema, tableCatalog, table.isQuoted());
            if (tableInfo == null) {
               script.add(table.sqlCreateString(dialect, this.mapping, tableCatalog, tableSchema));
            } else {
               Iterator<String> subiter = table.sqlAlterStrings(dialect, this.mapping, tableInfo, tableCatalog, tableSchema);

               while(subiter.hasNext()) {
                  script.add(subiter.next());
               }
            }

            Iterator<String> comments = table.sqlCommentStrings(dialect, defaultCatalog, defaultSchema);

            while(comments.hasNext()) {
               script.add(comments.next());
            }
         }
      }

      iter = this.getTableMappings();

      while(iter.hasNext()) {
         Table table = (Table)iter.next();
         String tableSchema = table.getSchema() == null ? defaultSchema : table.getSchema();
         String tableCatalog = table.getCatalog() == null ? defaultCatalog : table.getCatalog();
         if (table.isPhysicalTable()) {
            TableMetadata tableInfo = databaseMetadata.getTableMetadata(table.getName(), tableSchema, tableCatalog, table.isQuoted());
            if (dialect.hasAlterTable()) {
               Iterator subIter = table.getForeignKeyIterator();

               while(subIter.hasNext()) {
                  ForeignKey fk = (ForeignKey)subIter.next();
                  if (fk.isPhysicalConstraint()) {
                     boolean create = tableInfo == null || tableInfo.getForeignKeyMetadata(fk) == null && (!(dialect instanceof MySQLDialect) || tableInfo.getIndexMetadata(fk.getName()) == null);
                     if (create) {
                        script.add(fk.sqlCreateString(dialect, this.mapping, tableCatalog, tableSchema));
                     }
                  }
               }
            }

            Iterator subIter = table.getIndexIterator();

            while(subIter.hasNext()) {
               Index index = (Index)subIter.next();
               if (tableInfo != null && StringHelper.isNotEmpty(index.getName())) {
                  IndexMetadata meta = tableInfo.getIndexMetadata(index.getName());
                  if (meta != null) {
                     continue;
                  }
               }

               script.add(index.sqlCreateString(dialect, this.mapping, tableCatalog, tableSchema));
            }
         }
      }

      iter = this.iterateGenerators(dialect);

      while(iter.hasNext()) {
         PersistentIdentifierGenerator generator = (PersistentIdentifierGenerator)iter.next();
         Object key = generator.generatorKey();
         if (!databaseMetadata.isSequence(key) && !databaseMetadata.isTable(key)) {
            String[] lines = generator.sqlCreateStrings(dialect);
            script.addAll(Arrays.asList(lines));
         }
      }

      return ArrayHelper.toStringArray((java.util.Collection)script);
   }

   public void validateSchema(Dialect dialect, DatabaseMetadata databaseMetadata) throws HibernateException {
      this.secondPassCompile();
      String defaultCatalog = this.properties.getProperty("hibernate.default_catalog");
      String defaultSchema = this.properties.getProperty("hibernate.default_schema");
      Iterator iter = this.getTableMappings();

      while(iter.hasNext()) {
         Table table = (Table)iter.next();
         if (table.isPhysicalTable()) {
            TableMetadata tableInfo = databaseMetadata.getTableMetadata(table.getName(), table.getSchema() == null ? defaultSchema : table.getSchema(), table.getCatalog() == null ? defaultCatalog : table.getCatalog(), table.isQuoted());
            if (tableInfo == null) {
               throw new HibernateException("Missing table: " + table.getName());
            }

            table.validateColumns(dialect, this.mapping, tableInfo);
         }
      }

      iter = this.iterateGenerators(dialect);

      while(iter.hasNext()) {
         PersistentIdentifierGenerator generator = (PersistentIdentifierGenerator)iter.next();
         Object key = generator.generatorKey();
         if (!databaseMetadata.isSequence(key) && !databaseMetadata.isTable(key)) {
            throw new HibernateException("Missing sequence or table: " + key);
         }
      }

   }

   private void validate() throws MappingException {
      Iterator iter = this.classes.values().iterator();

      while(iter.hasNext()) {
         ((PersistentClass)iter.next()).validate(this.mapping);
      }

      iter = this.collections.values().iterator();

      while(iter.hasNext()) {
         ((Collection)iter.next()).validate(this.mapping);
      }

   }

   public void buildMappings() {
      this.secondPassCompile();
   }

   protected void secondPassCompile() throws MappingException {
      LOG.trace("Starting secondPassCompile() processing");
      if (!this.isDefaultProcessed) {
         Map defaults = this.reflectionManager.getDefaults();
         Object isDelimited = defaults.get("delimited-identifier");
         if (isDelimited != null && isDelimited == Boolean.TRUE) {
            this.getProperties().put("hibernate.globally_quoted_identifiers", "true");
         }

         String schema = (String)defaults.get("schema");
         if (StringHelper.isNotEmpty(schema)) {
            this.getProperties().put("hibernate.default_schema", schema);
         }

         String catalog = (String)defaults.get("catalog");
         if (StringHelper.isNotEmpty(catalog)) {
            this.getProperties().put("hibernate.default_catalog", catalog);
         }

         AnnotationBinder.bindDefaults(this.createMappings());
         this.isDefaultProcessed = true;
      }

      this.metadataSourceQueue.syncAnnotatedClasses();
      this.metadataSourceQueue.processMetadata(this.determineMetadataSourcePrecedence());

      try {
         this.inSecondPass = true;
         this.processSecondPassesOfType(PkDrivenByDefaultMapsIdSecondPass.class);
         this.processSecondPassesOfType(SetSimpleValueTypeSecondPass.class);
         this.processSecondPassesOfType(CopyIdentifierComponentSecondPass.class);
         this.processFkSecondPassInOrder();
         this.processSecondPassesOfType(CreateKeySecondPass.class);
         this.processSecondPassesOfType(SecondaryTableSecondPass.class);
         this.originalSecondPassCompile();
         this.inSecondPass = false;
      } catch (RecoverableException e) {
         throw (RuntimeException)e.getCause();
      }

      for(CacheHolder holder : this.caches) {
         if (holder.isClass) {
            this.applyCacheConcurrencyStrategy(holder);
         } else {
            this.applyCollectionCacheConcurrencyStrategy(holder);
         }
      }

      this.caches.clear();

      for(Map.Entry tableListEntry : this.uniqueConstraintHoldersByTable.entrySet()) {
         Table table = (Table)tableListEntry.getKey();
         List<UniqueConstraintHolder> uniqueConstraints = (List)tableListEntry.getValue();
         int uniqueIndexPerTable = 0;

         for(UniqueConstraintHolder holder : uniqueConstraints) {
            ++uniqueIndexPerTable;
            String keyName = StringHelper.isEmpty(holder.getName()) ? "key" + uniqueIndexPerTable : holder.getName();
            this.buildUniqueKeyFromColumnNames(table, keyName, holder.getColumns());
         }
      }

   }

   private void processSecondPassesOfType(Class type) {
      Iterator iter = this.secondPasses.iterator();

      while(iter.hasNext()) {
         SecondPass sp = (SecondPass)iter.next();
         if (type.isInstance(sp)) {
            sp.doSecondPass(this.classes);
            iter.remove();
         }
      }

   }

   private void processFkSecondPassInOrder() {
      LOG.debug("Processing fk mappings (*ToOne and JoinedSubclass)");
      List<FkSecondPass> fkSecondPasses = this.getFKSecondPassesOnly();
      if (fkSecondPasses.size() != 0) {
         Map<String, Set<FkSecondPass>> isADependencyOf = new HashMap();
         List<FkSecondPass> endOfQueueFkSecondPasses = new ArrayList(fkSecondPasses.size());

         for(FkSecondPass sp : fkSecondPasses) {
            if (sp.isInPrimaryKey()) {
               String referenceEntityName = sp.getReferencedEntityName();
               PersistentClass classMapping = this.getClassMapping(referenceEntityName);
               String dependentTable = this.quotedTableName(classMapping.getTable());
               if (!isADependencyOf.containsKey(dependentTable)) {
                  isADependencyOf.put(dependentTable, new HashSet());
               }

               ((Set)isADependencyOf.get(dependentTable)).add(sp);
            } else {
               endOfQueueFkSecondPasses.add(sp);
            }
         }

         List<FkSecondPass> orderedFkSecondPasses = new ArrayList(fkSecondPasses.size());

         for(String tableName : isADependencyOf.keySet()) {
            this.buildRecursiveOrderedFkSecondPasses(orderedFkSecondPasses, isADependencyOf, tableName, tableName);
         }

         for(FkSecondPass sp : orderedFkSecondPasses) {
            sp.doSecondPass(this.classes);
         }

         this.processEndOfQueue(endOfQueueFkSecondPasses);
      }
   }

   private List getFKSecondPassesOnly() {
      Iterator iter = this.secondPasses.iterator();
      List<FkSecondPass> fkSecondPasses = new ArrayList(this.secondPasses.size());

      while(iter.hasNext()) {
         SecondPass sp = (SecondPass)iter.next();
         if (sp instanceof FkSecondPass) {
            fkSecondPasses.add((FkSecondPass)sp);
            iter.remove();
         }
      }

      return fkSecondPasses;
   }

   private void buildRecursiveOrderedFkSecondPasses(List orderedFkSecondPasses, Map isADependencyOf, String startTable, String currentTable) {
      Set<FkSecondPass> dependencies = (Set)isADependencyOf.get(currentTable);
      if (dependencies != null && dependencies.size() != 0) {
         for(FkSecondPass sp : dependencies) {
            String dependentTable = this.quotedTableName(sp.getValue().getTable());
            if (dependentTable.compareTo(startTable) == 0) {
               StringBuilder sb = new StringBuilder("Foreign key circularity dependency involving the following tables: ");
               throw new AnnotationException(sb.toString());
            }

            this.buildRecursiveOrderedFkSecondPasses(orderedFkSecondPasses, isADependencyOf, startTable, dependentTable);
            if (!orderedFkSecondPasses.contains(sp)) {
               orderedFkSecondPasses.add(0, sp);
            }
         }

      }
   }

   private String quotedTableName(Table table) {
      return Table.qualify(table.getCatalog(), table.getQuotedSchema(), table.getQuotedName());
   }

   private void processEndOfQueue(List endOfQueueFkSecondPasses) {
      boolean stopProcess = false;

      RuntimeException originalException;
      List<FkSecondPass> failingSecondPasses;
      for(originalException = null; !stopProcess; endOfQueueFkSecondPasses = failingSecondPasses) {
         failingSecondPasses = new ArrayList();

         for(FkSecondPass pass : endOfQueueFkSecondPasses) {
            try {
               pass.doSecondPass(this.classes);
            } catch (RecoverableException e) {
               failingSecondPasses.add(pass);
               if (originalException == null) {
                  originalException = (RuntimeException)e.getCause();
               }
            }
         }

         stopProcess = failingSecondPasses.size() == 0 || failingSecondPasses.size() == endOfQueueFkSecondPasses.size();
      }

      if (endOfQueueFkSecondPasses.size() > 0) {
         throw originalException;
      }
   }

   private void buildUniqueKeyFromColumnNames(Table table, String keyName, String[] columnNames) {
      keyName = this.normalizer.normalizeIdentifierQuoting(keyName);
      int size = columnNames.length;
      Column[] columns = new Column[size];
      Set<Column> unbound = new HashSet();
      Set<Column> unboundNoLogical = new HashSet();

      for(int index = 0; index < size; ++index) {
         String column = columnNames[index];
         String logicalColumnName = StringHelper.isNotEmpty(column) ? this.normalizer.normalizeIdentifierQuoting(column) : "";

         try {
            String columnName = this.createMappings().getPhysicalColumnName(logicalColumnName, table);
            columns[index] = new Column(columnName);
            unbound.add(columns[index]);
         } catch (MappingException var14) {
            unboundNoLogical.add(new Column(logicalColumnName));
         }
      }

      UniqueKey uk = table.getOrCreateUniqueKey(keyName);

      for(Column column : columns) {
         if (table.containsColumn(column)) {
            uk.addColumn(column);
            unbound.remove(column);
         }
      }

      if (unbound.size() > 0 || unboundNoLogical.size() > 0) {
         StringBuilder sb = new StringBuilder("Unable to create unique key constraint (");

         for(String columnName : columnNames) {
            sb.append(columnName).append(", ");
         }

         sb.setLength(sb.length() - 2);
         sb.append(") on table ").append(table.getName()).append(": database column ");

         for(Column column : unbound) {
            sb.append("'").append(column.getName()).append("', ");
         }

         for(Column column : unboundNoLogical) {
            sb.append("'").append(column.getName()).append("', ");
         }

         sb.setLength(sb.length() - 2);
         sb.append(" not found. Make sure that you use the correct column name which depends on the naming strategy in use (it may not be the same as the property name in the entity, especially for relational types)");
         throw new AnnotationException(sb.toString());
      }
   }

   private void originalSecondPassCompile() throws MappingException {
      LOG.debug("Processing extends queue");
      this.processExtendsQueue();
      LOG.debug("Processing collection mappings");
      Iterator itr = this.secondPasses.iterator();

      while(itr.hasNext()) {
         SecondPass sp = (SecondPass)itr.next();
         if (!(sp instanceof QuerySecondPass)) {
            sp.doSecondPass(this.classes);
            itr.remove();
         }
      }

      LOG.debug("Processing native query and ResultSetMapping mappings");
      itr = this.secondPasses.iterator();

      while(itr.hasNext()) {
         SecondPass sp = (SecondPass)itr.next();
         sp.doSecondPass(this.classes);
         itr.remove();
      }

      LOG.debug("Processing association property references");

      for(Mappings.PropertyReference upr : this.propertyReferences) {
         PersistentClass clazz = this.getClassMapping(upr.referencedClass);
         if (clazz == null) {
            throw new MappingException("property-ref to unmapped class: " + upr.referencedClass);
         }

         Property prop = clazz.getReferencedProperty(upr.propertyName);
         if (upr.unique) {
            ((SimpleValue)prop.getValue()).setAlternateUniqueKey(true);
         }
      }

      LOG.debug("Creating tables' unique integer identifiers");
      LOG.debug("Processing foreign key constraints");
      itr = this.getTableMappings();
      int uniqueInteger = 0;
      Set<ForeignKey> done = new HashSet();

      while(itr.hasNext()) {
         Table table = (Table)itr.next();
         table.setUniqueInteger(uniqueInteger++);
         this.secondPassCompileForeignKeys(table, done);
      }

   }

   private int processExtendsQueue() {
      LOG.debug("Processing extends queue");
      int added = 0;

      for(ExtendsQueueEntry extendsQueueEntry = this.findPossibleExtends(); extendsQueueEntry != null; extendsQueueEntry = this.findPossibleExtends()) {
         this.metadataSourceQueue.processHbmXml(extendsQueueEntry.getMetadataXml(), extendsQueueEntry.getEntityNames());
      }

      if (this.extendsQueue.size() > 0) {
         Iterator iterator = this.extendsQueue.keySet().iterator();
         StringBuilder buf = new StringBuilder("Following super classes referenced in extends not found: ");

         while(iterator.hasNext()) {
            ExtendsQueueEntry entry = (ExtendsQueueEntry)iterator.next();
            buf.append(entry.getExplicitName());
            if (entry.getMappingPackage() != null) {
               buf.append("[").append(entry.getMappingPackage()).append("]");
            }

            if (iterator.hasNext()) {
               buf.append(",");
            }
         }

         throw new MappingException(buf.toString());
      } else {
         return added;
      }
   }

   protected ExtendsQueueEntry findPossibleExtends() {
      Iterator<ExtendsQueueEntry> itr = this.extendsQueue.keySet().iterator();

      while(itr.hasNext()) {
         ExtendsQueueEntry entry = (ExtendsQueueEntry)itr.next();
         boolean found = this.getClassMapping(entry.getExplicitName()) != null || this.getClassMapping(HbmBinder.getClassName(entry.getExplicitName(), entry.getMappingPackage())) != null;
         if (found) {
            itr.remove();
            return entry;
         }
      }

      return null;
   }

   protected void secondPassCompileForeignKeys(Table table, Set done) throws MappingException {
      table.createForeignKeys();
      Iterator iter = table.getForeignKeyIterator();

      while(iter.hasNext()) {
         ForeignKey fk = (ForeignKey)iter.next();
         if (!done.contains(fk)) {
            done.add(fk);
            String referencedEntityName = fk.getReferencedEntityName();
            if (referencedEntityName == null) {
               throw new MappingException("An association from the table " + fk.getTable().getName() + " does not specify the referenced entity");
            }

            LOG.debugf("Resolving reference to class: %s", referencedEntityName);
            PersistentClass referencedClass = (PersistentClass)this.classes.get(referencedEntityName);
            if (referencedClass == null) {
               throw new MappingException("An association from the table " + fk.getTable().getName() + " refers to an unmapped class: " + referencedEntityName);
            }

            if (referencedClass.isJoinedSubclass()) {
               this.secondPassCompileForeignKeys(referencedClass.getSuperclass().getTable(), done);
            }

            fk.setReferencedTable(referencedClass.getTable());
            fk.alignColumns();
         }
      }

   }

   public Map getNamedQueries() {
      return this.namedQueries;
   }

   public SessionFactory buildSessionFactory(ServiceRegistry serviceRegistry) throws HibernateException {
      LOG.debugf("Preparing to build session factory with filters : %s", this.filterDefinitions);
      this.secondPassCompile();
      if (!this.metadataSourceQueue.isEmpty()) {
         LOG.incompleteMappingMetadataCacheProcessing();
      }

      this.validate();
      Environment.verifyProperties(this.properties);
      Properties copy = new Properties();
      copy.putAll(this.properties);
      ConfigurationHelper.resolvePlaceHolders(copy);
      Settings settings = this.buildSettings(copy, serviceRegistry);
      return new SessionFactoryImpl(this, this.mapping, serviceRegistry, settings, this.sessionFactoryObserver);
   }

   /** @deprecated */
   public SessionFactory buildSessionFactory() throws HibernateException {
      Environment.verifyProperties(this.properties);
      ConfigurationHelper.resolvePlaceHolders(this.properties);
      final ServiceRegistry serviceRegistry = (new ServiceRegistryBuilder()).applySettings(this.properties).buildServiceRegistry();
      this.setSessionFactoryObserver(new SessionFactoryObserver() {
         public void sessionFactoryCreated(SessionFactory factory) {
         }

         public void sessionFactoryClosed(SessionFactory factory) {
            ((StandardServiceRegistryImpl)serviceRegistry).destroy();
         }
      });
      return this.buildSessionFactory(serviceRegistry);
   }

   public Interceptor getInterceptor() {
      return this.interceptor;
   }

   public Configuration setInterceptor(Interceptor interceptor) {
      this.interceptor = interceptor;
      return this;
   }

   public Properties getProperties() {
      return this.properties;
   }

   public String getProperty(String propertyName) {
      return this.properties.getProperty(propertyName);
   }

   public Configuration setProperties(Properties properties) {
      this.properties = properties;
      return this;
   }

   public Configuration addProperties(Properties extraProperties) {
      this.properties.putAll(extraProperties);
      return this;
   }

   public Configuration mergeProperties(Properties properties) {
      for(Map.Entry entry : properties.entrySet()) {
         if (!this.properties.containsKey(entry.getKey())) {
            this.properties.setProperty((String)entry.getKey(), (String)entry.getValue());
         }
      }

      return this;
   }

   public Configuration setProperty(String propertyName, String value) {
      this.properties.setProperty(propertyName, value);
      return this;
   }

   private void addProperties(Element parent) {
      Iterator itr = parent.elementIterator("property");

      while(itr.hasNext()) {
         Element node = (Element)itr.next();
         String name = node.attributeValue("name");
         String value = node.getText().trim();
         LOG.debugf("%s=%s", name, value);
         this.properties.setProperty(name, value);
         if (!name.startsWith("hibernate")) {
            this.properties.setProperty("hibernate." + name, value);
         }
      }

      Environment.verifyProperties(this.properties);
   }

   public Configuration configure() throws HibernateException {
      this.configure("/hibernate.cfg.xml");
      return this;
   }

   public Configuration configure(String resource) throws HibernateException {
      LOG.configuringFromResource(resource);
      InputStream stream = this.getConfigurationInputStream(resource);
      return this.doConfigure(stream, resource);
   }

   protected InputStream getConfigurationInputStream(String resource) throws HibernateException {
      LOG.configurationResource(resource);
      return ConfigHelper.getResourceAsStream(resource);
   }

   public Configuration configure(URL url) throws HibernateException {
      LOG.configuringFromUrl(url);

      try {
         return this.doConfigure(url.openStream(), url.toString());
      } catch (IOException ioe) {
         throw new HibernateException("could not configure from URL: " + url, ioe);
      }
   }

   public Configuration configure(File configFile) throws HibernateException {
      LOG.configuringFromFile(configFile.getName());

      try {
         return this.doConfigure(new FileInputStream(configFile), configFile.toString());
      } catch (FileNotFoundException fnfe) {
         throw new HibernateException("could not find file: " + configFile, fnfe);
      }
   }

   protected Configuration doConfigure(InputStream stream, String resourceName) throws HibernateException {
      try {
         ErrorLogger errorLogger = new ErrorLogger(resourceName);
         Document document = this.xmlHelper.createSAXReader(errorLogger, this.entityResolver).read(new InputSource(stream));
         if (errorLogger.hasErrors()) {
            throw new MappingException("invalid configuration", (Throwable)errorLogger.getErrors().get(0));
         }

         this.doConfigure(document);
      } catch (DocumentException e) {
         throw new HibernateException("Could not parse configuration: " + resourceName, e);
      } finally {
         try {
            stream.close();
         } catch (IOException ioe) {
            LOG.unableToCloseInputStreamForResource(resourceName, ioe);
         }

      }

      return this;
   }

   public Configuration configure(org.w3c.dom.Document document) throws HibernateException {
      LOG.configuringFromXmlDocument();
      return this.doConfigure(this.xmlHelper.createDOMReader().read(document));
   }

   protected Configuration doConfigure(Document doc) throws HibernateException {
      Element sfNode = doc.getRootElement().element("session-factory");
      String name = sfNode.attributeValue("name");
      if (name != null) {
         this.properties.setProperty("hibernate.session_factory_name", name);
      }

      this.addProperties(sfNode);
      this.parseSessionFactory(sfNode, name);
      Element secNode = doc.getRootElement().element("security");
      if (secNode != null) {
         this.parseSecurity(secNode);
      }

      LOG.configuredSessionFactory(name);
      LOG.debugf("Properties: %s", this.properties);
      return this;
   }

   private void parseSessionFactory(Element sfNode, String name) {
      Iterator elements = sfNode.elementIterator();

      while(elements.hasNext()) {
         Element subelement = (Element)elements.next();
         String subelementName = subelement.getName();
         if ("mapping".equals(subelementName)) {
            this.parseMappingElement(subelement, name);
         } else if ("class-cache".equals(subelementName)) {
            String className = subelement.attributeValue("class");
            Attribute regionNode = subelement.attribute("region");
            String region = regionNode == null ? className : regionNode.getValue();
            boolean includeLazy = !"non-lazy".equals(subelement.attributeValue("include"));
            this.setCacheConcurrencyStrategy(className, subelement.attributeValue("usage"), region, includeLazy);
         } else if ("collection-cache".equals(subelementName)) {
            String role = subelement.attributeValue("collection");
            Attribute regionNode = subelement.attribute("region");
            String region = regionNode == null ? role : regionNode.getValue();
            this.setCollectionCacheConcurrencyStrategy(role, subelement.attributeValue("usage"), region);
         }
      }

   }

   private void parseMappingElement(Element mappingElement, String name) {
      Attribute resourceAttribute = mappingElement.attribute("resource");
      Attribute fileAttribute = mappingElement.attribute("file");
      Attribute jarAttribute = mappingElement.attribute("jar");
      Attribute packageAttribute = mappingElement.attribute("package");
      Attribute classAttribute = mappingElement.attribute("class");
      if (resourceAttribute != null) {
         String resourceName = resourceAttribute.getValue();
         LOG.debugf("Session-factory config [%s] named resource [%s] for mapping", name, resourceName);
         this.addResource(resourceName);
      } else if (fileAttribute != null) {
         String fileName = fileAttribute.getValue();
         LOG.debugf("Session-factory config [%s] named file [%s] for mapping", name, fileName);
         this.addFile(fileName);
      } else if (jarAttribute != null) {
         String jarFileName = jarAttribute.getValue();
         LOG.debugf("Session-factory config [%s] named jar file [%s] for mapping", name, jarFileName);
         this.addJar(new File(jarFileName));
      } else if (packageAttribute != null) {
         String packageName = packageAttribute.getValue();
         LOG.debugf("Session-factory config [%s] named package [%s] for mapping", name, packageName);
         this.addPackage(packageName);
      } else {
         if (classAttribute == null) {
            throw new MappingException("<mapping> element in configuration specifies no known attributes");
         }

         String className = classAttribute.getValue();
         LOG.debugf("Session-factory config [%s] named class [%s] for mapping", name, className);

         try {
            this.addAnnotatedClass(ReflectHelper.classForName(className));
         } catch (Exception e) {
            throw new MappingException("Unable to load class [ " + className + "] declared in Hibernate configuration <mapping/> entry", e);
         }
      }

   }

   private void parseSecurity(Element secNode) {
      String contextId = secNode.attributeValue("context");
      this.setProperty("hibernate.jacc_context_id", contextId);
      LOG.jaccContextId(contextId);
      JACCConfiguration jcfg = new JACCConfiguration(contextId);
      Iterator grantElements = secNode.elementIterator();

      while(grantElements.hasNext()) {
         Element grantElement = (Element)grantElements.next();
         String elementName = grantElement.getName();
         if ("grant".equals(elementName)) {
            jcfg.addPermission(grantElement.attributeValue("role"), grantElement.attributeValue("entity-name"), grantElement.attributeValue("actions"));
         }
      }

   }

   RootClass getRootClassMapping(String clazz) throws MappingException {
      try {
         return (RootClass)this.getClassMapping(clazz);
      } catch (ClassCastException var3) {
         throw new MappingException("You may only specify a cache for root <class> mappings");
      }
   }

   public Configuration setCacheConcurrencyStrategy(String entityName, String concurrencyStrategy) {
      this.setCacheConcurrencyStrategy(entityName, concurrencyStrategy, entityName);
      return this;
   }

   public Configuration setCacheConcurrencyStrategy(String entityName, String concurrencyStrategy, String region) {
      this.setCacheConcurrencyStrategy(entityName, concurrencyStrategy, region, true);
      return this;
   }

   public void setCacheConcurrencyStrategy(String entityName, String concurrencyStrategy, String region, boolean cacheLazyProperty) throws MappingException {
      this.caches.add(new CacheHolder(entityName, concurrencyStrategy, region, true, cacheLazyProperty));
   }

   private void applyCacheConcurrencyStrategy(CacheHolder holder) {
      RootClass rootClass = this.getRootClassMapping(holder.role);
      if (rootClass == null) {
         throw new MappingException("Cannot cache an unknown entity: " + holder.role);
      } else {
         rootClass.setCacheConcurrencyStrategy(holder.usage);
         rootClass.setCacheRegionName(holder.region);
         rootClass.setLazyPropertiesCacheable(holder.cacheLazy);
      }
   }

   public Configuration setCollectionCacheConcurrencyStrategy(String collectionRole, String concurrencyStrategy) {
      this.setCollectionCacheConcurrencyStrategy(collectionRole, concurrencyStrategy, collectionRole);
      return this;
   }

   public void setCollectionCacheConcurrencyStrategy(String collectionRole, String concurrencyStrategy, String region) {
      this.caches.add(new CacheHolder(collectionRole, concurrencyStrategy, region, false, false));
   }

   private void applyCollectionCacheConcurrencyStrategy(CacheHolder holder) {
      Collection collection = this.getCollectionMapping(holder.role);
      if (collection == null) {
         throw new MappingException("Cannot cache an unknown collection: " + holder.role);
      } else {
         collection.setCacheConcurrencyStrategy(holder.usage);
         collection.setCacheRegionName(holder.region);
      }
   }

   public Map getImports() {
      return this.imports;
   }

   public Settings buildSettings(ServiceRegistry serviceRegistry) {
      Properties clone = (Properties)this.properties.clone();
      ConfigurationHelper.resolvePlaceHolders(clone);
      return this.buildSettingsInternal(clone, serviceRegistry);
   }

   public Settings buildSettings(Properties props, ServiceRegistry serviceRegistry) throws HibernateException {
      return this.buildSettingsInternal(props, serviceRegistry);
   }

   private Settings buildSettingsInternal(Properties props, ServiceRegistry serviceRegistry) {
      Settings settings = this.settingsFactory.buildSettings(props, serviceRegistry);
      settings.setEntityTuplizerFactory(this.getEntityTuplizerFactory());
      return settings;
   }

   public Map getNamedSQLQueries() {
      return this.namedSqlQueries;
   }

   public Map getSqlResultSetMappings() {
      return this.sqlResultSetMappings;
   }

   public NamingStrategy getNamingStrategy() {
      return this.namingStrategy;
   }

   public Configuration setNamingStrategy(NamingStrategy namingStrategy) {
      this.namingStrategy = namingStrategy;
      return this;
   }

   public MutableIdentifierGeneratorFactory getIdentifierGeneratorFactory() {
      return this.identifierGeneratorFactory;
   }

   public Mapping buildMapping() {
      return new Mapping() {
         public IdentifierGeneratorFactory getIdentifierGeneratorFactory() {
            return Configuration.this.identifierGeneratorFactory;
         }

         public Type getIdentifierType(String entityName) throws MappingException {
            PersistentClass pc = (PersistentClass)Configuration.this.classes.get(entityName);
            if (pc == null) {
               throw new MappingException("persistent class not known: " + entityName);
            } else {
               return pc.getIdentifier().getType();
            }
         }

         public String getIdentifierPropertyName(String entityName) throws MappingException {
            PersistentClass pc = (PersistentClass)Configuration.this.classes.get(entityName);
            if (pc == null) {
               throw new MappingException("persistent class not known: " + entityName);
            } else {
               return !pc.hasIdentifierProperty() ? null : pc.getIdentifierProperty().getName();
            }
         }

         public Type getReferencedPropertyType(String entityName, String propertyName) throws MappingException {
            PersistentClass pc = (PersistentClass)Configuration.this.classes.get(entityName);
            if (pc == null) {
               throw new MappingException("persistent class not known: " + entityName);
            } else {
               Property prop = pc.getReferencedProperty(propertyName);
               if (prop == null) {
                  throw new MappingException("property not known: " + entityName + '.' + propertyName);
               } else {
                  return prop.getType();
               }
            }
         }
      };
   }

   private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
      MetadataProvider metadataProvider = (MetadataProvider)ois.readObject();
      this.mapping = this.buildMapping();
      this.xmlHelper = new XMLHelper();
      this.createReflectionManager(metadataProvider);
      ois.defaultReadObject();
   }

   private void writeObject(ObjectOutputStream out) throws IOException {
      MetadataProvider metadataProvider = ((MetadataProviderInjector)this.reflectionManager).getMetadataProvider();
      out.writeObject(metadataProvider);
      out.defaultWriteObject();
   }

   private void createReflectionManager() {
      this.createReflectionManager(new JPAMetadataProvider());
   }

   private void createReflectionManager(MetadataProvider metadataProvider) {
      this.reflectionManager = new JavaReflectionManager();
      ((MetadataProviderInjector)this.reflectionManager).setMetadataProvider(metadataProvider);
   }

   public Map getFilterDefinitions() {
      return this.filterDefinitions;
   }

   public void addFilterDefinition(FilterDefinition definition) {
      this.filterDefinitions.put(definition.getFilterName(), definition);
   }

   public Iterator iterateFetchProfiles() {
      return this.fetchProfiles.values().iterator();
   }

   public void addFetchProfile(FetchProfile fetchProfile) {
      this.fetchProfiles.put(fetchProfile.getName(), fetchProfile);
   }

   public void addAuxiliaryDatabaseObject(AuxiliaryDatabaseObject object) {
      this.auxiliaryDatabaseObjects.add(object);
   }

   public Map getSqlFunctions() {
      return this.sqlFunctions;
   }

   public void addSqlFunction(String functionName, SQLFunction function) {
      this.sqlFunctions.put(functionName.toLowerCase(), function);
   }

   public TypeResolver getTypeResolver() {
      return this.typeResolver;
   }

   public void registerTypeOverride(BasicType type) {
      this.getTypeResolver().registerTypeOverride(type);
   }

   public void registerTypeOverride(UserType type, String[] keys) {
      this.getTypeResolver().registerTypeOverride(type, keys);
   }

   public void registerTypeOverride(CompositeUserType type, String[] keys) {
      this.getTypeResolver().registerTypeOverride(type, keys);
   }

   public SessionFactoryObserver getSessionFactoryObserver() {
      return this.sessionFactoryObserver;
   }

   public void setSessionFactoryObserver(SessionFactoryObserver sessionFactoryObserver) {
      this.sessionFactoryObserver = sessionFactoryObserver;
   }

   public CurrentTenantIdentifierResolver getCurrentTenantIdentifierResolver() {
      return this.currentTenantIdentifierResolver;
   }

   public void setCurrentTenantIdentifierResolver(CurrentTenantIdentifierResolver currentTenantIdentifierResolver) {
      this.currentTenantIdentifierResolver = currentTenantIdentifierResolver;
   }

   private List determineMetadataSourcePrecedence() {
      if (this.metadataSourcePrecedence.isEmpty() && StringHelper.isNotEmpty(this.getProperties().getProperty("hibernate.mapping.precedence"))) {
         this.metadataSourcePrecedence = this.parsePrecedence(this.getProperties().getProperty("hibernate.mapping.precedence"));
      }

      if (this.metadataSourcePrecedence.isEmpty()) {
         this.metadataSourcePrecedence = Arrays.asList(DEFAULT_ARTEFACT_PROCESSING_ORDER);
      }

      this.metadataSourcePrecedence = Collections.unmodifiableList(this.metadataSourcePrecedence);
      return this.metadataSourcePrecedence;
   }

   public void setPrecedence(String precedence) {
      this.metadataSourcePrecedence = this.parsePrecedence(precedence);
   }

   private List parsePrecedence(String s) {
      if (StringHelper.isEmpty(s)) {
         return Collections.emptyList();
      } else {
         StringTokenizer precedences = new StringTokenizer(s, ",; ", false);
         List<MetadataSourceType> tmpPrecedences = new ArrayList();

         while(precedences.hasMoreElements()) {
            tmpPrecedences.add(MetadataSourceType.parsePrecedence((String)precedences.nextElement()));
         }

         return tmpPrecedences;
      }
   }

   static {
      DEFAULT_ARTEFACT_PROCESSING_ORDER = new MetadataSourceType[]{MetadataSourceType.HBM, MetadataSourceType.CLASS};
   }

   protected class MappingsImpl implements ExtendedMappings, Serializable {
      private String schemaName;
      private String catalogName;
      private String defaultPackage;
      private boolean autoImport;
      private boolean defaultLazy;
      private String defaultCascade;
      private String defaultAccess;
      private Boolean useNewGeneratorMappings;
      private Boolean useNationalizedCharacterData;
      private Boolean forceDiscriminatorInSelectsByDefault;

      public String getSchemaName() {
         return this.schemaName;
      }

      public void setSchemaName(String schemaName) {
         this.schemaName = schemaName;
      }

      public String getCatalogName() {
         return this.catalogName;
      }

      public void setCatalogName(String catalogName) {
         this.catalogName = catalogName;
      }

      public String getDefaultPackage() {
         return this.defaultPackage;
      }

      public void setDefaultPackage(String defaultPackage) {
         this.defaultPackage = defaultPackage;
      }

      public boolean isAutoImport() {
         return this.autoImport;
      }

      public void setAutoImport(boolean autoImport) {
         this.autoImport = autoImport;
      }

      public boolean isDefaultLazy() {
         return this.defaultLazy;
      }

      public void setDefaultLazy(boolean defaultLazy) {
         this.defaultLazy = defaultLazy;
      }

      public String getDefaultCascade() {
         return this.defaultCascade;
      }

      public void setDefaultCascade(String defaultCascade) {
         this.defaultCascade = defaultCascade;
      }

      public String getDefaultAccess() {
         return this.defaultAccess;
      }

      public void setDefaultAccess(String defaultAccess) {
         this.defaultAccess = defaultAccess;
      }

      public NamingStrategy getNamingStrategy() {
         return Configuration.this.namingStrategy;
      }

      public void setNamingStrategy(NamingStrategy namingStrategy) {
         Configuration.this.namingStrategy = namingStrategy;
      }

      public TypeResolver getTypeResolver() {
         return Configuration.this.typeResolver;
      }

      public Iterator iterateClasses() {
         return Configuration.this.classes.values().iterator();
      }

      public PersistentClass getClass(String entityName) {
         return (PersistentClass)Configuration.this.classes.get(entityName);
      }

      public PersistentClass locatePersistentClassByEntityName(String entityName) {
         PersistentClass persistentClass = (PersistentClass)Configuration.this.classes.get(entityName);
         if (persistentClass == null) {
            String actualEntityName = (String)Configuration.this.imports.get(entityName);
            if (StringHelper.isNotEmpty(actualEntityName)) {
               persistentClass = (PersistentClass)Configuration.this.classes.get(actualEntityName);
            }
         }

         return persistentClass;
      }

      public void addClass(PersistentClass persistentClass) throws DuplicateMappingException {
         Object old = Configuration.this.classes.put(persistentClass.getEntityName(), persistentClass);
         if (old != null) {
            throw new DuplicateMappingException("class/entity", persistentClass.getEntityName());
         }
      }

      public void addImport(String entityName, String rename) throws DuplicateMappingException {
         String existing = (String)Configuration.this.imports.put(rename, entityName);
         if (existing != null) {
            if (!existing.equals(entityName)) {
               throw new DuplicateMappingException("duplicate import: " + rename + " refers to both " + entityName + " and " + existing + " (try using auto-import=\"false\")", "import", rename);
            }

            Configuration.LOG.duplicateImport(entityName, rename);
         }

      }

      public Collection getCollection(String role) {
         return (Collection)Configuration.this.collections.get(role);
      }

      public Iterator iterateCollections() {
         return Configuration.this.collections.values().iterator();
      }

      public void addCollection(Collection collection) throws DuplicateMappingException {
         Object old = Configuration.this.collections.put(collection.getRole(), collection);
         if (old != null) {
            throw new DuplicateMappingException("collection role", collection.getRole());
         }
      }

      public Table getTable(String schema, String catalog, String name) {
         String key = Table.qualify(catalog, schema, name);
         return (Table)Configuration.this.tables.get(key);
      }

      public Iterator iterateTables() {
         return Configuration.this.tables.values().iterator();
      }

      public Table addTable(String schema, String catalog, String name, String subselect, boolean isAbstract) {
         name = this.getObjectNameNormalizer().normalizeIdentifierQuoting(name);
         schema = this.getObjectNameNormalizer().normalizeIdentifierQuoting(schema);
         catalog = this.getObjectNameNormalizer().normalizeIdentifierQuoting(catalog);
         String key = subselect == null ? Table.qualify(catalog, schema, name) : subselect;
         Table table = (Table)Configuration.this.tables.get(key);
         if (table == null) {
            table = new Table();
            table.setAbstract(isAbstract);
            table.setName(name);
            table.setSchema(schema);
            table.setCatalog(catalog);
            table.setSubselect(subselect);
            Configuration.this.tables.put(key, table);
         } else if (!isAbstract) {
            table.setAbstract(false);
         }

         return table;
      }

      public Table addDenormalizedTable(String schema, String catalog, String name, boolean isAbstract, String subselect, Table includedTable) throws DuplicateMappingException {
         name = this.getObjectNameNormalizer().normalizeIdentifierQuoting(name);
         schema = this.getObjectNameNormalizer().normalizeIdentifierQuoting(schema);
         catalog = this.getObjectNameNormalizer().normalizeIdentifierQuoting(catalog);
         String key = subselect == null ? Table.qualify(catalog, schema, name) : subselect;
         if (Configuration.this.tables.containsKey(key)) {
            throw new DuplicateMappingException("table", name);
         } else {
            Table table = new DenormalizedTable(includedTable);
            table.setAbstract(isAbstract);
            table.setName(name);
            table.setSchema(schema);
            table.setCatalog(catalog);
            table.setSubselect(subselect);
            Configuration.this.tables.put(key, table);
            return table;
         }
      }

      public NamedQueryDefinition getQuery(String name) {
         return (NamedQueryDefinition)Configuration.this.namedQueries.get(name);
      }

      public void addQuery(String name, NamedQueryDefinition query) throws DuplicateMappingException {
         if (!Configuration.this.defaultNamedQueryNames.contains(name)) {
            this.applyQuery(name, query);
         }

      }

      private void applyQuery(String name, NamedQueryDefinition query) {
         this.checkQueryName(name);
         Configuration.this.namedQueries.put(name.intern(), query);
      }

      private void checkQueryName(String name) throws DuplicateMappingException {
         if (Configuration.this.namedQueries.containsKey(name) || Configuration.this.namedSqlQueries.containsKey(name)) {
            throw new DuplicateMappingException("query", name);
         }
      }

      public void addDefaultQuery(String name, NamedQueryDefinition query) {
         this.applyQuery(name, query);
         Configuration.this.defaultNamedQueryNames.add(name);
      }

      public NamedSQLQueryDefinition getSQLQuery(String name) {
         return (NamedSQLQueryDefinition)Configuration.this.namedSqlQueries.get(name);
      }

      public void addSQLQuery(String name, NamedSQLQueryDefinition query) throws DuplicateMappingException {
         if (!Configuration.this.defaultNamedNativeQueryNames.contains(name)) {
            this.applySQLQuery(name, query);
         }

      }

      private void applySQLQuery(String name, NamedSQLQueryDefinition query) throws DuplicateMappingException {
         this.checkQueryName(name);
         Configuration.this.namedSqlQueries.put(name.intern(), query);
      }

      public void addDefaultSQLQuery(String name, NamedSQLQueryDefinition query) {
         this.applySQLQuery(name, query);
         Configuration.this.defaultNamedNativeQueryNames.add(name);
      }

      public ResultSetMappingDefinition getResultSetMapping(String name) {
         return (ResultSetMappingDefinition)Configuration.this.sqlResultSetMappings.get(name);
      }

      public void addResultSetMapping(ResultSetMappingDefinition sqlResultSetMapping) throws DuplicateMappingException {
         if (!Configuration.this.defaultSqlResultSetMappingNames.contains(sqlResultSetMapping.getName())) {
            this.applyResultSetMapping(sqlResultSetMapping);
         }

      }

      public void applyResultSetMapping(ResultSetMappingDefinition sqlResultSetMapping) throws DuplicateMappingException {
         Object old = Configuration.this.sqlResultSetMappings.put(sqlResultSetMapping.getName(), sqlResultSetMapping);
         if (old != null) {
            throw new DuplicateMappingException("resultSet", sqlResultSetMapping.getName());
         }
      }

      public void addDefaultResultSetMapping(ResultSetMappingDefinition definition) {
         String name = definition.getName();
         if (!Configuration.this.defaultSqlResultSetMappingNames.contains(name) && this.getResultSetMapping(name) != null) {
            this.removeResultSetMapping(name);
         }

         this.applyResultSetMapping(definition);
         Configuration.this.defaultSqlResultSetMappingNames.add(name);
      }

      protected void removeResultSetMapping(String name) {
         Configuration.this.sqlResultSetMappings.remove(name);
      }

      public TypeDef getTypeDef(String typeName) {
         return (TypeDef)Configuration.this.typeDefs.get(typeName);
      }

      public void addTypeDef(String typeName, String typeClass, Properties paramMap) {
         TypeDef def = new TypeDef(typeClass, paramMap);
         Configuration.this.typeDefs.put(typeName, def);
         Configuration.LOG.debugf("Added %s with class %s", typeName, typeClass);
      }

      public Map getFilterDefinitions() {
         return Configuration.this.filterDefinitions;
      }

      public FilterDefinition getFilterDefinition(String name) {
         return (FilterDefinition)Configuration.this.filterDefinitions.get(name);
      }

      public void addFilterDefinition(FilterDefinition definition) {
         Configuration.this.filterDefinitions.put(definition.getFilterName(), definition);
      }

      public FetchProfile findOrCreateFetchProfile(String name, MetadataSource source) {
         FetchProfile profile = (FetchProfile)Configuration.this.fetchProfiles.get(name);
         if (profile == null) {
            profile = new FetchProfile(name, source);
            Configuration.this.fetchProfiles.put(name, profile);
         }

         return profile;
      }

      public Iterator iterateAuxliaryDatabaseObjects() {
         return this.iterateAuxiliaryDatabaseObjects();
      }

      public Iterator iterateAuxiliaryDatabaseObjects() {
         return Configuration.this.auxiliaryDatabaseObjects.iterator();
      }

      public ListIterator iterateAuxliaryDatabaseObjectsInReverse() {
         return this.iterateAuxiliaryDatabaseObjectsInReverse();
      }

      public ListIterator iterateAuxiliaryDatabaseObjectsInReverse() {
         return Configuration.this.auxiliaryDatabaseObjects.listIterator(Configuration.this.auxiliaryDatabaseObjects.size());
      }

      public void addAuxiliaryDatabaseObject(AuxiliaryDatabaseObject auxiliaryDatabaseObject) {
         Configuration.this.auxiliaryDatabaseObjects.add(auxiliaryDatabaseObject);
      }

      public String getLogicalTableName(Table table) throws MappingException {
         return this.getLogicalTableName(table.getQuotedSchema(), table.getQuotedCatalog(), table.getQuotedName());
      }

      private String getLogicalTableName(String schema, String catalog, String physicalName) throws MappingException {
         String key = this.buildTableNameKey(schema, catalog, physicalName);
         TableDescription descriptor = (TableDescription)Configuration.this.tableNameBinding.get(key);
         if (descriptor == null) {
            throw new MappingException("Unable to find physical table: " + physicalName);
         } else {
            return descriptor.logicalName;
         }
      }

      public void addTableBinding(String schema, String catalog, String logicalName, String physicalName, Table denormalizedSuperTable) throws DuplicateMappingException {
         String key = this.buildTableNameKey(schema, catalog, physicalName);
         TableDescription tableDescription = new TableDescription(logicalName, denormalizedSuperTable);
         TableDescription oldDescriptor = (TableDescription)Configuration.this.tableNameBinding.put(key, tableDescription);
         if (oldDescriptor != null && !oldDescriptor.logicalName.equals(logicalName)) {
            throw new DuplicateMappingException("Same physical table name [" + physicalName + "] references several logical table names: [" + oldDescriptor.logicalName + "], [" + logicalName + ']', "table", physicalName);
         }
      }

      private String buildTableNameKey(String schema, String catalog, String finalName) {
         StringBuilder keyBuilder = new StringBuilder();
         if (schema != null) {
            keyBuilder.append(schema);
         }

         keyBuilder.append(".");
         if (catalog != null) {
            keyBuilder.append(catalog);
         }

         keyBuilder.append(".");
         keyBuilder.append(finalName);
         return keyBuilder.toString();
      }

      public void addColumnBinding(String logicalName, Column physicalColumn, Table table) throws DuplicateMappingException {
         TableColumnNameBinding binding = (TableColumnNameBinding)Configuration.this.columnNameBindingPerTable.get(table);
         if (binding == null) {
            binding = new TableColumnNameBinding(table.getName());
            Configuration.this.columnNameBindingPerTable.put(table, binding);
         }

         binding.addBinding(logicalName, physicalColumn);
      }

      public String getPhysicalColumnName(String logicalName, Table table) throws MappingException {
         logicalName = logicalName.toLowerCase();
         String finalName = null;
         Table currentTable = table;

         do {
            TableColumnNameBinding binding = (TableColumnNameBinding)Configuration.this.columnNameBindingPerTable.get(currentTable);
            if (binding != null) {
               finalName = (String)binding.logicalToPhysical.get(logicalName);
            }

            String key = this.buildTableNameKey(currentTable.getQuotedSchema(), currentTable.getQuotedCatalog(), currentTable.getQuotedName());
            TableDescription description = (TableDescription)Configuration.this.tableNameBinding.get(key);
            if (description != null) {
               currentTable = description.denormalizedSupertable;
            } else {
               currentTable = null;
            }
         } while(finalName == null && currentTable != null);

         if (finalName == null) {
            throw new MappingException("Unable to find column with logical name " + logicalName + " in table " + table.getName());
         } else {
            return finalName;
         }
      }

      public String getLogicalColumnName(String physicalName, Table table) throws MappingException {
         String logical = null;
         Table currentTable = table;
         TableDescription description = null;

         do {
            TableColumnNameBinding binding = (TableColumnNameBinding)Configuration.this.columnNameBindingPerTable.get(currentTable);
            if (binding != null) {
               logical = (String)binding.physicalToLogical.get(physicalName);
            }

            String key = this.buildTableNameKey(currentTable.getQuotedSchema(), currentTable.getQuotedCatalog(), currentTable.getQuotedName());
            description = (TableDescription)Configuration.this.tableNameBinding.get(key);
            if (description != null) {
               currentTable = description.denormalizedSupertable;
            } else {
               currentTable = null;
            }
         } while(logical == null && currentTable != null && description != null);

         if (logical == null) {
            throw new MappingException("Unable to find logical column name from physical name " + physicalName + " in table " + table.getName());
         } else {
            return logical;
         }
      }

      public void addSecondPass(SecondPass sp) {
         this.addSecondPass(sp, false);
      }

      public void addSecondPass(SecondPass sp, boolean onTopOfTheQueue) {
         if (onTopOfTheQueue) {
            Configuration.this.secondPasses.add(0, sp);
         } else {
            Configuration.this.secondPasses.add(sp);
         }

      }

      public void addPropertyReference(String referencedClass, String propertyName) {
         Configuration.this.propertyReferences.add(new Mappings.PropertyReference(referencedClass, propertyName, false));
      }

      public void addUniquePropertyReference(String referencedClass, String propertyName) {
         Configuration.this.propertyReferences.add(new Mappings.PropertyReference(referencedClass, propertyName, true));
      }

      public void addToExtendsQueue(ExtendsQueueEntry entry) {
         Configuration.this.extendsQueue.put(entry, (Object)null);
      }

      public MutableIdentifierGeneratorFactory getIdentifierGeneratorFactory() {
         return Configuration.this.identifierGeneratorFactory;
      }

      public void addMappedSuperclass(Class type, MappedSuperclass mappedSuperclass) {
         Configuration.this.mappedSuperClasses.put(type, mappedSuperclass);
      }

      public MappedSuperclass getMappedSuperclass(Class type) {
         return (MappedSuperclass)Configuration.this.mappedSuperClasses.get(type);
      }

      public ObjectNameNormalizer getObjectNameNormalizer() {
         return Configuration.this.normalizer;
      }

      public Properties getConfigurationProperties() {
         return Configuration.this.properties;
      }

      public void addDefaultGenerator(IdGenerator generator) {
         this.addGenerator(generator);
         Configuration.this.defaultNamedGenerators.add(generator.getName());
      }

      public boolean isInSecondPass() {
         return Configuration.this.inSecondPass;
      }

      public PropertyData getPropertyAnnotatedWithMapsId(XClass entityType, String propertyName) {
         Map<String, PropertyData> map = (Map)Configuration.this.propertiesAnnotatedWithMapsId.get(entityType);
         return map == null ? null : (PropertyData)map.get(propertyName);
      }

      public void addPropertyAnnotatedWithMapsId(XClass entityType, PropertyData property) {
         Map<String, PropertyData> map = (Map)Configuration.this.propertiesAnnotatedWithMapsId.get(entityType);
         if (map == null) {
            map = new HashMap();
            Configuration.this.propertiesAnnotatedWithMapsId.put(entityType, map);
         }

         map.put(((MapsId)property.getProperty().getAnnotation(MapsId.class)).value(), property);
      }

      public boolean isSpecjProprietarySyntaxEnabled() {
         return Configuration.this.specjProprietarySyntaxEnabled;
      }

      public void addPropertyAnnotatedWithMapsIdSpecj(XClass entityType, PropertyData property, String mapsIdValue) {
         Map<String, PropertyData> map = (Map)Configuration.this.propertiesAnnotatedWithMapsId.get(entityType);
         if (map == null) {
            map = new HashMap();
            Configuration.this.propertiesAnnotatedWithMapsId.put(entityType, map);
         }

         map.put(mapsIdValue, property);
      }

      public PropertyData getPropertyAnnotatedWithIdAndToOne(XClass entityType, String propertyName) {
         Map<String, PropertyData> map = (Map)Configuration.this.propertiesAnnotatedWithIdAndToOne.get(entityType);
         return map == null ? null : (PropertyData)map.get(propertyName);
      }

      public void addToOneAndIdProperty(XClass entityType, PropertyData property) {
         Map<String, PropertyData> map = (Map)Configuration.this.propertiesAnnotatedWithIdAndToOne.get(entityType);
         if (map == null) {
            map = new HashMap();
            Configuration.this.propertiesAnnotatedWithIdAndToOne.put(entityType, map);
         }

         map.put(property.getPropertyName(), property);
      }

      public boolean useNewGeneratorMappings() {
         if (this.useNewGeneratorMappings == null) {
            String booleanName = this.getConfigurationProperties().getProperty("hibernate.id.new_generator_mappings");
            this.useNewGeneratorMappings = Boolean.valueOf(booleanName);
         }

         return this.useNewGeneratorMappings;
      }

      public boolean useNationalizedCharacterData() {
         if (this.useNationalizedCharacterData == null) {
            String booleanName = this.getConfigurationProperties().getProperty("hibernate.use_nationalized_character_data");
            this.useNationalizedCharacterData = Boolean.valueOf(booleanName);
         }

         return this.useNationalizedCharacterData;
      }

      public boolean forceDiscriminatorInSelectsByDefault() {
         if (this.forceDiscriminatorInSelectsByDefault == null) {
            String booleanName = this.getConfigurationProperties().getProperty("hibernate.discriminator.force_in_select");
            this.forceDiscriminatorInSelectsByDefault = Boolean.valueOf(booleanName);
         }

         return this.forceDiscriminatorInSelectsByDefault;
      }

      public IdGenerator getGenerator(String name) {
         return this.getGenerator(name, (Map)null);
      }

      public IdGenerator getGenerator(String name, Map localGenerators) {
         if (localGenerators != null) {
            IdGenerator result = (IdGenerator)localGenerators.get(name);
            if (result != null) {
               return result;
            }
         }

         return (IdGenerator)Configuration.this.namedGenerators.get(name);
      }

      public void addGenerator(IdGenerator generator) {
         if (!Configuration.this.defaultNamedGenerators.contains(generator.getName())) {
            IdGenerator old = (IdGenerator)Configuration.this.namedGenerators.put(generator.getName(), generator);
            if (old != null) {
               Configuration.LOG.duplicateGeneratorName(old.getName());
            }
         }

      }

      public void addGeneratorTable(String name, Properties params) {
         Object old = Configuration.this.generatorTables.put(name, params);
         if (old != null) {
            Configuration.LOG.duplicateGeneratorTable(name);
         }

      }

      public Properties getGeneratorTableProperties(String name, Map localGeneratorTables) {
         if (localGeneratorTables != null) {
            Properties result = (Properties)localGeneratorTables.get(name);
            if (result != null) {
               return result;
            }
         }

         return (Properties)Configuration.this.generatorTables.get(name);
      }

      public Map getJoins(String entityName) {
         return (Map)Configuration.this.joins.get(entityName);
      }

      public void addJoins(PersistentClass persistentClass, Map joins) {
         Object old = Configuration.this.joins.put(persistentClass.getEntityName(), joins);
         if (old != null) {
            Configuration.LOG.duplicateJoins(persistentClass.getEntityName());
         }

      }

      public AnnotatedClassType getClassType(XClass clazz) {
         AnnotatedClassType type = (AnnotatedClassType)Configuration.this.classTypes.get(clazz.getName());
         return type == null ? this.addClassType(clazz) : type;
      }

      public AnnotatedClassType addClassType(XClass clazz) {
         AnnotatedClassType type;
         if (clazz.isAnnotationPresent(Entity.class)) {
            type = AnnotatedClassType.ENTITY;
         } else if (clazz.isAnnotationPresent(Embeddable.class)) {
            type = AnnotatedClassType.EMBEDDABLE;
         } else if (clazz.isAnnotationPresent(javax.persistence.MappedSuperclass.class)) {
            type = AnnotatedClassType.EMBEDDABLE_SUPERCLASS;
         } else {
            type = AnnotatedClassType.NONE;
         }

         Configuration.this.classTypes.put(clazz.getName(), type);
         return type;
      }

      public Map getTableUniqueConstraints() {
         Map<Table, List<String[]>> deprecatedStructure = new HashMap(CollectionHelper.determineProperSizing(this.getUniqueConstraintHoldersByTable()), 0.75F);

         for(Map.Entry entry : this.getUniqueConstraintHoldersByTable().entrySet()) {
            List<String[]> columnsPerConstraint = new ArrayList(CollectionHelper.determineProperSizing(((List)entry.getValue()).size()));
            deprecatedStructure.put(entry.getKey(), columnsPerConstraint);

            for(UniqueConstraintHolder holder : (List)entry.getValue()) {
               columnsPerConstraint.add(holder.getColumns());
            }
         }

         return deprecatedStructure;
      }

      public Map getUniqueConstraintHoldersByTable() {
         return Configuration.this.uniqueConstraintHoldersByTable;
      }

      public void addUniqueConstraints(Table table, List uniqueConstraints) {
         List<UniqueConstraintHolder> constraintHolders = new ArrayList(CollectionHelper.determineProperSizing(uniqueConstraints.size()));
         int keyNameBase = this.determineCurrentNumberOfUniqueConstraintHolders(table);

         for(String[] columns : uniqueConstraints) {
            String keyName = "key" + keyNameBase++;
            constraintHolders.add((new UniqueConstraintHolder()).setName(keyName).setColumns(columns));
         }

         this.addUniqueConstraintHolders(table, constraintHolders);
      }

      private int determineCurrentNumberOfUniqueConstraintHolders(Table table) {
         List currentHolders = (List)this.getUniqueConstraintHoldersByTable().get(table);
         return currentHolders == null ? 0 : currentHolders.size();
      }

      public void addUniqueConstraintHolders(Table table, List uniqueConstraintHolders) {
         List<UniqueConstraintHolder> holderList = (List)this.getUniqueConstraintHoldersByTable().get(table);
         if (holderList == null) {
            holderList = new ArrayList();
            this.getUniqueConstraintHoldersByTable().put(table, holderList);
         }

         holderList.addAll(uniqueConstraintHolders);
      }

      public void addMappedBy(String entityName, String propertyName, String inversePropertyName) {
         Configuration.this.mappedByResolver.put(entityName + "." + propertyName, inversePropertyName);
      }

      public String getFromMappedBy(String entityName, String propertyName) {
         return (String)Configuration.this.mappedByResolver.get(entityName + "." + propertyName);
      }

      public void addPropertyReferencedAssociation(String entityName, String propertyName, String propertyRef) {
         Configuration.this.propertyRefResolver.put(entityName + "." + propertyName, propertyRef);
      }

      public String getPropertyReferencedAssociation(String entityName, String propertyName) {
         return (String)Configuration.this.propertyRefResolver.get(entityName + "." + propertyName);
      }

      public ReflectionManager getReflectionManager() {
         return Configuration.this.reflectionManager;
      }

      public Map getClasses() {
         return Configuration.this.classes;
      }

      public void addAnyMetaDef(AnyMetaDef defAnn) throws AnnotationException {
         if (Configuration.this.anyMetaDefs.containsKey(defAnn.name())) {
            throw new AnnotationException("Two @AnyMetaDef with the same name defined: " + defAnn.name());
         } else {
            Configuration.this.anyMetaDefs.put(defAnn.name(), defAnn);
         }
      }

      public AnyMetaDef getAnyMetaDef(String name) {
         return (AnyMetaDef)Configuration.this.anyMetaDefs.get(name);
      }

      private class TableDescription implements Serializable {
         final String logicalName;
         final Table denormalizedSupertable;

         TableDescription(String logicalName, Table denormalizedSupertable) {
            this.logicalName = logicalName;
            this.denormalizedSupertable = denormalizedSupertable;
         }
      }

      private class TableColumnNameBinding implements Serializable {
         private final String tableName;
         private Map logicalToPhysical;
         private Map physicalToLogical;

         private TableColumnNameBinding(String tableName) {
            this.logicalToPhysical = new HashMap();
            this.physicalToLogical = new HashMap();
            this.tableName = tableName;
         }

         public void addBinding(String logicalName, Column physicalColumn) {
            this.bindLogicalToPhysical(logicalName, physicalColumn);
            this.bindPhysicalToLogical(logicalName, physicalColumn);
         }

         private void bindLogicalToPhysical(String logicalName, Column physicalColumn) throws DuplicateMappingException {
            String logicalKey = logicalName.toLowerCase();
            String physicalName = physicalColumn.getQuotedName();
            String existingPhysicalName = (String)this.logicalToPhysical.put(logicalKey, physicalName);
            if (existingPhysicalName != null) {
               boolean areSamePhysicalColumn = physicalColumn.isQuoted() ? existingPhysicalName.equals(physicalName) : existingPhysicalName.equalsIgnoreCase(physicalName);
               if (!areSamePhysicalColumn) {
                  throw new DuplicateMappingException(" Table [" + this.tableName + "] contains logical column name [" + logicalName + "] referenced by multiple physical column names: [" + existingPhysicalName + "], [" + physicalName + "]", "column-binding", this.tableName + "." + logicalName);
               }
            }

         }

         private void bindPhysicalToLogical(String logicalName, Column physicalColumn) throws DuplicateMappingException {
            String physicalName = physicalColumn.getQuotedName();
            String existingLogicalName = (String)this.physicalToLogical.put(physicalName, logicalName);
            if (existingLogicalName != null && !existingLogicalName.equals(logicalName)) {
               throw new DuplicateMappingException(" Table [" + this.tableName + "] contains phyical column name [" + physicalName + "] represented by different logical column names: [" + existingLogicalName + "], [" + logicalName + "]", "column-binding", this.tableName + "." + physicalName);
            }
         }
      }
   }

   final class ObjectNameNormalizerImpl extends ObjectNameNormalizer implements Serializable {
      public boolean isUseQuotedIdentifiersGlobally() {
         String setting = (String)Configuration.this.properties.get("hibernate.globally_quoted_identifiers");
         return setting != null && Boolean.valueOf(setting);
      }

      public NamingStrategy getNamingStrategy() {
         return Configuration.this.namingStrategy;
      }
   }

   protected class MetadataSourceQueue implements Serializable {
      private LinkedHashMap hbmMetadataToEntityNamesMap = new LinkedHashMap();
      private Map hbmMetadataByEntityNameXRef = new HashMap();
      private transient List annotatedClasses = new ArrayList();
      private transient Map annotatedClassesByEntityNameMap = new HashMap();

      private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
         ois.defaultReadObject();
         this.annotatedClassesByEntityNameMap = new HashMap();
         List<Class> serializableAnnotatedClasses = (List)ois.readObject();
         this.annotatedClasses = new ArrayList(serializableAnnotatedClasses.size());

         for(Class clazz : serializableAnnotatedClasses) {
            this.annotatedClasses.add(Configuration.this.reflectionManager.toXClass(clazz));
         }

      }

      private void writeObject(ObjectOutputStream out) throws IOException {
         out.defaultWriteObject();
         List<Class> serializableAnnotatedClasses = new ArrayList(this.annotatedClasses.size());

         for(XClass xClass : this.annotatedClasses) {
            serializableAnnotatedClasses.add(Configuration.this.reflectionManager.toClass(xClass));
         }

         out.writeObject(serializableAnnotatedClasses);
      }

      public void add(XmlDocument metadataXml) {
         Document document = metadataXml.getDocumentTree();
         Element hmNode = document.getRootElement();
         Attribute packNode = hmNode.attribute("package");
         String defaultPackage = packNode != null ? packNode.getValue() : "";
         Set<String> entityNames = new HashSet();
         this.findClassNames(defaultPackage, hmNode, entityNames);

         for(String entity : entityNames) {
            this.hbmMetadataByEntityNameXRef.put(entity, metadataXml);
         }

         this.hbmMetadataToEntityNamesMap.put(metadataXml, entityNames);
      }

      private void findClassNames(String defaultPackage, Element startNode, Set names) {
         Iterator[] classes = new Iterator[4];
         classes[0] = startNode.elementIterator("class");
         classes[1] = startNode.elementIterator("subclass");
         classes[2] = startNode.elementIterator("joined-subclass");
         classes[3] = startNode.elementIterator("union-subclass");
         Iterator classIterator = new JoinedIterator(classes);

         while(classIterator.hasNext()) {
            Element element = (Element)classIterator.next();
            String entityName = element.attributeValue("entity-name");
            if (entityName == null) {
               entityName = this.getClassName(element.attribute("name"), defaultPackage);
            }

            names.add(entityName);
            this.findClassNames(defaultPackage, element, names);
         }

      }

      private String getClassName(Attribute name, String defaultPackage) {
         if (name == null) {
            return null;
         } else {
            String unqualifiedName = name.getValue();
            if (unqualifiedName == null) {
               return null;
            } else {
               return unqualifiedName.indexOf(46) < 0 && defaultPackage != null ? defaultPackage + '.' + unqualifiedName : unqualifiedName;
            }
         }
      }

      public void add(XClass annotatedClass) {
         this.annotatedClasses.add(annotatedClass);
      }

      protected void syncAnnotatedClasses() {
         Iterator<XClass> itr = this.annotatedClasses.iterator();

         while(itr.hasNext()) {
            XClass annotatedClass = (XClass)itr.next();
            if (annotatedClass.isAnnotationPresent(Entity.class)) {
               this.annotatedClassesByEntityNameMap.put(annotatedClass.getName(), annotatedClass);
            } else if (!annotatedClass.isAnnotationPresent(javax.persistence.MappedSuperclass.class)) {
               itr.remove();
            }
         }

      }

      protected void processMetadata(List order) {
         this.syncAnnotatedClasses();

         for(MetadataSourceType type : order) {
            if (MetadataSourceType.HBM.equals(type)) {
               this.processHbmXmlQueue();
            } else if (MetadataSourceType.CLASS.equals(type)) {
               this.processAnnotatedClassesQueue();
            }
         }

      }

      private void processHbmXmlQueue() {
         Configuration.LOG.debug("Processing hbm.xml files");

         for(Map.Entry entry : this.hbmMetadataToEntityNamesMap.entrySet()) {
            this.processHbmXml((XmlDocument)entry.getKey(), (Set)entry.getValue());
         }

         this.hbmMetadataToEntityNamesMap.clear();
         this.hbmMetadataByEntityNameXRef.clear();
      }

      private void processHbmXml(XmlDocument metadataXml, Set entityNames) {
         try {
            HbmBinder.bindRoot(metadataXml, Configuration.this.createMappings(), Collections.EMPTY_MAP, entityNames);
         } catch (MappingException me) {
            throw new InvalidMappingException(metadataXml.getOrigin().getType(), metadataXml.getOrigin().getName(), me);
         }

         for(String entityName : entityNames) {
            if (this.annotatedClassesByEntityNameMap.containsKey(entityName)) {
               this.annotatedClasses.remove(this.annotatedClassesByEntityNameMap.get(entityName));
               this.annotatedClassesByEntityNameMap.remove(entityName);
            }
         }

      }

      private void processAnnotatedClassesQueue() {
         Configuration.LOG.debug("Process annotated classes");
         List<XClass> orderedClasses = this.orderAndFillHierarchy(this.annotatedClasses);
         Mappings mappings = Configuration.this.createMappings();
         Map<XClass, InheritanceState> inheritanceStatePerClass = AnnotationBinder.buildInheritanceStates(orderedClasses, mappings);

         for(XClass clazz : orderedClasses) {
            AnnotationBinder.bindClass(clazz, inheritanceStatePerClass, mappings);
            String entityName = clazz.getName();
            if (this.hbmMetadataByEntityNameXRef.containsKey(entityName)) {
               this.hbmMetadataToEntityNamesMap.remove(this.hbmMetadataByEntityNameXRef.get(entityName));
               this.hbmMetadataByEntityNameXRef.remove(entityName);
            }
         }

         this.annotatedClasses.clear();
         this.annotatedClassesByEntityNameMap.clear();
      }

      private List orderAndFillHierarchy(List original) {
         List<XClass> copy = new ArrayList(original);
         this.insertMappedSuperclasses(original, copy);
         List<XClass> workingCopy = new ArrayList(copy);
         List<XClass> newList = new ArrayList(copy.size());

         while(workingCopy.size() > 0) {
            XClass clazz = (XClass)workingCopy.get(0);
            this.orderHierarchy(workingCopy, newList, copy, clazz);
         }

         return newList;
      }

      private void insertMappedSuperclasses(List original, List copy) {
         for(XClass clazz : original) {
            for(XClass superClass = clazz.getSuperclass(); superClass != null && !Configuration.this.reflectionManager.equals(superClass, Object.class) && !copy.contains(superClass); superClass = superClass.getSuperclass()) {
               if (superClass.isAnnotationPresent(Entity.class) || superClass.isAnnotationPresent(javax.persistence.MappedSuperclass.class)) {
                  copy.add(superClass);
               }
            }
         }

      }

      private void orderHierarchy(List copy, List newList, List original, XClass clazz) {
         if (clazz != null && !Configuration.this.reflectionManager.equals(clazz, Object.class)) {
            this.orderHierarchy(copy, newList, original, clazz.getSuperclass());
            if (original.contains(clazz)) {
               if (!newList.contains(clazz)) {
                  newList.add(clazz);
               }

               copy.remove(clazz);
            }

         }
      }

      public boolean isEmpty() {
         return this.hbmMetadataToEntityNamesMap.isEmpty() && this.annotatedClasses.isEmpty();
      }
   }

   private static class CacheHolder {
      public String role;
      public String usage;
      public String region;
      public boolean isClass;
      public boolean cacheLazy;

      public CacheHolder(String role, String usage, String region, boolean isClass, boolean cacheLazy) {
         this.role = role;
         this.usage = usage;
         this.region = region;
         this.isClass = isClass;
         this.cacheLazy = cacheLazy;
      }
   }
}
