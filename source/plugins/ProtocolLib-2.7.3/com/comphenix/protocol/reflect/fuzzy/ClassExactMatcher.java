package com.comphenix.protocol.reflect.fuzzy;

import com.google.common.base.Objects;

class ClassExactMatcher extends AbstractFuzzyMatcher {
   public static final ClassExactMatcher MATCH_ALL;
   private final Class matcher;
   private final Options option;

   ClassExactMatcher(Class matcher, Options option) {
      this.matcher = matcher;
      this.option = option;
   }

   public boolean isMatch(Class input, Object parent) {
      if (input == null) {
         throw new IllegalArgumentException("Input class cannot be NULL.");
      } else if (this.matcher == null) {
         return this.option != ClassExactMatcher.Options.MATCH_EXACT;
      } else if (this.option == ClassExactMatcher.Options.MATCH_SUPER) {
         return input.isAssignableFrom(this.matcher);
      } else {
         return this.option == ClassExactMatcher.Options.MATCH_DERIVED ? this.matcher.isAssignableFrom(input) : input.equals(this.matcher);
      }
   }

   protected int calculateRoundNumber() {
      return -getClassNumber(this.matcher);
   }

   public static int getClassNumber(Class clazz) {
      int count;
      for(count = 0; clazz != null; clazz = clazz.getSuperclass()) {
         ++count;
      }

      return count;
   }

   public Class getMatcher() {
      return this.matcher;
   }

   public Options getOptions() {
      return this.option;
   }

   public String toString() {
      if (this.option == ClassExactMatcher.Options.MATCH_SUPER) {
         return this.matcher + " instanceof input";
      } else {
         return this.option == ClassExactMatcher.Options.MATCH_DERIVED ? "input instanceof " + this.matcher : "Exact " + this.matcher;
      }
   }

   public int hashCode() {
      return Objects.hashCode(new Object[]{this.matcher, this.option});
   }

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (!(obj instanceof ClassExactMatcher)) {
         return false;
      } else {
         ClassExactMatcher other = (ClassExactMatcher)obj;
         return Objects.equal(this.matcher, other.matcher) && Objects.equal(this.option, other.option);
      }
   }

   static {
      MATCH_ALL = new ClassExactMatcher((Class)null, ClassExactMatcher.Options.MATCH_SUPER);
   }

   static enum Options {
      MATCH_EXACT,
      MATCH_SUPER,
      MATCH_DERIVED;
   }
}
