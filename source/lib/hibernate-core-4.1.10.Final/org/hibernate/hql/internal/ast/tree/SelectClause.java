package org.hibernate.hql.internal.ast.tree;

import antlr.SemanticException;
import antlr.collections.AST;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.hibernate.QueryException;
import org.hibernate.hql.internal.antlr.SqlTokenTypes;
import org.hibernate.hql.internal.ast.util.ASTAppender;
import org.hibernate.hql.internal.ast.util.ASTIterator;
import org.hibernate.hql.internal.ast.util.ASTPrinter;
import org.hibernate.type.Type;

public class SelectClause extends SelectExpressionList {
   private boolean prepared = false;
   private boolean scalarSelect;
   private List fromElementsForLoad = new ArrayList();
   private Type[] queryReturnTypes;
   private String[][] columnNames;
   private List collectionFromElements;
   private String[] aliases;
   private int[] columnNamesStartPositions;
   private AggregatedSelectExpression aggregatedSelectExpression;
   public static boolean VERSION2_SQL = false;

   public boolean isScalarSelect() {
      return this.scalarSelect;
   }

   public boolean isDistinct() {
      return this.getFirstChild() != null && this.getFirstChild().getType() == 16;
   }

   public List getFromElementsForLoad() {
      return this.fromElementsForLoad;
   }

   public Type[] getQueryReturnTypes() {
      return this.queryReturnTypes;
   }

   public String[] getQueryReturnAliases() {
      return this.aliases;
   }

   public String[][] getColumnNames() {
      return this.columnNames;
   }

   public AggregatedSelectExpression getAggregatedSelectExpression() {
      return this.aggregatedSelectExpression;
   }

   public void initializeExplicitSelectClause(FromClause fromClause) throws SemanticException {
      if (this.prepared) {
         throw new IllegalStateException("SelectClause was already prepared!");
      } else {
         ArrayList queryReturnTypeList = new ArrayList();
         SelectExpression[] selectExpressions = this.collectSelectExpressions();

         for(int i = 0; i < selectExpressions.length; ++i) {
            SelectExpression selectExpression = selectExpressions[i];
            if (AggregatedSelectExpression.class.isInstance(selectExpression)) {
               this.aggregatedSelectExpression = (AggregatedSelectExpression)selectExpression;
               queryReturnTypeList.addAll(this.aggregatedSelectExpression.getAggregatedSelectionTypeList());
               this.scalarSelect = true;
            } else {
               Type type = selectExpression.getDataType();
               if (type == null) {
                  throw new IllegalStateException("No data type for node: " + selectExpression.getClass().getName() + " " + (new ASTPrinter(SqlTokenTypes.class)).showAsString((AST)selectExpression, ""));
               }

               if (selectExpression.isScalar()) {
                  this.scalarSelect = true;
               }

               if (this.isReturnableEntity(selectExpression)) {
                  this.fromElementsForLoad.add(selectExpression.getFromElement());
               }

               queryReturnTypeList.add(type);
            }
         }

         this.initAliases(selectExpressions);
         if (!this.getWalker().isShallowQuery()) {
            List fromElements = fromClause.getProjectionList();
            ASTAppender appender = new ASTAppender(this.getASTFactory(), this);
            int size = fromElements.size();
            Iterator iterator = fromElements.iterator();

            for(int k = 0; iterator.hasNext(); ++k) {
               FromElement fromElement = (FromElement)iterator.next();
               if (fromElement.isFetch()) {
                  FromElement origin = null;
                  if (fromElement.getRealOrigin() == null) {
                     if (fromElement.getOrigin() == null) {
                        throw new QueryException("Unable to determine origin of join fetch [" + fromElement.getDisplayText() + "]");
                     }

                     origin = fromElement.getOrigin();
                  } else {
                     origin = fromElement.getRealOrigin();
                  }

                  if (!this.fromElementsForLoad.contains(origin)) {
                     throw new QueryException("query specified join fetching, but the owner of the fetched association was not present in the select list [" + fromElement.getDisplayText() + "]");
                  }

                  Type type = fromElement.getSelectType();
                  this.addCollectionFromElement(fromElement);
                  if (type != null) {
                     boolean collectionOfElements = fromElement.isCollectionOfValuesOrComponents();
                     if (!collectionOfElements) {
                        fromElement.setIncludeSubclasses(true);
                        this.fromElementsForLoad.add(fromElement);
                        String text = fromElement.renderIdentifierSelect(size, k);
                        SelectExpressionImpl generatedExpr = (SelectExpressionImpl)appender.append(144, text, false);
                        if (generatedExpr != null) {
                           generatedExpr.setFromElement(fromElement);
                        }
                     }
                  }
               }
            }

            this.renderNonScalarSelects(this.collectSelectExpressions(), fromClause);
         }

         if (this.scalarSelect || this.getWalker().isShallowQuery()) {
            this.renderScalarSelects(selectExpressions, fromClause);
         }

         this.finishInitialization(queryReturnTypeList);
      }
   }

