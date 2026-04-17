package org.hibernate.hql.internal.ast;

import antlr.ASTFactory;
import antlr.RecognitionException;
import antlr.SemanticException;
import antlr.collections.AST;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.hibernate.HibernateException;
import org.hibernate.QueryException;
import org.hibernate.engine.internal.JoinSequence;
import org.hibernate.engine.internal.ParameterBinder;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.hql.internal.antlr.HqlSqlBaseWalker;
import org.hibernate.hql.internal.antlr.SqlTokenTypes;
import org.hibernate.hql.internal.ast.tree.AggregateNode;
import org.hibernate.hql.internal.ast.tree.AssignmentSpecification;
import org.hibernate.hql.internal.ast.tree.CollectionFunction;
import org.hibernate.hql.internal.ast.tree.ConstructorNode;
import org.hibernate.hql.internal.ast.tree.DeleteStatement;
import org.hibernate.hql.internal.ast.tree.DotNode;
import org.hibernate.hql.internal.ast.tree.FromClause;
import org.hibernate.hql.internal.ast.tree.FromElement;
import org.hibernate.hql.internal.ast.tree.FromElementFactory;
import org.hibernate.hql.internal.ast.tree.FromReferenceNode;
import org.hibernate.hql.internal.ast.tree.IdentNode;
import org.hibernate.hql.internal.ast.tree.IndexNode;
import org.hibernate.hql.internal.ast.tree.InsertStatement;
import org.hibernate.hql.internal.ast.tree.IntoClause;
import org.hibernate.hql.internal.ast.tree.MethodNode;
import org.hibernate.hql.internal.ast.tree.OperatorNode;
import org.hibernate.hql.internal.ast.tree.ParameterContainer;
import org.hibernate.hql.internal.ast.tree.ParameterNode;
import org.hibernate.hql.internal.ast.tree.QueryNode;
import org.hibernate.hql.internal.ast.tree.ResolvableNode;
import org.hibernate.hql.internal.ast.tree.RestrictableStatement;
import org.hibernate.hql.internal.ast.tree.ResultVariableRefNode;
import org.hibernate.hql.internal.ast.tree.SelectClause;
import org.hibernate.hql.internal.ast.tree.SelectExpression;
import org.hibernate.hql.internal.ast.tree.UpdateStatement;
import org.hibernate.hql.internal.ast.util.ASTPrinter;
import org.hibernate.hql.internal.ast.util.ASTUtil;
import org.hibernate.hql.internal.ast.util.AliasGenerator;
import org.hibernate.hql.internal.ast.util.JoinProcessor;
import org.hibernate.hql.internal.ast.util.LiteralProcessor;
import org.hibernate.hql.internal.ast.util.NodeTraverser;
import org.hibernate.hql.internal.ast.util.SessionFactoryHelper;
import org.hibernate.hql.internal.ast.util.SyntheticAndFactory;
import org.hibernate.id.BulkInsertionCapableIdentifierGenerator;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.internal.util.collections.ArrayHelper;
import org.hibernate.param.CollectionFilterKeyParameterSpecification;
import org.hibernate.param.NamedParameterSpecification;
import org.hibernate.param.ParameterSpecification;
import org.hibernate.param.PositionalParameterSpecification;
import org.hibernate.param.VersionTypeSeedParameterSpecification;
import org.hibernate.persister.collection.QueryableCollection;
import org.hibernate.persister.entity.Queryable;
import org.hibernate.sql.JoinType;
import org.hibernate.type.AssociationType;
import org.hibernate.type.ComponentType;
import org.hibernate.type.DbTimestampType;
import org.hibernate.type.Type;
import org.hibernate.type.VersionType;
import org.hibernate.usertype.UserVersionType;
import org.jboss.logging.Logger;

