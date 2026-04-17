package org.hibernate.cfg.annotations.reflection;

import java.beans.Introspector;
import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Basic;
import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ColumnResult;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EntityResult;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ExcludeDefaultListeners;
import javax.persistence.ExcludeSuperclassListeners;
import javax.persistence.FetchType;
import javax.persistence.FieldResult;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.MapKeyClass;
import javax.persistence.MapKeyColumn;
import javax.persistence.MapKeyEnumerated;
import javax.persistence.MapKeyJoinColumn;
import javax.persistence.MapKeyJoinColumns;
import javax.persistence.MapKeyTemporal;
import javax.persistence.MappedSuperclass;
import javax.persistence.MapsId;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.OrderColumn;
import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.PrimaryKeyJoinColumns;
import javax.persistence.QueryHint;
import javax.persistence.SecondaryTable;
import javax.persistence.SecondaryTables;
import javax.persistence.SequenceGenerator;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.SqlResultSetMappings;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;
import org.dom4j.Attribute;
import org.dom4j.Element;
import org.hibernate.AnnotationException;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Columns;
import org.hibernate.annotations.common.annotationfactory.AnnotationDescriptor;
import org.hibernate.annotations.common.annotationfactory.AnnotationFactory;
import org.hibernate.annotations.common.reflection.AnnotationReader;
import org.hibernate.annotations.common.reflection.Filter;
import org.hibernate.annotations.common.reflection.ReflectionUtil;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.ReflectHelper;
import org.hibernate.internal.util.StringHelper;
import org.jboss.logging.Logger;