   private void finishInitialization(ArrayList queryReturnTypeList) {
      this.queryReturnTypes = (Type[])queryReturnTypeList.toArray(new Type[queryReturnTypeList.size()]);
      this.initializeColumnNames();
      this.prepared = true;
   }

   private void initializeColumnNames() {
      this.columnNames = this.getSessionFactoryHelper().generateColumnNames(this.queryReturnTypes);
      this.columnNamesStartPositions = new int[this.columnNames.length];
      int startPosition = 1;

      for(int i = 0; i < this.columnNames.length; ++i) {
         this.columnNamesStartPositions[i] = startPosition;
         startPosition += this.columnNames[i].length;
      }

   }

   public int getColumnNamesStartPosition(int i) {
      return this.columnNamesStartPositions[i];
   }

   public void initializeDerivedSelectClause(FromClause fromClause) throws SemanticException {
      if (this.prepared) {
         throw new IllegalStateException("SelectClause was already prepared!");
      } else {
         List fromElements = fromClause.getProjectionList();
         ASTAppender appender = new ASTAppender(this.getASTFactory(), this);
         int size = fromElements.size();
         ArrayList queryReturnTypeList = new ArrayList(size);
         Iterator iterator = fromElements.iterator();

         for(int k = 0; iterator.hasNext(); ++k) {
            FromElement fromElement = (FromElement)iterator.next();
            Type type = fromElement.getSelectType();
            this.addCollectionFromElement(fromElement);
            if (type != null) {
               boolean collectionOfElements = fromElement.isCollectionOfValuesOrComponents();
               if (!collectionOfElements) {
                  if (!fromElement.isFetch()) {
                     queryReturnTypeList.add(type);
                  }

                  this.fromElementsForLoad.add(fromElement);
                  String text = fromElement.renderIdentifierSelect(size, k);
                  SelectExpressionImpl generatedExpr = (SelectExpressionImpl)appender.append(144, text, false);
                  if (generatedExpr != null) {
                     generatedExpr.setFromElement(fromElement);
                  }
               }
            }
         }

         SelectExpression[] selectExpressions = this.collectSelectExpressions();
         if (this.getWalker().isShallowQuery()) {
            this.renderScalarSelects(selectExpressions, fromClause);
         } else {
            this.renderNonScalarSelects(selectExpressions, fromClause);
         }

         this.finishInitialization(queryReturnTypeList);
      }
   }

   private void addCollectionFromElement(FromElement fromElement) {
      if (fromElement.isFetch() && (fromElement.isCollectionJoin() || fromElement.getQueryableCollection() != null)) {
         String suffix;
         if (this.collectionFromElements == null) {
            this.collectionFromElements = new ArrayList();
            suffix = VERSION2_SQL ? "__" : "0__";
         } else {
            suffix = Integer.toString(this.collectionFromElements.size()) + "__";
         }

         this.collectionFromElements.add(fromElement);
         fromElement.setCollectionSuffix(suffix);
      }

   }

