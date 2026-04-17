package org.hibernate.sql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import org.hibernate.internal.util.StringHelper;

public class SelectFragment {
   private String suffix;
   private List columns = new ArrayList();
   private List columnAliases = new ArrayList();
   private String extraSelectList;
   private String[] usedAliases;

   public List getColumns() {
      return this.columns;
   }

   public String getExtraSelectList() {
      return this.extraSelectList;
   }

   public SelectFragment setUsedAliases(String[] aliases) {
      this.usedAliases = aliases;
      return this;
   }

   public SelectFragment setExtraSelectList(String extraSelectList) {
      this.extraSelectList = extraSelectList;
      return this;
   }

   public SelectFragment setExtraSelectList(CaseFragment caseFragment, String fragmentAlias) {
      this.setExtraSelectList(caseFragment.setReturnColumnName(fragmentAlias, this.suffix).toFragmentString());
      return this;
   }

   public SelectFragment setSuffix(String suffix) {
      this.suffix = suffix;
      return this;
   }

   public SelectFragment addColumn(String columnName) {
      this.addColumn((String)null, columnName);
      return this;
   }

   public SelectFragment addColumns(String[] columnNames) {
      for(int i = 0; i < columnNames.length; ++i) {
         this.addColumn(columnNames[i]);
      }

      return this;
   }

   public SelectFragment addColumn(String tableAlias, String columnName) {
      return this.addColumn(tableAlias, columnName, columnName);
   }

   public SelectFragment addColumn(String tableAlias, String columnName, String columnAlias) {
      this.columns.add(StringHelper.qualify(tableAlias, columnName));
      this.columnAliases.add(columnAlias);
      return this;
   }

   public SelectFragment addColumns(String tableAlias, String[] columnNames) {
      for(int i = 0; i < columnNames.length; ++i) {
         this.addColumn(tableAlias, columnNames[i]);
      }

      return this;
   }

   public SelectFragment addColumns(String tableAlias, String[] columnNames, String[] columnAliases) {
      for(int i = 0; i < columnNames.length; ++i) {
         if (columnNames[i] != null) {
            this.addColumn(tableAlias, columnNames[i], columnAliases[i]);
         }
      }

      return this;
   }

   public SelectFragment addFormulas(String tableAlias, String[] formulas, String[] formulaAliases) {
      for(int i = 0; i < formulas.length; ++i) {
         if (formulas[i] != null) {
            this.addFormula(tableAlias, formulas[i], formulaAliases[i]);
         }
      }

      return this;
   }

   public SelectFragment addFormula(String tableAlias, String formula, String formulaAlias) {
      this.columns.add(StringHelper.replace(formula, "$PlaceHolder$", tableAlias));
      this.columnAliases.add(formulaAlias);
      return this;
   }

   public SelectFragment addColumnTemplate(String tableAlias, String columnTemplate, String columnAlias) {
      return this.addFormula(tableAlias, columnTemplate, columnAlias);
   }

   public SelectFragment addColumnTemplates(String tableAlias, String[] columnTemplates, String[] columnAliases) {
      return this.addFormulas(tableAlias, columnTemplates, columnAliases);
   }

   public String toFragmentString() {
      StringBuilder buf = new StringBuilder(this.columns.size() * 10);
      Iterator iter = this.columns.iterator();
      Iterator columnAliasIter = this.columnAliases.iterator();
      HashSet columnsUnique = new HashSet();
      if (this.usedAliases != null) {
         columnsUnique.addAll(Arrays.asList(this.usedAliases));
      }

      while(iter.hasNext()) {
         String column = (String)iter.next();
         String columnAlias = (String)columnAliasIter.next();
         if (columnsUnique.add(columnAlias)) {
            buf.append(", ").append(column).append(" as ");
            if (this.suffix == null) {
               buf.append(columnAlias);
            } else {
               buf.append((new Alias(this.suffix)).toAliasString(columnAlias));
            }
         }
      }

      if (this.extraSelectList != null) {
         buf.append(", ").append(this.extraSelectList);
      }

      return buf.toString();
   }
}
