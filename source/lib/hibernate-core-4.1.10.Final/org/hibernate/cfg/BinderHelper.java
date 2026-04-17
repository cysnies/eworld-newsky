package org.hibernate.cfg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import org.hibernate.AnnotationException;
import org.hibernate.AssertionFailure;
import org.hibernate.MappingException;
import org.hibernate.annotations.AnyMetaDef;
import org.hibernate.annotations.AnyMetaDefs;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.MetaValue;
import org.hibernate.annotations.SqlFragmentAlias;
import org.hibernate.annotations.common.reflection.XAnnotatedElement;
import org.hibernate.annotations.common.reflection.XClass;
import org.hibernate.annotations.common.reflection.XPackage;
import org.hibernate.cfg.annotations.EntityBinder;
import org.hibernate.cfg.annotations.Nullability;
import org.hibernate.cfg.annotations.TableBinder;
import org.hibernate.id.MultipleHiLoPerTableGenerator;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.mapping.Any;
import org.hibernate.mapping.Collection;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Component;
import org.hibernate.mapping.IdGenerator;
import org.hibernate.mapping.Join;
import org.hibernate.mapping.MappedSuperclass;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.SimpleValue;
import org.hibernate.mapping.SyntheticProperty;
import org.hibernate.mapping.Table;
import org.hibernate.mapping.ToOne;
import org.hibernate.mapping.Value;
import org.hibernate.type.DiscriminatorType;
import org.hibernate.type.Type;
import org.jboss.logging.Logger;

