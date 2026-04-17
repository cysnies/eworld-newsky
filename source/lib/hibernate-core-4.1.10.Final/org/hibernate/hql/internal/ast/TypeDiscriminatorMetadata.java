package org.hibernate.hql.internal.ast;

import org.hibernate.type.Type;

public interface TypeDiscriminatorMetadata {
   String getSqlFragment();

   Type getResolutionType();
}
