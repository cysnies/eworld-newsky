package antlr;

import antlr.collections.impl.BitSet;

public class ANTLRParser extends LLkParser implements ANTLRTokenTypes {
   private static final boolean DEBUG_PARSER = false;
   ANTLRGrammarParseBehavior behavior;
   Tool antlrTool;
   protected int blockNesting;
   public static final String[] _tokenNames = new String[]{"<0>", "EOF", "<2>", "NULL_TREE_LOOKAHEAD", "\"tokens\"", "\"header\"", "STRING_LITERAL", "ACTION", "DOC_COMMENT", "\"lexclass\"", "\"class\"", "\"extends\"", "\"Lexer\"", "\"TreeParser\"", "OPTIONS", "ASSIGN", "SEMI", "RCURLY", "\"charVocabulary\"", "CHAR_LITERAL", "INT", "OR", "RANGE", "TOKENS", "TOKEN_REF", "OPEN_ELEMENT_OPTION", "CLOSE_ELEMENT_OPTION", "LPAREN", "RPAREN", "\"Parser\"", "\"protected\"", "\"public\"", "\"private\"", "BANG", "ARG_ACTION", "\"returns\"", "COLON", "\"throws\"", "COMMA", "\"exception\"", "\"catch\"", "RULE_REF", "NOT_OP", "SEMPRED", "TREE_BEGIN", "QUESTION", "STAR", "PLUS", "IMPLIES", "CARET", "WILDCARD", "\"options\"", "WS", "COMMENT", "SL_COMMENT", "ML_COMMENT", "ESC", "DIGIT", "XDIGIT", "NESTED_ARG_ACTION", "NESTED_ACTION", "WS_LOOP", "INTERNAL_RULE_REF", "WS_OPT"};
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

   public ANTLRParser(TokenBuffer var1, ANTLRGrammarParseBehavior var2, Tool var3) {
      super((TokenBuffer)var1, 1);
      this.blockNesting = -1;
      this.tokenNames = _tokenNames;
      this.behavior = var2;
      this.antlrTool = var3;
   }

   public void reportError(String var1) {
      this.antlrTool.error(var1, this.getFilename(), -1, -1);
   }

   public void reportError(RecognitionException var1) {
      this.reportError(var1, var1.getErrorMessage());
   }

   public void reportError(RecognitionException var1, String var2) {
      this.antlrTool.error(var2, var1.getFilename(), var1.getLine(), var1.getColumn());
   }

   public void reportWarning(String var1) {
      this.antlrTool.warning((String)var1, this.getFilename(), -1, -1);
   }

   private boolean lastInRule() throws TokenStreamException {
      return this.blockNesting == 0 && (this.LA(1) == 16 || this.LA(1) == 39 || this.LA(1) == 21);
   }

   private void checkForMissingEndRule(Token var1) {
      if (var1.getColumn() == 1) {
         this.antlrTool.warning("did you forget to terminate previous rule?", this.getFilename(), var1.getLine(), var1.getColumn());
      }

   }

   protected ANTLRParser(TokenBuffer var1, int var2) {
      super(var1, var2);
      this.blockNesting = -1;
      this.tokenNames = _tokenNames;
   }

   public ANTLRParser(TokenBuffer var1) {
      this((TokenBuffer)var1, 2);
   }

   protected ANTLRParser(TokenStream var1, int var2) {
      super(var1, var2);
      this.blockNesting = -1;
      this.tokenNames = _tokenNames;
   }

   public ANTLRParser(TokenStream var1) {
      this((TokenStream)var1, 2);
   }

   public ANTLRParser(ParserSharedInputState var1) {
      super((ParserSharedInputState)var1, 2);
      this.blockNesting = -1;
      this.tokenNames = _tokenNames;
   }

   public final void grammar() throws RecognitionException, TokenStreamException {
      Token var1 = null;
      Object var2 = null;

      try {
         while(this.LA(1) == 5) {
            if (this.inputState.guessing == 0) {
               var1 = null;
            }

            this.match(5);
            switch (this.LA(1)) {
               case 6:
                  var1 = this.LT(1);
                  this.match(6);
               case 7:
                  Token var5 = this.LT(1);
                  this.match(7);
                  if (this.inputState.guessing == 0) {
                     this.behavior.refHeaderAction(var1, var5);
                  }
                  break;
               default:
                  throw new NoViableAltException(this.LT(1), this.getFilename());
            }
         }

         switch (this.LA(1)) {
            case 1:
            case 7:
            case 8:
            case 9:
            case 10:
               break;
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 11:
            case 12:
            case 13:
            default:
               throw new NoViableAltException(this.LT(1), this.getFilename());
            case 14:
               this.fileOptionsSpec();
         }

         while(this.LA(1) >= 7 && this.LA(1) <= 10) {
            this.classDef();
         }

         this.match(1);
      } catch (RecognitionException var4) {
         if (this.inputState.guessing != 0) {
            throw var4;
         }

         this.reportError(var4, "rule grammar trapped:\n" + var4.toString());
         this.consumeUntil(1);
      }

   }

   public final void fileOptionsSpec() throws RecognitionException, TokenStreamException {
      this.match(14);

      for(; this.LA(1) == 24 || this.LA(1) == 41; this.match(16)) {
         Token var1 = this.id();
         this.match(15);
         Token var2 = this.optionValue();
         if (this.inputState.guessing == 0) {
            this.behavior.setFileOption(var1, var2, this.getInputState().filename);
         }
      }

      this.match(17);
   }

   public final void classDef() throws RecognitionException, TokenStreamException {
      Object var1 = null;
      Token var2 = null;
      String var3 = null;

      try {
         label90:
         switch (this.LA(1)) {
            case 7:
               Token var11 = this.LT(1);
               this.match(7);
               if (this.inputState.guessing == 0) {
                  this.behavior.refPreambleAction(var11);
               }
            case 8:
            case 9:
            case 10:
               switch (this.LA(1)) {
                  case 8:
                     var2 = this.LT(1);
                     this.match(8);
                     if (this.inputState.guessing == 0) {
                        var3 = var2.getText();
                     }
                  case 9:
                  case 10:
                     break label90;
                  default:
                     throw new NoViableAltException(this.LT(1), this.getFilename());
               }
            default:
               throw new NoViableAltException(this.LT(1), this.getFilename());
         }

         boolean var4 = false;
         if ((this.LA(1) == 9 || this.LA(1) == 10) && (this.LA(2) == 24 || this.LA(2) == 41)) {
            int var14 = this.mark();
            var4 = true;
            ++this.inputState.guessing;

            try {
               switch (this.LA(1)) {
                  case 9:
                     this.match(9);
                     break;
                  case 10:
                     this.match(10);
                     this.id();
                     this.match(11);
                     this.match(12);
                     break;
                  default:
                     throw new NoViableAltException(this.LT(1), this.getFilename());
               }
            } catch (RecognitionException var9) {
               var4 = false;
            }

            this.rewind(var14);
            --this.inputState.guessing;
         }

         if (var4) {
            this.lexerSpec(var3);
         } else {
            boolean var15 = false;
            if (this.LA(1) == 10 && (this.LA(2) == 24 || this.LA(2) == 41)) {
               int var6 = this.mark();
               var15 = true;
               ++this.inputState.guessing;

               try {
                  this.match(10);
                  this.id();
                  this.match(11);
                  this.match(13);
               } catch (RecognitionException var8) {
                  var15 = false;
               }

               this.rewind(var6);
               --this.inputState.guessing;
            }

            if (var15) {
               this.treeParserSpec(var3);
            } else {
               if (this.LA(1) != 10 || this.LA(2) != 24 && this.LA(2) != 41) {
                  throw new NoViableAltException(this.LT(1), this.getFilename());
               }

               this.parserSpec(var3);
            }
         }

         this.rules();
         if (this.inputState.guessing == 0) {
            this.behavior.endGrammar();
         }
      } catch (RecognitionException var10) {
         if (this.inputState.guessing != 0) {
            throw var10;
         }

         if (var10 instanceof NoViableAltException) {
            NoViableAltException var5 = (NoViableAltException)var10;
            if (var5.token.getType() == 8) {
               this.reportError(var10, "JAVADOC comments may only prefix rules and grammars");
            } else {
               this.reportError(var10, "rule classDef trapped:\n" + var10.toString());
            }
         } else {
            this.reportError(var10, "rule classDef trapped:\n" + var10.toString());
         }

         this.behavior.abortGrammar();
         boolean var13 = true;

         while(var13) {
            this.consume();
            switch (this.LA(1)) {
               case 1:
               case 9:
               case 10:
                  var13 = false;
            }
         }
      }

   }