public class BinderHelper {
   public static final String ANNOTATION_STRING_DEFAULT = "";
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, BinderHelper.class.getName());
   public static final Set PRIMITIVE_NAMES;

   private BinderHelper() {
   }

   public static Property shallowCopy(Property property) {
      Property clone = new Property();
      clone.setCascade(property.getCascade());
      clone.setInsertable(property.isInsertable());
      clone.setLazy(property.isLazy());
      clone.setName(property.getName());
      clone.setNodeName(property.getNodeName());
      clone.setNaturalIdentifier(property.isNaturalIdentifier());
      clone.setOptimisticLocked(property.isOptimisticLocked());
      clone.setOptional(property.isOptional());
      clone.setPersistentClass(property.getPersistentClass());
      clone.setPropertyAccessorName(property.getPropertyAccessorName());
      clone.setSelectable(property.isSelectable());
      clone.setUpdateable(property.isUpdateable());
      clone.setValue(property.getValue());
      return clone;
   }

   public static void createSyntheticPropertyReference(Ejb3JoinColumn[] columns, PersistentClass ownerEntity, PersistentClass associatedEntity, Value value, boolean inverse, Mappings mappings) {
      if (!columns[0].isImplicit() && !StringHelper.isNotEmpty(columns[0].getMappedBy())) {
         int fkEnum = Ejb3JoinColumn.checkReferencedColumnsType(columns, ownerEntity, mappings);
         PersistentClass associatedClass = columns[0].getPropertyHolder() != null ? columns[0].getPropertyHolder().getPersistentClass() : null;
         if (2 == fkEnum) {
            StringBuilder propertyNameBuffer = new StringBuilder("_");
            propertyNameBuffer.append(associatedClass.getEntityName().replace('.', '_'));
            propertyNameBuffer.append("_").append(columns[0].getPropertyName().replace('.', '_'));
            String syntheticPropertyName = propertyNameBuffer.toString();
            Object columnOwner = findColumnOwner(ownerEntity, columns[0].getReferencedColumn(), mappings);
            List<Property> properties = findPropertiesByColumns(columnOwner, columns, mappings);
            Property synthProp = null;
            if (properties == null) {
               StringBuilder columnsList = new StringBuilder();
               columnsList.append("referencedColumnNames(");

               for(Ejb3JoinColumn column : columns) {
                  columnsList.append(column.getReferencedColumn()).append(", ");
               }

               columnsList.setLength(columnsList.length() - 2);
               columnsList.append(") ");
               if (associatedEntity != null) {
                  columnsList.append("of ").append(associatedEntity.getEntityName()).append(".").append(columns[0].getPropertyName()).append(" ");
               } else if (columns[0].getPropertyHolder() != null) {
                  columnsList.append("of ").append(columns[0].getPropertyHolder().getEntityName()).append(".").append(columns[0].getPropertyName()).append(" ");
               }

               columnsList.append("referencing ").append(ownerEntity.getEntityName()).append(" not mapped to a single property");
               throw new AnnotationException(columnsList.toString());
            }

            Component embeddedComp = columnOwner instanceof PersistentClass ? new Component(mappings, (PersistentClass)columnOwner) : new Component(mappings, (Join)columnOwner);
            embeddedComp.setEmbedded(true);
            embeddedComp.setNodeName(syntheticPropertyName);
            embeddedComp.setComponentClassName(embeddedComp.getOwner().getClassName());

            for(Property property : properties) {
               Property clone = shallowCopy(property);
               clone.setInsertable(false);
               clone.setUpdateable(false);
               clone.setNaturalIdentifier(false);
               clone.setGeneration(property.getGeneration());
               embeddedComp.addProperty(clone);
            }

            synthProp = new SyntheticProperty();
            synthProp.setName(syntheticPropertyName);
            synthProp.setNodeName(syntheticPropertyName);
            synthProp.setPersistentClass(ownerEntity);
            synthProp.setUpdateable(false);
            synthProp.setInsertable(false);
            synthProp.setValue(embeddedComp);
            synthProp.setPropertyAccessorName("embedded");
            ownerEntity.addProperty(synthProp);
            TableBinder.createUniqueConstraint(embeddedComp);
            if (value instanceof ToOne) {
               ((ToOne)value).setReferencedPropertyName(syntheticPropertyName);
               mappings.addUniquePropertyReference(ownerEntity.getEntityName(), syntheticPropertyName);
            } else {
               if (!(value instanceof Collection)) {
                  throw new AssertionFailure("Do a property ref on an unexpected Value type: " + value.getClass().getName());
               }

               ((Collection)value).setReferencedPropertyName(syntheticPropertyName);
               mappings.addPropertyReference(ownerEntity.getEntityName(), syntheticPropertyName);
            }

            mappings.addPropertyReferencedAssociation((inverse ? "inverse__" : "") + associatedClass.getEntityName(), columns[0].getPropertyName(), syntheticPropertyName);
         }

      }
   }

   private static List findPropertiesByColumns(Object columnOwner, Ejb3JoinColumn[] columns, Mappings mappings) {
      Map<Column, Set<Property>> columnsToProperty = new HashMap();
      List<Column> orderedColumns = new ArrayList(columns.length);
      Table referencedTable = null;
      if (columnOwner instanceof PersistentClass) {
         referencedTable = ((PersistentClass)columnOwner).getTable();
      } else {
         if (!(columnOwner instanceof Join)) {
            throw new AssertionFailure(columnOwner == null ? "columnOwner is null" : "columnOwner neither PersistentClass nor Join: " + columnOwner.getClass());
         }

         referencedTable = ((Join)columnOwner).getTable();
      }

      for(Ejb3JoinColumn column1 : columns) {
         Column column = new Column(mappings.getPhysicalColumnName(column1.getReferencedColumn(), referencedTable));
         orderedColumns.add(column);
         columnsToProperty.put(column, new HashSet());
      }

      boolean isPersistentClass = columnOwner instanceof PersistentClass;
      Iterator it = isPersistentClass ? ((PersistentClass)columnOwner).getPropertyIterator() : ((Join)columnOwner).getPropertyIterator();

      while(it.hasNext()) {
         matchColumnsByProperty((Property)it.next(), columnsToProperty);
      }

      if (isPersistentClass) {
         matchColumnsByProperty(((PersistentClass)columnOwner).getIdentifierProperty(), columnsToProperty);
      }

      List<Property> orderedProperties = new ArrayList();

      for(Column column : orderedColumns) {
         boolean found = false;

         for(Property property : (Set)columnsToProperty.get(column)) {
            if (property.getColumnSpan() == 1) {
               orderedProperties.add(property);
               found = true;
               break;
            }
         }

         if (!found) {
            return null;
         }
      }

      return orderedProperties;
   }

   private static void matchColumnsByProperty(Property property, Map columnsToProperty) {
      if (property != null) {
         if (!"noop".equals(property.getPropertyAccessorName()) && !"embedded".equals(property.getPropertyAccessorName())) {
            Iterator columnIt = property.getColumnIterator();

            while(columnIt.hasNext()) {
               Object column = columnIt.next();
               if (columnsToProperty.containsKey(column)) {
                  ((Set)columnsToProperty.get(column)).add(property);
               }
            }

         }
      }
   }

   public static Property findPropertyByName(PersistentClass associatedClass, String propertyName) {
      Property property = null;
      Property idProperty = associatedClass.getIdentifierProperty();
      String idName = idProperty != null ? idProperty.getName() : null;

      try {
         if (propertyName != null && propertyName.length() != 0 && !propertyName.equals(idName)) {
            if (propertyName.indexOf(idName + ".") == 0) {
               property = idProperty;
               propertyName = propertyName.substring(idName.length() + 1);
            }

            StringTokenizer st = new StringTokenizer(propertyName, ".", false);

            while(st.hasMoreElements()) {
               String element = (String)st.nextElement();
               if (property == null) {
                  property = associatedClass.getProperty(element);
               } else {
                  if (!property.isComposite()) {
                     return null;
                  }

                  property = ((Component)property.getValue()).getProperty(element);
               }
            }
         } else {
            property = idProperty;
         }
      } catch (MappingException var9) {
         try {
            if (associatedClass.getIdentifierMapper() == null) {
               return null;
            }

            StringTokenizer st = new StringTokenizer(propertyName, ".", false);

            while(st.hasMoreElements()) {
               String element = (String)st.nextElement();
               if (property == null) {
                  property = associatedClass.getIdentifierMapper().getProperty(element);
               } else {
                  if (!property.isComposite()) {
                     return null;
                  }

                  property = ((Component)property.getValue()).getProperty(element);
               }
            }
         } catch (MappingException var8) {
            return null;
         }
      }

      return property;
   }

   public static Property findPropertyByName(Component component, String propertyName) {
      Property property = null;

      try {
         if (propertyName == null || propertyName.length() == 0) {
            return null;
         }

         StringTokenizer st = new StringTokenizer(propertyName, ".", false);

         while(st.hasMoreElements()) {
            String element = (String)st.nextElement();
            if (property == null) {
               property = component.getProperty(element);
            } else {
               if (!property.isComposite()) {
                  return null;
               }

               property = ((Component)property.getValue()).getProperty(element);
            }
         }
      } catch (MappingException var7) {
         try {
            if (component.getOwner().getIdentifierMapper() == null) {
               return null;
            }

            StringTokenizer st = new StringTokenizer(propertyName, ".", false);

            while(st.hasMoreElements()) {
               String element = (String)st.nextElement();
               if (property == null) {
                  property = component.getOwner().getIdentifierMapper().getProperty(element);
               } else {
                  if (!property.isComposite()) {
                     return null;
                  }

                  property = ((Component)property.getValue()).getProperty(element);
               }
            }
         } catch (MappingException var6) {
            return null;
         }
      }

      return property;
   }

   public static String getRelativePath(PropertyHolder propertyHolder, String propertyName) {
      if (propertyHolder == null) {
         return propertyName;
      } else {
         String path = propertyHolder.getPath();
         String entityName = propertyHolder.getPersistentClass().getEntityName();
         return path.length() == entityName.length() ? propertyName : StringHelper.qualify(path.substring(entityName.length() + 1), propertyName);
      }
   }

   public static Object findColumnOwner(PersistentClass persistentClass, String columnName, Mappings mappings) {
      if (StringHelper.isEmpty(columnName)) {
         return persistentClass;
      } else {
         PersistentClass current = persistentClass;
         boolean found = false;

         Object result;
         do {
            result = current;
            Table currentTable = current.getTable();

            try {
               mappings.getPhysicalColumnName(columnName, currentTable);
               found = true;
            } catch (MappingException var10) {
            }

            Iterator joins = current.getJoinIterator();

            while(!found && joins.hasNext()) {
               result = joins.next();
               currentTable = ((Join)result).getTable();

               try {
                  mappings.getPhysicalColumnName(columnName, currentTable);
                  found = true;
               } catch (MappingException var9) {
               }
            }

            current = current.getSuperclass();
         } while(!found && current != null);

         return found ? result : null;
      }
   }

   public static void makeIdGenerator(SimpleValue id, String generatorType, String generatorName, Mappings mappings, Map localGenerators) {
      Table table = id.getTable();
      table.setIdentifierValue(id);
      id.setIdentifierGeneratorStrategy(generatorType);
      Properties params = new Properties();
      params.setProperty("target_table", table.getName());
      if (id.getColumnSpan() == 1) {
         params.setProperty("target_column", ((Column)id.getColumnIterator().next()).getName());
      }

      params.put("identifier_normalizer", mappings.getObjectNameNormalizer());
      if (!isEmptyAnnotationValue(generatorName)) {
         IdGenerator gen = mappings.getGenerator(generatorName, localGenerators);
         if (gen == null) {
            throw new AnnotationException("Unknown Id.generator: " + generatorName);
         }

         String identifierGeneratorStrategy = gen.getIdentifierGeneratorStrategy();
         boolean avoidOverriding = identifierGeneratorStrategy.equals("identity") || identifierGeneratorStrategy.equals("seqhilo") || identifierGeneratorStrategy.equals(MultipleHiLoPerTableGenerator.class.getName());
         if (generatorType == null || !avoidOverriding) {
            id.setIdentifierGeneratorStrategy(identifierGeneratorStrategy);
         }

         for(Map.Entry elt : gen.getParams().entrySet()) {
            params.setProperty((String)elt.getKey(), (String)elt.getValue());
         }
      }

      if ("assigned".equals(generatorType)) {
         id.setNullValue("undefined");
      }

      id.setIdentifierGeneratorProperties(params);
   }

   public static boolean isEmptyAnnotationValue(String annotationString) {
      return annotationString != null && annotationString.length() == 0;
   }

   public static Any buildAnyValue(String anyMetaDefName, Ejb3JoinColumn[] columns, javax.persistence.Column metaColumn, PropertyData inferredData, boolean cascadeOnDelete, Nullability nullability, PropertyHolder propertyHolder, EntityBinder entityBinder, boolean optional, Mappings mappings) {
      Any value = new Any(mappings, columns[0].getTable());
      AnyMetaDef metaAnnDef = (AnyMetaDef)inferredData.getProperty().getAnnotation(AnyMetaDef.class);
      if (metaAnnDef != null) {
         bindAnyMetaDefs(inferredData.getProperty(), mappings);
      } else {
         metaAnnDef = mappings.getAnyMetaDef(anyMetaDefName);
      }

      if (metaAnnDef == null) {
         throw new AnnotationException("Unable to find @AnyMetaDef for an @(ManyTo)Any mapping: " + StringHelper.qualify(propertyHolder.getPath(), inferredData.getPropertyName()));
      } else {
         value.setIdentifierType(metaAnnDef.idType());
         value.setMetaType(metaAnnDef.metaType());
         HashMap values = new HashMap();
         Type metaType = mappings.getTypeResolver().heuristicType(value.getMetaType());

         for(MetaValue metaValue : metaAnnDef.metaValues()) {
            try {
               Object discrim = ((DiscriminatorType)metaType).stringToObject(metaValue.value());
               String entityName = metaValue.targetEntity().getName();
               values.put(discrim, entityName);
            } catch (ClassCastException var20) {
               throw new MappingException("metaType was not a DiscriminatorType: " + metaType.getName());
            } catch (Exception e) {
               throw new MappingException("could not interpret metaValue", e);
            }
         }

         if (!values.isEmpty()) {
            value.setMetaValues(values);
         }

         value.setCascadeDeleteEnabled(cascadeOnDelete);
         if (!optional) {
            for(Ejb3JoinColumn column : columns) {
               column.setNullable(false);
            }
         }

         Ejb3Column[] metaColumns = Ejb3Column.buildColumnFromAnnotation(new javax.persistence.Column[]{metaColumn}, (Formula)null, nullability, propertyHolder, inferredData, entityBinder.getSecondaryTables(), mappings);

         for(Ejb3Column column : metaColumns) {
            column.setTable(value.getTable());
         }

         for(Ejb3Column column : metaColumns) {
            column.linkWithValue(value);
         }

         String propertyName = inferredData.getPropertyName();
         Ejb3Column.checkPropertyConsistency(columns, propertyHolder.getEntityName() + propertyName);

         for(Ejb3JoinColumn column : columns) {
            column.linkWithValue(value);
         }

         return value;
      }
   }

   public static void bindAnyMetaDefs(XAnnotatedElement annotatedElement, Mappings mappings) {
      AnyMetaDef defAnn = (AnyMetaDef)annotatedElement.getAnnotation(AnyMetaDef.class);
      AnyMetaDefs defsAnn = (AnyMetaDefs)annotatedElement.getAnnotation(AnyMetaDefs.class);
      boolean mustHaveName = XClass.class.isAssignableFrom(annotatedElement.getClass()) || XPackage.class.isAssignableFrom(annotatedElement.getClass());
      if (defAnn != null) {
         checkAnyMetaDefValidity(mustHaveName, defAnn, annotatedElement);
         bindAnyMetaDef(defAnn, mappings);
      }

      if (defsAnn != null) {
         for(AnyMetaDef def : defsAnn.value()) {
            checkAnyMetaDefValidity(mustHaveName, def, annotatedElement);
            bindAnyMetaDef(def, mappings);
         }
      }

   }

   private static void checkAnyMetaDefValidity(boolean mustHaveName, AnyMetaDef defAnn, XAnnotatedElement annotatedElement) {
      if (mustHaveName && isEmptyAnnotationValue(defAnn.name())) {
         String name = XClass.class.isAssignableFrom(annotatedElement.getClass()) ? ((XClass)annotatedElement).getName() : ((XPackage)annotatedElement).getName();
         throw new AnnotationException("@AnyMetaDef.name cannot be null on an entity or a package: " + name);
      }
   }

   private static void bindAnyMetaDef(AnyMetaDef defAnn, Mappings mappings) {
      if (!isEmptyAnnotationValue(defAnn.name())) {
         if (LOG.isDebugEnabled()) {
            LOG.debugf("Binding Any Meta definition: %s", defAnn.name());
         }

         mappings.addAnyMetaDef(defAnn);
      }
   }

   public static MappedSuperclass getMappedSuperclassOrNull(XClass declaringClass, Map inheritanceStatePerClass, Mappings mappings) {
      boolean retrieve = false;
      if (declaringClass != null) {
         InheritanceState inheritanceState = (InheritanceState)inheritanceStatePerClass.get(declaringClass);
         if (inheritanceState == null) {
            throw new org.hibernate.annotations.common.AssertionFailure("Declaring class is not found in the inheritance state hierarchy: " + declaringClass);
         }

         if (inheritanceState.isEmbeddableSuperclass()) {
            retrieve = true;
         }
      }

      return retrieve ? mappings.getMappedSuperclass(mappings.getReflectionManager().toClass(declaringClass)) : null;
   }

   public static String getPath(PropertyHolder holder, PropertyData property) {
      return StringHelper.qualify(holder.getPath(), property.getPropertyName());
   }

   static PropertyData getPropertyOverriddenByMapperOrMapsId(boolean isId, PropertyHolder propertyHolder, String propertyName, Mappings mappings) {
      XClass persistentXClass;
      try {
         persistentXClass = mappings.getReflectionManager().classForName(propertyHolder.getPersistentClass().getClassName(), AnnotationBinder.class);
      } catch (ClassNotFoundException e) {
         throw new AssertionFailure("PersistentClass name cannot be converted into a Class", e);
      }

      if (propertyHolder.isInIdClass()) {
         PropertyData pd = mappings.getPropertyAnnotatedWithIdAndToOne(persistentXClass, propertyName);
         if (pd == null && mappings.isSpecjProprietarySyntaxEnabled()) {
            pd = mappings.getPropertyAnnotatedWithMapsId(persistentXClass, propertyName);
         }

         return pd;
      } else {
         String propertyPath = isId ? "" : propertyName;
         return mappings.getPropertyAnnotatedWithMapsId(persistentXClass, propertyPath);
      }
   }

   public static Map toAliasTableMap(SqlFragmentAlias[] aliases) {
      Map<String, String> ret = new HashMap();

      for(int i = 0; i < aliases.length; ++i) {
         if (StringHelper.isNotEmpty(aliases[i].table())) {
            ret.put(aliases[i].alias(), aliases[i].table());
         }
      }

      return ret;
   }

   public static Map toAliasEntityMap(SqlFragmentAlias[] aliases) {
      Map<String, String> ret = new HashMap();

      for(int i = 0; i < aliases.length; ++i) {
         if (aliases[i].entity() != Void.TYPE) {
            ret.put(aliases[i].alias(), aliases[i].entity().getName());
         }
      }

      return ret;
   }

   static {
      Set<String> primitiveNames = new HashSet();
      primitiveNames.add(Byte.TYPE.getName());
      primitiveNames.add(Short.TYPE.getName());
      primitiveNames.add(Integer.TYPE.getName());
      primitiveNames.add(Long.TYPE.getName());
      primitiveNames.add(Float.TYPE.getName());
      primitiveNames.add(Double.TYPE.getName());
      primitiveNames.add(Character.TYPE.getName());
      primitiveNames.add(Boolean.TYPE.getName());
      PRIMITIVE_NAMES = Collections.unmodifiableSet(primitiveNames);
   }
}
