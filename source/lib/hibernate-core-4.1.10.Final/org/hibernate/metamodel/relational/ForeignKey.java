package org.hibernate.metamodel.relational;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.hibernate.AssertionFailure;
import org.hibernate.MappingException;
import org.hibernate.dialect.Dialect;
import org.jboss.logging.Logger;

public class ForeignKey extends AbstractConstraint implements Constraint, Exportable {
   private static final Logger LOG = Logger.getLogger(ForeignKey.class);
   private static final String ON_DELETE = " on delete ";
   private static final String ON_UPDATE = " on update ";
   private final TableSpecification targetTable;
   private List targetColumns;
   private ReferentialAction deleteRule;
   private ReferentialAction updateRule;

   protected ForeignKey(TableSpecification sourceTable, TableSpecification targetTable, String name) {
      super(sourceTable, name);
      this.deleteRule = ForeignKey.ReferentialAction.NO_ACTION;
      this.updateRule = ForeignKey.ReferentialAction.NO_ACTION;
      this.targetTable = targetTable;
   }

   protected ForeignKey(TableSpecification sourceTable, TableSpecification targetTable) {
      this(sourceTable, targetTable, (String)null);
   }

   public TableSpecification getSourceTable() {
      return this.getTable();
   }

   public TableSpecification getTargetTable() {
      return this.targetTable;
   }

   public Iterable getSourceColumns() {
      return this.getColumns();
   }

   public Iterable getTargetColumns() {
      return (Iterable)(this.targetColumns == null ? this.getTargetTable().getPrimaryKey().getColumns() : this.targetColumns);
   }

   public void addColumn(Column column) {
      this.addColumnMapping(column, (Column)null);
   }

   public void addColumnMapping(Column sourceColumn, Column targetColumn) {
      if (targetColumn == null) {
         if (this.targetColumns != null) {
            LOG.debugf("Attempt to map column [%s] to no target column after explicit target column(s) named for FK [name=%s]", sourceColumn.toLoggableString(), this.getName());
         }
      } else {
         this.checkTargetTable(targetColumn);
         if (this.targetColumns == null) {
            if (!this.internalColumnAccess().isEmpty()) {
               LOG.debugf("Value mapping mismatch as part of FK [table=%s, name=%s] while adding source column [%s]", this.getTable().toLoggableString(), this.getName(), sourceColumn.toLoggableString());
            }

            this.targetColumns = new ArrayList();
         }

         this.targetColumns.add(targetColumn);
      }

      this.internalAddColumn(sourceColumn);
   }

   private void checkTargetTable(Column targetColumn) {
      if (targetColumn.getTable() != this.getTargetTable()) {
         throw new AssertionFailure(String.format("Unable to add column to constraint; tables [%s, %s] did not match", targetColumn.getTable().toLoggableString(), this.getTargetTable().toLoggableString()));
      }
   }

   public String getExportIdentifier() {
      return this.getSourceTable().getLoggableValueQualifier() + ".FK-" + this.getName();
   }

   public ReferentialAction getDeleteRule() {
      return this.deleteRule;
   }

   public void setDeleteRule(ReferentialAction deleteRule) {
      this.deleteRule = deleteRule;
   }

   public ReferentialAction getUpdateRule() {
      return this.updateRule;
   }

   public void setUpdateRule(ReferentialAction updateRule) {
      this.updateRule = updateRule;
   }

   public String[] sqlDropStrings(Dialect dialect) {
      return new String[]{"alter table " + this.getTable().getQualifiedName(dialect) + dialect.getDropForeignKeyString() + this.getName()};
   }

   public String sqlConstraintStringInAlterTable(Dialect dialect) {
      String[] columnNames = new String[this.getColumnSpan()];
      String[] targetColumnNames = new String[this.getColumnSpan()];
      int i = 0;
      Iterator<Column> itTargetColumn = this.getTargetColumns().iterator();

      for(Column column : this.getColumns()) {
         if (!itTargetColumn.hasNext()) {
            throw new MappingException("More constraint columns that foreign key target columns.");
         }

         columnNames[i] = column.getColumnName().encloseInQuotesIfQuoted(dialect);
         targetColumnNames[i] = ((Column)itTargetColumn.next()).getColumnName().encloseInQuotesIfQuoted(dialect);
         ++i;
      }

      if (itTargetColumn.hasNext()) {
         throw new MappingException("More foreign key target columns than constraint columns.");
      } else {
         StringBuilder sb = new StringBuilder(dialect.getAddForeignKeyConstraintString(this.getName(), columnNames, this.targetTable.getQualifiedName(dialect), targetColumnNames, this.targetColumns == null));
         if (dialect.supportsCascadeDelete()) {
            if (this.deleteRule != ForeignKey.ReferentialAction.NO_ACTION) {
               sb.append(" on delete ").append(this.deleteRule.getActionString());
            }

            if (this.updateRule != ForeignKey.ReferentialAction.NO_ACTION) {
               sb.append(" on update ").append(this.updateRule.getActionString());
            }
         }

         return sb.toString();
      }
   }

   public static enum ReferentialAction {
      NO_ACTION("no action"),
      CASCADE("cascade"),
      SET_NULL("set null"),
      SET_DEFAULT("set default"),
      RESTRICT("restrict");

      private final String actionString;

      private ReferentialAction(String actionString) {
         this.actionString = actionString;
      }

      public String getActionString() {
         return this.actionString;
      }
   }
}
