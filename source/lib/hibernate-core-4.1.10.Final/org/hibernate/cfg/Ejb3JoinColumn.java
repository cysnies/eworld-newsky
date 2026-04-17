package org.hibernate.cfg;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.persistence.JoinColumn;
import javax.persistence.PrimaryKeyJoinColumn;
import org.hibernate.AnnotationException;
import org.hibernate.AssertionFailure;
import org.hibernate.MappingException;
import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;
import org.hibernate.annotations.common.reflection.XClass;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Join;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.SimpleValue;
import org.hibernate.mapping.Table;
import org.hibernate.mapping.Value;

public class Ejb3JoinColumn extends Ejb3Column {
   private String referencedColumn;
   private String mappedBy;
   private String mappedByPropertyName;
   private String mappedByTableName;
   private String mappedByEntityName;
   private boolean JPA2ElementCollection;
   private String manyToManyOwnerSideEntityName;
   public static final int NO_REFERENCE = 0;
   public static final int PK_REFERENCE = 1;
   public static final int NON_PK_REFERENCE = 2;

   public void setJPA2ElementCollection(boolean JPA2ElementCollection) {
      this.JPA2ElementCollection = JPA2ElementCollection;
   }

   public String getManyToManyOwnerSideEntityName() {
      return this.manyToManyOwnerSideEntityName;
   }

   public void setManyToManyOwnerSideEntityName(String manyToManyOwnerSideEntityName) {
      this.manyToManyOwnerSideEntityName = manyToManyOwnerSideEntityName;
   }

   public void setReferencedColumn(String referencedColumn) {
      this.referencedColumn = referencedColumn;
   }

   public String getMappedBy() {
      return this.mappedBy;
   }

   public void setMappedBy(String mappedBy) {
      this.mappedBy = mappedBy;
   }

   private Ejb3JoinColumn() {
      this.setMappedBy("");
   }

   private Ejb3JoinColumn(String sqlType, String name, boolean nullable, boolean unique, boolean insertable, boolean updatable, String referencedColumn, String secondaryTable, Map joins, PropertyHolder propertyHolder, String propertyName, String mappedBy, boolean isImplicit, Mappings mappings) {
      this.setImplicit(isImplicit);
      this.setSqlType(sqlType);
      this.setLogicalColumnName(name);
      this.setNullable(nullable);
      this.setUnique(unique);
      this.setInsertable(insertable);
      this.setUpdatable(updatable);
      this.setSecondaryTableName(secondaryTable);
      this.setPropertyHolder(propertyHolder);
      this.setJoins(joins);
      this.setMappings(mappings);
      this.setPropertyName(BinderHelper.getRelativePath(propertyHolder, propertyName));
      this.bind();
      this.referencedColumn = referencedColumn;
      this.mappedBy = mappedBy;
   }

   public String getReferencedColumn() {
      return this.referencedColumn;
   }

   public static Ejb3JoinColumn[] buildJoinColumnsOrFormulas(JoinColumnsOrFormulas anns, String mappedBy, Map joins, PropertyHolder propertyHolder, String propertyName, Mappings mappings) {
      JoinColumnOrFormula[] ann = anns.value();
      Ejb3JoinColumn[] joinColumns = new Ejb3JoinColumn[ann.length];

      for(int i = 0; i < ann.length; ++i) {
         JoinColumnOrFormula join = ann[i];
         JoinFormula formula = join.formula();
         if (formula.value() != null && !formula.value().equals("")) {
            joinColumns[i] = buildJoinFormula(formula, mappedBy, joins, propertyHolder, propertyName, mappings);
         } else {
            joinColumns[i] = buildJoinColumns(new JoinColumn[]{join.column()}, mappedBy, joins, propertyHolder, propertyName, mappings)[0];
         }
      }

      return joinColumns;
   }

   public static Ejb3JoinColumn buildJoinFormula(JoinFormula ann, String mappedBy, Map joins, PropertyHolder propertyHolder, String propertyName, Mappings mappings) {
      Ejb3JoinColumn formulaColumn = new Ejb3JoinColumn();
      formulaColumn.setFormula(ann.value());
      formulaColumn.setReferencedColumn(ann.referencedColumnName());
      formulaColumn.setMappings(mappings);
      formulaColumn.setPropertyHolder(propertyHolder);
      formulaColumn.setJoins(joins);
      formulaColumn.setPropertyName(BinderHelper.getRelativePath(propertyHolder, propertyName));
      formulaColumn.bind();
      return formulaColumn;
   }

