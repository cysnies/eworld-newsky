package org.hibernate.hql.internal.ast;

import antlr.SemanticException;

public class InvalidPathException extends SemanticException {
   public InvalidPathException(String s) {
      super(s);
   }
}
