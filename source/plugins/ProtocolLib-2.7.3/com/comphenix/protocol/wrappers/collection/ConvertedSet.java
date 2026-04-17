package com.comphenix.protocol.wrappers.collection;

import java.util.Collection;
import java.util.Set;

public abstract class ConvertedSet extends ConvertedCollection implements Set {
   public ConvertedSet(Collection inner) {
      super(inner);
   }
}
