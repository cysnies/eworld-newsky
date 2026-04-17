package org.hibernate.hql.internal.ast.tree;

import antlr.SemanticException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.hql.internal.NameGenerator;
import org.hibernate.persister.collection.QueryableCollection;
import org.hibernate.persister.entity.Queryable;
import org.hibernate.sql.AliasGenerator;
import org.hibernate.sql.SelectFragment;
import org.hibernate.transform.BasicTransformerAdapter;
import org.hibernate.transform.ResultTransformer;
import org.hibernate.type.EntityType;
import org.hibernate.type.Type;

public class MapEntryNode extends AbstractMapComponentNode implements AggregatedSelectExpression {
   private int scalarColumnIndex = -1;
   private List types = new ArrayList(4);
   private static final String[] ALIASES = new String[]{null, null};
   private MapEntryBuilder mapEntryBuilder;

   protected String expressionDescription() {
      return "entry(*)";
   }

   public Class getAggregationResultType() {
      return Map.Entry.class;
   }

   protected Type resolveType(QueryableCollection collectionPersister) {
      Type keyType = collectionPersister.getIndexType();
      Type valueType = collectionPersister.getElementType();
      this.types.add(keyType);
      this.types.add(valueType);
      this.mapEntryBuilder = new MapEntryBuilder();
      return null;
   }

   protected String[] resolveColumns(QueryableCollection collectionPersister) {
      List selections = new ArrayList();
      this.determineKeySelectExpressions(collectionPersister, selections);
      this.determineValueSelectExpressions(collectionPersister, selections);
      String text = "";
      String[] columns = new String[selections.size()];

      for(int i = 0; i < selections.size(); ++i) {
         org.hibernate.sql.SelectExpression selectExpression = (org.hibernate.sql.SelectExpression)selections.get(i);
         text = text + ", " + selectExpression.getExpression() + " as " + selectExpression.getAlias();
         columns[i] = selectExpression.getExpression();
      }

      text = text.substring(2);
      this.setText(text);
      this.setResolved();
      return columns;
   }

   private void determineKeySelectExpressions(QueryableCollection collectionPersister, List selections) {
      AliasGenerator aliasGenerator = new LocalAliasGenerator(0);
      this.appendSelectExpressions(collectionPersister.getIndexColumnNames(), selections, aliasGenerator);
      Type keyType = collectionPersister.getIndexType();
      if (keyType.isAssociationType()) {
         EntityType entityType = (EntityType)keyType;
         Queryable keyEntityPersister = (Queryable)this.sfi().getEntityPersister(entityType.getAssociatedEntityName(this.sfi()));
         SelectFragment fragment = keyEntityPersister.propertySelectFragmentFragment(this.collectionTableAlias(), (String)null, false);
         this.appendSelectExpressions(fragment, selections, aliasGenerator);
      }

   }

   private void appendSelectExpressions(String[] columnNames, List selections, AliasGenerator aliasGenerator) {
      for(int i = 0; i < columnNames.length; ++i) {
         selections.add(new BasicSelectExpression(this.collectionTableAlias() + '.' + columnNames[i], aliasGenerator.generateAlias(columnNames[i])));
      }

   }

   private void appendSelectExpressions(SelectFragment fragment, List selections, AliasGenerator aliasGenerator) {
      for(String column : fragment.getColumns()) {
         selections.add(new BasicSelectExpression(column, aliasGenerator.generateAlias(column)));
      }

   }

   private void determineValueSelectExpressions(QueryableCollection collectionPersister, List selections) {
      AliasGenerator aliasGenerator = new LocalAliasGenerator(1);
      this.appendSelectExpressions(collectionPersister.getElementColumnNames(), selections, aliasGenerator);
      Type valueType = collectionPersister.getElementType();
      if (valueType.isAssociationType()) {
         EntityType valueEntityType = (EntityType)valueType;
         Queryable valueEntityPersister = (Queryable)this.sfi().getEntityPersister(valueEntityType.getAssociatedEntityName(this.sfi()));
         SelectFragment fragment = valueEntityPersister.propertySelectFragmentFragment(this.elementTableAlias(), (String)null, false);
         this.appendSelectExpressions(fragment, selections, aliasGenerator);
      }

   }

   private String collectionTableAlias() {
      return this.getFromElement().getCollectionTableAlias() != null ? this.getFromElement().getCollectionTableAlias() : this.getFromElement().getTableAlias();
   }

   private String elementTableAlias() {
      return this.getFromElement().getTableAlias();
   }

   public SessionFactoryImplementor sfi() {
      return this.getSessionFactoryHelper().getFactory();
   }

   public void setText(String s) {
      if (!this.isResolved()) {
         super.setText(s);
      }
   }

   public void setScalarColumn(int i) throws SemanticException {
      this.scalarColumnIndex = i;
   }

   public int getScalarColumnIndex() {
      return this.scalarColumnIndex;
   }

   public void setScalarColumnText(int i) throws SemanticException {
   }

   public boolean isScalar() {
      return true;
   }

   public List getAggregatedSelectionTypeList() {
      return this.types;
   }

   public String[] getAggregatedAliases() {
      return ALIASES;
   }

   public ResultTransformer getResultTransformer() {
      return this.mapEntryBuilder;
   }

   private static class LocalAliasGenerator implements AliasGenerator {
      private final int base;
      private int counter;

      private LocalAliasGenerator(int base) {
         this.counter = 0;
         this.base = base;
      }

      public String generateAlias(String sqlExpression) {
         return NameGenerator.scalarName(this.base, this.counter++);
      }
   }

   private static class BasicSelectExpression implements org.hibernate.sql.SelectExpression {
      private final String expression;
      private final String alias;

      private BasicSelectExpression(String expression, String alias) {
         this.expression = expression;
         this.alias = alias;
      }

      public String getExpression() {
         return this.expression;
      }

      public String getAlias() {
         return this.alias;
      }
   }

   private static class MapEntryBuilder extends BasicTransformerAdapter {
      private MapEntryBuilder() {
      }

      public Object transformTuple(Object[] tuple, String[] aliases) {
         if (tuple.length != 2) {
            throw new HibernateException("Expecting exactly 2 tuples to transform into Map.Entry");
         } else {
            return new EntryAdapter(tuple[0], tuple[1]);
         }
      }
   }

   private static class EntryAdapter implements Map.Entry {
      private final Object key;
      private Object value;

      private EntryAdapter(Object key, Object value) {
         this.key = key;
         this.value = value;
      }

      public Object getValue() {
         return this.value;
      }

      public Object getKey() {
         return this.key;
      }

      public Object setValue(Object value) {
         Object old = this.value;
         this.value = value;
         return old;
      }

      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            boolean var10000;
            label44: {
               label30: {
                  EntryAdapter that = (EntryAdapter)o;
                  if (this.key == null) {
                     if (that.key != null) {
                        break label30;
                     }
                  } else if (!this.key.equals(that.key)) {
                     break label30;
                  }

                  if (this.value == null) {
                     if (that.value == null) {
                        break label44;
                     }
                  } else if (this.value.equals(that.value)) {
                     break label44;
                  }
               }

               var10000 = false;
               return var10000;
            }

            var10000 = true;
            return var10000;
         } else {
            return false;
         }
      }

      public int hashCode() {
         int keyHash = this.key == null ? 0 : this.key.hashCode();
         int valueHash = this.value == null ? 0 : this.value.hashCode();
         return keyHash ^ valueHash;
      }
   }
}
