package com.comphenix.net.sf.cglib.core;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class DuplicatesPredicate implements Predicate {
   private Set unique = new HashSet();

   public boolean evaluate(Object arg) {
      return this.unique.add(MethodWrapper.create((Method)arg));
   }
}
