package org.hibernate.hql.internal.antlr;

import antlr.MismatchedTokenException;
import antlr.NoViableAltException;
import antlr.RecognitionException;
import antlr.TreeParser;
import antlr.collections.AST;
import antlr.collections.impl.BitSet;

public class SqlGeneratorBase extends TreeParser implements SqlTokenTypes {
   private StringBuilder buf = new StringBuilder();
   public static final String[] _tokenNames = new String[]{"<0>", "EOF", "<2>", "NULL_TREE_LOOKAHEAD", "\"all\"", "\"any\"", "\"and\"", "\"as\"", "\"asc\"", "\"avg\"", "\"between\"", "\"class\"", "\"count\"", "\"delete\"", "\"desc\"", "DOT", "\"distinct\"", "\"elements\"", "\"escape\"", "\"exists\"", "\"false\"", "\"fetch\"", "\"from\"", "\"full\"", "\"group\"", "\"having\"", "\"in\"", "\"indices\"", "\"inner\"", "\"insert\"", "\"into\"", "\"is\"", "\"join\"", "\"left\"", "\"like\"", "\"max\"", "\"min\"", "\"new\"", "\"not\"", "\"null\"", "\"or\"", "\"order\"", "\"outer\"", "\"properties\"", "\"right\"", "\"select\"", "\"set\"", "\"some\"", "\"sum\"", "\"true\"", "\"union\"", "\"update\"", "\"versioned\"", "\"where\"", "\"case\"", "\"end\"", "\"else\"", "\"then\"", "\"when\"", "\"on\"", "\"with\"", "\"both\"", "\"empty\"", "\"leading\"", "\"member\"", "\"object\"", "\"of\"", "\"trailing\"", "KEY", "VALUE", "ENTRY", "AGGREGATE", "ALIAS", "CONSTRUCTOR", "CASE2", "EXPR_LIST", "FILTER_ENTITY", "IN_LIST", "INDEX_OP", "IS_NOT_NULL", "IS_NULL", "METHOD_CALL", "NOT_BETWEEN", "NOT_IN", "NOT_LIKE", "ORDER_ELEMENT", "QUERY", "RANGE", "ROW_STAR", "SELECT_FROM", "UNARY_MINUS", "UNARY_PLUS", "VECTOR_EXPR", "WEIRD_IDENT", "CONSTANT", "NUM_DOUBLE", "NUM_FLOAT", "NUM_LONG", "NUM_BIG_INTEGER", "NUM_BIG_DECIMAL", "JAVA_CONSTANT", "COMMA", "EQ", "OPEN", "CLOSE", "\"by\"", "\"ascending\"", "\"descending\"", "NE", "SQL_NE", "LT", "GT", "LE", "GE", "CONCAT", "PLUS", "MINUS", "STAR", "DIV", "MOD", "OPEN_BRACKET", "CLOSE_BRACKET", "COLON", "PARAM", "NUM_INT", "QUOTED_STRING", "IDENT", "ID_START_LETTER", "ID_LETTER", "ESCqs", "WS", "HEX_DIGIT", "EXPONENT", "FLOAT_SUFFIX", "FROM_FRAGMENT", "IMPLIED_FROM", "JOIN_FRAGMENT", "SELECT_CLAUSE", "LEFT_OUTER", "RIGHT_OUTER", "ALIAS_REF", "PROPERTY_REF", "SQL_TOKEN", "SELECT_COLUMNS", "SELECT_EXPR", "THETA_JOINS", "FILTERS", "METHOD_NAME", "NAMED_PARAM", "BOGUS", "RESULT_VARIABLE_REF", "SQL_NODE"};
   public static final BitSet _tokenSet_0 = new BitSet(mk_tokenSet_0());
   public static final BitSet _tokenSet_1 = new BitSet(mk_tokenSet_1());
   public static final BitSet _tokenSet_2 = new BitSet(mk_tokenSet_2());
   public static final BitSet _tokenSet_3 = new BitSet(mk_tokenSet_3());
   public static final BitSet _tokenSet_4 = new BitSet(mk_tokenSet_4());
   public static final BitSet _tokenSet_5 = new BitSet(mk_tokenSet_5());

   protected void out(String s) {
      this.buf.append(s);
   }

   protected int getLastChar() {
      int len = this.buf.length();
      return len == 0 ? -1 : this.buf.charAt(len - 1);
   }

   protected void optionalSpace() {
   }

   protected void out(AST n) {
      this.out(n.getText());
   }

   protected void separator(AST n, String sep) {
      if (n.getNextSibling() != null) {
         this.out(sep);
      }

   }

   protected boolean hasText(AST a) {
      String t = a.getText();
      return t != null && t.length() > 0;
   }

   protected void fromFragmentSeparator(AST a) {
   }

   protected void nestedFromFragment(AST d, AST parent) {
   }

   protected StringBuilder getStringBuilder() {
      return this.buf;
   }

   protected void nyi(AST n) {
      throw new UnsupportedOperationException("Unsupported node: " + n);
   }

   protected void beginFunctionTemplate(AST m, AST i) {
      this.out(i);
      this.out("(");
   }

   protected void endFunctionTemplate(AST m) {
      this.out(")");
   }

   protected void commaBetweenParameters(String comma) {
      this.out(comma);
   }

   public SqlGeneratorBase() {
      this.tokenNames = _tokenNames;
   }

