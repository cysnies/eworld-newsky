package org.hibernate.sql.ordering.antlr;

import antlr.ASTFactory;
import antlr.ASTPair;
import antlr.LLkParser;
import antlr.NoViableAltException;
import antlr.ParserSharedInputState;
import antlr.RecognitionException;
import antlr.Token;
import antlr.TokenBuffer;
import antlr.TokenStream;
import antlr.TokenStreamException;
import antlr.collections.AST;
import antlr.collections.impl.ASTArray;
import antlr.collections.impl.BitSet;

public class GeneratedOrderByFragmentParser extends LLkParser implements OrderByTemplateTokenTypes {
   public static final String[] _tokenNames = new String[]{"<0>", "EOF", "<2>", "NULL_TREE_LOOKAHEAD", "ORDER_BY", "SORT_SPEC", "ORDER_SPEC", "SORT_KEY", "EXPR_LIST", "DOT", "IDENT_LIST", "COLUMN_REF", "\"collate\"", "\"asc\"", "\"desc\"", "COMMA", "HARD_QUOTE", "IDENT", "OPEN_PAREN", "CLOSE_PAREN", "NUM_DOUBLE", "NUM_FLOAT", "NUM_INT", "NUM_LONG", "QUOTED_STRING", "\"ascending\"", "\"descending\"", "ID_START_LETTER", "ID_LETTER", "ESCqs", "HEX_DIGIT", "EXPONENT", "FLOAT_SUFFIX", "WS"};
   public static final BitSet _tokenSet_0 = new BitSet(mk_tokenSet_0());
   public static final BitSet _tokenSet_1 = new BitSet(mk_tokenSet_1());
   public static final BitSet _tokenSet_2 = new BitSet(mk_tokenSet_2());
   public static final BitSet _tokenSet_3 = new BitSet(mk_tokenSet_3());
   public static final BitSet _tokenSet_4 = new BitSet(mk_tokenSet_4());
   public static final BitSet _tokenSet_5 = new BitSet(mk_tokenSet_5());
   public static final BitSet _tokenSet_6 = new BitSet(mk_tokenSet_6());
   public static final BitSet _tokenSet_7 = new BitSet(mk_tokenSet_7());
   public static final BitSet _tokenSet_8 = new BitSet(mk_tokenSet_8());

   protected void trace(String msg) {
      System.out.println(msg);
   }

   protected final String extractText(AST ast) {
      return ast.getText();
   }

   protected AST quotedIdentifier(AST ident) {
      return ident;
   }

   protected AST quotedString(AST ident) {
      return ident;
   }

   protected boolean isFunctionName(AST ast) {
      return false;
   }

   protected AST resolveFunction(AST ast) {
      return ast;
   }

   protected AST resolveIdent(AST ident) {
      return ident;
   }

   protected AST postProcessSortSpecification(AST sortSpec) {
      return sortSpec;
   }

   protected GeneratedOrderByFragmentParser(TokenBuffer tokenBuf, int k) {
      super(tokenBuf, k);
      this.tokenNames = _tokenNames;
      this.buildTokenTypeASTClassMap();
      this.astFactory = new ASTFactory(this.getTokenTypeToASTClassMap());
   }

   public GeneratedOrderByFragmentParser(TokenBuffer tokenBuf) {
      this((TokenBuffer)tokenBuf, 3);
   }

   protected GeneratedOrderByFragmentParser(TokenStream lexer, int k) {
      super(lexer, k);
      this.tokenNames = _tokenNames;
      this.buildTokenTypeASTClassMap();
      this.astFactory = new ASTFactory(this.getTokenTypeToASTClassMap());
   }

   public GeneratedOrderByFragmentParser(TokenStream lexer) {
      this((TokenStream)lexer, 3);
   }

   public GeneratedOrderByFragmentParser(ParserSharedInputState state) {
      super(state, 3);
      this.tokenNames = _tokenNames;
      this.buildTokenTypeASTClassMap();
      this.astFactory = new ASTFactory(this.getTokenTypeToASTClassMap());
   }

