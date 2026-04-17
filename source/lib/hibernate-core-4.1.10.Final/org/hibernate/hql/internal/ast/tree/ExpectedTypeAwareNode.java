package org.hibernate.hql.internal.ast.tree;

import org.hibernate.type.Type;

public interface ExpectedTypeAwareNode {
   void setExpectedType(Type var1);

   Type getExpectedType();
}
