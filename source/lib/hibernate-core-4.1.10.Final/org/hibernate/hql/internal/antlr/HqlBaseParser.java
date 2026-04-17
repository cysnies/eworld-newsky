package org.hibernate.hql.internal.antlr;

import antlr.ASTFactory;
import antlr.ASTPair;
import antlr.LLkParser;
import antlr.NoViableAltException;
import antlr.ParserSharedInputState;
import antlr.RecognitionException;
import antlr.SemanticException;
import antlr.Token;
import antlr.TokenBuffer;
import antlr.TokenStream;
import antlr.TokenStreamException;
import antlr.collections.AST;
import antlr.collections.impl.ASTArray;
import antlr.collections.impl.BitSet;
import org.hibernate.hql.internal.ast.util.ASTUtil;

public class HqlBaseParser extends LLkParser implements HqlTokenTypes {
   private boolean filter;
   public static final String[] _tokenNames = new String[]{"<0>", "EOF", "<2>", "NULL_TREE_LOOKAHEAD", "\"all\"", "\"any\"", "\"and\"", "\"as\"", "\"asc\"", "\"avg\"", "\"between\"", "\"class\"", "\"count\"", "\"delete\"", "\"desc\"", "DOT", "\"distinct\"", "\"elements\"", "\"escape\"", "\"exists\"", "\"false\"", "\"fetch\"", "\"from\"", "\"full\"", "\"group\"", "\"having\"", "\"in\"", "\"indices\"", "\"inner\"", "\"insert\"", "\"into\"", "\"is\"", "\"join\"", "\"left\"", "\"like\"", "\"max\"", "\"min\"", "\"new\"", "\"not\"", "\"null\"", "\"or\"", "\"order\"", "\"outer\"", "\"properties\"", "\"right\"", "\"select\"", "\"set\"", "\"some\"", "\"sum\"", "\"true\"", "\"union\"", "\"update\"", "\"versioned\"", "\"where\"", "\"case\"", "\"end\"", "\"else\"", "\"then\"", "\"when\"", "\"on\"", "\"with\"", "\"both\"", "\"empty\"", "\"leading\"", "\"member\"", "\"object\"", "\"of\"", "\"trailing\"", "KEY", "VALUE", "ENTRY", "AGGREGATE", "ALIAS", "CONSTRUCTOR", "CASE2", "EXPR_LIST", "FILTER_ENTITY", "IN_LIST", "INDEX_OP", "IS_NOT_NULL", "IS_NULL", "METHOD_CALL", "NOT_BETWEEN", "NOT_IN", "NOT_LIKE", "ORDER_ELEMENT", "QUERY", "RANGE", "ROW_STAR", "SELECT_FROM", "UNARY_MINUS", "UNARY_PLUS", "VECTOR_EXPR", "WEIRD_IDENT", "CONSTANT", "NUM_DOUBLE", "NUM_FLOAT", "NUM_LONG", "NUM_BIG_INTEGER", "NUM_BIG_DECIMAL", "JAVA_CONSTANT", "COMMA", "EQ", "OPEN", "CLOSE", "\"by\"", "\"ascending\"", "\"descending\"", "NE", "SQL_NE", "LT", "GT", "LE", "GE", "CONCAT", "PLUS", "MINUS", "STAR", "DIV", "MOD", "OPEN_BRACKET", "CLOSE_BRACKET", "COLON", "PARAM", "NUM_INT", "QUOTED_STRING", "IDENT", "ID_START_LETTER", "ID_LETTER", "ESCqs", "WS", "HEX_DIGIT", "EXPONENT", "FLOAT_SUFFIX"};
   public static final BitSet _tokenSet_0 = new BitSet(mk_tokenSet_0());
   public static final BitSet _tokenSet_1 = new BitSet(mk_tokenSet_1());
   public static final BitSet _tokenSet_2 = new BitSet(mk_tokenSet_2());
   public static final BitSet _tokenSet_3 = new BitSet(mk_tokenSet_3());
   public static final BitSet _tokenSet_4 = new BitSet(mk_tokenSet_4());
   public static final BitSet _tokenSet_5 = new BitSet(mk_tokenSet_5());
   public static final BitSet _tokenSet_6 = new BitSet(mk_tokenSet_6());
   public static final BitSet _tokenSet_7 = new BitSet(mk_tokenSet_7());
   public static final BitSet _tokenSet_8 = new BitSet(mk_tokenSet_8());
   public static final BitSet _tokenSet_9 = new BitSet(mk_tokenSet_9());
   public static final BitSet _tokenSet_10 = new BitSet(mk_tokenSet_10());
   public static final BitSet _tokenSet_11 = new BitSet(mk_tokenSet_11());
   public static final BitSet _tokenSet_12 = new BitSet(mk_tokenSet_12());
   public static final BitSet _tokenSet_13 = new BitSet(mk_tokenSet_13());
   public static final BitSet _tokenSet_14 = new BitSet(mk_tokenSet_14());
   public static final BitSet _tokenSet_15 = new BitSet(mk_tokenSet_15());
   public static final BitSet _tokenSet_16 = new BitSet(mk_tokenSet_16());
   public static final BitSet _tokenSet_17 = new BitSet(mk_tokenSet_17());
   public static final BitSet _tokenSet_18 = new BitSet(mk_tokenSet_18());
   public static final BitSet _tokenSet_19 = new BitSet(mk_tokenSet_19());
   public static final BitSet _tokenSet_20 = new BitSet(mk_tokenSet_20());
   public static final BitSet _tokenSet_21 = new BitSet(mk_tokenSet_21());
   public static final BitSet _tokenSet_22 = new BitSet(mk_tokenSet_22());
   public static final BitSet _tokenSet_23 = new BitSet(mk_tokenSet_23());
   public static final BitSet _tokenSet_24 = new BitSet(mk_tokenSet_24());
   public static final BitSet _tokenSet_25 = new BitSet(mk_tokenSet_25());
   public static final BitSet _tokenSet_26 = new BitSet(mk_tokenSet_26());
   public static final BitSet _tokenSet_27 = new BitSet(mk_tokenSet_27());
   public static final BitSet _tokenSet_28 = new BitSet(mk_tokenSet_28());
   public static final BitSet _tokenSet_29 = new BitSet(mk_tokenSet_29());
   public static final BitSet _tokenSet_30 = new BitSet(mk_tokenSet_30());
   public static final BitSet _tokenSet_31 = new BitSet(mk_tokenSet_31());
   public static final BitSet _tokenSet_32 = new BitSet(mk_tokenSet_32());
   public static final BitSet _tokenSet_33 = new BitSet(mk_tokenSet_33());
   public static final BitSet _tokenSet_34 = new BitSet(mk_tokenSet_34());
   public static final BitSet _tokenSet_35 = new BitSet(mk_tokenSet_35());
   public static final BitSet _tokenSet_36 = new BitSet(mk_tokenSet_36());
   public static final BitSet _tokenSet_37 = new BitSet(mk_tokenSet_37());

   public void setFilter(boolean f) {
      this.filter = f;
   }

   public boolean isFilter() {
      return this.filter;
   }

   public AST handleIdentifierError(Token token, RecognitionException ex) throws RecognitionException, TokenStreamException {
      throw ex;
   }

   public void handleDotIdent() throws TokenStreamException {
   }

   public AST negateNode(AST x) {
      return ASTUtil.createParent(this.astFactory, 38, "not", x);
   }

   public AST processEqualityExpression(AST x) throws RecognitionException {
      return x;
   }

   public void weakKeywords() throws TokenStreamException {
   }

   public void processMemberOf(Token n, AST p, ASTPair currentAST) {
   }

   protected HqlBaseParser(TokenBuffer tokenBuf, int k) {
      super(tokenBuf, k);
      this.filter = false;
      this.tokenNames = _tokenNames;
      this.buildTokenTypeASTClassMap();
      this.astFactory = new ASTFactory(this.getTokenTypeToASTClassMap());
   }

   public HqlBaseParser(TokenBuffer tokenBuf) {
      this((TokenBuffer)tokenBuf, 3);
   }

   protected HqlBaseParser(TokenStream lexer, int k) {
      super(lexer, k);
      this.filter = false;
      this.tokenNames = _tokenNames;
      this.buildTokenTypeASTClassMap();
      this.astFactory = new ASTFactory(this.getTokenTypeToASTClassMap());
   }

   public HqlBaseParser(TokenStream lexer) {
      this((TokenStream)lexer, 3);
   }

   public HqlBaseParser(ParserSharedInputState state) {
      super(state, 3);
      this.filter = false;
      this.tokenNames = _tokenNames;
      this.buildTokenTypeASTClassMap();
      this.astFactory = new ASTFactory(this.getTokenTypeToASTClassMap());
   }

   public final void statement() throws RecognitionException, TokenStreamException {
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST statement_AST = null;

      try {
         switch (this.LA(1)) {
            case 1:
            case 22:
            case 24:
            case 41:
            case 45:
            case 53:
               this.selectStatement();
               this.astFactory.addASTChild(currentAST, this.returnAST);
               break;
            case 13:
               this.deleteStatement();
               this.astFactory.addASTChild(currentAST, this.returnAST);
               break;
            case 29:
               this.insertStatement();
               this.astFactory.addASTChild(currentAST, this.returnAST);
               break;
            case 51:
               this.updateStatement();
               this.astFactory.addASTChild(currentAST, this.returnAST);
               break;
            default:
               throw new NoViableAltException(this.LT(1), this.getFilename());
         }

         statement_AST = currentAST.root;
      } catch (RecognitionException ex) {
         this.reportError(ex);
         this.recover(ex, _tokenSet_0);
      }

      this.returnAST = statement_AST;
   }

   public final void updateStatement() throws RecognitionException, TokenStreamException {
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST updateStatement_AST = null;

      try {
         AST tmp1_AST = null;
         tmp1_AST = this.astFactory.create(this.LT(1));
         this.astFactory.makeASTRoot(currentAST, tmp1_AST);
         this.match(51);
         label20:
         switch (this.LA(1)) {
            case 52:
               AST tmp2_AST = null;
               tmp2_AST = this.astFactory.create(this.LT(1));
               this.astFactory.addASTChild(currentAST, tmp2_AST);
               this.match(52);
            case 22:
            case 126:
               this.optionalFromTokenFromClause();
               this.astFactory.addASTChild(currentAST, this.returnAST);
               this.setClause();
               this.astFactory.addASTChild(currentAST, this.returnAST);
               switch (this.LA(1)) {
                  case 53:
                     this.whereClause();
                     this.astFactory.addASTChild(currentAST, this.returnAST);
                  case 1:
                     updateStatement_AST = currentAST.root;
                     break label20;
                  default:
                     throw new NoViableAltException(this.LT(1), this.getFilename());
               }
            default:
               throw new NoViableAltException(this.LT(1), this.getFilename());
         }
      } catch (RecognitionException ex) {
         this.reportError(ex);
         this.recover(ex, _tokenSet_0);
      }

      this.returnAST = updateStatement_AST;
   }

   public final void deleteStatement() throws RecognitionException, TokenStreamException {
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST deleteStatement_AST = null;

      try {
         AST tmp3_AST = null;
         tmp3_AST = this.astFactory.create(this.LT(1));
         this.astFactory.makeASTRoot(currentAST, tmp3_AST);
         this.match(13);
         this.optionalFromTokenFromClause();
         this.astFactory.addASTChild(currentAST, this.returnAST);
         switch (this.LA(1)) {
            case 53:
               this.whereClause();
               this.astFactory.addASTChild(currentAST, this.returnAST);
            case 1:
               deleteStatement_AST = currentAST.root;
               break;
            default:
               throw new NoViableAltException(this.LT(1), this.getFilename());
         }
      } catch (RecognitionException ex) {
         this.reportError(ex);
         this.recover(ex, _tokenSet_0);
      }

      this.returnAST = deleteStatement_AST;
   }

   public final void selectStatement() throws RecognitionException, TokenStreamException {
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST selectStatement_AST = null;

      try {
         this.queryRule();
         this.astFactory.addASTChild(currentAST, this.returnAST);
         selectStatement_AST = currentAST.root;
         selectStatement_AST = this.astFactory.make((new ASTArray(2)).add(this.astFactory.create(86, "query")).add(selectStatement_AST));
         currentAST.root = selectStatement_AST;
         currentAST.child = selectStatement_AST != null && selectStatement_AST.getFirstChild() != null ? selectStatement_AST.getFirstChild() : selectStatement_AST;
         currentAST.advanceChildToEnd();
         selectStatement_AST = currentAST.root;
      } catch (RecognitionException ex) {
         this.reportError(ex);
         this.recover(ex, _tokenSet_0);
      }

      this.returnAST = selectStatement_AST;
   }

   public final void insertStatement() throws RecognitionException, TokenStreamException {
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST insertStatement_AST = null;

      try {
         AST tmp4_AST = null;
         tmp4_AST = this.astFactory.create(this.LT(1));
         this.astFactory.makeASTRoot(currentAST, tmp4_AST);
         this.match(29);
         this.intoClause();
         this.astFactory.addASTChild(currentAST, this.returnAST);
         this.selectStatement();
         this.astFactory.addASTChild(currentAST, this.returnAST);
         insertStatement_AST = currentAST.root;
      } catch (RecognitionException ex) {
         this.reportError(ex);
         this.recover(ex, _tokenSet_0);
      }

      this.returnAST = insertStatement_AST;
   }

   public final void optionalFromTokenFromClause() throws RecognitionException, TokenStreamException {
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST optionalFromTokenFromClause_AST = null;
      AST f_AST = null;
      AST a_AST = null;

      try {
         switch (this.LA(1)) {
            case 22:
               this.match(22);
            case 126:
               this.path();
               f_AST = this.returnAST;
               switch (this.LA(1)) {
                  case 1:
                  case 46:
                  case 53:
                     break;
                  case 7:
                  case 126:
                     this.asAlias();
                     a_AST = this.returnAST;
                     break;
                  default:
                     throw new NoViableAltException(this.LT(1), this.getFilename());
               }

               AST var7 = currentAST.root;
               AST range = this.astFactory.make((new ASTArray(3)).add(this.astFactory.create(87, "RANGE")).add(f_AST).add(a_AST));
               optionalFromTokenFromClause_AST = this.astFactory.make((new ASTArray(2)).add(this.astFactory.create(22, "FROM")).add(range));
               currentAST.root = optionalFromTokenFromClause_AST;
               currentAST.child = optionalFromTokenFromClause_AST != null && optionalFromTokenFromClause_AST.getFirstChild() != null ? optionalFromTokenFromClause_AST.getFirstChild() : optionalFromTokenFromClause_AST;
               currentAST.advanceChildToEnd();
               break;
            default:
               throw new NoViableAltException(this.LT(1), this.getFilename());
         }
      } catch (RecognitionException ex) {
         this.reportError(ex);
         this.recover(ex, _tokenSet_1);
      }

      this.returnAST = optionalFromTokenFromClause_AST;
   }

   public final void setClause() throws RecognitionException, TokenStreamException {
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST setClause_AST = null;

      try {
         AST tmp6_AST = null;
         tmp6_AST = this.astFactory.create(this.LT(1));
         this.astFactory.makeASTRoot(currentAST, tmp6_AST);
         this.match(46);
         this.assignment();
         this.astFactory.addASTChild(currentAST, this.returnAST);

         while(this.LA(1) == 101) {
            this.match(101);
            this.assignment();
            this.astFactory.addASTChild(currentAST, this.returnAST);
         }

         setClause_AST = currentAST.root;
      } catch (RecognitionException ex) {
         this.reportError(ex);
         this.recover(ex, _tokenSet_2);
      }

      this.returnAST = setClause_AST;
   }