   public final void orderByFragment() throws RecognitionException, TokenStreamException {
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST orderByFragment_AST = null;
      this.trace("orderByFragment");

      try {
         this.sortSpecification();
         this.astFactory.addASTChild(currentAST, this.returnAST);

         while(this.LA(1) == 15) {
            this.match(15);
            this.sortSpecification();
            this.astFactory.addASTChild(currentAST, this.returnAST);
         }

         if (this.inputState.guessing == 0) {
            orderByFragment_AST = currentAST.root;
            orderByFragment_AST = this.astFactory.make((new ASTArray(2)).add(this.astFactory.create(4, "order-by")).add(orderByFragment_AST));
            currentAST.root = orderByFragment_AST;
            currentAST.child = orderByFragment_AST != null && orderByFragment_AST.getFirstChild() != null ? orderByFragment_AST.getFirstChild() : orderByFragment_AST;
            currentAST.advanceChildToEnd();
         }

         orderByFragment_AST = currentAST.root;
      } catch (RecognitionException ex) {
         if (this.inputState.guessing != 0) {
            throw ex;
         }

         this.reportError(ex);
         this.recover(ex, _tokenSet_0);
      }

      this.returnAST = orderByFragment_AST;
   }

   public final void sortSpecification() throws RecognitionException, TokenStreamException {
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST sortSpecification_AST = null;
      this.trace("sortSpecification");

      try {
         this.sortKey();
         this.astFactory.addASTChild(currentAST, this.returnAST);
         label32:
         switch (this.LA(1)) {
            case 12:
               this.collationSpecification();
               this.astFactory.addASTChild(currentAST, this.returnAST);
            case 1:
            case 13:
            case 14:
            case 15:
            case 25:
            case 26:
               switch (this.LA(1)) {
                  case 1:
                  case 15:
                     break label32;
                  case 13:
                  case 14:
                  case 25:
                  case 26:
                     this.orderingSpecification();
                     this.astFactory.addASTChild(currentAST, this.returnAST);
                     break label32;
                  default:
                     throw new NoViableAltException(this.LT(1), this.getFilename());
               }
            default:
               throw new NoViableAltException(this.LT(1), this.getFilename());
         }

         if (this.inputState.guessing == 0) {
            sortSpecification_AST = currentAST.root;
            sortSpecification_AST = this.astFactory.make((new ASTArray(2)).add(this.astFactory.create(5, "{sort specification}")).add(sortSpecification_AST));
            sortSpecification_AST = this.postProcessSortSpecification(sortSpecification_AST);
            currentAST.root = sortSpecification_AST;
            currentAST.child = sortSpecification_AST != null && sortSpecification_AST.getFirstChild() != null ? sortSpecification_AST.getFirstChild() : sortSpecification_AST;
            currentAST.advanceChildToEnd();
         }

         sortSpecification_AST = currentAST.root;
      } catch (RecognitionException ex) {
         if (this.inputState.guessing != 0) {
            throw ex;
         }

         this.reportError(ex);
         this.recover(ex, _tokenSet_1);
      }

      this.returnAST = sortSpecification_AST;
   }

   public final void sortKey() throws RecognitionException, TokenStreamException {
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST sortKey_AST = null;
      AST e_AST = null;
      this.trace("sortKey");

      try {
         this.expression();
         e_AST = this.returnAST;
         if (this.inputState.guessing == 0) {
            AST var6 = currentAST.root;
            sortKey_AST = this.astFactory.make((new ASTArray(2)).add(this.astFactory.create(7, "sort key")).add(e_AST));
            currentAST.root = sortKey_AST;
            currentAST.child = sortKey_AST != null && sortKey_AST.getFirstChild() != null ? sortKey_AST.getFirstChild() : sortKey_AST;
            currentAST.advanceChildToEnd();
         }
      } catch (RecognitionException ex) {
         if (this.inputState.guessing != 0) {
            throw ex;
         }

         this.reportError(ex);
         this.recover(ex, _tokenSet_2);
      }

      this.returnAST = sortKey_AST;
   }

   public final void collationSpecification() throws RecognitionException, TokenStreamException {
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST collationSpecification_AST = null;
      Token c = null;
      AST c_AST = null;
      AST cn_AST = null;
      this.trace("collationSpecification");

      try {
         c = this.LT(1);
         this.astFactory.create(c);
         this.match(12);
         this.collationName();
         cn_AST = this.returnAST;
         if (this.inputState.guessing == 0) {
            AST var8 = currentAST.root;
            collationSpecification_AST = this.astFactory.make((new ASTArray(1)).add(this.astFactory.create(12, this.extractText(cn_AST))));
            currentAST.root = collationSpecification_AST;
            currentAST.child = collationSpecification_AST != null && collationSpecification_AST.getFirstChild() != null ? collationSpecification_AST.getFirstChild() : collationSpecification_AST;
            currentAST.advanceChildToEnd();
         }
      } catch (RecognitionException ex) {
         if (this.inputState.guessing != 0) {
            throw ex;
         }

         this.reportError(ex);
         this.recover(ex, _tokenSet_3);
      }

      this.returnAST = collationSpecification_AST;
   }

