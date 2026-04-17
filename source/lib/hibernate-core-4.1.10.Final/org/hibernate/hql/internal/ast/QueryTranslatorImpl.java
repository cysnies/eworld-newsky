package org.hibernate.hql.internal.ast;

import antlr.ANTLRException;
import antlr.RecognitionException;
import antlr.TokenStreamException;
import antlr.collections.AST;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.QueryException;
import org.hibernate.ScrollableResults;
import org.hibernate.engine.spi.QueryParameters;
import org.hibernate.engine.spi.RowSelection;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.event.spi.EventSource;
import org.hibernate.hql.internal.QueryExecutionRequestException;
import org.hibernate.hql.internal.antlr.HqlTokenTypes;
import org.hibernate.hql.internal.antlr.SqlTokenTypes;
import org.hibernate.hql.internal.ast.exec.BasicExecutor;
import org.hibernate.hql.internal.ast.exec.MultiTableDeleteExecutor;
import org.hibernate.hql.internal.ast.exec.MultiTableUpdateExecutor;
import org.hibernate.hql.internal.ast.exec.StatementExecutor;
import org.hibernate.hql.internal.ast.tree.AggregatedSelectExpression;
import org.hibernate.hql.internal.ast.tree.FromElement;
import org.hibernate.hql.internal.ast.tree.InsertStatement;
import org.hibernate.hql.internal.ast.tree.QueryNode;
import org.hibernate.hql.internal.ast.tree.Statement;
import org.hibernate.hql.internal.ast.util.ASTPrinter;
import org.hibernate.hql.internal.ast.util.ASTUtil;
import org.hibernate.hql.internal.ast.util.NodeTraverser;
import org.hibernate.hql.spi.FilterTranslator;
import org.hibernate.hql.spi.ParameterTranslations;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.ReflectHelper;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.internal.util.collections.IdentitySet;
import org.hibernate.loader.hql.QueryLoader;
import org.hibernate.persister.entity.Queryable;
import org.hibernate.type.Type;
import org.jboss.logging.Logger;