   public final void whereClause() throws RecognitionException, TokenStreamException {
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST whereClause_AST = null;

      try {
         AST tmp8_AST = null;
         tmp8_AST = this.astFactory.create(this.LT(1));
         this.astFactory.makeASTRoot(currentAST, tmp8_AST);
         this.match(53);
         this.logicalExpression();
         this.astFactory.addASTChild(currentAST, this.returnAST);
         whereClause_AST = currentAST.root;
      } catch (RecognitionException ex) {
         this.reportError(ex);
         this.recover(ex, _tokenSet_3);
      }

      this.returnAST = whereClause_AST;
   }

   public final void assignment() throws RecognitionException, TokenStreamException {
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST assignment_AST = null;

      try {
         this.stateField();
         this.astFactory.addASTChild(currentAST, this.returnAST);
         AST tmp9_AST = null;
         tmp9_AST = this.astFactory.create(this.LT(1));
         this.astFactory.makeASTRoot(currentAST, tmp9_AST);
         this.match(102);
         this.newValue();
         this.astFactory.addASTChild(currentAST, this.returnAST);
         assignment_AST = currentAST.root;
      } catch (RecognitionException ex) {
         this.reportError(ex);
         this.recover(ex, _tokenSet_4);
      }

      this.returnAST = assignment_AST;
   }

   public final void stateField() throws RecognitionException, TokenStreamException {
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST stateField_AST = null;

      try {
         this.path();
         this.astFactory.addASTChild(currentAST, this.returnAST);
         stateField_AST = currentAST.root;
      } catch (RecognitionException ex) {
         this.reportError(ex);
         this.recover(ex, _tokenSet_5);
      }

      this.returnAST = stateField_AST;
   }

   public final void newValue() throws RecognitionException, TokenStreamException {
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST newValue_AST = null;

      try {
         this.concatenation();
         this.astFactory.addASTChild(currentAST, this.returnAST);
         newValue_AST = currentAST.root;
      } catch (RecognitionException ex) {
         this.reportError(ex);
         this.recover(ex, _tokenSet_4);
      }

      this.returnAST = newValue_AST;
   }

   public final void path() throws RecognitionException, TokenStreamException {
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST path_AST = null;

      try {
         this.identifier();
         this.astFactory.addASTChild(currentAST, this.returnAST);

         while(this.LA(1) == 15) {
            AST tmp10_AST = null;
            tmp10_AST = this.astFactory.create(this.LT(1));
            this.astFactory.makeASTRoot(currentAST, tmp10_AST);
            this.match(15);
            this.weakKeywords();
            this.identifier();
            this.astFactory.addASTChild(currentAST, this.returnAST);
         }

         path_AST = currentAST.root;
      } catch (RecognitionException ex) {
         this.reportError(ex);
         this.recover(ex, _tokenSet_6);
      }

      this.returnAST = path_AST;
   }

   public final void concatenation() throws RecognitionException, TokenStreamException {
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST concatenation_AST = null;
      Token c = null;
      AST c_AST = null;

      try {
         label47: {
            this.additiveExpression();
            this.astFactory.addASTChild(currentAST, this.returnAST);
            switch (this.LA(1)) {
               case 1:
               case 6:
               case 7:
               case 8:
               case 10:
               case 14:
               case 18:
               case 22:
               case 23:
               case 24:
               case 25:
               case 26:
               case 28:
               case 31:
               case 32:
               case 33:
               case 34:
               case 38:
               case 40:
               case 41:
               case 44:
               case 50:
               case 53:
               case 57:
               case 64:
               case 101:
               case 102:
               case 104:
               case 106:
               case 107:
               case 108:
               case 109:
               case 110:
               case 111:
               case 112:
               case 113:
               case 121:
                  break label47;
               case 2:
               case 3:
               case 4:
               case 5:
               case 9:
               case 11:
               case 12:
               case 13:
               case 15:
               case 16:
               case 17:
               case 19:
               case 20:
               case 21:
               case 27:
               case 29:
               case 30:
               case 35:
               case 36:
               case 37:
               case 39:
               case 42:
               case 43:
               case 45:
               case 46:
               case 47:
               case 48:
               case 49:
               case 51:
               case 52:
               case 54:
               case 55:
               case 56:
               case 58:
               case 59:
               case 60:
               case 61:
               case 62:
               case 63:
               case 65:
               case 66:
               case 67:
               case 68:
               case 69:
               case 70:
               case 71:
               case 72:
               case 73:
               case 74:
               case 75:
               case 76:
               case 77:
               case 78:
               case 79:
               case 80:
               case 81:
               case 82:
               case 83:
               case 84:
               case 85:
               case 86:
               case 87:
               case 88:
               case 89:
               case 90:
               case 91:
               case 92:
               case 93:
               case 94:
               case 95:
               case 96:
               case 97:
               case 98:
               case 99:
               case 100:
               case 103:
               case 105:
               case 115:
               case 116:
               case 117:
               case 118:
               case 119:
               case 120:
               default:
                  throw new NoViableAltException(this.LT(1), this.getFilename());
               case 114:
                  c = this.LT(1);
                  c_AST = this.astFactory.create(c);
                  this.astFactory.makeASTRoot(currentAST, c_AST);
                  this.match(114);
                  c_AST.setType(75);
                  c_AST.setText("concatList");
                  this.additiveExpression();
                  this.astFactory.addASTChild(currentAST, this.returnAST);
            }

            while(this.LA(1) == 114) {
               this.match(114);
               this.additiveExpression();
               this.astFactory.addASTChild(currentAST, this.returnAST);
            }

            concatenation_AST = currentAST.root;
            concatenation_AST = this.astFactory.make((new ASTArray(3)).add(this.astFactory.create(81, "||")).add(this.astFactory.make((new ASTArray(1)).add(this.astFactory.create(126, "concat")))).add(c_AST));
            currentAST.root = concatenation_AST;
            currentAST.child = concatenation_AST != null && concatenation_AST.getFirstChild() != null ? concatenation_AST.getFirstChild() : concatenation_AST;
            currentAST.advanceChildToEnd();
         }

         concatenation_AST = currentAST.root;
      } catch (RecognitionException ex) {
         this.reportError(ex);
         this.recover(ex, _tokenSet_7);
      }

      this.returnAST = concatenation_AST;
   }

   public final void asAlias() throws RecognitionException, TokenStreamException {
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST asAlias_AST = null;

      try {
         switch (this.LA(1)) {
            case 7:
               this.match(7);
            case 126:
               this.alias();
               this.astFactory.addASTChild(currentAST, this.returnAST);
               asAlias_AST = currentAST.root;
               break;
            default:
               throw new NoViableAltException(this.LT(1), this.getFilename());
         }
      } catch (RecognitionException ex) {
         this.reportError(ex);
         this.recover(ex, _tokenSet_8);
      }

      this.returnAST = asAlias_AST;
   }

   public final void queryRule() throws RecognitionException, TokenStreamException {
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST queryRule_AST = null;

      try {
         this.selectFrom();
         this.astFactory.addASTChild(currentAST, this.returnAST);
         label25:
         switch (this.LA(1)) {
            case 53:
               this.whereClause();
               this.astFactory.addASTChild(currentAST, this.returnAST);
            case 1:
            case 24:
            case 41:
            case 50:
            case 104:
               switch (this.LA(1)) {
                  case 24:
                     this.groupByClause();
                     this.astFactory.addASTChild(currentAST, this.returnAST);
                  case 1:
                  case 41:
                  case 50:
                  case 104:
                     switch (this.LA(1)) {
                        case 41:
                           this.orderByClause();
                           this.astFactory.addASTChild(currentAST, this.returnAST);
                        case 1:
                        case 50:
                        case 104:
                           queryRule_AST = currentAST.root;
                           break label25;
                        default:
                           throw new NoViableAltException(this.LT(1), this.getFilename());
                     }
                  default:
                     throw new NoViableAltException(this.LT(1), this.getFilename());
               }
            default:
               throw new NoViableAltException(this.LT(1), this.getFilename());
         }
      } catch (RecognitionException ex) {
         this.reportError(ex);
         this.recover(ex, _tokenSet_9);
      }

      this.returnAST = queryRule_AST;
   }

   public final void intoClause() throws RecognitionException, TokenStreamException {
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST intoClause_AST = null;

      try {
         AST tmp13_AST = null;
         tmp13_AST = this.astFactory.create(this.LT(1));
         this.astFactory.makeASTRoot(currentAST, tmp13_AST);
         this.match(30);
         this.path();
         this.astFactory.addASTChild(currentAST, this.returnAST);
         this.weakKeywords();
         this.insertablePropertySpec();
         this.astFactory.addASTChild(currentAST, this.returnAST);
         intoClause_AST = currentAST.root;
      } catch (RecognitionException ex) {
         this.reportError(ex);
         this.recover(ex, _tokenSet_10);
      }

      this.returnAST = intoClause_AST;
   }

   public final void insertablePropertySpec() throws RecognitionException, TokenStreamException {
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST insertablePropertySpec_AST = null;

      try {
         this.match(103);
         this.primaryExpression();
         this.astFactory.addASTChild(currentAST, this.returnAST);

         while(this.LA(1) == 101) {
            this.match(101);
            this.primaryExpression();
            this.astFactory.addASTChild(currentAST, this.returnAST);
         }

         this.match(104);
         insertablePropertySpec_AST = currentAST.root;
         insertablePropertySpec_AST = this.astFactory.make((new ASTArray(2)).add(this.astFactory.create(87, "column-spec")).add(insertablePropertySpec_AST));
         currentAST.root = insertablePropertySpec_AST;
         currentAST.child = insertablePropertySpec_AST != null && insertablePropertySpec_AST.getFirstChild() != null ? insertablePropertySpec_AST.getFirstChild() : insertablePropertySpec_AST;
         currentAST.advanceChildToEnd();
         insertablePropertySpec_AST = currentAST.root;
      } catch (RecognitionException ex) {
         this.reportError(ex);
         this.recover(ex, _tokenSet_10);
      }

      this.returnAST = insertablePropertySpec_AST;
   }

   public final void primaryExpression() throws RecognitionException, TokenStreamException {
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST primaryExpression_AST = null;

      try {
         switch (this.LA(1)) {
            case 9:
            case 12:
            case 17:
            case 27:
            case 35:
            case 36:
            case 48:
            case 126:
               this.identPrimary();
               this.astFactory.addASTChild(currentAST, this.returnAST);
               if (this.LA(1) == 15 && this.LA(2) == 11) {
                  AST tmp17_AST = null;
                  tmp17_AST = this.astFactory.create(this.LT(1));
                  this.astFactory.makeASTRoot(currentAST, tmp17_AST);
                  this.match(15);
                  AST tmp18_AST = null;
                  tmp18_AST = this.astFactory.create(this.LT(1));
                  this.astFactory.addASTChild(currentAST, tmp18_AST);
                  this.match(11);
               } else if (!_tokenSet_11.member(this.LA(1)) || !_tokenSet_12.member(this.LA(2))) {
                  throw new NoViableAltException(this.LT(1), this.getFilename());
               }

               primaryExpression_AST = currentAST.root;
               break;
            case 20:
            case 39:
            case 49:
            case 62:
            case 95:
            case 96:
            case 97:
            case 98:
            case 99:
            case 124:
            case 125:
               this.constant();
               this.astFactory.addASTChild(currentAST, this.returnAST);
               primaryExpression_AST = currentAST.root;
               break;
            case 103:
               this.match(103);
               switch (this.LA(1)) {
                  case 4:
                  case 5:
                  case 9:
                  case 12:
                  case 17:
                  case 19:
                  case 20:
                  case 27:
                  case 35:
                  case 36:
                  case 38:
                  case 39:
                  case 47:
                  case 48:
                  case 49:
                  case 54:
                  case 62:
                  case 95:
                  case 96:
                  case 97:
                  case 98:
                  case 99:
                  case 103:
                  case 115:
                  case 116:
                  case 122:
                  case 123:
                  case 124:
                  case 125:
                  case 126:
                     this.expressionOrVector();
                     this.astFactory.addASTChild(currentAST, this.returnAST);
                     break;
                  case 6:
                  case 7:
                  case 8:
                  case 10:
                  case 11:
                  case 13:
                  case 14:
                  case 15:
                  case 16:
                  case 18:
                  case 21:
                  case 23:
                  case 25:
                  case 26:
                  case 28:
                  case 29:
                  case 30:
                  case 31:
                  case 32:
                  case 33:
                  case 34:
                  case 37:
                  case 40:
                  case 42:
                  case 43:
                  case 44:
                  case 46:
                  case 51:
                  case 52:
                  case 55:
                  case 56:
                  case 57:
                  case 58:
                  case 59:
                  case 60:
                  case 61:
                  case 63:
                  case 64:
                  case 65:
                  case 66:
                  case 67:
                  case 68:
                  case 69:
                  case 70:
                  case 71:
                  case 72:
                  case 73:
                  case 74:
                  case 75:
                  case 76:
                  case 77:
                  case 78:
                  case 79:
                  case 80:
                  case 81:
                  case 82:
                  case 83:
                  case 84:
                  case 85:
                  case 86:
                  case 87:
                  case 88:
                  case 89:
                  case 90:
                  case 91:
                  case 92:
                  case 93:
                  case 94:
                  case 100:
                  case 101:
                  case 102:
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
                  case 117:
                  case 118:
                  case 119:
                  case 120:
                  case 121:
                  default:
                     throw new NoViableAltException(this.LT(1), this.getFilename());
                  case 22:
                  case 24:
                  case 41:
                  case 45:
                  case 50:
                  case 53:
                  case 104:
                     this.subQuery();
                     this.astFactory.addASTChild(currentAST, this.returnAST);
               }

               this.match(104);
               primaryExpression_AST = currentAST.root;
               break;
            case 122:
            case 123:
               this.parameter();
               this.astFactory.addASTChild(currentAST, this.returnAST);
               primaryExpression_AST = currentAST.root;
               break;
            default:
               throw new NoViableAltException(this.LT(1), this.getFilename());
         }
      } catch (RecognitionException ex) {
         this.reportError(ex);
         this.recover(ex, _tokenSet_11);
      }

      this.returnAST = primaryExpression_AST;
   }

   public final void union() throws RecognitionException, TokenStreamException {
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST union_AST = null;

      try {
         this.queryRule();
         this.astFactory.addASTChild(currentAST, this.returnAST);

         while(this.LA(1) == 50) {
            AST tmp21_AST = null;
            tmp21_AST = this.astFactory.create(this.LT(1));
            this.astFactory.addASTChild(currentAST, tmp21_AST);
            this.match(50);
            this.queryRule();
            this.astFactory.addASTChild(currentAST, this.returnAST);
         }

         union_AST = currentAST.root;
      } catch (RecognitionException ex) {
         this.reportError(ex);
         this.recover(ex, _tokenSet_13);
      }

      this.returnAST = union_AST;
   }

   public final void selectFrom() throws RecognitionException, TokenStreamException {
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST selectFrom_AST = null;
      AST s_AST = null;
      AST f_AST = null;

      try {
         switch (this.LA(1)) {
            case 1:
            case 22:
            case 24:
            case 41:
            case 50:
            case 53:
            case 104:
               break;
            case 45:
               this.selectClause();
               s_AST = this.returnAST;
               break;
            default:
               throw new NoViableAltException(this.LT(1), this.getFilename());
         }

         switch (this.LA(1)) {
            case 22:
               this.fromClause();
               f_AST = this.returnAST;
            case 1:
            case 24:
            case 41:
            case 50:
            case 53:
            case 104:
               selectFrom_AST = currentAST.root;
               if (f_AST == null) {
                  if (!this.filter) {
                     throw new SemanticException("FROM expected (non-filter queries must contain a FROM clause)");
                  }

                  f_AST = this.astFactory.make((new ASTArray(1)).add(this.astFactory.create(22, "{filter-implied FROM}")));
               }

               selectFrom_AST = this.astFactory.make((new ASTArray(3)).add(this.astFactory.create(89, "SELECT_FROM")).add(f_AST).add(s_AST));
               currentAST.root = selectFrom_AST;
               currentAST.child = selectFrom_AST != null && selectFrom_AST.getFirstChild() != null ? selectFrom_AST.getFirstChild() : selectFrom_AST;
               currentAST.advanceChildToEnd();
               break;
            default:
               throw new NoViableAltException(this.LT(1), this.getFilename());
         }
      } catch (RecognitionException ex) {
         this.reportError(ex);
         this.recover(ex, _tokenSet_14);
      }

      this.returnAST = selectFrom_AST;
   }