   public static Ejb3JoinColumn[] buildJoinColumns(JoinColumn[] anns, String mappedBy, Map joins, PropertyHolder propertyHolder, String propertyName, Mappings mappings) {
      return buildJoinColumnsWithDefaultColumnSuffix(anns, mappedBy, joins, propertyHolder, propertyName, "", mappings);
   }

   public static Ejb3JoinColumn[] buildJoinColumnsWithDefaultColumnSuffix(JoinColumn[] anns, String mappedBy, Map joins, PropertyHolder propertyHolder, String propertyName, String suffixForDefaultColumnName, Mappings mappings) {
      JoinColumn[] actualColumns = propertyHolder.getOverriddenJoinColumn(StringHelper.qualify(propertyHolder.getPath(), propertyName));
      if (actualColumns == null) {
         actualColumns = anns;
      }

      if (actualColumns != null && actualColumns.length != 0) {
         int size = actualColumns.length;
         Ejb3JoinColumn[] result = new Ejb3JoinColumn[size];

         for(int index = 0; index < size; ++index) {
            result[index] = buildJoinColumn(actualColumns[index], mappedBy, joins, propertyHolder, propertyName, suffixForDefaultColumnName, mappings);
         }

         return result;
      } else {
         return new Ejb3JoinColumn[]{buildJoinColumn((JoinColumn)null, mappedBy, joins, propertyHolder, propertyName, suffixForDefaultColumnName, mappings)};
      }
   }

   private static Ejb3JoinColumn buildJoinColumn(JoinColumn ann, String mappedBy, Map joins, PropertyHolder propertyHolder, String propertyName, String suffixForDefaultColumnName, Mappings mappings) {
      if (ann != null) {
         if (BinderHelper.isEmptyAnnotationValue(mappedBy)) {
            throw new AnnotationException("Illegal attempt to define a @JoinColumn with a mappedBy association: " + BinderHelper.getRelativePath(propertyHolder, propertyName));
         } else {
            Ejb3JoinColumn joinColumn = new Ejb3JoinColumn();
            joinColumn.setJoinAnnotation(ann, (String)null);
            if (StringHelper.isEmpty(joinColumn.getLogicalColumnName()) && !StringHelper.isEmpty(suffixForDefaultColumnName)) {
               joinColumn.setLogicalColumnName(propertyName + suffixForDefaultColumnName);
            }

            joinColumn.setJoins(joins);
            joinColumn.setPropertyHolder(propertyHolder);
            joinColumn.setPropertyName(BinderHelper.getRelativePath(propertyHolder, propertyName));
            joinColumn.setImplicit(false);
            joinColumn.setMappings(mappings);
            joinColumn.bind();
            return joinColumn;
         }
      } else {
         Ejb3JoinColumn joinColumn = new Ejb3JoinColumn();
         joinColumn.setMappedBy(mappedBy);
         joinColumn.setJoins(joins);
         joinColumn.setPropertyHolder(propertyHolder);
         joinColumn.setPropertyName(BinderHelper.getRelativePath(propertyHolder, propertyName));
         if (!StringHelper.isEmpty(suffixForDefaultColumnName)) {
            joinColumn.setLogicalColumnName(propertyName + suffixForDefaultColumnName);
            joinColumn.setImplicit(false);
         } else {
            joinColumn.setImplicit(true);
         }

         joinColumn.setMappings(mappings);
         joinColumn.bind();
         return joinColumn;
      }
   }

   public void setJoinAnnotation(JoinColumn annJoin, String defaultName) {
      if (annJoin == null) {
         this.setImplicit(true);
      } else {
         this.setImplicit(false);
         if (!BinderHelper.isEmptyAnnotationValue(annJoin.columnDefinition())) {
            this.setSqlType(annJoin.columnDefinition());
         }

         if (!BinderHelper.isEmptyAnnotationValue(annJoin.name())) {
            this.setLogicalColumnName(annJoin.name());
         }

         this.setNullable(annJoin.nullable());
         this.setUnique(annJoin.unique());
         this.setInsertable(annJoin.insertable());
         this.setUpdatable(annJoin.updatable());
         this.setReferencedColumn(annJoin.referencedColumnName());
         this.setSecondaryTableName(annJoin.table());
      }

   }

