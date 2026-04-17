package com.comphenix.protocol.reflect.fuzzy;

import com.google.common.base.Objects;
import java.util.regex.Pattern;

class ClassRegexMatcher extends AbstractFuzzyMatcher {
   private final Pattern regex;
   private final int priority;

   public ClassRegexMatcher(Pattern regex, int priority) {
      if (regex == null) {
         throw new IllegalArgumentException("Regular expression pattern cannot be NULL.");
      } else {
         this.regex = regex;
         this.priority = priority;
      }
   }

   public boolean isMatch(Class value, Object parent) {
      return value != null ? this.regex.matcher(value.getCanonicalName()).matches() : false;
   }

   protected int calculateRoundNumber() {
      return -this.priority;
   }

   public String toString() {
      return "class name of " + this.regex.toString();
   }

   public int hashCode() {
      return Objects.hashCode(new Object[]{this.regex, this.priority});
   }

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (!(obj instanceof ClassRegexMatcher)) {
         return false;
      } else {
         ClassRegexMatcher other = (ClassRegexMatcher)obj;
         return this.priority == other.priority && FuzzyMatchers.checkPattern(this.regex, other.regex);
      }
   }
}