   public final void groupByClause() throws RecognitionException, TokenStreamException {
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST groupByClause_AST = null;

      try {
         AST tmp22_AST = null;
         tmp22_AST = this.astFactory.create(this.LT(1));
         this.astFactory.makeASTRoot(currentAST, tmp22_AST);
         this.match(24);
         this.match(105);
         this.expression();
         this.astFactory.addASTChild(currentAST, this.returnAST);

         while(this.LA(1) == 101) {
            this.match(101);
            this.expression();
            this.astFactory.addASTChild(currentAST, this.returnAST);
         }

         switch (this.LA(1)) {
            case 25:
               this.havingClause();
               this.astFactory.addASTChild(currentAST, this.returnAST);
            case 1:
            case 41:
            case 50:
            case 104:
               groupByClause_AST = currentAST.root;
               break;
            default:
               throw new NoViableAltException(this.LT(1), this.getFilename());
         }
      } catch (RecognitionException ex) {
         this.reportError(ex);
         this.recover(ex, _tokenSet_15);
      }

      this.returnAST = groupByClause_AST;
   }

   public final void orderByClause() throws RecognitionException, TokenStreamException {
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST orderByClause_AST = null;

      try {
         AST tmp25_AST = null;
         tmp25_AST = this.astFactory.create(this.LT(1));
         this.astFactory.makeASTRoot(currentAST, tmp25_AST);
         this.match(41);
         this.match(105);
         this.orderElement();
         this.astFactory.addASTChild(currentAST, this.returnAST);

         while(this.LA(1) == 101) {
            this.match(101);
            this.orderElement();
            this.astFactory.addASTChild(currentAST, this.returnAST);
         }

         orderByClause_AST = currentAST.root;
      } catch (RecognitionException ex) {
         this.reportError(ex);
         this.recover(ex, _tokenSet_9);
      }

      this.returnAST = orderByClause_AST;
   }

   public final void selectClause() throws RecognitionException, TokenStreamException {
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST selectClause_AST = null;

      try {
         AST tmp28_AST = null;
         tmp28_AST = this.astFactory.create(this.LT(1));
         this.astFactory.makeASTRoot(currentAST, tmp28_AST);
         this.match(45);
         this.weakKeywords();
         switch (this.LA(1)) {
            case 6:
            case 7:
            case 8:
            case 10:
            case 11:
            case 13:
            case 14:
            case 15:
            case 18:
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
            case 40:
            case 41:
            case 42:
            case 43:
            case 44:
            case 45:
            case 46:
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
            case 63:
            case 64:
            case 66:
            case 67:
            case 68:
            case 69:
            case 70:
            case 71:
            case 72:
            case 73:
            case 74:
            case 75:
            case 76:
            case 77:
            case 78:
            case 79:
            case 80:
            case 81:
            case 82:
            case 83:
            case 84:
            case 85:
            case 86:
            case 87:
            case 88:
            case 89:
            case 90:
            case 91:
            case 92:
            case 93:
            case 94:
            case 100:
            case 101:
            case 102:
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
            case 117:
            case 118:
            case 119:
            case 120:
            case 121:
            default:
               throw new NoViableAltException(this.LT(1), this.getFilename());
            case 16:
               AST tmp29_AST = null;
               tmp29_AST = this.astFactory.create(this.LT(1));
               this.astFactory.addASTChild(currentAST, tmp29_AST);
               this.match(16);
            case 4:
            case 5:
            case 9:
            case 12:
            case 17:
            case 19:
            case 20:
            case 27:
            case 35:
            case 36:
            case 37:
            case 38:
            case 39:
            case 47:
            case 48:
            case 49:
            case 54:
            case 62:
            case 65:
            case 95:
            case 96:
            case 97:
            case 98:
            case 99:
            case 103:
            case 115:
            case 116:
            case 122:
            case 123:
            case 124:
            case 125:
            case 126:
               switch (this.LA(1)) {
                  case 4:
                  case 5:
                  case 9:
                  case 12:
                  case 17:
                  case 19:
                  case 20:
                  case 27:
                  case 35:
                  case 36:
                  case 38:
                  case 39:
                  case 47:
                  case 48:
                  case 49:
                  case 54:
                  case 62:
                  case 95:
                  case 96:
                  case 97:
                  case 98:
                  case 99:
                  case 103:
                  case 115:
                  case 116:
                  case 122:
                  case 123:
                  case 124:
                  case 125:
                  case 126:
                     this.selectedPropertiesList();
                     this.astFactory.addASTChild(currentAST, this.returnAST);
                     break;
                  case 6:
                  case 7:
                  case 8:
                  case 10:
                  case 11:
                  case 13:
                  case 14:
                  case 15:
                  case 16:
                  case 18:
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
                  case 40:
                  case 41:
                  case 42:
                  case 43:
                  case 44:
                  case 45:
                  case 46:
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
                  case 63:
                  case 64:
                  case 66:
                  case 67:
                  case 68:
                  case 69:
                  case 70:
                  case 71:
                  case 72:
                  case 73:
                  case 74:
                  case 75:
                  case 76:
                  case 77:
                  case 78:
                  case 79:
                  case 80:
                  case 81:
                  case 82:
                  case 83:
                  case 84:
                  case 85:
                  case 86:
                  case 87:
                  case 88:
                  case 89:
                  case 90:
                  case 91:
                  case 92:
                  case 93:
                  case 94:
                  case 100:
                  case 101:
                  case 102:
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
                  case 117:
                  case 118:
                  case 119:
                  case 120:
                  case 121:
                  default:
                     throw new NoViableAltException(this.LT(1), this.getFilename());
                  case 37:
                     this.newExpression();
                     this.astFactory.addASTChild(currentAST, this.returnAST);
                     break;
                  case 65:
                     this.selectObject();
                     this.astFactory.addASTChild(currentAST, this.returnAST);
               }

               selectClause_AST = currentAST.root;
         }
      } catch (RecognitionException ex) {
         this.reportError(ex);
         this.recover(ex, _tokenSet_16);
      }

      this.returnAST = selectClause_AST;
   }

   public final void fromClause() throws RecognitionException, TokenStreamException {
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST fromClause_AST = null;

      try {
         AST tmp30_AST = null;
         tmp30_AST = this.astFactory.create(this.LT(1));
         this.astFactory.makeASTRoot(currentAST, tmp30_AST);
         this.match(22);
         this.weakKeywords();
         this.fromRange();
         this.astFactory.addASTChild(currentAST, this.returnAST);

         label25:
         while(true) {
            switch (this.LA(1)) {
               case 23:
               case 28:
               case 32:
               case 33:
               case 44:
                  this.fromJoin();
                  this.astFactory.addASTChild(currentAST, this.returnAST);
                  break;
               case 101:
                  this.match(101);
                  this.weakKeywords();
                  this.fromRange();
                  this.astFactory.addASTChild(currentAST, this.returnAST);
                  break;
               default:
                  fromClause_AST = currentAST.root;
                  break label25;
            }
         }
      } catch (RecognitionException ex) {
         this.reportError(ex);
         this.recover(ex, _tokenSet_14);
      }

      this.returnAST = fromClause_AST;
   }

   public final void selectedPropertiesList() throws RecognitionException, TokenStreamException {
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST selectedPropertiesList_AST = null;

      try {
         this.aliasedExpression();
         this.astFactory.addASTChild(currentAST, this.returnAST);

         while(this.LA(1) == 101) {
            this.match(101);
            this.aliasedExpression();
            this.astFactory.addASTChild(currentAST, this.returnAST);
         }

         selectedPropertiesList_AST = currentAST.root;
      } catch (RecognitionException ex) {
         this.reportError(ex);
         this.recover(ex, _tokenSet_16);
      }

      this.returnAST = selectedPropertiesList_AST;
   }

   public final void newExpression() throws RecognitionException, TokenStreamException {
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST newExpression_AST = null;
      Token op = null;
      AST op_AST = null;

      try {
         this.match(37);
         this.path();
         this.astFactory.addASTChild(currentAST, this.returnAST);
         op = this.LT(1);
         op_AST = this.astFactory.create(op);
         this.astFactory.makeASTRoot(currentAST, op_AST);
         this.match(103);
         op_AST.setType(73);
         this.selectedPropertiesList();
         this.astFactory.addASTChild(currentAST, this.returnAST);
         this.match(104);
         newExpression_AST = currentAST.root;
      } catch (RecognitionException ex) {
         this.reportError(ex);
         this.recover(ex, _tokenSet_16);
      }

      this.returnAST = newExpression_AST;
   }

   public final void selectObject() throws RecognitionException, TokenStreamException {
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST selectObject_AST = null;

      try {
         AST tmp35_AST = null;
         tmp35_AST = this.astFactory.create(this.LT(1));
         this.astFactory.makeASTRoot(currentAST, tmp35_AST);
         this.match(65);
         this.match(103);
         this.identifier();
         this.astFactory.addASTChild(currentAST, this.returnAST);
         this.match(104);
         selectObject_AST = currentAST.root;
      } catch (RecognitionException ex) {
         this.reportError(ex);
         this.recover(ex, _tokenSet_16);
      }

      this.returnAST = selectObject_AST;
   }

   public final void identifier() throws RecognitionException, TokenStreamException {
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST identifier_AST = null;

      try {
         AST tmp38_AST = null;
         tmp38_AST = this.astFactory.create(this.LT(1));
         this.astFactory.addASTChild(currentAST, tmp38_AST);
         this.match(126);
         identifier_AST = currentAST.root;
      } catch (RecognitionException ex) {
         identifier_AST = this.handleIdentifierError(this.LT(1), ex);
      }

      this.returnAST = identifier_AST;
   }

   public final void fromRange() throws RecognitionException, TokenStreamException {
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST fromRange_AST = null;

      try {
         if (this.LA(1) == 126 && _tokenSet_17.member(this.LA(2))) {
            this.fromClassOrOuterQueryPath();
            this.astFactory.addASTChild(currentAST, this.returnAST);
            fromRange_AST = currentAST.root;
         } else if (this.LA(1) == 126 && this.LA(2) == 26 && this.LA(3) == 11) {
            this.inClassDeclaration();
            this.astFactory.addASTChild(currentAST, this.returnAST);
            fromRange_AST = currentAST.root;
         } else if (this.LA(1) == 26) {
            this.inCollectionDeclaration();
            this.astFactory.addASTChild(currentAST, this.returnAST);
            fromRange_AST = currentAST.root;
         } else {
            if (this.LA(1) != 126 || this.LA(2) != 26 || this.LA(3) != 17) {
               throw new NoViableAltException(this.LT(1), this.getFilename());
            }

            this.inCollectionElementsDeclaration();
            this.astFactory.addASTChild(currentAST, this.returnAST);
            fromRange_AST = currentAST.root;
         }
      } catch (RecognitionException ex) {
         this.reportError(ex);
         this.recover(ex, _tokenSet_18);
      }

      this.returnAST = fromRange_AST;
   }

   public final void fromJoin() throws RecognitionException, TokenStreamException {
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST fromJoin_AST = null;

      try {
         label49:
         switch (this.LA(1)) {
            case 23:
               AST tmp42_AST = null;
               tmp42_AST = this.astFactory.create(this.LT(1));
               this.astFactory.addASTChild(currentAST, tmp42_AST);
               this.match(23);
               break;
            case 28:
               AST tmp43_AST = null;
               tmp43_AST = this.astFactory.create(this.LT(1));
               this.astFactory.addASTChild(currentAST, tmp43_AST);
               this.match(28);
            case 32:
               break;
            case 33:
            case 44:
               switch (this.LA(1)) {
                  case 33:
                     AST tmp39_AST = null;
                     tmp39_AST = this.astFactory.create(this.LT(1));
                     this.astFactory.addASTChild(currentAST, tmp39_AST);
                     this.match(33);
                     break;
                  case 44:
                     AST tmp40_AST = null;
                     tmp40_AST = this.astFactory.create(this.LT(1));
                     this.astFactory.addASTChild(currentAST, tmp40_AST);
                     this.match(44);
                     break;
                  default:
                     throw new NoViableAltException(this.LT(1), this.getFilename());
               }

               switch (this.LA(1)) {
                  case 32:
                     break label49;
                  case 42:
                     AST tmp41_AST = null;
                     tmp41_AST = this.astFactory.create(this.LT(1));
                     this.astFactory.addASTChild(currentAST, tmp41_AST);
                     this.match(42);
                     break label49;
                  default:
                     throw new NoViableAltException(this.LT(1), this.getFilename());
               }
            default:
               throw new NoViableAltException(this.LT(1), this.getFilename());
         }

         AST tmp44_AST = null;
         tmp44_AST = this.astFactory.create(this.LT(1));
         this.astFactory.makeASTRoot(currentAST, tmp44_AST);
         this.match(32);
         label43:
         switch (this.LA(1)) {
            case 21:
               AST tmp45_AST = null;
               tmp45_AST = this.astFactory.create(this.LT(1));
               this.astFactory.addASTChild(currentAST, tmp45_AST);
               this.match(21);
            case 126:
               this.path();
               this.astFactory.addASTChild(currentAST, this.returnAST);
               switch (this.LA(1)) {
                  case 7:
                  case 126:
                     this.asAlias();
                     this.astFactory.addASTChild(currentAST, this.returnAST);
                  case 1:
                  case 21:
                  case 23:
                  case 24:
                  case 28:
                  case 32:
                  case 33:
                  case 41:
                  case 44:
                  case 50:
                  case 53:
                  case 60:
                  case 101:
                  case 104:
                     switch (this.LA(1)) {
                        case 21:
                           this.propertyFetch();
                           this.astFactory.addASTChild(currentAST, this.returnAST);
                        case 1:
                        case 23:
                        case 24:
                        case 28:
                        case 32:
                        case 33:
                        case 41:
                        case 44:
                        case 50:
                        case 53:
                        case 60:
                        case 101:
                        case 104:
                           switch (this.LA(1)) {
                              case 60:
                                 this.withClause();
                                 this.astFactory.addASTChild(currentAST, this.returnAST);
                              case 1:
                              case 23:
                              case 24:
                              case 28:
                              case 32:
                              case 33:
                              case 41:
                              case 44:
                              case 50:
                              case 53:
                              case 101:
                              case 104:
                                 fromJoin_AST = currentAST.root;
                                 break label43;
                              default:
                                 throw new NoViableAltException(this.LT(1), this.getFilename());
                           }
                        default:
                           throw new NoViableAltException(this.LT(1), this.getFilename());
                     }
                  default:
                     throw new NoViableAltException(this.LT(1), this.getFilename());
               }
            default:
               throw new NoViableAltException(this.LT(1), this.getFilename());
         }
      } catch (RecognitionException ex) {
         this.reportError(ex);
         this.recover(ex, _tokenSet_18);
      }

      this.returnAST = fromJoin_AST;
   }

   public final void propertyFetch() throws RecognitionException, TokenStreamException {
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST propertyFetch_AST = null;

      try {
         AST tmp46_AST = null;
         tmp46_AST = this.astFactory.create(this.LT(1));
         this.astFactory.addASTChild(currentAST, tmp46_AST);
         this.match(21);
         this.match(4);
         this.match(43);
         propertyFetch_AST = currentAST.root;
      } catch (RecognitionException ex) {
         this.reportError(ex);
         this.recover(ex, _tokenSet_19);
      }

      this.returnAST = propertyFetch_AST;
   }