   public final void orderingSpecification() throws RecognitionException, TokenStreamException {
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST orderingSpecification_AST = null;
      this.trace("orderingSpecification");

      try {
         switch (this.LA(1)) {
            case 13:
            case 25:
               switch (this.LA(1)) {
                  case 13:
                     this.match(13);
                     break;
                  case 25:
                     this.match(25);
                     break;
                  default:
                     throw new NoViableAltException(this.LT(1), this.getFilename());
               }

               if (this.inputState.guessing == 0) {
                  AST var6 = currentAST.root;
                  orderingSpecification_AST = this.astFactory.make((new ASTArray(1)).add(this.astFactory.create(6, "asc")));
                  currentAST.root = orderingSpecification_AST;
                  currentAST.child = orderingSpecification_AST != null && orderingSpecification_AST.getFirstChild() != null ? orderingSpecification_AST.getFirstChild() : orderingSpecification_AST;
                  currentAST.advanceChildToEnd();
               }
               break;
            case 14:
            case 26:
               switch (this.LA(1)) {
                  case 14:
                     this.match(14);
                     break;
                  case 26:
                     this.match(26);
                     break;
                  default:
                     throw new NoViableAltException(this.LT(1), this.getFilename());
               }

               if (this.inputState.guessing == 0) {
                  AST var5 = currentAST.root;
                  orderingSpecification_AST = this.astFactory.make((new ASTArray(1)).add(this.astFactory.create(6, "desc")));
                  currentAST.root = orderingSpecification_AST;
                  currentAST.child = orderingSpecification_AST != null && orderingSpecification_AST.getFirstChild() != null ? orderingSpecification_AST.getFirstChild() : orderingSpecification_AST;
                  currentAST.advanceChildToEnd();
               }
               break;
            default:
               throw new NoViableAltException(this.LT(1), this.getFilename());
         }
      } catch (RecognitionException ex) {
         if (this.inputState.guessing != 0) {
            throw ex;
         }

         this.reportError(ex);
         this.recover(ex, _tokenSet_1);
      }

      this.returnAST = orderingSpecification_AST;
   }

   public final void expression() throws RecognitionException, TokenStreamException {
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST expression_AST = null;
      Token qi = null;
      AST qi_AST = null;
      AST f_AST = null;
      AST p_AST = null;
      Token i = null;
      AST i_AST = null;
      this.trace("expression");

      try {
         if (this.LA(1) == 16) {
            AST tmp6_AST = null;
            tmp6_AST = this.astFactory.create(this.LT(1));
            this.match(16);
            qi = this.LT(1);
            qi_AST = this.astFactory.create(qi);
            this.match(17);
            AST tmp7_AST = null;
            tmp7_AST = this.astFactory.create(this.LT(1));
            this.match(16);
            if (this.inputState.guessing == 0) {
               AST var17 = currentAST.root;
               expression_AST = this.quotedIdentifier(qi_AST);
               currentAST.root = expression_AST;
               currentAST.child = expression_AST != null && expression_AST.getFirstChild() != null ? expression_AST.getFirstChild() : expression_AST;
               currentAST.advanceChildToEnd();
            }
         } else {
            boolean synPredMatched12 = false;
            if (this.LA(1) == 17 && (this.LA(2) == 9 || this.LA(2) == 18) && _tokenSet_4.member(this.LA(3))) {
               int _m12 = this.mark();
               synPredMatched12 = true;
               ++this.inputState.guessing;

               try {
                  this.match(17);

                  while(this.LA(1) == 9) {
                     this.match(9);
                     this.match(17);
                  }

                  this.match(18);
               } catch (RecognitionException var12) {
                  synPredMatched12 = false;
               }

               this.rewind(_m12);
               --this.inputState.guessing;
            }

            if (synPredMatched12) {
               this.functionCall();
               f_AST = this.returnAST;
               if (this.inputState.guessing == 0) {
                  expression_AST = currentAST.root;
                  expression_AST = f_AST;
                  currentAST.root = f_AST;
                  currentAST.child = f_AST != null && f_AST.getFirstChild() != null ? f_AST.getFirstChild() : f_AST;
                  currentAST.advanceChildToEnd();
               }
            } else if (this.LA(1) == 17 && this.LA(2) == 9 && this.LA(3) == 17) {
               this.simplePropertyPath();
               p_AST = this.returnAST;
               if (this.inputState.guessing == 0) {
                  AST var15 = currentAST.root;
                  expression_AST = this.resolveIdent(p_AST);
                  currentAST.root = expression_AST;
                  currentAST.child = expression_AST != null && expression_AST.getFirstChild() != null ? expression_AST.getFirstChild() : expression_AST;
                  currentAST.advanceChildToEnd();
               }
            } else {
               if (this.LA(1) != 17 || !_tokenSet_5.member(this.LA(2))) {
                  throw new NoViableAltException(this.LT(1), this.getFilename());
               }

               i = this.LT(1);
               i_AST = this.astFactory.create(i);
               this.match(17);
               if (this.inputState.guessing == 0) {
                  expression_AST = currentAST.root;
                  if (this.isFunctionName(i_AST)) {
                     expression_AST = this.resolveFunction(i_AST);
                  } else {
                     expression_AST = this.resolveIdent(i_AST);
                  }

                  currentAST.root = expression_AST;
                  currentAST.child = expression_AST != null && expression_AST.getFirstChild() != null ? expression_AST.getFirstChild() : expression_AST;
                  currentAST.advanceChildToEnd();
               }
            }
         }
      } catch (RecognitionException ex) {
         if (this.inputState.guessing != 0) {
            throw ex;
         }

         this.reportError(ex);
         this.recover(ex, _tokenSet_5);
      }

      this.returnAST = expression_AST;
   }

