package org.hibernate.hql.internal.ast;

import antlr.RecognitionException;

public interface ErrorReporter {
   void reportError(RecognitionException var1);

   void reportError(String var1);

   void reportWarning(String var1);
}
