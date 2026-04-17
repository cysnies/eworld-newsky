package org.hibernate.cfg.annotations;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.MapKeyClass;
import org.hibernate.AnnotationException;
import org.hibernate.AssertionFailure;
import org.hibernate.FetchMode;
import org.hibernate.MappingException;
import org.hibernate.annotations.MapKeyType;
import org.hibernate.annotations.common.reflection.XClass;
import org.hibernate.annotations.common.reflection.XProperty;
import org.hibernate.cfg.AccessType;
import org.hibernate.cfg.AnnotatedClassType;
import org.hibernate.cfg.AnnotationBinder;
import org.hibernate.cfg.BinderHelper;
import org.hibernate.cfg.CollectionSecondPass;
import org.hibernate.cfg.Ejb3Column;
import org.hibernate.cfg.Ejb3JoinColumn;
import org.hibernate.cfg.Mappings;
import org.hibernate.cfg.PropertyData;
import org.hibernate.cfg.PropertyHolder;
import org.hibernate.cfg.PropertyHolderBuilder;
import org.hibernate.cfg.PropertyPreloadedData;
import org.hibernate.cfg.SecondPass;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.HSQLDialect;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.mapping.Collection;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Component;
import org.hibernate.mapping.DependantValue;
import org.hibernate.mapping.Formula;
import org.hibernate.mapping.ManyToOne;
import org.hibernate.mapping.Map;
import org.hibernate.mapping.OneToMany;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.SimpleValue;
import org.hibernate.mapping.ToOne;
import org.hibernate.mapping.Value;
import org.hibernate.sql.Template;

public class MapBinder extends CollectionBinder {
   public MapBinder(boolean sorted) {
      super(sorted);
   }

   public MapBinder() {
   }

   public boolean isMap() {
      return true;
   }

   protected Collection createCollection(PersistentClass persistentClass) {
      return new Map(this.getMappings(), persistentClass);
   }

   public SecondPass getSecondPass(final Ejb3JoinColumn[] fkJoinColumns, final Ejb3JoinColumn[] keyColumns, final Ejb3JoinColumn[] inverseColumns, final Ejb3Column[] elementColumns, final Ejb3Column[] mapKeyColumns, final Ejb3JoinColumn[] mapKeyManyToManyColumns, final boolean isEmbedded, final XProperty property, final XClass collType, final boolean ignoreNotFound, final boolean unique, final TableBinder assocTableBinder, final Mappings mappings) {
      return new CollectionSecondPass(mappings, this.collection) {
         public void secondPass(java.util.Map persistentClasses, java.util.Map inheritedMetas) throws MappingException {
            MapBinder.this.bindStarToManySecondPass(persistentClasses, collType, fkJoinColumns, keyColumns, inverseColumns, elementColumns, isEmbedded, property, unique, assocTableBinder, ignoreNotFound, mappings);
            MapBinder.this.bindKeyFromAssociationTable(collType, persistentClasses, MapBinder.this.mapKeyPropertyName, property, isEmbedded, mappings, mapKeyColumns, mapKeyManyToManyColumns, inverseColumns != null ? inverseColumns[0].getPropertyName() : null);
         }
      };
   }