   public final void functionCall() throws RecognitionException, TokenStreamException {
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST functionCall_AST = null;
      AST fn_AST = null;
      AST pl_AST = null;
      this.trace("functionCall");

      try {
         this.functionName();
         fn_AST = this.returnAST;
         AST tmp8_AST = null;
         tmp8_AST = this.astFactory.create(this.LT(1));
         this.match(18);
         this.functionParameterList();
         pl_AST = this.returnAST;
         AST tmp9_AST = null;
         tmp9_AST = this.astFactory.create(this.LT(1));
         this.match(19);
         if (this.inputState.guessing == 0) {
            AST var8 = currentAST.root;
            AST var9 = this.astFactory.make((new ASTArray(2)).add(this.astFactory.create(17, this.extractText(fn_AST))).add(pl_AST));
            functionCall_AST = this.resolveFunction(var9);
            currentAST.root = functionCall_AST;
            currentAST.child = functionCall_AST != null && functionCall_AST.getFirstChild() != null ? functionCall_AST.getFirstChild() : functionCall_AST;
            currentAST.advanceChildToEnd();
         }
      } catch (RecognitionException ex) {
         if (this.inputState.guessing != 0) {
            throw ex;
         }

         this.reportError(ex);
         this.recover(ex, _tokenSet_5);
      }

      this.returnAST = functionCall_AST;
   }

   public final void simplePropertyPath() throws RecognitionException, TokenStreamException {
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST simplePropertyPath_AST = null;
      Token i = null;
      AST i_AST = null;
      Token i2 = null;
      AST i2_AST = null;
      this.trace("simplePropertyPath");
      StringBuilder buffer = new StringBuilder();

      try {
         i = this.LT(1);
         i_AST = this.astFactory.create(i);
         this.astFactory.addASTChild(currentAST, i_AST);
         this.match(17);
         if (this.inputState.guessing == 0) {
            buffer.append(i.getText());
         }

         int _cnt31;
         for(_cnt31 = 0; this.LA(1) == 9; ++_cnt31) {
            AST tmp10_AST = null;
            tmp10_AST = this.astFactory.create(this.LT(1));
            this.astFactory.addASTChild(currentAST, tmp10_AST);
            this.match(9);
            i2 = this.LT(1);
            i2_AST = this.astFactory.create(i2);
            this.astFactory.addASTChild(currentAST, i2_AST);
            this.match(17);
            if (this.inputState.guessing == 0) {
               buffer.append('.').append(i2.getText());
            }
         }

         if (_cnt31 < 1) {
            throw new NoViableAltException(this.LT(1), this.getFilename());
         }

         if (this.inputState.guessing == 0) {
            simplePropertyPath_AST = currentAST.root;
            simplePropertyPath_AST = this.astFactory.make((new ASTArray(1)).add(this.astFactory.create(17, buffer.toString())));
            currentAST.root = simplePropertyPath_AST;
            currentAST.child = simplePropertyPath_AST != null && simplePropertyPath_AST.getFirstChild() != null ? simplePropertyPath_AST.getFirstChild() : simplePropertyPath_AST;
            currentAST.advanceChildToEnd();
         }

         simplePropertyPath_AST = currentAST.root;
      } catch (RecognitionException ex) {
         if (this.inputState.guessing != 0) {
            throw ex;
         }

         this.reportError(ex);
         this.recover(ex, _tokenSet_5);
      }

      this.returnAST = simplePropertyPath_AST;
   }

