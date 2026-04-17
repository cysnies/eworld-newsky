package org.hibernate.metamodel.source.annotations.global;

import org.hibernate.AnnotationException;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.metamodel.relational.Column;
import org.hibernate.metamodel.relational.Index;
import org.hibernate.metamodel.relational.ObjectName;
import org.hibernate.metamodel.relational.Schema;
import org.hibernate.metamodel.relational.SimpleValue;
import org.hibernate.metamodel.relational.Table;
import org.hibernate.metamodel.source.MetadataImplementor;
import org.hibernate.metamodel.source.annotations.AnnotationBindingContext;
import org.hibernate.metamodel.source.annotations.HibernateDotNames;
import org.hibernate.metamodel.source.annotations.JandexHelper;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.logging.Logger;

public class TableBinder {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, TableBinder.class.getName());

   private TableBinder() {
   }

   public static void bind(AnnotationBindingContext bindingContext) {
      for(AnnotationInstance tableAnnotation : bindingContext.getIndex().getAnnotations(HibernateDotNames.TABLE)) {
         bind(bindingContext.getMetadataImplementor(), tableAnnotation);
      }

      for(AnnotationInstance tables : bindingContext.getIndex().getAnnotations(HibernateDotNames.TABLES)) {
         for(AnnotationInstance table : (AnnotationInstance[])JandexHelper.getValue(tables, "value", AnnotationInstance[].class)) {
            bind(bindingContext.getMetadataImplementor(), table);
         }
      }

   }

   private static void bind(MetadataImplementor metadata, AnnotationInstance tableAnnotation) {
      String tableName = (String)JandexHelper.getValue(tableAnnotation, "appliesTo", String.class);
      ObjectName objectName = new ObjectName(tableName);
      Schema schema = metadata.getDatabase().getSchema(objectName.getSchema(), objectName.getCatalog());
      Table table = schema.locateTable(objectName.getName());
      if (table != null) {
         bindHibernateTableAnnotation(table, tableAnnotation);
      }

   }

   private static void bindHibernateTableAnnotation(Table table, AnnotationInstance tableAnnotation) {
      for(AnnotationInstance indexAnnotation : (AnnotationInstance[])JandexHelper.getValue(tableAnnotation, "indexes", AnnotationInstance[].class)) {
         bindIndexAnnotation(table, indexAnnotation);
      }

      String comment = (String)JandexHelper.getValue(tableAnnotation, "comment", String.class);
      if (StringHelper.isNotEmpty(comment)) {
         table.addComment(comment.trim());
      }

   }

   private static void bindIndexAnnotation(Table table, AnnotationInstance indexAnnotation) {
      String indexName = (String)JandexHelper.getValue(indexAnnotation, "appliesTo", String.class);
      String[] columnNames = (String[])JandexHelper.getValue(indexAnnotation, "columnNames", String[].class);
      if (columnNames == null) {
         LOG.noColumnsSpecifiedForIndex(indexName, table.toLoggableString());
      } else {
         Index index = table.getOrCreateIndex(indexName);

         for(String columnName : columnNames) {
            Column column = findColumn(table, columnName);
            if (column == null) {
               throw new AnnotationException("@Index references a unknown column: " + columnName);
            }

            index.addColumn(column);
         }

      }
   }

   private static Column findColumn(Table table, String columnName) {
      Column column = null;

      for(SimpleValue value : table.values()) {
         if (value instanceof Column && ((Column)value).getColumnName().getName().equals(columnName)) {
            column = (Column)value;
            break;
         }
      }

      return column;
   }
}
