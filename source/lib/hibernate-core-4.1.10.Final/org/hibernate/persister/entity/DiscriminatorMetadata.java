package org.hibernate.persister.entity;

import org.hibernate.type.Type;

public interface DiscriminatorMetadata {
   String getSqlFragment(String var1);

   Type getResolutionType();
}