   private void bindKeyFromAssociationTable(XClass collType, java.util.Map persistentClasses, String mapKeyPropertyName, XProperty property, boolean isEmbedded, Mappings mappings, Ejb3Column[] mapKeyColumns, Ejb3JoinColumn[] mapKeyManyToManyColumns, String targetPropertyName) {
      if (mapKeyPropertyName != null) {
         PersistentClass associatedClass = (PersistentClass)persistentClasses.get(collType.getName());
         if (associatedClass == null) {
            throw new AnnotationException("Associated class not found: " + collType);
         }

         Property mapProperty = BinderHelper.findPropertyByName(associatedClass, mapKeyPropertyName);
         if (mapProperty == null) {
            throw new AnnotationException("Map key property not found: " + collType + "." + mapKeyPropertyName);
         }

         Map map = (Map)this.collection;
         Value indexValue = this.createFormulatedValue(mapProperty.getValue(), map, targetPropertyName, associatedClass, mappings);
         map.setIndex(indexValue);
      } else {
         Class target = Void.TYPE;
         if (property.isAnnotationPresent(MapKeyClass.class)) {
            target = ((MapKeyClass)property.getAnnotation(MapKeyClass.class)).value();
         }

         String mapKeyType;
         if (!Void.TYPE.equals(target)) {
            mapKeyType = target.getName();
         } else {
            mapKeyType = property.getMapKey().getName();
         }

         PersistentClass collectionEntity = (PersistentClass)persistentClasses.get(mapKeyType);
         boolean isIndexOfEntities = collectionEntity != null;
         ManyToOne element = null;
         Map mapValue = (Map)this.collection;
         if (isIndexOfEntities) {
            element = new ManyToOne(mappings, mapValue.getCollectionTable());
            mapValue.setIndex(element);
            element.setReferencedEntityName(mapKeyType);
            element.setFetchMode(FetchMode.JOIN);
            element.setLazy(false);
         } else {
            PropertyHolder holder = null;
            XClass elementClass;
            AnnotatedClassType classType;
            if (BinderHelper.PRIMITIVE_NAMES.contains(mapKeyType)) {
               classType = AnnotatedClassType.NONE;
               elementClass = null;
            } else {
               try {
                  elementClass = mappings.getReflectionManager().classForName(mapKeyType, MapBinder.class);
               } catch (ClassNotFoundException e) {
                  throw new AnnotationException("Unable to find class: " + mapKeyType, e);
               }

               classType = mappings.getClassType(elementClass);
               holder = PropertyHolderBuilder.buildPropertyHolder(mapValue, StringHelper.qualify(mapValue.getRole(), "mapkey"), elementClass, property, this.propertyHolder, mappings);
               boolean attributeOverride = property.isAnnotationPresent(AttributeOverride.class) || property.isAnnotationPresent(AttributeOverrides.class);
               if (isEmbedded || attributeOverride) {
                  classType = AnnotatedClassType.EMBEDDABLE;
               }
            }

            PersistentClass owner = mapValue.getOwner();
            AccessType accessType;
            if (owner.getIdentifierProperty() != null) {
               accessType = owner.getIdentifierProperty().getPropertyAccessorName().equals("property") ? AccessType.PROPERTY : AccessType.FIELD;
            } else {
               if (owner.getIdentifierMapper() == null || owner.getIdentifierMapper().getPropertySpan() <= 0) {
                  throw new AssertionFailure("Unable to guess collection property accessor name");
               }

               Property prop = (Property)owner.getIdentifierMapper().getPropertyIterator().next();
               accessType = prop.getPropertyAccessorName().equals("property") ? AccessType.PROPERTY : AccessType.FIELD;
            }

            if (AnnotatedClassType.EMBEDDABLE.equals(classType)) {
               EntityBinder entityBinder = new EntityBinder();
               PropertyData inferredData;
               if (this.isHibernateExtensionMapping()) {
                  inferredData = new PropertyPreloadedData(AccessType.PROPERTY, "index", elementClass);
               } else {
                  inferredData = new PropertyPreloadedData(AccessType.PROPERTY, "key", elementClass);
               }

               Component component = AnnotationBinder.fillComponent(holder, inferredData, accessType, true, entityBinder, false, false, true, mappings, this.inheritanceStatePerClass);
               mapValue.setIndex(component);
            } else {
               SimpleValueBinder elementBinder = new SimpleValueBinder();
               elementBinder.setMappings(mappings);
               elementBinder.setReturnedClassName(mapKeyType);
               Ejb3Column[] elementColumns = mapKeyColumns;
               if (mapKeyColumns == null || mapKeyColumns.length == 0) {
                  elementColumns = new Ejb3Column[1];
                  Ejb3Column column = new Ejb3Column();
                  column.setImplicit(false);
                  column.setNullable(true);
                  column.setLength(255);
                  column.setLogicalColumnName("id");
                  column.setJoins(new HashMap());
                  column.setMappings(mappings);
                  column.bind();
                  elementColumns[0] = column;
               }

               for(Ejb3Column column : elementColumns) {
                  column.setTable(mapValue.getCollectionTable());
               }

               elementBinder.setColumns(elementColumns);
               elementBinder.setKey(true);
               MapKeyType mapKeyTypeAnnotation = (MapKeyType)property.getAnnotation(MapKeyType.class);
               if (mapKeyTypeAnnotation != null && !BinderHelper.isEmptyAnnotationValue(mapKeyTypeAnnotation.value().type())) {
                  elementBinder.setExplicitType(mapKeyTypeAnnotation.value());
               } else {
                  elementBinder.setType(property, elementClass, this.collection.getOwnerEntityName());
               }

               elementBinder.setPersistentClassName(this.propertyHolder.getEntityName());
               elementBinder.setAccessType(accessType);
               mapValue.setIndex(elementBinder.make());
            }
         }

         if (!this.collection.isOneToMany()) {
            for(Ejb3JoinColumn col : mapKeyManyToManyColumns) {
               col.forceNotNull();
            }
         }

         if (isIndexOfEntities) {
            bindManytoManyInverseFk(collectionEntity, mapKeyManyToManyColumns, element, false, mappings);
         }
      }

   }