public class HqlSqlWalker extends HqlSqlBaseWalker implements ErrorReporter, ParameterBinder.NamedParameterSource {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, HqlSqlWalker.class.getName());
   private final QueryTranslatorImpl queryTranslatorImpl;
   private final HqlParser hqlParser;
   private final SessionFactoryHelper sessionFactoryHelper;
   private final Map tokenReplacements;
   private final AliasGenerator aliasGenerator = new AliasGenerator();
   private final LiteralProcessor literalProcessor;
   private final ParseErrorHandler parseErrorHandler;
   private final ASTPrinter printer;
   private final String collectionFilterRole;
   private FromClause currentFromClause = null;
   private SelectClause selectClause;
   private Map selectExpressionsByResultVariable = new HashMap();
   private Set querySpaces = new HashSet();
   private int parameterCount;
   private Map namedParameters = new HashMap();
   private ArrayList parameters = new ArrayList();
   private int numberOfParametersInSetClause;
   private int positionalParameterCount;
   private ArrayList assignmentSpecifications = new ArrayList();
   private JoinType impliedJoinType;
   private int traceDepth;

   public HqlSqlWalker(QueryTranslatorImpl qti, SessionFactoryImplementor sfi, HqlParser parser, Map tokenReplacements, String collectionRole) {
      this.impliedJoinType = JoinType.INNER_JOIN;
      this.traceDepth = 0;
      this.setASTFactory(new SqlASTFactory(this));
      this.parseErrorHandler = new ErrorCounter();
      this.queryTranslatorImpl = qti;
      this.sessionFactoryHelper = new SessionFactoryHelper(sfi);
      this.literalProcessor = new LiteralProcessor(this);
      this.tokenReplacements = tokenReplacements;
      this.collectionFilterRole = collectionRole;
      this.hqlParser = parser;
      this.printer = new ASTPrinter(SqlTokenTypes.class);
   }

   public void traceIn(String ruleName, AST tree) {
      if (LOG.isTraceEnabled()) {
         if (this.inputState.guessing <= 0) {
            String prefix = StringHelper.repeat('-', this.traceDepth++ * 2) + "-> ";
            String traceText = ruleName + " (" + this.buildTraceNodeName(tree) + ")";
            LOG.trace(prefix + traceText);
         }
      }
   }

   private String buildTraceNodeName(AST tree) {
      return tree == null ? "???" : tree.getText() + " [" + this.printer.getTokenTypeName(tree.getType()) + "]";
   }

   public void traceOut(String ruleName, AST tree) {
      if (LOG.isTraceEnabled()) {
         if (this.inputState.guessing <= 0) {
            String prefix = "<-" + StringHelper.repeat('-', --this.traceDepth * 2) + " ";
            LOG.trace(prefix + ruleName);
         }
      }
   }

   protected void prepareFromClauseInputTree(AST fromClauseInput) {
      if (!this.isSubQuery() && this.isFilter()) {
         QueryableCollection persister = this.sessionFactoryHelper.getCollectionPersister(this.collectionFilterRole);
         Type collectionElementType = persister.getElementType();
         if (!collectionElementType.isEntityType()) {
            throw new QueryException("collection of values in filter: this");
         }

         String collectionElementEntityName = persister.getElementPersister().getEntityName();
         ASTFactory inputAstFactory = this.hqlParser.getASTFactory();
         AST fromElement = ASTUtil.create(inputAstFactory, 76, collectionElementEntityName);
         ASTUtil.createSibling(inputAstFactory, 72, "this", fromElement);
         fromClauseInput.addChild(fromElement);
         LOG.debug("prepareFromClauseInputTree() : Filter - Added 'this' as a from element...");
         this.queryTranslatorImpl.showHqlAst(this.hqlParser.getAST());
         Type collectionFilterKeyType = this.sessionFactoryHelper.requireQueryableCollection(this.collectionFilterRole).getKeyType();
         ParameterNode collectionFilterKeyParameter = (ParameterNode)this.astFactory.create(123, "?");
         CollectionFilterKeyParameterSpecification collectionFilterKeyParameterSpec = new CollectionFilterKeyParameterSpecification(this.collectionFilterRole, collectionFilterKeyType, this.positionalParameterCount++);
         collectionFilterKeyParameter.setHqlParameterSpecification(collectionFilterKeyParameterSpec);
         this.parameters.add(collectionFilterKeyParameterSpec);
      }

   }

   public boolean isFilter() {
      return this.collectionFilterRole != null;
   }

   public String getCollectionFilterRole() {
      return this.collectionFilterRole;
   }

   public SessionFactoryHelper getSessionFactoryHelper() {
      return this.sessionFactoryHelper;
   }

   public Map getTokenReplacements() {
      return this.tokenReplacements;
   }

   public AliasGenerator getAliasGenerator() {
      return this.aliasGenerator;
   }

   public FromClause getCurrentFromClause() {
      return this.currentFromClause;
   }

   public ParseErrorHandler getParseErrorHandler() {
      return this.parseErrorHandler;
   }

   public void reportError(RecognitionException e) {
      this.parseErrorHandler.reportError(e);
   }

   public void reportError(String s) {
      this.parseErrorHandler.reportError(s);
   }

   public void reportWarning(String s) {
      this.parseErrorHandler.reportWarning(s);
   }

   public Set getQuerySpaces() {
      return this.querySpaces;
   }

   protected AST createFromElement(String path, AST alias, AST propertyFetch) throws SemanticException {
      FromElement fromElement = this.currentFromClause.addFromElement(path, alias);
      fromElement.setAllPropertyFetch(propertyFetch != null);
      return fromElement;
   }

   protected AST createFromFilterElement(AST filterEntity, AST alias) throws SemanticException {
      FromElement fromElement = this.currentFromClause.addFromElement(filterEntity.getText(), alias);
      FromClause fromClause = fromElement.getFromClause();
      QueryableCollection persister = this.sessionFactoryHelper.getCollectionPersister(this.collectionFilterRole);
      String[] keyColumnNames = persister.getKeyColumnNames();
      String fkTableAlias = persister.isOneToMany() ? fromElement.getTableAlias() : fromClause.getAliasGenerator().createName(this.collectionFilterRole);
      JoinSequence join = this.sessionFactoryHelper.createJoinSequence();
      join.setRoot(persister, fkTableAlias);
      if (!persister.isOneToMany()) {
         join.addJoin((AssociationType)persister.getElementType(), fromElement.getTableAlias(), JoinType.INNER_JOIN, persister.getElementColumnNames(fkTableAlias));
      }

      join.addCondition(fkTableAlias, keyColumnNames, " = ?");
      fromElement.setJoinSequence(join);
      fromElement.setFilter(true);
      LOG.debug("createFromFilterElement() : processed filter FROM element.");
      return fromElement;
   }

   protected void createFromJoinElement(AST path, AST alias, int joinType, AST fetchNode, AST propertyFetch, AST with) throws SemanticException {
      boolean fetch = fetchNode != null;
      if (fetch && this.isSubQuery()) {
         throw new QueryException("fetch not allowed in subquery from-elements");
      } else if (path.getType() != 15) {
         throw new SemanticException("Path expected for join!");
      } else {
         DotNode dot = (DotNode)path;
         JoinType hibernateJoinType = JoinProcessor.toHibernateJoinType(joinType);
         dot.setJoinType(hibernateJoinType);
         dot.setFetch(fetch);
         dot.resolve(true, false, alias == null ? null : alias.getText());
         FromElement fromElement;
         if (dot.getDataType() != null && dot.getDataType().isComponentType()) {
            FromElementFactory factory = new FromElementFactory(this.getCurrentFromClause(), dot.getLhs().getFromElement(), dot.getPropertyPath(), alias == null ? null : alias.getText(), (String[])null, false);
            fromElement = factory.createComponentJoin((ComponentType)dot.getDataType());
         } else {
            fromElement = dot.getImpliedJoin();
            fromElement.setAllPropertyFetch(propertyFetch != null);
            if (with != null) {
               if (fetch) {
                  throw new SemanticException("with-clause not allowed on fetched associations; use filters");
               }

               this.handleWithFragment(fromElement, with);
            }
         }

         if (LOG.isDebugEnabled()) {
            LOG.debugf("createFromJoinElement() : %s", this.getASTPrinter().showAsString(fromElement, "-- join tree --"));
         }

      }
   }

   private void handleWithFragment(FromElement fromElement, AST hqlWithNode) throws SemanticException {
      try {
         this.withClause(hqlWithNode);
         AST hqlSqlWithNode = this.returnAST;
         if (LOG.isDebugEnabled()) {
            LOG.debugf("handleWithFragment() : %s", this.getASTPrinter().showAsString(hqlSqlWithNode, "-- with clause --"));
         }

         WithClauseVisitor visitor = new WithClauseVisitor(fromElement);
         NodeTraverser traverser = new NodeTraverser(visitor);
         traverser.traverseDepthFirst(hqlSqlWithNode);
         String withClauseJoinAlias = visitor.getJoinAlias();
         if (withClauseJoinAlias == null) {
            withClauseJoinAlias = fromElement.getCollectionTableAlias();
         } else {
            FromElement referencedFromElement = visitor.getReferencedFromElement();
            if (referencedFromElement != fromElement) {
               throw new InvalidWithClauseException("with-clause expressions did not reference from-clause element to which the with-clause was associated");
            }
         }

         SqlGenerator sql = new SqlGenerator(this.getSessionFactoryHelper().getFactory());
         sql.whereExpr(hqlSqlWithNode.getFirstChild());
         fromElement.setWithClauseFragment(withClauseJoinAlias, "(" + sql.getSQL() + ")");
      } catch (SemanticException e) {
         throw e;
      } catch (InvalidWithClauseException e) {
         throw e;
      } catch (Exception e) {
         throw new SemanticException(e.getMessage());
      }
   }

   protected void pushFromClause(AST fromNode, AST inputFromNode) {
      FromClause newFromClause = (FromClause)fromNode;
      newFromClause.setParentFromClause(this.currentFromClause);
      this.currentFromClause = newFromClause;
   }

   private void popFromClause() {
      this.currentFromClause = this.currentFromClause.getParentFromClause();
   }

   protected void lookupAlias(AST aliasRef) throws SemanticException {
      FromElement alias = this.currentFromClause.getFromElement(aliasRef.getText());
      FromReferenceNode aliasRefNode = (FromReferenceNode)aliasRef;
      aliasRefNode.setFromElement(alias);
   }

   protected void setImpliedJoinType(int joinType) {
      this.impliedJoinType = JoinProcessor.toHibernateJoinType(joinType);
   }

   public JoinType getImpliedJoinType() {
      return this.impliedJoinType;
   }

   protected AST lookupProperty(AST dot, boolean root, boolean inSelect) throws SemanticException {
      DotNode dotNode = (DotNode)dot;
      FromReferenceNode lhs = dotNode.getLhs();
      AST rhs = lhs.getNextSibling();
      switch (rhs.getType()) {
         case 17:
         case 27:
            if (LOG.isDebugEnabled()) {
               LOG.debugf("lookupProperty() %s => %s(%s)", dotNode.getPath(), rhs.getText(), lhs.getPath());
            }

            CollectionFunction f = (CollectionFunction)rhs;
            f.setFirstChild(lhs);
            lhs.setNextSibling((AST)null);
            dotNode.setFirstChild(f);
            this.resolve(lhs);
            f.resolve(inSelect);
            return f;
         default:
            dotNode.resolveFirstChild();
            return dotNode;
      }
   }

   protected boolean isNonQualifiedPropertyRef(AST ident) {
      String identText = ident.getText();
      if (this.currentFromClause.isFromElementAlias(identText)) {
         return false;
      } else {
         List fromElements = this.currentFromClause.getExplicitFromElements();
         if (fromElements.size() == 1) {
            FromElement fromElement = (FromElement)fromElements.get(0);

            try {
               LOG.tracev("Attempting to resolve property [{0}] as a non-qualified ref", identText);
               return fromElement.getPropertyMapping(identText).toType(identText) != null;
            } catch (QueryException var6) {
            }
         }

         return false;
      }
   }

   protected AST lookupNonQualifiedProperty(AST property) throws SemanticException {
      FromElement fromElement = (FromElement)this.currentFromClause.getExplicitFromElements().get(0);
      AST syntheticDotNode = this.generateSyntheticDotNodeForNonQualifiedPropertyRef(property, fromElement);
      return this.lookupProperty(syntheticDotNode, false, this.getCurrentClauseType() == 45);
   }

   private AST generateSyntheticDotNodeForNonQualifiedPropertyRef(AST property, FromElement fromElement) {
      AST dot = this.getASTFactory().create(15, "{non-qualified-property-ref}");
      ((DotNode)dot).setPropertyPath(((FromReferenceNode)property).getPath());
      IdentNode syntheticAlias = (IdentNode)this.getASTFactory().create(126, "{synthetic-alias}");
      syntheticAlias.setFromElement(fromElement);
      syntheticAlias.setResolved();
      dot.setFirstChild(syntheticAlias);
      dot.addChild(property);
      return dot;
   }

   protected void processQuery(AST select, AST query) throws SemanticException {
      if (LOG.isDebugEnabled()) {
         LOG.debugf("processQuery() : %s", query.toStringTree());
      }

      try {
         QueryNode qn = (QueryNode)query;
         boolean explicitSelect = select != null && select.getNumberOfChildren() > 0;
         if (!explicitSelect) {
            this.createSelectClauseFromFromClause(qn);
         } else {
            this.useSelectClause(select);
         }

         JoinProcessor joinProcessor = new JoinProcessor(this);
         joinProcessor.processJoins(qn);

         for(FromElement fromElement : qn.getFromClause().getProjectionList()) {
            if (fromElement.isFetch() && fromElement.getQueryableCollection() != null) {
               if (fromElement.getQueryableCollection().hasOrdering()) {
                  String orderByFragment = fromElement.getQueryableCollection().getSQLOrderByString(fromElement.getCollectionTableAlias());
                  qn.getOrderByClause().addOrderFragment(orderByFragment);
               }

               if (fromElement.getQueryableCollection().hasManyToManyOrdering()) {
                  String orderByFragment = fromElement.getQueryableCollection().getManyToManyOrderByString(fromElement.getTableAlias());
                  qn.getOrderByClause().addOrderFragment(orderByFragment);
               }
            }
         }
      } finally {
         this.popFromClause();
      }

   }

   protected void postProcessDML(RestrictableStatement statement) throws SemanticException {
      statement.getFromClause().resolve();
      FromElement fromElement = (FromElement)statement.getFromClause().getFromElements().get(0);
      Queryable persister = fromElement.getQueryable();
      fromElement.setText(persister.getTableName());
      if (persister.getDiscriminatorType() != null || !this.queryTranslatorImpl.getEnabledFilters().isEmpty()) {
         (new SyntheticAndFactory(this)).addDiscriminatorWhereFragment(statement, persister, this.queryTranslatorImpl.getEnabledFilters(), fromElement.getTableAlias());
      }

   }

   protected void postProcessUpdate(AST update) throws SemanticException {
      UpdateStatement updateStatement = (UpdateStatement)update;
      this.postProcessDML(updateStatement);
   }

   protected void postProcessDelete(AST delete) throws SemanticException {
      this.postProcessDML((DeleteStatement)delete);
   }

   protected void postProcessInsert(AST insert) throws SemanticException, QueryException {
      InsertStatement insertStatement = (InsertStatement)insert;
      insertStatement.validate();
      SelectClause selectClause = insertStatement.getSelectClause();
      Queryable persister = insertStatement.getIntoClause().getQueryable();
      if (!insertStatement.getIntoClause().isExplicitIdInsertion()) {
         IdentifierGenerator generator = persister.getIdentifierGenerator();
         if (!BulkInsertionCapableIdentifierGenerator.class.isInstance(generator)) {
            throw new QueryException("Invalid identifier generator encountered for implicit id handling as part of bulk insertions");
         }

         BulkInsertionCapableIdentifierGenerator capableGenerator = (BulkInsertionCapableIdentifierGenerator)BulkInsertionCapableIdentifierGenerator.class.cast(generator);
         if (!capableGenerator.supportsBulkInsertionIdentifierGeneration()) {
            throw new QueryException("Identifier generator reported it does not support implicit id handling as part of bulk insertions");
         }

         String fragment = capableGenerator.determineBulkInsertionIdentifierGenerationSelectFragment(this.sessionFactoryHelper.getFactory().getDialect());
         if (fragment != null) {
            AST fragmentNode = this.getASTFactory().create(142, fragment);
            AST originalFirstSelectExprNode = selectClause.getFirstChild();
            selectClause.setFirstChild(fragmentNode);
            fragmentNode.setNextSibling(originalFirstSelectExprNode);
            insertStatement.getIntoClause().prependIdColumnSpec();
         }
      }

      boolean includeVersionProperty = persister.isVersioned() && !insertStatement.getIntoClause().isExplicitVersionInsertion() && persister.isVersionPropertyInsertable();
      if (includeVersionProperty) {
         VersionType versionType = persister.getVersionType();
         AST versionValueNode = null;
         if (this.sessionFactoryHelper.getFactory().getDialect().supportsParametersInInsertSelect()) {
            int[] sqlTypes = versionType.sqlTypes(this.sessionFactoryHelper.getFactory());
            if (sqlTypes == null || sqlTypes.length == 0) {
               throw new IllegalStateException(versionType.getClass() + ".sqlTypes() returns null or empty array");
            }

            if (sqlTypes.length > 1) {
               throw new IllegalStateException(versionType.getClass() + ".sqlTypes() returns > 1 element; only single-valued versions are allowed.");
            }

            versionValueNode = this.getASTFactory().create(123, "?");
            ParameterSpecification paramSpec = new VersionTypeSeedParameterSpecification(versionType);
            ((ParameterNode)versionValueNode).setHqlParameterSpecification(paramSpec);
            this.parameters.add(0, paramSpec);
            if (this.sessionFactoryHelper.getFactory().getDialect().requiresCastingOfParametersInSelectClause()) {
               MethodNode versionMethodNode = (MethodNode)this.getASTFactory().create(81, "(");
               AST methodIdentNode = this.getASTFactory().create(126, "cast");
               versionMethodNode.addChild(methodIdentNode);
               versionMethodNode.initializeMethodNode(methodIdentNode, true);
               AST castExprListNode = this.getASTFactory().create(75, "exprList");
               methodIdentNode.setNextSibling(castExprListNode);
               castExprListNode.addChild(versionValueNode);
               versionValueNode.setNextSibling(this.getASTFactory().create(126, this.sessionFactoryHelper.getFactory().getDialect().getTypeName(sqlTypes[0])));
               this.processFunction(versionMethodNode, true);
               versionValueNode = versionMethodNode;
            }
         } else if (this.isIntegral(versionType)) {
            try {
               Object seedValue = versionType.seed((SessionImplementor)null);
               versionValueNode = this.getASTFactory().create(142, seedValue.toString());
            } catch (Throwable var13) {
               throw new QueryException("could not determine seed value for version on bulk insert [" + versionType + "]");
            }
         } else {
            if (!this.isDatabaseGeneratedTimestamp(versionType)) {
               throw new QueryException("cannot handle version type [" + versionType + "] on bulk inserts with dialects not supporting parameters in insert-select statements");
            }

            String functionName = this.sessionFactoryHelper.getFactory().getDialect().getCurrentTimestampSQLFunctionName();
            versionValueNode = this.getASTFactory().create(142, functionName);
         }

         AST currentFirstSelectExprNode = selectClause.getFirstChild();
         selectClause.setFirstChild(versionValueNode);
         versionValueNode.setNextSibling(currentFirstSelectExprNode);
         insertStatement.getIntoClause().prependVersionColumnSpec();
      }

      if (insertStatement.getIntoClause().isDiscriminated()) {
         String sqlValue = insertStatement.getIntoClause().getQueryable().getDiscriminatorSQLValue();
         AST discrimValue = this.getASTFactory().create(142, sqlValue);
         insertStatement.getSelectClause().addChild(discrimValue);
      }

   }

   private boolean isDatabaseGeneratedTimestamp(Type type) {
      return DbTimestampType.class.isAssignableFrom(type.getClass());
   }

   private boolean isIntegral(Type type) {
      return Long.class.isAssignableFrom(type.getReturnedClass()) || Integer.class.isAssignableFrom(type.getReturnedClass()) || Long.TYPE.isAssignableFrom(type.getReturnedClass()) || Integer.TYPE.isAssignableFrom(type.getReturnedClass());
   }

   private void useSelectClause(AST select) throws SemanticException {
      this.selectClause = (SelectClause)select;
      this.selectClause.initializeExplicitSelectClause(this.currentFromClause);
   }

   private void createSelectClauseFromFromClause(QueryNode qn) throws SemanticException {
      AST select = this.astFactory.create(137, "{derived select clause}");
      AST sibling = qn.getFromClause();
      qn.setFirstChild(select);
      select.setNextSibling(sibling);
      this.selectClause = (SelectClause)select;
      this.selectClause.initializeDerivedSelectClause(this.currentFromClause);
      LOG.debug("Derived SELECT clause created.");
   }

   protected void resolve(AST node) throws SemanticException {
      if (node != null) {
         ResolvableNode r = (ResolvableNode)node;
         if (this.isInFunctionCall()) {
            r.resolveInFunctionCall(false, true);
         } else {
            r.resolve(false, true);
         }
      }

   }

   protected void resolveSelectExpression(AST node) throws SemanticException {
      int type = node.getType();
      switch (type) {
         case 15:
            DotNode dot = (DotNode)node;
            dot.resolveSelectExpression();
            break;
         case 140:
            FromReferenceNode aliasRefNode = (FromReferenceNode)node;
            aliasRefNode.resolve(false, false);
            FromElement fromElement = aliasRefNode.getFromElement();
            if (fromElement != null) {
               fromElement.setIncludeSubclasses(true);
            }
      }

   }

   protected void beforeSelectClause() throws SemanticException {
      FromClause from = this.getCurrentFromClause();

      for(FromElement fromElement : from.getFromElements()) {
         fromElement.setIncludeSubclasses(false);
      }

   }

   protected AST generatePositionalParameter(AST inputNode) throws SemanticException {
      if (this.namedParameters.size() > 0) {
         throw new SemanticException("cannot define positional parameter after any named parameters have been defined");
      } else {
         LOG.warnf("[DEPRECATION] Encountered positional parameter near line %s, column %s.  Positional parameter are considered deprecated; use named parameters or JPA-style positional parameters instead.", inputNode.getLine(), inputNode.getColumn());
         ParameterNode parameter = (ParameterNode)this.astFactory.create(123, "?");
         PositionalParameterSpecification paramSpec = new PositionalParameterSpecification(inputNode.getLine(), inputNode.getColumn(), this.positionalParameterCount++);
         parameter.setHqlParameterSpecification(paramSpec);
         this.parameters.add(paramSpec);
         return parameter;
      }
   }

   protected AST generateNamedParameter(AST delimiterNode, AST nameNode) throws SemanticException {
      String name = nameNode.getText();
      this.trackNamedParameterPositions(name);
      ParameterNode parameter = (ParameterNode)this.astFactory.create(148, name);
      parameter.setText("?");
      NamedParameterSpecification paramSpec = new NamedParameterSpecification(delimiterNode.getLine(), delimiterNode.getColumn(), name);
      parameter.setHqlParameterSpecification(paramSpec);
      this.parameters.add(paramSpec);
      return parameter;
   }

   private void trackNamedParameterPositions(String name) {
      Integer loc = this.parameterCount++;
      Object o = this.namedParameters.get(name);
      if (o == null) {
         this.namedParameters.put(name, loc);
      } else if (o instanceof Integer) {
         ArrayList list = new ArrayList(4);
         list.add(o);
         list.add(loc);
         this.namedParameters.put(name, list);
      } else {
         ((ArrayList)o).add(loc);
      }

   }

   protected void processConstant(AST constant) throws SemanticException {
      this.literalProcessor.processConstant(constant, true);
   }

   protected void processBoolean(AST constant) throws SemanticException {
      this.literalProcessor.processBoolean(constant);
   }

   protected void processNumericLiteral(AST literal) {
      this.literalProcessor.processNumeric(literal);
   }

   protected void processIndex(AST indexOp) throws SemanticException {
      IndexNode indexNode = (IndexNode)indexOp;
      indexNode.resolve(true, true);
   }

   protected void processFunction(AST functionCall, boolean inSelect) throws SemanticException {
      MethodNode methodNode = (MethodNode)functionCall;
      methodNode.resolve(inSelect);
   }

   protected void processAggregation(AST node, boolean inSelect) throws SemanticException {
      AggregateNode aggregateNode = (AggregateNode)node;
      aggregateNode.resolve();
   }

   protected void processConstructor(AST constructor) throws SemanticException {
      ConstructorNode constructorNode = (ConstructorNode)constructor;
      constructorNode.prepare();
   }

   protected void setAlias(AST selectExpr, AST ident) {
      ((SelectExpression)selectExpr).setAlias(ident.getText());
      if (!this.isSubQuery()) {
         this.selectExpressionsByResultVariable.put(ident.getText(), (SelectExpression)selectExpr);
      }

   }

   protected boolean isOrderExpressionResultVariableRef(AST orderExpressionNode) throws SemanticException {
      return !this.isSubQuery() && orderExpressionNode.getType() == 126 && this.selectExpressionsByResultVariable.containsKey(orderExpressionNode.getText());
   }

   protected void handleResultVariableRef(AST resultVariableRef) throws SemanticException {
      if (this.isSubQuery()) {
         throw new SemanticException("References to result variables in subqueries are not supported.");
      } else {
         ((ResultVariableRefNode)resultVariableRef).setSelectExpression((SelectExpression)this.selectExpressionsByResultVariable.get(resultVariableRef.getText()));
      }
   }

   public int[] getNamedParameterLocations(String name) throws QueryException {
      Object o = this.namedParameters.get(name);
      if (o == null) {
         QueryException qe = new QueryException("Named parameter does not appear in Query: " + name);
         qe.setQueryString(this.queryTranslatorImpl.getQueryString());
         throw qe;
      } else {
         return o instanceof Integer ? new int[]{(Integer)o} : ArrayHelper.toIntArray((ArrayList)o);
      }
   }

   public void addQuerySpaces(Serializable[] spaces) {
      this.querySpaces.addAll(Arrays.asList(spaces));
   }

   public Type[] getReturnTypes() {
      return this.selectClause.getQueryReturnTypes();
   }

   public String[] getReturnAliases() {
      return this.selectClause.getQueryReturnAliases();
   }

   public SelectClause getSelectClause() {
      return this.selectClause;
   }

   public FromClause getFinalFromClause() {
      FromClause top;
      for(top = this.currentFromClause; top.getParentFromClause() != null; top = top.getParentFromClause()) {
      }

      return top;
   }

   public boolean isShallowQuery() {
      return this.getStatementType() == 29 || this.queryTranslatorImpl.isShallowQuery();
   }

   public Map getEnabledFilters() {
      return this.queryTranslatorImpl.getEnabledFilters();
   }

   public LiteralProcessor getLiteralProcessor() {
      return this.literalProcessor;
   }

   public ASTPrinter getASTPrinter() {
      return this.printer;
   }

   public ArrayList getParameters() {
      return this.parameters;
   }

   public int getNumberOfParametersInSetClause() {
      return this.numberOfParametersInSetClause;
   }

   protected void evaluateAssignment(AST eq) throws SemanticException {
      this.prepareLogicOperator(eq);
      Queryable persister = this.getCurrentFromClause().getFromElement().getQueryable();
      this.evaluateAssignment(eq, persister, -1);
   }

   private void evaluateAssignment(AST eq, Queryable persister, int targetIndex) {
      if (persister.isMultiTable()) {
         AssignmentSpecification specification = new AssignmentSpecification(eq, persister);
         if (targetIndex >= 0) {
            this.assignmentSpecifications.add(targetIndex, specification);
         } else {
            this.assignmentSpecifications.add(specification);
         }

         this.numberOfParametersInSetClause += specification.getParameters().length;
      }

   }

   public ArrayList getAssignmentSpecifications() {
      return this.assignmentSpecifications;
   }

   protected AST createIntoClause(String path, AST propertySpec) throws SemanticException {
      Queryable persister = (Queryable)this.getSessionFactoryHelper().requireClassPersister(path);
      IntoClause intoClause = (IntoClause)this.getASTFactory().create(30, persister.getEntityName());
      intoClause.setFirstChild(propertySpec);
      intoClause.initialize(persister);
      this.addQuerySpaces(persister.getQuerySpaces());
      return intoClause;
   }

   protected void prepareVersioned(AST updateNode, AST versioned) throws SemanticException {
      UpdateStatement updateStatement = (UpdateStatement)updateNode;
      FromClause fromClause = updateStatement.getFromClause();
      if (versioned != null) {
         Queryable persister = fromClause.getFromElement().getQueryable();
         if (!persister.isVersioned()) {
            throw new SemanticException("increment option specified for update of non-versioned entity");
         }

         VersionType versionType = persister.getVersionType();
         if (versionType instanceof UserVersionType) {
            throw new SemanticException("user-defined version types not supported for increment option");
         }

         AST eq = this.getASTFactory().create(102, "=");
         AST versionPropertyNode = this.generateVersionPropertyNode(persister);
         eq.setFirstChild(versionPropertyNode);
         AST versionIncrementNode = null;
         if (this.isTimestampBasedVersion(versionType)) {
            versionIncrementNode = this.getASTFactory().create(123, "?");
            ParameterSpecification paramSpec = new VersionTypeSeedParameterSpecification(versionType);
            ((ParameterNode)versionIncrementNode).setHqlParameterSpecification(paramSpec);
            this.parameters.add(0, paramSpec);
         } else {
            versionIncrementNode = this.getASTFactory().create(115, "+");
            versionIncrementNode.setFirstChild(this.generateVersionPropertyNode(persister));
            versionIncrementNode.addChild(this.getASTFactory().create(126, "1"));
         }

         eq.addChild(versionIncrementNode);
         this.evaluateAssignment(eq, persister, 0);
         AST setClause = updateStatement.getSetClause();
         AST currentFirstSetElement = setClause.getFirstChild();
         setClause.setFirstChild(eq);
         eq.setNextSibling(currentFirstSetElement);
      }

   }

   private boolean isTimestampBasedVersion(VersionType versionType) {
      Class javaType = versionType.getReturnedClass();
      return Date.class.isAssignableFrom(javaType) || Calendar.class.isAssignableFrom(javaType);
   }

   private AST generateVersionPropertyNode(Queryable persister) throws SemanticException {
      String versionPropertyName = persister.getPropertyNames()[persister.getVersionProperty()];
      AST versionPropertyRef = this.getASTFactory().create(126, versionPropertyName);
      AST versionPropertyNode = this.lookupNonQualifiedProperty(versionPropertyRef);
      this.resolve(versionPropertyNode);
      return versionPropertyNode;
   }

   protected void prepareLogicOperator(AST operator) throws SemanticException {
      ((OperatorNode)operator).initialize();
   }

   protected void prepareArithmeticOperator(AST operator) throws SemanticException {
      ((OperatorNode)operator).initialize();
   }

   protected void validateMapPropertyExpression(AST node) throws SemanticException {
      try {
         FromReferenceNode fromReferenceNode = (FromReferenceNode)node;
         QueryableCollection collectionPersister = fromReferenceNode.getFromElement().getQueryableCollection();
         if (!Map.class.isAssignableFrom(collectionPersister.getCollectionType().getReturnedClass())) {
            throw new SemanticException("node did not reference a map");
         }
      } catch (SemanticException se) {
         throw se;
      } catch (Throwable var5) {
         throw new SemanticException("node did not reference a map");
      }
   }

   public static void panic() {
      throw new QueryException("TreeWalker: panic");
   }

   private static class WithClauseVisitor implements NodeTraverser.VisitationStrategy {
      private final FromElement joinFragment;
      private FromElement referencedFromElement;
      private String joinAlias;

      public WithClauseVisitor(FromElement fromElement) {
         this.joinFragment = fromElement;
      }

      public void visit(AST node) {
         if (node instanceof DotNode) {
            DotNode dotNode = (DotNode)node;
            FromElement fromElement = dotNode.getFromElement();
            if (this.referencedFromElement != null) {
               if (fromElement != this.referencedFromElement) {
                  throw new HibernateException("with-clause referenced two different from-clause elements");
               }
            } else {
               this.referencedFromElement = fromElement;
               this.joinAlias = this.extractAppliedAlias(dotNode);
               if (!this.joinAlias.equals(this.referencedFromElement.getTableAlias())) {
                  throw new InvalidWithClauseException("with clause can only reference columns in the driving table");
               }
            }
         } else if (node instanceof ParameterNode) {
            this.applyParameterSpecification(((ParameterNode)node).getHqlParameterSpecification());
         } else if (node instanceof ParameterContainer) {
            this.applyParameterSpecifications((ParameterContainer)node);
         }

      }

      private void applyParameterSpecifications(ParameterContainer parameterContainer) {
         if (parameterContainer.hasEmbeddedParameters()) {
            ParameterSpecification[] specs = parameterContainer.getEmbeddedParameters();

            for(int i = 0; i < specs.length; ++i) {
               this.applyParameterSpecification(specs[i]);
            }
         }

      }

      private void applyParameterSpecification(ParameterSpecification paramSpec) {
         this.joinFragment.addEmbeddedParameter(paramSpec);
      }

      private String extractAppliedAlias(DotNode dotNode) {
         return dotNode.getText().substring(0, dotNode.getText().indexOf(46));
      }

      public FromElement getReferencedFromElement() {
         return this.referencedFromElement;
      }

      public String getJoinAlias() {
         return this.joinAlias;
      }
   }
}
