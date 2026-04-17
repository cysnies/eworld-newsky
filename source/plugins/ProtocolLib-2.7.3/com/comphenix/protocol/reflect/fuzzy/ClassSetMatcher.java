package com.comphenix.protocol.reflect.fuzzy;

import com.google.common.base.Objects;
import java.util.Set;

class ClassSetMatcher extends AbstractFuzzyMatcher {
   private final Set classes;

   public ClassSetMatcher(Set classes) {
      if (classes == null) {
         throw new IllegalArgumentException("Set of classes cannot be NULL.");
      } else {
         this.classes = classes;
      }
   }

   public boolean isMatch(Class value, Object parent) {
      return this.classes.contains(value);
   }

   protected int calculateRoundNumber() {
      int roundNumber = 0;

      for(Class clazz : this.classes) {
         roundNumber = this.combineRounds(roundNumber, -ClassExactMatcher.getClassNumber(clazz));
      }

      return roundNumber;
   }

   public String toString() {
      return "match any: " + this.classes;
   }

   public int hashCode() {
      return this.classes.hashCode();
   }

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof ClassSetMatcher ? Objects.equal(this.classes, ((ClassSetMatcher)obj).classes) : true;
      }
   }
}