   protected Value createFormulatedValue(Value value, Collection collection, String targetPropertyName, PersistentClass associatedClass, Mappings mappings) {
      Value element = collection.getElement();
      String fromAndWhere = null;
      if (!(element instanceof OneToMany)) {
         String referencedPropertyName = null;
         if (element instanceof ToOne) {
            referencedPropertyName = ((ToOne)element).getReferencedPropertyName();
         } else if (element instanceof DependantValue) {
            if (this.propertyName == null) {
               throw new AnnotationException("SecondaryTable JoinColumn cannot reference a non primary key");
            }

            referencedPropertyName = collection.getReferencedPropertyName();
         }

         Iterator referencedEntityColumns;
         if (referencedPropertyName == null) {
            referencedEntityColumns = associatedClass.getIdentifier().getColumnIterator();
         } else {
            Property referencedProperty = associatedClass.getRecursiveProperty(referencedPropertyName);
            referencedEntityColumns = referencedProperty.getColumnIterator();
         }

         String alias = "$alias$";
         StringBuilder fromAndWhereSb = (new StringBuilder(" from ")).append(associatedClass.getTable().getName()).append(" ").append(alias).append(" where ");
         Iterator collectionTableColumns = element.getColumnIterator();

         while(collectionTableColumns.hasNext()) {
            Column colColumn = (Column)collectionTableColumns.next();
            Column refColumn = (Column)referencedEntityColumns.next();
            fromAndWhereSb.append(alias).append('.').append(refColumn.getQuotedName()).append('=').append(colColumn.getQuotedName()).append(" and ");
         }

         fromAndWhere = fromAndWhereSb.substring(0, fromAndWhereSb.length() - 5);
      }

      if (!(value instanceof Component)) {
         if (value instanceof SimpleValue) {
            SimpleValue sourceValue = (SimpleValue)value;
            SimpleValue targetValue;
            if (value instanceof ManyToOne) {
               ManyToOne sourceManyToOne = (ManyToOne)sourceValue;
               ManyToOne targetManyToOne = new ManyToOne(mappings, collection.getCollectionTable());
               targetManyToOne.setFetchMode(FetchMode.DEFAULT);
               targetManyToOne.setLazy(true);
               targetManyToOne.setReferencedEntityName(sourceManyToOne.getReferencedEntityName());
               targetValue = targetManyToOne;
            } else {
               targetValue = new SimpleValue(mappings, collection.getCollectionTable());
               targetValue.setTypeName(sourceValue.getTypeName());
               targetValue.setTypeParameters(sourceValue.getTypeParameters());
            }

            Iterator columns = sourceValue.getColumnIterator();
            Random random = new Random();

            while(columns.hasNext()) {
               Object current = columns.next();
               Formula formula = new Formula();
               String formulaString;
               if (current instanceof Column) {
                  formulaString = ((Column)current).getQuotedName();
               } else {
                  if (!(current instanceof Formula)) {
                     throw new AssertionFailure("Unknown element in column iterator: " + current.getClass());
                  }

                  formulaString = ((Formula)current).getFormula();
               }

               if (fromAndWhere != null) {
                  formulaString = Template.renderWhereStringTemplate(formulaString, (String)"$alias$", (Dialect)(new HSQLDialect()));
                  formulaString = "(select " + formulaString + fromAndWhere + ")";
                  formulaString = StringHelper.replace(formulaString, "$alias$", "a" + random.nextInt(16));
               }

               formula.setFormula(formulaString);
               targetValue.addFormula(formula);
            }

            return targetValue;
         } else {
            throw new AssertionFailure("Unknown type encounters for map key: " + value.getClass());
         }
      } else {
         Component component = (Component)value;
         Iterator properties = component.getPropertyIterator();
         Component indexComponent = new Component(mappings, collection);
         indexComponent.setComponentClassName(component.getComponentClassName());
         indexComponent.setNodeName("index");

         while(properties.hasNext()) {
            Property current = (Property)properties.next();
            Property newProperty = new Property();
            newProperty.setCascade(current.getCascade());
            newProperty.setGeneration(current.getGeneration());
            newProperty.setInsertable(false);
            newProperty.setUpdateable(false);
            newProperty.setMetaAttributes(current.getMetaAttributes());
            newProperty.setName(current.getName());
            newProperty.setNodeName(current.getNodeName());
            newProperty.setNaturalIdentifier(false);
            newProperty.setOptional(false);
            newProperty.setPersistentClass(current.getPersistentClass());
            newProperty.setPropertyAccessorName(current.getPropertyAccessorName());
            newProperty.setSelectable(current.isSelectable());
            newProperty.setValue(this.createFormulatedValue(current.getValue(), collection, targetPropertyName, associatedClass, mappings));
            indexComponent.addProperty(newProperty);
         }

         return indexComponent;
      }
   }
}