   public final Token id() throws RecognitionException, TokenStreamException {
      Object var2 = null;
      Object var3 = null;
      Token var1 = null;
      switch (this.LA(1)) {
         case 24:
            Token var4 = this.LT(1);
            this.match(24);
            if (this.inputState.guessing == 0) {
               var1 = var4;
            }
            break;
         case 41:
            Token var5 = this.LT(1);
            this.match(41);
            if (this.inputState.guessing == 0) {
               var1 = var5;
            }
            break;
         default:
            throw new NoViableAltException(this.LT(1), this.getFilename());
      }

      return var1;
   }

   public final void lexerSpec(String var1) throws RecognitionException, TokenStreamException {
      Token var4;
      String var5;
      Object var2 = null;
      Object var3 = null;
      var5 = null;
      label42:
      switch (this.LA(1)) {
         case 9:
            Token var6 = this.LT(1);
            this.match(9);
            var4 = this.id();
            if (this.inputState.guessing == 0) {
               this.antlrTool.warning("lexclass' is deprecated; use 'class X extends Lexer'", this.getFilename(), var6.getLine(), var6.getColumn());
            }
            break;
         case 10:
            this.match(10);
            var4 = this.id();
            this.match(11);
            this.match(12);
            switch (this.LA(1)) {
               case 16:
                  break label42;
               case 27:
                  var5 = this.superClass();
                  break label42;
               default:
                  throw new NoViableAltException(this.LT(1), this.getFilename());
            }
         default:
            throw new NoViableAltException(this.LT(1), this.getFilename());
      }

      if (this.inputState.guessing == 0) {
         this.behavior.startLexer(this.getFilename(), var4, var5, var1);
      }

      this.match(16);
      switch (this.LA(1)) {
         case 9:
         case 10:
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
         case 22:
         case 25:
         case 26:
         case 27:
         case 28:
         case 29:
         case 33:
         case 34:
         case 35:
         case 36:
         case 37:
         case 38:
         case 39:
         case 40:
         default:
            throw new NoViableAltException(this.LT(1), this.getFilename());
         case 14:
            this.lexerOptionsSpec();
         case 7:
         case 8:
         case 23:
         case 24:
         case 30:
         case 31:
         case 32:
         case 41:
            if (this.inputState.guessing == 0) {
               this.behavior.endOptions();
            }

            switch (this.LA(1)) {
               case 23:
                  this.tokensSpec();
               case 7:
               case 8:
               case 24:
               case 30:
               case 31:
               case 32:
               case 41:
                  switch (this.LA(1)) {
                     case 7:
                        Token var7 = this.LT(1);
                        this.match(7);
                        if (this.inputState.guessing == 0) {
                           this.behavior.refMemberAction(var7);
                        }
                     case 8:
                     case 24:
                     case 30:
                     case 31:
                     case 32:
                     case 41:
                        return;
                     default:
                        throw new NoViableAltException(this.LT(1), this.getFilename());
                  }
               default:
                  throw new NoViableAltException(this.LT(1), this.getFilename());
            }
      }
   }

   public final void treeParserSpec(String var1) throws RecognitionException, TokenStreamException {
      Object var2 = null;
      String var4 = null;
      this.match(10);
      Token var3 = this.id();
      this.match(11);
      this.match(13);
      switch (this.LA(1)) {
         case 27:
            var4 = this.superClass();
         case 16:
            if (this.inputState.guessing == 0) {
               this.behavior.startTreeWalker(this.getFilename(), var3, var4, var1);
            }

            this.match(16);
            switch (this.LA(1)) {
               case 9:
               case 10:
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
               case 22:
               case 25:
               case 26:
               case 27:
               case 28:
               case 29:
               case 33:
               case 34:
               case 35:
               case 36:
               case 37:
               case 38:
               case 39:
               case 40:
               default:
                  throw new NoViableAltException(this.LT(1), this.getFilename());
               case 14:
                  this.treeParserOptionsSpec();
               case 7:
               case 8:
               case 23:
               case 24:
               case 30:
               case 31:
               case 32:
               case 41:
                  if (this.inputState.guessing == 0) {
                     this.behavior.endOptions();
                  }

                  switch (this.LA(1)) {
                     case 23:
                        this.tokensSpec();
                     case 7:
                     case 8:
                     case 24:
                     case 30:
                     case 31:
                     case 32:
                     case 41:
                        switch (this.LA(1)) {
                           case 7:
                              Token var5 = this.LT(1);
                              this.match(7);
                              if (this.inputState.guessing == 0) {
                                 this.behavior.refMemberAction(var5);
                              }
                           case 8:
                           case 24:
                           case 30:
                           case 31:
                           case 32:
                           case 41:
                              return;
                           default:
                              throw new NoViableAltException(this.LT(1), this.getFilename());
                        }
                     default:
                        throw new NoViableAltException(this.LT(1), this.getFilename());
                  }
            }
         default:
            throw new NoViableAltException(this.LT(1), this.getFilename());
      }
   }

   public final void parserSpec(String var1) throws RecognitionException, TokenStreamException {
      Token var3;
      String var4;
      Object var2 = null;
      var4 = null;
      this.match(10);
      var3 = this.id();
      label42:
      switch (this.LA(1)) {
         case 11:
            this.match(11);
            this.match(29);
            switch (this.LA(1)) {
               case 16:
                  break label42;
               case 27:
                  var4 = this.superClass();
                  break label42;
               default:
                  throw new NoViableAltException(this.LT(1), this.getFilename());
            }
         case 16:
            if (this.inputState.guessing == 0) {
               this.antlrTool.warning("use 'class X extends Parser'", this.getFilename(), var3.getLine(), var3.getColumn());
            }
            break;
         default:
            throw new NoViableAltException(this.LT(1), this.getFilename());
      }

      if (this.inputState.guessing == 0) {
         this.behavior.startParser(this.getFilename(), var3, var4, var1);
      }

      this.match(16);
      switch (this.LA(1)) {
         case 9:
         case 10:
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
         case 22:
         case 25:
         case 26:
         case 27:
         case 28:
         case 29:
         case 33:
         case 34:
         case 35:
         case 36:
         case 37:
         case 38:
         case 39:
         case 40:
         default:
            throw new NoViableAltException(this.LT(1), this.getFilename());
         case 14:
            this.parserOptionsSpec();
         case 7:
         case 8:
         case 23:
         case 24:
         case 30:
         case 31:
         case 32:
         case 41:
            if (this.inputState.guessing == 0) {
               this.behavior.endOptions();
            }

            switch (this.LA(1)) {
               case 23:
                  this.tokensSpec();
               case 7:
               case 8:
               case 24:
               case 30:
               case 31:
               case 32:
               case 41:
                  switch (this.LA(1)) {
                     case 7:
                        Token var5 = this.LT(1);
                        this.match(7);
                        if (this.inputState.guessing == 0) {
                           this.behavior.refMemberAction(var5);
                        }
                     case 8:
                     case 24:
                     case 30:
                     case 31:
                     case 32:
                     case 41:
                        return;
                     default:
                        throw new NoViableAltException(this.LT(1), this.getFilename());
                  }
               default:
                  throw new NoViableAltException(this.LT(1), this.getFilename());
            }
      }
   }