   public final void functionCallCheck() throws RecognitionException, TokenStreamException {
      this.returnAST = null;
      new ASTPair();
      AST functionCallCheck_AST = null;
      this.trace("functionCallCheck");

      try {
         AST tmp11_AST = null;
         tmp11_AST = this.astFactory.create(this.LT(1));
         this.match(17);

         while(this.LA(1) == 9) {
            AST tmp12_AST = null;
            tmp12_AST = this.astFactory.create(this.LT(1));
            this.match(9);
            AST tmp13_AST = null;
            tmp13_AST = this.astFactory.create(this.LT(1));
            this.match(17);
         }

         AST tmp14_AST = null;
         tmp14_AST = this.astFactory.create(this.LT(1));
         this.match(18);
      } catch (RecognitionException ex) {
         if (this.inputState.guessing != 0) {
            throw ex;
         }

         this.reportError(ex);
         this.recover(ex, _tokenSet_0);
      }

      this.returnAST = functionCallCheck_AST;
   }

   public final void functionName() throws RecognitionException, TokenStreamException {
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST functionName_AST = null;
      Token i = null;
      AST i_AST = null;
      Token i2 = null;
      AST i2_AST = null;
      this.trace("functionName");
      StringBuilder buffer = new StringBuilder();

      try {
         i = this.LT(1);
         i_AST = this.astFactory.create(i);
         this.astFactory.addASTChild(currentAST, i_AST);
         this.match(17);
         if (this.inputState.guessing == 0) {
            buffer.append(i.getText());
         }

         while(this.LA(1) == 9) {
            AST tmp15_AST = null;
            tmp15_AST = this.astFactory.create(this.LT(1));
            this.astFactory.addASTChild(currentAST, tmp15_AST);
            this.match(9);
            i2 = this.LT(1);
            i2_AST = this.astFactory.create(i2);
            this.astFactory.addASTChild(currentAST, i2_AST);
            this.match(17);
            if (this.inputState.guessing == 0) {
               buffer.append('.').append(i2.getText());
            }
         }

         if (this.inputState.guessing == 0) {
            functionName_AST = currentAST.root;
            functionName_AST = this.astFactory.make((new ASTArray(1)).add(this.astFactory.create(17, buffer.toString())));
            currentAST.root = functionName_AST;
            currentAST.child = functionName_AST != null && functionName_AST.getFirstChild() != null ? functionName_AST.getFirstChild() : functionName_AST;
            currentAST.advanceChildToEnd();
         }

         functionName_AST = currentAST.root;
      } catch (RecognitionException ex) {
         if (this.inputState.guessing != 0) {
            throw ex;
         }

         this.reportError(ex);
         this.recover(ex, _tokenSet_6);
      }

      this.returnAST = functionName_AST;
   }

   public final void functionParameterList() throws RecognitionException, TokenStreamException {
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST functionParameterList_AST = null;
      this.trace("functionParameterList");

      try {
         this.functionParameter();
         this.astFactory.addASTChild(currentAST, this.returnAST);

         while(this.LA(1) == 15) {
            this.match(15);
            this.functionParameter();
            this.astFactory.addASTChild(currentAST, this.returnAST);
         }

         if (this.inputState.guessing == 0) {
            functionParameterList_AST = currentAST.root;
            functionParameterList_AST = this.astFactory.make((new ASTArray(2)).add(this.astFactory.create(8, "{param list}")).add(functionParameterList_AST));
            currentAST.root = functionParameterList_AST;
            currentAST.child = functionParameterList_AST != null && functionParameterList_AST.getFirstChild() != null ? functionParameterList_AST.getFirstChild() : functionParameterList_AST;
            currentAST.advanceChildToEnd();
         }

         functionParameterList_AST = currentAST.root;
      } catch (RecognitionException ex) {
         if (this.inputState.guessing != 0) {
            throw ex;
         }

         this.reportError(ex);
         this.recover(ex, _tokenSet_7);
      }

      this.returnAST = functionParameterList_AST;
   }

