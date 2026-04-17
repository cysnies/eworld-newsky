package com.sk89q.worldedit.expression.lexer;

import com.sk89q.worldedit.expression.ExpressionException;

public class LexerException extends ExpressionException {
   private static final long serialVersionUID = 1L;

   public LexerException(int position) {
      super(position, getPrefix(position));
   }

   public LexerException(int position, String message, Throwable cause) {
      super(position, getPrefix(position) + ": " + message, cause);
   }

   public LexerException(int position, String message) {
      super(position, getPrefix(position) + ": " + message);
   }

   public LexerException(int position, Throwable cause) {
      super(position, getPrefix(position), cause);
   }

   private static String getPrefix(int position) {
      return position < 0 ? "Lexer error" : "Lexer error at " + (position + 1);
   }
}