   public final void withClause() throws RecognitionException, TokenStreamException {
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST withClause_AST = null;

      try {
         AST tmp49_AST = null;
         tmp49_AST = this.astFactory.create(this.LT(1));
         this.astFactory.makeASTRoot(currentAST, tmp49_AST);
         this.match(60);
         this.logicalExpression();
         this.astFactory.addASTChild(currentAST, this.returnAST);
         withClause_AST = currentAST.root;
      } catch (RecognitionException ex) {
         this.reportError(ex);
         this.recover(ex, _tokenSet_18);
      }

      this.returnAST = withClause_AST;
   }

   public final void logicalExpression() throws RecognitionException, TokenStreamException {
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST logicalExpression_AST = null;

      try {
         this.expression();
         this.astFactory.addASTChild(currentAST, this.returnAST);
         logicalExpression_AST = currentAST.root;
      } catch (RecognitionException ex) {
         this.reportError(ex);
         this.recover(ex, _tokenSet_20);
      }

      this.returnAST = logicalExpression_AST;
   }

   public final void fromClassOrOuterQueryPath() throws RecognitionException, TokenStreamException {
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST fromClassOrOuterQueryPath_AST = null;
      AST c_AST = null;
      AST a_AST = null;
      AST p_AST = null;

      try {
         this.path();
         c_AST = this.returnAST;
         this.weakKeywords();
         switch (this.LA(1)) {
            case 7:
            case 126:
               this.asAlias();
               a_AST = this.returnAST;
            case 1:
            case 21:
            case 23:
            case 24:
            case 28:
            case 32:
            case 33:
            case 41:
            case 44:
            case 50:
            case 53:
            case 101:
            case 104:
               switch (this.LA(1)) {
                  case 1:
                  case 23:
                  case 24:
                  case 28:
                  case 32:
                  case 33:
                  case 41:
                  case 44:
                  case 50:
                  case 53:
                  case 101:
                  case 104:
                     break;
                  case 21:
                     this.propertyFetch();
                     p_AST = this.returnAST;
                     break;
                  default:
                     throw new NoViableAltException(this.LT(1), this.getFilename());
               }

               AST var8 = currentAST.root;
               fromClassOrOuterQueryPath_AST = this.astFactory.make((new ASTArray(4)).add(this.astFactory.create(87, "RANGE")).add(c_AST).add(a_AST).add(p_AST));
               currentAST.root = fromClassOrOuterQueryPath_AST;
               currentAST.child = fromClassOrOuterQueryPath_AST != null && fromClassOrOuterQueryPath_AST.getFirstChild() != null ? fromClassOrOuterQueryPath_AST.getFirstChild() : fromClassOrOuterQueryPath_AST;
               currentAST.advanceChildToEnd();
               break;
            default:
               throw new NoViableAltException(this.LT(1), this.getFilename());
         }
      } catch (RecognitionException ex) {
         this.reportError(ex);
         this.recover(ex, _tokenSet_18);
      }

      this.returnAST = fromClassOrOuterQueryPath_AST;
   }

   public final void inClassDeclaration() throws RecognitionException, TokenStreamException {
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST inClassDeclaration_AST = null;
      AST a_AST = null;
      AST c_AST = null;

      try {
         this.alias();
         a_AST = this.returnAST;
         this.match(26);
         this.match(11);
         this.path();
         c_AST = this.returnAST;
         AST var7 = currentAST.root;
         inClassDeclaration_AST = this.astFactory.make((new ASTArray(3)).add(this.astFactory.create(87, "RANGE")).add(c_AST).add(a_AST));
         currentAST.root = inClassDeclaration_AST;
         currentAST.child = inClassDeclaration_AST != null && inClassDeclaration_AST.getFirstChild() != null ? inClassDeclaration_AST.getFirstChild() : inClassDeclaration_AST;
         currentAST.advanceChildToEnd();
      } catch (RecognitionException ex) {
         this.reportError(ex);
         this.recover(ex, _tokenSet_18);
      }

      this.returnAST = inClassDeclaration_AST;
   }

   public final void inCollectionDeclaration() throws RecognitionException, TokenStreamException {
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST inCollectionDeclaration_AST = null;
      AST p_AST = null;
      AST a_AST = null;

      try {
         this.match(26);
         this.match(103);
         this.path();
         p_AST = this.returnAST;
         this.match(104);
         this.asAlias();
         a_AST = this.returnAST;
         AST var7 = currentAST.root;
         inCollectionDeclaration_AST = this.astFactory.make((new ASTArray(4)).add(this.astFactory.create(32, "join")).add(this.astFactory.create(28, "inner")).add(p_AST).add(a_AST));
         currentAST.root = inCollectionDeclaration_AST;
         currentAST.child = inCollectionDeclaration_AST != null && inCollectionDeclaration_AST.getFirstChild() != null ? inCollectionDeclaration_AST.getFirstChild() : inCollectionDeclaration_AST;
         currentAST.advanceChildToEnd();
      } catch (RecognitionException ex) {
         this.reportError(ex);
         this.recover(ex, _tokenSet_18);
      }

      this.returnAST = inCollectionDeclaration_AST;
   }

   public final void inCollectionElementsDeclaration() throws RecognitionException, TokenStreamException {
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST inCollectionElementsDeclaration_AST = null;
      AST a_AST = null;
      AST p_AST = null;

      try {
         this.alias();
         a_AST = this.returnAST;
         this.match(26);
         this.match(17);
         this.match(103);
         this.path();
         p_AST = this.returnAST;
         this.match(104);
         AST var7 = currentAST.root;
         inCollectionElementsDeclaration_AST = this.astFactory.make((new ASTArray(4)).add(this.astFactory.create(32, "join")).add(this.astFactory.create(28, "inner")).add(p_AST).add(a_AST));
         currentAST.root = inCollectionElementsDeclaration_AST;
         currentAST.child = inCollectionElementsDeclaration_AST != null && inCollectionElementsDeclaration_AST.getFirstChild() != null ? inCollectionElementsDeclaration_AST.getFirstChild() : inCollectionElementsDeclaration_AST;
         currentAST.advanceChildToEnd();
      } catch (RecognitionException ex) {
         this.reportError(ex);
         this.recover(ex, _tokenSet_18);
      }

      this.returnAST = inCollectionElementsDeclaration_AST;
   }

   public final void alias() throws RecognitionException, TokenStreamException {
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST alias_AST = null;
      AST a_AST = null;

      try {
         this.identifier();
         a_AST = this.returnAST;
         this.astFactory.addASTChild(currentAST, this.returnAST);
         a_AST.setType(72);
         alias_AST = currentAST.root;
      } catch (RecognitionException ex) {
         this.reportError(ex);
         this.recover(ex, _tokenSet_21);
      }

      this.returnAST = alias_AST;
   }

   public final void expression() throws RecognitionException, TokenStreamException {
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST expression_AST = null;

      try {
         this.logicalOrExpression();
         this.astFactory.addASTChild(currentAST, this.returnAST);
         expression_AST = currentAST.root;
      } catch (RecognitionException ex) {
         this.reportError(ex);
         this.recover(ex, _tokenSet_22);
      }

      this.returnAST = expression_AST;
   }

   public final void havingClause() throws RecognitionException, TokenStreamException {
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST havingClause_AST = null;

      try {
         AST tmp59_AST = null;
         tmp59_AST = this.astFactory.create(this.LT(1));
         this.astFactory.makeASTRoot(currentAST, tmp59_AST);
         this.match(25);
         this.logicalExpression();
         this.astFactory.addASTChild(currentAST, this.returnAST);
         havingClause_AST = currentAST.root;
      } catch (RecognitionException ex) {
         this.reportError(ex);
         this.recover(ex, _tokenSet_15);
      }

      this.returnAST = havingClause_AST;
   }

   public final void orderElement() throws RecognitionException, TokenStreamException {
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST orderElement_AST = null;

      try {
         this.expression();
         this.astFactory.addASTChild(currentAST, this.returnAST);
         switch (this.LA(1)) {
            case 8:
            case 14:
            case 106:
            case 107:
               this.ascendingOrDescending();
               this.astFactory.addASTChild(currentAST, this.returnAST);
            case 1:
            case 50:
            case 101:
            case 104:
               orderElement_AST = currentAST.root;
               break;
            default:
               throw new NoViableAltException(this.LT(1), this.getFilename());
         }
      } catch (RecognitionException ex) {
         this.reportError(ex);
         this.recover(ex, _tokenSet_23);
      }

      this.returnAST = orderElement_AST;
   }

   public final void ascendingOrDescending() throws RecognitionException, TokenStreamException {
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST ascendingOrDescending_AST = null;

      try {
         switch (this.LA(1)) {
            case 8:
            case 106:
               switch (this.LA(1)) {
                  case 8:
                     AST tmp60_AST = null;
                     tmp60_AST = this.astFactory.create(this.LT(1));
                     this.astFactory.addASTChild(currentAST, tmp60_AST);
                     this.match(8);
                     break;
                  case 106:
                     AST tmp61_AST = null;
                     tmp61_AST = this.astFactory.create(this.LT(1));
                     this.astFactory.addASTChild(currentAST, tmp61_AST);
                     this.match(106);
                     break;
                  default:
                     throw new NoViableAltException(this.LT(1), this.getFilename());
               }

               ascendingOrDescending_AST = currentAST.root;
               ascendingOrDescending_AST.setType(8);
               ascendingOrDescending_AST = currentAST.root;
               break;
            case 14:
            case 107:
               switch (this.LA(1)) {
                  case 14:
                     AST tmp62_AST = null;
                     tmp62_AST = this.astFactory.create(this.LT(1));
                     this.astFactory.addASTChild(currentAST, tmp62_AST);
                     this.match(14);
                     break;
                  case 107:
                     AST tmp63_AST = null;
                     tmp63_AST = this.astFactory.create(this.LT(1));
                     this.astFactory.addASTChild(currentAST, tmp63_AST);
                     this.match(107);
                     break;
                  default:
                     throw new NoViableAltException(this.LT(1), this.getFilename());
               }

               ascendingOrDescending_AST = currentAST.root;
               ascendingOrDescending_AST.setType(14);
               ascendingOrDescending_AST = currentAST.root;
               break;
            default:
               throw new NoViableAltException(this.LT(1), this.getFilename());
         }
      } catch (RecognitionException ex) {
         this.reportError(ex);
         this.recover(ex, _tokenSet_23);
      }

      this.returnAST = ascendingOrDescending_AST;
   }

   public final void aliasedExpression() throws RecognitionException, TokenStreamException {
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST aliasedExpression_AST = null;

      try {
         this.expression();
         this.astFactory.addASTChild(currentAST, this.returnAST);
         switch (this.LA(1)) {
            case 7:
               AST tmp64_AST = null;
               tmp64_AST = this.astFactory.create(this.LT(1));
               this.astFactory.makeASTRoot(currentAST, tmp64_AST);
               this.match(7);
               this.identifier();
               this.astFactory.addASTChild(currentAST, this.returnAST);
            case 1:
            case 22:
            case 24:
            case 41:
            case 50:
            case 53:
            case 101:
            case 104:
               aliasedExpression_AST = currentAST.root;
               break;
            default:
               throw new NoViableAltException(this.LT(1), this.getFilename());
         }
      } catch (RecognitionException ex) {
         this.reportError(ex);
         this.recover(ex, _tokenSet_24);
      }

      this.returnAST = aliasedExpression_AST;
   }

   public final void logicalOrExpression() throws RecognitionException, TokenStreamException {
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST logicalOrExpression_AST = null;

      try {
         this.logicalAndExpression();
         this.astFactory.addASTChild(currentAST, this.returnAST);

         while(this.LA(1) == 40) {
            AST tmp65_AST = null;
            tmp65_AST = this.astFactory.create(this.LT(1));
            this.astFactory.makeASTRoot(currentAST, tmp65_AST);
            this.match(40);
            this.logicalAndExpression();
            this.astFactory.addASTChild(currentAST, this.returnAST);
         }

         logicalOrExpression_AST = currentAST.root;
      } catch (RecognitionException ex) {
         this.reportError(ex);
         this.recover(ex, _tokenSet_22);
      }

      this.returnAST = logicalOrExpression_AST;
   }

   public final void logicalAndExpression() throws RecognitionException, TokenStreamException {
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST logicalAndExpression_AST = null;

      try {
         this.negatedExpression();
         this.astFactory.addASTChild(currentAST, this.returnAST);

         while(this.LA(1) == 6) {
            AST tmp66_AST = null;
            tmp66_AST = this.astFactory.create(this.LT(1));
            this.astFactory.makeASTRoot(currentAST, tmp66_AST);
            this.match(6);
            this.negatedExpression();
            this.astFactory.addASTChild(currentAST, this.returnAST);
         }

         logicalAndExpression_AST = currentAST.root;
      } catch (RecognitionException ex) {
         this.reportError(ex);
         this.recover(ex, _tokenSet_25);
      }

      this.returnAST = logicalAndExpression_AST;
   }

   public final void negatedExpression() throws RecognitionException, TokenStreamException {
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST negatedExpression_AST = null;
      AST x_AST = null;
      AST y_AST = null;
      this.weakKeywords();

      try {
         switch (this.LA(1)) {
            case 4:
            case 5:
            case 9:
            case 12:
            case 17:
            case 19:
            case 20:
            case 27:
            case 35:
            case 36:
            case 39:
            case 47:
            case 48:
            case 49:
            case 54:
            case 62:
            case 95:
            case 96:
            case 97:
            case 98:
            case 99:
            case 103:
            case 115:
            case 116:
            case 122:
            case 123:
            case 124:
            case 125:
            case 126:
               this.equalityExpression();
               y_AST = this.returnAST;
               negatedExpression_AST = currentAST.root;
               negatedExpression_AST = y_AST;
               currentAST.root = y_AST;
               currentAST.child = y_AST != null && y_AST.getFirstChild() != null ? y_AST.getFirstChild() : y_AST;
               currentAST.advanceChildToEnd();
               break;
            case 6:
            case 7:
            case 8:
            case 10:
            case 11:
            case 13:
            case 14:
            case 15:
            case 16:
            case 18:
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
            case 37:
            case 40:
            case 41:
            case 42:
            case 43:
            case 44:
            case 45:
            case 46:
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
            case 63:
            case 64:
            case 65:
            case 66:
            case 67:
            case 68:
            case 69:
            case 70:
            case 71:
            case 72:
            case 73:
            case 74:
            case 75:
            case 76:
            case 77:
            case 78:
            case 79:
            case 80:
            case 81:
            case 82:
            case 83:
            case 84:
            case 85:
            case 86:
            case 87:
            case 88:
            case 89:
            case 90:
            case 91:
            case 92:
            case 93:
            case 94:
            case 100:
            case 101:
            case 102:
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
            case 117:
            case 118:
            case 119:
            case 120:
            case 121:
            default:
               throw new NoViableAltException(this.LT(1), this.getFilename());
            case 38:
               AST tmp67_AST = null;
               tmp67_AST = this.astFactory.create(this.LT(1));
               this.match(38);
               this.negatedExpression();
               x_AST = this.returnAST;
               AST var7 = currentAST.root;
               negatedExpression_AST = this.negateNode(x_AST);
               currentAST.root = negatedExpression_AST;
               currentAST.child = negatedExpression_AST != null && negatedExpression_AST.getFirstChild() != null ? negatedExpression_AST.getFirstChild() : negatedExpression_AST;
               currentAST.advanceChildToEnd();
         }
      } catch (RecognitionException ex) {
         this.reportError(ex);
         this.recover(ex, _tokenSet_26);
      }

      this.returnAST = negatedExpression_AST;
   }

