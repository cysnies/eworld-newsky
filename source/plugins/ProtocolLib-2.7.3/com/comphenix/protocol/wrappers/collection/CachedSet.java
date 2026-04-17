package com.comphenix.protocol.wrappers.collection;

import java.util.Set;

public class CachedSet extends CachedCollection implements Set {
   public CachedSet(Set delegate) {
      super(delegate);
   }
}
