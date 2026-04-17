package org.hibernate.hql.internal.ast.tree;

import antlr.SemanticException;
import antlr.collections.AST;

public interface ResolvableNode {
   void resolve(boolean var1, boolean var2, String var3, AST var4) throws SemanticException;

   void resolve(boolean var1, boolean var2, String var3) throws SemanticException;

   void resolve(boolean var1, boolean var2) throws SemanticException;

   void resolveInFunctionCall(boolean var1, boolean var2) throws SemanticException;

   void resolveIndex(AST var1) throws SemanticException;
}