   public final void equalityExpression() throws RecognitionException, TokenStreamException {
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST equalityExpression_AST = null;
      AST x_AST = null;
      Token is = null;
      AST is_AST = null;
      Token ne = null;
      AST ne_AST = null;
      AST y_AST = null;

      try {
         this.relationalExpression();
         x_AST = this.returnAST;
         this.astFactory.addASTChild(currentAST, this.returnAST);

         while(_tokenSet_27.member(this.LA(1))) {
            label30:
            switch (this.LA(1)) {
               case 31:
                  is = this.LT(1);
                  is_AST = this.astFactory.create(is);
                  this.astFactory.makeASTRoot(currentAST, is_AST);
                  this.match(31);
                  is_AST.setType(102);
                  switch (this.LA(1)) {
                     case 4:
                     case 5:
                     case 9:
                     case 12:
                     case 17:
                     case 19:
                     case 20:
                     case 27:
                     case 35:
                     case 36:
                     case 39:
                     case 47:
                     case 48:
                     case 49:
                     case 54:
                     case 62:
                     case 95:
                     case 96:
                     case 97:
                     case 98:
                     case 99:
                     case 103:
                     case 115:
                     case 116:
                     case 122:
                     case 123:
                     case 124:
                     case 125:
                     case 126:
                        break label30;
                     case 6:
                     case 7:
                     case 8:
                     case 10:
                     case 11:
                     case 13:
                     case 14:
                     case 15:
                     case 16:
                     case 18:
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
                     case 37:
                     case 40:
                     case 41:
                     case 42:
                     case 43:
                     case 44:
                     case 45:
                     case 46:
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
                     case 63:
                     case 64:
                     case 65:
                     case 66:
                     case 67:
                     case 68:
                     case 69:
                     case 70:
                     case 71:
                     case 72:
                     case 73:
                     case 74:
                     case 75:
                     case 76:
                     case 77:
                     case 78:
                     case 79:
                     case 80:
                     case 81:
                     case 82:
                     case 83:
                     case 84:
                     case 85:
                     case 86:
                     case 87:
                     case 88:
                     case 89:
                     case 90:
                     case 91:
                     case 92:
                     case 93:
                     case 94:
                     case 100:
                     case 101:
                     case 102:
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
                     case 117:
                     case 118:
                     case 119:
                     case 120:
                     case 121:
                     default:
                        throw new NoViableAltException(this.LT(1), this.getFilename());
                     case 38:
                        this.match(38);
                        is_AST.setType(108);
                        break label30;
                  }
               case 102:
                  AST tmp68_AST = null;
                  tmp68_AST = this.astFactory.create(this.LT(1));
                  this.astFactory.makeASTRoot(currentAST, tmp68_AST);
                  this.match(102);
                  break;
               case 108:
                  AST tmp70_AST = null;
                  tmp70_AST = this.astFactory.create(this.LT(1));
                  this.astFactory.makeASTRoot(currentAST, tmp70_AST);
                  this.match(108);
                  break;
               case 109:
                  ne = this.LT(1);
                  ne_AST = this.astFactory.create(ne);
                  this.astFactory.makeASTRoot(currentAST, ne_AST);
                  this.match(109);
                  ne_AST.setType(108);
                  break;
               default:
                  throw new NoViableAltException(this.LT(1), this.getFilename());
            }

            this.relationalExpression();
            y_AST = this.returnAST;
            this.astFactory.addASTChild(currentAST, this.returnAST);
         }

         equalityExpression_AST = currentAST.root;
         equalityExpression_AST = this.processEqualityExpression(equalityExpression_AST);
         currentAST.root = equalityExpression_AST;
         currentAST.child = equalityExpression_AST != null && equalityExpression_AST.getFirstChild() != null ? equalityExpression_AST.getFirstChild() : equalityExpression_AST;
         currentAST.advanceChildToEnd();
         equalityExpression_AST = currentAST.root;
      } catch (RecognitionException ex) {
         this.reportError(ex);
         this.recover(ex, _tokenSet_26);
      }

      this.returnAST = equalityExpression_AST;
   }

   public final void relationalExpression() throws RecognitionException, TokenStreamException {
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST relationalExpression_AST = null;
      Token n = null;
      AST n_AST = null;
      Token i = null;
      AST i_AST = null;
      Token b = null;
      AST b_AST = null;
      Token l = null;
      AST l_AST = null;
      AST p_AST = null;

      try {
         this.concatenation();
         this.astFactory.addASTChild(currentAST, this.returnAST);
         switch (this.LA(1)) {
            case 1:
            case 6:
            case 7:
            case 8:
            case 14:
            case 22:
            case 23:
            case 24:
            case 25:
            case 28:
            case 31:
            case 32:
            case 33:
            case 40:
            case 41:
            case 44:
            case 50:
            case 53:
            case 57:
            case 101:
            case 102:
            case 104:
            case 106:
            case 107:
            case 108:
            case 109:
            case 110:
            case 111:
            case 112:
            case 113:
            case 121:
               while(this.LA(1) >= 110 && this.LA(1) <= 113) {
                  switch (this.LA(1)) {
                     case 110:
                        AST tmp71_AST = null;
                        tmp71_AST = this.astFactory.create(this.LT(1));
                        this.astFactory.makeASTRoot(currentAST, tmp71_AST);
                        this.match(110);
                        break;
                     case 111:
                        AST tmp72_AST = null;
                        tmp72_AST = this.astFactory.create(this.LT(1));
                        this.astFactory.makeASTRoot(currentAST, tmp72_AST);
                        this.match(111);
                        break;
                     case 112:
                        AST tmp73_AST = null;
                        tmp73_AST = this.astFactory.create(this.LT(1));
                        this.astFactory.makeASTRoot(currentAST, tmp73_AST);
                        this.match(112);
                        break;
                     case 113:
                        AST tmp74_AST = null;
                        tmp74_AST = this.astFactory.create(this.LT(1));
                        this.astFactory.makeASTRoot(currentAST, tmp74_AST);
                        this.match(113);
                        break;
                     default:
                        throw new NoViableAltException(this.LT(1), this.getFilename());
                  }

                  this.additiveExpression();
                  this.astFactory.addASTChild(currentAST, this.returnAST);
               }
               break;
            case 2:
            case 3:
            case 4:
            case 5:
            case 9:
            case 11:
            case 12:
            case 13:
            case 15:
            case 16:
            case 17:
            case 18:
            case 19:
            case 20:
            case 21:
            case 27:
            case 29:
            case 30:
            case 35:
            case 36:
            case 37:
            case 39:
            case 42:
            case 43:
            case 45:
            case 46:
            case 47:
            case 48:
            case 49:
            case 51:
            case 52:
            case 54:
            case 55:
            case 56:
            case 58:
            case 59:
            case 60:
            case 61:
            case 62:
            case 63:
            case 65:
            case 66:
            case 67:
            case 68:
            case 69:
            case 70:
            case 71:
            case 72:
            case 73:
            case 74:
            case 75:
            case 76:
            case 77:
            case 78:
            case 79:
            case 80:
            case 81:
            case 82:
            case 83:
            case 84:
            case 85:
            case 86:
            case 87:
            case 88:
            case 89:
            case 90:
            case 91:
            case 92:
            case 93:
            case 94:
            case 95:
            case 96:
            case 97:
            case 98:
            case 99:
            case 100:
            case 103:
            case 105:
            case 114:
            case 115:
            case 116:
            case 117:
            case 118:
            case 119:
            case 120:
            default:
               throw new NoViableAltException(this.LT(1), this.getFilename());
            case 10:
            case 26:
            case 34:
            case 38:
            case 64:
               label68:
               switch (this.LA(1)) {
                  case 38:
                     n = this.LT(1);
                     this.astFactory.create(n);
                     this.match(38);
                  case 10:
                  case 26:
                  case 34:
                  case 64:
                     switch (this.LA(1)) {
                        case 10:
                           b = this.LT(1);
                           b_AST = this.astFactory.create(b);
                           this.astFactory.makeASTRoot(currentAST, b_AST);
                           this.match(10);
                           b_AST.setType(n == null ? 10 : 82);
                           b_AST.setText(n == null ? "between" : "not between");
                           this.betweenList();
                           this.astFactory.addASTChild(currentAST, this.returnAST);
                           break label68;
                        case 26:
                           i = this.LT(1);
                           i_AST = this.astFactory.create(i);
                           this.astFactory.makeASTRoot(currentAST, i_AST);
                           this.match(26);
                           i_AST.setType(n == null ? 26 : 83);
                           i_AST.setText(n == null ? "in" : "not in");
                           this.inList();
                           this.astFactory.addASTChild(currentAST, this.returnAST);
                           break label68;
                        case 34:
                           l = this.LT(1);
                           l_AST = this.astFactory.create(l);
                           this.astFactory.makeASTRoot(currentAST, l_AST);
                           this.match(34);
                           l_AST.setType(n == null ? 34 : 84);
                           l_AST.setText(n == null ? "like" : "not like");
                           this.concatenation();
                           this.astFactory.addASTChild(currentAST, this.returnAST);
                           this.likeEscape();
                           this.astFactory.addASTChild(currentAST, this.returnAST);
                           break label68;
                        case 64:
                           this.match(64);
                           switch (this.LA(1)) {
                              case 66:
                                 this.match(66);
                              case 126:
                                 this.path();
                                 p_AST = this.returnAST;
                                 this.processMemberOf(n, p_AST, currentAST);
                                 break label68;
                              default:
                                 throw new NoViableAltException(this.LT(1), this.getFilename());
                           }
                        default:
                           throw new NoViableAltException(this.LT(1), this.getFilename());
                     }
                  default:
                     throw new NoViableAltException(this.LT(1), this.getFilename());
               }
         }

         relationalExpression_AST = currentAST.root;
      } catch (RecognitionException ex) {
         this.reportError(ex);
         this.recover(ex, _tokenSet_28);
      }

      this.returnAST = relationalExpression_AST;
   }

   public final void additiveExpression() throws RecognitionException, TokenStreamException {
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST additiveExpression_AST = null;

      try {
         this.multiplyExpression();
         this.astFactory.addASTChild(currentAST, this.returnAST);

         while(this.LA(1) == 115 || this.LA(1) == 116) {
            switch (this.LA(1)) {
               case 115:
                  AST tmp77_AST = null;
                  tmp77_AST = this.astFactory.create(this.LT(1));
                  this.astFactory.makeASTRoot(currentAST, tmp77_AST);
                  this.match(115);
                  break;
               case 116:
                  AST tmp78_AST = null;
                  tmp78_AST = this.astFactory.create(this.LT(1));
                  this.astFactory.makeASTRoot(currentAST, tmp78_AST);
                  this.match(116);
                  break;
               default:
                  throw new NoViableAltException(this.LT(1), this.getFilename());
            }

            this.multiplyExpression();
            this.astFactory.addASTChild(currentAST, this.returnAST);
         }

         additiveExpression_AST = currentAST.root;
      } catch (RecognitionException ex) {
         this.reportError(ex);
         this.recover(ex, _tokenSet_29);
      }

      this.returnAST = additiveExpression_AST;
   }

   public final void inList() throws RecognitionException, TokenStreamException {
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST inList_AST = null;
      AST x_AST = null;

      try {
         this.compoundExpr();
         x_AST = this.returnAST;
         this.astFactory.addASTChild(currentAST, this.returnAST);
         inList_AST = currentAST.root;
         inList_AST = this.astFactory.make((new ASTArray(2)).add(this.astFactory.create(77, "inList")).add(inList_AST));
         currentAST.root = inList_AST;
         currentAST.child = inList_AST != null && inList_AST.getFirstChild() != null ? inList_AST.getFirstChild() : inList_AST;
         currentAST.advanceChildToEnd();
         inList_AST = currentAST.root;
      } catch (RecognitionException ex) {
         this.reportError(ex);
         this.recover(ex, _tokenSet_28);
      }

      this.returnAST = inList_AST;
   }

   public final void betweenList() throws RecognitionException, TokenStreamException {
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST betweenList_AST = null;

      try {
         this.concatenation();
         this.astFactory.addASTChild(currentAST, this.returnAST);
         this.match(6);
         this.concatenation();
         this.astFactory.addASTChild(currentAST, this.returnAST);
         betweenList_AST = currentAST.root;
      } catch (RecognitionException ex) {
         this.reportError(ex);
         this.recover(ex, _tokenSet_28);
      }

      this.returnAST = betweenList_AST;
   }

   public final void likeEscape() throws RecognitionException, TokenStreamException {
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST likeEscape_AST = null;

      try {
         switch (this.LA(1)) {
            case 2:
            case 3:
            case 4:
            case 5:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 15:
            case 16:
            case 17:
            case 19:
            case 20:
            case 21:
            case 26:
            case 27:
            case 29:
            case 30:
            case 34:
            case 35:
            case 36:
            case 37:
            case 38:
            case 39:
            case 42:
            case 43:
            case 45:
            case 46:
            case 47:
            case 48:
            case 49:
            case 51:
            case 52:
            case 54:
            case 55:
            case 56:
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
            case 68:
            case 69:
            case 70:
            case 71:
            case 72:
            case 73:
            case 74:
            case 75:
            case 76:
            case 77:
            case 78:
            case 79:
            case 80:
            case 81:
            case 82:
            case 83:
            case 84:
            case 85:
            case 86:
            case 87:
            case 88:
            case 89:
            case 90:
            case 91:
            case 92:
            case 93:
            case 94:
            case 95:
            case 96:
            case 97:
            case 98:
            case 99:
            case 100:
            case 103:
            case 105:
            case 110:
            case 111:
            case 112:
            case 113:
            case 114:
            case 115:
            case 116:
            case 117:
            case 118:
            case 119:
            case 120:
            default:
               throw new NoViableAltException(this.LT(1), this.getFilename());
            case 18:
               AST tmp80_AST = null;
               tmp80_AST = this.astFactory.create(this.LT(1));
               this.astFactory.makeASTRoot(currentAST, tmp80_AST);
               this.match(18);
               this.concatenation();
               this.astFactory.addASTChild(currentAST, this.returnAST);
            case 1:
            case 6:
            case 7:
            case 8:
            case 14:
            case 22:
            case 23:
            case 24:
            case 25:
            case 28:
            case 31:
            case 32:
            case 33:
            case 40:
            case 41:
            case 44:
            case 50:
            case 53:
            case 57:
            case 101:
            case 102:
            case 104:
            case 106:
            case 107:
            case 108:
            case 109:
            case 121:
               likeEscape_AST = currentAST.root;
         }
      } catch (RecognitionException ex) {
         this.reportError(ex);
         this.recover(ex, _tokenSet_28);
      }

      this.returnAST = likeEscape_AST;
   }