   protected AST getFirstSelectExpression() {
      AST n;
      for(n = this.getFirstChild(); n != null && (n.getType() == 16 || n.getType() == 4); n = n.getNextSibling()) {
      }

      return n;
   }

   private boolean isReturnableEntity(SelectExpression selectExpression) throws SemanticException {
      FromElement fromElement = selectExpression.getFromElement();
      boolean isFetchOrValueCollection = fromElement != null && (fromElement.isFetch() || fromElement.isCollectionOfValuesOrComponents());
      return isFetchOrValueCollection ? false : selectExpression.isReturnableEntity();
   }

   private void renderScalarSelects(SelectExpression[] se, FromClause currentFromClause) throws SemanticException {
      if (!currentFromClause.isSubQuery()) {
         for(int i = 0; i < se.length; ++i) {
            SelectExpression expr = se[i];
            expr.setScalarColumn(i);
         }
      }

   }

   private void initAliases(SelectExpression[] selectExpressions) {
      if (this.aggregatedSelectExpression == null) {
         this.aliases = new String[selectExpressions.length];

         for(int i = 0; i < selectExpressions.length; ++i) {
            String alias = selectExpressions[i].getAlias();
            this.aliases[i] = alias == null ? Integer.toString(i) : alias;
         }
      } else {
         this.aliases = this.aggregatedSelectExpression.getAggregatedAliases();
      }

   }

   private void renderNonScalarSelects(SelectExpression[] selectExpressions, FromClause currentFromClause) throws SemanticException {
      ASTAppender appender = new ASTAppender(this.getASTFactory(), this);
      int size = selectExpressions.length;
      int nonscalarSize = 0;

      for(int i = 0; i < size; ++i) {
         if (!selectExpressions[i].isScalar()) {
            ++nonscalarSize;
         }
      }

      int j = 0;

      for(int i = 0; i < size; ++i) {
         if (!selectExpressions[i].isScalar()) {
            SelectExpression expr = selectExpressions[i];
            FromElement fromElement = expr.getFromElement();
            if (fromElement != null) {
               this.renderNonScalarIdentifiers(fromElement, nonscalarSize, j, expr, appender);
               ++j;
            }
         }
      }

      if (!currentFromClause.isSubQuery()) {
         int k = 0;

         for(int i = 0; i < size; ++i) {
            if (!selectExpressions[i].isScalar()) {
               FromElement fromElement = selectExpressions[i].getFromElement();
               if (fromElement != null) {
                  this.renderNonScalarProperties(appender, fromElement, nonscalarSize, k);
                  ++k;
               }
            }
         }
      }

   }

   private void renderNonScalarIdentifiers(FromElement fromElement, int nonscalarSize, int j, SelectExpression expr, ASTAppender appender) {
      String text = fromElement.renderIdentifierSelect(nonscalarSize, j);
      if (!fromElement.getFromClause().isSubQuery()) {
         if (!this.scalarSelect && !this.getWalker().isShallowQuery()) {
            expr.setText(text);
         } else {
            appender.append(142, text, false);
         }
      }

   }

   private void renderNonScalarProperties(ASTAppender appender, FromElement fromElement, int nonscalarSize, int k) {
      String text = fromElement.renderPropertySelect(nonscalarSize, k);
      appender.append(142, text, false);
      if (fromElement.getQueryableCollection() != null && fromElement.isFetch()) {
         text = fromElement.renderCollectionSelectFragment(nonscalarSize, k);
         appender.append(142, text, false);
      }

      ASTIterator iter = new ASTIterator(fromElement);

      while(iter.hasNext()) {
         FromElement child = (FromElement)iter.next();
         if (child.isCollectionOfValuesOrComponents() && child.isFetch()) {
            text = child.renderValueCollectionSelectFragment(nonscalarSize, nonscalarSize + k);
            appender.append(142, text, false);
         }
      }

   }

   public List getCollectionFromElements() {
      return this.collectionFromElements;
   }
}
