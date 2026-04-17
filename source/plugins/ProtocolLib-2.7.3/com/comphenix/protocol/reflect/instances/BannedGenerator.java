package com.comphenix.protocol.reflect.instances;

import com.comphenix.protocol.reflect.fuzzy.AbstractFuzzyMatcher;
import com.comphenix.protocol.reflect.fuzzy.FuzzyMatchers;
import javax.annotation.Nullable;

public class BannedGenerator implements InstanceProvider {
   private AbstractFuzzyMatcher classMatcher;

   public BannedGenerator(AbstractFuzzyMatcher classMatcher) {
      this.classMatcher = classMatcher;
   }

   public BannedGenerator(Class... classes) {
      this.classMatcher = FuzzyMatchers.matchAnyOf(classes);
   }

   public Object create(@Nullable Class type) {
      if (this.classMatcher.isMatch(type, (Object)null)) {
         throw new NotConstructableException();
      } else {
         return null;
      }
   }
}
