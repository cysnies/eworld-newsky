package org.hibernate.metamodel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import org.hibernate.cfg.EJB3DTDEntityResolver;
import org.hibernate.cfg.EJB3NamingStrategy;
import org.hibernate.cfg.NamingStrategy;
import org.hibernate.internal.jaxb.JaxbRoot;
import org.hibernate.internal.jaxb.Origin;
import org.hibernate.internal.jaxb.SourceType;
import org.hibernate.metamodel.source.MappingException;
import org.hibernate.metamodel.source.MappingNotFoundException;
import org.hibernate.metamodel.source.internal.JaxbHelper;
import org.hibernate.metamodel.source.internal.MetadataBuilderImpl;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.classloading.spi.ClassLoaderService;
import org.jboss.logging.Logger;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;

public class MetadataSources {
   private static final Logger LOG = Logger.getLogger(MetadataSources.class);
   private List jaxbRootList;
   private LinkedHashSet annotatedClasses;
   private LinkedHashSet annotatedPackages;
   private final JaxbHelper jaxbHelper;
   private final ServiceRegistry serviceRegistry;
   private final EntityResolver entityResolver;
   private final NamingStrategy namingStrategy;
   private final MetadataBuilderImpl metadataBuilder;

   public MetadataSources(ServiceRegistry serviceRegistry) {
      this(serviceRegistry, EJB3DTDEntityResolver.INSTANCE, EJB3NamingStrategy.INSTANCE);
   }

   public MetadataSources(ServiceRegistry serviceRegistry, EntityResolver entityResolver, NamingStrategy namingStrategy) {
      this.jaxbRootList = new ArrayList();
      this.annotatedClasses = new LinkedHashSet();
      this.annotatedPackages = new LinkedHashSet();
      this.serviceRegistry = serviceRegistry;
      this.entityResolver = entityResolver;
      this.namingStrategy = namingStrategy;
      this.jaxbHelper = new JaxbHelper(this);
      this.metadataBuilder = new MetadataBuilderImpl(this);
   }

   public List getJaxbRootList() {
      return this.jaxbRootList;
   }

   public Iterable getAnnotatedPackages() {
      return this.annotatedPackages;
   }

   public Iterable getAnnotatedClasses() {
      return this.annotatedClasses;
   }

   public ServiceRegistry getServiceRegistry() {
      return this.serviceRegistry;
   }

   public NamingStrategy getNamingStrategy() {
      return this.namingStrategy;
   }

   public MetadataBuilder getMetadataBuilder() {
      return this.metadataBuilder;
   }

   public Metadata buildMetadata() {
      return this.getMetadataBuilder().buildMetadata();
   }

   public MetadataSources addAnnotatedClass(Class annotatedClass) {
      this.annotatedClasses.add(annotatedClass);
      return this;
   }

   public MetadataSources addPackage(String packageName) {
      if (packageName == null) {
         throw new IllegalArgumentException("The specified package name cannot be null");
      } else {
         if (packageName.endsWith(".")) {
            packageName = packageName.substring(0, packageName.length() - 1);
         }

         this.annotatedPackages.add(packageName);
         return this;
      }
   }

   public MetadataSources addResource(String name) {
      LOG.tracef("reading mappings from resource : %s", name);
      Origin origin = new Origin(SourceType.RESOURCE, name);
      InputStream resourceInputStream = this.classLoaderService().locateResourceStream(name);
      if (resourceInputStream == null) {
         throw new MappingNotFoundException(origin);
      } else {
         this.add(resourceInputStream, origin, true);
         return this;
      }
   }

   private ClassLoaderService classLoaderService() {
      return (ClassLoaderService)this.serviceRegistry.getService(ClassLoaderService.class);
   }

   private JaxbRoot add(InputStream inputStream, Origin origin, boolean close) {
      JaxbRoot var5;
      try {
         JaxbRoot jaxbRoot = this.jaxbHelper.unmarshal(inputStream, origin);
         this.jaxbRootList.add(jaxbRoot);
         var5 = jaxbRoot;
      } finally {
         if (close) {
            try {
               inputStream.close();
            } catch (IOException var12) {
               LOG.trace("Was unable to close input stream");
            }
         }

      }

      return var5;
   }

   public MetadataSources addClass(Class entityClass) {
      if (entityClass == null) {
         throw new IllegalArgumentException("The specified class cannot be null");
      } else {
         LOG.debugf("adding resource mappings from class convention : %s", entityClass.getName());
         String mappingResourceName = entityClass.getName().replace('.', '/') + ".hbm.xml";
         this.addResource(mappingResourceName);
         return this;
      }
   }

   public MetadataSources addFile(String path) {
      return this.addFile(new File(path));
   }

   public MetadataSources addFile(File file) {
      String name = file.getAbsolutePath();
      LOG.tracef("reading mappings from file : %s", name);
      Origin origin = new Origin(SourceType.FILE, name);

      try {
         this.add(new FileInputStream(file), origin, true);
         return this;
      } catch (FileNotFoundException e) {
         throw new MappingNotFoundException(e, origin);
      }
   }

   public MetadataSources addCacheableFile(String path) {
      return this;
   }

   public MetadataSources addCacheableFile(File file) {
      return this;
   }

   public MetadataSources addInputStream(InputStream xmlInputStream) {
      this.add(xmlInputStream, new Origin(SourceType.INPUT_STREAM, "<unknown>"), false);
      return this;
   }

   public MetadataSources addURL(URL url) {
      String urlExternalForm = url.toExternalForm();
      LOG.debugf("Reading mapping document from URL : %s", urlExternalForm);
      Origin origin = new Origin(SourceType.URL, urlExternalForm);

      try {
         this.add(url.openStream(), origin, true);
         return this;
      } catch (IOException e) {
         throw new MappingNotFoundException("Unable to open url stream [" + urlExternalForm + "]", e, origin);
      }
   }

   public MetadataSources addDocument(Document document) {
      Origin origin = new Origin(SourceType.DOM, "<unknown>");
      JaxbRoot jaxbRoot = this.jaxbHelper.unmarshal(document, origin);
      this.jaxbRootList.add(jaxbRoot);
      return this;
   }

   public MetadataSources addJar(File jar) {
      LOG.debugf("Seeking mapping documents in jar file : %s", jar.getName());
      Origin origin = new Origin(SourceType.JAR, jar.getAbsolutePath());

      try {
         JarFile jarFile = new JarFile(jar);

         try {
            Enumeration jarEntries = jarFile.entries();

            while(jarEntries.hasMoreElements()) {
               ZipEntry zipEntry = (ZipEntry)jarEntries.nextElement();
               if (zipEntry.getName().endsWith(".hbm.xml")) {
                  LOG.tracef("found mapping document : %s", zipEntry.getName());

                  try {
                     this.add(jarFile.getInputStream(zipEntry), origin, true);
                  } catch (Exception e) {
                     throw new MappingException("could not read mapping documents", e, origin);
                  }
               }
            }
         } finally {
            try {
               jarFile.close();
            } catch (Exception var14) {
            }

         }

         return this;
      } catch (IOException e) {
         throw new MappingNotFoundException(e, origin);
      }
   }

   public MetadataSources addDirectory(File dir) {
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
}
