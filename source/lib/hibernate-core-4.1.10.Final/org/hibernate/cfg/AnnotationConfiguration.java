package org.hibernate.cfg;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import org.hibernate.HibernateException;
import org.hibernate.Interceptor;
import org.hibernate.MappingException;
import org.w3c.dom.Document;

/** @deprecated */
@Deprecated
public class AnnotationConfiguration extends Configuration {
   public AnnotationConfiguration addAnnotatedClass(Class annotatedClass) throws MappingException {
      return (AnnotationConfiguration)super.addAnnotatedClass(annotatedClass);
   }

   public AnnotationConfiguration addPackage(String packageName) throws MappingException {
      return (AnnotationConfiguration)super.addPackage(packageName);
   }

   public ExtendedMappings createExtendedMappings() {
      return new ExtendedMappingsImpl();
   }

   public AnnotationConfiguration addFile(String xmlFile) throws MappingException {
      super.addFile(xmlFile);
      return this;
   }

   public AnnotationConfiguration addFile(File xmlFile) throws MappingException {
      super.addFile(xmlFile);
      return this;
   }

   public AnnotationConfiguration addCacheableFile(File xmlFile) throws MappingException {
      super.addCacheableFile(xmlFile);
      return this;
   }

   public AnnotationConfiguration addCacheableFile(String xmlFile) throws MappingException {
      super.addCacheableFile(xmlFile);
      return this;
   }

   public AnnotationConfiguration addXML(String xml) throws MappingException {
      super.addXML(xml);
      return this;
   }

   public AnnotationConfiguration addURL(URL url) throws MappingException {
      super.addURL(url);
      return this;
   }

   public AnnotationConfiguration addResource(String resourceName, ClassLoader classLoader) throws MappingException {
      super.addResource(resourceName, classLoader);
      return this;
   }

   public AnnotationConfiguration addDocument(Document doc) throws MappingException {
      super.addDocument(doc);
      return this;
   }

   public AnnotationConfiguration addResource(String resourceName) throws MappingException {
      super.addResource(resourceName);
      return this;
   }

   public AnnotationConfiguration addClass(Class persistentClass) throws MappingException {
      super.addClass(persistentClass);
      return this;
   }

   public AnnotationConfiguration addJar(File jar) throws MappingException {
      super.addJar(jar);
      return this;
   }

   public AnnotationConfiguration addDirectory(File dir) throws MappingException {
      super.addDirectory(dir);
      return this;
   }

   public AnnotationConfiguration setInterceptor(Interceptor interceptor) {
      super.setInterceptor(interceptor);
      return this;
   }

   public AnnotationConfiguration setProperties(Properties properties) {
      super.setProperties(properties);
      return this;
   }

   public AnnotationConfiguration addProperties(Properties extraProperties) {
      super.addProperties(extraProperties);
      return this;
   }

   public AnnotationConfiguration mergeProperties(Properties properties) {
      super.mergeProperties(properties);
      return this;
   }

   public AnnotationConfiguration setProperty(String propertyName, String value) {
      super.setProperty(propertyName, value);
      return this;
   }

   public AnnotationConfiguration configure() throws HibernateException {
      super.configure();
      return this;
   }

   public AnnotationConfiguration configure(String resource) throws HibernateException {
      super.configure(resource);
      return this;
   }

   public AnnotationConfiguration configure(URL url) throws HibernateException {
      super.configure(url);
      return this;
   }

   public AnnotationConfiguration configure(File configFile) throws HibernateException {
      super.configure(configFile);
      return this;
   }

   protected AnnotationConfiguration doConfigure(InputStream stream, String resourceName) throws HibernateException {
      super.doConfigure(stream, resourceName);
      return this;
   }

   public AnnotationConfiguration configure(Document document) throws HibernateException {
      super.configure(document);
      return this;
   }

   protected AnnotationConfiguration doConfigure(org.dom4j.Document doc) throws HibernateException {
      super.doConfigure(doc);
      return this;
   }

   public AnnotationConfiguration setCacheConcurrencyStrategy(String clazz, String concurrencyStrategy) {
      super.setCacheConcurrencyStrategy(clazz, concurrencyStrategy);
      return this;
   }

   public AnnotationConfiguration setCacheConcurrencyStrategy(String clazz, String concurrencyStrategy, String region) {
      super.setCacheConcurrencyStrategy(clazz, concurrencyStrategy, region);
      return this;
   }

   public AnnotationConfiguration setCollectionCacheConcurrencyStrategy(String collectionRole, String concurrencyStrategy) throws MappingException {
      super.setCollectionCacheConcurrencyStrategy(collectionRole, concurrencyStrategy);
      return this;
   }

   public AnnotationConfiguration setNamingStrategy(NamingStrategy namingStrategy) {
      super.setNamingStrategy(namingStrategy);
      return this;
   }

   /** @deprecated */
   @Deprecated
   protected class ExtendedMappingsImpl extends Configuration.MappingsImpl {
   }
}
