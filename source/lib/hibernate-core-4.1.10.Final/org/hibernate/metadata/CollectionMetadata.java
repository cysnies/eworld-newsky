package org.hibernate.metadata;

import org.hibernate.type.Type;

public interface CollectionMetadata {
   Type getKeyType();

   Type getElementType();

   Type getIndexType();

   boolean hasIndex();

   String getRole();

   boolean isArray();

   boolean isPrimitiveArray();

   boolean isLazy();
}