   public final void compoundExpr() throws RecognitionException, TokenStreamException {
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST compoundExpr_AST = null;

      try {
         switch (this.LA(1)) {
            case 17:
            case 27:
               this.collectionExpr();
               this.astFactory.addASTChild(currentAST, this.returnAST);
               compoundExpr_AST = currentAST.root;
               break;
            case 122:
            case 123:
               this.parameter();
               this.astFactory.addASTChild(currentAST, this.returnAST);
               compoundExpr_AST = currentAST.root;
               break;
            case 126:
               this.path();
               this.astFactory.addASTChild(currentAST, this.returnAST);
               compoundExpr_AST = currentAST.root;
               break;
            default:
               if (this.LA(1) == 103 && this.LA(2) == 104 && _tokenSet_28.member(this.LA(3)) && this.LA(1) == 103 && this.LA(2) == 104) {
                  this.match(103);
                  this.match(104);
                  compoundExpr_AST = currentAST.root;
               } else {
                  if (this.LA(1) != 103 || !_tokenSet_30.member(this.LA(2)) || !_tokenSet_31.member(this.LA(3))) {
                     throw new NoViableAltException(this.LT(1), this.getFilename());
                  }

                  this.match(103);
                  switch (this.LA(1)) {
                     case 4:
                     case 5:
                     case 9:
                     case 12:
                     case 17:
                     case 19:
                     case 20:
                     case 27:
                     case 35:
                     case 36:
                     case 38:
                     case 39:
                     case 47:
                     case 48:
                     case 49:
                     case 54:
                     case 62:
                     case 95:
                     case 96:
                     case 97:
                     case 98:
                     case 99:
                     case 103:
                     case 115:
                     case 116:
                     case 122:
                     case 123:
                     case 124:
                     case 125:
                     case 126:
                        this.expression();
                        this.astFactory.addASTChild(currentAST, this.returnAST);

                        while(this.LA(1) == 101) {
                           this.match(101);
                           this.expression();
                           this.astFactory.addASTChild(currentAST, this.returnAST);
                        }
                        break;
                     case 6:
                     case 7:
                     case 8:
                     case 10:
                     case 11:
                     case 13:
                     case 14:
                     case 15:
                     case 16:
                     case 18:
                     case 21:
                     case 23:
                     case 25:
                     case 26:
                     case 28:
                     case 29:
                     case 30:
                     case 31:
                     case 32:
                     case 33:
                     case 34:
                     case 37:
                     case 40:
                     case 42:
                     case 43:
                     case 44:
                     case 46:
                     case 51:
                     case 52:
                     case 55:
                     case 56:
                     case 57:
                     case 58:
                     case 59:
                     case 60:
                     case 61:
                     case 63:
                     case 64:
                     case 65:
                     case 66:
                     case 67:
                     case 68:
                     case 69:
                     case 70:
                     case 71:
                     case 72:
                     case 73:
                     case 74:
                     case 75:
                     case 76:
                     case 77:
                     case 78:
                     case 79:
                     case 80:
                     case 81:
                     case 82:
                     case 83:
                     case 84:
                     case 85:
                     case 86:
                     case 87:
                     case 88:
                     case 89:
                     case 90:
                     case 91:
                     case 92:
                     case 93:
                     case 94:
                     case 100:
                     case 101:
                     case 102:
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
                     case 117:
                     case 118:
                     case 119:
                     case 120:
                     case 121:
                     default:
                        throw new NoViableAltException(this.LT(1), this.getFilename());
                     case 22:
                     case 24:
                     case 41:
                     case 45:
                     case 50:
                     case 53:
                     case 104:
                        this.subQuery();
                        this.astFactory.addASTChild(currentAST, this.returnAST);
                  }

                  this.match(104);
                  compoundExpr_AST = currentAST.root;
               }
         }
      } catch (RecognitionException ex) {
         this.reportError(ex);
         this.recover(ex, _tokenSet_28);
      }

      this.returnAST = compoundExpr_AST;
   }

   public final void multiplyExpression() throws RecognitionException, TokenStreamException {
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST multiplyExpression_AST = null;

      try {
         this.unaryExpression();
         this.astFactory.addASTChild(currentAST, this.returnAST);

         while(this.LA(1) >= 117 && this.LA(1) <= 119) {
            switch (this.LA(1)) {
               case 117:
                  AST tmp86_AST = null;
                  tmp86_AST = this.astFactory.create(this.LT(1));
                  this.astFactory.makeASTRoot(currentAST, tmp86_AST);
                  this.match(117);
                  break;
               case 118:
                  AST tmp87_AST = null;
                  tmp87_AST = this.astFactory.create(this.LT(1));
                  this.astFactory.makeASTRoot(currentAST, tmp87_AST);
                  this.match(118);
                  break;
               case 119:
                  AST tmp88_AST = null;
                  tmp88_AST = this.astFactory.create(this.LT(1));
                  this.astFactory.makeASTRoot(currentAST, tmp88_AST);
                  this.match(119);
                  break;
               default:
                  throw new NoViableAltException(this.LT(1), this.getFilename());
            }

            this.unaryExpression();
            this.astFactory.addASTChild(currentAST, this.returnAST);
         }

         multiplyExpression_AST = currentAST.root;
      } catch (RecognitionException ex) {
         this.reportError(ex);
         this.recover(ex, _tokenSet_32);
      }

      this.returnAST = multiplyExpression_AST;
   }

   public final void unaryExpression() throws RecognitionException, TokenStreamException {
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST unaryExpression_AST = null;

      try {
         switch (this.LA(1)) {
            case 4:
            case 5:
            case 19:
            case 47:
               this.quantifiedExpression();
               this.astFactory.addASTChild(currentAST, this.returnAST);
               unaryExpression_AST = currentAST.root;
               break;
            case 6:
            case 7:
            case 8:
            case 10:
            case 11:
            case 13:
            case 14:
            case 15:
            case 16:
            case 18:
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
            case 37:
            case 38:
            case 40:
            case 41:
            case 42:
            case 43:
            case 44:
            case 45:
            case 46:
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
            case 63:
            case 64:
            case 65:
            case 66:
            case 67:
            case 68:
            case 69:
            case 70:
            case 71:
            case 72:
            case 73:
            case 74:
            case 75:
            case 76:
            case 77:
            case 78:
            case 79:
            case 80:
            case 81:
            case 82:
            case 83:
            case 84:
            case 85:
            case 86:
            case 87:
            case 88:
            case 89:
            case 90:
            case 91:
            case 92:
            case 93:
            case 94:
            case 100:
            case 101:
            case 102:
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
            case 117:
            case 118:
            case 119:
            case 120:
            case 121:
            default:
               throw new NoViableAltException(this.LT(1), this.getFilename());
            case 9:
            case 12:
            case 17:
            case 20:
            case 27:
            case 35:
            case 36:
            case 39:
            case 48:
            case 49:
            case 62:
            case 95:
            case 96:
            case 97:
            case 98:
            case 99:
            case 103:
            case 122:
            case 123:
            case 124:
            case 125:
            case 126:
               this.atom();
               this.astFactory.addASTChild(currentAST, this.returnAST);
               unaryExpression_AST = currentAST.root;
               break;
            case 54:
               this.caseExpression();
               this.astFactory.addASTChild(currentAST, this.returnAST);
               unaryExpression_AST = currentAST.root;
               break;
            case 115:
               AST tmp90_AST = null;
               tmp90_AST = this.astFactory.create(this.LT(1));
               this.astFactory.makeASTRoot(currentAST, tmp90_AST);
               this.match(115);
               tmp90_AST.setType(91);
               this.unaryExpression();
               this.astFactory.addASTChild(currentAST, this.returnAST);
               unaryExpression_AST = currentAST.root;
               break;
            case 116:
               AST tmp89_AST = null;
               tmp89_AST = this.astFactory.create(this.LT(1));
               this.astFactory.makeASTRoot(currentAST, tmp89_AST);
               this.match(116);
               tmp89_AST.setType(90);
               this.unaryExpression();
               this.astFactory.addASTChild(currentAST, this.returnAST);
               unaryExpression_AST = currentAST.root;
         }
      } catch (RecognitionException ex) {
         this.reportError(ex);
         this.recover(ex, _tokenSet_33);
      }

      this.returnAST = unaryExpression_AST;
   }

   public final void caseExpression() throws RecognitionException, TokenStreamException {
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST caseExpression_AST = null;

      try {
         if (this.LA(1) == 54 && this.LA(2) == 58) {
            AST tmp91_AST = null;
            tmp91_AST = this.astFactory.create(this.LT(1));
            this.astFactory.makeASTRoot(currentAST, tmp91_AST);
            this.match(54);

            int _cnt130;
            for(_cnt130 = 0; this.LA(1) == 58; ++_cnt130) {
               this.whenClause();
               this.astFactory.addASTChild(currentAST, this.returnAST);
            }

            if (_cnt130 < 1) {
               throw new NoViableAltException(this.LT(1), this.getFilename());
            }

            switch (this.LA(1)) {
               case 56:
                  this.elseClause();
                  this.astFactory.addASTChild(currentAST, this.returnAST);
               case 55:
                  this.match(55);
                  caseExpression_AST = currentAST.root;
                  break;
               default:
                  throw new NoViableAltException(this.LT(1), this.getFilename());
            }
         } else {
            if (this.LA(1) != 54 || !_tokenSet_34.member(this.LA(2))) {
               throw new NoViableAltException(this.LT(1), this.getFilename());
            }

            AST tmp93_AST = null;
            tmp93_AST = this.astFactory.create(this.LT(1));
            this.astFactory.makeASTRoot(currentAST, tmp93_AST);
            this.match(54);
            tmp93_AST.setType(74);
            this.unaryExpression();
            this.astFactory.addASTChild(currentAST, this.returnAST);

            int _cnt133;
            for(_cnt133 = 0; this.LA(1) == 58; ++_cnt133) {
               this.altWhenClause();
               this.astFactory.addASTChild(currentAST, this.returnAST);
            }

            if (_cnt133 < 1) {
               throw new NoViableAltException(this.LT(1), this.getFilename());
            }

            switch (this.LA(1)) {
               case 56:
                  this.elseClause();
                  this.astFactory.addASTChild(currentAST, this.returnAST);
               case 55:
                  this.match(55);
                  caseExpression_AST = currentAST.root;
                  break;
               default:
                  throw new NoViableAltException(this.LT(1), this.getFilename());
            }
         }
      } catch (RecognitionException ex) {
         this.reportError(ex);
         this.recover(ex, _tokenSet_33);
      }

      this.returnAST = caseExpression_AST;
   }

   public final void quantifiedExpression() throws RecognitionException, TokenStreamException {
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST quantifiedExpression_AST = null;

      try {
         switch (this.LA(1)) {
            case 4:
               AST tmp97_AST = null;
               tmp97_AST = this.astFactory.create(this.LT(1));
               this.astFactory.makeASTRoot(currentAST, tmp97_AST);
               this.match(4);
               break;
            case 5:
               AST tmp98_AST = null;
               tmp98_AST = this.astFactory.create(this.LT(1));
               this.astFactory.makeASTRoot(currentAST, tmp98_AST);
               this.match(5);
               break;
            case 19:
               AST tmp96_AST = null;
               tmp96_AST = this.astFactory.create(this.LT(1));
               this.astFactory.makeASTRoot(currentAST, tmp96_AST);
               this.match(19);
               break;
            case 47:
               AST tmp95_AST = null;
               tmp95_AST = this.astFactory.create(this.LT(1));
               this.astFactory.makeASTRoot(currentAST, tmp95_AST);
               this.match(47);
               break;
            default:
               throw new NoViableAltException(this.LT(1), this.getFilename());
         }

         switch (this.LA(1)) {
            case 17:
            case 27:
               this.collectionExpr();
               this.astFactory.addASTChild(currentAST, this.returnAST);
               break;
            case 103:
               this.match(103);
               this.subQuery();
               this.astFactory.addASTChild(currentAST, this.returnAST);
               this.match(104);
               break;
            case 126:
               this.identifier();
               this.astFactory.addASTChild(currentAST, this.returnAST);
               break;
            default:
               throw new NoViableAltException(this.LT(1), this.getFilename());
         }

         quantifiedExpression_AST = currentAST.root;
      } catch (RecognitionException ex) {
         this.reportError(ex);
         this.recover(ex, _tokenSet_33);
      }

      this.returnAST = quantifiedExpression_AST;
   }

   public final void atom() throws RecognitionException, TokenStreamException {
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST atom_AST = null;
      Token op = null;
      AST op_AST = null;
      Token lb = null;
      AST lb_AST = null;

      try {
         this.primaryExpression();
         this.astFactory.addASTChild(currentAST, this.returnAST);

         label31:
         while(true) {
            switch (this.LA(1)) {
               case 15:
                  AST tmp101_AST = null;
                  tmp101_AST = this.astFactory.create(this.LT(1));
                  this.astFactory.makeASTRoot(currentAST, tmp101_AST);
                  this.match(15);
                  this.identifier();
                  this.astFactory.addASTChild(currentAST, this.returnAST);
                  switch (this.LA(1)) {
                     case 1:
                     case 6:
                     case 7:
                     case 8:
                     case 10:
                     case 14:
                     case 15:
                     case 18:
                     case 22:
                     case 23:
                     case 24:
                     case 25:
                     case 26:
                     case 28:
                     case 31:
                     case 32:
                     case 33:
                     case 34:
                     case 38:
                     case 40:
                     case 41:
                     case 44:
                     case 50:
                     case 53:
                     case 55:
                     case 56:
                     case 57:
                     case 58:
                     case 64:
                     case 101:
                     case 102:
                     case 104:
                     case 106:
                     case 107:
                     case 108:
                     case 109:
                     case 110:
                     case 111:
                     case 112:
                     case 113:
                     case 114:
                     case 115:
                     case 116:
                     case 117:
                     case 118:
                     case 119:
                     case 120:
                     case 121:
                        continue;
                     case 2:
                     case 3:
                     case 4:
                     case 5:
                     case 9:
                     case 11:
                     case 12:
                     case 13:
                     case 16:
                     case 17:
                     case 19:
                     case 20:
                     case 21:
                     case 27:
                     case 29:
                     case 30:
                     case 35:
                     case 36:
                     case 37:
                     case 39:
                     case 42:
                     case 43:
                     case 45:
                     case 46:
                     case 47:
                     case 48:
                     case 49:
                     case 51:
                     case 52:
                     case 54:
                     case 59:
                     case 60:
                     case 61:
                     case 62:
                     case 63:
                     case 65:
                     case 66:
                     case 67:
                     case 68:
                     case 69:
                     case 70:
                     case 71:
                     case 72:
                     case 73:
                     case 74:
                     case 75:
                     case 76:
                     case 77:
                     case 78:
                     case 79:
                     case 80:
                     case 81:
                     case 82:
                     case 83:
                     case 84:
                     case 85:
                     case 86:
                     case 87:
                     case 88:
                     case 89:
                     case 90:
                     case 91:
                     case 92:
                     case 93:
                     case 94:
                     case 95:
                     case 96:
                     case 97:
                     case 98:
                     case 99:
                     case 100:
                     case 105:
                     default:
                        throw new NoViableAltException(this.LT(1), this.getFilename());
                     case 103:
                        op = this.LT(1);
                        op_AST = this.astFactory.create(op);
                        this.astFactory.makeASTRoot(currentAST, op_AST);
                        this.match(103);
                        op_AST.setType(81);
                        this.exprList();
                        this.astFactory.addASTChild(currentAST, this.returnAST);
                        this.match(104);
                        continue;
                  }
               case 120:
                  lb = this.LT(1);
                  lb_AST = this.astFactory.create(lb);
                  this.astFactory.makeASTRoot(currentAST, lb_AST);
                  this.match(120);
                  lb_AST.setType(78);
                  this.expression();
                  this.astFactory.addASTChild(currentAST, this.returnAST);
                  this.match(121);
                  break;
               default:
                  atom_AST = currentAST.root;
                  break label31;
            }
         }
      } catch (RecognitionException ex) {
         this.reportError(ex);
         this.recover(ex, _tokenSet_33);
      }

      this.returnAST = atom_AST;
   }

   public final void whenClause() throws RecognitionException, TokenStreamException {
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST whenClause_AST = null;

      try {
         AST tmp104_AST = null;
         tmp104_AST = this.astFactory.create(this.LT(1));
         this.astFactory.makeASTRoot(currentAST, tmp104_AST);
         this.match(58);
         this.logicalExpression();
         this.astFactory.addASTChild(currentAST, this.returnAST);
         this.match(57);
         this.unaryExpression();
         this.astFactory.addASTChild(currentAST, this.returnAST);
         whenClause_AST = currentAST.root;
      } catch (RecognitionException ex) {
         this.reportError(ex);
         this.recover(ex, _tokenSet_35);
      }

      this.returnAST = whenClause_AST;
   }

   public final void elseClause() throws RecognitionException, TokenStreamException {
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST elseClause_AST = null;

      try {
         AST tmp106_AST = null;
         tmp106_AST = this.astFactory.create(this.LT(1));
         this.astFactory.makeASTRoot(currentAST, tmp106_AST);
         this.match(56);
         this.unaryExpression();
         this.astFactory.addASTChild(currentAST, this.returnAST);
         elseClause_AST = currentAST.root;
      } catch (RecognitionException ex) {
         this.reportError(ex);
         this.recover(ex, _tokenSet_36);
      }

      this.returnAST = elseClause_AST;
   }

