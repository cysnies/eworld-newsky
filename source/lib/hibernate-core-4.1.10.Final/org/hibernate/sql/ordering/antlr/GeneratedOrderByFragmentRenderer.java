package org.hibernate.sql.ordering.antlr;

import antlr.NoViableAltException;
import antlr.RecognitionException;
import antlr.TreeParser;
import antlr.collections.AST;

public class GeneratedOrderByFragmentRenderer extends TreeParser implements GeneratedOrderByFragmentRendererTokenTypes {
   private StringBuilder buffer = new StringBuilder();
   public static final String[] _tokenNames = new String[]{"<0>", "EOF", "<2>", "NULL_TREE_LOOKAHEAD", "ORDER_BY", "SORT_SPEC", "ORDER_SPEC", "SORT_KEY", "EXPR_LIST", "DOT", "IDENT_LIST", "COLUMN_REF", "\"collate\"", "\"asc\"", "\"desc\"", "COMMA", "HARD_QUOTE", "IDENT", "OPEN_PAREN", "CLOSE_PAREN", "NUM_DOUBLE", "NUM_FLOAT", "NUM_INT", "NUM_LONG", "QUOTED_STRING", "\"ascending\"", "\"descending\"", "ID_START_LETTER", "ID_LETTER", "ESCqs", "HEX_DIGIT", "EXPONENT", "FLOAT_SUFFIX", "WS"};

   protected void out(String text) {
      this.buffer.append(text);
   }

   protected void out(AST ast) {
      this.buffer.append(ast.getText());
   }

   String getRenderedFragment() {
      return this.buffer.toString();
   }

   public GeneratedOrderByFragmentRenderer() {
      this.tokenNames = _tokenNames;
   }

   public final void orderByFragment(AST _t) throws RecognitionException {
      AST orderByFragment_AST_in = _t == ASTNULL ? null : _t;

      try {
         AST __t2 = _t;
         this.match(_t, 4);
         _t = _t.getFirstChild();
         this.sortSpecification(_t);
         AST var7 = this._retTree;

         while(true) {
            if (var7 == null) {
               var7 = ASTNULL;
            }

            if (((AST)var7).getType() != 5) {
               _t = __t2.getNextSibling();
               break;
            }

            this.out(", ");
            this.sortSpecification((AST)var7);
            var7 = this._retTree;
         }
      } catch (RecognitionException ex) {
         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this._retTree = _t;
   }

   public final void sortSpecification(AST _t) throws RecognitionException {
      AST sortSpecification_AST_in = _t == ASTNULL ? null : _t;

      try {
         AST __t6 = _t;
         this.match(_t, 5);
         _t = _t.getFirstChild();
         this.sortKeySpecification(_t);
         AST var7 = this._retTree;
         if (var7 == null) {
            var7 = ASTNULL;
         }

         label30:
         switch (((AST)var7).getType()) {
            case 12:
               this.collationSpecification((AST)var7);
               var7 = this._retTree;
            case 3:
            case 6:
               if (var7 == null) {
                  var7 = ASTNULL;
               }

               switch (((AST)var7).getType()) {
                  case 6:
                     this.orderingSpecification((AST)var7);
                     AST var8 = this._retTree;
                  case 3:
                     _t = __t6.getNextSibling();
                     break label30;
                  default:
                     throw new NoViableAltException((AST)var7);
               }
            default:
               throw new NoViableAltException((AST)var7);
         }
      } catch (RecognitionException ex) {
         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this._retTree = _t;
   }

   public final void sortKeySpecification(AST _t) throws RecognitionException {
      AST sortKeySpecification_AST_in = _t == ASTNULL ? null : _t;

      try {
         AST __t10 = _t;
         this.match(_t, 7);
         _t = _t.getFirstChild();
         this.sortKey(_t);
         _t = this._retTree;
         _t = __t10.getNextSibling();
      } catch (RecognitionException ex) {
         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this._retTree = _t;
   }

   public final void collationSpecification(AST _t) throws RecognitionException {
      AST collationSpecification_AST_in = _t == ASTNULL ? null : _t;
      AST c = null;

      try {
         c = _t;
         this.match(_t, 12);
         _t = _t.getNextSibling();
         this.out(" collate ");
         this.out(c);
      } catch (RecognitionException ex) {
         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this._retTree = _t;
   }

   public final void orderingSpecification(AST _t) throws RecognitionException {
      AST orderingSpecification_AST_in = _t == ASTNULL ? null : _t;
      AST o = null;

      try {
         o = _t;
         this.match(_t, 6);
         _t = _t.getNextSibling();
         this.out(" ");
         this.out(o);
      } catch (RecognitionException ex) {
         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this._retTree = _t;
   }

   public final void sortKey(AST _t) throws RecognitionException {
      AST sortKey_AST_in = _t == ASTNULL ? null : _t;
      AST i = null;

      try {
         i = _t;
         this.match(_t, 17);
         _t = _t.getNextSibling();
         this.out(i);
      } catch (RecognitionException ex) {
         this.reportError(ex);
         if (_t != null) {
            _t = _t.getNextSibling();
         }
      }

      this._retTree = _t;
   }
}