public class JPAOverriddenAnnotationReader implements AnnotationReader {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, JPAOverriddenAnnotationReader.class.getName());
   private static final Map annotationToXml = new HashMap();
   private static final String SCHEMA_VALIDATION = "Activate schema validation for more information";
   private static final Filter FILTER = new Filter() {
      public boolean returnStatic() {
         return false;
      }

      public boolean returnTransient() {
         return false;
      }
   };
   private XMLContext xmlContext;
   private String className;
   private String propertyName;
   private PropertyType propertyType;
   private transient Annotation[] annotations;
   private transient Map annotationsMap;
   private static final String WORD_SEPARATOR = "-";
   private transient List elementsForProperty;
   private AccessibleObject mirroredAttribute;
   private final AnnotatedElement element;

   public JPAOverriddenAnnotationReader(AnnotatedElement el, XMLContext xmlContext) {
      this.element = el;
      this.xmlContext = xmlContext;
      if (el instanceof Class) {
         Class clazz = (Class)el;
         this.className = clazz.getName();
      } else if (el instanceof Field) {
         Field field = (Field)el;
         this.className = field.getDeclaringClass().getName();
         this.propertyName = field.getName();
         this.propertyType = JPAOverriddenAnnotationReader.PropertyType.FIELD;
         String expectedGetter = "get" + Character.toUpperCase(this.propertyName.charAt(0)) + this.propertyName.substring(1);

         try {
            this.mirroredAttribute = field.getDeclaringClass().getDeclaredMethod(expectedGetter);
         } catch (NoSuchMethodException var7) {
         }
      } else if (el instanceof Method) {
         Method method = (Method)el;
         this.className = method.getDeclaringClass().getName();
         this.propertyName = method.getName();
         if (ReflectionUtil.isProperty(method, (Type)null, FILTER)) {
            if (this.propertyName.startsWith("get")) {
               this.propertyName = Introspector.decapitalize(this.propertyName.substring("get".length()));
            } else {
               if (!this.propertyName.startsWith("is")) {
                  throw new RuntimeException("Method " + this.propertyName + " is not a property getter");
               }

               this.propertyName = Introspector.decapitalize(this.propertyName.substring("is".length()));
            }

            this.propertyType = JPAOverriddenAnnotationReader.PropertyType.PROPERTY;

            try {
               this.mirroredAttribute = method.getDeclaringClass().getDeclaredField(this.propertyName);
            } catch (NoSuchFieldException var6) {
            }
         } else {
            this.propertyType = JPAOverriddenAnnotationReader.PropertyType.METHOD;
         }
      } else {
         this.className = null;
         this.propertyName = null;
      }

   }

   public Annotation getAnnotation(Class annotationType) {
      this.initAnnotations();
      return (Annotation)this.annotationsMap.get(annotationType);
   }

   public boolean isAnnotationPresent(Class annotationType) {
      this.initAnnotations();
      return (Annotation)this.annotationsMap.get(annotationType) != null;
   }

   public Annotation[] getAnnotations() {
      this.initAnnotations();
      return this.annotations;
   }

   private void initAnnotations() {
      if (this.annotations == null) {
         XMLContext.Default defaults = this.xmlContext.getDefault(this.className);
         if (this.className != null && this.propertyName == null) {
            Element tree = this.xmlContext.getXMLTree(this.className);
            Annotation[] annotations = this.getJavaAnnotations();
            List<Annotation> annotationList = new ArrayList(annotations.length + 5);
            this.annotationsMap = new HashMap(annotations.length + 5);

            for(Annotation annotation : annotations) {
               if (!annotationToXml.containsKey(annotation.annotationType())) {
                  annotationList.add(annotation);
               }
            }

            this.addIfNotNull(annotationList, this.getEntity(tree, defaults));
            this.addIfNotNull(annotationList, this.getMappedSuperclass(tree, defaults));
            this.addIfNotNull(annotationList, this.getEmbeddable(tree, defaults));
            this.addIfNotNull(annotationList, this.getTable(tree, defaults));
            this.addIfNotNull(annotationList, this.getSecondaryTables(tree, defaults));
            this.addIfNotNull(annotationList, this.getPrimaryKeyJoinColumns(tree, defaults, true));
            this.addIfNotNull(annotationList, this.getIdClass(tree, defaults));
            this.addIfNotNull(annotationList, this.getCacheable(tree, defaults));
            this.addIfNotNull(annotationList, this.getInheritance(tree, defaults));
            this.addIfNotNull(annotationList, this.getDiscriminatorValue(tree, defaults));
            this.addIfNotNull(annotationList, this.getDiscriminatorColumn(tree, defaults));
            this.addIfNotNull(annotationList, this.getSequenceGenerator(tree, defaults));
            this.addIfNotNull(annotationList, this.getTableGenerator(tree, defaults));
            this.addIfNotNull(annotationList, this.getNamedQueries(tree, defaults));
            this.addIfNotNull(annotationList, this.getNamedNativeQueries(tree, defaults));
            this.addIfNotNull(annotationList, this.getSqlResultSetMappings(tree, defaults));
            this.addIfNotNull(annotationList, this.getExcludeDefaultListeners(tree, defaults));
            this.addIfNotNull(annotationList, this.getExcludeSuperclassListeners(tree, defaults));
            this.addIfNotNull(annotationList, this.getAccessType(tree, defaults));
            this.addIfNotNull(annotationList, this.getAttributeOverrides(tree, defaults, true));
            this.addIfNotNull(annotationList, this.getAssociationOverrides(tree, defaults, true));
            this.addIfNotNull(annotationList, this.getEntityListeners(tree, defaults));
            this.annotations = (Annotation[])annotationList.toArray(new Annotation[annotationList.size()]);

            for(Annotation ann : this.annotations) {
               this.annotationsMap.put(ann.annotationType(), ann);
            }

            this.checkForOrphanProperties(tree);
         } else if (this.className != null) {
            Element tree = this.xmlContext.getXMLTree(this.className);
            Annotation[] annotations = this.getJavaAnnotations();
            List<Annotation> annotationList = new ArrayList(annotations.length + 5);
            this.annotationsMap = new HashMap(annotations.length + 5);

            for(Annotation annotation : annotations) {
               if (!annotationToXml.containsKey(annotation.annotationType())) {
                  annotationList.add(annotation);
               }
            }

            this.preCalculateElementsForProperty(tree);
            Transient transientAnn = this.getTransient(defaults);
            if (transientAnn != null) {
               annotationList.add(transientAnn);
            } else {
               if (defaults.canUseJavaAnnotations()) {
                  Annotation annotation = this.getJavaAnnotation(Access.class);
                  this.addIfNotNull(annotationList, annotation);
               }

               this.getId(annotationList, defaults);
               this.getEmbeddedId(annotationList, defaults);
               this.getEmbedded(annotationList, defaults);
               this.getBasic(annotationList, defaults);
               this.getVersion(annotationList, defaults);
               this.getAssociation(ManyToOne.class, annotationList, defaults);
               this.getAssociation(OneToOne.class, annotationList, defaults);
               this.getAssociation(OneToMany.class, annotationList, defaults);
               this.getAssociation(ManyToMany.class, annotationList, defaults);
               this.getElementCollection(annotationList, defaults);
               this.addIfNotNull(annotationList, this.getSequenceGenerator(this.elementsForProperty, defaults));
               this.addIfNotNull(annotationList, this.getTableGenerator(this.elementsForProperty, defaults));
            }

            this.processEventAnnotations(annotationList, defaults);
            this.annotations = (Annotation[])annotationList.toArray(new Annotation[annotationList.size()]);

            for(Annotation ann : this.annotations) {
               this.annotationsMap.put(ann.annotationType(), ann);
            }
         } else {
            this.annotations = this.getJavaAnnotations();
            this.annotationsMap = new HashMap(this.annotations.length + 5);

            for(Annotation ann : this.annotations) {
               this.annotationsMap.put(ann.annotationType(), ann);
            }
         }
      }

   }

   private void checkForOrphanProperties(Element tree) {
      Class clazz;
      try {
         clazz = ReflectHelper.classForName(this.className, this.getClass());
      } catch (ClassNotFoundException var10) {
         return;
      }

      Element element = tree != null ? tree.element("attributes") : null;
      if (element != null) {
         Set<String> properties = new HashSet();

         for(Field field : clazz.getFields()) {
            properties.add(field.getName());
         }

         for(Method method : clazz.getMethods()) {
            String name = method.getName();
            if (name.startsWith("get")) {
               properties.add(Introspector.decapitalize(name.substring("get".length())));
            } else if (name.startsWith("is")) {
               properties.add(Introspector.decapitalize(name.substring("is".length())));
            }
         }

         for(Element subelement : element.elements()) {
            String propertyName = subelement.attributeValue("name");
            if (!properties.contains(propertyName)) {
               LOG.propertyNotFound(StringHelper.qualify(this.className, propertyName));
            }
         }
      }

   }

   private Annotation addIfNotNull(List annotationList, Annotation annotation) {
      if (annotation != null) {
         annotationList.add(annotation);
      }

      return annotation;
   }

   private Annotation getTableGenerator(List elementsForProperty, XMLContext.Default defaults) {
      for(Element element : elementsForProperty) {
         Element subelement = element != null ? element.element((String)annotationToXml.get(TableGenerator.class)) : null;
         if (subelement != null) {
            return buildTableGeneratorAnnotation(subelement, defaults);
         }
      }

      if (elementsForProperty.size() == 0 && defaults.canUseJavaAnnotations()) {
         return this.getJavaAnnotation(TableGenerator.class);
      } else {
         return null;
      }
   }

   private Annotation getSequenceGenerator(List elementsForProperty, XMLContext.Default defaults) {
      for(Element element : elementsForProperty) {
         Element subelement = element != null ? element.element((String)annotationToXml.get(SequenceGenerator.class)) : null;
         if (subelement != null) {
            return buildSequenceGeneratorAnnotation(subelement);
         }
      }

      if (elementsForProperty.size() == 0 && defaults.canUseJavaAnnotations()) {
         return this.getJavaAnnotation(SequenceGenerator.class);
      } else {
         return null;
      }
   }

   private void processEventAnnotations(List annotationList, XMLContext.Default defaults) {
      boolean eventElement = false;

      for(Element element : this.elementsForProperty) {
         String elementName = element.getName();
         if ("pre-persist".equals(elementName)) {
            AnnotationDescriptor ad = new AnnotationDescriptor(PrePersist.class);
            annotationList.add(AnnotationFactory.create(ad));
            eventElement = true;
         } else if ("pre-remove".equals(elementName)) {
            AnnotationDescriptor ad = new AnnotationDescriptor(PreRemove.class);
            annotationList.add(AnnotationFactory.create(ad));
            eventElement = true;
         } else if ("pre-update".equals(elementName)) {
            AnnotationDescriptor ad = new AnnotationDescriptor(PreUpdate.class);
            annotationList.add(AnnotationFactory.create(ad));
            eventElement = true;
         } else if ("post-persist".equals(elementName)) {
            AnnotationDescriptor ad = new AnnotationDescriptor(PostPersist.class);
            annotationList.add(AnnotationFactory.create(ad));
            eventElement = true;
         } else if ("post-remove".equals(elementName)) {
            AnnotationDescriptor ad = new AnnotationDescriptor(PostRemove.class);
            annotationList.add(AnnotationFactory.create(ad));
            eventElement = true;
         } else if ("post-update".equals(elementName)) {
            AnnotationDescriptor ad = new AnnotationDescriptor(PostUpdate.class);
            annotationList.add(AnnotationFactory.create(ad));
            eventElement = true;
         } else if ("post-load".equals(elementName)) {
            AnnotationDescriptor ad = new AnnotationDescriptor(PostLoad.class);
            annotationList.add(AnnotationFactory.create(ad));
            eventElement = true;
         }
      }

      if (!eventElement && defaults.canUseJavaAnnotations()) {
         Annotation ann = this.getJavaAnnotation(PrePersist.class);
         this.addIfNotNull(annotationList, ann);
         ann = this.getJavaAnnotation(PreRemove.class);
         this.addIfNotNull(annotationList, ann);
         ann = this.getJavaAnnotation(PreUpdate.class);
         this.addIfNotNull(annotationList, ann);
         ann = this.getJavaAnnotation(PostPersist.class);
         this.addIfNotNull(annotationList, ann);
         ann = this.getJavaAnnotation(PostRemove.class);
         this.addIfNotNull(annotationList, ann);
         ann = this.getJavaAnnotation(PostUpdate.class);
         this.addIfNotNull(annotationList, ann);
         ann = this.getJavaAnnotation(PostLoad.class);
         this.addIfNotNull(annotationList, ann);
      }

   }

   private EntityListeners getEntityListeners(Element tree, XMLContext.Default defaults) {
      Element element = tree != null ? tree.element("entity-listeners") : null;
      if (element == null) {
         return defaults.canUseJavaAnnotations() ? (EntityListeners)this.getJavaAnnotation(EntityListeners.class) : null;
      } else {
         List<Class> entityListenerClasses = new ArrayList();

         for(Element subelement : element.elements("entity-listener")) {
            String className = subelement.attributeValue("class");

            try {
               entityListenerClasses.add(ReflectHelper.classForName(XMLContext.buildSafeClassName(className, defaults), this.getClass()));
            } catch (ClassNotFoundException e) {
               throw new AnnotationException("Unable to find " + element.getPath() + ".class: " + className, e);
            }
         }

         AnnotationDescriptor ad = new AnnotationDescriptor(EntityListeners.class);
         ad.setValue("value", entityListenerClasses.toArray(new Class[entityListenerClasses.size()]));
         return (EntityListeners)AnnotationFactory.create(ad);
      }
   }

   private JoinTable overridesDefaultsInJoinTable(Annotation annotation, XMLContext.Default defaults) {
      boolean defaultToJoinTable = !this.isJavaAnnotationPresent(JoinColumn.class) && !this.isJavaAnnotationPresent(JoinColumns.class);
      Class<? extends Annotation> annotationClass = annotation.annotationType();
      defaultToJoinTable = defaultToJoinTable && (annotationClass == ManyToMany.class && StringHelper.isEmpty(((ManyToMany)annotation).mappedBy()) || annotationClass == OneToMany.class && StringHelper.isEmpty(((OneToMany)annotation).mappedBy()) || annotationClass == ElementCollection.class);
      Class<JoinTable> annotationType = JoinTable.class;
      if (!defaultToJoinTable || !StringHelper.isNotEmpty(defaults.getCatalog()) && !StringHelper.isNotEmpty(defaults.getSchema())) {
         return defaults.canUseJavaAnnotations() ? (JoinTable)this.getJavaAnnotation(annotationType) : null;
      } else {
         AnnotationDescriptor ad = new AnnotationDescriptor(annotationType);
         if (defaults.canUseJavaAnnotations()) {
            JoinTable table = (JoinTable)this.getJavaAnnotation(annotationType);
            if (table != null) {
               ad.setValue("name", table.name());
               ad.setValue("schema", table.schema());
               ad.setValue("catalog", table.catalog());
               ad.setValue("uniqueConstraints", table.uniqueConstraints());
               ad.setValue("joinColumns", table.joinColumns());
               ad.setValue("inverseJoinColumns", table.inverseJoinColumns());
            }
         }

         if (StringHelper.isEmpty((String)ad.valueOf("schema")) && StringHelper.isNotEmpty(defaults.getSchema())) {
            ad.setValue("schema", defaults.getSchema());
         }

         if (StringHelper.isEmpty((String)ad.valueOf("catalog")) && StringHelper.isNotEmpty(defaults.getCatalog())) {
            ad.setValue("catalog", defaults.getCatalog());
         }

         return (JoinTable)AnnotationFactory.create(ad);
      }
   }

   private void getJoinTable(List annotationList, Element tree, XMLContext.Default defaults) {
      this.addIfNotNull(annotationList, this.buildJoinTable(tree, defaults));
   }

   private JoinTable buildJoinTable(Element tree, XMLContext.Default defaults) {
      Element subelement = tree == null ? null : tree.element("join-table");
      Class<JoinTable> annotationType = JoinTable.class;
      if (subelement == null) {
         return null;
      } else {
         AnnotationDescriptor annotation = new AnnotationDescriptor(annotationType);
         copyStringAttribute(annotation, subelement, "name", false);
         copyStringAttribute(annotation, subelement, "catalog", false);
         if (StringHelper.isNotEmpty(defaults.getCatalog()) && StringHelper.isEmpty((String)annotation.valueOf("catalog"))) {
            annotation.setValue("catalog", defaults.getCatalog());
         }

         copyStringAttribute(annotation, subelement, "schema", false);
         if (StringHelper.isNotEmpty(defaults.getSchema()) && StringHelper.isEmpty((String)annotation.valueOf("schema"))) {
            annotation.setValue("schema", defaults.getSchema());
         }

         buildUniqueConstraints(annotation, subelement);
         annotation.setValue("joinColumns", this.getJoinColumns(subelement, false));
         annotation.setValue("inverseJoinColumns", this.getJoinColumns(subelement, true));
         return (JoinTable)AnnotationFactory.create(annotation);
      }
   }

   private void getAssociation(Class annotationType, List annotationList, XMLContext.Default defaults) {
      String xmlName = (String)annotationToXml.get(annotationType);

      for(Element element : this.elementsForProperty) {
         if (xmlName.equals(element.getName())) {
            AnnotationDescriptor ad = new AnnotationDescriptor(annotationType);
            this.addTargetClass(element, ad, "target-entity", defaults);
            this.getFetchType(ad, element);
            this.getCascades(ad, element, defaults);
            this.getJoinTable(annotationList, element, defaults);
            this.buildJoinColumns(annotationList, element);
            Annotation annotation = this.getPrimaryKeyJoinColumns(element, defaults, false);
            this.addIfNotNull(annotationList, annotation);
            copyBooleanAttribute(ad, element, "optional");
            copyBooleanAttribute(ad, element, "orphan-removal");
            copyStringAttribute(ad, element, "mapped-by", false);
            this.getOrderBy(annotationList, element);
            this.getMapKey(annotationList, element);
            this.getMapKeyClass(annotationList, element, defaults);
            this.getMapKeyColumn(annotationList, element);
            this.getOrderColumn(annotationList, element);
            this.getMapKeyTemporal(annotationList, element);
            this.getMapKeyEnumerated(annotationList, element);
            Annotation var53 = this.getMapKeyAttributeOverrides(element, defaults);
            this.addIfNotNull(annotationList, var53);
            this.buildMapKeyJoinColumns(annotationList, element);
            this.getAssociationId(annotationList, element);
            this.getMapsId(annotationList, element);
            annotationList.add(AnnotationFactory.create(ad));
            this.getAccessType(annotationList, element);
         }
      }

      if (this.elementsForProperty.size() == 0 && defaults.canUseJavaAnnotations()) {
         Annotation annotation = this.getJavaAnnotation(annotationType);
         if (annotation != null) {
            annotationList.add(annotation);
            Annotation var10 = this.overridesDefaultsInJoinTable(annotation, defaults);
            this.addIfNotNull(annotationList, var10);
            Annotation var11 = this.getJavaAnnotation(JoinColumn.class);
            this.addIfNotNull(annotationList, var11);
            var11 = this.getJavaAnnotation(JoinColumns.class);
            this.addIfNotNull(annotationList, var11);
            var11 = this.getJavaAnnotation(PrimaryKeyJoinColumn.class);
            this.addIfNotNull(annotationList, var11);
            var11 = this.getJavaAnnotation(PrimaryKeyJoinColumns.class);
            this.addIfNotNull(annotationList, var11);
            var11 = this.getJavaAnnotation(MapKey.class);
            this.addIfNotNull(annotationList, var11);
            var11 = this.getJavaAnnotation(OrderBy.class);
            this.addIfNotNull(annotationList, var11);
            var11 = this.getJavaAnnotation(AttributeOverride.class);
            this.addIfNotNull(annotationList, var11);
            var11 = this.getJavaAnnotation(AttributeOverrides.class);
            this.addIfNotNull(annotationList, var11);
            var11 = this.getJavaAnnotation(AssociationOverride.class);
            this.addIfNotNull(annotationList, var11);
            var11 = this.getJavaAnnotation(AssociationOverrides.class);
            this.addIfNotNull(annotationList, var11);
            var11 = this.getJavaAnnotation(Lob.class);
            this.addIfNotNull(annotationList, var11);
            var11 = this.getJavaAnnotation(Enumerated.class);
            this.addIfNotNull(annotationList, var11);
            var11 = this.getJavaAnnotation(Temporal.class);
            this.addIfNotNull(annotationList, var11);
            var11 = this.getJavaAnnotation(Column.class);
            this.addIfNotNull(annotationList, var11);
            var11 = this.getJavaAnnotation(Columns.class);
            this.addIfNotNull(annotationList, var11);
            var11 = this.getJavaAnnotation(MapKeyClass.class);
            this.addIfNotNull(annotationList, var11);
            var11 = this.getJavaAnnotation(MapKeyTemporal.class);
            this.addIfNotNull(annotationList, var11);
            var11 = this.getJavaAnnotation(MapKeyEnumerated.class);
            this.addIfNotNull(annotationList, var11);
            var11 = this.getJavaAnnotation(MapKeyColumn.class);
            this.addIfNotNull(annotationList, var11);
            var11 = this.getJavaAnnotation(MapKeyJoinColumn.class);
            this.addIfNotNull(annotationList, var11);
            var11 = this.getJavaAnnotation(MapKeyJoinColumns.class);
            this.addIfNotNull(annotationList, var11);
            var11 = this.getJavaAnnotation(OrderColumn.class);
            this.addIfNotNull(annotationList, var11);
            var11 = this.getJavaAnnotation(Cascade.class);
            this.addIfNotNull(annotationList, var11);
         } else if (this.isJavaAnnotationPresent(ElementCollection.class)) {
            Annotation var34 = this.overridesDefaultsInJoinTable(this.getJavaAnnotation(ElementCollection.class), defaults);
            this.addIfNotNull(annotationList, var34);
            Annotation var35 = this.getJavaAnnotation(MapKey.class);
            this.addIfNotNull(annotationList, var35);
            var35 = this.getJavaAnnotation(OrderBy.class);
            this.addIfNotNull(annotationList, var35);
            var35 = this.getJavaAnnotation(AttributeOverride.class);
            this.addIfNotNull(annotationList, var35);
            var35 = this.getJavaAnnotation(AttributeOverrides.class);
            this.addIfNotNull(annotationList, var35);
            var35 = this.getJavaAnnotation(AssociationOverride.class);
            this.addIfNotNull(annotationList, var35);
            var35 = this.getJavaAnnotation(AssociationOverrides.class);
            this.addIfNotNull(annotationList, var35);
            var35 = this.getJavaAnnotation(Lob.class);
            this.addIfNotNull(annotationList, var35);
            var35 = this.getJavaAnnotation(Enumerated.class);
            this.addIfNotNull(annotationList, var35);
            var35 = this.getJavaAnnotation(Temporal.class);
            this.addIfNotNull(annotationList, var35);
            var35 = this.getJavaAnnotation(Column.class);
            this.addIfNotNull(annotationList, var35);
            var35 = this.getJavaAnnotation(OrderColumn.class);
            this.addIfNotNull(annotationList, var35);
            var35 = this.getJavaAnnotation(MapKeyClass.class);
            this.addIfNotNull(annotationList, var35);
            var35 = this.getJavaAnnotation(MapKeyTemporal.class);
            this.addIfNotNull(annotationList, var35);
            var35 = this.getJavaAnnotation(MapKeyEnumerated.class);
            this.addIfNotNull(annotationList, var35);
            var35 = this.getJavaAnnotation(MapKeyColumn.class);
            this.addIfNotNull(annotationList, var35);
            var35 = this.getJavaAnnotation(MapKeyJoinColumn.class);
            this.addIfNotNull(annotationList, var35);
            var35 = this.getJavaAnnotation(MapKeyJoinColumns.class);
            this.addIfNotNull(annotationList, var35);
            var35 = this.getJavaAnnotation(CollectionTable.class);
            this.addIfNotNull(annotationList, var35);
         }
      }

   }

   private void buildMapKeyJoinColumns(List annotationList, Element element) {
      MapKeyJoinColumn[] joinColumns = this.getMapKeyJoinColumns(element);
      if (joinColumns.length > 0) {
         AnnotationDescriptor ad = new AnnotationDescriptor(MapKeyJoinColumns.class);
         ad.setValue("value", joinColumns);
         annotationList.add(AnnotationFactory.create(ad));
      }

   }

   private MapKeyJoinColumn[] getMapKeyJoinColumns(Element element) {
      List<Element> subelements = element != null ? element.elements("map-key-join-column") : null;
      List<MapKeyJoinColumn> joinColumns = new ArrayList();
      if (subelements != null) {
         for(Element subelement : subelements) {
            AnnotationDescriptor column = new AnnotationDescriptor(MapKeyJoinColumn.class);
            copyStringAttribute(column, subelement, "name", false);
            copyStringAttribute(column, subelement, "referenced-column-name", false);
            copyBooleanAttribute(column, subelement, "unique");
            copyBooleanAttribute(column, subelement, "nullable");
            copyBooleanAttribute(column, subelement, "insertable");
            copyBooleanAttribute(column, subelement, "updatable");
            copyStringAttribute(column, subelement, "column-definition", false);
            copyStringAttribute(column, subelement, "table", false);
            joinColumns.add((MapKeyJoinColumn)AnnotationFactory.create(column));
         }
      }

      return (MapKeyJoinColumn[])joinColumns.toArray(new MapKeyJoinColumn[joinColumns.size()]);
   }

   private AttributeOverrides getMapKeyAttributeOverrides(Element tree, XMLContext.Default defaults) {
      List<AttributeOverride> attributes = this.buildAttributeOverrides(tree, "map-key-attribute-override");
      return this.mergeAttributeOverrides(defaults, attributes, false);
   }

   private Cacheable getCacheable(Element element, XMLContext.Default defaults) {
      if (element != null) {
         String attValue = element.attributeValue("cacheable");
         if (attValue != null) {
            AnnotationDescriptor ad = new AnnotationDescriptor(Cacheable.class);
            ad.setValue("value", Boolean.valueOf(attValue));
            return (Cacheable)AnnotationFactory.create(ad);
         }
      }

      return defaults.canUseJavaAnnotations() ? (Cacheable)this.getJavaAnnotation(Cacheable.class) : null;
   }

   private void getMapKeyEnumerated(List annotationList, Element element) {
      Element subelement = element != null ? element.element("map-key-enumerated") : null;
      if (subelement != null) {
         AnnotationDescriptor ad = new AnnotationDescriptor(MapKeyEnumerated.class);
         EnumType value = EnumType.valueOf(subelement.getTextTrim());
         ad.setValue("value", value);
         annotationList.add(AnnotationFactory.create(ad));
      }

   }

   private void getMapKeyTemporal(List annotationList, Element element) {
      Element subelement = element != null ? element.element("map-key-temporal") : null;
      if (subelement != null) {
         AnnotationDescriptor ad = new AnnotationDescriptor(MapKeyTemporal.class);
         TemporalType value = TemporalType.valueOf(subelement.getTextTrim());
         ad.setValue("value", value);
         annotationList.add(AnnotationFactory.create(ad));
      }

   }

   private void getOrderColumn(List annotationList, Element element) {
      Element subelement = element != null ? element.element("order-column") : null;
      if (subelement != null) {
         AnnotationDescriptor ad = new AnnotationDescriptor(OrderColumn.class);
         copyStringAttribute(ad, subelement, "name", false);
         copyBooleanAttribute(ad, subelement, "nullable");
         copyBooleanAttribute(ad, subelement, "insertable");
         copyBooleanAttribute(ad, subelement, "updatable");
         copyStringAttribute(ad, subelement, "column-definition", false);
         annotationList.add(AnnotationFactory.create(ad));
      }

   }

   private void getMapsId(List annotationList, Element element) {
      String attrVal = element.attributeValue("maps-id");
      if (attrVal != null) {
         AnnotationDescriptor ad = new AnnotationDescriptor(MapsId.class);
         ad.setValue("value", attrVal);
         annotationList.add(AnnotationFactory.create(ad));
      }

   }

   private void getAssociationId(List annotationList, Element element) {
      String attrVal = element.attributeValue("id");
      if ("true".equals(attrVal)) {
         AnnotationDescriptor ad = new AnnotationDescriptor(Id.class);
         annotationList.add(AnnotationFactory.create(ad));
      }

   }

   private void addTargetClass(Element element, AnnotationDescriptor ad, String nodeName, XMLContext.Default defaults) {
      String className = element.attributeValue(nodeName);
      if (className != null) {
         Class clazz;
         try {
            clazz = ReflectHelper.classForName(XMLContext.buildSafeClassName(className, defaults), this.getClass());
         } catch (ClassNotFoundException e) {
            throw new AnnotationException("Unable to find " + element.getPath() + " " + nodeName + ": " + className, e);
         }

         ad.setValue(getJavaAttributeNameFromXMLOne(nodeName), clazz);
      }

   }

   private void getElementCollection(List annotationList, XMLContext.Default defaults) {
      for(Element element : this.elementsForProperty) {
         if ("element-collection".equals(element.getName())) {
            AnnotationDescriptor ad = new AnnotationDescriptor(ElementCollection.class);
            this.addTargetClass(element, ad, "target-class", defaults);
            this.getFetchType(ad, element);
            this.getOrderBy(annotationList, element);
            this.getOrderColumn(annotationList, element);
            this.getMapKey(annotationList, element);
            this.getMapKeyClass(annotationList, element, defaults);
            this.getMapKeyTemporal(annotationList, element);
            this.getMapKeyEnumerated(annotationList, element);
            this.getMapKeyColumn(annotationList, element);
            this.buildMapKeyJoinColumns(annotationList, element);
            Annotation annotation = this.getColumn(element.element("column"), false, element);
            this.addIfNotNull(annotationList, annotation);
            this.getTemporal(annotationList, element);
            this.getEnumerated(annotationList, element);
            this.getLob(annotationList, element);
            List<AttributeOverride> attributes = new ArrayList();
            attributes.addAll(this.buildAttributeOverrides(element, "map-key-attribute-override"));
            attributes.addAll(this.buildAttributeOverrides(element, "attribute-override"));
            Annotation var8 = this.mergeAttributeOverrides(defaults, attributes, false);
            this.addIfNotNull(annotationList, var8);
            Annotation var9 = this.getAssociationOverrides(element, defaults, false);
            this.addIfNotNull(annotationList, var9);
            this.getCollectionTable(annotationList, element, defaults);
            annotationList.add(AnnotationFactory.create(ad));
            this.getAccessType(annotationList, element);
         }
      }

   }

   private void getOrderBy(List annotationList, Element element) {
      Element subelement = element != null ? element.element("order-by") : null;
      if (subelement != null) {
         AnnotationDescriptor ad = new AnnotationDescriptor(OrderBy.class);
         copyStringElement(subelement, ad, "value");
         annotationList.add(AnnotationFactory.create(ad));
      }

   }

   private void getMapKey(List annotationList, Element element) {
      Element subelement = element != null ? element.element("map-key") : null;
      if (subelement != null) {
         AnnotationDescriptor ad = new AnnotationDescriptor(MapKey.class);
         copyStringAttribute(ad, subelement, "name", false);
         annotationList.add(AnnotationFactory.create(ad));
      }

   }

   private void getMapKeyColumn(List annotationList, Element element) {
      Element subelement = element != null ? element.element("map-key-column") : null;
      if (subelement != null) {
         AnnotationDescriptor ad = new AnnotationDescriptor(MapKeyColumn.class);
         copyStringAttribute(ad, subelement, "name", false);
         copyBooleanAttribute(ad, subelement, "unique");
         copyBooleanAttribute(ad, subelement, "nullable");
         copyBooleanAttribute(ad, subelement, "insertable");
         copyBooleanAttribute(ad, subelement, "updatable");
         copyStringAttribute(ad, subelement, "column-definition", false);
         copyStringAttribute(ad, subelement, "table", false);
         copyIntegerAttribute(ad, subelement, "length");
         copyIntegerAttribute(ad, subelement, "precision");
         copyIntegerAttribute(ad, subelement, "scale");
         annotationList.add(AnnotationFactory.create(ad));
      }

   }

   private void getMapKeyClass(List annotationList, Element element, XMLContext.Default defaults) {
      String nodeName = "map-key-class";
      Element subelement = element != null ? element.element(nodeName) : null;
      if (subelement != null) {
         String mapKeyClassName = subelement.attributeValue("class");
         AnnotationDescriptor ad = new AnnotationDescriptor(MapKeyClass.class);
         if (StringHelper.isNotEmpty(mapKeyClassName)) {
            Class clazz;
            try {
               clazz = ReflectHelper.classForName(XMLContext.buildSafeClassName(mapKeyClassName, defaults), this.getClass());
            } catch (ClassNotFoundException e) {
               throw new AnnotationException("Unable to find " + element.getPath() + " " + nodeName + ": " + mapKeyClassName, e);
            }

            ad.setValue("value", clazz);
         }

         annotationList.add(AnnotationFactory.create(ad));
      }

   }

   private void getCollectionTable(List annotationList, Element element, XMLContext.Default defaults) {
      Element subelement = element != null ? element.element("collection-table") : null;
      if (subelement != null) {
         AnnotationDescriptor annotation = new AnnotationDescriptor(CollectionTable.class);
         copyStringAttribute(annotation, subelement, "name", false);
         copyStringAttribute(annotation, subelement, "catalog", false);
         if (StringHelper.isNotEmpty(defaults.getCatalog()) && StringHelper.isEmpty((String)annotation.valueOf("catalog"))) {
            annotation.setValue("catalog", defaults.getCatalog());
         }

         copyStringAttribute(annotation, subelement, "schema", false);
         if (StringHelper.isNotEmpty(defaults.getSchema()) && StringHelper.isEmpty((String)annotation.valueOf("schema"))) {
            annotation.setValue("schema", defaults.getSchema());
         }

         JoinColumn[] joinColumns = this.getJoinColumns(subelement, false);
         if (joinColumns.length > 0) {
            annotation.setValue("joinColumns", joinColumns);
         }

         buildUniqueConstraints(annotation, subelement);
         annotationList.add(AnnotationFactory.create(annotation));
      }

   }

   private void buildJoinColumns(List annotationList, Element element) {
      JoinColumn[] joinColumns = this.getJoinColumns(element, false);
      if (joinColumns.length > 0) {
         AnnotationDescriptor ad = new AnnotationDescriptor(JoinColumns.class);
         ad.setValue("value", joinColumns);
         annotationList.add(AnnotationFactory.create(ad));
      }

   }

   private void getCascades(AnnotationDescriptor ad, Element element, XMLContext.Default defaults) {
      List<Element> elements = (List<Element>)(element != null ? element.elements("cascade") : new ArrayList(0));
      List<CascadeType> cascades = new ArrayList();

      for(Element subelement : elements) {
         if (subelement.element("cascade-all") != null) {
            cascades.add(CascadeType.ALL);
         }

         if (subelement.element("cascade-persist") != null) {
            cascades.add(CascadeType.PERSIST);
         }

         if (subelement.element("cascade-merge") != null) {
            cascades.add(CascadeType.MERGE);
         }

         if (subelement.element("cascade-remove") != null) {
            cascades.add(CascadeType.REMOVE);
         }

         if (subelement.element("cascade-refresh") != null) {
            cascades.add(CascadeType.REFRESH);
         }

         if (subelement.element("cascade-detach") != null) {
            cascades.add(CascadeType.DETACH);
         }
      }

      if (Boolean.TRUE.equals(defaults.getCascadePersist()) && !cascades.contains(CascadeType.ALL) && !cascades.contains(CascadeType.PERSIST)) {
         cascades.add(CascadeType.PERSIST);
      }

      if (cascades.size() > 0) {
         ad.setValue("cascade", cascades.toArray(new CascadeType[cascades.size()]));
      }

   }

   private void getEmbedded(List annotationList, XMLContext.Default defaults) {
      for(Element element : this.elementsForProperty) {
         if ("embedded".equals(element.getName())) {
            AnnotationDescriptor ad = new AnnotationDescriptor(Embedded.class);
            annotationList.add(AnnotationFactory.create(ad));
            Annotation annotation = this.getAttributeOverrides(element, defaults, false);
            this.addIfNotNull(annotationList, annotation);
            Annotation var12 = this.getAssociationOverrides(element, defaults, false);
            this.addIfNotNull(annotationList, var12);
            this.getAccessType(annotationList, element);
         }
      }

      if (this.elementsForProperty.size() == 0 && defaults.canUseJavaAnnotations()) {
         Annotation annotation = this.getJavaAnnotation(Embedded.class);
         if (annotation != null) {
            annotationList.add(annotation);
            annotation = this.getJavaAnnotation(AttributeOverride.class);
            this.addIfNotNull(annotationList, annotation);
            annotation = this.getJavaAnnotation(AttributeOverrides.class);
            this.addIfNotNull(annotationList, annotation);
            annotation = this.getJavaAnnotation(AssociationOverride.class);
            this.addIfNotNull(annotationList, annotation);
            annotation = this.getJavaAnnotation(AssociationOverrides.class);
            this.addIfNotNull(annotationList, annotation);
         }
      }

   }

   private Transient getTransient(XMLContext.Default defaults) {
      for(Element element : this.elementsForProperty) {
         if ("transient".equals(element.getName())) {
            AnnotationDescriptor ad = new AnnotationDescriptor(Transient.class);
            return (Transient)AnnotationFactory.create(ad);
         }
      }

      if (this.elementsForProperty.size() == 0 && defaults.canUseJavaAnnotations()) {
         return (Transient)this.getJavaAnnotation(Transient.class);
      } else {
         return null;
      }
   }

   private void getVersion(List annotationList, XMLContext.Default defaults) {
      for(Element element : this.elementsForProperty) {
         if ("version".equals(element.getName())) {
            Annotation annotation = this.buildColumns(element);
            this.addIfNotNull(annotationList, annotation);
            this.getTemporal(annotationList, element);
            AnnotationDescriptor basic = new AnnotationDescriptor(Version.class);
            annotationList.add(AnnotationFactory.create(basic));
            this.getAccessType(annotationList, element);
         }
      }

      if (this.elementsForProperty.size() == 0 && defaults.canUseJavaAnnotations()) {
         Annotation annotation = this.getJavaAnnotation(Version.class);
         if (annotation != null) {
            annotationList.add(annotation);
            annotation = this.getJavaAnnotation(Column.class);
            this.addIfNotNull(annotationList, annotation);
            annotation = this.getJavaAnnotation(Columns.class);
            this.addIfNotNull(annotationList, annotation);
            annotation = this.getJavaAnnotation(Temporal.class);
            this.addIfNotNull(annotationList, annotation);
         }
      }

   }

   private void getBasic(List annotationList, XMLContext.Default defaults) {
      for(Element element : this.elementsForProperty) {
         if ("basic".equals(element.getName())) {
            Annotation annotation = this.buildColumns(element);
            this.addIfNotNull(annotationList, annotation);
            this.getAccessType(annotationList, element);
            this.getTemporal(annotationList, element);
            this.getLob(annotationList, element);
            this.getEnumerated(annotationList, element);
            AnnotationDescriptor basic = new AnnotationDescriptor(Basic.class);
            this.getFetchType(basic, element);
            copyBooleanAttribute(basic, element, "optional");
            annotationList.add(AnnotationFactory.create(basic));
         }
      }

      if (this.elementsForProperty.size() == 0 && defaults.canUseJavaAnnotations()) {
         Annotation annotation = this.getJavaAnnotation(Basic.class);
         this.addIfNotNull(annotationList, annotation);
         annotation = this.getJavaAnnotation(Lob.class);
         this.addIfNotNull(annotationList, annotation);
         annotation = this.getJavaAnnotation(Enumerated.class);
         this.addIfNotNull(annotationList, annotation);
         annotation = this.getJavaAnnotation(Temporal.class);
         this.addIfNotNull(annotationList, annotation);
         annotation = this.getJavaAnnotation(Column.class);
         this.addIfNotNull(annotationList, annotation);
         annotation = this.getJavaAnnotation(Columns.class);
         this.addIfNotNull(annotationList, annotation);
         annotation = this.getJavaAnnotation(AttributeOverride.class);
         this.addIfNotNull(annotationList, annotation);
         annotation = this.getJavaAnnotation(AttributeOverrides.class);
         this.addIfNotNull(annotationList, annotation);
         annotation = this.getJavaAnnotation(AssociationOverride.class);
         this.addIfNotNull(annotationList, annotation);
         annotation = this.getJavaAnnotation(AssociationOverrides.class);
         this.addIfNotNull(annotationList, annotation);
      }

   }

   private void getEnumerated(List annotationList, Element element) {
      Element subElement = element != null ? element.element("enumerated") : null;
      if (subElement != null) {
         AnnotationDescriptor ad = new AnnotationDescriptor(Enumerated.class);
         String enumerated = subElement.getTextTrim();
         if ("ORDINAL".equalsIgnoreCase(enumerated)) {
            ad.setValue("value", EnumType.ORDINAL);
         } else if ("STRING".equalsIgnoreCase(enumerated)) {
            ad.setValue("value", EnumType.STRING);
         } else if (StringHelper.isNotEmpty(enumerated)) {
            throw new AnnotationException("Unknown EnumType: " + enumerated + ". " + "Activate schema validation for more information");
         }

         annotationList.add(AnnotationFactory.create(ad));
      }

   }

   private void getLob(List annotationList, Element element) {
      Element subElement = element != null ? element.element("lob") : null;
      if (subElement != null) {
         annotationList.add(AnnotationFactory.create(new AnnotationDescriptor(Lob.class)));
      }

   }

   private void getFetchType(AnnotationDescriptor descriptor, Element element) {
      String fetchString = element != null ? element.attributeValue("fetch") : null;
      if (fetchString != null) {
         if ("eager".equalsIgnoreCase(fetchString)) {
            descriptor.setValue("fetch", FetchType.EAGER);
         } else if ("lazy".equalsIgnoreCase(fetchString)) {
            descriptor.setValue("fetch", FetchType.LAZY);
         }
      }

   }

   private void getEmbeddedId(List annotationList, XMLContext.Default defaults) {
      for(Element element : this.elementsForProperty) {
         if ("embedded-id".equals(element.getName()) && this.isProcessingId(defaults)) {
            Annotation annotation = this.getAttributeOverrides(element, defaults, false);
            this.addIfNotNull(annotationList, annotation);
            Annotation var18 = this.getAssociationOverrides(element, defaults, false);
            this.addIfNotNull(annotationList, var18);
            AnnotationDescriptor ad = new AnnotationDescriptor(EmbeddedId.class);
            annotationList.add(AnnotationFactory.create(ad));
            this.getAccessType(annotationList, element);
         }
      }

      if (this.elementsForProperty.size() == 0 && defaults.canUseJavaAnnotations()) {
         Annotation annotation = this.getJavaAnnotation(EmbeddedId.class);
         if (annotation != null) {
            annotationList.add(annotation);
            annotation = this.getJavaAnnotation(Column.class);
            this.addIfNotNull(annotationList, annotation);
            annotation = this.getJavaAnnotation(Columns.class);
            this.addIfNotNull(annotationList, annotation);
            annotation = this.getJavaAnnotation(GeneratedValue.class);
            this.addIfNotNull(annotationList, annotation);
            annotation = this.getJavaAnnotation(Temporal.class);
            this.addIfNotNull(annotationList, annotation);
            annotation = this.getJavaAnnotation(TableGenerator.class);
            this.addIfNotNull(annotationList, annotation);
            annotation = this.getJavaAnnotation(SequenceGenerator.class);
            this.addIfNotNull(annotationList, annotation);
            annotation = this.getJavaAnnotation(AttributeOverride.class);
            this.addIfNotNull(annotationList, annotation);
            annotation = this.getJavaAnnotation(AttributeOverrides.class);
            this.addIfNotNull(annotationList, annotation);
            annotation = this.getJavaAnnotation(AssociationOverride.class);
            this.addIfNotNull(annotationList, annotation);
            annotation = this.getJavaAnnotation(AssociationOverrides.class);
            this.addIfNotNull(annotationList, annotation);
         }
      }

   }

   private void preCalculateElementsForProperty(Element tree) {
      this.elementsForProperty = new ArrayList();
      Element element = tree != null ? tree.element("attributes") : null;
      if (element != null) {
         for(Element subelement : element.elements()) {
            if (this.propertyName.equals(subelement.attributeValue("name"))) {
               this.elementsForProperty.add(subelement);
            }
         }
      }

      if (tree != null) {
         for(Element subelement : tree.elements()) {
            if (this.propertyName.equals(subelement.attributeValue("method-name"))) {
               this.elementsForProperty.add(subelement);
            }
         }
      }

   }

   private void getId(List annotationList, XMLContext.Default defaults) {
      for(Element element : this.elementsForProperty) {
         if ("id".equals(element.getName())) {
            boolean processId = this.isProcessingId(defaults);
            if (processId) {
               Annotation annotation = this.buildColumns(element);
               this.addIfNotNull(annotationList, annotation);
               Annotation var19 = this.buildGeneratedValue(element);
               this.addIfNotNull(annotationList, var19);
               this.getTemporal(annotationList, element);
               Annotation var20 = this.getTableGenerator(element, defaults);
               this.addIfNotNull(annotationList, var20);
               Annotation var21 = this.getSequenceGenerator(element, defaults);
               this.addIfNotNull(annotationList, var21);
               AnnotationDescriptor id = new AnnotationDescriptor(Id.class);
               annotationList.add(AnnotationFactory.create(id));
               this.getAccessType(annotationList, element);
            }
         }
      }

      if (this.elementsForProperty.size() == 0 && defaults.canUseJavaAnnotations()) {
         Annotation annotation = this.getJavaAnnotation(Id.class);
         if (annotation != null) {
            annotationList.add(annotation);
            annotation = this.getJavaAnnotation(Column.class);
            this.addIfNotNull(annotationList, annotation);
            annotation = this.getJavaAnnotation(Columns.class);
            this.addIfNotNull(annotationList, annotation);
            annotation = this.getJavaAnnotation(GeneratedValue.class);
            this.addIfNotNull(annotationList, annotation);
            annotation = this.getJavaAnnotation(Temporal.class);
            this.addIfNotNull(annotationList, annotation);
            annotation = this.getJavaAnnotation(TableGenerator.class);
            this.addIfNotNull(annotationList, annotation);
            annotation = this.getJavaAnnotation(SequenceGenerator.class);
            this.addIfNotNull(annotationList, annotation);
            annotation = this.getJavaAnnotation(AttributeOverride.class);
            this.addIfNotNull(annotationList, annotation);
            annotation = this.getJavaAnnotation(AttributeOverrides.class);
            this.addIfNotNull(annotationList, annotation);
            annotation = this.getJavaAnnotation(AssociationOverride.class);
            this.addIfNotNull(annotationList, annotation);
            annotation = this.getJavaAnnotation(AssociationOverrides.class);
            this.addIfNotNull(annotationList, annotation);
         }
      }

   }

   private boolean isProcessingId(XMLContext.Default defaults) {
      boolean isExplicit = defaults.getAccess() != null;
      boolean correctAccess = JPAOverriddenAnnotationReader.PropertyType.PROPERTY.equals(this.propertyType) && AccessType.PROPERTY.equals(defaults.getAccess()) || JPAOverriddenAnnotationReader.PropertyType.FIELD.equals(this.propertyType) && AccessType.FIELD.equals(defaults.getAccess());
      boolean hasId = defaults.canUseJavaAnnotations() && (this.isJavaAnnotationPresent(Id.class) || this.isJavaAnnotationPresent(EmbeddedId.class));
      boolean mirrorAttributeIsId = defaults.canUseJavaAnnotations() && this.mirroredAttribute != null && (this.mirroredAttribute.isAnnotationPresent(Id.class) || this.mirroredAttribute.isAnnotationPresent(EmbeddedId.class));
      boolean propertyIsDefault = JPAOverriddenAnnotationReader.PropertyType.PROPERTY.equals(this.propertyType) && !mirrorAttributeIsId;
      return correctAccess || !isExplicit && hasId || !isExplicit && propertyIsDefault;
   }

   private Columns buildColumns(Element element) {
      List<Element> subelements = element.elements("column");
      List<Column> columns = new ArrayList(subelements.size());

      for(Element subelement : subelements) {
         columns.add(this.getColumn(subelement, false, element));
      }

      if (columns.size() > 0) {
         AnnotationDescriptor columnsDescr = new AnnotationDescriptor(Columns.class);
         columnsDescr.setValue("columns", columns.toArray(new Column[columns.size()]));
         return (Columns)AnnotationFactory.create(columnsDescr);
      } else {
         return null;
      }
   }

   private GeneratedValue buildGeneratedValue(Element element) {
      Element subElement = element != null ? element.element("generated-value") : null;
      if (subElement != null) {
         AnnotationDescriptor ad = new AnnotationDescriptor(GeneratedValue.class);
         String strategy = subElement.attributeValue("strategy");
         if ("TABLE".equalsIgnoreCase(strategy)) {
            ad.setValue("strategy", GenerationType.TABLE);
         } else if ("SEQUENCE".equalsIgnoreCase(strategy)) {
            ad.setValue("strategy", GenerationType.SEQUENCE);
         } else if ("IDENTITY".equalsIgnoreCase(strategy)) {
            ad.setValue("strategy", GenerationType.IDENTITY);
         } else if ("AUTO".equalsIgnoreCase(strategy)) {
            ad.setValue("strategy", GenerationType.AUTO);
         } else if (StringHelper.isNotEmpty(strategy)) {
            throw new AnnotationException("Unknown GenerationType: " + strategy + ". " + "Activate schema validation for more information");
         }

         copyStringAttribute(ad, subElement, "generator", false);
         return (GeneratedValue)AnnotationFactory.create(ad);
      } else {
         return null;
      }
   }

   private void getTemporal(List annotationList, Element element) {
      Element subElement = element != null ? element.element("temporal") : null;
      if (subElement != null) {
         AnnotationDescriptor ad = new AnnotationDescriptor(Temporal.class);
         String temporal = subElement.getTextTrim();
         if ("DATE".equalsIgnoreCase(temporal)) {
            ad.setValue("value", TemporalType.DATE);
         } else if ("TIME".equalsIgnoreCase(temporal)) {
            ad.setValue("value", TemporalType.TIME);
         } else if ("TIMESTAMP".equalsIgnoreCase(temporal)) {
            ad.setValue("value", TemporalType.TIMESTAMP);
         } else if (StringHelper.isNotEmpty(temporal)) {
            throw new AnnotationException("Unknown TemporalType: " + temporal + ". " + "Activate schema validation for more information");
         }

         annotationList.add(AnnotationFactory.create(ad));
      }

   }

   private void getAccessType(List annotationList, Element element) {
      if (element != null) {
         String access = element.attributeValue("access");
         if (access != null) {
            AnnotationDescriptor ad = new AnnotationDescriptor(Access.class);

            AccessType type;
            try {
               type = AccessType.valueOf(access);
            } catch (IllegalArgumentException var7) {
               throw new AnnotationException(access + " is not a valid access type. Check you xml confguration.");
            }

            if (AccessType.PROPERTY.equals(type) && this.element instanceof Method || AccessType.FIELD.equals(type) && this.element instanceof Field) {
               return;
            }

            ad.setValue("value", type);
            annotationList.add(AnnotationFactory.create(ad));
         }

      }
   }

   private AssociationOverrides getAssociationOverrides(Element tree, XMLContext.Default defaults, boolean mergeWithAnnotations) {
      List<AssociationOverride> attributes = this.buildAssociationOverrides(tree, defaults);
      if (mergeWithAnnotations && defaults.canUseJavaAnnotations()) {
         AssociationOverride annotation = (AssociationOverride)this.getJavaAnnotation(AssociationOverride.class);
         this.addAssociationOverrideIfNeeded(annotation, attributes);
         AssociationOverrides annotations = (AssociationOverrides)this.getJavaAnnotation(AssociationOverrides.class);
         if (annotations != null) {
            for(AssociationOverride current : annotations.value()) {
               this.addAssociationOverrideIfNeeded(current, attributes);
            }
         }
      }

      if (attributes.size() > 0) {
         AnnotationDescriptor ad = new AnnotationDescriptor(AssociationOverrides.class);
         ad.setValue("value", attributes.toArray(new AssociationOverride[attributes.size()]));
         return (AssociationOverrides)AnnotationFactory.create(ad);
      } else {
         return null;
      }
   }

   private List buildAssociationOverrides(Element element, XMLContext.Default defaults) {
      List<Element> subelements = element == null ? null : element.elements("association-override");
      List<AssociationOverride> overrides = new ArrayList();
      if (subelements != null && subelements.size() > 0) {
         for(Element current : subelements) {
            AnnotationDescriptor override = new AnnotationDescriptor(AssociationOverride.class);
            copyStringAttribute(override, current, "name", true);
            override.setValue("joinColumns", this.getJoinColumns(current, false));
            JoinTable joinTable = this.buildJoinTable(current, defaults);
            if (joinTable != null) {
               override.setValue("joinTable", joinTable);
            }

            overrides.add((AssociationOverride)AnnotationFactory.create(override));
         }
      }

      return overrides;
   }

   private JoinColumn[] getJoinColumns(Element element, boolean isInverse) {
      List<Element> subelements = element != null ? element.elements(isInverse ? "inverse-join-column" : "join-column") : null;
      List<JoinColumn> joinColumns = new ArrayList();
      if (subelements != null) {
         for(Element subelement : subelements) {
            AnnotationDescriptor column = new AnnotationDescriptor(JoinColumn.class);
            copyStringAttribute(column, subelement, "name", false);
            copyStringAttribute(column, subelement, "referenced-column-name", false);
            copyBooleanAttribute(column, subelement, "unique");
            copyBooleanAttribute(column, subelement, "nullable");
            copyBooleanAttribute(column, subelement, "insertable");
            copyBooleanAttribute(column, subelement, "updatable");
            copyStringAttribute(column, subelement, "column-definition", false);
            copyStringAttribute(column, subelement, "table", false);
            joinColumns.add((JoinColumn)AnnotationFactory.create(column));
         }
      }

      return (JoinColumn[])joinColumns.toArray(new JoinColumn[joinColumns.size()]);
   }

   private void addAssociationOverrideIfNeeded(AssociationOverride annotation, List overrides) {
      if (annotation != null) {
         String overrideName = annotation.name();
         boolean present = false;

         for(AssociationOverride current : overrides) {
            if (current.name().equals(overrideName)) {
               present = true;
               break;
            }
         }

         if (!present) {
            overrides.add(annotation);
         }
      }

   }

   private AttributeOverrides getAttributeOverrides(Element tree, XMLContext.Default defaults, boolean mergeWithAnnotations) {
      List<AttributeOverride> attributes = this.buildAttributeOverrides(tree, "attribute-override");
      return this.mergeAttributeOverrides(defaults, attributes, mergeWithAnnotations);
   }

   private AttributeOverrides mergeAttributeOverrides(XMLContext.Default defaults, List attributes, boolean mergeWithAnnotations) {
      if (mergeWithAnnotations && defaults.canUseJavaAnnotations()) {
         AttributeOverride annotation = (AttributeOverride)this.getJavaAnnotation(AttributeOverride.class);
         this.addAttributeOverrideIfNeeded(annotation, attributes);
         AttributeOverrides annotations = (AttributeOverrides)this.getJavaAnnotation(AttributeOverrides.class);
         if (annotations != null) {
            for(AttributeOverride current : annotations.value()) {
               this.addAttributeOverrideIfNeeded(current, attributes);
            }
         }
      }

      if (attributes.size() > 0) {
         AnnotationDescriptor ad = new AnnotationDescriptor(AttributeOverrides.class);
         ad.setValue("value", attributes.toArray(new AttributeOverride[attributes.size()]));
         return (AttributeOverrides)AnnotationFactory.create(ad);
      } else {
         return null;
      }
   }

   private List buildAttributeOverrides(Element element, String nodeName) {
      List<Element> subelements = element == null ? null : element.elements(nodeName);
      return this.buildAttributeOverrides(subelements, nodeName);
   }

   private List buildAttributeOverrides(List subelements, String nodeName) {
      List<AttributeOverride> overrides = new ArrayList();
      if (subelements != null && subelements.size() > 0) {
         for(Element current : subelements) {
            if (current.getName().equals(nodeName)) {
               AnnotationDescriptor override = new AnnotationDescriptor(AttributeOverride.class);
               copyStringAttribute(override, current, "name", true);
               Element column = current.element("column");
               override.setValue("column", this.getColumn(column, true, current));
               overrides.add((AttributeOverride)AnnotationFactory.create(override));
            }
         }
      }

      return overrides;
   }

   private Column getColumn(Element element, boolean isMandatory, Element current) {
      if (element != null) {
         AnnotationDescriptor column = new AnnotationDescriptor(Column.class);
         copyStringAttribute(column, element, "name", false);
         copyBooleanAttribute(column, element, "unique");
         copyBooleanAttribute(column, element, "nullable");
         copyBooleanAttribute(column, element, "insertable");
         copyBooleanAttribute(column, element, "updatable");
         copyStringAttribute(column, element, "column-definition", false);
         copyStringAttribute(column, element, "table", false);
         copyIntegerAttribute(column, element, "length");
         copyIntegerAttribute(column, element, "precision");
         copyIntegerAttribute(column, element, "scale");
         return (Column)AnnotationFactory.create(column);
      } else if (isMandatory) {
         throw new AnnotationException(current.getPath() + ".column is mandatory. " + "Activate schema validation for more information");
      } else {
         return null;
      }
   }

   private void addAttributeOverrideIfNeeded(AttributeOverride annotation, List overrides) {
      if (annotation != null) {
         String overrideName = annotation.name();
         boolean present = false;

         for(AttributeOverride current : overrides) {
            if (current.name().equals(overrideName)) {
               present = true;
               break;
            }
         }

         if (!present) {
            overrides.add(annotation);
         }
      }

   }

   private Access getAccessType(Element tree, XMLContext.Default defaults) {
      String access = tree == null ? null : tree.attributeValue("access");
      if (access != null) {
         AnnotationDescriptor ad = new AnnotationDescriptor(Access.class);

         AccessType type;
         try {
            type = AccessType.valueOf(access);
         } catch (IllegalArgumentException var7) {
            throw new AnnotationException(access + " is not a valid access type. Check you xml confguration.");
         }

         ad.setValue("value", type);
         return (Access)AnnotationFactory.create(ad);
      } else if (defaults.canUseJavaAnnotations() && this.isJavaAnnotationPresent(Access.class)) {
         return (Access)this.getJavaAnnotation(Access.class);
      } else if (defaults.getAccess() != null) {
         AnnotationDescriptor ad = new AnnotationDescriptor(Access.class);
         ad.setValue("value", defaults.getAccess());
         return (Access)AnnotationFactory.create(ad);
      } else {
         return null;
      }
   }

   private ExcludeSuperclassListeners getExcludeSuperclassListeners(Element tree, XMLContext.Default defaults) {
      return (ExcludeSuperclassListeners)this.getMarkerAnnotation(ExcludeSuperclassListeners.class, tree, defaults);
   }

   private ExcludeDefaultListeners getExcludeDefaultListeners(Element tree, XMLContext.Default defaults) {
      return (ExcludeDefaultListeners)this.getMarkerAnnotation(ExcludeDefaultListeners.class, tree, defaults);
   }

   private Annotation getMarkerAnnotation(Class clazz, Element element, XMLContext.Default defaults) {
      Element subelement = element == null ? null : element.element((String)annotationToXml.get(clazz));
      if (subelement != null) {
         return AnnotationFactory.create(new AnnotationDescriptor(clazz));
      } else {
         return defaults.canUseJavaAnnotations() ? this.getJavaAnnotation(clazz) : null;
      }
   }

   private SqlResultSetMappings getSqlResultSetMappings(Element tree, XMLContext.Default defaults) {
      List<SqlResultSetMapping> results = buildSqlResultsetMappings(tree, defaults);
      if (defaults.canUseJavaAnnotations()) {
         SqlResultSetMapping annotation = (SqlResultSetMapping)this.getJavaAnnotation(SqlResultSetMapping.class);
         this.addSqlResultsetMappingIfNeeded(annotation, results);
         SqlResultSetMappings annotations = (SqlResultSetMappings)this.getJavaAnnotation(SqlResultSetMappings.class);
         if (annotations != null) {
            for(SqlResultSetMapping current : annotations.value()) {
               this.addSqlResultsetMappingIfNeeded(current, results);
            }
         }
      }

      if (results.size() > 0) {
         AnnotationDescriptor ad = new AnnotationDescriptor(SqlResultSetMappings.class);
         ad.setValue("value", results.toArray(new SqlResultSetMapping[results.size()]));
         return (SqlResultSetMappings)AnnotationFactory.create(ad);
      } else {
         return null;
      }
   }

   public static List buildSqlResultsetMappings(Element element, XMLContext.Default defaults) {
      if (element == null) {
         return new ArrayList();
      } else {
         List resultsetElementList = element.elements("sql-result-set-mapping");
         List<SqlResultSetMapping> resultsets = new ArrayList();
         Iterator it = resultsetElementList.listIterator();

         while(it.hasNext()) {
            Element subelement = (Element)it.next();
            AnnotationDescriptor ann = new AnnotationDescriptor(SqlResultSetMapping.class);
            copyStringAttribute(ann, subelement, "name", true);
            List<Element> elements = subelement.elements("entity-result");
            List<EntityResult> entityResults = new ArrayList(elements.size());

            for(Element entityResult : elements) {
               AnnotationDescriptor entityResultDescriptor = new AnnotationDescriptor(EntityResult.class);
               String clazzName = entityResult.attributeValue("entity-class");
               if (clazzName == null) {
                  throw new AnnotationException("<entity-result> without entity-class. Activate schema validation for more information");
               }

               Class clazz;
               try {
                  clazz = ReflectHelper.classForName(XMLContext.buildSafeClassName(clazzName, defaults), JPAOverriddenAnnotationReader.class);
               } catch (ClassNotFoundException e) {
                  throw new AnnotationException("Unable to find entity-class: " + clazzName, e);
               }

               entityResultDescriptor.setValue("entityClass", clazz);
               copyStringAttribute(entityResultDescriptor, entityResult, "discriminator-column", false);
               List<FieldResult> fieldResults = new ArrayList();

               for(Element fieldResult : entityResult.elements("field-result")) {
                  AnnotationDescriptor fieldResultDescriptor = new AnnotationDescriptor(FieldResult.class);
                  copyStringAttribute(fieldResultDescriptor, fieldResult, "name", true);
                  copyStringAttribute(fieldResultDescriptor, fieldResult, "column", true);
                  fieldResults.add((FieldResult)AnnotationFactory.create(fieldResultDescriptor));
               }

               entityResultDescriptor.setValue("fields", fieldResults.toArray(new FieldResult[fieldResults.size()]));
               entityResults.add((EntityResult)AnnotationFactory.create(entityResultDescriptor));
            }

            ann.setValue("entities", entityResults.toArray(new EntityResult[entityResults.size()]));
            elements = subelement.elements("column-result");
            List<ColumnResult> columnResults = new ArrayList(elements.size());

            for(Element columnResult : elements) {
               AnnotationDescriptor columnResultDescriptor = new AnnotationDescriptor(ColumnResult.class);
               copyStringAttribute(columnResultDescriptor, columnResult, "name", true);
               columnResults.add((ColumnResult)AnnotationFactory.create(columnResultDescriptor));
            }

            ann.setValue("columns", columnResults.toArray(new ColumnResult[columnResults.size()]));
            String clazzName = subelement.attributeValue("result-class");
            if (StringHelper.isNotEmpty(clazzName)) {
               Class clazz;
               try {
                  clazz = ReflectHelper.classForName(XMLContext.buildSafeClassName(clazzName, defaults), JPAOverriddenAnnotationReader.class);
               } catch (ClassNotFoundException e) {
                  throw new AnnotationException("Unable to find entity-class: " + clazzName, e);
               }

               ann.setValue("resultClass", clazz);
            }

            copyStringAttribute(ann, subelement, "result-set-mapping", false);
            resultsets.add((SqlResultSetMapping)AnnotationFactory.create(ann));
         }

         return resultsets;
      }
   }

   private void addSqlResultsetMappingIfNeeded(SqlResultSetMapping annotation, List resultsets) {
      if (annotation != null) {
         String resultsetName = annotation.name();
         boolean present = false;

         for(SqlResultSetMapping current : resultsets) {
            if (current.name().equals(resultsetName)) {
               present = true;
               break;
            }
         }

         if (!present) {
            resultsets.add(annotation);
         }
      }

   }

   private NamedQueries getNamedQueries(Element tree, XMLContext.Default defaults) {
      List<NamedQuery> queries = buildNamedQueries(tree, false, defaults);
      if (defaults.canUseJavaAnnotations()) {
         NamedQuery annotation = (NamedQuery)this.getJavaAnnotation(NamedQuery.class);
         this.addNamedQueryIfNeeded(annotation, queries);
         NamedQueries annotations = (NamedQueries)this.getJavaAnnotation(NamedQueries.class);
         if (annotations != null) {
            for(NamedQuery current : annotations.value()) {
               this.addNamedQueryIfNeeded(current, queries);
            }
         }
      }

      if (queries.size() > 0) {
         AnnotationDescriptor ad = new AnnotationDescriptor(NamedQueries.class);
         ad.setValue("value", queries.toArray(new NamedQuery[queries.size()]));
         return (NamedQueries)AnnotationFactory.create(ad);
      } else {
         return null;
      }
   }

   private void addNamedQueryIfNeeded(NamedQuery annotation, List queries) {
      if (annotation != null) {
         String queryName = annotation.name();
         boolean present = false;

         for(NamedQuery current : queries) {
            if (current.name().equals(queryName)) {
               present = true;
               break;
            }
         }

         if (!present) {
            queries.add(annotation);
         }
      }

   }

   private NamedNativeQueries getNamedNativeQueries(Element tree, XMLContext.Default defaults) {
      List<NamedNativeQuery> queries = buildNamedQueries(tree, true, defaults);
      if (defaults.canUseJavaAnnotations()) {
         NamedNativeQuery annotation = (NamedNativeQuery)this.getJavaAnnotation(NamedNativeQuery.class);
         this.addNamedNativeQueryIfNeeded(annotation, queries);
         NamedNativeQueries annotations = (NamedNativeQueries)this.getJavaAnnotation(NamedNativeQueries.class);
         if (annotations != null) {
            for(NamedNativeQuery current : annotations.value()) {
               this.addNamedNativeQueryIfNeeded(current, queries);
            }
         }
      }

      if (queries.size() > 0) {
         AnnotationDescriptor ad = new AnnotationDescriptor(NamedNativeQueries.class);
         ad.setValue("value", queries.toArray(new NamedNativeQuery[queries.size()]));
         return (NamedNativeQueries)AnnotationFactory.create(ad);
      } else {
         return null;
      }
   }

   private void addNamedNativeQueryIfNeeded(NamedNativeQuery annotation, List queries) {
      if (annotation != null) {
         String queryName = annotation.name();
         boolean present = false;

         for(NamedNativeQuery current : queries) {
            if (current.name().equals(queryName)) {
               present = true;
               break;
            }
         }

         if (!present) {
            queries.add(annotation);
         }
      }

   }

   public static List buildNamedQueries(Element element, boolean isNative, XMLContext.Default defaults) {
      if (element == null) {
         return new ArrayList();
      } else {
         List namedQueryElementList = isNative ? element.elements("named-native-query") : element.elements("named-query");
         List namedQueries = new ArrayList();
         Iterator it = namedQueryElementList.listIterator();

         while(it.hasNext()) {
            Element subelement = (Element)it.next();
            AnnotationDescriptor ann = new AnnotationDescriptor(isNative ? NamedNativeQuery.class : NamedQuery.class);
            copyStringAttribute(ann, subelement, "name", false);
            Element queryElt = subelement.element("query");
            if (queryElt == null) {
               throw new AnnotationException("No <query> element found.Activate schema validation for more information");
            }

            copyStringElement(queryElt, ann, "query");
            List<Element> elements = subelement.elements("hint");
            List<QueryHint> queryHints = new ArrayList(elements.size());

            for(Element hint : elements) {
               AnnotationDescriptor hintDescriptor = new AnnotationDescriptor(QueryHint.class);
               String value = hint.attributeValue("name");
               if (value == null) {
                  throw new AnnotationException("<hint> without name. Activate schema validation for more information");
               }

               hintDescriptor.setValue("name", value);
               value = hint.attributeValue("value");
               if (value == null) {
                  throw new AnnotationException("<hint> without value. Activate schema validation for more information");
               }

               hintDescriptor.setValue("value", value);
               queryHints.add((QueryHint)AnnotationFactory.create(hintDescriptor));
            }

            ann.setValue("hints", queryHints.toArray(new QueryHint[queryHints.size()]));
            String clazzName = subelement.attributeValue("result-class");
            if (StringHelper.isNotEmpty(clazzName)) {
               Class clazz;
               try {
                  clazz = ReflectHelper.classForName(XMLContext.buildSafeClassName(clazzName, defaults), JPAOverriddenAnnotationReader.class);
               } catch (ClassNotFoundException e) {
                  throw new AnnotationException("Unable to find entity-class: " + clazzName, e);
               }

               ann.setValue("resultClass", clazz);
            }

            copyStringAttribute(ann, subelement, "result-set-mapping", false);
            namedQueries.add(AnnotationFactory.create(ann));
         }

         return namedQueries;
      }
   }

   private TableGenerator getTableGenerator(Element tree, XMLContext.Default defaults) {
      Element element = tree != null ? tree.element((String)annotationToXml.get(TableGenerator.class)) : null;
      if (element != null) {
         return buildTableGeneratorAnnotation(element, defaults);
      } else if (defaults.canUseJavaAnnotations() && this.isJavaAnnotationPresent(TableGenerator.class)) {
         TableGenerator tableAnn = (TableGenerator)this.getJavaAnnotation(TableGenerator.class);
         if (!StringHelper.isNotEmpty(defaults.getSchema()) && !StringHelper.isNotEmpty(defaults.getCatalog())) {
            return tableAnn;
         } else {
            AnnotationDescriptor annotation = new AnnotationDescriptor(TableGenerator.class);
            annotation.setValue("name", tableAnn.name());
            annotation.setValue("table", tableAnn.table());
            annotation.setValue("catalog", tableAnn.table());
            if (StringHelper.isEmpty((String)annotation.valueOf("catalog")) && StringHelper.isNotEmpty(defaults.getCatalog())) {
               annotation.setValue("catalog", defaults.getCatalog());
            }

            annotation.setValue("schema", tableAnn.table());
            if (StringHelper.isEmpty((String)annotation.valueOf("schema")) && StringHelper.isNotEmpty(defaults.getSchema())) {
               annotation.setValue("catalog", defaults.getSchema());
            }

            annotation.setValue("pkColumnName", tableAnn.pkColumnName());
            annotation.setValue("valueColumnName", tableAnn.valueColumnName());
            annotation.setValue("pkColumnValue", tableAnn.pkColumnValue());
            annotation.setValue("initialValue", tableAnn.initialValue());
            annotation.setValue("allocationSize", tableAnn.allocationSize());
            annotation.setValue("uniqueConstraints", tableAnn.uniqueConstraints());
            return (TableGenerator)AnnotationFactory.create(annotation);
         }
      } else {
         return null;
      }
   }

   public static TableGenerator buildTableGeneratorAnnotation(Element element, XMLContext.Default defaults) {
      AnnotationDescriptor ad = new AnnotationDescriptor(TableGenerator.class);
      copyStringAttribute(ad, element, "name", false);
      copyStringAttribute(ad, element, "table", false);
      copyStringAttribute(ad, element, "catalog", false);
      copyStringAttribute(ad, element, "schema", false);
      copyStringAttribute(ad, element, "pk-column-name", false);
      copyStringAttribute(ad, element, "value-column-name", false);
      copyStringAttribute(ad, element, "pk-column-value", false);
      copyIntegerAttribute(ad, element, "initial-value");
      copyIntegerAttribute(ad, element, "allocation-size");
      buildUniqueConstraints(ad, element);
      if (StringHelper.isEmpty((String)ad.valueOf("schema")) && StringHelper.isNotEmpty(defaults.getSchema())) {
         ad.setValue("schema", defaults.getSchema());
      }

      if (StringHelper.isEmpty((String)ad.valueOf("catalog")) && StringHelper.isNotEmpty(defaults.getCatalog())) {
         ad.setValue("catalog", defaults.getCatalog());
      }

      return (TableGenerator)AnnotationFactory.create(ad);
   }

   private SequenceGenerator getSequenceGenerator(Element tree, XMLContext.Default defaults) {
      Element element = tree != null ? tree.element((String)annotationToXml.get(SequenceGenerator.class)) : null;
      if (element != null) {
         return buildSequenceGeneratorAnnotation(element);
      } else {
         return defaults.canUseJavaAnnotations() ? (SequenceGenerator)this.getJavaAnnotation(SequenceGenerator.class) : null;
      }
   }

   public static SequenceGenerator buildSequenceGeneratorAnnotation(Element element) {
      if (element != null) {
         AnnotationDescriptor ad = new AnnotationDescriptor(SequenceGenerator.class);
         copyStringAttribute(ad, element, "name", false);
         copyStringAttribute(ad, element, "sequence-name", false);
         copyIntegerAttribute(ad, element, "initial-value");
         copyIntegerAttribute(ad, element, "allocation-size");
         return (SequenceGenerator)AnnotationFactory.create(ad);
      } else {
         return null;
      }
   }

   private DiscriminatorColumn getDiscriminatorColumn(Element tree, XMLContext.Default defaults) {
      Element element = tree != null ? tree.element("discriminator-column") : null;
      if (element != null) {
         AnnotationDescriptor ad = new AnnotationDescriptor(DiscriminatorColumn.class);
         copyStringAttribute(ad, element, "name", false);
         copyStringAttribute(ad, element, "column-definition", false);
         String value = element.attributeValue("discriminator-type");
         DiscriminatorType type = DiscriminatorType.STRING;
         if (value != null) {
            if ("STRING".equals(value)) {
               type = DiscriminatorType.STRING;
            } else if ("CHAR".equals(value)) {
               type = DiscriminatorType.CHAR;
            } else {
               if (!"INTEGER".equals(value)) {
                  throw new AnnotationException("Unknown DiscrimiatorType in XML: " + value + " (" + "Activate schema validation for more information" + ")");
               }

               type = DiscriminatorType.INTEGER;
            }
         }

         ad.setValue("discriminatorType", type);
         copyIntegerAttribute(ad, element, "length");
         return (DiscriminatorColumn)AnnotationFactory.create(ad);
      } else {
         return defaults.canUseJavaAnnotations() ? (DiscriminatorColumn)this.getJavaAnnotation(DiscriminatorColumn.class) : null;
      }
   }

   private DiscriminatorValue getDiscriminatorValue(Element tree, XMLContext.Default defaults) {
      Element element = tree != null ? tree.element("discriminator-value") : null;
      if (element != null) {
         AnnotationDescriptor ad = new AnnotationDescriptor(DiscriminatorValue.class);
         copyStringElement(element, ad, "value");
         return (DiscriminatorValue)AnnotationFactory.create(ad);
      } else {
         return defaults.canUseJavaAnnotations() ? (DiscriminatorValue)this.getJavaAnnotation(DiscriminatorValue.class) : null;
      }
   }

   private Inheritance getInheritance(Element tree, XMLContext.Default defaults) {
      Element element = tree != null ? tree.element("inheritance") : null;
      if (element != null) {
         AnnotationDescriptor ad = new AnnotationDescriptor(Inheritance.class);
         Attribute attr = element.attribute("strategy");
         InheritanceType strategy = InheritanceType.SINGLE_TABLE;
         if (attr != null) {
            String value = attr.getValue();
            if ("SINGLE_TABLE".equals(value)) {
               strategy = InheritanceType.SINGLE_TABLE;
            } else if ("JOINED".equals(value)) {
               strategy = InheritanceType.JOINED;
            } else {
               if (!"TABLE_PER_CLASS".equals(value)) {
                  throw new AnnotationException("Unknown InheritanceType in XML: " + value + " (" + "Activate schema validation for more information" + ")");
               }

               strategy = InheritanceType.TABLE_PER_CLASS;
            }
         }

         ad.setValue("strategy", strategy);
         return (Inheritance)AnnotationFactory.create(ad);
      } else {
         return defaults.canUseJavaAnnotations() ? (Inheritance)this.getJavaAnnotation(Inheritance.class) : null;
      }
   }

   private IdClass getIdClass(Element tree, XMLContext.Default defaults) {
      Element element = tree == null ? null : tree.element("id-class");
      if (element != null) {
         Attribute attr = element.attribute("class");
         if (attr != null) {
            AnnotationDescriptor ad = new AnnotationDescriptor(IdClass.class);

            Class clazz;
            try {
               clazz = ReflectHelper.classForName(XMLContext.buildSafeClassName(attr.getValue(), defaults), this.getClass());
            } catch (ClassNotFoundException e) {
               throw new AnnotationException("Unable to find id-class: " + attr.getValue(), e);
            }

            ad.setValue("value", clazz);
            return (IdClass)AnnotationFactory.create(ad);
         } else {
            throw new AnnotationException("id-class without class. Activate schema validation for more information");
         }
      } else {
         return defaults.canUseJavaAnnotations() ? (IdClass)this.getJavaAnnotation(IdClass.class) : null;
      }
   }

   private PrimaryKeyJoinColumns getPrimaryKeyJoinColumns(Element element, XMLContext.Default defaults, boolean mergeWithAnnotations) {
      PrimaryKeyJoinColumn[] columns = this.buildPrimaryKeyJoinColumns(element);
      if (mergeWithAnnotations && columns.length == 0 && defaults.canUseJavaAnnotations()) {
         PrimaryKeyJoinColumn annotation = (PrimaryKeyJoinColumn)this.getJavaAnnotation(PrimaryKeyJoinColumn.class);
         if (annotation != null) {
            columns = new PrimaryKeyJoinColumn[]{annotation};
         } else {
            PrimaryKeyJoinColumns annotations = (PrimaryKeyJoinColumns)this.getJavaAnnotation(PrimaryKeyJoinColumns.class);
            columns = annotations != null ? annotations.value() : columns;
         }
      }

      if (columns.length > 0) {
         AnnotationDescriptor ad = new AnnotationDescriptor(PrimaryKeyJoinColumns.class);
         ad.setValue("value", columns);
         return (PrimaryKeyJoinColumns)AnnotationFactory.create(ad);
      } else {
         return null;
      }
   }

   private Entity getEntity(Element tree, XMLContext.Default defaults) {
      if (tree == null) {
         return defaults.canUseJavaAnnotations() ? (Entity)this.getJavaAnnotation(Entity.class) : null;
      } else if ("entity".equals(tree.getName())) {
         AnnotationDescriptor entity = new AnnotationDescriptor(Entity.class);
         copyStringAttribute(entity, tree, "name", false);
         if (defaults.canUseJavaAnnotations() && StringHelper.isEmpty((String)entity.valueOf("name"))) {
            Entity javaAnn = (Entity)this.getJavaAnnotation(Entity.class);
            if (javaAnn != null) {
               entity.setValue("name", javaAnn.name());
            }
         }

         return (Entity)AnnotationFactory.create(entity);
      } else {
         return null;
      }
   }

   private MappedSuperclass getMappedSuperclass(Element tree, XMLContext.Default defaults) {
      if (tree == null) {
         return defaults.canUseJavaAnnotations() ? (MappedSuperclass)this.getJavaAnnotation(MappedSuperclass.class) : null;
      } else if ("mapped-superclass".equals(tree.getName())) {
         AnnotationDescriptor entity = new AnnotationDescriptor(MappedSuperclass.class);
         return (MappedSuperclass)AnnotationFactory.create(entity);
      } else {
         return null;
      }
   }

   private Embeddable getEmbeddable(Element tree, XMLContext.Default defaults) {
      if (tree == null) {
         return defaults.canUseJavaAnnotations() ? (Embeddable)this.getJavaAnnotation(Embeddable.class) : null;
      } else if ("embeddable".equals(tree.getName())) {
         AnnotationDescriptor entity = new AnnotationDescriptor(Embeddable.class);
         return (Embeddable)AnnotationFactory.create(entity);
      } else {
         return null;
      }
   }

   private Table getTable(Element tree, XMLContext.Default defaults) {
      Element subelement = tree == null ? null : tree.element("table");
      if (subelement == null) {
         if (!StringHelper.isNotEmpty(defaults.getCatalog()) && !StringHelper.isNotEmpty(defaults.getSchema())) {
            return defaults.canUseJavaAnnotations() ? (Table)this.getJavaAnnotation(Table.class) : null;
         } else {
            AnnotationDescriptor annotation = new AnnotationDescriptor(Table.class);
            if (defaults.canUseJavaAnnotations()) {
               Table table = (Table)this.getJavaAnnotation(Table.class);
               if (table != null) {
                  annotation.setValue("name", table.name());
                  annotation.setValue("schema", table.schema());
                  annotation.setValue("catalog", table.catalog());
                  annotation.setValue("uniqueConstraints", table.uniqueConstraints());
               }
            }

            if (StringHelper.isEmpty((String)annotation.valueOf("schema")) && StringHelper.isNotEmpty(defaults.getSchema())) {
               annotation.setValue("schema", defaults.getSchema());
            }

            if (StringHelper.isEmpty((String)annotation.valueOf("catalog")) && StringHelper.isNotEmpty(defaults.getCatalog())) {
               annotation.setValue("catalog", defaults.getCatalog());
            }

            return (Table)AnnotationFactory.create(annotation);
         }
      } else {
         AnnotationDescriptor annotation = new AnnotationDescriptor(Table.class);
         copyStringAttribute(annotation, subelement, "name", false);
         copyStringAttribute(annotation, subelement, "catalog", false);
         if (StringHelper.isNotEmpty(defaults.getCatalog()) && StringHelper.isEmpty((String)annotation.valueOf("catalog"))) {
            annotation.setValue("catalog", defaults.getCatalog());
         }

         copyStringAttribute(annotation, subelement, "schema", false);
         if (StringHelper.isNotEmpty(defaults.getSchema()) && StringHelper.isEmpty((String)annotation.valueOf("schema"))) {
            annotation.setValue("schema", defaults.getSchema());
         }

         buildUniqueConstraints(annotation, subelement);
         return (Table)AnnotationFactory.create(annotation);
      }
   }

   private SecondaryTables getSecondaryTables(Element tree, XMLContext.Default defaults) {
      List<Element> elements = (List<Element>)(tree == null ? new ArrayList() : tree.elements("secondary-table"));
      List<SecondaryTable> secondaryTables = new ArrayList(3);

      for(Element element : elements) {
         AnnotationDescriptor annotation = new AnnotationDescriptor(SecondaryTable.class);
         copyStringAttribute(annotation, element, "name", false);
         copyStringAttribute(annotation, element, "catalog", false);
         if (StringHelper.isNotEmpty(defaults.getCatalog()) && StringHelper.isEmpty((String)annotation.valueOf("catalog"))) {
            annotation.setValue("catalog", defaults.getCatalog());
         }

         copyStringAttribute(annotation, element, "schema", false);
         if (StringHelper.isNotEmpty(defaults.getSchema()) && StringHelper.isEmpty((String)annotation.valueOf("schema"))) {
            annotation.setValue("schema", defaults.getSchema());
         }

         buildUniqueConstraints(annotation, element);
         annotation.setValue("pkJoinColumns", this.buildPrimaryKeyJoinColumns(element));
         secondaryTables.add((SecondaryTable)AnnotationFactory.create(annotation));
      }

      if (secondaryTables.size() == 0 && defaults.canUseJavaAnnotations()) {
         SecondaryTable secTableAnn = (SecondaryTable)this.getJavaAnnotation(SecondaryTable.class);
         this.overridesDefaultInSecondaryTable(secTableAnn, defaults, secondaryTables);
         SecondaryTables secTablesAnn = (SecondaryTables)this.getJavaAnnotation(SecondaryTables.class);
         if (secTablesAnn != null) {
            for(SecondaryTable table : secTablesAnn.value()) {
               this.overridesDefaultInSecondaryTable(table, defaults, secondaryTables);
            }
         }
      }

      if (secondaryTables.size() > 0) {
         AnnotationDescriptor descriptor = new AnnotationDescriptor(SecondaryTables.class);
         descriptor.setValue("value", secondaryTables.toArray(new SecondaryTable[secondaryTables.size()]));
         return (SecondaryTables)AnnotationFactory.create(descriptor);
      } else {
         return null;
      }
   }

   private void overridesDefaultInSecondaryTable(SecondaryTable secTableAnn, XMLContext.Default defaults, List secondaryTables) {
      if (secTableAnn != null) {
         if (!StringHelper.isNotEmpty(defaults.getCatalog()) && !StringHelper.isNotEmpty(defaults.getSchema())) {
            secondaryTables.add(secTableAnn);
         } else {
            AnnotationDescriptor annotation = new AnnotationDescriptor(SecondaryTable.class);
            annotation.setValue("name", secTableAnn.name());
            annotation.setValue("schema", secTableAnn.schema());
            annotation.setValue("catalog", secTableAnn.catalog());
            annotation.setValue("uniqueConstraints", secTableAnn.uniqueConstraints());
            annotation.setValue("pkJoinColumns", secTableAnn.pkJoinColumns());
            if (StringHelper.isEmpty((String)annotation.valueOf("schema")) && StringHelper.isNotEmpty(defaults.getSchema())) {
               annotation.setValue("schema", defaults.getSchema());
            }

            if (StringHelper.isEmpty((String)annotation.valueOf("catalog")) && StringHelper.isNotEmpty(defaults.getCatalog())) {
               annotation.setValue("catalog", defaults.getCatalog());
            }

            secondaryTables.add((SecondaryTable)AnnotationFactory.create(annotation));
         }
      }

   }

   private static void buildUniqueConstraints(AnnotationDescriptor annotation, Element element) {
      List uniqueConstraintElementList = element.elements("unique-constraint");
      UniqueConstraint[] uniqueConstraints = new UniqueConstraint[uniqueConstraintElementList.size()];
      int ucIndex = 0;

      AnnotationDescriptor ucAnn;
      for(Iterator ucIt = uniqueConstraintElementList.listIterator(); ucIt.hasNext(); uniqueConstraints[ucIndex++] = (UniqueConstraint)AnnotationFactory.create(ucAnn)) {
         Element subelement = (Element)ucIt.next();
         List<Element> columnNamesElements = subelement.elements("column-name");
         String[] columnNames = new String[columnNamesElements.size()];
         int columnNameIndex = 0;

         for(Iterator it = columnNamesElements.listIterator(); it.hasNext(); columnNames[columnNameIndex++] = columnNameElt.getTextTrim()) {
            columnNameElt = (Element)it.next();
         }

         ucAnn = new AnnotationDescriptor(UniqueConstraint.class);
         copyStringAttribute(ucAnn, subelement, "name", false);
         ucAnn.setValue("columnNames", columnNames);
      }

      annotation.setValue("uniqueConstraints", uniqueConstraints);
   }

   private PrimaryKeyJoinColumn[] buildPrimaryKeyJoinColumns(Element element) {
      if (element == null) {
         return new PrimaryKeyJoinColumn[0];
      } else {
         List pkJoinColumnElementList = element.elements("primary-key-join-column");
         PrimaryKeyJoinColumn[] pkJoinColumns = new PrimaryKeyJoinColumn[pkJoinColumnElementList.size()];
         int index = 0;

         AnnotationDescriptor pkAnn;
         for(Iterator pkIt = pkJoinColumnElementList.listIterator(); pkIt.hasNext(); pkJoinColumns[index++] = (PrimaryKeyJoinColumn)AnnotationFactory.create(pkAnn)) {
            Element subelement = (Element)pkIt.next();
            pkAnn = new AnnotationDescriptor(PrimaryKeyJoinColumn.class);
            copyStringAttribute(pkAnn, subelement, "name", false);
            copyStringAttribute(pkAnn, subelement, "referenced-column-name", false);
            copyStringAttribute(pkAnn, subelement, "column-definition", false);
         }

         return pkJoinColumns;
      }
   }

   private static void copyStringAttribute(AnnotationDescriptor annotation, Element element, String attributeName, boolean mandatory) {
      String attribute = element.attributeValue(attributeName);
      if (attribute != null) {
         String annotationAttributeName = getJavaAttributeNameFromXMLOne(attributeName);
         annotation.setValue(annotationAttributeName, attribute);
      } else if (mandatory) {
         throw new AnnotationException(element.getName() + "." + attributeName + " is mandatory in XML overriding. " + "Activate schema validation for more information");
      }

   }

   private static void copyIntegerAttribute(AnnotationDescriptor annotation, Element element, String attributeName) {
      String attribute = element.attributeValue(attributeName);
      if (attribute != null) {
         String annotationAttributeName = getJavaAttributeNameFromXMLOne(attributeName);
         annotation.setValue(annotationAttributeName, attribute);

         try {
            int length = Integer.parseInt(attribute);
            annotation.setValue(annotationAttributeName, length);
         } catch (NumberFormatException var6) {
            throw new AnnotationException(element.getPath() + attributeName + " not parseable: " + attribute + " (" + "Activate schema validation for more information" + ")");
         }
      }

   }

   private static String getJavaAttributeNameFromXMLOne(String attributeName) {
      StringBuilder annotationAttributeName = new StringBuilder(attributeName);

      for(int index = annotationAttributeName.indexOf("-"); index != -1; index = annotationAttributeName.indexOf("-")) {
         annotationAttributeName.deleteCharAt(index);
         annotationAttributeName.setCharAt(index, Character.toUpperCase(annotationAttributeName.charAt(index)));
      }

      return annotationAttributeName.toString();
   }

   private static void copyStringElement(Element element, AnnotationDescriptor ad, String annotationAttribute) {
      String discr = element.getTextTrim();
      ad.setValue(annotationAttribute, discr);
   }

   private static void copyBooleanAttribute(AnnotationDescriptor descriptor, Element element, String attribute) {
      String attributeValue = element.attributeValue(attribute);
      if (StringHelper.isNotEmpty(attributeValue)) {
         String javaAttribute = getJavaAttributeNameFromXMLOne(attribute);
         descriptor.setValue(javaAttribute, Boolean.parseBoolean(attributeValue));
      }

   }

   private Annotation getJavaAnnotation(Class annotationType) {
      return this.element.getAnnotation(annotationType);
   }

   private boolean isJavaAnnotationPresent(Class annotationType) {
      return this.element.isAnnotationPresent(annotationType);
   }

   private Annotation[] getJavaAnnotations() {
      return this.element.getAnnotations();
   }

   static {
      annotationToXml.put(Entity.class, "entity");
      annotationToXml.put(MappedSuperclass.class, "mapped-superclass");
      annotationToXml.put(Embeddable.class, "embeddable");
      annotationToXml.put(Table.class, "table");
      annotationToXml.put(SecondaryTable.class, "secondary-table");
      annotationToXml.put(SecondaryTables.class, "secondary-table");
      annotationToXml.put(PrimaryKeyJoinColumn.class, "primary-key-join-column");
      annotationToXml.put(PrimaryKeyJoinColumns.class, "primary-key-join-column");
      annotationToXml.put(IdClass.class, "id-class");
      annotationToXml.put(Inheritance.class, "inheritance");
      annotationToXml.put(DiscriminatorValue.class, "discriminator-value");
      annotationToXml.put(DiscriminatorColumn.class, "discriminator-column");
      annotationToXml.put(SequenceGenerator.class, "sequence-generator");
      annotationToXml.put(TableGenerator.class, "table-generator");
      annotationToXml.put(NamedQuery.class, "named-query");
      annotationToXml.put(NamedQueries.class, "named-query");
      annotationToXml.put(NamedNativeQuery.class, "named-native-query");
      annotationToXml.put(NamedNativeQueries.class, "named-native-query");
      annotationToXml.put(SqlResultSetMapping.class, "sql-result-set-mapping");
      annotationToXml.put(SqlResultSetMappings.class, "sql-result-set-mapping");
      annotationToXml.put(ExcludeDefaultListeners.class, "exclude-default-listeners");
      annotationToXml.put(ExcludeSuperclassListeners.class, "exclude-superclass-listeners");
      annotationToXml.put(AccessType.class, "access");
      annotationToXml.put(AttributeOverride.class, "attribute-override");
      annotationToXml.put(AttributeOverrides.class, "attribute-override");
      annotationToXml.put(AttributeOverride.class, "association-override");
      annotationToXml.put(AttributeOverrides.class, "association-override");
      annotationToXml.put(AttributeOverride.class, "map-key-attribute-override");
      annotationToXml.put(AttributeOverrides.class, "map-key-attribute-override");
      annotationToXml.put(Id.class, "id");
      annotationToXml.put(EmbeddedId.class, "embedded-id");
      annotationToXml.put(GeneratedValue.class, "generated-value");
      annotationToXml.put(Column.class, "column");
      annotationToXml.put(Columns.class, "column");
      annotationToXml.put(Temporal.class, "temporal");
      annotationToXml.put(Lob.class, "lob");
      annotationToXml.put(Enumerated.class, "enumerated");
      annotationToXml.put(Version.class, "version");
      annotationToXml.put(Transient.class, "transient");
      annotationToXml.put(Basic.class, "basic");
      annotationToXml.put(Embedded.class, "embedded");
      annotationToXml.put(ManyToOne.class, "many-to-one");
      annotationToXml.put(OneToOne.class, "one-to-one");
      annotationToXml.put(OneToMany.class, "one-to-many");
      annotationToXml.put(ManyToMany.class, "many-to-many");
      annotationToXml.put(JoinTable.class, "join-table");
      annotationToXml.put(JoinColumn.class, "join-column");
      annotationToXml.put(JoinColumns.class, "join-column");
      annotationToXml.put(MapKey.class, "map-key");
      annotationToXml.put(OrderBy.class, "order-by");
      annotationToXml.put(EntityListeners.class, "entity-listeners");
      annotationToXml.put(PrePersist.class, "pre-persist");
      annotationToXml.put(PreRemove.class, "pre-remove");
      annotationToXml.put(PreUpdate.class, "pre-update");
      annotationToXml.put(PostPersist.class, "post-persist");
      annotationToXml.put(PostRemove.class, "post-remove");
      annotationToXml.put(PostUpdate.class, "post-update");
      annotationToXml.put(PostLoad.class, "post-load");
      annotationToXml.put(CollectionTable.class, "collection-table");
      annotationToXml.put(MapKeyClass.class, "map-key-class");
      annotationToXml.put(MapKeyTemporal.class, "map-key-temporal");
      annotationToXml.put(MapKeyEnumerated.class, "map-key-enumerated");
      annotationToXml.put(MapKeyColumn.class, "map-key-column");
      annotationToXml.put(MapKeyJoinColumn.class, "map-key-join-column");
      annotationToXml.put(MapKeyJoinColumns.class, "map-key-join-column");
      annotationToXml.put(OrderColumn.class, "order-column");
      annotationToXml.put(Cacheable.class, "cacheable");
   }

   private static enum PropertyType {
      PROPERTY,
      FIELD,
      METHOD;
   }
}
