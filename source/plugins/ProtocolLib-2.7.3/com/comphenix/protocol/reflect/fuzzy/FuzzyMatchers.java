package com.comphenix.protocol.reflect.fuzzy;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import java.lang.reflect.Member;
import java.util.Set;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;

public class FuzzyMatchers {
   private static AbstractFuzzyMatcher MATCH_ALL = new AbstractFuzzyMatcher() {
      public boolean isMatch(Class value, Object parent) {
         return true;
      }

      protected int calculateRoundNumber() {
         return 0;
      }
   };

   private FuzzyMatchers() {
   }

   public static AbstractFuzzyMatcher matchArray(@Nonnull final AbstractFuzzyMatcher componentMatcher) {
      Preconditions.checkNotNull(componentMatcher, "componentMatcher cannot be NULL.");
      return new AbstractFuzzyMatcher() {
         public boolean isMatch(Class value, Object parent) {
            return value.isArray() && componentMatcher.isMatch(value.getComponentType(), parent);
         }

         protected int calculateRoundNumber() {
            return -1;
         }
      };
   }

   public static AbstractFuzzyMatcher matchAll() {
      return MATCH_ALL;
   }

   public static AbstractFuzzyMatcher matchExact(Class matcher) {
      return new ClassExactMatcher(matcher, ClassExactMatcher.Options.MATCH_EXACT);
   }

   public static AbstractFuzzyMatcher matchAnyOf(Class... classes) {
      return matchAnyOf((Set)Sets.newHashSet(classes));
   }

   public static AbstractFuzzyMatcher matchAnyOf(Set classes) {
      return new ClassSetMatcher(classes);
   }

   public static AbstractFuzzyMatcher matchSuper(Class matcher) {
      return new ClassExactMatcher(matcher, ClassExactMatcher.Options.MATCH_SUPER);
   }

   public static AbstractFuzzyMatcher matchDerived(Class matcher) {
      return new ClassExactMatcher(matcher, ClassExactMatcher.Options.MATCH_DERIVED);
   }

   public static AbstractFuzzyMatcher matchRegex(Pattern regex, int priority) {
      return new ClassRegexMatcher(regex, priority);
   }

   public static AbstractFuzzyMatcher matchRegex(String regex, int priority) {
      return matchRegex(Pattern.compile(regex), priority);
   }

   public static AbstractFuzzyMatcher matchParent() {
      return new AbstractFuzzyMatcher() {
         public boolean isMatch(Class value, Object parent) {
            if (parent instanceof Member) {
               return ((Member)parent).getDeclaringClass().equals(value);
            } else {
               return parent instanceof Class ? parent.equals(value) : false;
            }
         }

         protected int calculateRoundNumber() {
            return -100;
         }

         public String toString() {
            return "match parent class";
         }

         public int hashCode() {
            return 0;
         }

         public boolean equals(Object obj) {
            return obj != null && obj.getClass() == this.getClass();
         }
      };
   }

   static boolean checkPattern(Pattern a, Pattern b) {
      if (a == null) {
         return b == null;
      } else {
         return b == null ? false : a.pattern().equals(b.pattern());
      }
   }
}
