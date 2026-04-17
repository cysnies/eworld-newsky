package org.hibernate.cfg.annotations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.persistence.UniqueConstraint;
import org.hibernate.AnnotationException;
import org.hibernate.AssertionFailure;
import org.hibernate.annotations.Index;
import org.hibernate.cfg.BinderHelper;
import org.hibernate.cfg.Ejb3JoinColumn;
import org.hibernate.cfg.IndexOrUniqueKeySecondPass;
import org.hibernate.cfg.Mappings;
import org.hibernate.cfg.NamingStrategy;
import org.hibernate.cfg.ObjectNameNormalizer;
import org.hibernate.cfg.ObjectNameSource;
import org.hibernate.cfg.UniqueConstraintHolder;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.internal.util.collections.CollectionHelper;
import org.hibernate.mapping.Collection;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.DependantValue;
import org.hibernate.mapping.JoinedSubclass;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.SimpleValue;
import org.hibernate.mapping.Table;
import org.hibernate.mapping.ToOne;
import org.hibernate.mapping.Value;
import org.jboss.logging.Logger;

public class TableBinder {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, TableBinder.class.getName());
   private String schema;
   private String catalog;
   private String name;
   private boolean isAbstract;
   private List uniqueConstraints;
   String constraints;
   Table denormalizedSuperTable;
   Mappings mappings;
   private String ownerEntityTable;
   private String associatedEntityTable;
   private String propertyName;
   private String ownerEntity;
   private String associatedEntity;
   private boolean isJPA2ElementCollection;

   public void setSchema(String schema) {
      this.schema = schema;
   }

   public void setCatalog(String catalog) {
      this.catalog = catalog;
   }

   public String getName() {
      return this.name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public void setAbstract(boolean anAbstract) {
      this.isAbstract = anAbstract;
   }

   public void setUniqueConstraints(UniqueConstraint[] uniqueConstraints) {
      this.uniqueConstraints = buildUniqueConstraintHolders(uniqueConstraints);
   }

   public void setConstraints(String constraints) {
      this.constraints = constraints;
   }

   public void setDenormalizedSuperTable(Table denormalizedSuperTable) {
      this.denormalizedSuperTable = denormalizedSuperTable;
   }

   public void setMappings(Mappings mappings) {
      this.mappings = mappings;
   }

   public void setJPA2ElementCollection(boolean isJPA2ElementCollection) {
      this.isJPA2ElementCollection = isJPA2ElementCollection;
   }

   public Table bind() {
      String unquotedOwnerTable = StringHelper.unquote(this.ownerEntityTable);
      final String unquotedAssocTable = StringHelper.unquote(this.associatedEntityTable);
      final String ownerObjectName = this.isJPA2ElementCollection && this.ownerEntity != null ? StringHelper.unqualify(this.ownerEntity) : unquotedOwnerTable;
      ObjectNameSource nameSource = this.buildNameContext(ownerObjectName, unquotedAssocTable);
      final boolean ownerEntityTableQuoted = StringHelper.isQuoted(this.ownerEntityTable);
      final boolean associatedEntityTableQuoted = StringHelper.isQuoted(this.associatedEntityTable);
      ObjectNameNormalizer.NamingStrategyHelper namingStrategyHelper = new ObjectNameNormalizer.NamingStrategyHelper() {
         public String determineImplicitName(NamingStrategy strategy) {
            String strategyResult = strategy.collectionTableName(TableBinder.this.ownerEntity, ownerObjectName, TableBinder.this.associatedEntity, unquotedAssocTable, TableBinder.this.propertyName);
            return !ownerEntityTableQuoted && !associatedEntityTableQuoted ? strategyResult : StringHelper.quote(strategyResult);
         }

         public String handleExplicitName(NamingStrategy strategy, String name) {
            return strategy.tableName(name);
         }
      };
      return buildAndFillTable(this.schema, this.catalog, nameSource, namingStrategyHelper, this.isAbstract, this.uniqueConstraints, this.constraints, this.denormalizedSuperTable, this.mappings, (String)null);
   }

   private ObjectNameSource buildNameContext(String unquotedOwnerTable, String unquotedAssocTable) {
      String logicalName = this.mappings.getNamingStrategy().logicalCollectionTableName(this.name, unquotedOwnerTable, unquotedAssocTable, this.propertyName);
      if (StringHelper.isQuoted(this.ownerEntityTable) || StringHelper.isQuoted(this.associatedEntityTable)) {
         logicalName = StringHelper.quote(logicalName);
      }

      return new AssociationTableNameSource(this.name, logicalName);
   }

   public static Table buildAndFillTable(String schema, String catalog, ObjectNameSource nameSource, ObjectNameNormalizer.NamingStrategyHelper namingStrategyHelper, boolean isAbstract, List uniqueConstraints, String constraints, Table denormalizedSuperTable, Mappings mappings, String subselect) {
      schema = BinderHelper.isEmptyAnnotationValue(schema) ? mappings.getSchemaName() : schema;
      catalog = BinderHelper.isEmptyAnnotationValue(catalog) ? mappings.getCatalogName() : catalog;
      String realTableName = mappings.getObjectNameNormalizer().normalizeDatabaseIdentifier(nameSource.getExplicitName(), namingStrategyHelper);
      Table table;
      if (denormalizedSuperTable != null) {
         table = mappings.addDenormalizedTable(schema, catalog, realTableName, isAbstract, subselect, denormalizedSuperTable);
      } else {
         table = mappings.addTable(schema, catalog, realTableName, subselect, isAbstract);
      }

      if (uniqueConstraints != null && uniqueConstraints.size() > 0) {
         mappings.addUniqueConstraintHolders(table, uniqueConstraints);
      }

      if (constraints != null) {
         table.addCheckConstraint(constraints);
      }

      String logicalName = nameSource.getLogicalName();
      if (logicalName != null) {
         mappings.addTableBinding(schema, catalog, logicalName, realTableName, denormalizedSuperTable);
      }

      return table;
   }

   /** @deprecated */
   @Deprecated
   public static Table fillTable(String schema, String catalog, String realTableName, String logicalName, boolean isAbstract, List uniqueConstraints, String constraints, Table denormalizedSuperTable, Mappings mappings) {
      schema = BinderHelper.isEmptyAnnotationValue(schema) ? mappings.getSchemaName() : schema;
      catalog = BinderHelper.isEmptyAnnotationValue(catalog) ? mappings.getCatalogName() : catalog;
      Table table;
      if (denormalizedSuperTable != null) {
         table = mappings.addDenormalizedTable(schema, catalog, realTableName, isAbstract, (String)null, denormalizedSuperTable);
      } else {
         table = mappings.addTable(schema, catalog, realTableName, (String)null, isAbstract);
      }

      if (uniqueConstraints != null && uniqueConstraints.size() > 0) {
         mappings.addUniqueConstraints(table, uniqueConstraints);
      }

      if (constraints != null) {
         table.addCheckConstraint(constraints);
      }

      if (logicalName != null) {
         mappings.addTableBinding(schema, catalog, logicalName, realTableName, denormalizedSuperTable);
      }

      return table;
   }

   public static void bindFk(PersistentClass referencedEntity, PersistentClass destinationEntity, Ejb3JoinColumn[] columns, SimpleValue value, boolean unique, Mappings mappings) {
      PersistentClass associatedClass;
      if (destinationEntity != null) {
         associatedClass = destinationEntity;
      } else {
         associatedClass = columns[0].getPropertyHolder() == null ? null : columns[0].getPropertyHolder().getPersistentClass();
      }

      String mappedByProperty = columns[0].getMappedBy();
      if (StringHelper.isNotEmpty(mappedByProperty)) {
         LOG.debugf("Retrieving property %s.%s", associatedClass.getEntityName(), mappedByProperty);
         Property property = associatedClass.getRecursiveProperty(columns[0].getMappedBy());
         Iterator mappedByColumns;
         if (property.getValue() instanceof Collection) {
            Collection collection = (Collection)property.getValue();
            Value element = collection.getElement();
            if (element == null) {
               throw new AnnotationException("Illegal use of mappedBy on both sides of the relationship: " + associatedClass.getEntityName() + "." + mappedByProperty);
            }

            mappedByColumns = element.getColumnIterator();
         } else {
            mappedByColumns = property.getValue().getColumnIterator();
         }

         while(mappedByColumns.hasNext()) {
            Column column = (Column)mappedByColumns.next();
            columns[0].overrideFromReferencedColumnIfNecessary(column);
            columns[0].linkValueUsingAColumnCopy(column, value);
         }
      } else if (columns[0].isImplicit()) {
         Iterator idColumns;
         if (referencedEntity instanceof JoinedSubclass) {
            idColumns = referencedEntity.getKey().getColumnIterator();
         } else {
            idColumns = referencedEntity.getIdentifier().getColumnIterator();
         }

         while(idColumns.hasNext()) {
            Column column = (Column)idColumns.next();
            columns[0].overrideFromReferencedColumnIfNecessary(column);
            columns[0].linkValueUsingDefaultColumnNaming(column, referencedEntity, value);
         }
      } else {
         int fkEnum = Ejb3JoinColumn.checkReferencedColumnsType(columns, referencedEntity, mappings);
         if (2 == fkEnum) {
            String referencedPropertyName;
            if (value instanceof ToOne) {
               referencedPropertyName = ((ToOne)value).getReferencedPropertyName();
            } else {
               if (!(value instanceof DependantValue)) {
                  throw new AssertionFailure("Do a property ref on an unexpected Value type: " + value.getClass().getName());
               }

               String propertyName = columns[0].getPropertyName();
               if (propertyName == null) {
                  throw new AnnotationException("SecondaryTable JoinColumn cannot reference a non primary key");
               }

               Collection collection = (Collection)referencedEntity.getRecursiveProperty(propertyName).getValue();
               referencedPropertyName = collection.getReferencedPropertyName();
            }

            if (referencedPropertyName == null) {
               throw new AssertionFailure("No property ref found while expected");
            }

            Property synthProp = referencedEntity.getReferencedProperty(referencedPropertyName);
            if (synthProp == null) {
               throw new AssertionFailure("Cannot find synthProp: " + referencedEntity.getEntityName() + "." + referencedPropertyName);
            }

            linkJoinColumnWithValueOverridingNameIfImplicit(referencedEntity, synthProp.getColumnIterator(), columns, value);
         } else if (0 == fkEnum) {
            if (columns.length != referencedEntity.getIdentifier().getColumnSpan()) {
               throw new AnnotationException("A Foreign key refering " + referencedEntity.getEntityName() + " from " + associatedClass.getEntityName() + " has the wrong number of column. should be " + referencedEntity.getIdentifier().getColumnSpan());
            }

            linkJoinColumnWithValueOverridingNameIfImplicit(referencedEntity, referencedEntity.getIdentifier().getColumnIterator(), columns, value);
         } else {
            Iterator idColItr = referencedEntity.getKey().getColumnIterator();
            Table table = referencedEntity.getTable();
            if (!idColItr.hasNext()) {
               LOG.debug("No column in the identifier!");
            }

            while(idColItr.hasNext()) {
               boolean match = false;
               Column col = (Column)idColItr.next();

               for(Ejb3JoinColumn joinCol : columns) {
                  String referencedColumn = joinCol.getReferencedColumn();
                  referencedColumn = mappings.getPhysicalColumnName(referencedColumn, table);
                  if (referencedColumn.equalsIgnoreCase(col.getQuotedName())) {
                     if (joinCol.isNameDeferred()) {
                        joinCol.linkValueUsingDefaultColumnNaming(col, referencedEntity, value);
                     } else {
                        joinCol.linkWithValue(value);
                     }

                     joinCol.overrideFromReferencedColumnIfNecessary(col);
                     match = true;
                     break;
                  }
               }

               if (!match) {
                  throw new AnnotationException("Column name " + col.getName() + " of " + referencedEntity.getEntityName() + " not found in JoinColumns.referencedColumnName");
               }
            }
         }
      }

      value.createForeignKey();
      if (unique) {
         createUniqueConstraint(value);
      }

   }

   public static void linkJoinColumnWithValueOverridingNameIfImplicit(PersistentClass referencedEntity, Iterator columnIterator, Ejb3JoinColumn[] columns, SimpleValue value) {
      for(Ejb3JoinColumn joinCol : columns) {
         Column synthCol = (Column)columnIterator.next();
         if (joinCol.isNameDeferred()) {
            joinCol.linkValueUsingDefaultColumnNaming(synthCol, referencedEntity, value);
         } else {
            joinCol.linkWithValue(value);
            joinCol.overrideFromReferencedColumnIfNecessary(synthCol);
         }
      }

   }

   public static void createUniqueConstraint(Value value) {
      Iterator iter = value.getColumnIterator();
      ArrayList cols = new ArrayList();

      while(iter.hasNext()) {
         cols.add(iter.next());
      }

      value.getTable().createUniqueKey(cols);
   }

   public static void addIndexes(Table hibTable, Index[] indexes, Mappings mappings) {
      for(Index index : indexes) {
         mappings.addSecondPass(new IndexOrUniqueKeySecondPass(hibTable, index.name(), index.columnNames(), mappings));
      }

   }

   /** @deprecated */
   @Deprecated
   public static List buildUniqueConstraints(UniqueConstraint[] constraintsArray) {
      List<String[]> result = new ArrayList();
      if (constraintsArray.length != 0) {
         for(UniqueConstraint uc : constraintsArray) {
            result.add(uc.columnNames());
         }
      }

      return result;
   }

   public static List buildUniqueConstraintHolders(UniqueConstraint[] annotations) {
      List<UniqueConstraintHolder> result;
      if (annotations != null && annotations.length != 0) {
         result = new ArrayList(CollectionHelper.determineProperSizing(annotations.length));

         for(UniqueConstraint uc : annotations) {
            result.add((new UniqueConstraintHolder()).setName(uc.name()).setColumns(uc.columnNames()));
         }
      } else {
         result = Collections.emptyList();
      }

      return result;
   }

   public void setDefaultName(String ownerEntity, String ownerEntityTable, String associatedEntity, String associatedEntityTable, String propertyName) {
      this.ownerEntity = ownerEntity;
      this.ownerEntityTable = ownerEntityTable;
      this.associatedEntity = associatedEntity;
      this.associatedEntityTable = associatedEntityTable;
      this.propertyName = propertyName;
      this.name = null;
   }

   private static class AssociationTableNameSource implements ObjectNameSource {
      private final String explicitName;
      private final String logicalName;

      private AssociationTableNameSource(String explicitName, String logicalName) {
         this.explicitName = explicitName;
         this.logicalName = logicalName;
      }

      public String getExplicitName() {
         return this.explicitName;
      }

      public String getLogicalName() {
         return this.logicalName;
      }
   }
}
