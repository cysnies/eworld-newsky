package org.hibernate.hql.internal.ast.tree;

import antlr.RecognitionException;
import antlr.SemanticException;
import antlr.collections.AST;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import org.hibernate.QueryException;
import org.hibernate.engine.internal.JoinSequence;
import org.hibernate.engine.spi.QueryParameters;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.hql.internal.ast.SqlGenerator;
import org.hibernate.hql.internal.ast.util.SessionFactoryHelper;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.param.ParameterSpecification;
import org.hibernate.persister.collection.QueryableCollection;
import org.hibernate.type.CollectionType;
import org.hibernate.type.Type;
import org.jboss.logging.Logger;

public class IndexNode extends FromReferenceNode {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, IndexNode.class.getName());

   public void setScalarColumnText(int i) throws SemanticException {
      throw new UnsupportedOperationException("An IndexNode cannot generate column text!");
   }

   public void prepareForDot(String propertyName) throws SemanticException {
      FromElement fromElement = this.getFromElement();
      if (fromElement == null) {
         throw new IllegalStateException("No FROM element for index operator!");
      } else {
         QueryableCollection queryableCollection = fromElement.getQueryableCollection();
         if (queryableCollection != null && !queryableCollection.isOneToMany()) {
            FromReferenceNode collectionNode = (FromReferenceNode)this.getFirstChild();
            String path = collectionNode.getPath() + "[]." + propertyName;
            LOG.debugf("Creating join for many-to-many elements for %s", path);
            FromElementFactory factory = new FromElementFactory(fromElement.getFromClause(), fromElement, path);
            FromElement elementJoin = factory.createElementJoin(queryableCollection);
            this.setFromElement(elementJoin);
         }

      }
   }

   public void resolveIndex(AST parent) throws SemanticException {
      throw new UnsupportedOperationException();
   }

   public void resolve(boolean generateJoin, boolean implicitJoin, String classAlias, AST parent) throws SemanticException {
      if (!this.isResolved()) {
         FromReferenceNode collectionNode = (FromReferenceNode)this.getFirstChild();
         SessionFactoryHelper sessionFactoryHelper = this.getSessionFactoryHelper();
         collectionNode.resolveIndex(this);
         Type type = collectionNode.getDataType();
         if (!type.isCollectionType()) {
            throw new SemanticException("The [] operator cannot be applied to type " + type.toString());
         } else {
            String collectionRole = ((CollectionType)type).getRole();
            QueryableCollection queryableCollection = sessionFactoryHelper.requireQueryableCollection(collectionRole);
            if (!queryableCollection.hasIndex()) {
               throw new QueryException("unindexed fromElement before []: " + collectionNode.getPath());
            } else {
               FromElement fromElement = collectionNode.getFromElement();
               String elementTable = fromElement.getTableAlias();
               FromClause fromClause = fromElement.getFromClause();
               String path = collectionNode.getPath();
               FromElement elem = fromClause.findCollectionJoin(path);
               if (elem == null) {
                  FromElementFactory factory = new FromElementFactory(fromClause, fromElement, path);
                  elem = factory.createCollectionElementsJoin(queryableCollection, elementTable);
                  LOG.debugf("No FROM element found for the elements of collection join path %s, created %s", path, elem);
               } else {
                  LOG.debugf("FROM element found for collection join path %s", path);
               }

               this.setFromElement(fromElement);
               AST selector = collectionNode.getNextSibling();
               if (selector == null) {
                  throw new QueryException("No index value!");
               } else {
                  String collectionTableAlias = elementTable;
                  if (elem.getCollectionTableAlias() != null) {
                     collectionTableAlias = elem.getCollectionTableAlias();
                  }

                  JoinSequence joinSequence = fromElement.getJoinSequence();
                  String[] indexCols = queryableCollection.getIndexColumnNames();
                  if (indexCols.length != 1) {
                     throw new QueryException("composite-index appears in []: " + collectionNode.getPath());
                  } else {
                     SqlGenerator gen = new SqlGenerator(this.getSessionFactoryHelper().getFactory());

                     try {
                        gen.simpleExpr(selector);
                     } catch (RecognitionException e) {
                        throw new QueryException(e.getMessage(), e);
                     }

                     String selectorExpression = gen.getSQL();
                     joinSequence.addCondition(collectionTableAlias + '.' + indexCols[0] + " = " + selectorExpression);
                     List paramSpecs = gen.getCollectedParameters();
                     if (paramSpecs != null) {
                        switch (paramSpecs.size()) {
                           case 0:
                              break;
                           case 1:
                              ParameterSpecification paramSpec = (ParameterSpecification)paramSpecs.get(0);
                              paramSpec.setExpectedType(queryableCollection.getIndexType());
                              fromElement.setIndexCollectionSelectorParamSpec(paramSpec);
                              break;
                           default:
                              fromElement.setIndexCollectionSelectorParamSpec(new AggregatedIndexCollectionSelectorParameterSpecifications(paramSpecs));
                        }
                     }

                     String[] elementColumns = queryableCollection.getElementColumnNames(elementTable);
                     this.setText(elementColumns[0]);
                     this.setResolved();
                  }
               }
            }
         }
      }
   }

   private static class AggregatedIndexCollectionSelectorParameterSpecifications implements ParameterSpecification {
      private final List paramSpecs;

      public AggregatedIndexCollectionSelectorParameterSpecifications(List paramSpecs) {
         this.paramSpecs = paramSpecs;
      }

      public int bind(PreparedStatement statement, QueryParameters qp, SessionImplementor session, int position) throws SQLException {
         int bindCount = 0;

         for(ParameterSpecification paramSpec : this.paramSpecs) {
            bindCount += paramSpec.bind(statement, qp, session, position + bindCount);
         }

         return bindCount;
      }

      public Type getExpectedType() {
         return null;
      }

      public void setExpectedType(Type expectedType) {
      }

      public String renderDisplayInfo() {
         return "index-selector [" + this.collectDisplayInfo() + "]";
      }

      private String collectDisplayInfo() {
         StringBuilder buffer = new StringBuilder();
         Iterator itr = this.paramSpecs.iterator();

         while(itr.hasNext()) {
            buffer.append(((ParameterSpecification)itr.next()).renderDisplayInfo());
         }

         return buffer.toString();
      }
   }
}
