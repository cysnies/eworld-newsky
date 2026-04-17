package com.sk89q.worldedit.expression.lexer;

import com.sk89q.worldedit.expression.lexer.tokens.CharacterToken;
import com.sk89q.worldedit.expression.lexer.tokens.IdentifierToken;
import com.sk89q.worldedit.expression.lexer.tokens.KeywordToken;
import com.sk89q.worldedit.expression.lexer.tokens.NumberToken;
import com.sk89q.worldedit.expression.lexer.tokens.OperatorToken;
import com.sk89q.worldedit.expression.lexer.tokens.Token;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lexer {
   private final String expression;
   private int position = 0;
   private final DecisionTree operatorTree = new DecisionTree((String)null, new Object[]{'+', new DecisionTree("+", new Object[]{'=', new DecisionTree("+=", new Object[0]), '+', new DecisionTree("++", new Object[0])}), '-', new DecisionTree("-", new Object[]{'=', new DecisionTree("-=", new Object[0]), '-', new DecisionTree("--", new Object[0])}), '*', new DecisionTree("*", new Object[]{'=', new DecisionTree("*=", new Object[0]), '*', new DecisionTree("**", new Object[0])}), '/', new DecisionTree("/", new Object[]{'=', new DecisionTree("/=", new Object[0])}), '%', new DecisionTree("%", new Object[]{'=', new DecisionTree("%=", new Object[0])}), '^', new DecisionTree("^", new Object[]{'=', new DecisionTree("^=", new Object[0])}), '=', new DecisionTree("=", new Object[]{'=', new DecisionTree("==", new Object[0])}), '!', new DecisionTree("!", new Object[]{'=', new DecisionTree("!=", new Object[0])}), '<', new DecisionTree("<", new Object[]{'<', new DecisionTree("<<", new Object[0]), '=', new DecisionTree("<=", new Object[0])}), '>', new DecisionTree(">", new Object[]{'>', new DecisionTree(">>", new Object[0]), '=', new DecisionTree(">=", new Object[0])}), '&', new DecisionTree((String)null, new Object[]{'&', new DecisionTree("&&", new Object[0])}), '|', new DecisionTree((String)null, new Object[]{'|', new DecisionTree("||", new Object[0])}), '~', new DecisionTree("~", new Object[]{'=', new DecisionTree("~=", new Object[0])})});
   private static final Set characterTokens = new HashSet();
   private static final Set keywords;
   private static final Pattern numberPattern;
   private static final Pattern identifierPattern;

   private Lexer(String expression) {
      this.expression = expression;
   }

   public static final List tokenize(String expression) throws LexerException {
      return (new Lexer(expression)).tokenize();
   }

   private final List tokenize() throws LexerException {
      List<Token> tokens = new ArrayList();

      do {
         this.skipWhitespace();
         if (this.position >= this.expression.length()) {
            break;
         }

         Token token = this.operatorTree.evaluate(this.position);
         if (token != null) {
            tokens.add(token);
         } else {
            char ch = this.peek();
            if (characterTokens.contains(ch)) {
               tokens.add(new CharacterToken(this.position++, ch));
            } else {
               Matcher numberMatcher = numberPattern.matcher(this.expression.substring(this.position));
               if (numberMatcher.lookingAt()) {
                  String numberPart = numberMatcher.group(1);
                  if (numberPart.length() > 0) {
                     try {
                        tokens.add(new NumberToken(this.position, Double.parseDouble(numberPart)));
                     } catch (NumberFormatException e) {
                        throw new LexerException(this.position, "Number parsing failed", e);
                     }

                     this.position += numberPart.length();
                     continue;
                  }
               }

               Matcher identifierMatcher = identifierPattern.matcher(this.expression.substring(this.position));
               if (!identifierMatcher.lookingAt()) {
                  throw new LexerException(this.position, "Unknown character '" + ch + "'");
               }

               String identifierPart = identifierMatcher.group(1);
               if (identifierPart.length() <= 0) {
                  throw new LexerException(this.position, "Unknown character '" + ch + "'");
               }

               if (keywords.contains(identifierPart)) {
                  tokens.add(new KeywordToken(this.position, identifierPart));
               } else {
                  tokens.add(new IdentifierToken(this.position, identifierPart));
               }

               this.position += identifierPart.length();
            }
         }
      } while(this.position < this.expression.length());

      return tokens;
   }

   private char peek() {
      return this.expression.charAt(this.position);
   }

   private final void skipWhitespace() {
      while(this.position < this.expression.length() && Character.isWhitespace(this.peek())) {
         ++this.position;
      }

   }

   static {
      characterTokens.add(',');
      characterTokens.add('(');
      characterTokens.add(')');
      characterTokens.add('{');
      characterTokens.add('}');
      characterTokens.add(';');
      characterTokens.add('?');
      characterTokens.add(':');
      keywords = new HashSet(Arrays.asList("if", "else", "while", "do", "for", "break", "continue", "return", "switch", "case", "default"));
      numberPattern = Pattern.compile("^([0-9]*(?:\\.[0-9]+)?(?:[eE][+-]?[0-9]+)?)");
      identifierPattern = Pattern.compile("^([A-Za-z][0-9A-Za-z_]*)");
   }

   public class DecisionTree {
      private final String tokenName;
      private final Map subTrees;

      private DecisionTree(String tokenName, Object... args) {
         this.subTrees = new HashMap();
         this.tokenName = tokenName;
         if (args.length % 2 != 0) {
            throw new UnsupportedOperationException("You need to pass an even number of arguments.");
         } else {
            for(int i = 0; i < args.length; i += 2) {
               if (!(args[i] instanceof Character)) {
                  throw new UnsupportedOperationException("Argument #" + i + " expected to be 'Character', not '" + args[i].getClass().getName() + "'.");
               }

               if (!(args[i + 1] instanceof DecisionTree)) {
                  throw new UnsupportedOperationException("Argument #" + (i + 1) + " expected to be 'DecisionTree', not '" + args[i + 1].getClass().getName() + "'.");
               }

               Character next = (Character)args[i];
               DecisionTree subTree = (DecisionTree)args[i + 1];
               this.subTrees.put(next, subTree);
            }

         }
      }

      private Token evaluate(int startPosition) throws LexerException {
         if (Lexer.this.position < Lexer.this.expression.length()) {
            char next = Lexer.this.peek();
            DecisionTree subTree = (DecisionTree)this.subTrees.get(next);
            if (subTree != null) {
               ++Lexer.this.position;
               Token subTreeResult = subTree.evaluate(startPosition);
               if (subTreeResult != null) {
                  return subTreeResult;
               }

               --Lexer.this.position;
            }
         }

         return this.tokenName == null ? null : new OperatorToken(startPosition, this.tokenName);
      }
   }
}
