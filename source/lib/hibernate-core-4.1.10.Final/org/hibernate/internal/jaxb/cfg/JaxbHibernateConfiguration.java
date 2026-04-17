package org.hibernate.internal.jaxb.cfg;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
   name = "",
   propOrder = {"sessionFactory", "security"}
)
@XmlRootElement(
   name = "hibernate-configuration"
)
public class JaxbHibernateConfiguration {
   @XmlElement(
      name = "session-factory",
      required = true
   )
   protected JaxbSessionFactory sessionFactory;
   protected JaxbSecurity security;

   public JaxbSessionFactory getSessionFactory() {
      return this.sessionFactory;
   }

   public void setSessionFactory(JaxbSessionFactory value) {
      this.sessionFactory = value;
   }

   public JaxbSecurity getSecurity() {
      return this.security;
   }

   public void setSecurity(JaxbSecurity value) {
      this.security = value;
   }

   @XmlAccessorType(XmlAccessType.FIELD)
   @XmlType(
      name = "",
      propOrder = {"grant"}
   )
   public static class JaxbSecurity {
      protected List grant;
      @XmlAttribute(
         required = true
      )
      protected String context;

      public List getGrant() {
         if (this.grant == null) {
            this.grant = new ArrayList();
         }

         return this.grant;
      }

      public String getContext() {
         return this.context;
      }

      public void setContext(String value) {
         this.context = value;
      }

      @XmlAccessorType(XmlAccessType.FIELD)
      @XmlType(
         name = ""
      )
      public static class JaxbGrant {
         @XmlAttribute(
            required = true
         )
         protected String actions;
         @XmlAttribute(
            name = "entity-name",
            required = true
         )
         protected String entityName;
         @XmlAttribute(
            required = true
         )
         protected String role;

         public String getActions() {
            return this.actions;
         }

         public void setActions(String value) {
            this.actions = value;
         }

         public String getEntityName() {
            return this.entityName;
         }

         public void setEntityName(String value) {
            this.entityName = value;
         }

         public String getRole() {
            return this.role;
         }

         public void setRole(String value) {
            this.role = value;
         }
      }
   }

   @XmlAccessorType(XmlAccessType.FIELD)
   @XmlType(
      name = "",
      propOrder = {"property", "mapping", "classCacheOrCollectionCache", "event", "listener"}
   )
   public static class JaxbSessionFactory {
      protected List property;
      protected List mapping;
      @XmlElements({@XmlElement(
   name = "class-cache",
   type = JaxbClassCache.class
), @XmlElement(
   name = "collection-cache",
   type = JaxbCollectionCache.class
)})
      protected List classCacheOrCollectionCache;
      protected List event;
      protected List listener;
      @XmlAttribute
      protected String name;

      public List getProperty() {
         if (this.property == null) {
            this.property = new ArrayList();
         }

         return this.property;
      }

      public List getMapping() {
         if (this.mapping == null) {
            this.mapping = new ArrayList();
         }

         return this.mapping;
      }

      public List getClassCacheOrCollectionCache() {
         if (this.classCacheOrCollectionCache == null) {
            this.classCacheOrCollectionCache = new ArrayList();
         }

         return this.classCacheOrCollectionCache;
      }

      public List getEvent() {
         if (this.event == null) {
            this.event = new ArrayList();
         }

         return this.event;
      }

      public List getListener() {
         if (this.listener == null) {
            this.listener = new ArrayList();
         }

         return this.listener;
      }

      public String getName() {
         return this.name;
      }

      public void setName(String value) {
         this.name = value;
      }

      @XmlAccessorType(XmlAccessType.FIELD)
      @XmlType(
         name = ""
      )
      public static class JaxbClassCache {
         @XmlAttribute(
            name = "class",
            required = true
         )
         protected String clazz;
         @XmlAttribute
         @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
         protected String include;
         @XmlAttribute
         protected String region;
         @XmlAttribute(
            required = true
         )
         protected JaxbUsageAttribute usage;

         public String getClazz() {
            return this.clazz;
         }

         public void setClazz(String value) {
            this.clazz = value;
         }

         public String getInclude() {
            return this.include == null ? "all" : this.include;
         }

         public void setInclude(String value) {
            this.include = value;
         }

         public String getRegion() {
            return this.region;
         }

         public void setRegion(String value) {
            this.region = value;
         }

         public JaxbUsageAttribute getUsage() {
            return this.usage;
         }

         public void setUsage(JaxbUsageAttribute value) {
            this.usage = value;
         }
      }

      @XmlAccessorType(XmlAccessType.FIELD)
      @XmlType(
         name = ""
      )
      public static class JaxbCollectionCache {
         @XmlAttribute(
            required = true
         )
         protected String collection;
         @XmlAttribute
         protected String region;
         @XmlAttribute(
            required = true
         )
         protected JaxbUsageAttribute usage;

         public String getCollection() {
            return this.collection;
         }

         public void setCollection(String value) {
            this.collection = value;
         }

         public String getRegion() {
            return this.region;
         }

         public void setRegion(String value) {
            this.region = value;
         }

         public JaxbUsageAttribute getUsage() {
            return this.usage;
         }

         public void setUsage(JaxbUsageAttribute value) {
            this.usage = value;
         }
      }

      @XmlAccessorType(XmlAccessType.FIELD)
      @XmlType(
         name = "",
         propOrder = {"listener"}
      )
      public static class JaxbEvent {
         protected List listener;
         @XmlAttribute(
            required = true
         )
         protected JaxbTypeAttribute type;

         public List getListener() {
            if (this.listener == null) {
               this.listener = new ArrayList();
            }

            return this.listener;
         }

         public JaxbTypeAttribute getType() {
            return this.type;
         }

         public void setType(JaxbTypeAttribute value) {
            this.type = value;
         }
      }

      @XmlAccessorType(XmlAccessType.FIELD)
      @XmlType(
         name = ""
      )
      public static class JaxbMapping {
         @XmlAttribute(
            name = "class"
         )
         protected String clazz;
         @XmlAttribute
         protected String file;
         @XmlAttribute
         protected String jar;
         @XmlAttribute(
            name = "package"
         )
         protected String _package;
         @XmlAttribute
         protected String resource;

         public String getClazz() {
            return this.clazz;
         }

         public void setClazz(String value) {
            this.clazz = value;
         }

         public String getFile() {
            return this.file;
         }

         public void setFile(String value) {
            this.file = value;
         }

         public String getJar() {
            return this.jar;
         }

         public void setJar(String value) {
            this.jar = value;
         }

         public String getPackage() {
            return this._package;
         }

         public void setPackage(String value) {
            this._package = value;
         }

         public String getResource() {
            return this.resource;
         }

         public void setResource(String value) {
            this.resource = value;
         }
      }

      @XmlAccessorType(XmlAccessType.FIELD)
      @XmlType(
         name = "",
         propOrder = {"value"}
      )
      public static class JaxbProperty {
         @XmlValue
         protected String value;
         @XmlAttribute(
            required = true
         )
         protected String name;

         public String getValue() {
            return this.value;
         }

         public void setValue(String value) {
            this.value = value;
         }

         public String getName() {
            return this.name;
         }

         public void setName(String value) {
            this.name = value;
         }
      }
   }
}