   public final void rules() throws RecognitionException, TokenStreamException {
      int var1;
      for(var1 = 0; _tokenSet_0.member(this.LA(1)) && _tokenSet_1.member(this.LA(2)); ++var1) {
         this.rule();
      }

      if (var1 < 1) {
         throw new NoViableAltException(this.LT(1), this.getFilename());
      }
   }

   public final Token optionValue() throws RecognitionException, TokenStreamException {
      Object var2 = null;
      Object var3 = null;
      Object var4 = null;
      Token var1 = null;
      switch (this.LA(1)) {
         case 6:
            Token var5 = this.LT(1);
            this.match(6);
            if (this.inputState.guessing == 0) {
               var1 = var5;
            }
            break;
         case 19:
            Token var6 = this.LT(1);
            this.match(19);
            if (this.inputState.guessing == 0) {
               var1 = var6;
            }
            break;
         case 20:
            Token var7 = this.LT(1);
            this.match(20);
            if (this.inputState.guessing == 0) {
               var1 = var7;
            }
            break;
         case 24:
         case 41:
            var1 = this.qualifiedID();
            break;
         default:
            throw new NoViableAltException(this.LT(1), this.getFilename());
      }

      return var1;
   }

   public final void parserOptionsSpec() throws RecognitionException, TokenStreamException {
      this.match(14);

      for(; this.LA(1) == 24 || this.LA(1) == 41; this.match(16)) {
         Token var1 = this.id();
         this.match(15);
         Token var2 = this.optionValue();
         if (this.inputState.guessing == 0) {
            this.behavior.setGrammarOption(var1, var2);
         }
      }

      this.match(17);
   }

   public final void treeParserOptionsSpec() throws RecognitionException, TokenStreamException {
      this.match(14);

      for(; this.LA(1) == 24 || this.LA(1) == 41; this.match(16)) {
         Token var1 = this.id();
         this.match(15);
         Token var2 = this.optionValue();
         if (this.inputState.guessing == 0) {
            this.behavior.setGrammarOption(var1, var2);
         }
      }

      this.match(17);
   }

   public final void lexerOptionsSpec() throws RecognitionException, TokenStreamException {
      this.match(14);

      while(true) {
         switch (this.LA(1)) {
            case 18:
               this.match(18);
               this.match(15);
               BitSet var3 = this.charSet();
               this.match(16);
               if (this.inputState.guessing == 0) {
                  this.behavior.setCharVocabulary(var3);
               }
               break;
            case 24:
            case 41:
               Token var1 = this.id();
               this.match(15);
               Token var2 = this.optionValue();
               if (this.inputState.guessing == 0) {
                  this.behavior.setGrammarOption(var1, var2);
               }

               this.match(16);
               break;
            default:
               this.match(17);
               return;
         }
      }
   }

   public final BitSet charSet() throws RecognitionException, TokenStreamException {
      BitSet var1 = null;
      Object var2 = null;
      var1 = this.setBlockElement();

      while(this.LA(1) == 21) {
         this.match(21);
         BitSet var4 = this.setBlockElement();
         if (this.inputState.guessing == 0) {
            var1.orInPlace(var4);
         }
      }

      return var1;
   }

   public final void subruleOptionsSpec() throws RecognitionException, TokenStreamException {
      this.match(14);

      for(; this.LA(1) == 24 || this.LA(1) == 41; this.match(16)) {
         Token var1 = this.id();
         this.match(15);
         Token var2 = this.optionValue();
         if (this.inputState.guessing == 0) {
            this.behavior.setSubruleOption(var1, var2);
         }
      }

      this.match(17);
   }

   public final Token qualifiedID() throws RecognitionException, TokenStreamException {
      CommonToken var1 = null;
      StringBuffer var2 = new StringBuffer(30);
      Token var3 = this.id();
      if (this.inputState.guessing == 0) {
         var2.append(var3.getText());
      }

      while(this.LA(1) == 50) {
         this.match(50);
         var3 = this.id();
         if (this.inputState.guessing == 0) {
            var2.append('.');
            var2.append(var3.getText());
         }
      }

      if (this.inputState.guessing == 0) {
         var1 = new CommonToken(24, var2.toString());
         ((Token)var1).setLine(var3.getLine());
      }

      return var1;
   }

   public final BitSet setBlockElement() throws RecognitionException, TokenStreamException {
      Object var2 = null;
      Object var3 = null;
      BitSet var1 = null;
      int var4 = 0;
      Token var7 = this.LT(1);
      this.match(19);
      if (this.inputState.guessing == 0) {
         var4 = ANTLRLexer.tokenTypeForCharLiteral(var7.getText());
         var1 = BitSet.of(var4);
      }

      switch (this.LA(1)) {
         case 22:
            this.match(22);
            Token var8 = this.LT(1);
            this.match(19);
            if (this.inputState.guessing == 0) {
               int var5 = ANTLRLexer.tokenTypeForCharLiteral(var8.getText());
               if (var5 < var4) {
                  this.antlrTool.error("Malformed range line ", this.getFilename(), var7.getLine(), var7.getColumn());
               }

               for(int var6 = var4 + 1; var6 <= var5; ++var6) {
                  var1.add(var6);
               }
            }
         case 16:
         case 21:
            return var1;
         default:
            throw new NoViableAltException(this.LT(1), this.getFilename());
      }
   }

   public final void tokensSpec() throws RecognitionException, TokenStreamException {
      Object var1 = null;
      Token var2 = null;
      Object var3 = null;
      this.match(23);

      int var4;
      for(var4 = 0; this.LA(1) == 6 || this.LA(1) == 24; ++var4) {
         label50:
         switch (this.LA(1)) {
            case 6:
               Token var6 = this.LT(1);
               this.match(6);
               if (this.inputState.guessing == 0) {
                  this.behavior.defineToken((Token)null, var6);
               }

               switch (this.LA(1)) {
                  case 16:
                     break label50;
                  case 25:
                     this.tokensSpecOptions(var6);
                     break label50;
                  default:
                     throw new NoViableAltException(this.LT(1), this.getFilename());
               }
            case 24:
               if (this.inputState.guessing == 0) {
                  var2 = null;
               }

               Token var5 = this.LT(1);
               this.match(24);
               switch (this.LA(1)) {
                  case 15:
                     this.match(15);
                     var2 = this.LT(1);
                     this.match(6);
                  case 16:
                  case 25:
                     if (this.inputState.guessing == 0) {
                        this.behavior.defineToken(var5, var2);
                     }

                     switch (this.LA(1)) {
                        case 16:
                           break label50;
                        case 25:
                           this.tokensSpecOptions(var5);
                           break label50;
                        default:
                           throw new NoViableAltException(this.LT(1), this.getFilename());
                     }
                  default:
                     throw new NoViableAltException(this.LT(1), this.getFilename());
               }
            default:
               throw new NoViableAltException(this.LT(1), this.getFilename());
         }

         this.match(16);
      }

      if (var4 >= 1) {
         this.match(17);
      } else {
         throw new NoViableAltException(this.LT(1), this.getFilename());
      }
   }

