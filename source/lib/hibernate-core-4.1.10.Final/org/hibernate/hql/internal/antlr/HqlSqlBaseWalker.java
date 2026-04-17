package org.hibernate.hql.internal.antlr;

import antlr.ASTPair;
import antlr.NoViableAltException;
import antlr.RecognitionException;
import antlr.SemanticException;
import antlr.TreeParser;
import antlr.collections.AST;
import antlr.collections.impl.ASTArray;
import antlr.collections.impl.BitSet;
import org.hibernate.internal.CoreMessageLogger;
import org.jboss.logging.Logger;

public class HqlSqlBaseWalker extends TreeParser implements HqlSqlTokenTypes {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, HqlSqlBaseWalker.class.getName());
   private int level = 0;
   private boolean inSelect = false;
   private boolean inFunctionCall = false;
   private boolean inCase = false;
   private boolean inFrom = false;
   private boolean inCount = false;
   private boolean inCountDistinct = false;
   private int statementType;
   private String statementTypeName;
   private int currentClauseType;
   private int currentTopLevelClauseType;
   private int currentStatementType;
   public static final String[] _tokenNames = new String[]{"<0>", "EOF", "<2>", "NULL_TREE_LOOKAHEAD", "\"all\"", "\"any\"", "\"and\"", "\"as\"", "\"asc\"", "\"avg\"", "\"between\"", "\"class\"", "\"count\"", "\"delete\"", "\"desc\"", "DOT", "\"distinct\"", "\"elements\"", "\"escape\"", "\"exists\"", "\"false\"", "\"fetch\"", "\"from\"", "\"full\"", "\"group\"", "\"having\"", "\"in\"", "\"indices\"", "\"inner\"", "\"insert\"", "\"into\"", "\"is\"", "\"join\"", "\"left\"", "\"like\"", "\"max\"", "\"min\"", "\"new\"", "\"not\"", "\"null\"", "\"or\"", "\"order\"", "\"outer\"", "\"properties\"", "\"right\"", "\"select\"", "\"set\"", "\"some\"", "\"sum\"", "\"true\"", "\"union\"", "\"update\"", "\"versioned\"", "\"where\"", "\"case\"", "\"end\"", "\"else\"", "\"then\"", "\"when\"", "\"on\"", "\"with\"", "\"both\"", "\"empty\"", "\"leading\"", "\"member\"", "\"object\"", "\"of\"", "\"trailing\"", "KEY", "VALUE", "ENTRY", "AGGREGATE", "ALIAS", "CONSTRUCTOR", "CASE2", "EXPR_LIST", "FILTER_ENTITY", "IN_LIST", "INDEX_OP", "IS_NOT_NULL", "IS_NULL", "METHOD_CALL", "NOT_BETWEEN", "NOT_IN", "NOT_LIKE", "ORDER_ELEMENT", "QUERY", "RANGE", "ROW_STAR", "SELECT_FROM", "UNARY_MINUS", "UNARY_PLUS", "VECTOR_EXPR", "WEIRD_IDENT", "CONSTANT", "NUM_DOUBLE", "NUM_FLOAT", "NUM_LONG", "NUM_BIG_INTEGER", "NUM_BIG_DECIMAL", "JAVA_CONSTANT", "COMMA", "EQ", "OPEN", "CLOSE", "\"by\"", "\"ascending\"", "\"descending\"", "NE", "SQL_NE", "LT", "GT", "LE", "GE", "CONCAT", "PLUS", "MINUS", "STAR", "DIV", "MOD", "OPEN_BRACKET", "CLOSE_BRACKET", "COLON", "PARAM", "NUM_INT", "QUOTED_STRING", "IDENT", "ID_START_LETTER", "ID_LETTER", "ESCqs", "WS", "HEX_DIGIT", "EXPONENT", "FLOAT_SUFFIX", "FROM_FRAGMENT", "IMPLIED_FROM", "JOIN_FRAGMENT", "SELECT_CLAUSE", "LEFT_OUTER", "RIGHT_OUTER", "ALIAS_REF", "PROPERTY_REF", "SQL_TOKEN", "SELECT_COLUMNS", "SELECT_EXPR", "THETA_JOINS", "FILTERS", "METHOD_NAME", "NAMED_PARAM", "BOGUS", "RESULT_VARIABLE_REF"};
   public static final BitSet _tokenSet_0 = new BitSet(mk_tokenSet_0());
   public static final BitSet _tokenSet_1 = new BitSet(mk_tokenSet_1());

   public final boolean isSubQuery() {
      return this.level > 1;
   }

   public final boolean isInFrom() {
      return this.inFrom;
   }

   public final boolean isInFunctionCall() {
      return this.inFunctionCall;
   }

   public final boolean isInSelect() {
      return this.inSelect;
   }

   public final boolean isInCase() {
      return this.inCase;
   }

   public final boolean isInCount() {
      return this.inCount;
   }

   public final boolean isInCountDistinct() {
      return this.inCountDistinct;
   }

   public final int getStatementType() {
      return this.statementType;
   }

   public final int getCurrentClauseType() {
      return this.currentClauseType;
   }

   public final int getCurrentTopLevelClauseType() {
      return this.currentTopLevelClauseType;
   }

   public final int getCurrentStatementType() {
      return this.currentStatementType;
   }

   public final boolean isComparativeExpressionClause() {
      return this.getCurrentClauseType() == 53 || this.getCurrentClauseType() == 60 || this.isInCase();
   }

   public final boolean isSelectStatement() {
      return this.statementType == 45;
   }

   private void beforeStatement(String statementName, int statementType) {
      this.inFunctionCall = false;
      ++this.level;
      if (this.level == 1) {
         this.statementTypeName = statementName;
         this.statementType = statementType;
      }

      this.currentStatementType = statementType;
      LOG.debugf("%s << begin [level=%s, statement=%s]", statementName, this.level, this.statementTypeName);
   }

   private void beforeStatementCompletion(String statementName) {
      LOG.debugf("%s : finishing up [level=%s, statement=%s]", statementName, this.level, this.statementTypeName);
   }

   private void afterStatementCompletion(String statementName) {
      LOG.debugf("%s >> end [level=%s, statement=%s]", statementName, this.level, this.statementTypeName);
      --this.level;
   }

   private void handleClauseStart(int clauseType) {
      this.currentClauseType = clauseType;
      if (this.level == 1) {
         this.currentTopLevelClauseType = clauseType;
      }

   }

   protected void evaluateAssignment(AST eq) throws SemanticException {
   }

   protected void prepareFromClauseInputTree(AST fromClauseInput) {
   }

   protected void pushFromClause(AST fromClause, AST inputFromNode) {
   }

   protected AST createFromElement(String path, AST alias, AST propertyFetch) throws SemanticException {
      return null;
   }

   protected void createFromJoinElement(AST path, AST alias, int joinType, AST fetch, AST propertyFetch, AST with) throws SemanticException {
   }

   protected AST createFromFilterElement(AST filterEntity, AST alias) throws SemanticException {
      return null;
   }

   protected void processQuery(AST select, AST query) throws SemanticException {
   }

   protected void postProcessUpdate(AST update) throws SemanticException {
   }

   protected void postProcessDelete(AST delete) throws SemanticException {
   }

   protected void postProcessInsert(AST insert) throws SemanticException {
   }

   protected void beforeSelectClause() throws SemanticException {
   }

   protected void processIndex(AST indexOp) throws SemanticException {
   }

   protected void processConstant(AST constant) throws SemanticException {
   }

   protected void processBoolean(AST constant) throws SemanticException {
   }

   protected void processNumericLiteral(AST literal) throws SemanticException {
   }

   protected void resolve(AST node) throws SemanticException {
   }

   protected void resolveSelectExpression(AST dotNode) throws SemanticException {
   }

   protected void processFunction(AST functionCall, boolean inSelect) throws SemanticException {
   }

   protected void processAggregation(AST node, boolean inSelect) throws SemanticException {
   }

   protected void processConstructor(AST constructor) throws SemanticException {
   }

   protected AST generateNamedParameter(AST delimiterNode, AST nameNode) throws SemanticException {
      return this.astFactory.make((new ASTArray(1)).add(this.astFactory.create(148, nameNode.getText())));
   }

   protected AST generatePositionalParameter(AST inputNode) throws SemanticException {
      return this.astFactory.make((new ASTArray(1)).add(this.astFactory.create(123, "?")));
   }

   protected void lookupAlias(AST ident) throws SemanticException {
   }

   protected void setAlias(AST selectExpr, AST ident) {
   }

   protected boolean isOrderExpressionResultVariableRef(AST ident) throws SemanticException {
      return false;
   }

   protected void handleResultVariableRef(AST resultVariableRef) throws SemanticException {
   }

   protected AST lookupProperty(AST dot, boolean root, boolean inSelect) throws SemanticException {
      return dot;
   }

   protected boolean isNonQualifiedPropertyRef(AST ident) {
      return false;
   }

   protected AST lookupNonQualifiedProperty(AST property) throws SemanticException {
      return property;
   }

   protected void setImpliedJoinType(int joinType) {
   }

   protected AST createIntoClause(String path, AST propertySpec) throws SemanticException {
      return null;
   }

   protected void prepareVersioned(AST updateNode, AST versionedNode) throws SemanticException {
   }

   protected void prepareLogicOperator(AST operator) throws SemanticException {
   }

   protected void prepareArithmeticOperator(AST operator) throws SemanticException {
   }

   protected void processMapComponentReference(AST node) throws SemanticException {
   }

   protected void validateMapPropertyExpression(AST node) throws SemanticException {
   }

   public HqlSqlBaseWalker() {
      this.tokenNames = _tokenNames;
   }

   public final void statement(AST _t) throws RecognitionException {
      AST statement_AST_in = _t == ASTNULL ? null : _t;
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST statement_AST = null;

      try {
         if (_t == null) {
            _t = ASTNULL;
         }

         switch (_t.getType()) {
            case 13:
               this.deleteStatement(_t);
               _t = this._retTree;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               statement_AST = currentAST.root;
               break;
            case 29:
               this.insertStatement(_t);
               _t = this._retTree;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               statement_AST = currentAST.root;
               break;
            case 51:
               this.updateStatement(_t);
               _t = this._retTree;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               statement_AST = currentAST.root;
               break;
            case 86:
               this.selectStatement(_t);
               _t = this._retTree;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               statement_AST = currentAST.root;
               break;
            default:
               throw new NoViableAltException(_t);
         }
      } catch (RecognitionException ex) {
         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this.returnAST = statement_AST;
      this._retTree = _t;
   }

   public final void selectStatement(AST _t) throws RecognitionException {
      AST selectStatement_AST_in = _t == ASTNULL ? null : _t;
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST selectStatement_AST = null;

      try {
         this.query(_t);
         _t = this._retTree;
         this.astFactory.addASTChild(currentAST, this.returnAST);
         selectStatement_AST = currentAST.root;
      } catch (RecognitionException ex) {
         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this.returnAST = selectStatement_AST;
      this._retTree = _t;
   }

   public final void updateStatement(AST _t) throws RecognitionException {
      AST updateStatement_AST_in = _t == ASTNULL ? null : _t;
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST updateStatement_AST = null;
      AST u = null;
      AST u_AST = null;
      AST v = null;
      AST v_AST = null;
      AST f_AST = null;
      AST f = null;
      AST s_AST = null;
      AST s = null;
      AST w_AST = null;
      AST w = null;

      try {
         AST __t4 = _t;
         u = _t == ASTNULL ? null : _t;
         AST u_AST_in = null;
         u_AST = this.astFactory.create(u);
         ASTPair __currentAST4 = currentAST.copy();
         currentAST.root = currentAST.child;
         currentAST.child = null;
         this.match(_t, 51);
         _t = _t.getFirstChild();
         this.beforeStatement("update", 51);
         if (_t == null) {
            _t = ASTNULL;
         }

         switch (_t.getType()) {
            case 52:
               AST v_AST_in = null;
               v_AST = this.astFactory.create(_t);
               this.match(_t, 52);
               _t = _t.getNextSibling();
            case 22:
               AST var28 = _t == ASTNULL ? null : _t;
               this.fromClause(_t);
               _t = this._retTree;
               f_AST = this.returnAST;
               s = _t == ASTNULL ? null : _t;
               this.setClause(_t);
               AST var22 = this._retTree;
               s_AST = this.returnAST;
               if (var22 == null) {
                  var22 = ASTNULL;
               }

               switch (((AST)var22).getType()) {
                  case 3:
                     break;
                  case 53:
                     AST var31 = var22 == ASTNULL ? null : var22;
                     this.whereClause((AST)var22);
                     AST var23 = this._retTree;
                     w_AST = this.returnAST;
                     break;
                  default:
                     throw new NoViableAltException((AST)var22);
               }

               _t = __t4.getNextSibling();
               AST var24 = __currentAST4.root;
               updateStatement_AST = this.astFactory.make((new ASTArray(4)).add(u_AST).add(f_AST).add(s_AST).add(w_AST));
               this.beforeStatementCompletion("update");
               this.prepareVersioned(updateStatement_AST, v_AST);
               this.postProcessUpdate(updateStatement_AST);
               this.afterStatementCompletion("update");
               __currentAST4.root = updateStatement_AST;
               __currentAST4.child = updateStatement_AST != null && updateStatement_AST.getFirstChild() != null ? updateStatement_AST.getFirstChild() : updateStatement_AST;
               __currentAST4.advanceChildToEnd();
               break;
            default:
               throw new NoViableAltException(_t);
         }
      } catch (RecognitionException ex) {
         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this.returnAST = updateStatement_AST;
      this._retTree = _t;
   }

   public final void deleteStatement(AST _t) throws RecognitionException {
      AST deleteStatement_AST_in = _t == ASTNULL ? null : _t;
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST deleteStatement_AST = null;

      try {
         AST __t8 = _t;
         AST tmp1_AST = null;
         AST tmp1_AST_in = null;
         tmp1_AST = this.astFactory.create(_t);
         this.astFactory.addASTChild(currentAST, tmp1_AST);
         ASTPair __currentAST8 = currentAST.copy();
         currentAST.root = currentAST.child;
         currentAST.child = null;
         this.match(_t, 13);
         _t = _t.getFirstChild();
         this.beforeStatement("delete", 13);
         this.fromClause(_t);
         AST var11 = this._retTree;
         this.astFactory.addASTChild(currentAST, this.returnAST);
         if (var11 == null) {
            var11 = ASTNULL;
         }

         switch (((AST)var11).getType()) {
            case 53:
               this.whereClause((AST)var11);
               AST var12 = this._retTree;
               this.astFactory.addASTChild(currentAST, this.returnAST);
            case 3:
               _t = __t8.getNextSibling();
               deleteStatement_AST = __currentAST8.root;
               this.beforeStatementCompletion("delete");
               this.postProcessDelete(deleteStatement_AST);
               this.afterStatementCompletion("delete");
               deleteStatement_AST = __currentAST8.root;
               break;
            default:
               throw new NoViableAltException((AST)var11);
         }
      } catch (RecognitionException ex) {
         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this.returnAST = deleteStatement_AST;
      this._retTree = _t;
   }

   public final void insertStatement(AST _t) throws RecognitionException {
      AST insertStatement_AST_in = _t == ASTNULL ? null : _t;
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST insertStatement_AST = null;

      try {
         AST __t11 = _t;
         AST tmp2_AST = null;
         AST tmp2_AST_in = null;
         tmp2_AST = this.astFactory.create(_t);
         this.astFactory.addASTChild(currentAST, tmp2_AST);
         ASTPair __currentAST11 = currentAST.copy();
         currentAST.root = currentAST.child;
         currentAST.child = null;
         this.match(_t, 29);
         _t = _t.getFirstChild();
         this.beforeStatement("insert", 29);
         this.intoClause(_t);
         _t = this._retTree;
         this.astFactory.addASTChild(currentAST, this.returnAST);
         this.query(_t);
         _t = this._retTree;
         this.astFactory.addASTChild(currentAST, this.returnAST);
         _t = __t11.getNextSibling();
         insertStatement_AST = __currentAST11.root;
         this.beforeStatementCompletion("insert");
         this.postProcessInsert(insertStatement_AST);
         this.afterStatementCompletion("insert");
         insertStatement_AST = __currentAST11.root;
      } catch (RecognitionException ex) {
         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this.returnAST = insertStatement_AST;
      this._retTree = _t;
   }

   public final void query(AST _t) throws RecognitionException {
      AST query_AST_in = _t == ASTNULL ? null : _t;
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST query_AST = null;
      AST f_AST = null;
      AST f = null;
      AST s_AST = null;
      AST s = null;
      AST w_AST = null;
      AST w = null;
      AST g_AST = null;
      AST g = null;
      AST o_AST = null;
      AST o = null;

      try {
         AST __t29 = _t;
         AST tmp3_AST = null;
         AST tmp3_AST_in = null;
         this.astFactory.create(_t);
         ASTPair __currentAST29 = currentAST.copy();
         currentAST.root = currentAST.child;
         currentAST.child = null;
         this.match(_t, 86);
         _t = _t.getFirstChild();
         this.beforeStatement("select", 45);
         AST __t30 = _t;
         AST tmp4_AST = null;
         AST tmp4_AST_in = null;
         this.astFactory.create(_t);
         ASTPair __currentAST30 = currentAST.copy();
         currentAST.root = currentAST.child;
         currentAST.child = null;
         this.match(_t, 89);
         _t = _t.getFirstChild();
         f = _t == ASTNULL ? null : _t;
         this.fromClause(_t);
         AST var26 = this._retTree;
         f_AST = this.returnAST;
         if (var26 == null) {
            var26 = ASTNULL;
         }

         switch (((AST)var26).getType()) {
            case 3:
               break;
            case 45:
               AST var33 = var26 == ASTNULL ? null : var26;
               this.selectClause((AST)var26);
               AST var27 = this._retTree;
               s_AST = this.returnAST;
               break;
            default:
               throw new NoViableAltException((AST)var26);
         }

         var26 = __t30.getNextSibling();
         if (var26 == null) {
            var26 = ASTNULL;
         }

         switch (((AST)var26).getType()) {
            case 3:
            case 24:
            case 41:
               break;
            case 53:
               AST var34 = var26 == ASTNULL ? null : var26;
               this.whereClause((AST)var26);
               var26 = this._retTree;
               w_AST = this.returnAST;
               break;
            default:
               throw new NoViableAltException((AST)var26);
         }

         if (var26 == null) {
            var26 = ASTNULL;
         }

         switch (((AST)var26).getType()) {
            case 3:
            case 41:
               break;
            case 24:
               AST var35 = var26 == ASTNULL ? null : var26;
               this.groupClause((AST)var26);
               var26 = this._retTree;
               g_AST = this.returnAST;
               break;
            default:
               throw new NoViableAltException((AST)var26);
         }

         if (var26 == null) {
            var26 = ASTNULL;
         }

         switch (((AST)var26).getType()) {
            case 3:
               break;
            case 41:
               AST var36 = var26 == ASTNULL ? null : var26;
               this.orderClause((AST)var26);
               AST var29 = this._retTree;
               o_AST = this.returnAST;
               break;
            default:
               throw new NoViableAltException((AST)var26);
         }

         _t = __t29.getNextSibling();
         AST var30 = __currentAST29.root;
         query_AST = this.astFactory.make((new ASTArray(6)).add(this.astFactory.create(45, "SELECT")).add(s_AST).add(f_AST).add(w_AST).add(g_AST).add(o_AST));
         this.beforeStatementCompletion("select");
         this.processQuery(s_AST, query_AST);
         this.afterStatementCompletion("select");
         __currentAST29.root = query_AST;
         __currentAST29.child = query_AST != null && query_AST.getFirstChild() != null ? query_AST.getFirstChild() : query_AST;
         __currentAST29.advanceChildToEnd();
      } catch (RecognitionException ex) {
         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this.returnAST = query_AST;
      this._retTree = _t;
   }

   public final void fromClause(AST _t) throws RecognitionException {
      AST fromClause_AST_in = _t == ASTNULL ? null : _t;
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST fromClause_AST = null;
      AST f = null;
      AST f_AST = null;
      this.prepareFromClauseInputTree(fromClause_AST_in);

      try {
         AST __t69 = _t;
         f = _t == ASTNULL ? null : _t;
         AST f_AST_in = null;
         f_AST = this.astFactory.create(f);
         this.astFactory.addASTChild(currentAST, f_AST);
         ASTPair __currentAST69 = currentAST.copy();
         currentAST.root = currentAST.child;
         currentAST.child = null;
         this.match(_t, 22);
         _t = _t.getFirstChild();
         fromClause_AST = currentAST.root;
         this.pushFromClause(fromClause_AST, f);
         this.handleClauseStart(22);
         this.fromElementList(_t);
         _t = this._retTree;
         this.astFactory.addASTChild(currentAST, this.returnAST);
         _t = __t69.getNextSibling();
         fromClause_AST = __currentAST69.root;
      } catch (RecognitionException ex) {
         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this.returnAST = fromClause_AST;
      this._retTree = _t;
   }

   public final void setClause(AST _t) throws RecognitionException {
      AST setClause_AST_in = _t == ASTNULL ? null : _t;
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST setClause_AST = null;

      try {
         AST __t20 = _t;
         AST tmp5_AST = null;
         AST tmp5_AST_in = null;
         tmp5_AST = this.astFactory.create(_t);
         this.astFactory.addASTChild(currentAST, tmp5_AST);
         ASTPair __currentAST20 = currentAST.copy();
         currentAST.root = currentAST.child;
         currentAST.child = null;
         this.match(_t, 46);
         AST var10 = _t.getFirstChild();
         this.handleClauseStart(46);

         while(true) {
            if (var10 == null) {
               var10 = ASTNULL;
            }

            if (((AST)var10).getType() != 102) {
               _t = __t20.getNextSibling();
               setClause_AST = __currentAST20.root;
               break;
            }

            this.assignment((AST)var10);
            var10 = this._retTree;
            this.astFactory.addASTChild(currentAST, this.returnAST);
         }
      } catch (RecognitionException ex) {
         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this.returnAST = setClause_AST;
      this._retTree = _t;
   }

   public final void whereClause(AST _t) throws RecognitionException {
      AST whereClause_AST_in = _t == ASTNULL ? null : _t;
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST whereClause_AST = null;
      AST w = null;
      AST w_AST = null;
      AST b_AST = null;
      AST b = null;

      try {
         AST __t94 = _t;
         w = _t == ASTNULL ? null : _t;
         AST w_AST_in = null;
         w_AST = this.astFactory.create(w);
         this.astFactory.addASTChild(currentAST, w_AST);
         ASTPair __currentAST94 = currentAST.copy();
         currentAST.root = currentAST.child;
         currentAST.child = null;
         this.match(_t, 53);
         _t = _t.getFirstChild();
         this.handleClauseStart(53);
         b = _t == ASTNULL ? null : _t;
         this.logicalExpr(_t);
         _t = this._retTree;
         b_AST = this.returnAST;
         this.astFactory.addASTChild(currentAST, this.returnAST);
         _t = __t94.getNextSibling();
         whereClause_AST = __currentAST94.root;
         whereClause_AST = this.astFactory.make((new ASTArray(2)).add(w_AST).add(b_AST));
         __currentAST94.root = whereClause_AST;
         __currentAST94.child = whereClause_AST != null && whereClause_AST.getFirstChild() != null ? whereClause_AST.getFirstChild() : whereClause_AST;
         __currentAST94.advanceChildToEnd();
         whereClause_AST = __currentAST94.root;
      } catch (RecognitionException ex) {
         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this.returnAST = whereClause_AST;
      this._retTree = _t;
   }

   public final void intoClause(AST _t) throws RecognitionException {
      AST intoClause_AST_in = _t == ASTNULL ? null : _t;
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST intoClause_AST = null;
      AST ps_AST = null;
      AST ps = null;
      String p = null;

      try {
         AST __t13 = _t;
         AST tmp6_AST = null;
         AST tmp6_AST_in = null;
         this.astFactory.create(_t);
         ASTPair __currentAST13 = currentAST.copy();
         currentAST.root = currentAST.child;
         currentAST.child = null;
         this.match(_t, 30);
         _t = _t.getFirstChild();
         this.handleClauseStart(30);
         p = this.path(_t);
         _t = this._retTree;
         ps = _t == ASTNULL ? null : _t;
         this.insertablePropertySpec(_t);
         _t = this._retTree;
         ps_AST = this.returnAST;
         _t = __t13.getNextSibling();
         AST var16 = __currentAST13.root;
         intoClause_AST = this.createIntoClause(p, ps);
         __currentAST13.root = intoClause_AST;
         __currentAST13.child = intoClause_AST != null && intoClause_AST.getFirstChild() != null ? intoClause_AST.getFirstChild() : intoClause_AST;
         __currentAST13.advanceChildToEnd();
      } catch (RecognitionException ex) {
         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this.returnAST = intoClause_AST;
      this._retTree = _t;
   }

   public final String path(AST _t) throws RecognitionException {
      AST path_AST_in = _t == ASTNULL ? null : _t;
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST path_AST = null;
      AST a_AST = null;
      AST a = null;
      AST y_AST = null;
      AST y = null;
      String p = "???";
      String x = "?x?";

      try {
         if (_t == null) {
            _t = ASTNULL;
         }

         switch (_t.getType()) {
            case 15:
               AST __t89 = _t;
               AST tmp7_AST = null;
               AST tmp7_AST_in = null;
               tmp7_AST = this.astFactory.create(_t);
               this.astFactory.addASTChild(currentAST, tmp7_AST);
               ASTPair __currentAST89 = currentAST.copy();
               currentAST.root = currentAST.child;
               currentAST.child = null;
               this.match(_t, 15);
               _t = _t.getFirstChild();
               x = this.path(_t);
               _t = this._retTree;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               y = _t == ASTNULL ? null : _t;
               this.identifier(_t);
               _t = this._retTree;
               y_AST = this.returnAST;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               _t = __t89.getNextSibling();
               StringBuilder buf = new StringBuilder();
               buf.append(x).append(".").append(y.getText());
               p = buf.toString();
               path_AST = __currentAST89.root;
               break;
            case 93:
            case 126:
               a = _t == ASTNULL ? null : _t;
               this.identifier(_t);
               _t = this._retTree;
               a_AST = this.returnAST;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               p = a.getText();
               path_AST = currentAST.root;
               break;
            default:
               throw new NoViableAltException(_t);
         }
      } catch (RecognitionException ex) {
         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this.returnAST = path_AST;
      this._retTree = _t;
      return p;
   }

   public final void insertablePropertySpec(AST _t) throws RecognitionException {
      AST insertablePropertySpec_AST_in = _t == ASTNULL ? null : _t;
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST insertablePropertySpec_AST = null;

      try {
         AST tmp8_AST = null;
         AST tmp8_AST_in = null;
         tmp8_AST = this.astFactory.create(_t);
         this.astFactory.addASTChild(currentAST, tmp8_AST);
         ASTPair __currentAST16 = currentAST.copy();
         currentAST.root = currentAST.child;
         currentAST.child = null;
         this.match(_t, 87);
         _t = _t.getFirstChild();
         int _cnt18 = 0;

         while(true) {
            if (_t == null) {
               _t = ASTNULL;
            }

            if (_t.getType() != 126) {
               if (_cnt18 < 1) {
                  throw new NoViableAltException(_t);
               }

               _t = _t.getNextSibling();
               insertablePropertySpec_AST = __currentAST16.root;
               break;
            }

            AST tmp9_AST = null;
            AST tmp9_AST_in = null;
            tmp9_AST = this.astFactory.create(_t);
            this.astFactory.addASTChild(currentAST, tmp9_AST);
            this.match(_t, 126);
            _t = _t.getNextSibling();
            ++_cnt18;
         }
      } catch (RecognitionException ex) {
         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this.returnAST = insertablePropertySpec_AST;
      this._retTree = _t;
   }

   public final void assignment(AST _t) throws RecognitionException {
      AST assignment_AST_in = _t == ASTNULL ? null : _t;
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST assignment_AST = null;
      AST p_AST = null;
      AST p = null;

      try {
         AST __t24 = _t;
         AST tmp10_AST = null;
         AST tmp10_AST_in = null;
         tmp10_AST = this.astFactory.create(_t);
         this.astFactory.addASTChild(currentAST, tmp10_AST);
         ASTPair __currentAST24 = currentAST.copy();
         currentAST.root = currentAST.child;
         currentAST.child = null;
         this.match(_t, 102);
         _t = _t.getFirstChild();
         p = _t == ASTNULL ? null : _t;
         this.propertyRef(_t);
         _t = this._retTree;
         p_AST = this.returnAST;
         this.astFactory.addASTChild(currentAST, this.returnAST);
         this.resolve(p_AST);
         this.newValue(_t);
         _t = this._retTree;
         this.astFactory.addASTChild(currentAST, this.returnAST);
         _t = __t24.getNextSibling();
         assignment_AST = __currentAST24.root;
         this.evaluateAssignment(assignment_AST);
         assignment_AST = __currentAST24.root;
      } catch (RecognitionException ex) {
         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this.returnAST = assignment_AST;
      this._retTree = _t;
   }

   public final void propertyRef(AST _t) throws RecognitionException {
      AST propertyRef_AST_in = _t == ASTNULL ? null : _t;
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST propertyRef_AST = null;
      AST mcr_AST = null;
      AST mcr = null;
      AST d = null;
      AST d_AST = null;
      AST lhs_AST = null;
      AST lhs = null;
      AST rhs_AST = null;
      AST rhs = null;
      AST p_AST = null;
      AST p = null;

      try {
         if (_t == null) {
            _t = ASTNULL;
         }

         switch (_t.getType()) {
            case 15:
               AST __t176 = _t;
               AST var28 = _t == ASTNULL ? null : _t;
               AST d_AST_in = null;
               d_AST = this.astFactory.create((AST)var28);
               ASTPair __currentAST176 = currentAST.copy();
               currentAST.root = currentAST.child;
               currentAST.child = null;
               this.match(_t, 15);
               _t = _t.getFirstChild();
               lhs = _t == ASTNULL ? null : _t;
               this.propertyRefLhs(_t);
               _t = this._retTree;
               lhs_AST = this.returnAST;
               rhs = _t == ASTNULL ? null : _t;
               this.propertyName(_t);
               _t = this._retTree;
               rhs_AST = this.returnAST;
               _t = __t176.getNextSibling();
               AST var24 = __currentAST176.root;
               AST var25 = this.astFactory.make((new ASTArray(3)).add(d_AST).add(lhs_AST).add(rhs_AST));
               propertyRef_AST = this.lookupProperty(var25, false, true);
               __currentAST176.root = propertyRef_AST;
               __currentAST176.child = propertyRef_AST != null && propertyRef_AST.getFirstChild() != null ? propertyRef_AST.getFirstChild() : propertyRef_AST;
               __currentAST176.advanceChildToEnd();
               break;
            case 68:
            case 69:
            case 70:
               AST var27 = _t == ASTNULL ? null : _t;
               this.mapComponentReference(_t);
               _t = this._retTree;
               mcr_AST = this.returnAST;
               propertyRef_AST = currentAST.root;
               this.resolve(mcr_AST);
               propertyRef_AST = mcr_AST;
               currentAST.root = mcr_AST;
               currentAST.child = mcr_AST != null && mcr_AST.getFirstChild() != null ? mcr_AST.getFirstChild() : mcr_AST;
               currentAST.advanceChildToEnd();
               break;
            case 93:
            case 126:
               AST var35 = _t == ASTNULL ? null : _t;
               this.identifier(_t);
               _t = this._retTree;
               p_AST = this.returnAST;
               propertyRef_AST = currentAST.root;
               if (this.isNonQualifiedPropertyRef(p_AST)) {
                  propertyRef_AST = this.lookupNonQualifiedProperty(p_AST);
               } else {
                  this.resolve(p_AST);
                  propertyRef_AST = p_AST;
               }

               currentAST.root = propertyRef_AST;
               currentAST.child = propertyRef_AST != null && propertyRef_AST.getFirstChild() != null ? propertyRef_AST.getFirstChild() : propertyRef_AST;
               currentAST.advanceChildToEnd();
               break;
            default:
               throw new NoViableAltException(_t);
         }
      } catch (RecognitionException ex) {
         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this.returnAST = propertyRef_AST;
      this._retTree = _t;
   }

   public final void newValue(AST _t) throws RecognitionException {
      AST newValue_AST_in = _t == ASTNULL ? null : _t;
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST newValue_AST = null;

      try {
         if (_t == null) {
            _t = ASTNULL;
         }

         switch (_t.getType()) {
            case 12:
            case 15:
            case 20:
            case 39:
            case 49:
            case 54:
            case 68:
            case 69:
            case 70:
            case 71:
            case 74:
            case 78:
            case 81:
            case 90:
            case 92:
            case 93:
            case 95:
            case 96:
            case 97:
            case 98:
            case 99:
            case 100:
            case 115:
            case 116:
            case 117:
            case 118:
            case 119:
            case 122:
            case 123:
            case 124:
            case 125:
            case 126:
               this.expr(_t);
               _t = this._retTree;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               newValue_AST = currentAST.root;
               break;
            case 13:
            case 14:
            case 16:
            case 17:
            case 18:
            case 19:
            case 21:
            case 22:
            case 23:
            case 24:
            case 25:
            case 26:
            case 27:
            case 28:
            case 29:
            case 30:
            case 31:
            case 32:
            case 33:
            case 34:
            case 35:
            case 36:
            case 37:
            case 38:
            case 40:
            case 41:
            case 42:
            case 43:
            case 44:
            case 45:
            case 46:
            case 47:
            case 48:
            case 50:
            case 51:
            case 52:
            case 53:
            case 55:
            case 56:
            case 57:
            case 58:
            case 59:
            case 60:
            case 61:
            case 62:
            case 63:
            case 64:
            case 65:
            case 66:
            case 67:
            case 72:
            case 73:
            case 75:
            case 76:
            case 77:
            case 79:
            case 80:
            case 82:
            case 83:
            case 84:
            case 85:
            case 87:
            case 88:
            case 89:
            case 91:
            case 94:
            case 101:
            case 102:
            case 103:
            case 104:
            case 105:
            case 106:
            case 107:
            case 108:
            case 109:
            case 110:
            case 111:
            case 112:
            case 113:
            case 114:
            case 120:
            case 121:
            default:
               throw new NoViableAltException(_t);
            case 86:
               this.query(_t);
               _t = this._retTree;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               newValue_AST = currentAST.root;
         }
      } catch (RecognitionException ex) {
         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this.returnAST = newValue_AST;
      this._retTree = _t;
   }

   public final void expr(AST _t) throws RecognitionException {
      AST expr_AST_in = _t == ASTNULL ? null : _t;
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST expr_AST = null;
      AST ae_AST = null;
      AST ae = null;

      try {
         if (_t == null) {
            _t = ASTNULL;
         }

         label49:
         switch (_t.getType()) {
            case 12:
               this.count(_t);
               _t = this._retTree;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               expr_AST = currentAST.root;
               break;
            case 13:
            case 14:
            case 16:
            case 17:
            case 18:
            case 19:
            case 21:
            case 22:
            case 23:
            case 24:
            case 25:
            case 26:
            case 27:
            case 28:
            case 29:
            case 30:
            case 31:
            case 32:
            case 33:
            case 34:
            case 35:
            case 36:
            case 37:
            case 38:
            case 40:
            case 41:
            case 42:
            case 43:
            case 44:
            case 45:
            case 46:
            case 47:
            case 48:
            case 50:
            case 51:
            case 52:
            case 53:
            case 55:
            case 56:
            case 57:
            case 58:
            case 59:
            case 60:
            case 61:
            case 62:
            case 63:
            case 64:
            case 65:
            case 66:
            case 67:
            case 72:
            case 73:
            case 75:
            case 76:
            case 77:
            case 79:
            case 80:
            case 82:
            case 83:
            case 84:
            case 85:
            case 86:
            case 87:
            case 88:
            case 89:
            case 91:
            case 94:
            case 101:
            case 102:
            case 103:
            case 104:
            case 105:
            case 106:
            case 107:
            case 108:
            case 109:
            case 110:
            case 111:
            case 112:
            case 113:
            case 114:
            case 120:
            case 121:
            default:
               throw new NoViableAltException(_t);
            case 15:
            case 68:
            case 69:
            case 70:
            case 78:
            case 93:
            case 126:
               AST var14 = _t == ASTNULL ? null : _t;
               this.addrExpr(_t, true);
               _t = this._retTree;
               ae_AST = this.returnAST;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               this.resolve(ae_AST);
               expr_AST = currentAST.root;
               break;
            case 20:
            case 39:
            case 49:
            case 95:
            case 96:
            case 97:
            case 98:
            case 99:
            case 100:
            case 124:
            case 125:
               this.constant(_t);
               _t = this._retTree;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               expr_AST = currentAST.root;
               break;
            case 54:
            case 74:
            case 90:
            case 115:
            case 116:
            case 117:
            case 118:
            case 119:
               this.arithmeticExpr(_t);
               _t = this._retTree;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               expr_AST = currentAST.root;
               break;
            case 71:
            case 81:
               this.functionCall(_t);
               _t = this._retTree;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               expr_AST = currentAST.root;
               break;
            case 92:
               AST __t133 = _t;
               AST tmp11_AST = null;
               AST tmp11_AST_in = null;
               tmp11_AST = this.astFactory.create(_t);
               this.astFactory.addASTChild(currentAST, tmp11_AST);
               ASTPair __currentAST133 = currentAST.copy();
               currentAST.root = currentAST.child;
               currentAST.child = null;
               this.match(_t, 92);
               _t = _t.getFirstChild();

               while(true) {
                  if (_t == null) {
                     _t = ASTNULL;
                  }

                  if (!_tokenSet_0.member(_t.getType())) {
                     _t = __t133.getNextSibling();
                     expr_AST = __currentAST133.root;
                     break label49;
                  }

                  this.expr(_t);
                  _t = this._retTree;
                  this.astFactory.addASTChild(currentAST, this.returnAST);
               }
            case 122:
            case 123:
               this.parameter(_t);
               _t = this._retTree;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               expr_AST = currentAST.root;
         }
      } catch (RecognitionException ex) {
         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this.returnAST = expr_AST;
      this._retTree = _t;
   }

   public final void selectClause(AST _t) throws RecognitionException {
      AST selectClause_AST_in = _t == ASTNULL ? null : _t;
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST selectClause_AST = null;
      AST d = null;
      AST d_AST = null;
      AST x_AST = null;
      AST x = null;

      try {
         AST __t49 = _t;
         AST tmp12_AST = null;
         AST tmp12_AST_in = null;
         this.astFactory.create(_t);
         ASTPair __currentAST49 = currentAST.copy();
         currentAST.root = currentAST.child;
         currentAST.child = null;
         this.match(_t, 45);
         _t = _t.getFirstChild();
         this.handleClauseStart(45);
         this.beforeSelectClause();
         if (_t == null) {
            _t = ASTNULL;
         }

         switch (_t.getType()) {
            case 5:
            case 6:
            case 8:
            case 9:
            case 10:
            case 11:
            case 13:
            case 14:
            case 18:
            case 19:
            case 20:
            case 21:
            case 22:
            case 23:
            case 24:
            case 25:
            case 26:
            case 28:
            case 29:
            case 30:
            case 31:
            case 32:
            case 33:
            case 34:
            case 35:
            case 36:
            case 37:
            case 38:
            case 39:
            case 40:
            case 41:
            case 42:
            case 43:
            case 44:
            case 45:
            case 46:
            case 47:
            case 48:
            case 49:
            case 50:
            case 51:
            case 52:
            case 53:
            case 55:
            case 56:
            case 57:
            case 58:
            case 59:
            case 60:
            case 61:
            case 62:
            case 63:
            case 64:
            case 66:
            case 67:
            case 72:
            case 75:
            case 76:
            case 77:
            case 78:
            case 79:
            case 80:
            case 82:
            case 83:
            case 84:
            case 85:
            case 87:
            case 88:
            case 89:
            case 91:
            case 92:
            case 94:
            case 100:
            case 101:
            case 102:
            case 103:
            case 104:
            case 105:
            case 106:
            case 107:
            case 108:
            case 109:
            case 110:
            case 111:
            case 112:
            case 113:
            case 114:
            case 120:
            case 121:
            case 122:
            case 123:
            default:
               throw new NoViableAltException(_t);
            case 16:
               AST d_AST_in = null;
               d_AST = this.astFactory.create(_t);
               this.match(_t, 16);
               _t = _t.getNextSibling();
            case 4:
            case 7:
            case 12:
            case 15:
            case 17:
            case 27:
            case 54:
            case 65:
            case 68:
            case 69:
            case 70:
            case 71:
            case 73:
            case 74:
            case 81:
            case 86:
            case 90:
            case 93:
            case 95:
            case 96:
            case 97:
            case 98:
            case 99:
            case 115:
            case 116:
            case 117:
            case 118:
            case 119:
            case 124:
            case 125:
            case 126:
               AST var19 = _t == ASTNULL ? null : _t;
               this.selectExprList(_t);
               _t = this._retTree;
               x_AST = this.returnAST;
               _t = __t49.getNextSibling();
               AST var17 = __currentAST49.root;
               selectClause_AST = this.astFactory.make((new ASTArray(3)).add(this.astFactory.create(137, "{select clause}")).add(d_AST).add(x_AST));
               __currentAST49.root = selectClause_AST;
               __currentAST49.child = selectClause_AST != null && selectClause_AST.getFirstChild() != null ? selectClause_AST.getFirstChild() : selectClause_AST;
               __currentAST49.advanceChildToEnd();
         }
      } catch (RecognitionException ex) {
         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this.returnAST = selectClause_AST;
      this._retTree = _t;
   }

   public final void groupClause(AST _t) throws RecognitionException {
      AST groupClause_AST_in = _t == ASTNULL ? null : _t;
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST groupClause_AST = null;

      try {
         AST tmp13_AST = null;
         AST tmp13_AST_in = null;
         tmp13_AST = this.astFactory.create(_t);
         this.astFactory.addASTChild(currentAST, tmp13_AST);
         ASTPair __currentAST43 = currentAST.copy();
         currentAST.root = currentAST.child;
         currentAST.child = null;
         this.match(_t, 24);
         _t = _t.getFirstChild();
         this.handleClauseStart(24);
         int _cnt45 = 0;

         label45:
         while(true) {
            if (_t == null) {
               _t = ASTNULL;
            }

            if (!_tokenSet_0.member(_t.getType())) {
               if (_cnt45 < 1) {
                  throw new NoViableAltException(_t);
               }

               if (_t == null) {
                  _t = ASTNULL;
               }

               switch (_t.getType()) {
                  case 25:
                     AST tmp14_AST = null;
                     AST tmp14_AST_in = null;
                     tmp14_AST = this.astFactory.create(_t);
                     this.astFactory.addASTChild(currentAST, tmp14_AST);
                     ASTPair __currentAST47 = currentAST.copy();
                     currentAST.root = currentAST.child;
                     currentAST.child = null;
                     this.match(_t, 25);
                     AST var15 = _t.getFirstChild();
                     this.logicalExpr(var15);
                     AST var16 = this._retTree;
                     this.astFactory.addASTChild(currentAST, this.returnAST);
                     _t = _t.getNextSibling();
                  case 3:
                     _t = _t.getNextSibling();
                     groupClause_AST = __currentAST43.root;
                     break label45;
                  default:
                     throw new NoViableAltException(_t);
               }
            }

            this.expr(_t);
            _t = this._retTree;
            this.astFactory.addASTChild(currentAST, this.returnAST);
            ++_cnt45;
         }
      } catch (RecognitionException ex) {
         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this.returnAST = groupClause_AST;
      this._retTree = _t;
   }

   public final void orderClause(AST _t) throws RecognitionException {
      AST orderClause_AST_in = _t == ASTNULL ? null : _t;
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST orderClause_AST = null;

      try {
         AST __t36 = _t;
         AST tmp15_AST = null;
         AST tmp15_AST_in = null;
         tmp15_AST = this.astFactory.create(_t);
         this.astFactory.addASTChild(currentAST, tmp15_AST);
         ASTPair __currentAST36 = currentAST.copy();
         currentAST.root = currentAST.child;
         currentAST.child = null;
         this.match(_t, 41);
         _t = _t.getFirstChild();
         this.handleClauseStart(41);
         this.orderExprs(_t);
         _t = this._retTree;
         this.astFactory.addASTChild(currentAST, this.returnAST);
         _t = __t36.getNextSibling();
         orderClause_AST = __currentAST36.root;
      } catch (RecognitionException ex) {
         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this.returnAST = orderClause_AST;
      this._retTree = _t;
   }

   public final void orderExprs(AST _t) throws RecognitionException {
      AST orderExprs_AST_in = _t == ASTNULL ? null : _t;
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST orderExprs_AST = null;

      try {
         this.orderExpr(_t);
         _t = this._retTree;
         this.astFactory.addASTChild(currentAST, this.returnAST);
         if (_t == null) {
            _t = ASTNULL;
         }

         switch (_t.getType()) {
            case 3:
            case 12:
            case 15:
            case 20:
            case 39:
            case 49:
            case 54:
            case 68:
            case 69:
            case 70:
            case 71:
            case 74:
            case 78:
            case 81:
            case 90:
            case 92:
            case 93:
            case 95:
            case 96:
            case 97:
            case 98:
            case 99:
            case 100:
            case 115:
            case 116:
            case 117:
            case 118:
            case 119:
            case 122:
            case 123:
            case 124:
            case 125:
            case 126:
               break;
            case 4:
            case 5:
            case 6:
            case 7:
            case 9:
            case 10:
            case 11:
            case 13:
            case 16:
            case 17:
            case 18:
            case 19:
            case 21:
            case 22:
            case 23:
            case 24:
            case 25:
            case 26:
            case 27:
            case 28:
            case 29:
            case 30:
            case 31:
            case 32:
            case 33:
            case 34:
            case 35:
            case 36:
            case 37:
            case 38:
            case 40:
            case 41:
            case 42:
            case 43:
            case 44:
            case 45:
            case 46:
            case 47:
            case 48:
            case 50:
            case 51:
            case 52:
            case 53:
            case 55:
            case 56:
            case 57:
            case 58:
            case 59:
            case 60:
            case 61:
            case 62:
            case 63:
            case 64:
            case 65:
            case 66:
            case 67:
            case 72:
            case 73:
            case 75:
            case 76:
            case 77:
            case 79:
            case 80:
            case 82:
            case 83:
            case 84:
            case 85:
            case 86:
            case 87:
            case 88:
            case 89:
            case 91:
            case 94:
            case 101:
            case 102:
            case 103:
            case 104:
            case 105:
            case 106:
            case 107:
            case 108:
            case 109:
            case 110:
            case 111:
            case 112:
            case 113:
            case 114:
            case 120:
            case 121:
            default:
               throw new NoViableAltException(_t);
            case 8:
               AST tmp16_AST = null;
               AST tmp16_AST_in = null;
               tmp16_AST = this.astFactory.create(_t);
               this.astFactory.addASTChild(currentAST, tmp16_AST);
               this.match(_t, 8);
               _t = _t.getNextSibling();
               break;
            case 14:
               AST tmp17_AST = null;
               AST tmp17_AST_in = null;
               tmp17_AST = this.astFactory.create(_t);
               this.astFactory.addASTChild(currentAST, tmp17_AST);
               this.match(_t, 14);
               _t = _t.getNextSibling();
         }

         if (_t == null) {
            _t = ASTNULL;
         }

         switch (_t.getType()) {
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 13:
            case 14:
            case 16:
            case 17:
            case 18:
            case 19:
            case 21:
            case 22:
            case 23:
            case 24:
            case 25:
            case 26:
            case 27:
            case 28:
            case 29:
            case 30:
            case 31:
            case 32:
            case 33:
            case 34:
            case 35:
            case 36:
            case 37:
            case 38:
            case 40:
            case 41:
            case 42:
            case 43:
            case 44:
            case 45:
            case 46:
            case 47:
            case 48:
            case 50:
            case 51:
            case 52:
            case 53:
            case 55:
            case 56:
            case 57:
            case 58:
            case 59:
            case 60:
            case 61:
            case 62:
            case 63:
            case 64:
            case 65:
            case 66:
            case 67:
            case 72:
            case 73:
            case 75:
            case 76:
            case 77:
            case 79:
            case 80:
            case 82:
            case 83:
            case 84:
            case 85:
            case 86:
            case 87:
            case 88:
            case 89:
            case 91:
            case 94:
            case 101:
            case 102:
            case 103:
            case 104:
            case 105:
            case 106:
            case 107:
            case 108:
            case 109:
            case 110:
            case 111:
            case 112:
            case 113:
            case 114:
            case 120:
            case 121:
            default:
               throw new NoViableAltException(_t);
            case 12:
            case 15:
            case 20:
            case 39:
            case 49:
            case 54:
            case 68:
            case 69:
            case 70:
            case 71:
            case 74:
            case 78:
            case 81:
            case 90:
            case 92:
            case 93:
            case 95:
            case 96:
            case 97:
            case 98:
            case 99:
            case 100:
            case 115:
            case 116:
            case 117:
            case 118:
            case 119:
            case 122:
            case 123:
            case 124:
            case 125:
            case 126:
               this.orderExprs(_t);
               _t = this._retTree;
               this.astFactory.addASTChild(currentAST, this.returnAST);
            case 3:
               orderExprs_AST = currentAST.root;
         }
      } catch (RecognitionException ex) {
         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this.returnAST = orderExprs_AST;
      this._retTree = _t;
   }

   public final void orderExpr(AST _t) throws RecognitionException {
      AST orderExpr_AST_in = _t == ASTNULL ? null : _t;
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST orderExpr_AST = null;

      try {
         if (_t == null) {
            _t = ASTNULL;
         }

         if ((_t.getType() == 93 || _t.getType() == 126) && this.isOrderExpressionResultVariableRef(_t)) {
            this.resultVariableRef(_t);
            _t = this._retTree;
            this.astFactory.addASTChild(currentAST, this.returnAST);
            orderExpr_AST = currentAST.root;
         } else {
            if (!_tokenSet_0.member(_t.getType())) {
               throw new NoViableAltException(_t);
            }

            this.expr(_t);
            _t = this._retTree;
            this.astFactory.addASTChild(currentAST, this.returnAST);
            orderExpr_AST = currentAST.root;
         }
      } catch (RecognitionException ex) {
         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this.returnAST = orderExpr_AST;
      this._retTree = _t;
   }

   public final void resultVariableRef(AST _t) throws RecognitionException {
      AST resultVariableRef_AST_in = _t == ASTNULL ? null : _t;
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST resultVariableRef_AST = null;
      AST i_AST = null;
      AST i = null;

      try {
         i = _t == ASTNULL ? null : _t;
         this.identifier(_t);
         _t = this._retTree;
         i_AST = this.returnAST;
         AST var9 = currentAST.root;
         resultVariableRef_AST = this.astFactory.make((new ASTArray(1)).add(this.astFactory.create(150, i.getText())));
         this.handleResultVariableRef(resultVariableRef_AST);
         currentAST.root = resultVariableRef_AST;
         currentAST.child = resultVariableRef_AST != null && resultVariableRef_AST.getFirstChild() != null ? resultVariableRef_AST.getFirstChild() : resultVariableRef_AST;
         currentAST.advanceChildToEnd();
      } catch (RecognitionException ex) {
         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this.returnAST = resultVariableRef_AST;
      this._retTree = _t;
   }

   public final void identifier(AST _t) throws RecognitionException {
      AST identifier_AST_in = _t == ASTNULL ? null : _t;
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST identifier_AST = null;

      try {
         if (_t == null) {
            _t = ASTNULL;
         }

         switch (_t.getType()) {
            case 93:
               AST tmp19_AST = null;
               AST tmp19_AST_in = null;
               tmp19_AST = this.astFactory.create(_t);
               this.astFactory.addASTChild(currentAST, tmp19_AST);
               this.match(_t, 93);
               _t = _t.getNextSibling();
               break;
            case 126:
               AST tmp18_AST = null;
               AST tmp18_AST_in = null;
               tmp18_AST = this.astFactory.create(_t);
               this.astFactory.addASTChild(currentAST, tmp18_AST);
               this.match(_t, 126);
               _t = _t.getNextSibling();
               break;
            default:
               throw new NoViableAltException(_t);
         }

         identifier_AST = currentAST.root;
      } catch (RecognitionException ex) {
         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this.returnAST = identifier_AST;
      this._retTree = _t;
   }

   public final void logicalExpr(AST _t) throws RecognitionException {
      AST logicalExpr_AST_in = _t == ASTNULL ? null : _t;
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST logicalExpr_AST = null;

      try {
         if (_t == null) {
            _t = ASTNULL;
         }

         switch (_t.getType()) {
            case 6:
               AST __t96 = _t;
               AST tmp20_AST = null;
               AST tmp20_AST_in = null;
               tmp20_AST = this.astFactory.create(_t);
               this.astFactory.addASTChild(currentAST, tmp20_AST);
               ASTPair __currentAST96 = currentAST.copy();
               currentAST.root = currentAST.child;
               currentAST.child = null;
               this.match(_t, 6);
               _t = _t.getFirstChild();
               this.logicalExpr(_t);
               _t = this._retTree;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               this.logicalExpr(_t);
               _t = this._retTree;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               _t = __t96.getNextSibling();
               logicalExpr_AST = __currentAST96.root;
               break;
            case 10:
            case 19:
            case 26:
            case 34:
            case 79:
            case 80:
            case 82:
            case 83:
            case 84:
            case 102:
            case 108:
            case 110:
            case 111:
            case 112:
            case 113:
               this.comparisonExpr(_t);
               _t = this._retTree;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               logicalExpr_AST = currentAST.root;
               break;
            case 38:
               AST __t98 = _t;
               AST tmp22_AST = null;
               AST tmp22_AST_in = null;
               tmp22_AST = this.astFactory.create(_t);
               this.astFactory.addASTChild(currentAST, tmp22_AST);
               ASTPair __currentAST98 = currentAST.copy();
               currentAST.root = currentAST.child;
               currentAST.child = null;
               this.match(_t, 38);
               _t = _t.getFirstChild();
               this.logicalExpr(_t);
               _t = this._retTree;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               _t = __t98.getNextSibling();
               logicalExpr_AST = __currentAST98.root;
               break;
            case 40:
               AST __t97 = _t;
               AST tmp21_AST = null;
               AST tmp21_AST_in = null;
               tmp21_AST = this.astFactory.create(_t);
               this.astFactory.addASTChild(currentAST, tmp21_AST);
               ASTPair __currentAST97 = currentAST.copy();
               currentAST.root = currentAST.child;
               currentAST.child = null;
               this.match(_t, 40);
               _t = _t.getFirstChild();
               this.logicalExpr(_t);
               _t = this._retTree;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               this.logicalExpr(_t);
               _t = this._retTree;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               _t = __t97.getNextSibling();
               logicalExpr_AST = __currentAST97.root;
               break;
            default:
               throw new NoViableAltException(_t);
         }
      } catch (RecognitionException ex) {
         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this.returnAST = logicalExpr_AST;
      this._retTree = _t;
   }

   public final void selectExprList(AST _t) throws RecognitionException {
      AST selectExprList_AST_in = _t == ASTNULL ? null : _t;
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST selectExprList_AST = null;
      boolean oldInSelect = this.inSelect;
      this.inSelect = true;

      try {
         int _cnt53 = 0;

         label39:
         while(true) {
            if (_t == null) {
               _t = ASTNULL;
            }

            switch (_t.getType()) {
               case 4:
               case 12:
               case 15:
               case 17:
               case 27:
               case 54:
               case 65:
               case 68:
               case 69:
               case 70:
               case 71:
               case 73:
               case 74:
               case 81:
               case 86:
               case 90:
               case 93:
               case 95:
               case 96:
               case 97:
               case 98:
               case 99:
               case 115:
               case 116:
               case 117:
               case 118:
               case 119:
               case 124:
               case 125:
               case 126:
                  this.selectExpr(_t);
                  _t = this._retTree;
                  this.astFactory.addASTChild(currentAST, this.returnAST);
                  break;
               case 5:
               case 6:
               case 8:
               case 9:
               case 10:
               case 11:
               case 13:
               case 14:
               case 16:
               case 18:
               case 19:
               case 20:
               case 21:
               case 22:
               case 23:
               case 24:
               case 25:
               case 26:
               case 28:
               case 29:
               case 30:
               case 31:
               case 32:
               case 33:
               case 34:
               case 35:
               case 36:
               case 37:
               case 38:
               case 39:
               case 40:
               case 41:
               case 42:
               case 43:
               case 44:
               case 45:
               case 46:
               case 47:
               case 48:
               case 49:
               case 50:
               case 51:
               case 52:
               case 53:
               case 55:
               case 56:
               case 57:
               case 58:
               case 59:
               case 60:
               case 61:
               case 62:
               case 63:
               case 64:
               case 66:
               case 67:
               case 72:
               case 75:
               case 76:
               case 77:
               case 78:
               case 79:
               case 80:
               case 82:
               case 83:
               case 84:
               case 85:
               case 87:
               case 88:
               case 89:
               case 91:
               case 92:
               case 94:
               case 100:
               case 101:
               case 102:
               case 103:
               case 104:
               case 105:
               case 106:
               case 107:
               case 108:
               case 109:
               case 110:
               case 111:
               case 112:
               case 113:
               case 114:
               case 120:
               case 121:
               case 122:
               case 123:
               default:
                  if (_cnt53 < 1) {
                     throw new NoViableAltException(_t);
                  }

                  this.inSelect = oldInSelect;
                  selectExprList_AST = currentAST.root;
                  break label39;
               case 7:
                  this.aliasedSelectExpr(_t);
                  _t = this._retTree;
                  this.astFactory.addASTChild(currentAST, this.returnAST);
            }

            ++_cnt53;
         }
      } catch (RecognitionException ex) {
         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this.returnAST = selectExprList_AST;
      this._retTree = _t;
   }

   public final void selectExpr(AST _t) throws RecognitionException {
      AST selectExpr_AST_in = _t == ASTNULL ? null : _t;
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST selectExpr_AST = null;
      AST p_AST = null;
      AST p = null;
      AST ar2_AST = null;
      AST ar2 = null;
      AST ar3_AST = null;
      AST ar3 = null;
      AST con_AST = null;
      AST con = null;

      try {
         if (_t == null) {
            _t = ASTNULL;
         }

         switch (_t.getType()) {
            case 4:
               AST __t57 = _t;
               AST tmp23_AST = null;
               AST tmp23_AST_in = null;
               tmp23_AST = this.astFactory.create(_t);
               this.astFactory.addASTChild(currentAST, tmp23_AST);
               ASTPair __currentAST57 = currentAST.copy();
               currentAST.root = currentAST.child;
               currentAST.child = null;
               this.match(_t, 4);
               _t = _t.getFirstChild();
               ar2 = _t == ASTNULL ? null : _t;
               this.aliasRef(_t);
               _t = this._retTree;
               ar2_AST = this.returnAST;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               _t = __t57.getNextSibling();
               selectExpr_AST = __currentAST57.root;
               this.resolveSelectExpression(ar2_AST);
               __currentAST57.root = ar2_AST;
               __currentAST57.child = ar2_AST != null && ar2_AST.getFirstChild() != null ? ar2_AST.getFirstChild() : ar2_AST;
               __currentAST57.advanceChildToEnd();
               selectExpr_AST = __currentAST57.root;
               break;
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 13:
            case 14:
            case 16:
            case 18:
            case 19:
            case 20:
            case 21:
            case 22:
            case 23:
            case 24:
            case 25:
            case 26:
            case 28:
            case 29:
            case 30:
            case 31:
            case 32:
            case 33:
            case 34:
            case 35:
            case 36:
            case 37:
            case 38:
            case 39:
            case 40:
            case 41:
            case 42:
            case 43:
            case 44:
            case 45:
            case 46:
            case 47:
            case 48:
            case 49:
            case 50:
            case 51:
            case 52:
            case 53:
            case 55:
            case 56:
            case 57:
            case 58:
            case 59:
            case 60:
            case 61:
            case 62:
            case 63:
            case 64:
            case 66:
            case 67:
            case 72:
            case 75:
            case 76:
            case 77:
            case 78:
            case 79:
            case 80:
            case 82:
            case 83:
            case 84:
            case 85:
            case 87:
            case 88:
            case 89:
            case 91:
            case 92:
            case 94:
            case 100:
            case 101:
            case 102:
            case 103:
            case 104:
            case 105:
            case 106:
            case 107:
            case 108:
            case 109:
            case 110:
            case 111:
            case 112:
            case 113:
            case 114:
            case 120:
            case 121:
            case 122:
            case 123:
            default:
               throw new NoViableAltException(_t);
            case 12:
               this.count(_t);
               _t = this._retTree;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               selectExpr_AST = currentAST.root;
               break;
            case 15:
            case 68:
            case 69:
            case 70:
            case 93:
            case 126:
               AST var25 = _t == ASTNULL ? null : _t;
               this.propertyRef(_t);
               _t = this._retTree;
               p_AST = this.returnAST;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               this.resolveSelectExpression(p_AST);
               selectExpr_AST = currentAST.root;
               break;
            case 17:
            case 27:
               this.collectionFunction(_t);
               _t = this._retTree;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               selectExpr_AST = currentAST.root;
               break;
            case 54:
            case 74:
            case 90:
            case 115:
            case 116:
            case 117:
            case 118:
            case 119:
               this.arithmeticExpr(_t);
               _t = this._retTree;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               selectExpr_AST = currentAST.root;
               break;
            case 65:
               AST __t58 = _t;
               AST tmp24_AST = null;
               AST tmp24_AST_in = null;
               tmp24_AST = this.astFactory.create(_t);
               this.astFactory.addASTChild(currentAST, tmp24_AST);
               ASTPair __currentAST58 = currentAST.copy();
               currentAST.root = currentAST.child;
               currentAST.child = null;
               this.match(_t, 65);
               _t = _t.getFirstChild();
               ar3 = _t == ASTNULL ? null : _t;
               this.aliasRef(_t);
               _t = this._retTree;
               ar3_AST = this.returnAST;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               _t = __t58.getNextSibling();
               selectExpr_AST = __currentAST58.root;
               this.resolveSelectExpression(ar3_AST);
               __currentAST58.root = ar3_AST;
               __currentAST58.child = ar3_AST != null && ar3_AST.getFirstChild() != null ? ar3_AST.getFirstChild() : ar3_AST;
               __currentAST58.advanceChildToEnd();
               selectExpr_AST = __currentAST58.root;
               break;
            case 71:
            case 81:
               this.functionCall(_t);
               _t = this._retTree;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               selectExpr_AST = currentAST.root;
               break;
            case 73:
               AST var31 = _t == ASTNULL ? null : _t;
               this.constructor(_t);
               _t = this._retTree;
               con_AST = this.returnAST;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               this.processConstructor(con_AST);
               selectExpr_AST = currentAST.root;
               break;
            case 86:
               this.query(_t);
               _t = this._retTree;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               selectExpr_AST = currentAST.root;
               break;
            case 95:
            case 96:
            case 97:
            case 98:
            case 99:
            case 124:
            case 125:
               this.literal(_t);
               _t = this._retTree;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               selectExpr_AST = currentAST.root;
         }
      } catch (RecognitionException ex) {
         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this.returnAST = selectExpr_AST;
      this._retTree = _t;
   }

   public final void aliasedSelectExpr(AST _t) throws RecognitionException {
      AST aliasedSelectExpr_AST_in = _t == ASTNULL ? null : _t;
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST aliasedSelectExpr_AST = null;
      AST se_AST = null;
      AST se = null;
      AST i_AST = null;
      AST i = null;

      try {
         AST __t55 = _t;
         AST tmp25_AST = null;
         AST tmp25_AST_in = null;
         this.astFactory.create(_t);
         ASTPair __currentAST55 = currentAST.copy();
         currentAST.root = currentAST.child;
         currentAST.child = null;
         this.match(_t, 7);
         _t = _t.getFirstChild();
         se = _t == ASTNULL ? null : _t;
         this.selectExpr(_t);
         _t = this._retTree;
         se_AST = this.returnAST;
         i = _t == ASTNULL ? null : _t;
         this.identifier(_t);
         _t = this._retTree;
         i_AST = this.returnAST;
         _t = __t55.getNextSibling();
         aliasedSelectExpr_AST = __currentAST55.root;
         this.setAlias(se_AST, i_AST);
         aliasedSelectExpr_AST = se_AST;
         __currentAST55.root = se_AST;
         __currentAST55.child = se_AST != null && se_AST.getFirstChild() != null ? se_AST.getFirstChild() : se_AST;
         __currentAST55.advanceChildToEnd();
      } catch (RecognitionException ex) {
         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this.returnAST = aliasedSelectExpr_AST;
      this._retTree = _t;
   }

   public final void aliasRef(AST _t) throws RecognitionException {
      AST aliasRef_AST_in = _t == ASTNULL ? null : _t;
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST aliasRef_AST = null;
      AST i_AST = null;
      AST i = null;

      try {
         i = _t == ASTNULL ? null : _t;
         this.identifier(_t);
         _t = this._retTree;
         i_AST = this.returnAST;
         AST var9 = currentAST.root;
         aliasRef_AST = this.astFactory.make((new ASTArray(1)).add(this.astFactory.create(140, i.getText())));
         this.lookupAlias(aliasRef_AST);
         currentAST.root = aliasRef_AST;
         currentAST.child = aliasRef_AST != null && aliasRef_AST.getFirstChild() != null ? aliasRef_AST.getFirstChild() : aliasRef_AST;
         currentAST.advanceChildToEnd();
      } catch (RecognitionException ex) {
         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this.returnAST = aliasRef_AST;
      this._retTree = _t;
   }

   public final void constructor(AST _t) throws RecognitionException {
      AST constructor_AST_in = _t == ASTNULL ? null : _t;
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST constructor_AST = null;
      String className = null;

      try {
         AST __t64 = _t;
         AST tmp26_AST = null;
         AST tmp26_AST_in = null;
         tmp26_AST = this.astFactory.create(_t);
         this.astFactory.addASTChild(currentAST, tmp26_AST);
         ASTPair __currentAST64 = currentAST.copy();
         currentAST.root = currentAST.child;
         currentAST.child = null;
         this.match(_t, 73);
         _t = _t.getFirstChild();
         this.path(_t);
         AST var12 = this._retTree;
         this.astFactory.addASTChild(currentAST, this.returnAST);

         label33:
         while(true) {
            if (var12 == null) {
               var12 = ASTNULL;
            }

            switch (((AST)var12).getType()) {
               case 4:
               case 12:
               case 15:
               case 17:
               case 27:
               case 54:
               case 65:
               case 68:
               case 69:
               case 70:
               case 71:
               case 73:
               case 74:
               case 81:
               case 86:
               case 90:
               case 93:
               case 95:
               case 96:
               case 97:
               case 98:
               case 99:
               case 115:
               case 116:
               case 117:
               case 118:
               case 119:
               case 124:
               case 125:
               case 126:
                  this.selectExpr((AST)var12);
                  var12 = this._retTree;
                  this.astFactory.addASTChild(currentAST, this.returnAST);
                  break;
               case 5:
               case 6:
               case 8:
               case 9:
               case 10:
               case 11:
               case 13:
               case 14:
               case 16:
               case 18:
               case 19:
               case 20:
               case 21:
               case 22:
               case 23:
               case 24:
               case 25:
               case 26:
               case 28:
               case 29:
               case 30:
               case 31:
               case 32:
               case 33:
               case 34:
               case 35:
               case 36:
               case 37:
               case 38:
               case 39:
               case 40:
               case 41:
               case 42:
               case 43:
               case 44:
               case 45:
               case 46:
               case 47:
               case 48:
               case 49:
               case 50:
               case 51:
               case 52:
               case 53:
               case 55:
               case 56:
               case 57:
               case 58:
               case 59:
               case 60:
               case 61:
               case 62:
               case 63:
               case 64:
               case 66:
               case 67:
               case 72:
               case 75:
               case 76:
               case 77:
               case 78:
               case 79:
               case 80:
               case 82:
               case 83:
               case 84:
               case 85:
               case 87:
               case 88:
               case 89:
               case 91:
               case 92:
               case 94:
               case 100:
               case 101:
               case 102:
               case 103:
               case 104:
               case 105:
               case 106:
               case 107:
               case 108:
               case 109:
               case 110:
               case 111:
               case 112:
               case 113:
               case 114:
               case 120:
               case 121:
               case 122:
               case 123:
               default:
                  _t = __t64.getNextSibling();
                  constructor_AST = __currentAST64.root;
                  break label33;
               case 7:
                  this.aliasedSelectExpr((AST)var12);
                  var12 = this._retTree;
                  this.astFactory.addASTChild(currentAST, this.returnAST);
            }
         }
      } catch (RecognitionException ex) {
         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this.returnAST = constructor_AST;
      this._retTree = _t;
   }

   public final void functionCall(AST _t) throws RecognitionException {
      AST functionCall_AST_in = _t == ASTNULL ? null : _t;
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST functionCall_AST = null;

      try {
         if (_t == null) {
            _t = ASTNULL;
         }

         label51:
         switch (_t.getType()) {
            case 71:
               AST __t165 = _t;
               AST tmp29_AST = null;
               AST tmp29_AST_in = null;
               tmp29_AST = this.astFactory.create(_t);
               this.astFactory.addASTChild(currentAST, tmp29_AST);
               ASTPair __currentAST165 = currentAST.copy();
               currentAST.root = currentAST.child;
               currentAST.child = null;
               this.match(_t, 71);
               _t = _t.getFirstChild();
               this.aggregateExpr(_t);
               _t = this._retTree;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               _t = __t165.getNextSibling();
               functionCall_AST = __currentAST165.root;
               break;
            case 81:
               AST __t160 = _t;
               AST tmp27_AST = null;
               AST tmp27_AST_in = null;
               tmp27_AST = this.astFactory.create(_t);
               this.astFactory.addASTChild(currentAST, tmp27_AST);
               ASTPair __currentAST160 = currentAST.copy();
               currentAST.root = currentAST.child;
               currentAST.child = null;
               this.match(_t, 81);
               _t = _t.getFirstChild();
               this.inFunctionCall = true;
               this.pathAsIdent(_t);
               _t = this._retTree;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               if (_t == null) {
                  _t = ASTNULL;
               }

               switch (_t.getType()) {
                  case 75:
                     AST __t162 = _t;
                     AST tmp28_AST = null;
                     AST tmp28_AST_in = null;
                     tmp28_AST = this.astFactory.create(_t);
                     this.astFactory.addASTChild(currentAST, tmp28_AST);
                     ASTPair __currentAST162 = currentAST.copy();
                     currentAST.root = currentAST.child;
                     currentAST.child = null;
                     this.match(_t, 75);
                     AST var16 = _t.getFirstChild();

                     while(true) {
                        if (var16 == null) {
                           var16 = ASTNULL;
                        }

                        if (!_tokenSet_1.member(((AST)var16).getType())) {
                           AST var17 = __t162.getNextSibling();
                           break;
                        }

                        this.exprOrSubquery((AST)var16);
                        var16 = this._retTree;
                        this.astFactory.addASTChild(currentAST, this.returnAST);
                     }
                  case 3:
                     _t = __t160.getNextSibling();
                     functionCall_AST = __currentAST160.root;
                     this.processFunction(functionCall_AST, this.inSelect);
                     this.inFunctionCall = false;
                     functionCall_AST = __currentAST160.root;
                     break label51;
                  default:
                     throw new NoViableAltException(_t);
               }
            default:
               throw new NoViableAltException(_t);
         }
      } catch (RecognitionException ex) {
         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this.returnAST = functionCall_AST;
      this._retTree = _t;
   }

   public final void count(AST _t) throws RecognitionException {
      AST count_AST_in = _t == ASTNULL ? null : _t;
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST count_AST = null;

      try {
         AST __t60 = _t;
         AST tmp30_AST = null;
         AST tmp30_AST_in = null;
         tmp30_AST = this.astFactory.create(_t);
         this.astFactory.addASTChild(currentAST, tmp30_AST);
         ASTPair __currentAST60 = currentAST.copy();
         currentAST.root = currentAST.child;
         currentAST.child = null;
         this.match(_t, 12);
         _t = _t.getFirstChild();
         this.inCount = true;
         if (_t == null) {
            _t = ASTNULL;
         }

         switch (_t.getType()) {
            case 4:
               AST tmp32_AST = null;
               AST tmp32_AST_in = null;
               tmp32_AST = this.astFactory.create(_t);
               this.astFactory.addASTChild(currentAST, tmp32_AST);
               this.match(_t, 4);
               _t = _t.getNextSibling();
               break;
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 13:
            case 14:
            case 18:
            case 19:
            case 21:
            case 22:
            case 23:
            case 24:
            case 25:
            case 26:
            case 28:
            case 29:
            case 30:
            case 31:
            case 32:
            case 33:
            case 34:
            case 35:
            case 36:
            case 37:
            case 38:
            case 40:
            case 41:
            case 42:
            case 43:
            case 44:
            case 45:
            case 46:
            case 47:
            case 48:
            case 50:
            case 51:
            case 52:
            case 53:
            case 55:
            case 56:
            case 57:
            case 58:
            case 59:
            case 60:
            case 61:
            case 62:
            case 63:
            case 64:
            case 65:
            case 66:
            case 67:
            case 72:
            case 73:
            case 75:
            case 76:
            case 77:
            case 79:
            case 80:
            case 82:
            case 83:
            case 84:
            case 85:
            case 86:
            case 87:
            case 89:
            case 91:
            case 94:
            case 101:
            case 102:
            case 103:
            case 104:
            case 105:
            case 106:
            case 107:
            case 108:
            case 109:
            case 110:
            case 111:
            case 112:
            case 113:
            case 114:
            case 120:
            case 121:
            default:
               throw new NoViableAltException(_t);
            case 12:
            case 15:
            case 17:
            case 20:
            case 27:
            case 39:
            case 49:
            case 54:
            case 68:
            case 69:
            case 70:
            case 71:
            case 74:
            case 78:
            case 81:
            case 88:
            case 90:
            case 92:
            case 93:
            case 95:
            case 96:
            case 97:
            case 98:
            case 99:
            case 100:
            case 115:
            case 116:
            case 117:
            case 118:
            case 119:
            case 122:
            case 123:
            case 124:
            case 125:
            case 126:
               break;
            case 16:
               AST tmp31_AST = null;
               AST tmp31_AST_in = null;
               tmp31_AST = this.astFactory.create(_t);
               this.astFactory.addASTChild(currentAST, tmp31_AST);
               this.match(_t, 16);
               _t = _t.getNextSibling();
               this.inCountDistinct = true;
         }

         if (_t == null) {
            _t = ASTNULL;
         }

         switch (_t.getType()) {
            case 12:
            case 15:
            case 17:
            case 20:
            case 27:
            case 39:
            case 49:
            case 54:
            case 68:
            case 69:
            case 70:
            case 71:
            case 74:
            case 78:
            case 81:
            case 90:
            case 92:
            case 93:
            case 95:
            case 96:
            case 97:
            case 98:
            case 99:
            case 100:
            case 115:
            case 116:
            case 117:
            case 118:
            case 119:
            case 122:
            case 123:
            case 124:
            case 125:
            case 126:
               this.aggregateExpr(_t);
               _t = this._retTree;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               break;
            case 13:
            case 14:
            case 16:
            case 18:
            case 19:
            case 21:
            case 22:
            case 23:
            case 24:
            case 25:
            case 26:
            case 28:
            case 29:
            case 30:
            case 31:
            case 32:
            case 33:
            case 34:
            case 35:
            case 36:
            case 37:
            case 38:
            case 40:
            case 41:
            case 42:
            case 43:
            case 44:
            case 45:
            case 46:
            case 47:
            case 48:
            case 50:
            case 51:
            case 52:
            case 53:
            case 55:
            case 56:
            case 57:
            case 58:
            case 59:
            case 60:
            case 61:
            case 62:
            case 63:
            case 64:
            case 65:
            case 66:
            case 67:
            case 72:
            case 73:
            case 75:
            case 76:
            case 77:
            case 79:
            case 80:
            case 82:
            case 83:
            case 84:
            case 85:
            case 86:
            case 87:
            case 89:
            case 91:
            case 94:
            case 101:
            case 102:
            case 103:
            case 104:
            case 105:
            case 106:
            case 107:
            case 108:
            case 109:
            case 110:
            case 111:
            case 112:
            case 113:
            case 114:
            case 120:
            case 121:
            default:
               throw new NoViableAltException(_t);
            case 88:
               AST tmp33_AST = null;
               AST tmp33_AST_in = null;
               tmp33_AST = this.astFactory.create(_t);
               this.astFactory.addASTChild(currentAST, tmp33_AST);
               this.match(_t, 88);
               _t = _t.getNextSibling();
         }

         _t = __t60.getNextSibling();
         this.inCount = false;
         this.inCountDistinct = false;
         count_AST = __currentAST60.root;
      } catch (RecognitionException ex) {
         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this.returnAST = count_AST;
      this._retTree = _t;
   }

   public final void collectionFunction(AST _t) throws RecognitionException {
      AST collectionFunction_AST_in = _t == ASTNULL ? null : _t;
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST collectionFunction_AST = null;
      AST e = null;
      AST e_AST = null;
      AST p1_AST = null;
      AST p1 = null;
      AST i = null;
      AST i_AST = null;
      AST p2_AST = null;
      AST p2 = null;

      try {
         if (_t == null) {
            _t = ASTNULL;
         }

         switch (_t.getType()) {
            case 17:
               AST __t157 = _t;
               AST var21 = _t == ASTNULL ? null : _t;
               AST e_AST_in = null;
               e_AST = this.astFactory.create((AST)var21);
               this.astFactory.addASTChild(currentAST, e_AST);
               ASTPair __currentAST157 = currentAST.copy();
               currentAST.root = currentAST.child;
               currentAST.child = null;
               this.match(_t, 17);
               _t = _t.getFirstChild();
               this.inFunctionCall = true;
               p1 = _t == ASTNULL ? null : _t;
               this.propertyRef(_t);
               _t = this._retTree;
               p1_AST = this.returnAST;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               this.resolve(p1_AST);
               _t = __t157.getNextSibling();
               this.processFunction(e_AST, this.inSelect);
               this.inFunctionCall = false;
               collectionFunction_AST = __currentAST157.root;
               break;
            case 27:
               AST __t158 = _t;
               AST var25 = _t == ASTNULL ? null : _t;
               AST i_AST_in = null;
               i_AST = this.astFactory.create((AST)var25);
               this.astFactory.addASTChild(currentAST, i_AST);
               ASTPair __currentAST158 = currentAST.copy();
               currentAST.root = currentAST.child;
               currentAST.child = null;
               this.match(_t, 27);
               _t = _t.getFirstChild();
               this.inFunctionCall = true;
               p2 = _t == ASTNULL ? null : _t;
               this.propertyRef(_t);
               _t = this._retTree;
               p2_AST = this.returnAST;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               this.resolve(p2_AST);
               _t = __t158.getNextSibling();
               this.processFunction(i_AST, this.inSelect);
               this.inFunctionCall = false;
               collectionFunction_AST = __currentAST158.root;
               break;
            default:
               throw new NoViableAltException(_t);
         }
      } catch (RecognitionException ex) {
         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this.returnAST = collectionFunction_AST;
      this._retTree = _t;
   }

   public final void literal(AST _t) throws RecognitionException {
      AST literal_AST_in = _t == ASTNULL ? null : _t;
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST literal_AST = null;

      try {
         if (_t == null) {
            _t = ASTNULL;
         }

         switch (_t.getType()) {
            case 95:
               AST tmp37_AST = null;
               AST tmp37_AST_in = null;
               tmp37_AST = this.astFactory.create(_t);
               this.astFactory.addASTChild(currentAST, tmp37_AST);
               this.match(_t, 95);
               _t = _t.getNextSibling();
               literal_AST = currentAST.root;
               this.processNumericLiteral(literal_AST);
               literal_AST = currentAST.root;
               break;
            case 96:
               AST tmp36_AST = null;
               AST tmp36_AST_in = null;
               tmp36_AST = this.astFactory.create(_t);
               this.astFactory.addASTChild(currentAST, tmp36_AST);
               this.match(_t, 96);
               _t = _t.getNextSibling();
               literal_AST = currentAST.root;
               this.processNumericLiteral(literal_AST);
               literal_AST = currentAST.root;
               break;
            case 97:
               AST tmp35_AST = null;
               AST tmp35_AST_in = null;
               tmp35_AST = this.astFactory.create(_t);
               this.astFactory.addASTChild(currentAST, tmp35_AST);
               this.match(_t, 97);
               _t = _t.getNextSibling();
               literal_AST = currentAST.root;
               this.processNumericLiteral(literal_AST);
               literal_AST = currentAST.root;
               break;
            case 98:
               AST tmp38_AST = null;
               AST tmp38_AST_in = null;
               tmp38_AST = this.astFactory.create(_t);
               this.astFactory.addASTChild(currentAST, tmp38_AST);
               this.match(_t, 98);
               _t = _t.getNextSibling();
               literal_AST = currentAST.root;
               this.processNumericLiteral(literal_AST);
               literal_AST = currentAST.root;
               break;
            case 99:
               AST tmp39_AST = null;
               AST tmp39_AST_in = null;
               tmp39_AST = this.astFactory.create(_t);
               this.astFactory.addASTChild(currentAST, tmp39_AST);
               this.match(_t, 99);
               _t = _t.getNextSibling();
               literal_AST = currentAST.root;
               this.processNumericLiteral(literal_AST);
               literal_AST = currentAST.root;
               break;
            case 124:
               AST tmp34_AST = null;
               AST tmp34_AST_in = null;
               tmp34_AST = this.astFactory.create(_t);
               this.astFactory.addASTChild(currentAST, tmp34_AST);
               this.match(_t, 124);
               _t = _t.getNextSibling();
               literal_AST = currentAST.root;
               this.processNumericLiteral(literal_AST);
               literal_AST = currentAST.root;
               break;
            case 125:
               AST tmp40_AST = null;
               AST tmp40_AST_in = null;
               tmp40_AST = this.astFactory.create(_t);
               this.astFactory.addASTChild(currentAST, tmp40_AST);
               this.match(_t, 125);
               _t = _t.getNextSibling();
               literal_AST = currentAST.root;
               break;
            default:
               throw new NoViableAltException(_t);
         }
      } catch (RecognitionException ex) {
         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this.returnAST = literal_AST;
      this._retTree = _t;
   }

   public final void arithmeticExpr(AST _t) throws RecognitionException {
      AST arithmeticExpr_AST_in = _t == ASTNULL ? null : _t;
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST arithmeticExpr_AST = null;

      try {
         if (_t == null) {
            _t = ASTNULL;
         }

         switch (_t.getType()) {
            case 54:
            case 74:
               this.caseExpr(_t);
               _t = this._retTree;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               arithmeticExpr_AST = currentAST.root;
               break;
            case 90:
               AST __t142 = _t;
               AST tmp46_AST = null;
               AST tmp46_AST_in = null;
               tmp46_AST = this.astFactory.create(_t);
               this.astFactory.addASTChild(currentAST, tmp46_AST);
               ASTPair __currentAST142 = currentAST.copy();
               currentAST.root = currentAST.child;
               currentAST.child = null;
               this.match(_t, 90);
               _t = _t.getFirstChild();
               this.expr(_t);
               _t = this._retTree;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               _t = __t142.getNextSibling();
               arithmeticExpr_AST = __currentAST142.root;
               this.prepareArithmeticOperator(arithmeticExpr_AST);
               arithmeticExpr_AST = __currentAST142.root;
               break;
            case 115:
               AST __t137 = _t;
               AST tmp41_AST = null;
               AST tmp41_AST_in = null;
               tmp41_AST = this.astFactory.create(_t);
               this.astFactory.addASTChild(currentAST, tmp41_AST);
               ASTPair __currentAST137 = currentAST.copy();
               currentAST.root = currentAST.child;
               currentAST.child = null;
               this.match(_t, 115);
               _t = _t.getFirstChild();
               this.exprOrSubquery(_t);
               _t = this._retTree;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               this.exprOrSubquery(_t);
               _t = this._retTree;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               _t = __t137.getNextSibling();
               arithmeticExpr_AST = __currentAST137.root;
               this.prepareArithmeticOperator(arithmeticExpr_AST);
               arithmeticExpr_AST = __currentAST137.root;
               break;
            case 116:
               AST __t138 = _t;
               AST tmp42_AST = null;
               AST tmp42_AST_in = null;
               tmp42_AST = this.astFactory.create(_t);
               this.astFactory.addASTChild(currentAST, tmp42_AST);
               ASTPair __currentAST138 = currentAST.copy();
               currentAST.root = currentAST.child;
               currentAST.child = null;
               this.match(_t, 116);
               _t = _t.getFirstChild();
               this.exprOrSubquery(_t);
               _t = this._retTree;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               this.exprOrSubquery(_t);
               _t = this._retTree;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               _t = __t138.getNextSibling();
               arithmeticExpr_AST = __currentAST138.root;
               this.prepareArithmeticOperator(arithmeticExpr_AST);
               arithmeticExpr_AST = __currentAST138.root;
               break;
            case 117:
               AST __t141 = _t;
               AST tmp45_AST = null;
               AST tmp45_AST_in = null;
               tmp45_AST = this.astFactory.create(_t);
               this.astFactory.addASTChild(currentAST, tmp45_AST);
               ASTPair __currentAST141 = currentAST.copy();
               currentAST.root = currentAST.child;
               currentAST.child = null;
               this.match(_t, 117);
               _t = _t.getFirstChild();
               this.exprOrSubquery(_t);
               _t = this._retTree;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               this.exprOrSubquery(_t);
               _t = this._retTree;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               _t = __t141.getNextSibling();
               arithmeticExpr_AST = __currentAST141.root;
               this.prepareArithmeticOperator(arithmeticExpr_AST);
               arithmeticExpr_AST = __currentAST141.root;
               break;
            case 118:
               AST __t139 = _t;
               AST tmp43_AST = null;
               AST tmp43_AST_in = null;
               tmp43_AST = this.astFactory.create(_t);
               this.astFactory.addASTChild(currentAST, tmp43_AST);
               ASTPair __currentAST139 = currentAST.copy();
               currentAST.root = currentAST.child;
               currentAST.child = null;
               this.match(_t, 118);
               _t = _t.getFirstChild();
               this.exprOrSubquery(_t);
               _t = this._retTree;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               this.exprOrSubquery(_t);
               _t = this._retTree;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               _t = __t139.getNextSibling();
               arithmeticExpr_AST = __currentAST139.root;
               this.prepareArithmeticOperator(arithmeticExpr_AST);
               arithmeticExpr_AST = __currentAST139.root;
               break;
            case 119:
               AST __t140 = _t;
               AST tmp44_AST = null;
               AST tmp44_AST_in = null;
               tmp44_AST = this.astFactory.create(_t);
               this.astFactory.addASTChild(currentAST, tmp44_AST);
               ASTPair __currentAST140 = currentAST.copy();
               currentAST.root = currentAST.child;
               currentAST.child = null;
               this.match(_t, 119);
               _t = _t.getFirstChild();
               this.exprOrSubquery(_t);
               _t = this._retTree;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               this.exprOrSubquery(_t);
               _t = this._retTree;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               _t = __t140.getNextSibling();
               arithmeticExpr_AST = __currentAST140.root;
               this.prepareArithmeticOperator(arithmeticExpr_AST);
               arithmeticExpr_AST = __currentAST140.root;
               break;
            default:
               throw new NoViableAltException(_t);
         }
      } catch (RecognitionException ex) {
         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this.returnAST = arithmeticExpr_AST;
      this._retTree = _t;
   }

   public final void aggregateExpr(AST _t) throws RecognitionException {
      AST aggregateExpr_AST_in = _t == ASTNULL ? null : _t;
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST aggregateExpr_AST = null;

      try {
         if (_t == null) {
            _t = ASTNULL;
         }

         switch (_t.getType()) {
            case 12:
            case 15:
            case 20:
            case 39:
            case 49:
            case 54:
            case 68:
            case 69:
            case 70:
            case 71:
            case 74:
            case 78:
            case 81:
            case 90:
            case 92:
            case 93:
            case 95:
            case 96:
            case 97:
            case 98:
            case 99:
            case 100:
            case 115:
            case 116:
            case 117:
            case 118:
            case 119:
            case 122:
            case 123:
            case 124:
            case 125:
            case 126:
               this.expr(_t);
               _t = this._retTree;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               aggregateExpr_AST = currentAST.root;
               break;
            case 13:
            case 14:
            case 16:
            case 18:
            case 19:
            case 21:
            case 22:
            case 23:
            case 24:
            case 25:
            case 26:
            case 28:
            case 29:
            case 30:
            case 31:
            case 32:
            case 33:
            case 34:
            case 35:
            case 36:
            case 37:
            case 38:
            case 40:
            case 41:
            case 42:
            case 43:
            case 44:
            case 45:
            case 46:
            case 47:
            case 48:
            case 50:
            case 51:
            case 52:
            case 53:
            case 55:
            case 56:
            case 57:
            case 58:
            case 59:
            case 60:
            case 61:
            case 62:
            case 63:
            case 64:
            case 65:
            case 66:
            case 67:
            case 72:
            case 73:
            case 75:
            case 76:
            case 77:
            case 79:
            case 80:
            case 82:
            case 83:
            case 84:
            case 85:
            case 86:
            case 87:
            case 88:
            case 89:
            case 91:
            case 94:
            case 101:
            case 102:
            case 103:
            case 104:
            case 105:
            case 106:
            case 107:
            case 108:
            case 109:
            case 110:
            case 111:
            case 112:
            case 113:
            case 114:
            case 120:
            case 121:
            default:
               throw new NoViableAltException(_t);
            case 17:
            case 27:
               this.collectionFunction(_t);
               _t = this._retTree;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               aggregateExpr_AST = currentAST.root;
         }
      } catch (RecognitionException ex) {
         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this.returnAST = aggregateExpr_AST;
      this._retTree = _t;
   }

   public final void fromElementList(AST _t) throws RecognitionException {
      AST fromElementList_AST_in = _t == ASTNULL ? null : _t;
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST fromElementList_AST = null;
      boolean oldInFrom = this.inFrom;
      this.inFrom = true;

      try {
         int _cnt72 = 0;

         while(true) {
            if (_t == null) {
               _t = ASTNULL;
            }

            if (_t.getType() != 32 && _t.getType() != 76 && _t.getType() != 87) {
               if (_cnt72 < 1) {
                  throw new NoViableAltException(_t);
               }

               this.inFrom = oldInFrom;
               fromElementList_AST = currentAST.root;
               break;
            }

            this.fromElement(_t);
            _t = this._retTree;
            this.astFactory.addASTChild(currentAST, this.returnAST);
            ++_cnt72;
         }
      } catch (RecognitionException ex) {
         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this.returnAST = fromElementList_AST;
      this._retTree = _t;
   }

   public final void fromElement(AST _t) throws RecognitionException {
      AST fromElement_AST_in = _t == ASTNULL ? null : _t;
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST fromElement_AST = null;
      AST a = null;
      AST a_AST = null;
      AST pf = null;
      AST pf_AST = null;
      AST je_AST = null;
      AST je = null;
      AST fe = null;
      AST fe_AST = null;
      AST a3 = null;
      AST a3_AST = null;
      String p = null;

      try {
         if (_t == null) {
            _t = ASTNULL;
         }

         switch (_t.getType()) {
            case 32:
               AST var32 = _t == ASTNULL ? null : _t;
               this.joinElement(_t);
               _t = this._retTree;
               je_AST = this.returnAST;
               fromElement_AST = currentAST.root;
               fromElement_AST = je_AST;
               currentAST.root = je_AST;
               currentAST.child = je_AST != null && je_AST.getFirstChild() != null ? je_AST.getFirstChild() : je_AST;
               currentAST.advanceChildToEnd();
               break;
            case 76:
               AST var33 = _t;
               AST fe_AST_in = null;
               fe_AST = this.astFactory.create(_t);
               this.match(_t, 76);
               _t = _t.getNextSibling();
               a3 = _t;
               AST a3_AST_in = null;
               a3_AST = this.astFactory.create(_t);
               this.match(_t, 72);
               _t = _t.getNextSibling();
               AST var27 = currentAST.root;
               fromElement_AST = this.createFromFilterElement((AST)var33, a3);
               currentAST.root = fromElement_AST;
               currentAST.child = fromElement_AST != null && fromElement_AST.getFirstChild() != null ? fromElement_AST.getFirstChild() : fromElement_AST;
               currentAST.advanceChildToEnd();
               break;
            case 87:
               AST __t74 = _t;
               AST tmp47_AST = null;
               AST tmp47_AST_in = null;
               this.astFactory.create(_t);
               ASTPair __currentAST74 = currentAST.copy();
               currentAST.root = currentAST.child;
               currentAST.child = null;
               this.match(_t, 87);
               _t = _t.getFirstChild();
               p = this.path(_t);
               _t = this._retTree;
               if (_t == null) {
                  _t = ASTNULL;
               }

               switch (_t.getType()) {
                  case 3:
                  case 21:
                     break;
                  case 72:
                     a = _t;
                     AST a_AST_in = null;
                     a_AST = this.astFactory.create(_t);
                     this.match(_t, 72);
                     _t = _t.getNextSibling();
                     break;
                  default:
                     throw new NoViableAltException(_t);
               }

               if (_t == null) {
                  _t = ASTNULL;
               }

               switch (_t.getType()) {
                  case 3:
                     break;
                  case 21:
                     pf = _t;
                     AST pf_AST_in = null;
                     pf_AST = this.astFactory.create(_t);
                     this.match(_t, 21);
                     _t = _t.getNextSibling();
                     break;
                  default:
                     throw new NoViableAltException(_t);
               }

               _t = __t74.getNextSibling();
               AST var26 = __currentAST74.root;
               fromElement_AST = this.createFromElement(p, a, pf);
               __currentAST74.root = fromElement_AST;
               __currentAST74.child = fromElement_AST != null && fromElement_AST.getFirstChild() != null ? fromElement_AST.getFirstChild() : fromElement_AST;
               __currentAST74.advanceChildToEnd();
               break;
            default:
               throw new NoViableAltException(_t);
         }
      } catch (RecognitionException ex) {
         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this.returnAST = fromElement_AST;
      this._retTree = _t;
   }

   public final void joinElement(AST _t) throws RecognitionException {
      AST joinElement_AST_in = _t == ASTNULL ? null : _t;
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST joinElement_AST = null;
      AST f = null;
      AST f_AST = null;
      AST ref_AST = null;
      AST ref = null;
      AST a = null;
      AST a_AST = null;
      AST pf = null;
      AST pf_AST = null;
      AST with = null;
      AST with_AST = null;
      int j = 28;

      try {
         AST __t78 = _t;
         AST tmp48_AST = null;
         AST tmp48_AST_in = null;
         this.astFactory.create(_t);
         ASTPair __currentAST78 = currentAST.copy();
         currentAST.root = currentAST.child;
         currentAST.child = null;
         this.match(_t, 32);
         _t = _t.getFirstChild();
         if (_t == null) {
            _t = ASTNULL;
         }

         label62:
         switch (_t.getType()) {
            case 23:
            case 28:
            case 33:
            case 44:
               j = this.joinType(_t);
               _t = this._retTree;
               this.setImpliedJoinType(j);
            case 15:
            case 21:
            case 68:
            case 69:
            case 70:
            case 93:
            case 126:
               if (_t == null) {
                  _t = ASTNULL;
               }

               switch (_t.getType()) {
                  case 21:
                     f = _t;
                     AST f_AST_in = null;
                     f_AST = this.astFactory.create(_t);
                     this.match(_t, 21);
                     _t = _t.getNextSibling();
                  case 15:
                  case 68:
                  case 69:
                  case 70:
                  case 93:
                  case 126:
                     AST var27 = _t == ASTNULL ? null : _t;
                     this.propertyRef(_t);
                     _t = this._retTree;
                     ref_AST = this.returnAST;
                     if (_t == null) {
                        _t = ASTNULL;
                     }

                     switch (_t.getType()) {
                        case 72:
                           a = _t;
                           AST a_AST_in = null;
                           a_AST = this.astFactory.create(_t);
                           this.match(_t, 72);
                           _t = _t.getNextSibling();
                        case 3:
                        case 21:
                        case 60:
                           if (_t == null) {
                              _t = ASTNULL;
                           }

                           switch (_t.getType()) {
                              case 21:
                                 pf = _t;
                                 AST pf_AST_in = null;
                                 pf_AST = this.astFactory.create(_t);
                                 this.match(_t, 21);
                                 _t = _t.getNextSibling();
                              case 3:
                              case 60:
                                 if (_t == null) {
                                    _t = ASTNULL;
                                 }

                                 switch (_t.getType()) {
                                    case 60:
                                       with = _t;
                                       AST with_AST_in = null;
                                       with_AST = this.astFactory.create(_t);
                                       this.match(_t, 60);
                                       _t = _t.getNextSibling();
                                    case 3:
                                       _t = __t78.getNextSibling();
                                       this.createFromJoinElement(ref_AST, a, j, f, pf, with);
                                       this.setImpliedJoinType(28);
                                       break label62;
                                    default:
                                       throw new NoViableAltException(_t);
                                 }
                              default:
                                 throw new NoViableAltException(_t);
                           }
                        default:
                           throw new NoViableAltException(_t);
                     }
                  default:
                     throw new NoViableAltException(_t);
               }
            default:
               throw new NoViableAltException(_t);
         }
      } catch (RecognitionException ex) {
         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this.returnAST = joinElement_AST;
      this._retTree = _t;
   }

   public final int joinType(AST _t) throws RecognitionException {
      AST joinType_AST_in = _t == ASTNULL ? null : _t;
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST joinType_AST = null;
      AST left = null;
      AST left_AST = null;
      AST right = null;
      AST right_AST = null;
      AST outer = null;
      AST outer_AST = null;
      int j = 28;

      try {
         if (_t == null) {
            _t = ASTNULL;
         }

         label50:
         switch (_t.getType()) {
            case 23:
               AST tmp49_AST = null;
               AST tmp49_AST_in = null;
               tmp49_AST = this.astFactory.create(_t);
               this.astFactory.addASTChild(currentAST, tmp49_AST);
               this.match(_t, 23);
               _t = _t.getNextSibling();
               j = 23;
               joinType_AST = currentAST.root;
               break;
            case 28:
               AST tmp50_AST = null;
               AST tmp50_AST_in = null;
               tmp50_AST = this.astFactory.create(_t);
               this.astFactory.addASTChild(currentAST, tmp50_AST);
               this.match(_t, 28);
               _t = _t.getNextSibling();
               j = 28;
               joinType_AST = currentAST.root;
               break;
            case 33:
            case 44:
               if (_t == null) {
                  _t = ASTNULL;
               }

               switch (_t.getType()) {
                  case 33:
                     left = _t;
                     AST left_AST_in = null;
                     left_AST = this.astFactory.create(_t);
                     this.astFactory.addASTChild(currentAST, left_AST);
                     this.match(_t, 33);
                     _t = _t.getNextSibling();
                     break;
                  case 44:
                     right = _t;
                     AST right_AST_in = null;
                     right_AST = this.astFactory.create(_t);
                     this.astFactory.addASTChild(currentAST, right_AST);
                     this.match(_t, 44);
                     _t = _t.getNextSibling();
                     break;
                  default:
                     throw new NoViableAltException(_t);
               }

               if (_t == null) {
                  _t = ASTNULL;
               }

               switch (_t.getType()) {
                  case 42:
                     outer = _t;
                     AST outer_AST_in = null;
                     outer_AST = this.astFactory.create(_t);
                     this.astFactory.addASTChild(currentAST, outer_AST);
                     this.match(_t, 42);
                     _t = _t.getNextSibling();
                  case 15:
                  case 21:
                  case 68:
                  case 69:
                  case 70:
                  case 93:
                  case 126:
                     if (left != null) {
                        j = 138;
                     } else if (right != null) {
                        j = 139;
                     } else if (outer != null) {
                        j = 139;
                     }

                     joinType_AST = currentAST.root;
                     break label50;
                  default:
                     throw new NoViableAltException(_t);
               }
            default:
               throw new NoViableAltException(_t);
         }
      } catch (RecognitionException ex) {
         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this.returnAST = joinType_AST;
      this._retTree = _t;
      return j;
   }

   public final void pathAsIdent(AST _t) throws RecognitionException {
      AST pathAsIdent_AST_in = _t == ASTNULL ? null : _t;
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST pathAsIdent_AST = null;
      String text = "?text?";

      try {
         text = this.path(_t);
         _t = this._retTree;
         this.astFactory.addASTChild(currentAST, this.returnAST);
         pathAsIdent_AST = currentAST.root;
         pathAsIdent_AST = this.astFactory.make((new ASTArray(1)).add(this.astFactory.create(126, text)));
         currentAST.root = pathAsIdent_AST;
         currentAST.child = pathAsIdent_AST != null && pathAsIdent_AST.getFirstChild() != null ? pathAsIdent_AST.getFirstChild() : pathAsIdent_AST;
         currentAST.advanceChildToEnd();
         pathAsIdent_AST = currentAST.root;
      } catch (RecognitionException ex) {
         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this.returnAST = pathAsIdent_AST;
      this._retTree = _t;
   }

   public final void withClause(AST _t) throws RecognitionException {
      AST withClause_AST_in = _t == ASTNULL ? null : _t;
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST withClause_AST = null;
      AST w = null;
      AST w_AST = null;
      AST b_AST = null;
      AST b = null;

      try {
         AST __t92 = _t;
         w = _t == ASTNULL ? null : _t;
         AST w_AST_in = null;
         w_AST = this.astFactory.create(w);
         this.astFactory.addASTChild(currentAST, w_AST);
         ASTPair __currentAST92 = currentAST.copy();
         currentAST.root = currentAST.child;
         currentAST.child = null;
         this.match(_t, 60);
         _t = _t.getFirstChild();
         this.handleClauseStart(60);
         b = _t == ASTNULL ? null : _t;
         this.logicalExpr(_t);
         _t = this._retTree;
         b_AST = this.returnAST;
         this.astFactory.addASTChild(currentAST, this.returnAST);
         _t = __t92.getNextSibling();
         withClause_AST = __currentAST92.root;
         withClause_AST = this.astFactory.make((new ASTArray(2)).add(w_AST).add(b_AST));
         __currentAST92.root = withClause_AST;
         __currentAST92.child = withClause_AST != null && withClause_AST.getFirstChild() != null ? withClause_AST.getFirstChild() : withClause_AST;
         __currentAST92.advanceChildToEnd();
         withClause_AST = __currentAST92.root;
      } catch (RecognitionException ex) {
         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this.returnAST = withClause_AST;
      this._retTree = _t;
   }

   public final void comparisonExpr(AST _t) throws RecognitionException {
      AST comparisonExpr_AST_in = _t == ASTNULL ? null : _t;
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST comparisonExpr_AST = null;

      try {
         if (_t == null) {
            _t = ASTNULL;
         }

         label62:
         switch (_t.getType()) {
            case 10:
               AST __t113 = _t;
               AST tmp61_AST = null;
               AST tmp61_AST_in = null;
               tmp61_AST = this.astFactory.create(_t);
               this.astFactory.addASTChild(currentAST, tmp61_AST);
               ASTPair __currentAST113 = currentAST.copy();
               currentAST.root = currentAST.child;
               currentAST.child = null;
               this.match(_t, 10);
               _t = _t.getFirstChild();
               this.exprOrSubquery(_t);
               _t = this._retTree;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               this.exprOrSubquery(_t);
               _t = this._retTree;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               this.exprOrSubquery(_t);
               _t = this._retTree;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               currentAST = __currentAST113;
               _t = __t113.getNextSibling();
               break;
            case 19:
               AST __t119 = _t;
               AST tmp67_AST = null;
               AST tmp67_AST_in = null;
               tmp67_AST = this.astFactory.create(_t);
               this.astFactory.addASTChild(currentAST, tmp67_AST);
               ASTPair __currentAST119 = currentAST.copy();
               currentAST.root = currentAST.child;
               currentAST.child = null;
               this.match(_t, 19);
               AST var58 = _t.getFirstChild();
               if (var58 == null) {
                  var58 = ASTNULL;
               }

               switch (((AST)var58).getType()) {
                  case 12:
                  case 15:
                  case 20:
                  case 39:
                  case 49:
                  case 54:
                  case 68:
                  case 69:
                  case 70:
                  case 71:
                  case 74:
                  case 78:
                  case 81:
                  case 90:
                  case 92:
                  case 93:
                  case 95:
                  case 96:
                  case 97:
                  case 98:
                  case 99:
                  case 100:
                  case 115:
                  case 116:
                  case 117:
                  case 118:
                  case 119:
                  case 122:
                  case 123:
                  case 124:
                  case 125:
                  case 126:
                     this.expr((AST)var58);
                     AST var60 = this._retTree;
                     this.astFactory.addASTChild(currentAST, this.returnAST);
                     break;
                  case 13:
                  case 14:
                  case 16:
                  case 18:
                  case 19:
                  case 21:
                  case 22:
                  case 23:
                  case 24:
                  case 25:
                  case 26:
                  case 28:
                  case 29:
                  case 30:
                  case 31:
                  case 32:
                  case 33:
                  case 34:
                  case 35:
                  case 36:
                  case 37:
                  case 38:
                  case 40:
                  case 41:
                  case 42:
                  case 43:
                  case 44:
                  case 45:
                  case 46:
                  case 47:
                  case 48:
                  case 50:
                  case 51:
                  case 52:
                  case 53:
                  case 55:
                  case 56:
                  case 57:
                  case 58:
                  case 59:
                  case 60:
                  case 61:
                  case 62:
                  case 63:
                  case 64:
                  case 65:
                  case 66:
                  case 67:
                  case 72:
                  case 73:
                  case 75:
                  case 76:
                  case 77:
                  case 79:
                  case 80:
                  case 82:
                  case 83:
                  case 84:
                  case 85:
                  case 87:
                  case 88:
                  case 89:
                  case 91:
                  case 94:
                  case 101:
                  case 102:
                  case 103:
                  case 104:
                  case 105:
                  case 106:
                  case 107:
                  case 108:
                  case 109:
                  case 110:
                  case 111:
                  case 112:
                  case 113:
                  case 114:
                  case 120:
                  case 121:
                  default:
                     throw new NoViableAltException((AST)var58);
                  case 17:
                  case 27:
                  case 86:
                     this.collectionFunctionOrSubselect((AST)var58);
                     AST var59 = this._retTree;
                     this.astFactory.addASTChild(currentAST, this.returnAST);
               }

               currentAST = __currentAST119;
               _t = __t119.getNextSibling();
               break;
            case 26:
               AST __t115 = _t;
               AST tmp63_AST = null;
               AST tmp63_AST_in = null;
               tmp63_AST = this.astFactory.create(_t);
               this.astFactory.addASTChild(currentAST, tmp63_AST);
               ASTPair __currentAST115 = currentAST.copy();
               currentAST.root = currentAST.child;
               currentAST.child = null;
               this.match(_t, 26);
               _t = _t.getFirstChild();
               this.exprOrSubquery(_t);
               _t = this._retTree;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               this.inRhs(_t);
               _t = this._retTree;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               currentAST = __currentAST115;
               _t = __t115.getNextSibling();
               break;
            case 34:
               AST __t107 = _t;
               AST tmp57_AST = null;
               AST tmp57_AST_in = null;
               tmp57_AST = this.astFactory.create(_t);
               this.astFactory.addASTChild(currentAST, tmp57_AST);
               ASTPair __currentAST107 = currentAST.copy();
               currentAST.root = currentAST.child;
               currentAST.child = null;
               this.match(_t, 34);
               _t = _t.getFirstChild();
               this.exprOrSubquery(_t);
               _t = this._retTree;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               this.expr(_t);
               _t = this._retTree;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               if (_t == null) {
                  _t = ASTNULL;
               }

               switch (_t.getType()) {
                  case 18:
                     AST __t109 = _t;
                     AST tmp58_AST = null;
                     AST tmp58_AST_in = null;
                     tmp58_AST = this.astFactory.create(_t);
                     this.astFactory.addASTChild(currentAST, tmp58_AST);
                     ASTPair __currentAST109 = currentAST.copy();
                     currentAST.root = currentAST.child;
                     currentAST.child = null;
                     this.match(_t, 18);
                     _t = _t.getFirstChild();
                     this.expr(_t);
                     _t = this._retTree;
                     this.astFactory.addASTChild(currentAST, this.returnAST);
                     _t = __t109.getNextSibling();
                  case 3:
                     currentAST = __currentAST107;
                     _t = __t107.getNextSibling();
                     break label62;
                  default:
                     throw new NoViableAltException(_t);
               }
            case 79:
               AST __t118 = _t;
               AST tmp66_AST = null;
               AST tmp66_AST_in = null;
               tmp66_AST = this.astFactory.create(_t);
               this.astFactory.addASTChild(currentAST, tmp66_AST);
               ASTPair __currentAST118 = currentAST.copy();
               currentAST.root = currentAST.child;
               currentAST.child = null;
               this.match(_t, 79);
               _t = _t.getFirstChild();
               this.exprOrSubquery(_t);
               _t = this._retTree;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               currentAST = __currentAST118;
               _t = __t118.getNextSibling();
               break;
            case 80:
               AST __t117 = _t;
               AST tmp65_AST = null;
               AST tmp65_AST_in = null;
               tmp65_AST = this.astFactory.create(_t);
               this.astFactory.addASTChild(currentAST, tmp65_AST);
               ASTPair __currentAST117 = currentAST.copy();
               currentAST.root = currentAST.child;
               currentAST.child = null;
               this.match(_t, 80);
               _t = _t.getFirstChild();
               this.exprOrSubquery(_t);
               _t = this._retTree;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               currentAST = __currentAST117;
               _t = __t117.getNextSibling();
               break;
            case 82:
               AST __t114 = _t;
               AST tmp62_AST = null;
               AST tmp62_AST_in = null;
               tmp62_AST = this.astFactory.create(_t);
               this.astFactory.addASTChild(currentAST, tmp62_AST);
               ASTPair __currentAST114 = currentAST.copy();
               currentAST.root = currentAST.child;
               currentAST.child = null;
               this.match(_t, 82);
               _t = _t.getFirstChild();
               this.exprOrSubquery(_t);
               _t = this._retTree;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               this.exprOrSubquery(_t);
               _t = this._retTree;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               this.exprOrSubquery(_t);
               _t = this._retTree;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               currentAST = __currentAST114;
               _t = __t114.getNextSibling();
               break;
            case 83:
               AST __t116 = _t;
               AST tmp64_AST = null;
               AST tmp64_AST_in = null;
               tmp64_AST = this.astFactory.create(_t);
               this.astFactory.addASTChild(currentAST, tmp64_AST);
               ASTPair __currentAST116 = currentAST.copy();
               currentAST.root = currentAST.child;
               currentAST.child = null;
               this.match(_t, 83);
               _t = _t.getFirstChild();
               this.exprOrSubquery(_t);
               _t = this._retTree;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               this.inRhs(_t);
               _t = this._retTree;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               currentAST = __currentAST116;
               _t = __t116.getNextSibling();
               break;
            case 84:
               AST __t110 = _t;
               AST tmp59_AST = null;
               AST tmp59_AST_in = null;
               tmp59_AST = this.astFactory.create(_t);
               this.astFactory.addASTChild(currentAST, tmp59_AST);
               ASTPair __currentAST110 = currentAST.copy();
               currentAST.root = currentAST.child;
               currentAST.child = null;
               this.match(_t, 84);
               _t = _t.getFirstChild();
               this.exprOrSubquery(_t);
               _t = this._retTree;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               this.expr(_t);
               _t = this._retTree;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               if (_t == null) {
                  _t = ASTNULL;
               }

               switch (_t.getType()) {
                  case 18:
                     AST __t112 = _t;
                     AST tmp60_AST = null;
                     AST tmp60_AST_in = null;
                     tmp60_AST = this.astFactory.create(_t);
                     this.astFactory.addASTChild(currentAST, tmp60_AST);
                     ASTPair __currentAST112 = currentAST.copy();
                     currentAST.root = currentAST.child;
                     currentAST.child = null;
                     this.match(_t, 18);
                     _t = _t.getFirstChild();
                     this.expr(_t);
                     _t = this._retTree;
                     this.astFactory.addASTChild(currentAST, this.returnAST);
                     _t = __t112.getNextSibling();
                  case 3:
                     currentAST = __currentAST110;
                     _t = __t110.getNextSibling();
                     break label62;
                  default:
                     throw new NoViableAltException(_t);
               }
            case 102:
               AST __t101 = _t;
               AST tmp51_AST = null;
               AST tmp51_AST_in = null;
               tmp51_AST = this.astFactory.create(_t);
               this.astFactory.addASTChild(currentAST, tmp51_AST);
               ASTPair __currentAST101 = currentAST.copy();
               currentAST.root = currentAST.child;
               currentAST.child = null;
               this.match(_t, 102);
               _t = _t.getFirstChild();
               this.exprOrSubquery(_t);
               _t = this._retTree;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               this.exprOrSubquery(_t);
               _t = this._retTree;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               currentAST = __currentAST101;
               _t = __t101.getNextSibling();
               break;
            case 108:
               AST __t102 = _t;
               AST tmp52_AST = null;
               AST tmp52_AST_in = null;
               tmp52_AST = this.astFactory.create(_t);
               this.astFactory.addASTChild(currentAST, tmp52_AST);
               ASTPair __currentAST102 = currentAST.copy();
               currentAST.root = currentAST.child;
               currentAST.child = null;
               this.match(_t, 108);
               _t = _t.getFirstChild();
               this.exprOrSubquery(_t);
               _t = this._retTree;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               this.exprOrSubquery(_t);
               _t = this._retTree;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               currentAST = __currentAST102;
               _t = __t102.getNextSibling();
               break;
            case 110:
               AST __t103 = _t;
               AST tmp53_AST = null;
               AST tmp53_AST_in = null;
               tmp53_AST = this.astFactory.create(_t);
               this.astFactory.addASTChild(currentAST, tmp53_AST);
               ASTPair __currentAST103 = currentAST.copy();
               currentAST.root = currentAST.child;
               currentAST.child = null;
               this.match(_t, 110);
               _t = _t.getFirstChild();
               this.exprOrSubquery(_t);
               _t = this._retTree;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               this.exprOrSubquery(_t);
               _t = this._retTree;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               currentAST = __currentAST103;
               _t = __t103.getNextSibling();
               break;
            case 111:
               AST __t104 = _t;
               AST tmp54_AST = null;
               AST tmp54_AST_in = null;
               tmp54_AST = this.astFactory.create(_t);
               this.astFactory.addASTChild(currentAST, tmp54_AST);
               ASTPair __currentAST104 = currentAST.copy();
               currentAST.root = currentAST.child;
               currentAST.child = null;
               this.match(_t, 111);
               _t = _t.getFirstChild();
               this.exprOrSubquery(_t);
               _t = this._retTree;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               this.exprOrSubquery(_t);
               _t = this._retTree;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               currentAST = __currentAST104;
               _t = __t104.getNextSibling();
               break;
            case 112:
               AST __t105 = _t;
               AST tmp55_AST = null;
               AST tmp55_AST_in = null;
               tmp55_AST = this.astFactory.create(_t);
               this.astFactory.addASTChild(currentAST, tmp55_AST);
               ASTPair __currentAST105 = currentAST.copy();
               currentAST.root = currentAST.child;
               currentAST.child = null;
               this.match(_t, 112);
               _t = _t.getFirstChild();
               this.exprOrSubquery(_t);
               _t = this._retTree;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               this.exprOrSubquery(_t);
               _t = this._retTree;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               currentAST = __currentAST105;
               _t = __t105.getNextSibling();
               break;
            case 113:
               AST __t106 = _t;
               AST tmp56_AST = null;
               AST tmp56_AST_in = null;
               tmp56_AST = this.astFactory.create(_t);
               this.astFactory.addASTChild(currentAST, tmp56_AST);
               ASTPair __currentAST106 = currentAST.copy();
               currentAST.root = currentAST.child;
               currentAST.child = null;
               this.match(_t, 113);
               _t = _t.getFirstChild();
               this.exprOrSubquery(_t);
               _t = this._retTree;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               this.exprOrSubquery(_t);
               _t = this._retTree;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               currentAST = __currentAST106;
               _t = __t106.getNextSibling();
               break;
            default:
               throw new NoViableAltException(_t);
         }

         comparisonExpr_AST = currentAST.root;
         this.prepareLogicOperator(comparisonExpr_AST);
         comparisonExpr_AST = currentAST.root;
      } catch (RecognitionException ex) {
         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this.returnAST = comparisonExpr_AST;
      this._retTree = _t;
   }

   public final void exprOrSubquery(AST _t) throws RecognitionException {
      AST exprOrSubquery_AST_in = _t == ASTNULL ? null : _t;
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST exprOrSubquery_AST = null;

      try {
         if (_t == null) {
            _t = ASTNULL;
         }

         switch (_t.getType()) {
            case 4:
               AST __t129 = _t;
               AST tmp69_AST = null;
               AST tmp69_AST_in = null;
               tmp69_AST = this.astFactory.create(_t);
               this.astFactory.addASTChild(currentAST, tmp69_AST);
               ASTPair __currentAST129 = currentAST.copy();
               currentAST.root = currentAST.child;
               currentAST.child = null;
               this.match(_t, 4);
               _t = _t.getFirstChild();
               this.collectionFunctionOrSubselect(_t);
               _t = this._retTree;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               _t = __t129.getNextSibling();
               exprOrSubquery_AST = __currentAST129.root;
               break;
            case 5:
               AST __t128 = _t;
               AST tmp68_AST = null;
               AST tmp68_AST_in = null;
               tmp68_AST = this.astFactory.create(_t);
               this.astFactory.addASTChild(currentAST, tmp68_AST);
               ASTPair __currentAST128 = currentAST.copy();
               currentAST.root = currentAST.child;
               currentAST.child = null;
               this.match(_t, 5);
               _t = _t.getFirstChild();
               this.collectionFunctionOrSubselect(_t);
               _t = this._retTree;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               _t = __t128.getNextSibling();
               exprOrSubquery_AST = __currentAST128.root;
               break;
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 13:
            case 14:
            case 16:
            case 17:
            case 18:
            case 19:
            case 21:
            case 22:
            case 23:
            case 24:
            case 25:
            case 26:
            case 27:
            case 28:
            case 29:
            case 30:
            case 31:
            case 32:
            case 33:
            case 34:
            case 35:
            case 36:
            case 37:
            case 38:
            case 40:
            case 41:
            case 42:
            case 43:
            case 44:
            case 45:
            case 46:
            case 48:
            case 50:
            case 51:
            case 52:
            case 53:
            case 55:
            case 56:
            case 57:
            case 58:
            case 59:
            case 60:
            case 61:
            case 62:
            case 63:
            case 64:
            case 65:
            case 66:
            case 67:
            case 72:
            case 73:
            case 75:
            case 76:
            case 77:
            case 79:
            case 80:
            case 82:
            case 83:
            case 84:
            case 85:
            case 87:
            case 88:
            case 89:
            case 91:
            case 94:
            case 101:
            case 102:
            case 103:
            case 104:
            case 105:
            case 106:
            case 107:
            case 108:
            case 109:
            case 110:
            case 111:
            case 112:
            case 113:
            case 114:
            case 120:
            case 121:
            default:
               throw new NoViableAltException(_t);
            case 12:
            case 15:
            case 20:
            case 39:
            case 49:
            case 54:
            case 68:
            case 69:
            case 70:
            case 71:
            case 74:
            case 78:
            case 81:
            case 90:
            case 92:
            case 93:
            case 95:
            case 96:
            case 97:
            case 98:
            case 99:
            case 100:
            case 115:
            case 116:
            case 117:
            case 118:
            case 119:
            case 122:
            case 123:
            case 124:
            case 125:
            case 126:
               this.expr(_t);
               _t = this._retTree;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               exprOrSubquery_AST = currentAST.root;
               break;
            case 47:
               AST __t130 = _t;
               AST tmp70_AST = null;
               AST tmp70_AST_in = null;
               tmp70_AST = this.astFactory.create(_t);
               this.astFactory.addASTChild(currentAST, tmp70_AST);
               ASTPair __currentAST130 = currentAST.copy();
               currentAST.root = currentAST.child;
               currentAST.child = null;
               this.match(_t, 47);
               _t = _t.getFirstChild();
               this.collectionFunctionOrSubselect(_t);
               _t = this._retTree;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               _t = __t130.getNextSibling();
               exprOrSubquery_AST = __currentAST130.root;
               break;
            case 86:
               this.query(_t);
               _t = this._retTree;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               exprOrSubquery_AST = currentAST.root;
         }
      } catch (RecognitionException ex) {
         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this.returnAST = exprOrSubquery_AST;
      this._retTree = _t;
   }

   public final void inRhs(AST _t) throws RecognitionException {
      AST inRhs_AST_in = _t == ASTNULL ? null : _t;
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST inRhs_AST = null;

      try {
         AST __t122 = _t;
         AST tmp71_AST = null;
         AST tmp71_AST_in = null;
         tmp71_AST = this.astFactory.create(_t);
         this.astFactory.addASTChild(currentAST, tmp71_AST);
         ASTPair __currentAST122 = currentAST.copy();
         currentAST.root = currentAST.child;
         currentAST.child = null;
         this.match(_t, 77);
         AST var10 = _t.getFirstChild();
         if (var10 == null) {
            var10 = ASTNULL;
         }

         label37:
         switch (((AST)var10).getType()) {
            case 3:
            case 12:
            case 15:
            case 20:
            case 39:
            case 49:
            case 54:
            case 68:
            case 69:
            case 70:
            case 71:
            case 74:
            case 78:
            case 81:
            case 90:
            case 92:
            case 93:
            case 95:
            case 96:
            case 97:
            case 98:
            case 99:
            case 100:
            case 115:
            case 116:
            case 117:
            case 118:
            case 119:
            case 122:
            case 123:
            case 124:
            case 125:
            case 126:
               while(true) {
                  if (var10 == null) {
                     var10 = ASTNULL;
                  }

                  if (!_tokenSet_0.member(((AST)var10).getType())) {
                     break label37;
                  }

                  this.expr((AST)var10);
                  var10 = this._retTree;
                  this.astFactory.addASTChild(currentAST, this.returnAST);
               }
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 13:
            case 14:
            case 16:
            case 18:
            case 19:
            case 21:
            case 22:
            case 23:
            case 24:
            case 25:
            case 26:
            case 28:
            case 29:
            case 30:
            case 31:
            case 32:
            case 33:
            case 34:
            case 35:
            case 36:
            case 37:
            case 38:
            case 40:
            case 41:
            case 42:
            case 43:
            case 44:
            case 45:
            case 46:
            case 47:
            case 48:
            case 50:
            case 51:
            case 52:
            case 53:
            case 55:
            case 56:
            case 57:
            case 58:
            case 59:
            case 60:
            case 61:
            case 62:
            case 63:
            case 64:
            case 65:
            case 66:
            case 67:
            case 72:
            case 73:
            case 75:
            case 76:
            case 77:
            case 79:
            case 80:
            case 82:
            case 83:
            case 84:
            case 85:
            case 87:
            case 88:
            case 89:
            case 91:
            case 94:
            case 101:
            case 102:
            case 103:
            case 104:
            case 105:
            case 106:
            case 107:
            case 108:
            case 109:
            case 110:
            case 111:
            case 112:
            case 113:
            case 114:
            case 120:
            case 121:
            default:
               throw new NoViableAltException((AST)var10);
            case 17:
            case 27:
            case 86:
               this.collectionFunctionOrSubselect((AST)var10);
               AST var11 = this._retTree;
               this.astFactory.addASTChild(currentAST, this.returnAST);
         }

         _t = __t122.getNextSibling();
         inRhs_AST = __currentAST122.root;
      } catch (RecognitionException ex) {
         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this.returnAST = inRhs_AST;
      this._retTree = _t;
   }

   public final void collectionFunctionOrSubselect(AST _t) throws RecognitionException {
      AST collectionFunctionOrSubselect_AST_in = _t == ASTNULL ? null : _t;
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST collectionFunctionOrSubselect_AST = null;

      try {
         if (_t == null) {
            _t = ASTNULL;
         }

         switch (_t.getType()) {
            case 17:
            case 27:
               this.collectionFunction(_t);
               _t = this._retTree;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               collectionFunctionOrSubselect_AST = currentAST.root;
               break;
            case 86:
               this.query(_t);
               _t = this._retTree;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               collectionFunctionOrSubselect_AST = currentAST.root;
               break;
            default:
               throw new NoViableAltException(_t);
         }
      } catch (RecognitionException ex) {
         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this.returnAST = collectionFunctionOrSubselect_AST;
      this._retTree = _t;
   }

   public final void addrExpr(AST _t, boolean root) throws RecognitionException {
      AST addrExpr_AST_in = _t == ASTNULL ? null : _t;
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST addrExpr_AST = null;
      AST d = null;
      AST d_AST = null;
      AST lhs_AST = null;
      AST lhs = null;
      AST rhs_AST = null;
      AST rhs = null;
      AST i = null;
      AST i_AST = null;
      AST lhs2_AST = null;
      AST lhs2 = null;
      AST rhs2_AST = null;
      AST rhs2 = null;
      AST mcr_AST = null;
      AST mcr = null;
      AST p_AST = null;
      AST p = null;

      try {
         if (_t == null) {
            _t = ASTNULL;
         }

         switch (_t.getType()) {
            case 15:
               AST __t171 = _t;
               AST var37 = _t == ASTNULL ? null : _t;
               AST d_AST_in = null;
               d_AST = this.astFactory.create((AST)var37);
               ASTPair __currentAST171 = currentAST.copy();
               currentAST.root = currentAST.child;
               currentAST.child = null;
               this.match(_t, 15);
               _t = _t.getFirstChild();
               lhs = _t == ASTNULL ? null : _t;
               this.addrExprLhs(_t);
               _t = this._retTree;
               lhs_AST = this.returnAST;
               rhs = _t == ASTNULL ? null : _t;
               this.propertyName(_t);
               _t = this._retTree;
               rhs_AST = this.returnAST;
               _t = __t171.getNextSibling();
               AST var35 = __currentAST171.root;
               AST var36 = this.astFactory.make((new ASTArray(3)).add(d_AST).add(lhs_AST).add(rhs_AST));
               addrExpr_AST = this.lookupProperty(var36, root, false);
               __currentAST171.root = addrExpr_AST;
               __currentAST171.child = addrExpr_AST != null && addrExpr_AST.getFirstChild() != null ? addrExpr_AST.getFirstChild() : addrExpr_AST;
               __currentAST171.advanceChildToEnd();
               break;
            case 68:
            case 69:
            case 70:
               AST var50 = _t == ASTNULL ? null : _t;
               this.mapComponentReference(_t);
               _t = this._retTree;
               mcr_AST = this.returnAST;
               addrExpr_AST = currentAST.root;
               addrExpr_AST = mcr_AST;
               currentAST.root = mcr_AST;
               currentAST.child = mcr_AST != null && mcr_AST.getFirstChild() != null ? mcr_AST.getFirstChild() : mcr_AST;
               currentAST.advanceChildToEnd();
               break;
            case 78:
               AST __t172 = _t;
               AST var43 = _t == ASTNULL ? null : _t;
               AST i_AST_in = null;
               i_AST = this.astFactory.create((AST)var43);
               ASTPair __currentAST172 = currentAST.copy();
               currentAST.root = currentAST.child;
               currentAST.child = null;
               this.match(_t, 78);
               _t = _t.getFirstChild();
               lhs2 = _t == ASTNULL ? null : _t;
               this.addrExprLhs(_t);
               _t = this._retTree;
               lhs2_AST = this.returnAST;
               rhs2 = _t == ASTNULL ? null : _t;
               this.expr(_t);
               _t = this._retTree;
               rhs2_AST = this.returnAST;
               _t = __t172.getNextSibling();
               AST var33 = __currentAST172.root;
               addrExpr_AST = this.astFactory.make((new ASTArray(3)).add(i_AST).add(lhs2_AST).add(rhs2_AST));
               this.processIndex(addrExpr_AST);
               __currentAST172.root = addrExpr_AST;
               __currentAST172.child = addrExpr_AST != null && addrExpr_AST.getFirstChild() != null ? addrExpr_AST.getFirstChild() : addrExpr_AST;
               __currentAST172.advanceChildToEnd();
               break;
            case 93:
            case 126:
               AST var52 = _t == ASTNULL ? null : _t;
               this.identifier(_t);
               _t = this._retTree;
               p_AST = this.returnAST;
               addrExpr_AST = currentAST.root;
               if (this.isNonQualifiedPropertyRef(p_AST)) {
                  addrExpr_AST = this.lookupNonQualifiedProperty(p_AST);
               } else {
                  this.resolve(p_AST);
                  addrExpr_AST = p_AST;
               }

               currentAST.root = addrExpr_AST;
               currentAST.child = addrExpr_AST != null && addrExpr_AST.getFirstChild() != null ? addrExpr_AST.getFirstChild() : addrExpr_AST;
               currentAST.advanceChildToEnd();
               break;
            default:
               throw new NoViableAltException(_t);
         }
      } catch (RecognitionException ex) {
         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this.returnAST = addrExpr_AST;
      this._retTree = _t;
   }

   public final void constant(AST _t) throws RecognitionException {
      AST constant_AST_in = _t == ASTNULL ? null : _t;
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST constant_AST = null;

      try {
         if (_t == null) {
            _t = ASTNULL;
         }

         switch (_t.getType()) {
            case 20:
               AST tmp74_AST = null;
               AST tmp74_AST_in = null;
               tmp74_AST = this.astFactory.create(_t);
               this.astFactory.addASTChild(currentAST, tmp74_AST);
               this.match(_t, 20);
               _t = _t.getNextSibling();
               constant_AST = currentAST.root;
               this.processBoolean(constant_AST);
               constant_AST = currentAST.root;
               break;
            case 39:
               AST tmp72_AST = null;
               AST tmp72_AST_in = null;
               tmp72_AST = this.astFactory.create(_t);
               this.astFactory.addASTChild(currentAST, tmp72_AST);
               this.match(_t, 39);
               _t = _t.getNextSibling();
               constant_AST = currentAST.root;
               break;
            case 49:
               AST tmp73_AST = null;
               AST tmp73_AST_in = null;
               tmp73_AST = this.astFactory.create(_t);
               this.astFactory.addASTChild(currentAST, tmp73_AST);
               this.match(_t, 49);
               _t = _t.getNextSibling();
               constant_AST = currentAST.root;
               this.processBoolean(constant_AST);
               constant_AST = currentAST.root;
               break;
            case 95:
            case 96:
            case 97:
            case 98:
            case 99:
            case 124:
            case 125:
               this.literal(_t);
               _t = this._retTree;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               constant_AST = currentAST.root;
               break;
            case 100:
               AST tmp75_AST = null;
               AST tmp75_AST_in = null;
               tmp75_AST = this.astFactory.create(_t);
               this.astFactory.addASTChild(currentAST, tmp75_AST);
               this.match(_t, 100);
               _t = _t.getNextSibling();
               constant_AST = currentAST.root;
               break;
            default:
               throw new NoViableAltException(_t);
         }
      } catch (RecognitionException ex) {
         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this.returnAST = constant_AST;
      this._retTree = _t;
   }

   public final void parameter(AST _t) throws RecognitionException {
      AST parameter_AST_in = _t == ASTNULL ? null : _t;
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST parameter_AST = null;
      AST c = null;
      AST c_AST = null;
      AST a_AST = null;
      AST a = null;
      AST p = null;
      AST p_AST = null;
      AST n = null;
      AST n_AST = null;

      try {
         if (_t == null) {
            _t = ASTNULL;
         }

         label73:
         switch (_t.getType()) {
            case 122:
               AST __t185 = _t;
               AST var24 = _t == ASTNULL ? null : _t;
               AST c_AST_in = null;
               this.astFactory.create((AST)var24);
               ASTPair __currentAST185 = currentAST.copy();
               currentAST.root = currentAST.child;
               currentAST.child = null;
               this.match(_t, 122);
               _t = _t.getFirstChild();
               a = _t == ASTNULL ? null : _t;
               this.identifier(_t);
               _t = this._retTree;
               a_AST = this.returnAST;
               _t = __t185.getNextSibling();
               AST var23 = __currentAST185.root;
               parameter_AST = this.generateNamedParameter((AST)var24, a);
               __currentAST185.root = parameter_AST;
               __currentAST185.child = parameter_AST != null && parameter_AST.getFirstChild() != null ? parameter_AST.getFirstChild() : parameter_AST;
               __currentAST185.advanceChildToEnd();
               break;
            case 123:
               AST __t186 = _t;
               AST var27 = _t == ASTNULL ? null : _t;
               AST p_AST_in = null;
               this.astFactory.create((AST)var27);
               ASTPair __currentAST186 = currentAST.copy();
               currentAST.root = currentAST.child;
               currentAST.child = null;
               this.match(_t, 123);
               _t = _t.getFirstChild();
               if (_t == null) {
                  _t = ASTNULL;
               }

               switch (_t.getType()) {
                  case 124:
                     n = _t;
                     AST n_AST_in = null;
                     n_AST = this.astFactory.create(_t);
                     this.match(_t, 124);
                     _t = _t.getNextSibling();
                  case 3:
                     _t = __t186.getNextSibling();
                     parameter_AST = __currentAST186.root;
                     if (n != null) {
                        parameter_AST = this.generateNamedParameter((AST)var27, n);
                     } else {
                        parameter_AST = this.generatePositionalParameter((AST)var27);
                     }

                     __currentAST186.root = parameter_AST;
                     __currentAST186.child = parameter_AST != null && parameter_AST.getFirstChild() != null ? parameter_AST.getFirstChild() : parameter_AST;
                     __currentAST186.advanceChildToEnd();
                     break label73;
                  default:
                     throw new NoViableAltException(_t);
               }
            default:
               throw new NoViableAltException(_t);
         }
      } catch (RecognitionException ex) {
         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this.returnAST = parameter_AST;
      this._retTree = _t;
   }

   public final void caseExpr(AST _t) throws RecognitionException {
      AST caseExpr_AST_in = _t == ASTNULL ? null : _t;
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST caseExpr_AST = null;

      try {
         if (_t == null) {
            _t = ASTNULL;
         }

         label82:
         switch (_t.getType()) {
            case 54:
               AST tmp76_AST = null;
               AST tmp76_AST_in = null;
               tmp76_AST = this.astFactory.create(_t);
               this.astFactory.addASTChild(currentAST, tmp76_AST);
               ASTPair __currentAST144 = currentAST.copy();
               currentAST.root = currentAST.child;
               currentAST.child = null;
               this.match(_t, 54);
               _t = _t.getFirstChild();
               this.inCase = true;
               int _cnt147 = 0;

               while(true) {
                  if (_t == null) {
                     _t = ASTNULL;
                  }

                  if (_t.getType() != 58) {
                     if (_cnt147 < 1) {
                        throw new NoViableAltException(_t);
                     }

                     if (_t == null) {
                        _t = ASTNULL;
                     }

                     switch (_t.getType()) {
                        case 56:
                           AST tmp78_AST = null;
                           AST tmp78_AST_in = null;
                           tmp78_AST = this.astFactory.create(_t);
                           this.astFactory.addASTChild(currentAST, tmp78_AST);
                           ASTPair __currentAST149 = currentAST.copy();
                           currentAST.root = currentAST.child;
                           currentAST.child = null;
                           this.match(_t, 56);
                           AST var27 = _t.getFirstChild();
                           this.expr(var27);
                           AST var28 = this._retTree;
                           this.astFactory.addASTChild(currentAST, this.returnAST);
                           _t = _t.getNextSibling();
                        case 3:
                           _t = _t.getNextSibling();
                           this.inCase = false;
                           caseExpr_AST = __currentAST144.root;
                           break label82;
                        default:
                           throw new NoViableAltException(_t);
                     }
                  }

                  AST tmp77_AST = null;
                  AST tmp77_AST_in = null;
                  tmp77_AST = this.astFactory.create(_t);
                  this.astFactory.addASTChild(currentAST, tmp77_AST);
                  ASTPair __currentAST146 = currentAST.copy();
                  currentAST.root = currentAST.child;
                  currentAST.child = null;
                  this.match(_t, 58);
                  AST var24 = _t.getFirstChild();
                  this.logicalExpr(var24);
                  AST var25 = this._retTree;
                  this.astFactory.addASTChild(currentAST, this.returnAST);
                  this.expr(var25);
                  AST var26 = this._retTree;
                  this.astFactory.addASTChild(currentAST, this.returnAST);
                  currentAST = __currentAST146;
                  _t = _t.getNextSibling();
                  ++_cnt147;
               }
            case 74:
               AST tmp79_AST = null;
               AST tmp79_AST_in = null;
               tmp79_AST = this.astFactory.create(_t);
               this.astFactory.addASTChild(currentAST, tmp79_AST);
               ASTPair __currentAST150 = currentAST.copy();
               currentAST.root = currentAST.child;
               currentAST.child = null;
               this.match(_t, 74);
               _t = _t.getFirstChild();
               this.inCase = true;
               this.expr(_t);
               _t = this._retTree;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               int _cnt153 = 0;

               while(true) {
                  if (_t == null) {
                     _t = ASTNULL;
                  }

                  if (_t.getType() != 58) {
                     if (_cnt153 < 1) {
                        throw new NoViableAltException(_t);
                     }

                     if (_t == null) {
                        _t = ASTNULL;
                     }

                     switch (_t.getType()) {
                        case 56:
                           AST tmp81_AST = null;
                           AST tmp81_AST_in = null;
                           tmp81_AST = this.astFactory.create(_t);
                           this.astFactory.addASTChild(currentAST, tmp81_AST);
                           ASTPair __currentAST155 = currentAST.copy();
                           currentAST.root = currentAST.child;
                           currentAST.child = null;
                           this.match(_t, 56);
                           AST var20 = _t.getFirstChild();
                           this.expr(var20);
                           AST var21 = this._retTree;
                           this.astFactory.addASTChild(currentAST, this.returnAST);
                           _t = _t.getNextSibling();
                        case 3:
                           _t = _t.getNextSibling();
                           this.inCase = false;
                           caseExpr_AST = __currentAST150.root;
                           break label82;
                        default:
                           throw new NoViableAltException(_t);
                     }
                  }

                  AST tmp80_AST = null;
                  AST tmp80_AST_in = null;
                  tmp80_AST = this.astFactory.create(_t);
                  this.astFactory.addASTChild(currentAST, tmp80_AST);
                  ASTPair __currentAST152 = currentAST.copy();
                  currentAST.root = currentAST.child;
                  currentAST.child = null;
                  this.match(_t, 58);
                  AST var17 = _t.getFirstChild();
                  this.expr(var17);
                  AST var18 = this._retTree;
                  this.astFactory.addASTChild(currentAST, this.returnAST);
                  this.expr(var18);
                  AST var19 = this._retTree;
                  this.astFactory.addASTChild(currentAST, this.returnAST);
                  currentAST = __currentAST152;
                  _t = _t.getNextSibling();
                  ++_cnt153;
               }
            default:
               throw new NoViableAltException(_t);
         }
      } catch (RecognitionException ex) {
         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this.returnAST = caseExpr_AST;
      this._retTree = _t;
   }

   public final void addrExprLhs(AST _t) throws RecognitionException {
      AST addrExprLhs_AST_in = _t == ASTNULL ? null : _t;
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST addrExprLhs_AST = null;

      try {
         this.addrExpr(_t, false);
         _t = this._retTree;
         this.astFactory.addASTChild(currentAST, this.returnAST);
         addrExprLhs_AST = currentAST.root;
      } catch (RecognitionException ex) {
         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this.returnAST = addrExprLhs_AST;
      this._retTree = _t;
   }

   public final void propertyName(AST _t) throws RecognitionException {
      AST propertyName_AST_in = _t == ASTNULL ? null : _t;
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST propertyName_AST = null;

      try {
         if (_t == null) {
            _t = ASTNULL;
         }

         switch (_t.getType()) {
            case 11:
               AST tmp82_AST = null;
               AST tmp82_AST_in = null;
               tmp82_AST = this.astFactory.create(_t);
               this.astFactory.addASTChild(currentAST, tmp82_AST);
               this.match(_t, 11);
               _t = _t.getNextSibling();
               propertyName_AST = currentAST.root;
               break;
            case 17:
               AST tmp83_AST = null;
               AST tmp83_AST_in = null;
               tmp83_AST = this.astFactory.create(_t);
               this.astFactory.addASTChild(currentAST, tmp83_AST);
               this.match(_t, 17);
               _t = _t.getNextSibling();
               propertyName_AST = currentAST.root;
               break;
            case 27:
               AST tmp84_AST = null;
               AST tmp84_AST_in = null;
               tmp84_AST = this.astFactory.create(_t);
               this.astFactory.addASTChild(currentAST, tmp84_AST);
               this.match(_t, 27);
               _t = _t.getNextSibling();
               propertyName_AST = currentAST.root;
               break;
            case 93:
            case 126:
               this.identifier(_t);
               _t = this._retTree;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               propertyName_AST = currentAST.root;
               break;
            default:
               throw new NoViableAltException(_t);
         }
      } catch (RecognitionException ex) {
         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this.returnAST = propertyName_AST;
      this._retTree = _t;
   }

   public final void mapComponentReference(AST _t) throws RecognitionException {
      AST mapComponentReference_AST_in = _t == ASTNULL ? null : _t;
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST mapComponentReference_AST = null;

      try {
         if (_t == null) {
            _t = ASTNULL;
         }

         switch (_t.getType()) {
            case 68:
               AST __t180 = _t;
               AST tmp85_AST = null;
               AST tmp85_AST_in = null;
               tmp85_AST = this.astFactory.create(_t);
               this.astFactory.addASTChild(currentAST, tmp85_AST);
               ASTPair __currentAST180 = currentAST.copy();
               currentAST.root = currentAST.child;
               currentAST.child = null;
               this.match(_t, 68);
               _t = _t.getFirstChild();
               this.mapPropertyExpression(_t);
               _t = this._retTree;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               _t = __t180.getNextSibling();
               mapComponentReference_AST = __currentAST180.root;
               break;
            case 69:
               AST __t181 = _t;
               AST tmp86_AST = null;
               AST tmp86_AST_in = null;
               tmp86_AST = this.astFactory.create(_t);
               this.astFactory.addASTChild(currentAST, tmp86_AST);
               ASTPair __currentAST181 = currentAST.copy();
               currentAST.root = currentAST.child;
               currentAST.child = null;
               this.match(_t, 69);
               _t = _t.getFirstChild();
               this.mapPropertyExpression(_t);
               _t = this._retTree;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               _t = __t181.getNextSibling();
               mapComponentReference_AST = __currentAST181.root;
               break;
            case 70:
               AST __t182 = _t;
               AST tmp87_AST = null;
               AST tmp87_AST_in = null;
               tmp87_AST = this.astFactory.create(_t);
               this.astFactory.addASTChild(currentAST, tmp87_AST);
               ASTPair __currentAST182 = currentAST.copy();
               currentAST.root = currentAST.child;
               currentAST.child = null;
               this.match(_t, 70);
               _t = _t.getFirstChild();
               this.mapPropertyExpression(_t);
               _t = this._retTree;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               _t = __t182.getNextSibling();
               mapComponentReference_AST = __currentAST182.root;
               break;
            default:
               throw new NoViableAltException(_t);
         }
      } catch (RecognitionException ex) {
         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this.returnAST = mapComponentReference_AST;
      this._retTree = _t;
   }

   public final void propertyRefLhs(AST _t) throws RecognitionException {
      AST propertyRefLhs_AST_in = _t == ASTNULL ? null : _t;
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST propertyRefLhs_AST = null;

      try {
         this.propertyRef(_t);
         _t = this._retTree;
         this.astFactory.addASTChild(currentAST, this.returnAST);
         propertyRefLhs_AST = currentAST.root;
      } catch (RecognitionException ex) {
         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this.returnAST = propertyRefLhs_AST;
      this._retTree = _t;
   }

   public final void mapPropertyExpression(AST _t) throws RecognitionException {
      AST mapPropertyExpression_AST_in = _t == ASTNULL ? null : _t;
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST mapPropertyExpression_AST = null;
      AST e_AST = null;
      AST e = null;

      try {
         e = _t == ASTNULL ? null : _t;
         this.expr(_t);
         _t = this._retTree;
         e_AST = this.returnAST;
         this.astFactory.addASTChild(currentAST, this.returnAST);
         this.validateMapPropertyExpression(e_AST);
         mapPropertyExpression_AST = currentAST.root;
      } catch (RecognitionException ex) {
         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this.returnAST = mapPropertyExpression_AST;
      this._retTree = _t;
   }

   public final void numericInteger(AST _t) throws RecognitionException {
      AST numericInteger_AST_in = _t == ASTNULL ? null : _t;
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST numericInteger_AST = null;

      try {
         AST tmp88_AST = null;
         AST tmp88_AST_in = null;
         tmp88_AST = this.astFactory.create(_t);
         this.astFactory.addASTChild(currentAST, tmp88_AST);
         this.match(_t, 124);
         _t = _t.getNextSibling();
         numericInteger_AST = currentAST.root;
      } catch (RecognitionException ex) {
         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this.returnAST = numericInteger_AST;
      this._retTree = _t;
   }

   private static final long[] mk_tokenSet_0() {
      long[] data = new long[]{18577898219802624L, 9004947591091340528L, 0L, 0L};
      return data;
   }

   private static final long[] mk_tokenSet_1() {
      long[] data = new long[]{18718635708158000L, 9004947591095534832L, 0L, 0L};
      return data;
   }
}