public class QueryTranslatorImpl implements FilterTranslator {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, QueryTranslatorImpl.class.getName());
   private SessionFactoryImplementor factory;
   private final String queryIdentifier;
   private String hql;
   private boolean shallowQuery;
   private Map tokenReplacements;
   private Map enabledFilters;
   private boolean compiled;
   private QueryLoader queryLoader;
   private StatementExecutor statementExecutor;
   private Statement sqlAst;
   private String sql;
   private ParameterTranslations paramTranslations;
   private List collectedParameterSpecifications;

   public QueryTranslatorImpl(String queryIdentifier, String query, Map enabledFilters, SessionFactoryImplementor factory) {
      this.queryIdentifier = queryIdentifier;
      this.hql = query;
      this.compiled = false;
      this.shallowQuery = false;
      this.enabledFilters = enabledFilters;
      this.factory = factory;
   }

   public void compile(Map replacements, boolean shallow) throws QueryException, MappingException {
      this.doCompile(replacements, shallow, (String)null);
   }

   public void compile(String collectionRole, Map replacements, boolean shallow) throws QueryException, MappingException {
      this.doCompile(replacements, shallow, collectionRole);
   }

   private synchronized void doCompile(Map replacements, boolean shallow, String collectionRole) {
      if (this.compiled) {
         LOG.debug("compile() : The query is already compiled, skipping...");
      } else {
         this.tokenReplacements = replacements;
         if (this.tokenReplacements == null) {
            this.tokenReplacements = new HashMap();
         }

         this.shallowQuery = shallow;

         try {
            HqlParser parser = this.parse(true);
            HqlSqlWalker w = this.analyze(parser, collectionRole);
            this.sqlAst = (Statement)w.getAST();
            if (this.sqlAst.needsExecutor()) {
               this.statementExecutor = this.buildAppropriateStatementExecutor(w);
            } else {
               this.generate((QueryNode)this.sqlAst);
               this.queryLoader = new QueryLoader(this, this.factory, w.getSelectClause());
            }

            this.compiled = true;
         } catch (QueryException qe) {
            qe.setQueryString(this.hql);
            throw qe;
         } catch (RecognitionException e) {
            LOG.trace("Converted antlr.RecognitionException", e);
            throw QuerySyntaxException.convert(e, this.hql);
         } catch (ANTLRException e) {
            LOG.trace("Converted antlr.ANTLRException", e);
            throw new QueryException(e.getMessage(), this.hql);
         }

         this.enabledFilters = null;
      }
   }

   private void generate(AST sqlAst) throws QueryException, RecognitionException {
      if (this.sql == null) {
         SqlGenerator gen = new SqlGenerator(this.factory);
         gen.statement(sqlAst);
         this.sql = gen.getSQL();
         if (LOG.isDebugEnabled()) {
            LOG.debugf("HQL: %s", this.hql);
            LOG.debugf("SQL: %s", this.sql);
         }

         gen.getParseErrorHandler().throwQueryException();
         this.collectedParameterSpecifications = gen.getCollectedParameters();
      }

   }

   private HqlSqlWalker analyze(HqlParser parser, String collectionRole) throws QueryException, RecognitionException {
      HqlSqlWalker w = new HqlSqlWalker(this, this.factory, parser, this.tokenReplacements, collectionRole);
      AST hqlAst = parser.getAST();
      w.statement(hqlAst);
      if (LOG.isDebugEnabled()) {
         ASTPrinter printer = new ASTPrinter(SqlTokenTypes.class);
         LOG.debug(printer.showAsString(w.getAST(), "--- SQL AST ---"));
      }

      w.getParseErrorHandler().throwQueryException();
      return w;
   }

   private HqlParser parse(boolean filter) throws TokenStreamException, RecognitionException {
      HqlParser parser = HqlParser.getInstance(this.hql);
      parser.setFilter(filter);
      LOG.debugf("parse() - HQL: %s", this.hql);
      parser.statement();
      AST hqlAst = parser.getAST();
      JavaConstantConverter converter = new JavaConstantConverter();
      NodeTraverser walker = new NodeTraverser(converter);
      walker.traverseDepthFirst(hqlAst);
      this.showHqlAst(hqlAst);
      parser.getParseErrorHandler().throwQueryException();
      return parser;
   }

   void showHqlAst(AST hqlAst) {
      if (LOG.isDebugEnabled()) {
         ASTPrinter printer = new ASTPrinter(HqlTokenTypes.class);
         LOG.debug(printer.showAsString(hqlAst, "--- HQL AST ---"));
      }

   }

   private void errorIfDML() throws HibernateException {
      if (this.sqlAst.needsExecutor()) {
         throw new QueryExecutionRequestException("Not supported for DML operations", this.hql);
      }
   }

   private void errorIfSelect() throws HibernateException {
      if (!this.sqlAst.needsExecutor()) {
         throw new QueryExecutionRequestException("Not supported for select queries", this.hql);
      }
   }

   public String getQueryIdentifier() {
      return this.queryIdentifier;
   }

   public Statement getSqlAST() {
      return this.sqlAst;
   }

   private HqlSqlWalker getWalker() {
      return this.sqlAst.getWalker();
   }

   public Type[] getReturnTypes() {
      this.errorIfDML();
      return this.getWalker().getReturnTypes();
   }

   public String[] getReturnAliases() {
      this.errorIfDML();
      return this.getWalker().getReturnAliases();
   }

   public String[][] getColumnNames() {
      this.errorIfDML();
      return this.getWalker().getSelectClause().getColumnNames();
   }

   public Set getQuerySpaces() {
      return this.getWalker().getQuerySpaces();
   }

   public List list(SessionImplementor session, QueryParameters queryParameters) throws HibernateException {
      this.errorIfDML();
      QueryNode query = (QueryNode)this.sqlAst;
      boolean hasLimit = queryParameters.getRowSelection() != null && queryParameters.getRowSelection().definesLimits();
      boolean needsDistincting = (query.getSelectClause().isDistinct() || hasLimit) && this.containsCollectionFetches();
      QueryParameters queryParametersToUse;
      if (hasLimit && this.containsCollectionFetches()) {
         LOG.firstOrMaxResultsSpecifiedWithCollectionFetch();
         RowSelection selection = new RowSelection();
         selection.setFetchSize(queryParameters.getRowSelection().getFetchSize());
         selection.setTimeout(queryParameters.getRowSelection().getTimeout());
         queryParametersToUse = queryParameters.createCopyUsing(selection);
      } else {
         queryParametersToUse = queryParameters;
      }

      List results = this.queryLoader.list(session, queryParametersToUse);
      if (needsDistincting) {
         int includedCount = -1;
         int first = hasLimit && queryParameters.getRowSelection().getFirstRow() != null ? queryParameters.getRowSelection().getFirstRow() : 0;
         int max = hasLimit && queryParameters.getRowSelection().getMaxRows() != null ? queryParameters.getRowSelection().getMaxRows() : -1;
         int size = results.size();
         List tmp = new ArrayList();
         IdentitySet distinction = new IdentitySet();

         for(int i = 0; i < size; ++i) {
            Object result = results.get(i);
            if (distinction.add(result)) {
               ++includedCount;
               if (includedCount >= first) {
                  tmp.add(result);
                  if (max >= 0 && includedCount - first >= max - 1) {
                     break;
                  }
               }
            }
         }

         results = tmp;
      }

      return results;
   }

   public Iterator iterate(QueryParameters queryParameters, EventSource session) throws HibernateException {
      this.errorIfDML();
      return this.queryLoader.iterate(queryParameters, session);
   }

   public ScrollableResults scroll(QueryParameters queryParameters, SessionImplementor session) throws HibernateException {
      this.errorIfDML();
      return this.queryLoader.scroll(queryParameters, session);
   }

   public int executeUpdate(QueryParameters queryParameters, SessionImplementor session) throws HibernateException {
      this.errorIfSelect();
      return this.statementExecutor.execute(queryParameters, session);
   }

   public String getSQLString() {
      return this.sql;
   }

   public List collectSqlStrings() {
      ArrayList<String> list = new ArrayList();
      if (this.isManipulationStatement()) {
         String[] sqlStatements = this.statementExecutor.getSqlStatements();

         for(int i = 0; i < sqlStatements.length; ++i) {
            list.add(sqlStatements[i]);
         }
      } else {
         list.add(this.sql);
      }

      return list;
   }

   public boolean isShallowQuery() {
      return this.shallowQuery;
   }

   public String getQueryString() {
      return this.hql;
   }

   public Map getEnabledFilters() {
      return this.enabledFilters;
   }

   public int[] getNamedParameterLocs(String name) {
      return this.getWalker().getNamedParameterLocations(name);
   }

   public boolean containsCollectionFetches() {
      this.errorIfDML();
      List collectionFetches = ((QueryNode)this.sqlAst).getFromClause().getCollectionFetches();
      return collectionFetches != null && collectionFetches.size() > 0;
   }

   public boolean isManipulationStatement() {
      return this.sqlAst.needsExecutor();
   }

   public void validateScrollability() throws HibernateException {
      this.errorIfDML();
      QueryNode query = (QueryNode)this.sqlAst;
      List collectionFetches = query.getFromClause().getCollectionFetches();
      if (!collectionFetches.isEmpty()) {
         if (!this.isShallowQuery()) {
            if (this.getReturnTypes().length > 1) {
               throw new HibernateException("cannot scroll with collection fetches and returned tuples");
            } else {
               FromElement owner = null;

               for(FromElement fromElement : query.getSelectClause().getFromElementsForLoad()) {
                  if (fromElement.getOrigin() == null) {
                     owner = fromElement;
                     break;
                  }
               }

               if (owner == null) {
                  throw new HibernateException("unable to locate collection fetch(es) owner for scrollability checks");
               } else {
                  AST primaryOrdering = query.getOrderByClause().getFirstChild();
                  if (primaryOrdering != null) {
                     String[] idColNames = owner.getQueryable().getIdentifierColumnNames();
                     String expectedPrimaryOrderSeq = StringHelper.join(", ", StringHelper.qualify(owner.getTableAlias(), idColNames));
                     if (!primaryOrdering.getText().startsWith(expectedPrimaryOrderSeq)) {
                        throw new HibernateException("cannot scroll results with collection fetches which are not ordered primarily by the root entity's PK");
                     }
                  }

               }
            }
         }
      }
   }

   private StatementExecutor buildAppropriateStatementExecutor(HqlSqlWalker walker) {
      Statement statement = (Statement)walker.getAST();
      if (walker.getStatementType() == 13) {
         FromElement fromElement = walker.getFinalFromClause().getFromElement();
         Queryable persister = fromElement.getQueryable();
         return (StatementExecutor)(persister.isMultiTable() ? new MultiTableDeleteExecutor(walker) : new BasicExecutor(walker, persister));
      } else if (walker.getStatementType() == 51) {
         FromElement fromElement = walker.getFinalFromClause().getFromElement();
         Queryable persister = fromElement.getQueryable();
         return (StatementExecutor)(persister.isMultiTable() ? new MultiTableUpdateExecutor(walker) : new BasicExecutor(walker, persister));
      } else if (walker.getStatementType() == 29) {
         return new BasicExecutor(walker, ((InsertStatement)statement).getIntoClause().getQueryable());
      } else {
         throw new QueryException("Unexpected statement type");
      }
   }

   public ParameterTranslations getParameterTranslations() {
      if (this.paramTranslations == null) {
         this.paramTranslations = new ParameterTranslationsImpl(this.getWalker().getParameters());
      }

      return this.paramTranslations;
   }

   public List getCollectedParameterSpecifications() {
      return this.collectedParameterSpecifications;
   }

   public Class getDynamicInstantiationResultType() {
      AggregatedSelectExpression aggregation = this.queryLoader.getAggregatedSelectExpression();
      return aggregation == null ? null : aggregation.getAggregationResultType();
   }

   public static class JavaConstantConverter implements NodeTraverser.VisitationStrategy {
      private AST dotRoot;

      public void visit(AST node) {
         if (this.dotRoot != null) {
            if (ASTUtil.isSubtreeChild(this.dotRoot, node)) {
               return;
            }

            this.dotRoot = null;
         }

         if (this.dotRoot == null && node.getType() == 15) {
            this.dotRoot = node;
            this.handleDotStructure(this.dotRoot);
         }

      }

      private void handleDotStructure(AST dotStructureRoot) {
         String expression = ASTUtil.getPathText(dotStructureRoot);
         Object constant = ReflectHelper.getConstantValue(expression);
         if (constant != null) {
            dotStructureRoot.setFirstChild((AST)null);
            dotStructureRoot.setType(100);
            dotStructureRoot.setText(expression);
         }

      }
   }
}
