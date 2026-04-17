package org.hibernate.hql.internal.ast.tree;

import org.hibernate.engine.spi.SessionFactoryImplementor;

public interface SessionFactoryAwareNode {
   void setSessionFactory(SessionFactoryImplementor var1);
}