   public final void tokensSpecOptions(Token var1) throws RecognitionException, TokenStreamException {
      Object var2 = null;
      Object var3 = null;
      this.match(25);
      Token var4 = this.id();
      this.match(15);
      Token var6 = this.optionValue();
      if (this.inputState.guessing == 0) {
         this.behavior.refTokensSpecElementOption(var1, var4, var6);
      }

      while(this.LA(1) == 16) {
         this.match(16);
         var4 = this.id();
         this.match(15);
         var6 = this.optionValue();
         if (this.inputState.guessing == 0) {
            this.behavior.refTokensSpecElementOption(var1, var4, var6);
         }
      }

      this.match(26);
   }

   public final String superClass() throws RecognitionException, TokenStreamException {
      String var1 = null;
      this.match(27);
      if (this.inputState.guessing == 0) {
         var1 = this.LT(1).getText();
         var1 = StringUtils.stripFrontBack(var1, "\"", "\"");
      }

      this.match(6);
      this.match(28);
      return var1;
   }

   public final void rule() throws RecognitionException, TokenStreamException {
      Token var1 = null;
      Token var2 = null;
      Token var3 = null;
      Token var4 = null;
      Object var5 = null;
      Object var6 = null;
      Object var7 = null;
      String var8 = "public";
      String var10 = null;
      boolean var11 = true;
      this.blockNesting = -1;
      switch (this.LA(1)) {
         case 8:
            var1 = this.LT(1);
            this.match(8);
            if (this.inputState.guessing == 0) {
               var10 = var1.getText();
            }
         case 24:
         case 30:
         case 31:
         case 32:
         case 41:
            switch (this.LA(1)) {
               case 24:
               case 41:
                  break;
               case 30:
                  var2 = this.LT(1);
                  this.match(30);
                  if (this.inputState.guessing == 0) {
                     var8 = var2.getText();
                  }
                  break;
               case 31:
                  var3 = this.LT(1);
                  this.match(31);
                  if (this.inputState.guessing == 0) {
                     var8 = var3.getText();
                  }
                  break;
               case 32:
                  var4 = this.LT(1);
                  this.match(32);
                  if (this.inputState.guessing == 0) {
                     var8 = var4.getText();
                  }
                  break;
               default:
                  throw new NoViableAltException(this.LT(1), this.getFilename());
            }

            Token var9 = this.id();
            switch (this.LA(1)) {
               case 33:
                  this.match(33);
                  if (this.inputState.guessing == 0) {
                     var11 = false;
                  }
               case 7:
               case 14:
               case 34:
               case 35:
               case 36:
               case 37:
                  if (this.inputState.guessing == 0) {
                     this.behavior.defineRuleName(var9, var8, var11, var10);
                  }

                  switch (this.LA(1)) {
                     case 34:
                        Token var16 = this.LT(1);
                        this.match(34);
                        if (this.inputState.guessing == 0) {
                           this.behavior.refArgAction(var16);
                        }
                     case 7:
                     case 14:
                     case 35:
                     case 36:
                     case 37:
                        switch (this.LA(1)) {
                           case 35:
                              this.match(35);
                              Token var17 = this.LT(1);
                              this.match(34);
                              if (this.inputState.guessing == 0) {
                                 this.behavior.refReturnAction(var17);
                              }
                           case 7:
                           case 14:
                           case 36:
                           case 37:
                              switch (this.LA(1)) {
                                 case 37:
                                    this.throwsSpec();
                                 case 7:
                                 case 14:
                                 case 36:
                                    switch (this.LA(1)) {
                                       case 14:
                                          this.ruleOptionsSpec();
                                       case 7:
                                       case 36:
                                          switch (this.LA(1)) {
                                             case 7:
                                                Token var18 = this.LT(1);
                                                this.match(7);
                                                if (this.inputState.guessing == 0) {
                                                   this.behavior.refInitAction(var18);
                                                }
                                             case 36:
                                                this.match(36);
                                                this.block();
                                                this.match(16);
                                                switch (this.LA(1)) {
                                                   case 2:
                                                   case 3:
                                                   case 4:
                                                   case 5:
                                                   case 6:
                                                   case 11:
                                                   case 12:
                                                   case 13:
                                                   case 14:
                                                   case 15:
                                                   case 16:
                                                   case 17:
                                                   case 18:
                                                   case 19:
                                                   case 20:
                                                   case 21:
                                                   case 22:
                                                   case 23:
                                                   case 25:
                                                   case 26:
                                                   case 27:
                                                   case 28:
                                                   case 29:
                                                   case 33:
                                                   case 34:
                                                   case 35:
                                                   case 36:
                                                   case 37:
                                                   case 38:
                                                   case 40:
                                                   default:
                                                      throw new NoViableAltException(this.LT(1), this.getFilename());
                                                   case 39:
                                                      this.exceptionGroup();
                                                   case 1:
                                                   case 7:
                                                   case 8:
                                                   case 9:
                                                   case 10:
                                                   case 24:
                                                   case 30:
                                                   case 31:
                                                   case 32:
                                                   case 41:
                                                      if (this.inputState.guessing == 0) {
                                                         this.behavior.endRule(var9.getText());
                                                      }

                                                      return;
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
   }

   public final void throwsSpec() throws RecognitionException, TokenStreamException {
      String var1 = null;
      this.match(37);
      Token var2 = this.id();
      if (this.inputState.guessing == 0) {
         var1 = var2.getText();
      }

      while(this.LA(1) == 38) {
         this.match(38);
         Token var3 = this.id();
         if (this.inputState.guessing == 0) {
            var1 = var1 + "," + var3.getText();
         }
      }

      if (this.inputState.guessing == 0) {
         this.behavior.setUserExceptions(var1);
      }

   }

   public final void ruleOptionsSpec() throws RecognitionException, TokenStreamException {
      this.match(14);

      for(; this.LA(1) == 24 || this.LA(1) == 41; this.match(16)) {
         Token var1 = this.id();
         this.match(15);
         Token var2 = this.optionValue();
         if (this.inputState.guessing == 0) {
            this.behavior.setRuleOption(var1, var2);
         }
      }

      this.match(17);
   }

   public final void block() throws RecognitionException, TokenStreamException {
      if (this.inputState.guessing == 0) {
         ++this.blockNesting;
      }

      this.alternative();

      while(this.LA(1) == 21) {
         this.match(21);
         this.alternative();
      }

      if (this.inputState.guessing == 0) {
         --this.blockNesting;
      }

   }

   public final void exceptionGroup() throws RecognitionException, TokenStreamException {
      if (this.inputState.guessing == 0) {
         this.behavior.beginExceptionGroup();
      }

      int var1;
      for(var1 = 0; this.LA(1) == 39; ++var1) {
         this.exceptionSpec();
      }

      if (var1 >= 1) {
         if (this.inputState.guessing == 0) {
            this.behavior.endExceptionGroup();
         }

      } else {
         throw new NoViableAltException(this.LT(1), this.getFilename());
      }
   }

   public final void alternative() throws RecognitionException, TokenStreamException {
      boolean var1 = true;
      switch (this.LA(1)) {
         case 8:
         case 9:
         case 10:
         case 11:
         case 12:
         case 13:
         case 14:
         case 15:
         case 17:
         case 18:
         case 20:
         case 22:
         case 23:
         case 25:
         case 26:
         case 29:
         case 30:
         case 31:
         case 32:
         case 34:
         case 35:
         case 36:
         case 37:
         case 38:
         case 40:
         case 45:
         case 46:
         case 47:
         case 48:
         case 49:
         default:
            throw new NoViableAltException(this.LT(1), this.getFilename());
         case 33:
            this.match(33);
            if (this.inputState.guessing == 0) {
               var1 = false;
            }
         case 6:
         case 7:
         case 16:
         case 19:
         case 21:
         case 24:
         case 27:
         case 28:
         case 39:
         case 41:
         case 42:
         case 43:
         case 44:
         case 50:
            if (this.inputState.guessing == 0) {
               this.behavior.beginAlt(var1);
            }
      }

      while(_tokenSet_2.member(this.LA(1))) {
         this.element();
      }

      switch (this.LA(1)) {
         case 39:
            this.exceptionSpecNoLabel();
         case 16:
         case 21:
         case 28:
            if (this.inputState.guessing == 0) {
               this.behavior.endAlt();
            }

            return;
         default:
            throw new NoViableAltException(this.LT(1), this.getFilename());
      }
   }

   public final void element() throws RecognitionException, TokenStreamException {
      this.elementNoOptionSpec();
      switch (this.LA(1)) {
         case 8:
         case 9:
         case 10:
         case 11:
         case 12:
         case 13:
         case 14:
         case 15:
         case 17:
         case 18:
         case 20:
         case 22:
         case 23:
         case 26:
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
         case 45:
         case 46:
         case 47:
         case 48:
         case 49:
         default:
            throw new NoViableAltException(this.LT(1), this.getFilename());
         case 25:
            this.elementOptionSpec();
         case 6:
         case 7:
         case 16:
         case 19:
         case 21:
         case 24:
         case 27:
         case 28:
         case 39:
         case 41:
         case 42:
         case 43:
         case 44:
         case 50:
      }
   }

   public final void exceptionSpecNoLabel() throws RecognitionException, TokenStreamException {
      this.match(39);
      if (this.inputState.guessing == 0) {
         this.behavior.beginExceptionSpec((Token)null);
      }

      while(this.LA(1) == 40) {
         this.exceptionHandler();
      }

      if (this.inputState.guessing == 0) {
         this.behavior.endExceptionSpec();
      }

   }

   public final void exceptionSpec() throws RecognitionException, TokenStreamException {
      Object var1 = null;
      Token var2 = null;
      this.match(39);
      switch (this.LA(1)) {
         case 2:
         case 3:
         case 4:
         case 5:
         case 6:
         case 11:
         case 12:
         case 13:
         case 14:
         case 15:
         case 16:
         case 17:
         case 18:
         case 19:
         case 20:
         case 21:
         case 22:
         case 23:
         case 25:
         case 26:
         case 27:
         case 28:
         case 29:
         case 33:
         case 35:
         case 36:
         case 37:
         case 38:
         default:
            throw new NoViableAltException(this.LT(1), this.getFilename());
         case 34:
            Token var3 = this.LT(1);
            this.match(34);
            if (this.inputState.guessing == 0) {
               var2 = var3;
            }
         case 1:
         case 7:
         case 8:
         case 9:
         case 10:
         case 24:
         case 30:
         case 31:
         case 32:
         case 39:
         case 40:
         case 41:
            if (this.inputState.guessing == 0) {
               this.behavior.beginExceptionSpec(var2);
            }
      }

      while(this.LA(1) == 40) {
         this.exceptionHandler();
      }

      if (this.inputState.guessing == 0) {
         this.behavior.endExceptionSpec();
      }

   }

   public final void exceptionHandler() throws RecognitionException, TokenStreamException {
      Object var1 = null;
      Object var2 = null;
      this.match(40);
      Token var5 = this.LT(1);
      this.match(34);
      Token var6 = this.LT(1);
      this.match(7);
      if (this.inputState.guessing == 0) {
         this.behavior.refExceptionHandler(var5, var6);
      }

   }

   public final void elementNoOptionSpec() throws RecognitionException, TokenStreamException {
      Object var1 = null;
      Object var2 = null;
      Object var3 = null;
      Object var4 = null;
      Object var5 = null;
      Object var6 = null;
      Object var7 = null;
      Object var8 = null;
      Token var9 = null;
      Object var10 = null;
      Token var11 = null;
      byte var12 = 1;
      switch (this.LA(1)) {
         case 7:
            Token var19 = this.LT(1);
            this.match(7);
            if (this.inputState.guessing == 0) {
               this.behavior.refAction(var19);
            }
            break;
         case 43:
            Token var20 = this.LT(1);
            this.match(43);
            if (this.inputState.guessing == 0) {
               this.behavior.refSemPred(var20);
            }
            break;
         case 44:
            this.tree();
            break;
         default:
            if ((this.LA(1) == 24 || this.LA(1) == 41) && this.LA(2) == 15) {
               Token var21 = this.id();
               this.match(15);
               if ((this.LA(1) == 24 || this.LA(1) == 41) && this.LA(2) == 36) {
                  var9 = this.id();
                  this.match(36);
                  if (this.inputState.guessing == 0) {
                     this.checkForMissingEndRule(var9);
                  }
               } else if (this.LA(1) != 24 && this.LA(1) != 41 || !_tokenSet_3.member(this.LA(2))) {
                  throw new NoViableAltException(this.LT(1), this.getFilename());
               }

               switch (this.LA(1)) {
                  case 24:
                     Token var15 = this.LT(1);
                     this.match(24);
                     switch (this.LA(1)) {
                        case 8:
                        case 9:
                        case 10:
                        case 11:
                        case 12:
                        case 13:
                        case 14:
                        case 15:
                        case 17:
                        case 18:
                        case 20:
                        case 22:
                        case 23:
                        case 26:
                        case 29:
                        case 30:
                        case 31:
                        case 32:
                        case 33:
                        case 35:
                        case 36:
                        case 37:
                        case 38:
                        case 40:
                        case 45:
                        case 46:
                        case 47:
                        case 48:
                        case 49:
                        default:
                           throw new NoViableAltException(this.LT(1), this.getFilename());
                        case 34:
                           Token var16 = this.LT(1);
                           this.match(34);
                           if (this.inputState.guessing == 0) {
                              var11 = var16;
                           }
                        case 6:
                        case 7:
                        case 16:
                        case 19:
                        case 21:
                        case 24:
                        case 25:
                        case 27:
                        case 28:
                        case 39:
                        case 41:
                        case 42:
                        case 43:
                        case 44:
                        case 50:
                           if (this.inputState.guessing == 0) {
                              this.behavior.refToken(var21, var15, var9, var11, false, var12, this.lastInRule());
                           }

                           return;
                     }
                  case 41:
                     Token var13 = this.LT(1);
                     this.match(41);
                     switch (this.LA(1)) {
                        case 8:
                        case 9:
                        case 10:
                        case 11:
                        case 12:
                        case 13:
                        case 14:
                        case 15:
                        case 17:
                        case 18:
                        case 20:
                        case 22:
                        case 23:
                        case 26:
                        case 29:
                        case 30:
                        case 31:
                        case 32:
                        case 35:
                        case 36:
                        case 37:
                        case 38:
                        case 40:
                        case 45:
                        case 46:
                        case 47:
                        case 48:
                        case 49:
                        default:
                           throw new NoViableAltException(this.LT(1), this.getFilename());
                        case 34:
                           Token var14 = this.LT(1);
                           this.match(34);
                           if (this.inputState.guessing == 0) {
                              var11 = var14;
                           }
                        case 6:
                        case 7:
                        case 16:
                        case 19:
                        case 21:
                        case 24:
                        case 25:
                        case 27:
                        case 28:
                        case 33:
                        case 39:
                        case 41:
                        case 42:
                        case 43:
                        case 44:
                        case 50:
                           switch (this.LA(1)) {
                              case 8:
                              case 9:
                              case 10:
                              case 11:
                              case 12:
                              case 13:
                              case 14:
                              case 15:
                              case 17:
                              case 18:
                              case 20:
                              case 22:
                              case 23:
                              case 26:
                              case 29:
                              case 30:
                              case 31:
                              case 32:
                              case 34:
                              case 35:
                              case 36:
                              case 37:
                              case 38:
                              case 40:
                              case 45:
                              case 46:
                              case 47:
                              case 48:
                              case 49:
                              default:
                                 throw new NoViableAltException(this.LT(1), this.getFilename());
                              case 33:
                                 this.match(33);
                                 if (this.inputState.guessing == 0) {
                                    var12 = 3;
                                 }
                              case 6:
                              case 7:
                              case 16:
                              case 19:
                              case 21:
                              case 24:
                              case 25:
                              case 27:
                              case 28:
                              case 39:
                              case 41:
                              case 42:
                              case 43:
                              case 44:
                              case 50:
                                 if (this.inputState.guessing == 0) {
                                    this.behavior.refRule(var21, var13, var9, var11, var12);
                                 }

                                 return;
                           }
                     }
                  default:
                     throw new NoViableAltException(this.LT(1), this.getFilename());
               }
            } else {
               if (!_tokenSet_4.member(this.LA(1)) || !_tokenSet_5.member(this.LA(2))) {
                  throw new NoViableAltException(this.LT(1), this.getFilename());
               }

               if ((this.LA(1) == 24 || this.LA(1) == 41) && this.LA(2) == 36) {
                  var9 = this.id();
                  this.match(36);
                  if (this.inputState.guessing == 0) {
                     this.checkForMissingEndRule(var9);
                  }
               } else if (!_tokenSet_4.member(this.LA(1)) || !_tokenSet_6.member(this.LA(2))) {
                  throw new NoViableAltException(this.LT(1), this.getFilename());
               }

               switch (this.LA(1)) {
                  case 27:
                     this.ebnf(var9, false);
                     break;
                  case 41:
                     Token var17 = this.LT(1);
                     this.match(41);
                     switch (this.LA(1)) {
                        case 8:
                        case 9:
                        case 10:
                        case 11:
                        case 12:
                        case 13:
                        case 14:
                        case 15:
                        case 17:
                        case 18:
                        case 20:
                        case 22:
                        case 23:
                        case 26:
                        case 29:
                        case 30:
                        case 31:
                        case 32:
                        case 35:
                        case 36:
                        case 37:
                        case 38:
                        case 40:
                        case 45:
                        case 46:
                        case 47:
                        case 48:
                        case 49:
                        default:
                           throw new NoViableAltException(this.LT(1), this.getFilename());
                        case 34:
                           Token var18 = this.LT(1);
                           this.match(34);
                           if (this.inputState.guessing == 0) {
                              var11 = var18;
                           }
                        case 6:
                        case 7:
                        case 16:
                        case 19:
                        case 21:
                        case 24:
                        case 25:
                        case 27:
                        case 28:
                        case 33:
                        case 39:
                        case 41:
                        case 42:
                        case 43:
                        case 44:
                        case 50:
                           switch (this.LA(1)) {
                              case 8:
                              case 9:
                              case 10:
                              case 11:
                              case 12:
                              case 13:
                              case 14:
                              case 15:
                              case 17:
                              case 18:
                              case 20:
                              case 22:
                              case 23:
                              case 26:
                              case 29:
                              case 30:
                              case 31:
                              case 32:
                              case 34:
                              case 35:
                              case 36:
                              case 37:
                              case 38:
                              case 40:
                              case 45:
                              case 46:
                              case 47:
                              case 48:
                              case 49:
                              default:
                                 throw new NoViableAltException(this.LT(1), this.getFilename());
                              case 33:
                                 this.match(33);
                                 if (this.inputState.guessing == 0) {
                                    var12 = 3;
                                 }
                              case 6:
                              case 7:
                              case 16:
                              case 19:
                              case 21:
                              case 24:
                              case 25:
                              case 27:
                              case 28:
                              case 39:
                              case 41:
                              case 42:
                              case 43:
                              case 44:
                              case 50:
                                 if (this.inputState.guessing == 0) {
                                    this.behavior.refRule((Token)var10, var17, var9, var11, var12);
                                 }

                                 return;
                           }
                     }
                  case 42:
                     this.match(42);
                     switch (this.LA(1)) {
                        case 19:
                        case 24:
                           this.notTerminal(var9);
                           return;
                        case 27:
                           this.ebnf(var9, true);
                           return;
                        default:
                           throw new NoViableAltException(this.LT(1), this.getFilename());
                     }
                  default:
                     if ((this.LA(1) == 6 || this.LA(1) == 19 || this.LA(1) == 24) && this.LA(2) == 22) {
                        this.range(var9);
                     } else {
                        if (!_tokenSet_7.member(this.LA(1)) || !_tokenSet_8.member(this.LA(2))) {
                           throw new NoViableAltException(this.LT(1), this.getFilename());
                        }

                        this.terminal(var9);
                     }
               }
            }
      }

   }

   public final void elementOptionSpec() throws RecognitionException, TokenStreamException {
      Object var1 = null;
      Object var2 = null;
      this.match(25);
      Token var3 = this.id();
      this.match(15);
      Token var5 = this.optionValue();
      if (this.inputState.guessing == 0) {
         this.behavior.refElementOption(var3, var5);
      }

      while(this.LA(1) == 16) {
         this.match(16);
         var3 = this.id();
         this.match(15);
         var5 = this.optionValue();
         if (this.inputState.guessing == 0) {
            this.behavior.refElementOption(var3, var5);
         }
      }

      this.match(26);
   }

   public final void range(Token var1) throws RecognitionException, TokenStreamException {
      Object var2 = null;
      Object var3 = null;
      Object var4 = null;
      Object var5 = null;
      Object var6 = null;
      Object var7 = null;
      Token var8 = null;
      Token var9 = null;
      int var10 = 1;
      switch (this.LA(1)) {
         case 6:
         case 24:
            switch (this.LA(1)) {
               case 6:
                  Token var14 = this.LT(1);
                  this.match(6);
                  if (this.inputState.guessing == 0) {
                     var8 = var14;
                  }
                  break;
               case 24:
                  Token var13 = this.LT(1);
                  this.match(24);
                  if (this.inputState.guessing == 0) {
                     var8 = var13;
                  }
                  break;
               default:
                  throw new NoViableAltException(this.LT(1), this.getFilename());
            }

            this.match(22);
            switch (this.LA(1)) {
               case 6:
                  Token var16 = this.LT(1);
                  this.match(6);
                  if (this.inputState.guessing == 0) {
                     var9 = var16;
                  }
                  break;
               case 24:
                  Token var15 = this.LT(1);
                  this.match(24);
                  if (this.inputState.guessing == 0) {
                     var9 = var15;
                  }
                  break;
               default:
                  throw new NoViableAltException(this.LT(1), this.getFilename());
            }

            var10 = this.ast_type_spec();
            if (this.inputState.guessing == 0) {
               this.behavior.refTokenRange(var8, var9, var1, var10, this.lastInRule());
            }
            break;
         case 19:
            Token var11 = this.LT(1);
            this.match(19);
            this.match(22);
            Token var12 = this.LT(1);
            this.match(19);
            switch (this.LA(1)) {
               case 8:
               case 9:
               case 10:
               case 11:
               case 12:
               case 13:
               case 14:
               case 15:
               case 17:
               case 18:
               case 20:
               case 22:
               case 23:
               case 26:
               case 29:
               case 30:
               case 31:
               case 32:
               case 34:
               case 35:
               case 36:
               case 37:
               case 38:
               case 40:
               case 45:
               case 46:
               case 47:
               case 48:
               case 49:
               default:
                  throw new NoViableAltException(this.LT(1), this.getFilename());
               case 33:
                  this.match(33);
                  if (this.inputState.guessing == 0) {
                     var10 = 3;
                  }
               case 6:
               case 7:
               case 16:
               case 19:
               case 21:
               case 24:
               case 25:
               case 27:
               case 28:
               case 39:
               case 41:
               case 42:
               case 43:
               case 44:
               case 50:
                  if (this.inputState.guessing == 0) {
                     this.behavior.refCharRange(var11, var12, var1, var10, this.lastInRule());
                  }

                  return;
            }
         default:
            throw new NoViableAltException(this.LT(1), this.getFilename());
      }

   }

   public final void terminal(Token var1) throws RecognitionException, TokenStreamException {
      Object var2 = null;
      Object var3 = null;
      Object var4 = null;
      Object var5 = null;
      Object var6 = null;
      int var7 = 1;
      Token var8 = null;
      switch (this.LA(1)) {
         case 6:
            Token var12 = this.LT(1);
            this.match(6);
            var7 = this.ast_type_spec();
            if (this.inputState.guessing == 0) {
               this.behavior.refStringLiteral(var12, var1, var7, this.lastInRule());
            }
            break;
         case 19:
            Token var9 = this.LT(1);
            this.match(19);
            switch (this.LA(1)) {
               case 8:
               case 9:
               case 10:
               case 11:
               case 12:
               case 13:
               case 14:
               case 15:
               case 17:
               case 18:
               case 20:
               case 22:
               case 23:
               case 26:
               case 29:
               case 30:
               case 31:
               case 32:
               case 34:
               case 35:
               case 36:
               case 37:
               case 38:
               case 40:
               case 45:
               case 46:
               case 47:
               case 48:
               case 49:
               default:
                  throw new NoViableAltException(this.LT(1), this.getFilename());
               case 33:
                  this.match(33);
                  if (this.inputState.guessing == 0) {
                     var7 = 3;
                  }
               case 6:
               case 7:
               case 16:
               case 19:
               case 21:
               case 24:
               case 25:
               case 27:
               case 28:
               case 39:
               case 41:
               case 42:
               case 43:
               case 44:
               case 50:
                  if (this.inputState.guessing == 0) {
                     this.behavior.refCharLiteral(var9, var1, false, var7, this.lastInRule());
                  }

                  return;
            }
         case 24:
            Token var10 = this.LT(1);
            this.match(24);
            var7 = this.ast_type_spec();
            switch (this.LA(1)) {
               case 8:
               case 9:
               case 10:
               case 11:
               case 12:
               case 13:
               case 14:
               case 15:
               case 17:
               case 18:
               case 20:
               case 22:
               case 23:
               case 26:
               case 29:
               case 30:
               case 31:
               case 32:
               case 33:
               case 35:
               case 36:
               case 37:
               case 38:
               case 40:
               case 45:
               case 46:
               case 47:
               case 48:
               case 49:
               default:
                  throw new NoViableAltException(this.LT(1), this.getFilename());
               case 34:
                  Token var11 = this.LT(1);
                  this.match(34);
                  if (this.inputState.guessing == 0) {
                     var8 = var11;
                  }
               case 6:
               case 7:
               case 16:
               case 19:
               case 21:
               case 24:
               case 25:
               case 27:
               case 28:
               case 39:
               case 41:
               case 42:
               case 43:
               case 44:
               case 50:
                  if (this.inputState.guessing == 0) {
                     this.behavior.refToken((Token)null, var10, var1, var8, false, var7, this.lastInRule());
                  }

                  return;
            }
         case 50:
            Token var13 = this.LT(1);
            this.match(50);
            var7 = this.ast_type_spec();
            if (this.inputState.guessing == 0) {
               this.behavior.refWildcard(var13, var1, var7);
            }
            break;
         default:
            throw new NoViableAltException(this.LT(1), this.getFilename());
      }

   }

   public final void notTerminal(Token var1) throws RecognitionException, TokenStreamException {
      Object var2 = null;
      Object var3 = null;
      int var4 = 1;
      switch (this.LA(1)) {
         case 19:
            Token var5 = this.LT(1);
            this.match(19);
            switch (this.LA(1)) {
               case 8:
               case 9:
               case 10:
               case 11:
               case 12:
               case 13:
               case 14:
               case 15:
               case 17:
               case 18:
               case 20:
               case 22:
               case 23:
               case 26:
               case 29:
               case 30:
               case 31:
               case 32:
               case 34:
               case 35:
               case 36:
               case 37:
               case 38:
               case 40:
               case 45:
               case 46:
               case 47:
               case 48:
               case 49:
               default:
                  throw new NoViableAltException(this.LT(1), this.getFilename());
               case 33:
                  this.match(33);
                  if (this.inputState.guessing == 0) {
                     var4 = 3;
                  }
               case 6:
               case 7:
               case 16:
               case 19:
               case 21:
               case 24:
               case 25:
               case 27:
               case 28:
               case 39:
               case 41:
               case 42:
               case 43:
               case 44:
               case 50:
                  if (this.inputState.guessing == 0) {
                     this.behavior.refCharLiteral(var5, var1, true, var4, this.lastInRule());
                  }

                  return;
            }
         case 24:
            Token var6 = this.LT(1);
            this.match(24);
            var4 = this.ast_type_spec();
            if (this.inputState.guessing == 0) {
               this.behavior.refToken((Token)null, var6, var1, (Token)null, true, var4, this.lastInRule());
            }
            break;
         default:
            throw new NoViableAltException(this.LT(1), this.getFilename());
      }

   }

   public final void ebnf(Token var1, boolean var2) throws RecognitionException, TokenStreamException {
      Object var3 = null;
      Object var4 = null;
      Object var5 = null;
      Token var6 = this.LT(1);
      this.match(27);
      if (this.inputState.guessing == 0) {
         this.behavior.beginSubRule(var1, var6, var2);
      }

      if (this.LA(1) == 14) {
         this.subruleOptionsSpec();
         switch (this.LA(1)) {
            case 7:
               Token var7 = this.LT(1);
               this.match(7);
               if (this.inputState.guessing == 0) {
                  this.behavior.refInitAction(var7);
               }
            case 36:
               this.match(36);
               break;
            default:
               throw new NoViableAltException(this.LT(1), this.getFilename());
         }
      } else if (this.LA(1) == 7 && this.LA(2) == 36) {
         Token var8 = this.LT(1);
         this.match(7);
         if (this.inputState.guessing == 0) {
            this.behavior.refInitAction(var8);
         }

         this.match(36);
      } else if (!_tokenSet_9.member(this.LA(1)) || !_tokenSet_10.member(this.LA(2))) {
         throw new NoViableAltException(this.LT(1), this.getFilename());
      }

      this.block();
      this.match(28);
      label57:
      switch (this.LA(1)) {
         case 6:
         case 7:
         case 16:
         case 19:
         case 21:
         case 24:
         case 25:
         case 27:
         case 28:
         case 33:
         case 39:
         case 41:
         case 42:
         case 43:
         case 44:
         case 45:
         case 46:
         case 47:
         case 50:
            switch (this.LA(1)) {
               case 6:
               case 7:
               case 16:
               case 19:
               case 21:
               case 24:
               case 25:
               case 27:
               case 28:
               case 33:
               case 39:
               case 41:
               case 42:
               case 43:
               case 44:
               case 50:
                  break;
               case 8:
               case 9:
               case 10:
               case 11:
               case 12:
               case 13:
               case 14:
               case 15:
               case 17:
               case 18:
               case 20:
               case 22:
               case 23:
               case 26:
               case 29:
               case 30:
               case 31:
               case 32:
               case 34:
               case 35:
               case 36:
               case 37:
               case 38:
               case 40:
               case 48:
               case 49:
               default:
                  throw new NoViableAltException(this.LT(1), this.getFilename());
               case 45:
                  this.match(45);
                  if (this.inputState.guessing == 0) {
                     this.behavior.optionalSubRule();
                  }
                  break;
               case 46:
                  this.match(46);
                  if (this.inputState.guessing == 0) {
                     this.behavior.zeroOrMoreSubRule();
                  }
                  break;
               case 47:
                  this.match(47);
                  if (this.inputState.guessing == 0) {
                     this.behavior.oneOrMoreSubRule();
                  }
            }

            switch (this.LA(1)) {
               case 6:
               case 7:
               case 16:
               case 19:
               case 21:
               case 24:
               case 25:
               case 27:
               case 28:
               case 39:
               case 41:
               case 42:
               case 43:
               case 44:
               case 50:
                  break label57;
               case 8:
               case 9:
               case 10:
               case 11:
               case 12:
               case 13:
               case 14:
               case 15:
               case 17:
               case 18:
               case 20:
               case 22:
               case 23:
               case 26:
               case 29:
               case 30:
               case 31:
               case 32:
               case 34:
               case 35:
               case 36:
               case 37:
               case 38:
               case 40:
               case 45:
               case 46:
               case 47:
               case 48:
               case 49:
               default:
                  throw new NoViableAltException(this.LT(1), this.getFilename());
               case 33:
                  this.match(33);
                  if (this.inputState.guessing == 0) {
                     this.behavior.noASTSubRule();
                  }
                  break label57;
            }
         case 8:
         case 9:
         case 10:
         case 11:
         case 12:
         case 13:
         case 14:
         case 15:
         case 17:
         case 18:
         case 20:
         case 22:
         case 23:
         case 26:
         case 29:
         case 30:
         case 31:
         case 32:
         case 34:
         case 35:
         case 36:
         case 37:
         case 38:
         case 40:
         case 49:
         default:
            throw new NoViableAltException(this.LT(1), this.getFilename());
         case 48:
            this.match(48);
            if (this.inputState.guessing == 0) {
               this.behavior.synPred();
            }
      }

      if (this.inputState.guessing == 0) {
         this.behavior.endSubRule();
      }

   }

   public final void tree() throws RecognitionException, TokenStreamException {
      Object var1 = null;
      Token var3 = this.LT(1);
      this.match(44);
      if (this.inputState.guessing == 0) {
         this.behavior.beginTree(var3);
      }

      this.rootNode();
      if (this.inputState.guessing == 0) {
         this.behavior.beginChildList();
      }

      int var2;
      for(var2 = 0; _tokenSet_2.member(this.LA(1)); ++var2) {
         this.element();
      }

      if (var2 >= 1) {
         if (this.inputState.guessing == 0) {
            this.behavior.endChildList();
         }

         this.match(28);
         if (this.inputState.guessing == 0) {
            this.behavior.endTree();
         }

      } else {
         throw new NoViableAltException(this.LT(1), this.getFilename());
      }
   }

   public final void rootNode() throws RecognitionException, TokenStreamException {
      Token var1 = null;
      if ((this.LA(1) == 24 || this.LA(1) == 41) && this.LA(2) == 36) {
         var1 = this.id();
         this.match(36);
         if (this.inputState.guessing == 0) {
            this.checkForMissingEndRule(var1);
         }
      } else if (!_tokenSet_7.member(this.LA(1)) || !_tokenSet_11.member(this.LA(2))) {
         throw new NoViableAltException(this.LT(1), this.getFilename());
      }

      this.terminal(var1);
   }

   public final int ast_type_spec() throws RecognitionException, TokenStreamException {
      byte var1 = 1;
      switch (this.LA(1)) {
         case 6:
         case 7:
         case 16:
         case 19:
         case 21:
         case 24:
         case 25:
         case 27:
         case 28:
         case 34:
         case 39:
         case 41:
         case 42:
         case 43:
         case 44:
         case 50:
            break;
         case 8:
         case 9:
         case 10:
         case 11:
         case 12:
         case 13:
         case 14:
         case 15:
         case 17:
         case 18:
         case 20:
         case 22:
         case 23:
         case 26:
         case 29:
         case 30:
         case 31:
         case 32:
         case 35:
         case 36:
         case 37:
         case 38:
         case 40:
         case 45:
         case 46:
         case 47:
         case 48:
         default:
            throw new NoViableAltException(this.LT(1), this.getFilename());
         case 33:
            this.match(33);
            if (this.inputState.guessing == 0) {
               var1 = 3;
            }
            break;
         case 49:
            this.match(49);
            if (this.inputState.guessing == 0) {
               var1 = 2;
            }
      }

      return var1;
   }

   private static final long[] mk_tokenSet_0() {
      long[] var0 = new long[]{2206556225792L, 0L};
      return var0;
   }

   private static final long[] mk_tokenSet_1() {
      long[] var0 = new long[]{2472844214400L, 0L};
      return var0;
   }

   private static final long[] mk_tokenSet_2() {
      long[] var0 = new long[]{1158885407195328L, 0L};
      return var0;
   }

   private static final long[] mk_tokenSet_3() {
      long[] var0 = new long[]{1159461236965568L, 0L};
      return var0;
   }

   private static final long[] mk_tokenSet_4() {
      long[] var0 = new long[]{1132497128128576L, 0L};
      return var0;
   }

   private static final long[] mk_tokenSet_5() {
      long[] var0 = new long[]{1722479914074304L, 0L};
      return var0;
   }

   private static final long[] mk_tokenSet_6() {
      long[] var0 = new long[]{1722411194597568L, 0L};
      return var0;
   }

   private static final long[] mk_tokenSet_7() {
      long[] var0 = new long[]{1125899924144192L, 0L};
      return var0;
   }

   private static final long[] mk_tokenSet_8() {
      long[] var0 = new long[]{1722411190386880L, 0L};
      return var0;
   }

   private static final long[] mk_tokenSet_9() {
      long[] var0 = new long[]{1159444023476416L, 0L};
      return var0;
   }

   private static final long[] mk_tokenSet_10() {
      long[] var0 = new long[]{2251345007067328L, 0L};
      return var0;
   }

   private static final long[] mk_tokenSet_11() {
      long[] var0 = new long[]{1721861130420416L, 0L};
      return var0;
   }
}