   public static Ejb3JoinColumn buildJoinColumn(PrimaryKeyJoinColumn pkJoinAnn, JoinColumn joinAnn, Value identifier, Map joins, PropertyHolder propertyHolder, Mappings mappings) {
      Column col = (Column)identifier.getColumnIterator().next();
      String defaultName = mappings.getLogicalColumnName(col.getQuotedName(), identifier.getTable());
      if (pkJoinAnn == null && joinAnn == null) {
         defaultName = mappings.getObjectNameNormalizer().normalizeIdentifierQuoting(defaultName);
         return new Ejb3JoinColumn((String)null, defaultName, false, false, true, true, (String)null, (String)null, joins, propertyHolder, (String)null, (String)null, true, mappings);
      } else {
         String colName;
         String columnDefinition;
         String referencedColumnName;
         if (pkJoinAnn != null) {
            colName = pkJoinAnn.name();
            columnDefinition = pkJoinAnn.columnDefinition();
            referencedColumnName = pkJoinAnn.referencedColumnName();
         } else {
            colName = joinAnn.name();
            columnDefinition = joinAnn.columnDefinition();
            referencedColumnName = joinAnn.referencedColumnName();
         }

         String sqlType = "".equals(columnDefinition) ? null : mappings.getObjectNameNormalizer().normalizeIdentifierQuoting(columnDefinition);
         String name = "".equals(colName) ? defaultName : colName;
         name = mappings.getObjectNameNormalizer().normalizeIdentifierQuoting(name);
         return new Ejb3JoinColumn(sqlType, name, false, false, true, true, referencedColumnName, (String)null, joins, propertyHolder, (String)null, (String)null, false, mappings);
      }
   }

   public void setPersistentClass(PersistentClass persistentClass, Map joins, Map inheritanceStatePerClass) {
      this.propertyHolder = PropertyHolderBuilder.buildPropertyHolder(persistentClass, joins, this.getMappings(), inheritanceStatePerClass);
   }

   public static void checkIfJoinColumn(Object columns, PropertyHolder holder, PropertyData property) {
      if (!(columns instanceof Ejb3JoinColumn[])) {
         throw new AnnotationException("@Column cannot be used on an association property: " + holder.getEntityName() + "." + property.getPropertyName());
      }
   }

   public void copyReferencedStructureAndCreateDefaultJoinColumns(PersistentClass referencedEntity, Iterator columnIterator, SimpleValue value) {
      if (!this.isNameDeferred()) {
         throw new AssertionFailure("Building implicit column but the column is not implicit");
      } else {
         while(columnIterator.hasNext()) {
            Column synthCol = (Column)columnIterator.next();
            this.linkValueUsingDefaultColumnNaming(synthCol, referencedEntity, value);
         }

         this.setMappingColumn((Column)null);
      }
   }

   public void linkValueUsingDefaultColumnNaming(Column referencedColumn, PersistentClass referencedEntity, SimpleValue value) {
      String logicalReferencedColumn = this.getMappings().getLogicalColumnName(referencedColumn.getQuotedName(), referencedEntity.getTable());
      String columnName = this.buildDefaultColumnName(referencedEntity, logicalReferencedColumn);
      this.setLogicalColumnName(columnName);
      this.setReferencedColumn(logicalReferencedColumn);
      this.initMappingColumn(columnName, (String)null, referencedColumn.getLength(), referencedColumn.getPrecision(), referencedColumn.getScale(), this.getMappingColumn() != null ? this.getMappingColumn().isNullable() : false, referencedColumn.getSqlType(), this.getMappingColumn() != null ? this.getMappingColumn().isUnique() : false, false);
      this.linkWithValue(value);
   }

   public void addDefaultJoinColumnName(PersistentClass referencedEntity, String logicalReferencedColumn) {
      String columnName = this.buildDefaultColumnName(referencedEntity, logicalReferencedColumn);
      this.getMappingColumn().setName(columnName);
      this.setLogicalColumnName(columnName);
   }

