package org.hibernate.cfg.annotations.reflection;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityListeners;
import javax.persistence.NamedNativeQuery;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.TableGenerator;
import org.dom4j.Element;
import org.hibernate.annotations.common.reflection.AnnotationReader;
import org.hibernate.annotations.common.reflection.MetadataProvider;
import org.hibernate.annotations.common.reflection.java.JavaMetadataProvider;
import org.hibernate.internal.util.ReflectHelper;

public class JPAMetadataProvider implements MetadataProvider, Serializable {
   private transient MetadataProvider delegate = new JavaMetadataProvider();
   private transient Map defaults;
   private transient Map cache = new HashMap(100);
   private XMLContext xmlContext = new XMLContext();

   private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
      ois.defaultReadObject();
      this.delegate = new JavaMetadataProvider();
      this.cache = new HashMap(100);
   }

   public AnnotationReader getAnnotationReader(AnnotatedElement annotatedElement) {
      AnnotationReader reader = (AnnotationReader)this.cache.get(annotatedElement);
      if (reader == null) {
         if (this.xmlContext.hasContext()) {
            reader = new JPAOverriddenAnnotationReader(annotatedElement, this.xmlContext);
         } else {
            reader = this.delegate.getAnnotationReader(annotatedElement);
         }

         this.cache.put(annotatedElement, reader);
      }

      return reader;
   }

   public Map getDefaults() {
      if (this.defaults == null) {
         this.defaults = new HashMap();
         XMLContext.Default xmlDefaults = this.xmlContext.getDefault((String)null);
         this.defaults.put("schema", xmlDefaults.getSchema());
         this.defaults.put("catalog", xmlDefaults.getCatalog());
         this.defaults.put("delimited-identifier", xmlDefaults.getDelimitedIdentifier());
         List<Class> entityListeners = new ArrayList();

         for(String className : this.xmlContext.getDefaultEntityListeners()) {
            try {
               entityListeners.add(ReflectHelper.classForName(className, this.getClass()));
            } catch (ClassNotFoundException var14) {
               throw new IllegalStateException("Default entity listener class not found: " + className);
            }
         }

         this.defaults.put(EntityListeners.class, entityListeners);

         for(Element element : this.xmlContext.getAllDocuments()) {
            List<Element> elements = element.elements("sequence-generator");
            List<SequenceGenerator> sequenceGenerators = (List)this.defaults.get(SequenceGenerator.class);
            if (sequenceGenerators == null) {
               sequenceGenerators = new ArrayList();
               this.defaults.put(SequenceGenerator.class, sequenceGenerators);
            }

            for(Element subelement : elements) {
               sequenceGenerators.add(JPAOverriddenAnnotationReader.buildSequenceGeneratorAnnotation(subelement));
            }

            elements = element.elements("table-generator");
            List<TableGenerator> tableGenerators = (List)this.defaults.get(TableGenerator.class);
            if (tableGenerators == null) {
               tableGenerators = new ArrayList();
               this.defaults.put(TableGenerator.class, tableGenerators);
            }

            for(Element subelement : elements) {
               tableGenerators.add(JPAOverriddenAnnotationReader.buildTableGeneratorAnnotation(subelement, xmlDefaults));
            }

            List<NamedQuery> namedQueries = (List)this.defaults.get(NamedQuery.class);
            if (namedQueries == null) {
               namedQueries = new ArrayList();
               this.defaults.put(NamedQuery.class, namedQueries);
            }

            List<NamedQuery> currentNamedQueries = JPAOverriddenAnnotationReader.buildNamedQueries(element, false, xmlDefaults);
            namedQueries.addAll(currentNamedQueries);
            List<NamedNativeQuery> namedNativeQueries = (List)this.defaults.get(NamedNativeQuery.class);
            if (namedNativeQueries == null) {
               namedNativeQueries = new ArrayList();
               this.defaults.put(NamedNativeQuery.class, namedNativeQueries);
            }

            List<NamedNativeQuery> currentNamedNativeQueries = JPAOverriddenAnnotationReader.buildNamedQueries(element, true, xmlDefaults);
            namedNativeQueries.addAll(currentNamedNativeQueries);
            List<SqlResultSetMapping> sqlResultSetMappings = (List)this.defaults.get(SqlResultSetMapping.class);
            if (sqlResultSetMappings == null) {
               sqlResultSetMappings = new ArrayList();
               this.defaults.put(SqlResultSetMapping.class, sqlResultSetMappings);
            }

            List<SqlResultSetMapping> currentSqlResultSetMappings = JPAOverriddenAnnotationReader.buildSqlResultsetMappings(element, xmlDefaults);
            sqlResultSetMappings.addAll(currentSqlResultSetMappings);
         }
      }

      return this.defaults;
   }

   public XMLContext getXMLContext() {
      return this.xmlContext;
   }
}