   public final void altWhenClause() throws RecognitionException, TokenStreamException {
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST altWhenClause_AST = null;

      try {
         AST tmp107_AST = null;
         tmp107_AST = this.astFactory.create(this.LT(1));
         this.astFactory.makeASTRoot(currentAST, tmp107_AST);
         this.match(58);
         this.unaryExpression();
         this.astFactory.addASTChild(currentAST, this.returnAST);
         this.match(57);
         this.unaryExpression();
         this.astFactory.addASTChild(currentAST, this.returnAST);
         altWhenClause_AST = currentAST.root;
      } catch (RecognitionException ex) {
         this.reportError(ex);
         this.recover(ex, _tokenSet_35);
      }

      this.returnAST = altWhenClause_AST;
   }

   public final void collectionExpr() throws RecognitionException, TokenStreamException {
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST collectionExpr_AST = null;

      try {
         switch (this.LA(1)) {
            case 17:
               AST tmp109_AST = null;
               tmp109_AST = this.astFactory.create(this.LT(1));
               this.astFactory.makeASTRoot(currentAST, tmp109_AST);
               this.match(17);
               break;
            case 27:
               AST tmp110_AST = null;
               tmp110_AST = this.astFactory.create(this.LT(1));
               this.astFactory.makeASTRoot(currentAST, tmp110_AST);
               this.match(27);
               break;
            default:
               throw new NoViableAltException(this.LT(1), this.getFilename());
         }

         this.match(103);
         this.path();
         this.astFactory.addASTChild(currentAST, this.returnAST);
         this.match(104);
         collectionExpr_AST = currentAST.root;
      } catch (RecognitionException ex) {
         this.reportError(ex);
         this.recover(ex, _tokenSet_11);
      }

      this.returnAST = collectionExpr_AST;
   }

   public final void subQuery() throws RecognitionException, TokenStreamException {
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST subQuery_AST = null;

      try {
         this.union();
         this.astFactory.addASTChild(currentAST, this.returnAST);
         subQuery_AST = currentAST.root;
         subQuery_AST = this.astFactory.make((new ASTArray(2)).add(this.astFactory.create(86, "query")).add(subQuery_AST));
         currentAST.root = subQuery_AST;
         currentAST.child = subQuery_AST != null && subQuery_AST.getFirstChild() != null ? subQuery_AST.getFirstChild() : subQuery_AST;
         currentAST.advanceChildToEnd();
         subQuery_AST = currentAST.root;
      } catch (RecognitionException ex) {
         this.reportError(ex);
         this.recover(ex, _tokenSet_13);
      }

      this.returnAST = subQuery_AST;
   }

   public final void exprList() throws RecognitionException, TokenStreamException {
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST exprList_AST = null;
      Token t = null;
      AST t_AST = null;
      Token l = null;
      AST l_AST = null;
      Token b = null;
      AST b_AST = null;
      AST trimSpec = null;

      try {
         switch (this.LA(1)) {
            case 4:
            case 5:
            case 9:
            case 12:
            case 17:
            case 19:
            case 20:
            case 22:
            case 27:
            case 35:
            case 36:
            case 38:
            case 39:
            case 47:
            case 48:
            case 49:
            case 54:
            case 62:
            case 95:
            case 96:
            case 97:
            case 98:
            case 99:
            case 103:
            case 104:
            case 115:
            case 116:
            case 122:
            case 123:
            case 124:
            case 125:
            case 126:
               break;
            case 6:
            case 7:
            case 8:
            case 10:
            case 11:
            case 13:
            case 14:
            case 15:
            case 16:
            case 18:
            case 21:
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
            case 37:
            case 40:
            case 41:
            case 42:
            case 43:
            case 44:
            case 45:
            case 46:
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
            case 64:
            case 65:
            case 66:
            case 68:
            case 69:
            case 70:
            case 71:
            case 72:
            case 73:
            case 74:
            case 75:
            case 76:
            case 77:
            case 78:
            case 79:
            case 80:
            case 81:
            case 82:
            case 83:
            case 84:
            case 85:
            case 86:
            case 87:
            case 88:
            case 89:
            case 90:
            case 91:
            case 92:
            case 93:
            case 94:
            case 100:
            case 101:
            case 102:
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
            case 117:
            case 118:
            case 119:
            case 120:
            case 121:
            default:
               throw new NoViableAltException(this.LT(1), this.getFilename());
            case 61:
               b = this.LT(1);
               b_AST = this.astFactory.create(b);
               this.astFactory.addASTChild(currentAST, b_AST);
               this.match(61);
               trimSpec = b_AST;
               break;
            case 63:
               l = this.LT(1);
               l_AST = this.astFactory.create(l);
               this.astFactory.addASTChild(currentAST, l_AST);
               this.match(63);
               trimSpec = l_AST;
               break;
            case 67:
               t = this.LT(1);
               t_AST = this.astFactory.create(t);
               this.astFactory.addASTChild(currentAST, t_AST);
               this.match(67);
               trimSpec = t_AST;
         }

         if (trimSpec != null) {
            trimSpec.setType(126);
         }

         label63:
         switch (this.LA(1)) {
            case 4:
            case 5:
            case 9:
            case 12:
            case 17:
            case 19:
            case 20:
            case 27:
            case 35:
            case 36:
            case 38:
            case 39:
            case 47:
            case 48:
            case 49:
            case 54:
            case 62:
            case 95:
            case 96:
            case 97:
            case 98:
            case 99:
            case 103:
            case 115:
            case 116:
            case 122:
            case 123:
            case 124:
            case 125:
            case 126:
               this.expression();
               this.astFactory.addASTChild(currentAST, this.returnAST);
               switch (this.LA(1)) {
                  case 7:
                     this.match(7);
                     this.identifier();
                     this.astFactory.addASTChild(currentAST, this.returnAST);
                     break label63;
                  case 22:
                     AST tmp114_AST = null;
                     tmp114_AST = this.astFactory.create(this.LT(1));
                     this.astFactory.addASTChild(currentAST, tmp114_AST);
                     this.match(22);
                     tmp114_AST.setType(126);
                     this.expression();
                     this.astFactory.addASTChild(currentAST, this.returnAST);
                     break label63;
                  case 101:
                     int _cnt187;
                     for(_cnt187 = 0; this.LA(1) == 101; ++_cnt187) {
                        this.match(101);
                        this.expression();
                        this.astFactory.addASTChild(currentAST, this.returnAST);
                     }

                     if (_cnt187 < 1) {
                        throw new NoViableAltException(this.LT(1), this.getFilename());
                     }
                  case 104:
                     break label63;
                  default:
                     throw new NoViableAltException(this.LT(1), this.getFilename());
               }
            case 6:
            case 7:
            case 8:
            case 10:
            case 11:
            case 13:
            case 14:
            case 15:
            case 16:
            case 18:
            case 21:
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
            case 37:
            case 40:
            case 41:
            case 42:
            case 43:
            case 44:
            case 45:
            case 46:
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
            case 63:
            case 64:
            case 65:
            case 66:
            case 67:
            case 68:
            case 69:
            case 70:
            case 71:
            case 72:
            case 73:
            case 74:
            case 75:
            case 76:
            case 77:
            case 78:
            case 79:
            case 80:
            case 81:
            case 82:
            case 83:
            case 84:
            case 85:
            case 86:
            case 87:
            case 88:
            case 89:
            case 90:
            case 91:
            case 92:
            case 93:
            case 94:
            case 100:
            case 101:
            case 102:
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
            case 117:
            case 118:
            case 119:
            case 120:
            case 121:
            default:
               throw new NoViableAltException(this.LT(1), this.getFilename());
            case 22:
               AST tmp116_AST = null;
               tmp116_AST = this.astFactory.create(this.LT(1));
               this.astFactory.addASTChild(currentAST, tmp116_AST);
               this.match(22);
               tmp116_AST.setType(126);
               this.expression();
               this.astFactory.addASTChild(currentAST, this.returnAST);
            case 104:
         }

         exprList_AST = currentAST.root;
         exprList_AST = this.astFactory.make((new ASTArray(2)).add(this.astFactory.create(75, "exprList")).add(exprList_AST));
         currentAST.root = exprList_AST;
         currentAST.child = exprList_AST != null && exprList_AST.getFirstChild() != null ? exprList_AST.getFirstChild() : exprList_AST;
         currentAST.advanceChildToEnd();
         exprList_AST = currentAST.root;
      } catch (RecognitionException ex) {
         this.reportError(ex);
         this.recover(ex, _tokenSet_13);
      }

      this.returnAST = exprList_AST;
   }

   public final void identPrimary() throws RecognitionException, TokenStreamException {
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST identPrimary_AST = null;
      AST i_AST = null;
      Token o = null;
      AST o_AST = null;
      Token op = null;
      AST op_AST = null;
      AST e_AST = null;

      try {
         label46:
         switch (this.LA(1)) {
            case 9:
            case 12:
            case 17:
            case 27:
            case 35:
            case 36:
            case 48:
               this.aggregate();
               this.astFactory.addASTChild(currentAST, this.returnAST);
               identPrimary_AST = currentAST.root;
               break;
            case 126:
               this.identifier();
               i_AST = this.returnAST;
               this.astFactory.addASTChild(currentAST, this.returnAST);
               this.handleDotIdent();

               while(this.LA(1) == 15 && (this.LA(2) == 17 || this.LA(2) == 65 || this.LA(2) == 126) && _tokenSet_37.member(this.LA(3))) {
                  AST tmp117_AST = null;
                  tmp117_AST = this.astFactory.create(this.LT(1));
                  this.astFactory.makeASTRoot(currentAST, tmp117_AST);
                  this.match(15);
                  switch (this.LA(1)) {
                     case 17:
                        AST tmp118_AST = null;
                        tmp118_AST = this.astFactory.create(this.LT(1));
                        this.astFactory.addASTChild(currentAST, tmp118_AST);
                        this.match(17);
                        break;
                     case 65:
                        o = this.LT(1);
                        o_AST = this.astFactory.create(o);
                        this.astFactory.addASTChild(currentAST, o_AST);
                        this.match(65);
                        o_AST.setType(126);
                        break;
                     case 126:
                        this.identifier();
                        this.astFactory.addASTChild(currentAST, this.returnAST);
                        break;
                     default:
                        throw new NoViableAltException(this.LT(1), this.getFilename());
                  }
               }

               switch (this.LA(1)) {
                  case 2:
                  case 3:
                  case 4:
                  case 5:
                  case 9:
                  case 11:
                  case 12:
                  case 13:
                  case 16:
                  case 17:
                  case 19:
                  case 20:
                  case 21:
                  case 27:
                  case 29:
                  case 30:
                  case 35:
                  case 36:
                  case 37:
                  case 39:
                  case 42:
                  case 43:
                  case 45:
                  case 46:
                  case 47:
                  case 48:
                  case 49:
                  case 51:
                  case 52:
                  case 54:
                  case 59:
                  case 60:
                  case 61:
                  case 62:
                  case 63:
                  case 65:
                  case 66:
                  case 67:
                  case 68:
                  case 69:
                  case 70:
                  case 71:
                  case 72:
                  case 73:
                  case 74:
                  case 75:
                  case 76:
                  case 77:
                  case 78:
                  case 79:
                  case 80:
                  case 81:
                  case 82:
                  case 83:
                  case 84:
                  case 85:
                  case 86:
                  case 87:
                  case 88:
                  case 89:
                  case 90:
                  case 91:
                  case 92:
                  case 93:
                  case 94:
                  case 95:
                  case 96:
                  case 97:
                  case 98:
                  case 99:
                  case 100:
                  case 105:
                  default:
                     throw new NoViableAltException(this.LT(1), this.getFilename());
                  case 103:
                     op = this.LT(1);
                     op_AST = this.astFactory.create(op);
                     this.astFactory.makeASTRoot(currentAST, op_AST);
                     this.match(103);
                     op_AST.setType(81);
                     this.exprList();
                     e_AST = this.returnAST;
                     this.astFactory.addASTChild(currentAST, this.returnAST);
                     this.match(104);
                     identPrimary_AST = currentAST.root;
                     AST path = e_AST.getFirstChild();
                     if (i_AST.getText().equalsIgnoreCase("key")) {
                        identPrimary_AST = this.astFactory.make((new ASTArray(2)).add(this.astFactory.create(68)).add(path));
                     } else if (i_AST.getText().equalsIgnoreCase("value")) {
                        identPrimary_AST = this.astFactory.make((new ASTArray(2)).add(this.astFactory.create(69)).add(path));
                     } else if (i_AST.getText().equalsIgnoreCase("entry")) {
                        identPrimary_AST = this.astFactory.make((new ASTArray(2)).add(this.astFactory.create(70)).add(path));
                     }

                     currentAST.root = identPrimary_AST;
                     currentAST.child = identPrimary_AST != null && identPrimary_AST.getFirstChild() != null ? identPrimary_AST.getFirstChild() : identPrimary_AST;
                     currentAST.advanceChildToEnd();
                  case 1:
                  case 6:
                  case 7:
                  case 8:
                  case 10:
                  case 14:
                  case 15:
                  case 18:
                  case 22:
                  case 23:
                  case 24:
                  case 25:
                  case 26:
                  case 28:
                  case 31:
                  case 32:
                  case 33:
                  case 34:
                  case 38:
                  case 40:
                  case 41:
                  case 44:
                  case 50:
                  case 53:
                  case 55:
                  case 56:
                  case 57:
                  case 58:
                  case 64:
                  case 101:
                  case 102:
                  case 104:
                  case 106:
                  case 107:
                  case 108:
                  case 109:
                  case 110:
                  case 111:
                  case 112:
                  case 113:
                  case 114:
                  case 115:
                  case 116:
                  case 117:
                  case 118:
                  case 119:
                  case 120:
                  case 121:
                     identPrimary_AST = currentAST.root;
                     break label46;
               }
            default:
               throw new NoViableAltException(this.LT(1), this.getFilename());
         }
      } catch (RecognitionException ex) {
         this.reportError(ex);
         this.recover(ex, _tokenSet_11);
      }

      this.returnAST = identPrimary_AST;
   }

   public final void constant() throws RecognitionException, TokenStreamException {
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST constant_AST = null;

      try {
         switch (this.LA(1)) {
            case 20:
               AST tmp129_AST = null;
               tmp129_AST = this.astFactory.create(this.LT(1));
               this.astFactory.addASTChild(currentAST, tmp129_AST);
               this.match(20);
               constant_AST = currentAST.root;
               break;
            case 39:
               AST tmp127_AST = null;
               tmp127_AST = this.astFactory.create(this.LT(1));
               this.astFactory.addASTChild(currentAST, tmp127_AST);
               this.match(39);
               constant_AST = currentAST.root;
               break;
            case 49:
               AST tmp128_AST = null;
               tmp128_AST = this.astFactory.create(this.LT(1));
               this.astFactory.addASTChild(currentAST, tmp128_AST);
               this.match(49);
               constant_AST = currentAST.root;
               break;
            case 62:
               AST tmp130_AST = null;
               tmp130_AST = this.astFactory.create(this.LT(1));
               this.astFactory.addASTChild(currentAST, tmp130_AST);
               this.match(62);
               constant_AST = currentAST.root;
               break;
            case 95:
               AST tmp123_AST = null;
               tmp123_AST = this.astFactory.create(this.LT(1));
               this.astFactory.addASTChild(currentAST, tmp123_AST);
               this.match(95);
               constant_AST = currentAST.root;
               break;
            case 96:
               AST tmp121_AST = null;
               tmp121_AST = this.astFactory.create(this.LT(1));
               this.astFactory.addASTChild(currentAST, tmp121_AST);
               this.match(96);
               constant_AST = currentAST.root;
               break;
            case 97:
               AST tmp122_AST = null;
               tmp122_AST = this.astFactory.create(this.LT(1));
               this.astFactory.addASTChild(currentAST, tmp122_AST);
               this.match(97);
               constant_AST = currentAST.root;
               break;
            case 98:
               AST tmp124_AST = null;
               tmp124_AST = this.astFactory.create(this.LT(1));
               this.astFactory.addASTChild(currentAST, tmp124_AST);
               this.match(98);
               constant_AST = currentAST.root;
               break;
            case 99:
               AST tmp125_AST = null;
               tmp125_AST = this.astFactory.create(this.LT(1));
               this.astFactory.addASTChild(currentAST, tmp125_AST);
               this.match(99);
               constant_AST = currentAST.root;
               break;
            case 124:
               AST tmp120_AST = null;
               tmp120_AST = this.astFactory.create(this.LT(1));
               this.astFactory.addASTChild(currentAST, tmp120_AST);
               this.match(124);
               constant_AST = currentAST.root;
               break;
            case 125:
               AST tmp126_AST = null;
               tmp126_AST = this.astFactory.create(this.LT(1));
               this.astFactory.addASTChild(currentAST, tmp126_AST);
               this.match(125);
               constant_AST = currentAST.root;
               break;
            default:
               throw new NoViableAltException(this.LT(1), this.getFilename());
         }
      } catch (RecognitionException ex) {
         this.reportError(ex);
         this.recover(ex, _tokenSet_11);
      }

      this.returnAST = constant_AST;
   }