   private String buildDefaultColumnName(PersistentClass referencedEntity, String logicalReferencedColumn) {
      boolean mappedBySide = this.mappedByTableName != null || this.mappedByPropertyName != null;
      boolean ownerSide = this.getPropertyName() != null;
      Boolean isRefColumnQuoted = StringHelper.isQuoted(logicalReferencedColumn);
      String unquotedLogicalReferenceColumn = isRefColumnQuoted ? StringHelper.unquote(logicalReferencedColumn) : logicalReferencedColumn;
      String columnName;
      if (mappedBySide) {
         String unquotedMappedbyTable = StringHelper.unquote(this.mappedByTableName);
         String ownerObjectName = this.JPA2ElementCollection && this.mappedByEntityName != null ? StringHelper.unqualify(this.mappedByEntityName) : unquotedMappedbyTable;
         columnName = this.getMappings().getNamingStrategy().foreignKeyColumnName(this.mappedByPropertyName, this.mappedByEntityName, ownerObjectName, unquotedLogicalReferenceColumn);
         if (isRefColumnQuoted || StringHelper.isQuoted(this.mappedByTableName)) {
            columnName = StringHelper.quote(columnName);
         }
      } else if (ownerSide) {
         String logicalTableName = this.getMappings().getLogicalTableName(referencedEntity.getTable());
         String unquotedLogicalTableName = StringHelper.unquote(logicalTableName);
         columnName = this.getMappings().getNamingStrategy().foreignKeyColumnName(this.getPropertyName(), referencedEntity.getEntityName(), unquotedLogicalTableName, unquotedLogicalReferenceColumn);
         if (isRefColumnQuoted || StringHelper.isQuoted(logicalTableName)) {
            columnName = StringHelper.quote(columnName);
         }
      } else {
         String logicalTableName = this.getMappings().getLogicalTableName(referencedEntity.getTable());
         String unquotedLogicalTableName = StringHelper.unquote(logicalTableName);
         columnName = this.getMappings().getNamingStrategy().joinKeyColumnName(unquotedLogicalReferenceColumn, unquotedLogicalTableName);
         if (isRefColumnQuoted || StringHelper.isQuoted(logicalTableName)) {
            columnName = StringHelper.quote(columnName);
         }
      }

      return columnName;
   }

   public void linkValueUsingAColumnCopy(Column column, SimpleValue value) {
      this.initMappingColumn(column.getQuotedName(), (String)null, column.getLength(), column.getPrecision(), column.getScale(), this.getMappingColumn().isNullable(), column.getSqlType(), this.getMappingColumn().isUnique(), false);
      this.linkWithValue(value);
   }

   protected void addColumnBinding(SimpleValue value) {
      if (StringHelper.isEmpty(this.mappedBy)) {
         String unquotedLogColName = StringHelper.unquote(this.getLogicalColumnName());
         String unquotedRefColumn = StringHelper.unquote(this.getReferencedColumn());
         String logicalColumnName = this.getMappings().getNamingStrategy().logicalCollectionColumnName(unquotedLogColName, this.getPropertyName(), unquotedRefColumn);
         if (StringHelper.isQuoted(this.getLogicalColumnName()) || StringHelper.isQuoted(this.getLogicalColumnName())) {
            logicalColumnName = StringHelper.quote(logicalColumnName);
         }

         this.getMappings().addColumnBinding(logicalColumnName, this.getMappingColumn(), value.getTable());
      }

   }

   public static int checkReferencedColumnsType(Ejb3JoinColumn[] columns, PersistentClass referencedEntity, Mappings mappings) {
      Set<Column> idColumns = new HashSet();
      Iterator idColumnsIt = referencedEntity.getKey().getColumnIterator();

      while(idColumnsIt.hasNext()) {
         idColumns.add((Column)idColumnsIt.next());
      }

      boolean isFkReferencedColumnName = false;
      boolean noReferencedColumn = true;
      if (columns.length == 0) {
         return 0;
      } else {
         Object columnOwner = BinderHelper.findColumnOwner(referencedEntity, columns[0].getReferencedColumn(), mappings);
         if (columnOwner == null) {
            try {
               throw new MappingException("Unable to find column with logical name: " + columns[0].getReferencedColumn() + " in " + referencedEntity.getTable() + " and its related " + "supertables and secondary tables");
            } catch (MappingException e) {
               throw new RecoverableException(e);
            }
         } else {
            Table matchingTable = columnOwner instanceof PersistentClass ? ((PersistentClass)columnOwner).getTable() : ((Join)columnOwner).getTable();

            for(Ejb3JoinColumn ejb3Column : columns) {
               String logicalReferencedColumnName = ejb3Column.getReferencedColumn();
               if (StringHelper.isNotEmpty(logicalReferencedColumnName)) {
                  String referencedColumnName;
                  try {
                     referencedColumnName = mappings.getPhysicalColumnName(logicalReferencedColumnName, matchingTable);
                  } catch (MappingException var18) {
                     throw new MappingException("Unable to find column with logical name: " + logicalReferencedColumnName + " in " + matchingTable.getName());
                  }

                  noReferencedColumn = false;
                  Column refCol = new Column(referencedColumnName);
                  boolean contains = idColumns.contains(refCol);
                  if (!contains) {
                     isFkReferencedColumnName = true;
                     break;
                  }
               }
            }

            if (isFkReferencedColumnName) {
               return 2;
            } else if (noReferencedColumn) {
               return 0;
            } else {
               return idColumns.size() != columns.length ? 2 : 1;
            }
         }
      }
   }