   public final void statement(AST _t) throws RecognitionException {
      AST statement_AST_in = _t == ASTNULL ? null : _t;

      try {
         if (_t == null) {
            _t = ASTNULL;
         }

         switch (_t.getType()) {
            case 13:
               this.deleteStatement(_t);
               _t = this._retTree;
               break;
            case 29:
               this.insertStatement(_t);
               _t = this._retTree;
               break;
            case 45:
               this.selectStatement(_t);
               _t = this._retTree;
               break;
            case 51:
               this.updateStatement(_t);
               _t = this._retTree;
               break;
            default:
               throw new NoViableAltException(_t);
         }
      } catch (RecognitionException ex) {
         if (this.inputState.guessing != 0) {
            throw ex;
         }

         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this._retTree = _t;
   }

   public final void selectStatement(AST _t) throws RecognitionException {
      AST selectStatement_AST_in = _t == ASTNULL ? null : _t;

      try {
         AST __t3 = _t;
         this.match(_t, 45);
         _t = _t.getFirstChild();
         if (this.inputState.guessing == 0) {
            this.out("select ");
         }

         this.selectClause(_t);
         _t = this._retTree;
         this.from(_t);
         _t = this._retTree;
         if (_t == null) {
            _t = ASTNULL;
         }

         label69:
         switch (_t.getType()) {
            case 53:
               AST __t5 = _t;
               this.match(_t, 53);
               _t = _t.getFirstChild();
               if (this.inputState.guessing == 0) {
                  this.out(" where ");
               }

               this.whereExpr(_t);
               _t = this._retTree;
               _t = __t5.getNextSibling();
            case 3:
            case 24:
            case 41:
               if (_t == null) {
                  _t = ASTNULL;
               }

               switch (_t.getType()) {
                  case 24:
                     AST __t7 = _t;
                     this.match(_t, 24);
                     _t = _t.getFirstChild();
                     if (this.inputState.guessing == 0) {
                        this.out(" group by ");
                     }

                     this.groupExprs(_t);
                     _t = this._retTree;
                     if (_t == null) {
                        _t = ASTNULL;
                     }

                     switch (_t.getType()) {
                        case 25:
                           AST __t9 = _t;
                           this.match(_t, 25);
                           _t = _t.getFirstChild();
                           if (this.inputState.guessing == 0) {
                              this.out(" having ");
                           }

                           this.booleanExpr(_t, false);
                           _t = this._retTree;
                           _t = __t9.getNextSibling();
                        case 3:
                           _t = __t7.getNextSibling();
                           break;
                        default:
                           throw new NoViableAltException(_t);
                     }
                  case 3:
                  case 41:
                     if (_t == null) {
                        _t = ASTNULL;
                     }

                     switch (_t.getType()) {
                        case 41:
                           AST __t11 = _t;
                           this.match(_t, 41);
                           _t = _t.getFirstChild();
                           if (this.inputState.guessing == 0) {
                              this.out(" order by ");
                           }

                           this.orderExprs(_t);
                           _t = this._retTree;
                           _t = __t11.getNextSibling();
                        case 3:
                           _t = __t3.getNextSibling();
                           break label69;
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
         if (this.inputState.guessing != 0) {
            throw ex;
         }

         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this._retTree = _t;
   }

   public final void updateStatement(AST _t) throws RecognitionException {
      AST updateStatement_AST_in = _t == ASTNULL ? null : _t;

      try {
         AST __t13 = _t;
         this.match(_t, 51);
         _t = _t.getFirstChild();
         if (this.inputState.guessing == 0) {
            this.out("update ");
         }

         AST __t14 = _t;
         this.match(_t, 22);
         _t = _t.getFirstChild();
         this.fromTable(_t);
         _t = this._retTree;
         _t = __t14.getNextSibling();
         this.setClause(_t);
         AST var12 = this._retTree;
         if (var12 == null) {
            var12 = ASTNULL;
         }

         switch (((AST)var12).getType()) {
            case 53:
               this.whereClause((AST)var12);
               AST var13 = this._retTree;
            case 3:
               _t = __t13.getNextSibling();
               break;
            default:
               throw new NoViableAltException((AST)var12);
         }
      } catch (RecognitionException ex) {
         if (this.inputState.guessing != 0) {
            throw ex;
         }

         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this._retTree = _t;
   }

   public final void deleteStatement(AST _t) throws RecognitionException {
      AST deleteStatement_AST_in = _t == ASTNULL ? null : _t;

      try {
         AST __t17 = _t;
         this.match(_t, 13);
         _t = _t.getFirstChild();
         if (this.inputState.guessing == 0) {
            this.out("delete");
         }

         this.from(_t);
         AST var7 = this._retTree;
         if (var7 == null) {
            var7 = ASTNULL;
         }

         switch (((AST)var7).getType()) {
            case 53:
               this.whereClause((AST)var7);
               AST var8 = this._retTree;
            case 3:
               _t = __t17.getNextSibling();
               break;
            default:
               throw new NoViableAltException((AST)var7);
         }
      } catch (RecognitionException ex) {
         if (this.inputState.guessing != 0) {
            throw ex;
         }

         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this._retTree = _t;
   }

   public final void insertStatement(AST _t) throws RecognitionException {
      AST insertStatement_AST_in = _t == ASTNULL ? null : _t;
      AST i = null;

      try {
         AST __t20 = _t;
         this.match(_t, 29);
         _t = _t.getFirstChild();
         if (this.inputState.guessing == 0) {
            this.out("insert ");
         }

         i = _t;
         this.match(_t, 30);
         _t = _t.getNextSibling();
         if (this.inputState.guessing == 0) {
            this.out(i);
            this.out(" ");
         }

         this.selectStatement(_t);
         _t = this._retTree;
         _t = __t20.getNextSibling();
      } catch (RecognitionException ex) {
         if (this.inputState.guessing != 0) {
            throw ex;
         }

         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this._retTree = _t;
   }

   public final void selectClause(AST _t) throws RecognitionException {
      AST selectClause_AST_in = _t == ASTNULL ? null : _t;

      try {
         this.match(_t, 137);
         AST var7 = _t.getFirstChild();
         if (var7 == null) {
            var7 = ASTNULL;
         }

         label36:
         switch (((AST)var7).getType()) {
            case 4:
            case 16:
               this.distinctOrAll((AST)var7);
               var7 = this._retTree;
            case 12:
            case 15:
            case 20:
            case 45:
            case 49:
            case 54:
            case 68:
            case 69:
            case 70:
            case 71:
            case 73:
            case 74:
            case 81:
            case 90:
            case 94:
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
            case 123:
            case 124:
            case 125:
            case 126:
            case 140:
            case 142:
            case 144:
            case 151:
               int _cnt51 = 0;

               while(true) {
                  if (var7 == null) {
                     var7 = ASTNULL;
                  }

                  if (!_tokenSet_0.member(((AST)var7).getType())) {
                     if (_cnt51 < 1) {
                        throw new NoViableAltException((AST)var7);
                     }

                     _t = _t.getNextSibling();
                     break label36;
                  }

                  this.selectColumn((AST)var7);
                  var7 = this._retTree;
                  ++_cnt51;
               }
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 13:
            case 14:
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
            case 39:
            case 40:
            case 41:
            case 42:
            case 43:
            case 44:
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
            case 86:
            case 87:
            case 88:
            case 89:
            case 91:
            case 92:
            case 93:
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
            case 127:
            case 128:
            case 129:
            case 130:
            case 131:
            case 132:
            case 133:
            case 134:
            case 135:
            case 136:
            case 137:
            case 138:
            case 139:
            case 141:
            case 143:
            case 145:
            case 146:
            case 147:
            case 148:
            case 149:
            case 150:
            default:
               throw new NoViableAltException((AST)var7);
         }
      } catch (RecognitionException ex) {
         if (this.inputState.guessing != 0) {
            throw ex;
         }

         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this._retTree = _t;
   }

   public final void from(AST _t) throws RecognitionException {
      AST from_AST_in = _t == ASTNULL ? null : _t;
      AST f = null;

      try {
         AST __t67 = _t;
         f = _t == ASTNULL ? null : _t;
         this.match(_t, 22);
         AST var6 = _t.getFirstChild();
         if (this.inputState.guessing == 0) {
            this.out(" from ");
         }

         while(true) {
            if (var6 == null) {
               var6 = ASTNULL;
            }

            if (((AST)var6).getType() != 134 && ((AST)var6).getType() != 136) {
               _t = __t67.getNextSibling();
               break;
            }

            this.fromTable((AST)var6);
            var6 = this._retTree;
         }
      } catch (RecognitionException ex) {
         if (this.inputState.guessing != 0) {
            throw ex;
         }

         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this._retTree = _t;
   }

   public final void whereExpr(AST _t) throws RecognitionException {
      AST whereExpr_AST_in = _t == ASTNULL ? null : _t;

      try {
         if (_t == null) {
            _t = ASTNULL;
         }

         label65:
         switch (_t.getType()) {
            case 6:
            case 10:
            case 19:
            case 26:
            case 34:
            case 38:
            case 40:
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
            case 142:
               this.booleanExpr(_t, false);
               _t = this._retTree;
               break;
            case 145:
               this.thetaJoins(_t);
               _t = this._retTree;
               if (_t == null) {
                  _t = ASTNULL;
               }

               switch (_t.getType()) {
                  case 3:
                     break label65;
                  case 6:
                  case 10:
                  case 19:
                  case 26:
                  case 34:
                  case 38:
                  case 40:
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
                  case 142:
                     if (this.inputState.guessing == 0) {
                        this.out(" and ");
                     }

                     this.booleanExpr(_t, true);
                     _t = this._retTree;
                     break label65;
                  default:
                     throw new NoViableAltException(_t);
               }
            case 146:
               this.filters(_t);
               _t = this._retTree;
               if (_t == null) {
                  _t = ASTNULL;
               }

               switch (_t.getType()) {
                  case 145:
                     if (this.inputState.guessing == 0) {
                        this.out(" and ");
                     }

                     this.thetaJoins(_t);
                     _t = this._retTree;
                  case 3:
                  case 6:
                  case 10:
                  case 19:
                  case 26:
                  case 34:
                  case 38:
                  case 40:
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
                  case 142:
                     if (_t == null) {
                        _t = ASTNULL;
                     }

                     switch (_t.getType()) {
                        case 3:
                           break label65;
                        case 6:
                        case 10:
                        case 19:
                        case 26:
                        case 34:
                        case 38:
                        case 40:
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
                        case 142:
                           if (this.inputState.guessing == 0) {
                              this.out(" and ");
                           }

                           this.booleanExpr(_t, true);
                           _t = this._retTree;
                           break label65;
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
         if (this.inputState.guessing != 0) {
            throw ex;
         }

         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this._retTree = _t;
   }

   public final void groupExprs(AST _t) throws RecognitionException {
      AST groupExprs_AST_in = _t == ASTNULL ? null : _t;

      try {
         this.expr(_t);
         _t = this._retTree;
         if (_t == null) {
            _t = ASTNULL;
         }

         switch (_t.getType()) {
            case 3:
            case 25:
               break;
            case 4:
            case 5:
            case 12:
            case 15:
            case 20:
            case 39:
            case 45:
            case 47:
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
            case 94:
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
            case 123:
            case 124:
            case 125:
            case 126:
            case 140:
            case 142:
            case 148:
            case 150:
               if (this.inputState.guessing == 0) {
                  this.out(" , ");
               }

               this.groupExprs(_t);
               _t = this._retTree;
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
            case 86:
            case 87:
            case 88:
            case 89:
            case 91:
            case 93:
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
            case 127:
            case 128:
            case 129:
            case 130:
            case 131:
            case 132:
            case 133:
            case 134:
            case 135:
            case 136:
            case 137:
            case 138:
            case 139:
            case 141:
            case 143:
            case 144:
            case 145:
            case 146:
            case 147:
            case 149:
            default:
               throw new NoViableAltException(_t);
         }
      } catch (RecognitionException ex) {
         if (this.inputState.guessing != 0) {
            throw ex;
         }

         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this._retTree = _t;
   }

   public final void booleanExpr(AST _t, boolean parens) throws RecognitionException {
      AST booleanExpr_AST_in = _t == ASTNULL ? null : _t;
      AST st = null;

      try {
         if (_t == null) {
            _t = ASTNULL;
         }

         switch (_t.getType()) {
            case 6:
            case 38:
            case 40:
               this.booleanOp(_t, parens);
               _t = this._retTree;
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
               this.comparisonExpr(_t, parens);
               _t = this._retTree;
               break;
            case 142:
               AST var7 = _t;
               this.match(_t, 142);
               _t = _t.getNextSibling();
               if (this.inputState.guessing == 0) {
                  this.out((AST)var7);
               }
               break;
            default:
               throw new NoViableAltException(_t);
         }
      } catch (RecognitionException ex) {
         if (this.inputState.guessing != 0) {
            throw ex;
         }

         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this._retTree = _t;
   }

   public final void orderExprs(AST _t) throws RecognitionException {
      AST orderExprs_AST_in = _t == ASTNULL ? null : _t;
      AST dir = null;

      try {
         this.expr(_t);
         _t = this._retTree;
         if (_t == null) {
            _t = ASTNULL;
         }

         switch (_t.getType()) {
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
            case 86:
            case 87:
            case 88:
            case 89:
            case 91:
            case 93:
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
            case 127:
            case 128:
            case 129:
            case 130:
            case 131:
            case 132:
            case 133:
            case 134:
            case 135:
            case 136:
            case 137:
            case 138:
            case 139:
            case 141:
            case 143:
            case 144:
            case 145:
            case 146:
            case 147:
            case 149:
            default:
               throw new NoViableAltException(_t);
            case 8:
            case 14:
               AST var6 = _t == ASTNULL ? null : _t;
               this.orderDirection(_t);
               _t = this._retTree;
               if (this.inputState.guessing == 0) {
                  this.out(" ");
                  this.out((AST)var6);
               }
            case 3:
            case 4:
            case 5:
            case 12:
            case 15:
            case 20:
            case 39:
            case 45:
            case 47:
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
            case 94:
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
            case 123:
            case 124:
            case 125:
            case 126:
            case 140:
            case 142:
            case 148:
            case 150:
               if (_t == null) {
                  _t = ASTNULL;
               }

               switch (_t.getType()) {
                  case 3:
                     break;
                  case 4:
                  case 5:
                  case 12:
                  case 15:
                  case 20:
                  case 39:
                  case 45:
                  case 47:
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
                  case 94:
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
                  case 123:
                  case 124:
                  case 125:
                  case 126:
                  case 140:
                  case 142:
                  case 148:
                  case 150:
                     if (this.inputState.guessing == 0) {
                        this.out(", ");
                     }

                     this.orderExprs(_t);
                     _t = this._retTree;
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
                  case 86:
                  case 87:
                  case 88:
                  case 89:
                  case 91:
                  case 93:
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
                  case 127:
                  case 128:
                  case 129:
                  case 130:
                  case 131:
                  case 132:
                  case 133:
                  case 134:
                  case 135:
                  case 136:
                  case 137:
                  case 138:
                  case 139:
                  case 141:
                  case 143:
                  case 144:
                  case 145:
                  case 146:
                  case 147:
                  case 149:
                  default:
                     throw new NoViableAltException(_t);
               }
         }
      } catch (RecognitionException ex) {
         if (this.inputState.guessing != 0) {
            throw ex;
         }

         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this._retTree = _t;
   }

   public final void fromTable(AST _t) throws RecognitionException {
      AST fromTable_AST_in = _t == ASTNULL ? null : _t;
      AST a = null;
      AST b = null;

      try {
         if (_t == null) {
            _t = ASTNULL;
         }

         label79:
         switch (_t.getType()) {
            case 134:
               AST __t71 = _t;
               AST var9 = _t == ASTNULL ? null : _t;
               this.match(_t, 134);
               _t = _t.getFirstChild();
               if (this.inputState.guessing == 0) {
                  this.out((AST)var9);
               }

               while(true) {
                  if (_t == null) {
                     _t = ASTNULL;
                  }

                  if (_t.getType() != 134 && _t.getType() != 136) {
                     if (this.inputState.guessing == 0) {
                        this.fromFragmentSeparator((AST)var9);
                     }

                     _t = __t71.getNextSibling();
                     break label79;
                  }

                  this.tableJoin(_t, (AST)var9);
                  _t = this._retTree;
               }
            case 136:
               AST __t74 = _t;
               AST var10 = _t == ASTNULL ? null : _t;
               this.match(_t, 136);
               _t = _t.getFirstChild();
               if (this.inputState.guessing == 0) {
                  this.out((AST)var10);
               }

               while(true) {
                  if (_t == null) {
                     _t = ASTNULL;
                  }

                  if (_t.getType() != 134 && _t.getType() != 136) {
                     if (this.inputState.guessing == 0) {
                        this.fromFragmentSeparator((AST)var10);
                     }

                     _t = __t74.getNextSibling();
                     break label79;
                  }

                  this.tableJoin(_t, (AST)var10);
                  _t = this._retTree;
               }
            default:
               throw new NoViableAltException(_t);
         }
      } catch (RecognitionException ex) {
         if (this.inputState.guessing != 0) {
            throw ex;
         }

         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this._retTree = _t;
   }

   public final void setClause(AST _t) throws RecognitionException {
      AST setClause_AST_in = _t == ASTNULL ? null : _t;

      try {
         AST __t22 = _t;
         this.match(_t, 46);
         _t = _t.getFirstChild();
         if (this.inputState.guessing == 0) {
            this.out(" set ");
         }

         this.comparisonExpr(_t, false);
         AST var7 = this._retTree;

         while(true) {
            if (var7 == null) {
               var7 = ASTNULL;
            }

            if (!_tokenSet_1.member(((AST)var7).getType())) {
               _t = __t22.getNextSibling();
               break;
            }

            if (this.inputState.guessing == 0) {
               this.out(", ");
            }

            this.comparisonExpr((AST)var7, false);
            var7 = this._retTree;
         }
      } catch (RecognitionException ex) {
         if (this.inputState.guessing != 0) {
            throw ex;
         }

         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this._retTree = _t;
   }

   public final void whereClause(AST _t) throws RecognitionException {
      AST whereClause_AST_in = _t == ASTNULL ? null : _t;

      try {
         AST __t26 = _t;
         this.match(_t, 53);
         _t = _t.getFirstChild();
         if (this.inputState.guessing == 0) {
            this.out(" where ");
         }

         this.whereClauseExpr(_t);
         _t = this._retTree;
         _t = __t26.getNextSibling();
      } catch (RecognitionException ex) {
         if (this.inputState.guessing != 0) {
            throw ex;
         }

         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this._retTree = _t;
   }

   public final void comparisonExpr(AST _t, boolean parens) throws RecognitionException {
      AST comparisonExpr_AST_in = _t == ASTNULL ? null : _t;

      try {
         if (_t == null) {
            _t = ASTNULL;
         }

         switch (_t.getType()) {
            case 10:
            case 19:
            case 26:
            case 34:
            case 79:
            case 80:
            case 82:
            case 83:
            case 84:
               if (this.inputState.guessing == 0 && parens) {
                  this.out("(");
               }

               this.exoticComparisonExpression(_t);
               _t = this._retTree;
               if (this.inputState.guessing == 0 && parens) {
                  this.out(")");
               }
               break;
            case 102:
            case 108:
            case 110:
            case 111:
            case 112:
            case 113:
               this.binaryComparisonExpression(_t);
               _t = this._retTree;
               break;
            default:
               throw new NoViableAltException(_t);
         }
      } catch (RecognitionException ex) {
         if (this.inputState.guessing != 0) {
            throw ex;
         }

         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this._retTree = _t;
   }

   public final void whereClauseExpr(AST _t) throws RecognitionException {
      AST whereClauseExpr_AST_in = _t == ASTNULL ? null : _t;

      try {
         boolean synPredMatched29 = false;
         if (_t == null) {
            _t = ASTNULL;
         }

         if (_t.getType() == 142) {
            synPredMatched29 = true;
            ++this.inputState.guessing;

            try {
               this.match(_t, 142);
               _t = _t.getNextSibling();
            } catch (RecognitionException var6) {
               synPredMatched29 = false;
            }

            _t = _t;
            --this.inputState.guessing;
         }

         if (synPredMatched29) {
            this.conditionList(_t);
            _t = this._retTree;
         } else {
            if (!_tokenSet_2.member(_t.getType())) {
               throw new NoViableAltException(_t);
            }

            this.booleanExpr(_t, false);
            _t = this._retTree;
         }
      } catch (RecognitionException ex) {
         if (this.inputState.guessing != 0) {
            throw ex;
         }

         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this._retTree = _t;
   }

   public final void conditionList(AST _t) throws RecognitionException {
      AST conditionList_AST_in = _t == ASTNULL ? null : _t;

      try {
         this.sqlToken(_t);
         _t = this._retTree;
         if (_t == null) {
            _t = ASTNULL;
         }

         switch (_t.getType()) {
            case 3:
               break;
            case 142:
               if (this.inputState.guessing == 0) {
                  this.out(" and ");
               }

               this.conditionList(_t);
               _t = this._retTree;
               break;
            default:
               throw new NoViableAltException(_t);
         }
      } catch (RecognitionException ex) {
         if (this.inputState.guessing != 0) {
            throw ex;
         }

         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this._retTree = _t;
   }

   public final void expr(AST _t) throws RecognitionException {
      AST expr_AST_in = _t == ASTNULL ? null : _t;

      try {
         if (_t == null) {
            _t = ASTNULL;
         }

         switch (_t.getType()) {
            case 4:
               AST __t119 = _t;
               this.match(_t, 4);
               _t = _t.getFirstChild();
               if (this.inputState.guessing == 0) {
                  this.out("all ");
               }

               this.quantified(_t);
               _t = this._retTree;
               _t = __t119.getNextSibling();
               break;
            case 5:
               AST __t118 = _t;
               this.match(_t, 5);
               _t = _t.getFirstChild();
               if (this.inputState.guessing == 0) {
                  this.out("any ");
               }

               this.quantified(_t);
               _t = this._retTree;
               _t = __t118.getNextSibling();
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
            case 86:
            case 87:
            case 88:
            case 89:
            case 91:
            case 93:
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
            case 127:
            case 128:
            case 129:
            case 130:
            case 131:
            case 132:
            case 133:
            case 134:
            case 135:
            case 136:
            case 137:
            case 138:
            case 139:
            case 141:
            case 143:
            case 144:
            case 145:
            case 146:
            case 147:
            case 149:
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
            case 94:
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
            case 123:
            case 124:
            case 125:
            case 126:
            case 140:
            case 142:
            case 148:
            case 150:
               this.simpleExpr(_t);
               _t = this._retTree;
               break;
            case 45:
               this.parenSelect(_t);
               _t = this._retTree;
               break;
            case 47:
               AST __t120 = _t;
               this.match(_t, 47);
               _t = _t.getFirstChild();
               if (this.inputState.guessing == 0) {
                  this.out("some ");
               }

               this.quantified(_t);
               _t = this._retTree;
               _t = __t120.getNextSibling();
               break;
            case 92:
               this.tupleExpr(_t);
               _t = this._retTree;
         }
      } catch (RecognitionException ex) {
         if (this.inputState.guessing != 0) {
            throw ex;
         }

         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this._retTree = _t;
   }

   public final void orderDirection(AST _t) throws RecognitionException {
      AST orderDirection_AST_in = _t == ASTNULL ? null : _t;

      try {
         if (_t == null) {
            _t = ASTNULL;
         }

         switch (_t.getType()) {
            case 8:
               this.match(_t, 8);
               _t = _t.getNextSibling();
               break;
            case 14:
               this.match(_t, 14);
               _t = _t.getNextSibling();
               break;
            default:
               throw new NoViableAltException(_t);
         }
      } catch (RecognitionException ex) {
         if (this.inputState.guessing != 0) {
            throw ex;
         }

         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this._retTree = _t;
   }

   public final void filters(AST _t) throws RecognitionException {
      AST filters_AST_in = _t == ASTNULL ? null : _t;

      try {
         AST __t42 = _t;
         this.match(_t, 146);
         _t = _t.getFirstChild();
         this.conditionList(_t);
         _t = this._retTree;
         _t = __t42.getNextSibling();
      } catch (RecognitionException ex) {
         if (this.inputState.guessing != 0) {
            throw ex;
         }

         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this._retTree = _t;
   }

   public final void thetaJoins(AST _t) throws RecognitionException {
      AST thetaJoins_AST_in = _t == ASTNULL ? null : _t;

      try {
         AST __t44 = _t;
         this.match(_t, 145);
         _t = _t.getFirstChild();
         this.conditionList(_t);
         _t = this._retTree;
         _t = __t44.getNextSibling();
      } catch (RecognitionException ex) {
         if (this.inputState.guessing != 0) {
            throw ex;
         }

         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this._retTree = _t;
   }

   public final void sqlToken(AST _t) throws RecognitionException {
      AST sqlToken_AST_in = _t == ASTNULL ? null : _t;
      AST t = null;

      try {
         t = _t;
         this.match(_t, 142);
         _t = _t.getNextSibling();
         if (this.inputState.guessing == 0) {
            this.out(t);
         }
      } catch (RecognitionException ex) {
         if (this.inputState.guessing != 0) {
            throw ex;
         }

         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this._retTree = _t;
   }

   public final void distinctOrAll(AST _t) throws RecognitionException {
      AST distinctOrAll_AST_in = _t == ASTNULL ? null : _t;

      try {
         if (_t == null) {
            _t = ASTNULL;
         }

         switch (_t.getType()) {
            case 4:
               this.match(_t, 4);
               _t = _t.getNextSibling();
               if (this.inputState.guessing == 0) {
                  this.out("all ");
               }
               break;
            case 16:
               this.match(_t, 16);
               _t = _t.getNextSibling();
               if (this.inputState.guessing == 0) {
                  this.out("distinct ");
               }
               break;
            default:
               throw new NoViableAltException(_t);
         }
      } catch (RecognitionException ex) {
         if (this.inputState.guessing != 0) {
            throw ex;
         }

         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this._retTree = _t;
   }

   public final void selectColumn(AST _t) throws RecognitionException {
      AST selectColumn_AST_in = _t == ASTNULL ? null : _t;
      AST p = null;
      AST sc = null;

      try {
         AST var7 = _t == ASTNULL ? null : _t;
         this.selectExpr(_t);
         _t = this._retTree;
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
            case 39:
            case 40:
            case 41:
            case 42:
            case 43:
            case 44:
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
            case 86:
            case 87:
            case 88:
            case 89:
            case 91:
            case 92:
            case 93:
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
            case 127:
            case 128:
            case 129:
            case 130:
            case 131:
            case 132:
            case 133:
            case 134:
            case 135:
            case 136:
            case 137:
            case 138:
            case 139:
            case 141:
            case 145:
            case 146:
            case 147:
            case 148:
            case 149:
            case 150:
            default:
               throw new NoViableAltException(_t);
            case 143:
               sc = _t;
               this.match(_t, 143);
               _t = _t.getNextSibling();
               if (this.inputState.guessing == 0) {
                  this.out(sc);
               }
            case 3:
            case 12:
            case 15:
            case 20:
            case 45:
            case 49:
            case 54:
            case 68:
            case 69:
            case 70:
            case 71:
            case 73:
            case 74:
            case 81:
            case 90:
            case 94:
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
            case 123:
            case 124:
            case 125:
            case 126:
            case 140:
            case 142:
            case 144:
            case 151:
               if (this.inputState.guessing == 0) {
                  this.separator((AST)(sc != null ? sc : var7), ", ");
               }
         }
      } catch (RecognitionException ex) {
         if (this.inputState.guessing != 0) {
            throw ex;
         }

         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this._retTree = _t;
   }

   public final void selectExpr(AST _t) throws RecognitionException {
      AST selectExpr_AST_in = _t == ASTNULL ? null : _t;
      AST e = null;
      AST mcr = null;
      AST c = null;
      AST param = null;
      AST sn = null;

      try {
         if (_t == null) {
            _t = ASTNULL;
         }

         label98:
         switch (_t.getType()) {
            case 12:
               this.count(_t);
               _t = this._retTree;
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
            case 39:
            case 40:
            case 41:
            case 42:
            case 43:
            case 44:
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
            case 86:
            case 87:
            case 88:
            case 89:
            case 91:
            case 92:
            case 93:
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
            case 127:
            case 128:
            case 129:
            case 130:
            case 131:
            case 132:
            case 133:
            case 134:
            case 135:
            case 136:
            case 137:
            case 138:
            case 139:
            case 141:
            case 143:
            case 145:
            case 146:
            case 147:
            case 148:
            case 149:
            case 150:
            default:
               throw new NoViableAltException(_t);
            case 15:
            case 140:
            case 142:
            case 144:
               AST var14 = _t == ASTNULL ? null : _t;
               this.selectAtom(_t);
               _t = this._retTree;
               if (this.inputState.guessing == 0) {
                  this.out((AST)var14);
               }
               break;
            case 20:
            case 49:
            case 94:
            case 95:
            case 96:
            case 97:
            case 98:
            case 99:
            case 100:
            case 124:
            case 125:
            case 126:
               AST var16 = _t == ASTNULL ? null : _t;
               this.constant(_t);
               _t = this._retTree;
               if (this.inputState.guessing == 0) {
                  this.out((AST)var16);
               }
               break;
            case 45:
               if (this.inputState.guessing == 0) {
                  this.out("(");
               }

               this.selectStatement(_t);
               _t = this._retTree;
               if (this.inputState.guessing == 0) {
                  this.out(")");
               }
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
               break;
            case 68:
            case 69:
            case 70:
               AST var15 = _t == ASTNULL ? null : _t;
               this.mapComponentReference(_t);
               _t = this._retTree;
               if (this.inputState.guessing == 0) {
                  this.out((AST)var15);
               }
               break;
            case 71:
               this.aggregate(_t);
               _t = this._retTree;
               break;
            case 73:
               this.match(_t, 73);
               _t = _t.getFirstChild();
               if (_t == null) {
                  _t = ASTNULL;
               }

               switch (_t.getType()) {
                  case 15:
                     this.match(_t, 15);
                     _t = _t.getNextSibling();
                     break;
                  case 126:
                     this.match(_t, 126);
                     _t = _t.getNextSibling();
                     break;
                  default:
                     throw new NoViableAltException(_t);
               }

               int _cnt58 = 0;

               while(true) {
                  if (_t == null) {
                     _t = ASTNULL;
                  }

                  if (!_tokenSet_0.member(_t.getType())) {
                     if (_cnt58 < 1) {
                        throw new NoViableAltException(_t);
                     }

                     _t = _t.getNextSibling();
                     break label98;
                  }

                  this.selectColumn(_t);
                  _t = this._retTree;
                  ++_cnt58;
               }
            case 81:
               this.methodCall(_t);
               _t = this._retTree;
               break;
            case 123:
               AST var17 = _t;
               this.match(_t, 123);
               _t = _t.getNextSibling();
               if (this.inputState.guessing == 0) {
                  this.out((AST)var17);
               }
               break;
            case 151:
               AST var18 = _t;
               this.match(_t, 151);
               _t = _t.getNextSibling();
               if (this.inputState.guessing == 0) {
                  this.out((AST)var18);
               }
         }
      } catch (RecognitionException ex) {
         if (this.inputState.guessing != 0) {
            throw ex;
         }

         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this._retTree = _t;
   }

   public final void selectAtom(AST _t) throws RecognitionException {
      AST selectAtom_AST_in = _t == ASTNULL ? null : _t;

      try {
         if (_t == null) {
            _t = ASTNULL;
         }

         switch (_t.getType()) {
            case 15:
               this.match(_t, 15);
               _t = _t.getNextSibling();
               break;
            case 140:
               this.match(_t, 140);
               _t = _t.getNextSibling();
               break;
            case 142:
               this.match(_t, 142);
               _t = _t.getNextSibling();
               break;
            case 144:
               this.match(_t, 144);
               _t = _t.getNextSibling();
               break;
            default:
               throw new NoViableAltException(_t);
         }
      } catch (RecognitionException ex) {
         if (this.inputState.guessing != 0) {
            throw ex;
         }

         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this._retTree = _t;
   }

   public final void mapComponentReference(AST _t) throws RecognitionException {
      AST mapComponentReference_AST_in = _t == ASTNULL ? null : _t;

      try {
         if (_t == null) {
            _t = ASTNULL;
         }

         switch (_t.getType()) {
            case 68:
               this.match(_t, 68);
               _t = _t.getNextSibling();
               break;
            case 69:
               this.match(_t, 69);
               _t = _t.getNextSibling();
               break;
            case 70:
               this.match(_t, 70);
               _t = _t.getNextSibling();
               break;
            default:
               throw new NoViableAltException(_t);
         }
      } catch (RecognitionException ex) {
         if (this.inputState.guessing != 0) {
            throw ex;
         }

         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this._retTree = _t;
   }

   public final void count(AST _t) throws RecognitionException {
      AST count_AST_in = _t == ASTNULL ? null : _t;

      try {
         AST __t60 = _t;
         this.match(_t, 12);
         AST var6 = _t.getFirstChild();
         if (this.inputState.guessing == 0) {
            this.out("count(");
         }

         if (var6 == null) {
            var6 = ASTNULL;
         }

         switch (((AST)var6).getType()) {
            case 4:
            case 16:
               this.distinctOrAll((AST)var6);
               var6 = this._retTree;
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
            case 88:
            case 90:
            case 94:
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
            case 123:
            case 124:
            case 125:
            case 126:
            case 140:
            case 142:
            case 148:
            case 150:
               this.countExpr((AST)var6);
               AST var7 = this._retTree;
               if (this.inputState.guessing == 0) {
                  this.out(")");
               }

               _t = __t60.getNextSibling();
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
            case 89:
            case 91:
            case 92:
            case 93:
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
            case 127:
            case 128:
            case 129:
            case 130:
            case 131:
            case 132:
            case 133:
            case 134:
            case 135:
            case 136:
            case 137:
            case 138:
            case 139:
            case 141:
            case 143:
            case 144:
            case 145:
            case 146:
            case 147:
            case 149:
            default:
               throw new NoViableAltException((AST)var6);
         }
      } catch (RecognitionException ex) {
         if (this.inputState.guessing != 0) {
            throw ex;
         }

         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this._retTree = _t;
   }

   public final void methodCall(AST _t) throws RecognitionException {
      AST methodCall_AST_in = _t == ASTNULL ? null : _t;
      AST m = null;
      AST i = null;

      try {
         AST __t161 = _t;
         m = _t == ASTNULL ? null : _t;
         this.match(_t, 81);
         _t = _t.getFirstChild();
         i = _t;
         this.match(_t, 147);
         _t = _t.getNextSibling();
         if (this.inputState.guessing == 0) {
            this.beginFunctionTemplate(m, i);
         }

         if (_t == null) {
            _t = ASTNULL;
         }

         switch (_t.getType()) {
            case 75:
               AST __t163 = _t;
               this.match(_t, 75);
               AST var11 = _t.getFirstChild();
               if (var11 == null) {
                  var11 = ASTNULL;
               }

               switch (((AST)var11).getType()) {
                  case 4:
                  case 5:
                  case 12:
                  case 15:
                  case 20:
                  case 39:
                  case 45:
                  case 47:
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
                  case 94:
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
                  case 123:
                  case 124:
                  case 125:
                  case 126:
                  case 140:
                  case 142:
                  case 148:
                  case 150:
                     this.arguments((AST)var11);
                     AST var12 = this._retTree;
                  case 3:
                     AST var13 = __t163.getNextSibling();
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
                  case 86:
                  case 87:
                  case 88:
                  case 89:
                  case 91:
                  case 93:
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
                  case 127:
                  case 128:
                  case 129:
                  case 130:
                  case 131:
                  case 132:
                  case 133:
                  case 134:
                  case 135:
                  case 136:
                  case 137:
                  case 138:
                  case 139:
                  case 141:
                  case 143:
                  case 144:
                  case 145:
                  case 146:
                  case 147:
                  case 149:
                  default:
                     throw new NoViableAltException((AST)var11);
               }
            case 3:
               if (this.inputState.guessing == 0) {
                  this.endFunctionTemplate(m);
               }

               _t = __t161.getNextSibling();
               break;
            default:
               throw new NoViableAltException(_t);
         }
      } catch (RecognitionException ex) {
         if (this.inputState.guessing != 0) {
            throw ex;
         }

         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this._retTree = _t;
   }

   public final void aggregate(AST _t) throws RecognitionException {
      AST aggregate_AST_in = _t == ASTNULL ? null : _t;
      AST a = null;

      try {
         AST __t159 = _t;
         a = _t == ASTNULL ? null : _t;
         this.match(_t, 71);
         _t = _t.getFirstChild();
         if (this.inputState.guessing == 0) {
            this.beginFunctionTemplate(a, a);
         }

         this.expr(_t);
         _t = this._retTree;
         if (this.inputState.guessing == 0) {
            this.endFunctionTemplate(a);
         }

         _t = __t159.getNextSibling();
      } catch (RecognitionException ex) {
         if (this.inputState.guessing != 0) {
            throw ex;
         }

         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this._retTree = _t;
   }

   public final void constant(AST _t) throws RecognitionException {
      AST constant_AST_in = _t == ASTNULL ? null : _t;

      try {
         if (_t == null) {
            _t = ASTNULL;
         }

         switch (_t.getType()) {
            case 20:
               this.match(_t, 20);
               _t = _t.getNextSibling();
               break;
            case 49:
               this.match(_t, 49);
               _t = _t.getNextSibling();
               break;
            case 94:
               this.match(_t, 94);
               _t = _t.getNextSibling();
               break;
            case 95:
               this.match(_t, 95);
               _t = _t.getNextSibling();
               break;
            case 96:
               this.match(_t, 96);
               _t = _t.getNextSibling();
               break;
            case 97:
               this.match(_t, 97);
               _t = _t.getNextSibling();
               break;
            case 98:
               this.match(_t, 98);
               _t = _t.getNextSibling();
               break;
            case 99:
               this.match(_t, 99);
               _t = _t.getNextSibling();
               break;
            case 100:
               this.match(_t, 100);
               _t = _t.getNextSibling();
               break;
            case 124:
               this.match(_t, 124);
               _t = _t.getNextSibling();
               break;
            case 125:
               this.match(_t, 125);
               _t = _t.getNextSibling();
               break;
            case 126:
               this.match(_t, 126);
               _t = _t.getNextSibling();
               break;
            default:
               throw new NoViableAltException(_t);
         }
      } catch (RecognitionException ex) {
         if (this.inputState.guessing != 0) {
            throw ex;
         }

         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this._retTree = _t;
   }

   public final void arithmeticExpr(AST _t) throws RecognitionException {
      AST arithmeticExpr_AST_in = _t == ASTNULL ? null : _t;

      try {
         if (_t == null) {
            _t = ASTNULL;
         }

         switch (_t.getType()) {
            case 54:
            case 74:
               this.caseExpr(_t);
               _t = this._retTree;
               break;
            case 90:
               AST __t131 = _t;
               this.match(_t, 90);
               _t = _t.getFirstChild();
               if (this.inputState.guessing == 0) {
                  this.out("-");
               }

               this.nestedExprAfterMinusDiv(_t);
               _t = this._retTree;
               _t = __t131.getNextSibling();
               break;
            case 115:
            case 116:
               this.additiveExpr(_t);
               _t = this._retTree;
               break;
            case 117:
            case 118:
            case 119:
               this.multiplicativeExpr(_t);
               _t = this._retTree;
               break;
            default:
               throw new NoViableAltException(_t);
         }
      } catch (RecognitionException ex) {
         if (this.inputState.guessing != 0) {
            throw ex;
         }

         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this._retTree = _t;
   }

   public final void countExpr(AST _t) throws RecognitionException {
      AST countExpr_AST_in = _t == ASTNULL ? null : _t;

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
            case 94:
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
            case 123:
            case 124:
            case 125:
            case 126:
            case 140:
            case 142:
            case 148:
            case 150:
               this.simpleExpr(_t);
               _t = this._retTree;
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
            case 89:
            case 91:
            case 92:
            case 93:
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
            case 127:
            case 128:
            case 129:
            case 130:
            case 131:
            case 132:
            case 133:
            case 134:
            case 135:
            case 136:
            case 137:
            case 138:
            case 139:
            case 141:
            case 143:
            case 144:
            case 145:
            case 146:
            case 147:
            case 149:
            default:
               throw new NoViableAltException(_t);
            case 88:
               this.match(_t, 88);
               _t = _t.getNextSibling();
               if (this.inputState.guessing == 0) {
                  this.out("*");
               }
         }
      } catch (RecognitionException ex) {
         if (this.inputState.guessing != 0) {
            throw ex;
         }

         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this._retTree = _t;
   }

   public final void simpleExpr(AST _t) throws RecognitionException {
      AST simpleExpr_AST_in = _t == ASTNULL ? null : _t;
      AST c = null;

      try {
         if (_t == null) {
            _t = ASTNULL;
         }

         switch (_t.getType()) {
            case 12:
               this.count(_t);
               _t = this._retTree;
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
            case 92:
            case 93:
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
            case 127:
            case 128:
            case 129:
            case 130:
            case 131:
            case 132:
            case 133:
            case 134:
            case 135:
            case 136:
            case 137:
            case 138:
            case 139:
            case 141:
            case 143:
            case 144:
            case 145:
            case 146:
            case 147:
            case 149:
            default:
               throw new NoViableAltException(_t);
            case 15:
            case 68:
            case 69:
            case 70:
            case 78:
            case 140:
            case 150:
               this.addrExpr(_t);
               _t = this._retTree;
               break;
            case 20:
            case 49:
            case 94:
            case 95:
            case 96:
            case 97:
            case 98:
            case 99:
            case 100:
            case 124:
            case 125:
            case 126:
               AST var6 = _t == ASTNULL ? null : _t;
               this.constant(_t);
               _t = this._retTree;
               if (this.inputState.guessing == 0) {
                  this.out((AST)var6);
               }
               break;
            case 39:
               this.match(_t, 39);
               _t = _t.getNextSibling();
               if (this.inputState.guessing == 0) {
                  this.out("null");
               }
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
               break;
            case 71:
               this.aggregate(_t);
               _t = this._retTree;
               break;
            case 81:
               this.methodCall(_t);
               _t = this._retTree;
               break;
            case 123:
            case 148:
               this.parameter(_t);
               _t = this._retTree;
               break;
            case 142:
               this.sqlToken(_t);
               _t = this._retTree;
         }
      } catch (RecognitionException ex) {
         if (this.inputState.guessing != 0) {
            throw ex;
         }

         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this._retTree = _t;
   }

   public final void tableJoin(AST _t, AST parent) throws RecognitionException {
      AST tableJoin_AST_in = _t == ASTNULL ? null : _t;
      AST c = null;
      AST d = null;

      try {
         if (_t == null) {
            _t = ASTNULL;
         }

         label71:
         switch (_t.getType()) {
            case 134:
               AST __t81 = _t;
               AST var11 = _t == ASTNULL ? null : _t;
               this.match(_t, 134);
               _t = _t.getFirstChild();
               if (this.inputState.guessing == 0) {
                  this.nestedFromFragment((AST)var11, parent);
               }

               while(true) {
                  if (_t == null) {
                     _t = ASTNULL;
                  }

                  if (_t.getType() != 134 && _t.getType() != 136) {
                     _t = __t81.getNextSibling();
                     break label71;
                  }

                  this.tableJoin(_t, (AST)var11);
                  _t = this._retTree;
               }
            case 136:
               AST __t78 = _t;
               AST var10 = _t == ASTNULL ? null : _t;
               this.match(_t, 136);
               _t = _t.getFirstChild();
               if (this.inputState.guessing == 0) {
                  this.out(" ");
                  this.out((AST)var10);
               }

               while(true) {
                  if (_t == null) {
                     _t = ASTNULL;
                  }

                  if (_t.getType() != 134 && _t.getType() != 136) {
                     _t = __t78.getNextSibling();
                     break label71;
                  }

                  this.tableJoin(_t, (AST)var10);
                  _t = this._retTree;
               }
            default:
               throw new NoViableAltException(_t);
         }
      } catch (RecognitionException ex) {
         if (this.inputState.guessing != 0) {
            throw ex;
         }

         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this._retTree = _t;
   }

   public final void booleanOp(AST _t, boolean parens) throws RecognitionException {
      AST booleanOp_AST_in = _t == ASTNULL ? null : _t;

      try {
         if (_t == null) {
            _t = ASTNULL;
         }

         switch (_t.getType()) {
            case 6:
               AST __t85 = _t;
               this.match(_t, 6);
               _t = _t.getFirstChild();
               this.booleanExpr(_t, true);
               _t = this._retTree;
               if (this.inputState.guessing == 0) {
                  this.out(" and ");
               }

               this.booleanExpr(_t, true);
               _t = this._retTree;
               _t = __t85.getNextSibling();
               break;
            case 38:
               AST __t87 = _t;
               this.match(_t, 38);
               _t = _t.getFirstChild();
               if (this.inputState.guessing == 0) {
                  this.out(" not (");
               }

               this.booleanExpr(_t, false);
               _t = this._retTree;
               if (this.inputState.guessing == 0) {
                  this.out(")");
               }

               _t = __t87.getNextSibling();
               break;
            case 40:
               AST __t86 = _t;
               this.match(_t, 40);
               _t = _t.getFirstChild();
               if (this.inputState.guessing == 0 && parens) {
                  this.out("(");
               }

               this.booleanExpr(_t, false);
               _t = this._retTree;
               if (this.inputState.guessing == 0) {
                  this.out(" or ");
               }

               this.booleanExpr(_t, false);
               _t = this._retTree;
               if (this.inputState.guessing == 0 && parens) {
                  this.out(")");
               }

               _t = __t86.getNextSibling();
               break;
            default:
               throw new NoViableAltException(_t);
         }
      } catch (RecognitionException ex) {
         if (this.inputState.guessing != 0) {
            throw ex;
         }

         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this._retTree = _t;
   }

   public final void binaryComparisonExpression(AST _t) throws RecognitionException {
      AST binaryComparisonExpression_AST_in = _t == ASTNULL ? null : _t;

      try {
         if (_t == null) {
            _t = ASTNULL;
         }

         switch (_t.getType()) {
            case 102:
               AST __t91 = _t;
               this.match(_t, 102);
               _t = _t.getFirstChild();
               this.expr(_t);
               _t = this._retTree;
               if (this.inputState.guessing == 0) {
                  this.out("=");
               }

               this.expr(_t);
               _t = this._retTree;
               _t = __t91.getNextSibling();
               break;
            case 103:
            case 104:
            case 105:
            case 106:
            case 107:
            case 109:
            default:
               throw new NoViableAltException(_t);
            case 108:
               AST __t92 = _t;
               this.match(_t, 108);
               _t = _t.getFirstChild();
               this.expr(_t);
               _t = this._retTree;
               if (this.inputState.guessing == 0) {
                  this.out("<>");
               }

               this.expr(_t);
               _t = this._retTree;
               _t = __t92.getNextSibling();
               break;
            case 110:
               AST __t95 = _t;
               this.match(_t, 110);
               _t = _t.getFirstChild();
               this.expr(_t);
               _t = this._retTree;
               if (this.inputState.guessing == 0) {
                  this.out("<");
               }

               this.expr(_t);
               _t = this._retTree;
               _t = __t95.getNextSibling();
               break;
            case 111:
               AST __t93 = _t;
               this.match(_t, 111);
               _t = _t.getFirstChild();
               this.expr(_t);
               _t = this._retTree;
               if (this.inputState.guessing == 0) {
                  this.out(">");
               }

               this.expr(_t);
               _t = this._retTree;
               _t = __t93.getNextSibling();
               break;
            case 112:
               AST __t96 = _t;
               this.match(_t, 112);
               _t = _t.getFirstChild();
               this.expr(_t);
               _t = this._retTree;
               if (this.inputState.guessing == 0) {
                  this.out("<=");
               }

               this.expr(_t);
               _t = this._retTree;
               _t = __t96.getNextSibling();
               break;
            case 113:
               AST __t94 = _t;
               this.match(_t, 113);
               _t = _t.getFirstChild();
               this.expr(_t);
               _t = this._retTree;
               if (this.inputState.guessing == 0) {
                  this.out(">=");
               }

               this.expr(_t);
               _t = this._retTree;
               _t = __t94.getNextSibling();
         }
      } catch (RecognitionException ex) {
         if (this.inputState.guessing != 0) {
            throw ex;
         }

         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this._retTree = _t;
   }

   public final void exoticComparisonExpression(AST _t) throws RecognitionException {
      AST exoticComparisonExpression_AST_in = _t == ASTNULL ? null : _t;

      try {
         if (_t == null) {
            _t = ASTNULL;
         }

         switch (_t.getType()) {
            case 10:
               AST __t100 = _t;
               this.match(_t, 10);
               _t = _t.getFirstChild();
               this.expr(_t);
               _t = this._retTree;
               if (this.inputState.guessing == 0) {
                  this.out(" between ");
               }

               this.expr(_t);
               _t = this._retTree;
               if (this.inputState.guessing == 0) {
                  this.out(" and ");
               }

               this.expr(_t);
               _t = this._retTree;
               _t = __t100.getNextSibling();
               break;
            case 19:
               AST __t104 = _t;
               this.match(_t, 19);
               _t = _t.getFirstChild();
               if (this.inputState.guessing == 0) {
                  this.optionalSpace();
                  this.out("exists ");
               }

               this.quantified(_t);
               _t = this._retTree;
               _t = __t104.getNextSibling();
               break;
            case 26:
               AST __t102 = _t;
               this.match(_t, 26);
               _t = _t.getFirstChild();
               this.expr(_t);
               _t = this._retTree;
               if (this.inputState.guessing == 0) {
                  this.out(" in");
               }

               this.inList(_t);
               _t = this._retTree;
               _t = __t102.getNextSibling();
               break;
            case 34:
               AST __t98 = _t;
               this.match(_t, 34);
               _t = _t.getFirstChild();
               this.expr(_t);
               _t = this._retTree;
               if (this.inputState.guessing == 0) {
                  this.out(" like ");
               }

               this.expr(_t);
               _t = this._retTree;
               this.likeEscape(_t);
               _t = this._retTree;
               _t = __t98.getNextSibling();
               break;
            case 79:
               AST __t106 = _t;
               this.match(_t, 79);
               _t = _t.getFirstChild();
               this.expr(_t);
               _t = this._retTree;
               _t = __t106.getNextSibling();
               if (this.inputState.guessing == 0) {
                  this.out(" is not null");
               }
               break;
            case 80:
               AST __t105 = _t;
               this.match(_t, 80);
               _t = _t.getFirstChild();
               this.expr(_t);
               _t = this._retTree;
               _t = __t105.getNextSibling();
               if (this.inputState.guessing == 0) {
                  this.out(" is null");
               }
               break;
            case 82:
               AST __t101 = _t;
               this.match(_t, 82);
               _t = _t.getFirstChild();
               this.expr(_t);
               _t = this._retTree;
               if (this.inputState.guessing == 0) {
                  this.out(" not between ");
               }

               this.expr(_t);
               _t = this._retTree;
               if (this.inputState.guessing == 0) {
                  this.out(" and ");
               }

               this.expr(_t);
               _t = this._retTree;
               _t = __t101.getNextSibling();
               break;
            case 83:
               AST __t103 = _t;
               this.match(_t, 83);
               _t = _t.getFirstChild();
               this.expr(_t);
               _t = this._retTree;
               if (this.inputState.guessing == 0) {
                  this.out(" not in ");
               }

               this.inList(_t);
               _t = this._retTree;
               _t = __t103.getNextSibling();
               break;
            case 84:
               AST __t99 = _t;
               this.match(_t, 84);
               _t = _t.getFirstChild();
               this.expr(_t);
               _t = this._retTree;
               if (this.inputState.guessing == 0) {
                  this.out(" not like ");
               }

               this.expr(_t);
               _t = this._retTree;
               this.likeEscape(_t);
               _t = this._retTree;
               _t = __t99.getNextSibling();
               break;
            default:
               throw new NoViableAltException(_t);
         }
      } catch (RecognitionException ex) {
         if (this.inputState.guessing != 0) {
            throw ex;
         }

         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this._retTree = _t;
   }

   public final void likeEscape(AST _t) throws RecognitionException {
      AST likeEscape_AST_in = _t == ASTNULL ? null : _t;

      try {
         if (_t == null) {
            _t = ASTNULL;
         }

         switch (_t.getType()) {
            case 3:
               break;
            case 18:
               AST __t109 = _t;
               this.match(_t, 18);
               _t = _t.getFirstChild();
               if (this.inputState.guessing == 0) {
                  this.out(" escape ");
               }

               this.expr(_t);
               _t = this._retTree;
               _t = __t109.getNextSibling();
               break;
            default:
               throw new NoViableAltException(_t);
         }
      } catch (RecognitionException ex) {
         if (this.inputState.guessing != 0) {
            throw ex;
         }

         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this._retTree = _t;
   }

   public final void inList(AST _t) throws RecognitionException {
      AST inList_AST_in = _t == ASTNULL ? null : _t;

      try {
         AST __t111 = _t;
         this.match(_t, 77);
         AST var6 = _t.getFirstChild();
         if (this.inputState.guessing == 0) {
            this.out(" ");
         }

         if (var6 == null) {
            var6 = ASTNULL;
         }

         switch (((AST)var6).getType()) {
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
            case 94:
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
            case 123:
            case 124:
            case 125:
            case 126:
            case 140:
            case 142:
            case 148:
            case 150:
               this.simpleExprList((AST)var6);
               AST var8 = this._retTree;
               break;
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
            case 93:
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
            case 127:
            case 128:
            case 129:
            case 130:
            case 131:
            case 132:
            case 133:
            case 134:
            case 135:
            case 136:
            case 137:
            case 138:
            case 139:
            case 141:
            case 143:
            case 144:
            case 145:
            case 146:
            case 147:
            case 149:
            default:
               throw new NoViableAltException((AST)var6);
            case 45:
               this.parenSelect((AST)var6);
               AST var7 = this._retTree;
         }

         _t = __t111.getNextSibling();
      } catch (RecognitionException ex) {
         if (this.inputState.guessing != 0) {
            throw ex;
         }

         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this._retTree = _t;
   }

   public final void quantified(AST _t) throws RecognitionException {
      AST quantified_AST_in = _t == ASTNULL ? null : _t;

      try {
         if (this.inputState.guessing == 0) {
            this.out("(");
         }

         if (_t == null) {
            _t = ASTNULL;
         }

         switch (_t.getType()) {
            case 45:
               this.selectStatement(_t);
               _t = this._retTree;
               break;
            case 142:
               this.sqlToken(_t);
               _t = this._retTree;
               break;
            default:
               throw new NoViableAltException(_t);
         }

         if (this.inputState.guessing == 0) {
            this.out(")");
         }
      } catch (RecognitionException ex) {
         if (this.inputState.guessing != 0) {
            throw ex;
         }

         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this._retTree = _t;
   }

   public final void parenSelect(AST _t) throws RecognitionException {
      AST parenSelect_AST_in = _t == ASTNULL ? null : _t;

      try {
         if (this.inputState.guessing == 0) {
            this.out("(");
         }

         this.selectStatement(_t);
         _t = this._retTree;
         if (this.inputState.guessing == 0) {
            this.out(")");
         }
      } catch (RecognitionException ex) {
         if (this.inputState.guessing != 0) {
            throw ex;
         }

         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this._retTree = _t;
   }

   public final void simpleExprList(AST _t) throws RecognitionException {
      AST simpleExprList_AST_in = _t == ASTNULL ? null : _t;
      AST e = null;

      try {
         if (this.inputState.guessing == 0) {
            this.out("(");
         }

         while(true) {
            if (_t == null) {
               _t = ASTNULL;
            }

            if (!_tokenSet_3.member(_t.getType())) {
               if (this.inputState.guessing == 0) {
                  this.out(")");
               }
               break;
            }

            AST var6 = _t == ASTNULL ? null : _t;
            this.simpleOrTupleExpr(_t);
            _t = this._retTree;
            if (this.inputState.guessing == 0) {
               this.separator((AST)var6, " , ");
            }
         }
      } catch (RecognitionException ex) {
         if (this.inputState.guessing != 0) {
            throw ex;
         }

         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this._retTree = _t;
   }

   public final void simpleOrTupleExpr(AST _t) throws RecognitionException {
      AST simpleOrTupleExpr_AST_in = _t == ASTNULL ? null : _t;

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
            case 94:
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
            case 123:
            case 124:
            case 125:
            case 126:
            case 140:
            case 142:
            case 148:
            case 150:
               this.simpleExpr(_t);
               _t = this._retTree;
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
            case 93:
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
            case 127:
            case 128:
            case 129:
            case 130:
            case 131:
            case 132:
            case 133:
            case 134:
            case 135:
            case 136:
            case 137:
            case 138:
            case 139:
            case 141:
            case 143:
            case 144:
            case 145:
            case 146:
            case 147:
            case 149:
            default:
               throw new NoViableAltException(_t);
            case 92:
               this.tupleExpr(_t);
               _t = this._retTree;
         }
      } catch (RecognitionException ex) {
         if (this.inputState.guessing != 0) {
            throw ex;
         }

         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this._retTree = _t;
   }

   public final void tupleExpr(AST _t) throws RecognitionException {
      AST tupleExpr_AST_in = _t == ASTNULL ? null : _t;
      AST e = null;

      try {
         AST __t122 = _t;
         this.match(_t, 92);
         AST var7 = _t.getFirstChild();
         if (this.inputState.guessing == 0) {
            this.out("(");
         }

         while(true) {
            if (var7 == null) {
               var7 = ASTNULL;
            }

            if (!_tokenSet_4.member(((AST)var7).getType())) {
               if (this.inputState.guessing == 0) {
                  this.out(")");
               }

               _t = __t122.getNextSibling();
               break;
            }

            AST var8 = var7 == ASTNULL ? null : var7;
            this.expr((AST)var7);
            var7 = this._retTree;
            if (this.inputState.guessing == 0) {
               this.separator((AST)var8, " , ");
            }
         }
      } catch (RecognitionException ex) {
         if (this.inputState.guessing != 0) {
            throw ex;
         }

         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this._retTree = _t;
   }

   public final void addrExpr(AST _t) throws RecognitionException {
      AST addrExpr_AST_in = _t == ASTNULL ? null : _t;
      AST r = null;
      AST i = null;
      AST j = null;
      AST v = null;
      AST mcr = null;

      try {
         if (_t == null) {
            _t = ASTNULL;
         }

         switch (_t.getType()) {
            case 15:
               AST __t170 = _t;
               AST var15 = _t == ASTNULL ? null : _t;
               this.match(_t, 15);
               _t = _t.getFirstChild();
               if (_t == null) {
                  throw new MismatchedTokenException();
               }

               _t = _t.getNextSibling();
               if (_t == null) {
                  throw new MismatchedTokenException();
               }

               _t = _t.getNextSibling();
               _t = __t170.getNextSibling();
               if (this.inputState.guessing == 0) {
                  this.out((AST)var15);
               }
               break;
            case 68:
            case 69:
            case 70:
               AST var19 = _t == ASTNULL ? null : _t;
               this.mapComponentReference(_t);
               _t = this._retTree;
               if (this.inputState.guessing == 0) {
                  this.out((AST)var19);
               }
               break;
            case 78:
               AST var17 = _t;
               this.match(_t, 78);
               _t = _t.getNextSibling();
               if (this.inputState.guessing == 0) {
                  this.out((AST)var17);
               }
               break;
            case 140:
               AST var16 = _t;
               this.match(_t, 140);
               _t = _t.getNextSibling();
               if (this.inputState.guessing == 0) {
                  this.out((AST)var16);
               }
               break;
            case 150:
               AST var18 = _t;
               this.match(_t, 150);
               _t = _t.getNextSibling();
               if (this.inputState.guessing == 0) {
                  this.out((AST)var18);
               }
               break;
            default:
               throw new NoViableAltException(_t);
         }
      } catch (RecognitionException ex) {
         if (this.inputState.guessing != 0) {
            throw ex;
         }

         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this._retTree = _t;
   }

   public final void parameter(AST _t) throws RecognitionException {
      AST parameter_AST_in = _t == ASTNULL ? null : _t;
      AST n = null;
      AST p = null;

      try {
         if (_t == null) {
            _t = ASTNULL;
         }

         switch (_t.getType()) {
            case 123:
               AST var8 = _t;
               this.match(_t, 123);
               _t = _t.getNextSibling();
               if (this.inputState.guessing == 0) {
                  this.out((AST)var8);
               }
               break;
            case 148:
               AST var7 = _t;
               this.match(_t, 148);
               _t = _t.getNextSibling();
               if (this.inputState.guessing == 0) {
                  this.out((AST)var7);
               }
               break;
            default:
               throw new NoViableAltException(_t);
         }
      } catch (RecognitionException ex) {
         if (this.inputState.guessing != 0) {
            throw ex;
         }

         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this._retTree = _t;
   }

   public final void additiveExpr(AST _t) throws RecognitionException {
      AST additiveExpr_AST_in = _t == ASTNULL ? null : _t;

      try {
         if (_t == null) {
            _t = ASTNULL;
         }

         switch (_t.getType()) {
            case 115:
               AST __t133 = _t;
               this.match(_t, 115);
               _t = _t.getFirstChild();
               this.expr(_t);
               _t = this._retTree;
               if (this.inputState.guessing == 0) {
                  this.out("+");
               }

               this.expr(_t);
               _t = this._retTree;
               _t = __t133.getNextSibling();
               break;
            case 116:
               AST __t134 = _t;
               this.match(_t, 116);
               _t = _t.getFirstChild();
               this.expr(_t);
               _t = this._retTree;
               if (this.inputState.guessing == 0) {
                  this.out("-");
               }

               this.nestedExprAfterMinusDiv(_t);
               _t = this._retTree;
               _t = __t134.getNextSibling();
               break;
            default:
               throw new NoViableAltException(_t);
         }
      } catch (RecognitionException ex) {
         if (this.inputState.guessing != 0) {
            throw ex;
         }

         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this._retTree = _t;
   }

   public final void multiplicativeExpr(AST _t) throws RecognitionException {
      AST multiplicativeExpr_AST_in = _t == ASTNULL ? null : _t;

      try {
         if (_t == null) {
            _t = ASTNULL;
         }

         switch (_t.getType()) {
            case 117:
               AST __t136 = _t;
               this.match(_t, 117);
               _t = _t.getFirstChild();
               this.nestedExpr(_t);
               _t = this._retTree;
               if (this.inputState.guessing == 0) {
                  this.out("*");
               }

               this.nestedExpr(_t);
               _t = this._retTree;
               _t = __t136.getNextSibling();
               break;
            case 118:
               AST __t137 = _t;
               this.match(_t, 118);
               _t = _t.getFirstChild();
               this.nestedExpr(_t);
               _t = this._retTree;
               if (this.inputState.guessing == 0) {
                  this.out("/");
               }

               this.nestedExprAfterMinusDiv(_t);
               _t = this._retTree;
               _t = __t137.getNextSibling();
               break;
            case 119:
               AST __t138 = _t;
               this.match(_t, 119);
               _t = _t.getFirstChild();
               this.nestedExpr(_t);
               _t = this._retTree;
               if (this.inputState.guessing == 0) {
                  this.out(" % ");
               }

               this.nestedExprAfterMinusDiv(_t);
               _t = this._retTree;
               _t = __t138.getNextSibling();
               break;
            default:
               throw new NoViableAltException(_t);
         }
      } catch (RecognitionException ex) {
         if (this.inputState.guessing != 0) {
            throw ex;
         }

         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this._retTree = _t;
   }

   public final void nestedExprAfterMinusDiv(AST _t) throws RecognitionException {
      AST nestedExprAfterMinusDiv_AST_in = _t == ASTNULL ? null : _t;

      try {
         boolean synPredMatched144 = false;
         if (_t == null) {
            _t = ASTNULL;
         }

         if (_tokenSet_5.member(_t.getType())) {
            synPredMatched144 = true;
            ++this.inputState.guessing;

            try {
               this.arithmeticExpr(_t);
               _t = this._retTree;
            } catch (RecognitionException var6) {
               synPredMatched144 = false;
            }

            _t = _t;
            --this.inputState.guessing;
         }

         if (synPredMatched144) {
            if (this.inputState.guessing == 0) {
               this.out("(");
            }

            this.arithmeticExpr(_t);
            _t = this._retTree;
            if (this.inputState.guessing == 0) {
               this.out(")");
            }
         } else {
            if (!_tokenSet_4.member(_t.getType())) {
               throw new NoViableAltException(_t);
            }

            this.expr(_t);
            _t = this._retTree;
         }
      } catch (RecognitionException ex) {
         if (this.inputState.guessing != 0) {
            throw ex;
         }

         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this._retTree = _t;
   }

   public final void caseExpr(AST _t) throws RecognitionException {
      AST caseExpr_AST_in = _t == ASTNULL ? null : _t;

      try {
         if (_t == null) {
            _t = ASTNULL;
         }

         label123:
         switch (_t.getType()) {
            case 54:
               this.match(_t, 54);
               _t = _t.getFirstChild();
               if (this.inputState.guessing == 0) {
                  this.out("case");
               }

               int _cnt149 = 0;

               while(true) {
                  if (_t == null) {
                     _t = ASTNULL;
                  }

                  if (_t.getType() != 58) {
                     if (_cnt149 < 1) {
                        throw new NoViableAltException(_t);
                     }

                     if (_t == null) {
                        _t = ASTNULL;
                     }

                     switch (_t.getType()) {
                        case 56:
                           this.match(_t, 56);
                           _t = _t.getFirstChild();
                           if (this.inputState.guessing == 0) {
                              this.out(" else ");
                           }

                           this.expr(_t);
                           AST var22 = this._retTree;
                           _t = _t.getNextSibling();
                        case 3:
                           if (this.inputState.guessing == 0) {
                              this.out(" end");
                           }

                           _t = _t.getNextSibling();
                           break label123;
                        default:
                           throw new NoViableAltException(_t);
                     }
                  }

                  this.match(_t, 58);
                  _t = _t.getFirstChild();
                  if (this.inputState.guessing == 0) {
                     this.out(" when ");
                  }

                  this.booleanExpr(_t, false);
                  _t = this._retTree;
                  if (this.inputState.guessing == 0) {
                     this.out(" then ");
                  }

                  this.expr(_t);
                  AST var20 = this._retTree;
                  _t = _t.getNextSibling();
                  ++_cnt149;
               }
            case 74:
               this.match(_t, 74);
               _t = _t.getFirstChild();
               if (this.inputState.guessing == 0) {
                  this.out("case ");
               }

               this.expr(_t);
               _t = this._retTree;
               int _cnt155 = 0;

               while(true) {
                  if (_t == null) {
                     _t = ASTNULL;
                  }

                  if (_t.getType() != 58) {
                     if (_cnt155 < 1) {
                        throw new NoViableAltException(_t);
                     }

                     if (_t == null) {
                        _t = ASTNULL;
                     }

                     switch (_t.getType()) {
                        case 56:
                           this.match(_t, 56);
                           _t = _t.getFirstChild();
                           if (this.inputState.guessing == 0) {
                              this.out(" else ");
                           }

                           this.expr(_t);
                           AST var15 = this._retTree;
                           _t = _t.getNextSibling();
                        case 3:
                           if (this.inputState.guessing == 0) {
                              this.out(" end");
                           }

                           _t = _t.getNextSibling();
                           break label123;
                        default:
                           throw new NoViableAltException(_t);
                     }
                  }

                  this.match(_t, 58);
                  _t = _t.getFirstChild();
                  if (this.inputState.guessing == 0) {
                     this.out(" when ");
                  }

                  this.expr(_t);
                  _t = this._retTree;
                  if (this.inputState.guessing == 0) {
                     this.out(" then ");
                  }

                  this.expr(_t);
                  AST var13 = this._retTree;
                  _t = _t.getNextSibling();
                  ++_cnt155;
               }
            default:
               throw new NoViableAltException(_t);
         }
      } catch (RecognitionException ex) {
         if (this.inputState.guessing != 0) {
            throw ex;
         }

         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this._retTree = _t;
   }

   public final void nestedExpr(AST _t) throws RecognitionException {
      AST nestedExpr_AST_in = _t == ASTNULL ? null : _t;

      try {
         boolean synPredMatched141 = false;
         if (_t == null) {
            _t = ASTNULL;
         }

         if (_t.getType() == 115 || _t.getType() == 116) {
            synPredMatched141 = true;
            ++this.inputState.guessing;

            try {
               this.additiveExpr(_t);
               _t = this._retTree;
            } catch (RecognitionException var6) {
               synPredMatched141 = false;
            }

            _t = _t;
            --this.inputState.guessing;
         }

         if (synPredMatched141) {
            if (this.inputState.guessing == 0) {
               this.out("(");
            }

            this.additiveExpr(_t);
            _t = this._retTree;
            if (this.inputState.guessing == 0) {
               this.out(")");
            }
         } else {
            if (!_tokenSet_4.member(_t.getType())) {
               throw new NoViableAltException(_t);
            }

            this.expr(_t);
            _t = this._retTree;
         }
      } catch (RecognitionException ex) {
         if (this.inputState.guessing != 0) {
            throw ex;
         }

         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this._retTree = _t;
   }

   public final void arguments(AST _t) throws RecognitionException {
      AST arguments_AST_in = _t == ASTNULL ? null : _t;

      try {
         this.expr(_t);
         _t = this._retTree;

         while(true) {
            if (_t == null) {
               _t = ASTNULL;
            }

            if (!_tokenSet_4.member(_t.getType())) {
               break;
            }

            if (this.inputState.guessing == 0) {
               this.commaBetweenParameters(", ");
            }

            this.expr(_t);
            _t = this._retTree;
         }
      } catch (RecognitionException ex) {
         if (this.inputState.guessing != 0) {
            throw ex;
         }

         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this._retTree = _t;
   }

   private static final long[] mk_tokenSet_0() {
      long[] data = new long[]{18612532836077568L, 8716717215208048368L, 8474624L, 0L, 0L, 0L};
      return data;
   }

   private static final long[] mk_tokenSet_1() {
      long[] data = new long[]{17247503360L, 1073398228549632L, 0L, 0L};
      return data;
   }

   private static final long[] mk_tokenSet_2() {
      long[] data = new long[]{1391637038144L, 1073398228549632L, 16384L, 0L, 0L, 0L};
      return data;
   }

   private static final long[] mk_tokenSet_3() {
      long[] data = new long[]{18577898219802624L, 8716717215476499696L, 5263360L, 0L, 0L, 0L};
      return data;
   }

   private static final long[] mk_tokenSet_4() {
      long[] data = new long[]{18753820080246832L, 8716717215476499696L, 5263360L, 0L, 0L, 0L};
      return data;
   }

   private static final long[] mk_tokenSet_5() {
      long[] data = new long[]{18014398509481984L, 69805794291352576L, 0L, 0L};
      return data;
   }
}