   public final void parameter() throws RecognitionException, TokenStreamException {
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST parameter_AST = null;

      try {
         label19:
         switch (this.LA(1)) {
            case 122:
               AST tmp131_AST = null;
               tmp131_AST = this.astFactory.create(this.LT(1));
               this.astFactory.makeASTRoot(currentAST, tmp131_AST);
               this.match(122);
               this.identifier();
               this.astFactory.addASTChild(currentAST, this.returnAST);
               parameter_AST = currentAST.root;
               break;
            case 123:
               AST tmp132_AST = null;
               tmp132_AST = this.astFactory.create(this.LT(1));
               this.astFactory.makeASTRoot(currentAST, tmp132_AST);
               this.match(123);
               switch (this.LA(1)) {
                  case 2:
                  case 3:
                  case 4:
                  case 5:
                  case 9:
                  case 11:
                  case 12:
                  case 13:
                  case 16:
                  case 17:
                  case 19:
                  case 20:
                  case 21:
                  case 27:
                  case 29:
                  case 30:
                  case 35:
                  case 36:
                  case 37:
                  case 39:
                  case 42:
                  case 43:
                  case 45:
                  case 46:
                  case 47:
                  case 48:
                  case 49:
                  case 51:
                  case 52:
                  case 54:
                  case 59:
                  case 60:
                  case 61:
                  case 62:
                  case 63:
                  case 65:
                  case 66:
                  case 67:
                  case 68:
                  case 69:
                  case 70:
                  case 71:
                  case 72:
                  case 73:
                  case 74:
                  case 75:
                  case 76:
                  case 77:
                  case 78:
                  case 79:
                  case 80:
                  case 81:
                  case 82:
                  case 83:
                  case 84:
                  case 85:
                  case 86:
                  case 87:
                  case 88:
                  case 89:
                  case 90:
                  case 91:
                  case 92:
                  case 93:
                  case 94:
                  case 95:
                  case 96:
                  case 97:
                  case 98:
                  case 99:
                  case 100:
                  case 103:
                  case 105:
                  case 122:
                  case 123:
                  default:
                     throw new NoViableAltException(this.LT(1), this.getFilename());
                  case 124:
                     AST tmp133_AST = null;
                     tmp133_AST = this.astFactory.create(this.LT(1));
                     this.astFactory.addASTChild(currentAST, tmp133_AST);
                     this.match(124);
                  case 1:
                  case 6:
                  case 7:
                  case 8:
                  case 10:
                  case 14:
                  case 15:
                  case 18:
                  case 22:
                  case 23:
                  case 24:
                  case 25:
                  case 26:
                  case 28:
                  case 31:
                  case 32:
                  case 33:
                  case 34:
                  case 38:
                  case 40:
                  case 41:
                  case 44:
                  case 50:
                  case 53:
                  case 55:
                  case 56:
                  case 57:
                  case 58:
                  case 64:
                  case 101:
                  case 102:
                  case 104:
                  case 106:
                  case 107:
                  case 108:
                  case 109:
                  case 110:
                  case 111:
                  case 112:
                  case 113:
                  case 114:
                  case 115:
                  case 116:
                  case 117:
                  case 118:
                  case 119:
                  case 120:
                  case 121:
                     parameter_AST = currentAST.root;
                     break label19;
               }
            default:
               throw new NoViableAltException(this.LT(1), this.getFilename());
         }
      } catch (RecognitionException ex) {
         this.reportError(ex);
         this.recover(ex, _tokenSet_11);
      }

      this.returnAST = parameter_AST;
   }

   public final void expressionOrVector() throws RecognitionException, TokenStreamException {
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST expressionOrVector_AST = null;
      AST e_AST = null;
      AST v_AST = null;

      try {
         this.expression();
         e_AST = this.returnAST;
         switch (this.LA(1)) {
            case 101:
               this.vectorExpr();
               v_AST = this.returnAST;
            case 104:
               expressionOrVector_AST = currentAST.root;
               if (v_AST != null) {
                  expressionOrVector_AST = this.astFactory.make((new ASTArray(3)).add(this.astFactory.create(92, "{vector}")).add(e_AST).add(v_AST));
               } else {
                  expressionOrVector_AST = e_AST;
               }

               currentAST.root = expressionOrVector_AST;
               currentAST.child = expressionOrVector_AST != null && expressionOrVector_AST.getFirstChild() != null ? expressionOrVector_AST.getFirstChild() : expressionOrVector_AST;
               currentAST.advanceChildToEnd();
               break;
            default:
               throw new NoViableAltException(this.LT(1), this.getFilename());
         }
      } catch (RecognitionException ex) {
         this.reportError(ex);
         this.recover(ex, _tokenSet_13);
      }

      this.returnAST = expressionOrVector_AST;
   }

   public final void vectorExpr() throws RecognitionException, TokenStreamException {
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST vectorExpr_AST = null;

      try {
         this.match(101);
         this.expression();
         this.astFactory.addASTChild(currentAST, this.returnAST);

         while(this.LA(1) == 101) {
            this.match(101);
            this.expression();
            this.astFactory.addASTChild(currentAST, this.returnAST);
         }

         vectorExpr_AST = currentAST.root;
      } catch (RecognitionException ex) {
         this.reportError(ex);
         this.recover(ex, _tokenSet_13);
      }

      this.returnAST = vectorExpr_AST;
   }

   public final void aggregate() throws RecognitionException, TokenStreamException {
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST aggregate_AST = null;

      try {
         switch (this.LA(1)) {
            case 9:
            case 35:
            case 36:
            case 48:
               switch (this.LA(1)) {
                  case 9:
                     AST tmp137_AST = null;
                     tmp137_AST = this.astFactory.create(this.LT(1));
                     this.astFactory.makeASTRoot(currentAST, tmp137_AST);
                     this.match(9);
                     break;
                  case 35:
                     AST tmp138_AST = null;
                     tmp138_AST = this.astFactory.create(this.LT(1));
                     this.astFactory.makeASTRoot(currentAST, tmp138_AST);
                     this.match(35);
                     break;
                  case 36:
                     AST tmp139_AST = null;
                     tmp139_AST = this.astFactory.create(this.LT(1));
                     this.astFactory.makeASTRoot(currentAST, tmp139_AST);
                     this.match(36);
                     break;
                  case 48:
                     AST tmp136_AST = null;
                     tmp136_AST = this.astFactory.create(this.LT(1));
                     this.astFactory.makeASTRoot(currentAST, tmp136_AST);
                     this.match(48);
                     break;
                  default:
                     throw new NoViableAltException(this.LT(1), this.getFilename());
               }

               this.match(103);
               this.additiveExpression();
               this.astFactory.addASTChild(currentAST, this.returnAST);
               this.match(104);
               aggregate_AST = currentAST.root;
               aggregate_AST.setType(71);
               aggregate_AST = currentAST.root;
               break;
            case 12:
               AST tmp142_AST = null;
               tmp142_AST = this.astFactory.create(this.LT(1));
               this.astFactory.makeASTRoot(currentAST, tmp142_AST);
               this.match(12);
               this.match(103);
               label39:
               switch (this.LA(1)) {
                  case 4:
                  case 16:
                  case 17:
                  case 27:
                  case 54:
                  case 126:
                     switch (this.LA(1)) {
                        case 4:
                           AST tmp146_AST = null;
                           tmp146_AST = this.astFactory.create(this.LT(1));
                           this.astFactory.addASTChild(currentAST, tmp146_AST);
                           this.match(4);
                           break;
                        case 16:
                           AST tmp145_AST = null;
                           tmp145_AST = this.astFactory.create(this.LT(1));
                           this.astFactory.addASTChild(currentAST, tmp145_AST);
                           this.match(16);
                        case 17:
                        case 27:
                        case 54:
                        case 126:
                           break;
                        default:
                           throw new NoViableAltException(this.LT(1), this.getFilename());
                     }

                     switch (this.LA(1)) {
                        case 17:
                        case 27:
                           this.collectionExpr();
                           this.astFactory.addASTChild(currentAST, this.returnAST);
                           break label39;
                        case 54:
                           this.caseExpression();
                           this.astFactory.addASTChild(currentAST, this.returnAST);
                           break label39;
                        case 126:
                           this.path();
                           this.astFactory.addASTChild(currentAST, this.returnAST);
                           break label39;
                        default:
                           throw new NoViableAltException(this.LT(1), this.getFilename());
                     }
                  case 117:
                     AST tmp144_AST = null;
                     tmp144_AST = this.astFactory.create(this.LT(1));
                     this.astFactory.addASTChild(currentAST, tmp144_AST);
                     this.match(117);
                     tmp144_AST.setType(88);
                     break;
                  default:
                     throw new NoViableAltException(this.LT(1), this.getFilename());
               }

               this.match(104);
               aggregate_AST = currentAST.root;
               break;
            case 17:
            case 27:
               this.collectionExpr();
               this.astFactory.addASTChild(currentAST, this.returnAST);
               aggregate_AST = currentAST.root;
               break;
            default:
               throw new NoViableAltException(this.LT(1), this.getFilename());
         }
      } catch (RecognitionException ex) {
         this.reportError(ex);
         this.recover(ex, _tokenSet_11);
      }

      this.returnAST = aggregate_AST;
   }

   protected void buildTokenTypeASTClassMap() {
      this.tokenTypeToASTClassMap = null;
   }

   private static final long[] mk_tokenSet_0() {
      long[] data = new long[]{2L, 0L, 0L};
      return data;
   }

   private static final long[] mk_tokenSet_1() {
      long[] data = new long[]{9077567998918658L, 0L, 0L};
      return data;
   }

   private static final long[] mk_tokenSet_2() {
      long[] data = new long[]{9007199254740994L, 0L, 0L};
      return data;
   }

   private static final long[] mk_tokenSet_3() {
      long[] data = new long[]{1128098946875394L, 1099511627776L, 0L, 0L};
      return data;
   }

   private static final long[] mk_tokenSet_4() {
      long[] data = new long[]{9007199254740994L, 137438953472L, 0L, 0L};
      return data;
   }

   private static final long[] mk_tokenSet_5() {
      long[] data = new long[]{0L, 274877906944L, 0L, 0L};
      return data;
   }

   private static final long[] mk_tokenSet_6() {
      long[] data = new long[]{1307261066675241410L, 4755869238785212416L, 0L, 0L};
      return data;
   }

   private static final long[] mk_tokenSet_7() {
      long[] data = new long[]{154269485447267778L, 145238201764675585L, 0L, 0L};
      return data;
   }

   private static final long[] mk_tokenSet_8() {
      long[] data = new long[]{1163144776902508546L, 1236950581248L, 0L, 0L};
      return data;
   }

   private static final long[] mk_tokenSet_9() {
      long[] data = new long[]{1125899906842626L, 1099511627776L, 0L, 0L};
      return data;
   }

   private static final long[] mk_tokenSet_10() {
      long[] data = new long[]{9044582671056898L, 0L, 0L};
      return data;
   }

   private static final long[] mk_tokenSet_11() {
      long[] data = new long[]{550586252655904194L, 288227489933688833L, 0L, 0L};
      return data;
   }

   private static final long[] mk_tokenSet_12() {
      long[] data = new long[]{5181312067402913778L, 9223371965987815429L, 0L, 0L};
      return data;
   }

   private static final long[] mk_tokenSet_13() {
      long[] data = new long[]{0L, 1099511627776L, 0L, 0L};
      return data;
   }

   private static final long[] mk_tokenSet_14() {
      long[] data = new long[]{10135298201616386L, 1099511627776L, 0L, 0L};
      return data;
   }

   private static final long[] mk_tokenSet_15() {
      long[] data = new long[]{1128098930098178L, 1099511627776L, 0L, 0L};
      return data;
   }

   private static final long[] mk_tokenSet_16() {
      long[] data = new long[]{10135298205810690L, 1099511627776L, 0L, 0L};
      return data;
   }

   private static final long[] mk_tokenSet_17() {
      long[] data = new long[]{10152903551516802L, 4611687255377969152L, 0L, 0L};
      return data;
   }

   private static final long[] mk_tokenSet_18() {
      long[] data = new long[]{10152903549386754L, 1236950581248L, 0L, 0L};
      return data;
   }

   private static final long[] mk_tokenSet_19() {
      long[] data = new long[]{1163074408156233730L, 1236950581248L, 0L, 0L};
      return data;
   }

   private static final long[] mk_tokenSet_20() {
      long[] data = new long[]{154268091625242626L, 1236950581248L, 0L, 0L};
      return data;
   }

   private static final long[] mk_tokenSet_21() {
      long[] data = new long[]{1163144776969617410L, 1236950581248L, 0L, 0L};
      return data;
   }

   private static final long[] mk_tokenSet_22() {
      long[] data = new long[]{154268091663008130L, 144129619165970432L, 0L, 0L};
      return data;
   }

   private static final long[] mk_tokenSet_23() {
      long[] data = new long[]{1125899906842626L, 1236950581248L, 0L, 0L};
      return data;
   }

   private static final long[] mk_tokenSet_24() {
      long[] data = new long[]{10135298205810690L, 1236950581248L, 0L, 0L};
      return data;
   }

   private static final long[] mk_tokenSet_25() {
      long[] data = new long[]{154269191174635906L, 144129619165970432L, 0L, 0L};
      return data;
   }

   private static final long[] mk_tokenSet_26() {
      long[] data = new long[]{154269191174635970L, 144129619165970432L, 0L, 0L};
      return data;
   }

   private static final long[] mk_tokenSet_27() {
      long[] data = new long[]{2147483648L, 53051436040192L, 0L, 0L};
      return data;
   }

   private static final long[] mk_tokenSet_28() {
      long[] data = new long[]{154269193322119618L, 144182670602010624L, 0L, 0L};
      return data;
   }

   private static final long[] mk_tokenSet_29() {
      long[] data = new long[]{154269485447267778L, 146364101671518209L, 0L, 0L};
      return data;
   }

   private static final long[] mk_tokenSet_30() {
      long[] data = new long[]{4640856989782118960L, 8941898775983554560L, 0L, 0L};
      return data;
   }

   private static final long[] mk_tokenSet_31() {
      long[] data = new long[]{5073221415736170482L, 9223371965987815427L, 0L, 0L};
      return data;
   }

   private static final long[] mk_tokenSet_32() {
      long[] data = new long[]{154269485447267778L, 153119501112573953L, 0L, 0L};
      return data;
   }

   private static final long[] mk_tokenSet_33() {
      long[] data = new long[]{550586252655871426L, 216169895895760897L, 0L, 0L};
      return data;
   }

   private static final long[] mk_tokenSet_34() {
      long[] data = new long[]{4630686232326312496L, 8941897676471926784L, 0L, 0L};
      return data;
   }

   private static final long[] mk_tokenSet_35() {
      long[] data = new long[]{396316767208603648L, 0L, 0L};
      return data;
   }

   private static final long[] mk_tokenSet_36() {
      long[] data = new long[]{36028797018963968L, 0L, 0L};
      return data;
   }

   private static final long[] mk_tokenSet_37() {
      long[] data = new long[]{550586252655904194L, 288228039689502721L, 0L, 0L};
      return data;
   }
}