   public void overrideFromReferencedColumnIfNecessary(Column column) {
      if (this.getMappingColumn() != null) {
         if (StringHelper.isEmpty(this.sqlType)) {
            this.sqlType = column.getSqlType();
            this.getMappingColumn().setSqlType(this.sqlType);
         }

         this.getMappingColumn().setLength(column.getLength());
         this.getMappingColumn().setPrecision(column.getPrecision());
         this.getMappingColumn().setScale(column.getScale());
      }

   }

   public void redefineColumnName(String columnName, String propertyName, boolean applyNamingStrategy) {
      if (StringHelper.isNotEmpty(columnName)) {
         this.getMappingColumn().setName(applyNamingStrategy ? this.getMappings().getNamingStrategy().columnName(columnName) : columnName);
      }

   }

   public static Ejb3JoinColumn[] buildJoinTableJoinColumns(JoinColumn[] annJoins, Map secondaryTables, PropertyHolder propertyHolder, String propertyName, String mappedBy, Mappings mappings) {
      Ejb3JoinColumn[] joinColumns;
      if (annJoins == null) {
         Ejb3JoinColumn currentJoinColumn = new Ejb3JoinColumn();
         currentJoinColumn.setImplicit(true);
         currentJoinColumn.setNullable(false);
         currentJoinColumn.setPropertyHolder(propertyHolder);
         currentJoinColumn.setJoins(secondaryTables);
         currentJoinColumn.setMappings(mappings);
         currentJoinColumn.setPropertyName(BinderHelper.getRelativePath(propertyHolder, propertyName));
         currentJoinColumn.setMappedBy(mappedBy);
         currentJoinColumn.bind();
         joinColumns = new Ejb3JoinColumn[]{currentJoinColumn};
      } else {
         joinColumns = new Ejb3JoinColumn[annJoins.length];
         int length = annJoins.length;

         for(int index = 0; index < length; ++index) {
            JoinColumn annJoin = annJoins[index];
            Ejb3JoinColumn currentJoinColumn = new Ejb3JoinColumn();
            currentJoinColumn.setImplicit(true);
            currentJoinColumn.setPropertyHolder(propertyHolder);
            currentJoinColumn.setJoins(secondaryTables);
            currentJoinColumn.setMappings(mappings);
            currentJoinColumn.setPropertyName(BinderHelper.getRelativePath(propertyHolder, propertyName));
            currentJoinColumn.setMappedBy(mappedBy);
            currentJoinColumn.setJoinAnnotation(annJoin, propertyName);
            currentJoinColumn.setNullable(false);
            currentJoinColumn.bind();
            joinColumns[index] = currentJoinColumn;
         }
      }

      return joinColumns;
   }

   public void setMappedBy(String entityName, String logicalTableName, String mappedByProperty) {
      this.mappedByEntityName = entityName;
      this.mappedByTableName = logicalTableName;
      this.mappedByPropertyName = mappedByProperty;
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("Ejb3JoinColumn");
      sb.append("{logicalColumnName='").append(this.getLogicalColumnName()).append('\'');
      sb.append(", referencedColumn='").append(this.referencedColumn).append('\'');
      sb.append(", mappedBy='").append(this.mappedBy).append('\'');
      sb.append('}');
      return sb.toString();
   }
}
