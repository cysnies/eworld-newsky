package org.hibernate.hql.internal.ast.tree;

import antlr.SemanticException;
import org.hibernate.type.Type;

public interface OperatorNode {
   void initialize() throws SemanticException;

   Type getDataType();
}
