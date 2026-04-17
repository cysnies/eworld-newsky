package org.hibernate.cfg.annotations.reflection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.AccessType;
import org.dom4j.Document;
import org.dom4j.Element;
import org.hibernate.AnnotationException;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.StringHelper;
import org.jboss.logging.Logger;

public class XMLContext implements Serializable {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, XMLContext.class.getName());
   private Default globalDefaults;
   private Map classOverriding = new HashMap();
   private Map defaultsOverriding = new HashMap();
   private List defaultElements = new ArrayList();
   private List defaultEntityListeners = new ArrayList();
   private boolean hasContext = false;

   public List addDocument(Document doc) {
      this.hasContext = true;
      List<String> addedClasses = new ArrayList();
      Element root = doc.getRootElement();
      Element metadata = root.element("persistence-unit-metadata");
      if (metadata != null) {
         if (this.globalDefaults == null) {
            this.globalDefaults = new Default();
            this.globalDefaults.setMetadataComplete(metadata.element("xml-mapping-metadata-complete") != null ? Boolean.TRUE : null);
            Element defaultElement = metadata.element("persistence-unit-defaults");
            if (defaultElement != null) {
               Element unitElement = defaultElement.element("schema");
               this.globalDefaults.setSchema(unitElement != null ? unitElement.getTextTrim() : null);
               unitElement = defaultElement.element("catalog");
               this.globalDefaults.setCatalog(unitElement != null ? unitElement.getTextTrim() : null);
               unitElement = defaultElement.element("access");
               this.setAccess(unitElement, this.globalDefaults);
               unitElement = defaultElement.element("cascade-persist");
               this.globalDefaults.setCascadePersist(unitElement != null ? Boolean.TRUE : null);
               unitElement = defaultElement.element("delimited-identifiers");
               this.globalDefaults.setDelimitedIdentifiers(unitElement != null ? Boolean.TRUE : null);
               this.defaultEntityListeners.addAll(this.addEntityListenerClasses(defaultElement, (String)null, addedClasses));
            }
         } else {
            LOG.duplicateMetadata();
         }
      }

      Default entityMappingDefault = new Default();
      Element unitElement = root.element("package");
      String packageName = unitElement != null ? unitElement.getTextTrim() : null;
      entityMappingDefault.setPackageName(packageName);
      unitElement = root.element("schema");
      entityMappingDefault.setSchema(unitElement != null ? unitElement.getTextTrim() : null);
      unitElement = root.element("catalog");
      entityMappingDefault.setCatalog(unitElement != null ? unitElement.getTextTrim() : null);
      unitElement = root.element("access");
      this.setAccess(unitElement, entityMappingDefault);
      this.defaultElements.add(root);
      List<Element> entities = root.elements("entity");
      this.addClass(entities, packageName, entityMappingDefault, addedClasses);
      entities = root.elements("mapped-superclass");
      this.addClass(entities, packageName, entityMappingDefault, addedClasses);
      entities = root.elements("embeddable");
      this.addClass(entities, packageName, entityMappingDefault, addedClasses);
      return addedClasses;
   }

   private void setAccess(Element unitElement, Default defaultType) {
      if (unitElement != null) {
         String access = unitElement.getTextTrim();
         this.setAccess(access, defaultType);
      }

   }

   private void setAccess(String access, Default defaultType) {
      if (access != null) {
         AccessType type;
         try {
            type = AccessType.valueOf(access);
         } catch (IllegalArgumentException var5) {
            throw new AnnotationException("Invalid access type " + access + " (check your xml configuration)");
         }

         defaultType.setAccess(type);
      }

   }

   private void addClass(List entities, String packageName, Default defaults, List addedClasses) {
      for(Element element : entities) {
         String className = buildSafeClassName(element.attributeValue("class"), packageName);
         if (this.classOverriding.containsKey(className)) {
            throw new IllegalStateException("Duplicate XML entry for " + className);
         }

         addedClasses.add(className);
         this.classOverriding.put(className, element);
         Default localDefault = new Default();
         localDefault.override(defaults);
         String metadataCompleteString = element.attributeValue("metadata-complete");
         if (metadataCompleteString != null) {
            localDefault.setMetadataComplete(Boolean.parseBoolean(metadataCompleteString));
         }

         String access = element.attributeValue("access");
         this.setAccess(access, localDefault);
         this.defaultsOverriding.put(className, localDefault);
         LOG.debugf("Adding XML overriding information for %s", className);
         this.addEntityListenerClasses(element, packageName, addedClasses);
      }

   }

   private List addEntityListenerClasses(Element element, String packageName, List addedClasses) {
      List<String> localAddedClasses = new ArrayList();
      Element listeners = element.element("entity-listeners");
      if (listeners != null) {
         for(Element listener : listeners.elements("entity-listener")) {
            String listenerClassName = buildSafeClassName(listener.attributeValue("class"), packageName);
            if (this.classOverriding.containsKey(listenerClassName)) {
               if (!"entity-listener".equals(((Element)this.classOverriding.get(listenerClassName)).getName())) {
                  throw new IllegalStateException("Duplicate XML entry for " + listenerClassName);
               }

               LOG.duplicateListener(listenerClassName);
            } else {
               localAddedClasses.add(listenerClassName);
               this.classOverriding.put(listenerClassName, listener);
            }
         }
      }

      LOG.debugf("Adding XML overriding information for listeners: %s", localAddedClasses);
      addedClasses.addAll(localAddedClasses);
      return localAddedClasses;
   }

   public static String buildSafeClassName(String className, String defaultPackageName) {
      if (className.indexOf(46) < 0 && StringHelper.isNotEmpty(defaultPackageName)) {
         className = StringHelper.qualify(defaultPackageName, className);
      }

      return className;
   }

   public static String buildSafeClassName(String className, Default defaults) {
      return buildSafeClassName(className, defaults.getPackageName());
   }

   public Default getDefault(String className) {
      Default xmlDefault = new Default();
      xmlDefault.override(this.globalDefaults);
      if (className != null) {
         Default entityMappingOverriding = (Default)this.defaultsOverriding.get(className);
         xmlDefault.override(entityMappingOverriding);
      }

      return xmlDefault;
   }

   public Element getXMLTree(String className) {
      return (Element)this.classOverriding.get(className);
   }

   public List getAllDocuments() {
      return this.defaultElements;
   }

   public boolean hasContext() {
      return this.hasContext;
   }

   public List getDefaultEntityListeners() {
      return this.defaultEntityListeners;
   }

   public static class Default implements Serializable {
      private AccessType access;
      private String packageName;
      private String schema;
      private String catalog;
      private Boolean metadataComplete;
      private Boolean cascadePersist;
      private Boolean delimitedIdentifier;

      public AccessType getAccess() {
         return this.access;
      }

      protected void setAccess(AccessType access) {
         this.access = access;
      }

      public String getCatalog() {
         return this.catalog;
      }

      protected void setCatalog(String catalog) {
         this.catalog = catalog;
      }

      public String getPackageName() {
         return this.packageName;
      }

      protected void setPackageName(String packageName) {
         this.packageName = packageName;
      }

      public String getSchema() {
         return this.schema;
      }

      protected void setSchema(String schema) {
         this.schema = schema;
      }

      public Boolean getMetadataComplete() {
         return this.metadataComplete;
      }

      public boolean canUseJavaAnnotations() {
         return this.metadataComplete == null || !this.metadataComplete;
      }

      protected void setMetadataComplete(Boolean metadataComplete) {
         this.metadataComplete = metadataComplete;
      }

      public Boolean getCascadePersist() {
         return this.cascadePersist;
      }

      void setCascadePersist(Boolean cascadePersist) {
         this.cascadePersist = cascadePersist;
      }

      public void override(Default globalDefault) {
         if (globalDefault != null) {
            if (globalDefault.getAccess() != null) {
               this.access = globalDefault.getAccess();
            }

            if (globalDefault.getPackageName() != null) {
               this.packageName = globalDefault.getPackageName();
            }

            if (globalDefault.getSchema() != null) {
               this.schema = globalDefault.getSchema();
            }

            if (globalDefault.getCatalog() != null) {
               this.catalog = globalDefault.getCatalog();
            }

            if (globalDefault.getDelimitedIdentifier() != null) {
               this.delimitedIdentifier = globalDefault.getDelimitedIdentifier();
            }

            if (globalDefault.getMetadataComplete() != null) {
               this.metadataComplete = globalDefault.getMetadataComplete();
            }

            if (globalDefault.getCascadePersist() != null) {
               this.cascadePersist = globalDefault.getCascadePersist();
            }
         }

      }

      public void setDelimitedIdentifiers(Boolean delimitedIdentifier) {
         this.delimitedIdentifier = delimitedIdentifier;
      }

      public Boolean getDelimitedIdentifier() {
         return this.delimitedIdentifier;
      }
   }
}