   public final void functionParameter() throws RecognitionException, TokenStreamException {
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST functionParameter_AST = null;
      this.trace("functionParameter");

      try {
         label48: {
            switch (this.LA(1)) {
               case 16:
               case 17:
                  this.expression();
                  this.astFactory.addASTChild(currentAST, this.returnAST);
                  functionParameter_AST = currentAST.root;
                  break label48;
               case 18:
               case 19:
               default:
                  throw new NoViableAltException(this.LT(1), this.getFilename());
               case 20:
                  AST tmp17_AST = null;
                  tmp17_AST = this.astFactory.create(this.LT(1));
                  this.astFactory.addASTChild(currentAST, tmp17_AST);
                  this.match(20);
                  functionParameter_AST = currentAST.root;
                  break label48;
               case 21:
                  AST tmp18_AST = null;
                  tmp18_AST = this.astFactory.create(this.LT(1));
                  this.astFactory.addASTChild(currentAST, tmp18_AST);
                  this.match(21);
                  functionParameter_AST = currentAST.root;
                  break label48;
               case 22:
                  AST tmp19_AST = null;
                  tmp19_AST = this.astFactory.create(this.LT(1));
                  this.astFactory.addASTChild(currentAST, tmp19_AST);
                  this.match(22);
                  functionParameter_AST = currentAST.root;
                  break label48;
               case 23:
                  AST tmp20_AST = null;
                  tmp20_AST = this.astFactory.create(this.LT(1));
                  this.astFactory.addASTChild(currentAST, tmp20_AST);
                  this.match(23);
                  functionParameter_AST = currentAST.root;
                  break label48;
               case 24:
            }

            AST tmp21_AST = null;
            tmp21_AST = this.astFactory.create(this.LT(1));
            this.astFactory.addASTChild(currentAST, tmp21_AST);
            this.match(24);
            if (this.inputState.guessing == 0) {
               functionParameter_AST = currentAST.root;
               functionParameter_AST = this.quotedString(functionParameter_AST);
               currentAST.root = functionParameter_AST;
               currentAST.child = functionParameter_AST != null && functionParameter_AST.getFirstChild() != null ? functionParameter_AST.getFirstChild() : functionParameter_AST;
               currentAST.advanceChildToEnd();
            }

            functionParameter_AST = currentAST.root;
         }
      } catch (RecognitionException ex) {
         if (this.inputState.guessing != 0) {
            throw ex;
         }

         this.reportError(ex);
         this.recover(ex, _tokenSet_8);
      }

      this.returnAST = functionParameter_AST;
   }

   public final void collationName() throws RecognitionException, TokenStreamException {
      this.returnAST = null;
      ASTPair currentAST = new ASTPair();
      AST collationName_AST = null;
      this.trace("collationSpecification");

      try {
         AST tmp22_AST = null;
         tmp22_AST = this.astFactory.create(this.LT(1));
         this.astFactory.addASTChild(currentAST, tmp22_AST);
         this.match(17);
         collationName_AST = currentAST.root;
      } catch (RecognitionException ex) {
         if (this.inputState.guessing != 0) {
            throw ex;
         }

         this.reportError(ex);
         this.recover(ex, _tokenSet_3);
      }

      this.returnAST = collationName_AST;
   }

   protected void buildTokenTypeASTClassMap() {
      this.tokenTypeToASTClassMap = null;
   }

   private static final long[] mk_tokenSet_0() {
      long[] data = new long[]{2L, 0L};
      return data;
   }

   private static final long[] mk_tokenSet_1() {
      long[] data = new long[]{32770L, 0L};
      return data;
   }

   private static final long[] mk_tokenSet_2() {
      long[] data = new long[]{100724738L, 0L};
      return data;
   }

   private static final long[] mk_tokenSet_3() {
      long[] data = new long[]{100720642L, 0L};
      return data;
   }

   private static final long[] mk_tokenSet_4() {
      long[] data = new long[]{32702464L, 0L};
      return data;
   }

   private static final long[] mk_tokenSet_5() {
      long[] data = new long[]{101249026L, 0L};
      return data;
   }

   private static final long[] mk_tokenSet_6() {
      long[] data = new long[]{262144L, 0L};
      return data;
   }

   private static final long[] mk_tokenSet_7() {
      long[] data = new long[]{524288L, 0L};
      return data;
   }

   private static final long[] mk_tokenSet_8() {
      long[] data = new long[]{557056L, 0L};
      return data;
   }
}
