package com.sk89q.worldedit.expression.parser;

import com.sk89q.worldedit.expression.ExpressionException;

public class ParserException extends ExpressionException {
   private static final long serialVersionUID = 1L;

   public ParserException(int position) {
      super(position, getPrefix(position));
   }

   public ParserException(int position, String message, Throwable cause) {
      super(position, getPrefix(position) + ": " + message, cause);
   }

   public ParserException(int position, String message) {
      super(position, getPrefix(position) + ": " + message);
   }

   public ParserException(int position, Throwable cause) {
      super(position, getPrefix(position), cause);
   }

   private static String getPrefix(int position) {
      return position < 0 ? "Parser error" : "Parser error at " + (position + 1);
   }
}
