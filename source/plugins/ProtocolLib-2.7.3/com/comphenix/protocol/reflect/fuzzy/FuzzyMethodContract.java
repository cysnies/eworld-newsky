package com.comphenix.protocol.reflect.fuzzy;

import com.comphenix.protocol.reflect.MethodInfo;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.lang.reflect.Member;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;

public class FuzzyMethodContract extends AbstractFuzzyMember {
   private AbstractFuzzyMatcher returnMatcher;
   private List paramMatchers;
   private List exceptionMatchers;
   private Integer paramCount;

   public static Builder newBuilder() {
      return new Builder();
   }

   private FuzzyMethodContract() {
      this.returnMatcher = ClassExactMatcher.MATCH_ALL;
      this.paramMatchers = Lists.newArrayList();
      this.exceptionMatchers = Lists.newArrayList();
   }

   private FuzzyMethodContract(FuzzyMethodContract other) {
      super(other);
      this.returnMatcher = ClassExactMatcher.MATCH_ALL;
      this.returnMatcher = other.returnMatcher;
      this.paramMatchers = other.paramMatchers;
      this.exceptionMatchers = other.exceptionMatchers;
      this.paramCount = other.paramCount;
   }

   private static FuzzyMethodContract immutableCopy(FuzzyMethodContract other) {
      FuzzyMethodContract copy = new FuzzyMethodContract(other);
      copy.paramMatchers = ImmutableList.copyOf(copy.paramMatchers);
      copy.exceptionMatchers = ImmutableList.copyOf(copy.exceptionMatchers);
      return copy;
   }

   public AbstractFuzzyMatcher getReturnMatcher() {
      return this.returnMatcher;
   }

   public ImmutableList getParamMatchers() {
      if (this.paramMatchers instanceof ImmutableList) {
         return (ImmutableList)this.paramMatchers;
      } else {
         throw new IllegalStateException("Lists haven't been sealed yet.");
      }
   }

   public List getExceptionMatchers() {
      if (this.exceptionMatchers instanceof ImmutableList) {
         return this.exceptionMatchers;
      } else {
         throw new IllegalStateException("Lists haven't been sealed yet.");
      }
   }

   public Integer getParamCount() {
      return this.paramCount;
   }

   protected void prepareBuild() {
      super.prepareBuild();
      Collections.sort(this.paramMatchers);
      Collections.sort(this.exceptionMatchers);
   }

   public boolean isMatch(MethodInfo value, Object parent) {
      if (!super.isMatch((Member)value, parent)) {
         return false;
      } else {
         Class<?>[] params = value.getParameterTypes();
         Class<?>[] exceptions = value.getExceptionTypes();
         if (!this.returnMatcher.isMatch(value.getReturnType(), value)) {
            return false;
         } else if (this.paramCount != null && this.paramCount != value.getParameterTypes().length) {
            return false;
         } else {
            return this.matchParameters(params, value, this.paramMatchers) && this.matchParameters(exceptions, value, this.exceptionMatchers);
         }
      }
   }

   private boolean matchParameters(Class[] types, MethodInfo parent, List matchers) {
      boolean[] accepted = new boolean[matchers.size()];
      int count = accepted.length;

      for(int i = 0; i < types.length; ++i) {
         int matcherIndex = this.processValue(types[i], parent, i, accepted, matchers);
         if (matcherIndex >= 0) {
            accepted[matcherIndex] = true;
            --count;
         }

         if (count == 0) {
            return true;
         }
      }

      return count == 0;
   }

   private int processValue(Class value, MethodInfo parent, int index, boolean[] accepted, List matchers) {
      for(int i = 0; i < matchers.size(); ++i) {
         if (!accepted[i] && ((ParameterClassMatcher)matchers.get(i)).isParameterMatch(value, parent, index)) {
            return i;
         }
      }

      return -1;
   }

   protected int calculateRoundNumber() {
      int current = 0;
      current = this.returnMatcher.getRoundNumber();

      for(ParameterClassMatcher matcher : this.paramMatchers) {
         current = this.combineRounds(current, matcher.calculateRoundNumber());
      }

      for(ParameterClassMatcher matcher : this.exceptionMatchers) {
         current = this.combineRounds(current, matcher.calculateRoundNumber());
      }

      return this.combineRounds(super.calculateRoundNumber(), current);
   }

   protected Map getKeyValueView() {
      Map<String, Object> member = super.getKeyValueView();
      if (this.returnMatcher != ClassExactMatcher.MATCH_ALL) {
         member.put("return", this.returnMatcher);
      }

      if (this.paramMatchers.size() > 0) {
         member.put("params", this.paramMatchers);
      }

      if (this.exceptionMatchers.size() > 0) {
         member.put("exceptions", this.exceptionMatchers);
      }

      if (this.paramCount != null) {
         member.put("paramCount", this.paramCount);
      }

      return member;
   }

   public int hashCode() {
      return Objects.hashCode(new Object[]{this.returnMatcher, this.paramMatchers, this.exceptionMatchers, this.paramCount, super.hashCode()});
   }

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj instanceof FuzzyMethodContract && super.equals(obj)) {
         FuzzyMethodContract other = (FuzzyMethodContract)obj;
         return Objects.equal(this.paramCount, other.paramCount) && Objects.equal(this.returnMatcher, other.returnMatcher) && Objects.equal(this.paramMatchers, other.paramMatchers) && Objects.equal(this.exceptionMatchers, other.exceptionMatchers);
      } else {
         return true;
      }
   }

   private static class ParameterClassMatcher extends AbstractFuzzyMatcher {
      private final AbstractFuzzyMatcher typeMatcher;
      private final Integer indexMatch;

      public ParameterClassMatcher(@Nonnull AbstractFuzzyMatcher typeMatcher) {
         this(typeMatcher, (Integer)null);
      }

      public ParameterClassMatcher(@Nonnull AbstractFuzzyMatcher typeMatcher, Integer indexMatch) {
         if (typeMatcher == null) {
            throw new IllegalArgumentException("Type matcher cannot be NULL.");
         } else {
            this.typeMatcher = typeMatcher;
            this.indexMatch = indexMatch;
         }
      }

      public boolean isParameterMatch(Class param, MethodInfo parent, int index) {
         return this.indexMatch != null && this.indexMatch != index ? false : this.typeMatcher.isMatch(param, parent);
      }

      public boolean isMatch(Class[] value, Object parent) {
         throw new UnsupportedOperationException("Use the parameter match instead.");
      }

      protected int calculateRoundNumber() {
         return this.typeMatcher.getRoundNumber();
      }

      public String toString() {
         return String.format("{Type: %s, Index: %s}", this.typeMatcher, this.indexMatch);
      }
   }

   public static class Builder extends AbstractFuzzyMember.Builder {
      public Builder requireModifier(int modifier) {
         super.requireModifier(modifier);
         return this;
      }

      public Builder requirePublic() {
         super.requirePublic();
         return this;
      }

      public Builder banModifier(int modifier) {
         super.banModifier(modifier);
         return this;
      }

      public Builder nameRegex(String regex) {
         super.nameRegex(regex);
         return this;
      }

      public Builder nameRegex(Pattern pattern) {
         super.nameRegex(pattern);
         return this;
      }

      public Builder nameExact(String name) {
         super.nameExact(name);
         return this;
      }

      public Builder declaringClassExactType(Class declaringClass) {
         super.declaringClassExactType(declaringClass);
         return this;
      }

      public Builder declaringClassSuperOf(Class declaringClass) {
         super.declaringClassSuperOf(declaringClass);
         return this;
      }

      public Builder declaringClassDerivedOf(Class declaringClass) {
         super.declaringClassDerivedOf(declaringClass);
         return this;
      }

      public Builder declaringClassMatching(AbstractFuzzyMatcher classMatcher) {
         super.declaringClassMatching(classMatcher);
         return this;
      }

      public Builder parameterExactType(Class type) {
         ((FuzzyMethodContract)this.member).paramMatchers.add(new ParameterClassMatcher(FuzzyMatchers.matchExact(type)));
         return this;
      }

      public Builder parameterSuperOf(Class type) {
         ((FuzzyMethodContract)this.member).paramMatchers.add(new ParameterClassMatcher(FuzzyMatchers.matchSuper(type)));
         return this;
      }

      public Builder parameterDerivedOf(Class type) {
         ((FuzzyMethodContract)this.member).paramMatchers.add(new ParameterClassMatcher(FuzzyMatchers.matchDerived(type)));
         return this;
      }

      public Builder parameterMatches(AbstractFuzzyMatcher classMatcher) {
         ((FuzzyMethodContract)this.member).paramMatchers.add(new ParameterClassMatcher(classMatcher));
         return this;
      }

      public Builder parameterExactType(Class type, int index) {
         ((FuzzyMethodContract)this.member).paramMatchers.add(new ParameterClassMatcher(FuzzyMatchers.matchExact(type), index));
         return this;
      }

      public Builder parameterSuperOf(Class type, int index) {
         ((FuzzyMethodContract)this.member).paramMatchers.add(new ParameterClassMatcher(FuzzyMatchers.matchSuper(type), index));
         return this;
      }

      public Builder parameterDerivedOf(Class type, int index) {
         ((FuzzyMethodContract)this.member).paramMatchers.add(new ParameterClassMatcher(FuzzyMatchers.matchDerived(type), index));
         return this;
      }

      public Builder parameterMatches(AbstractFuzzyMatcher classMatcher, int index) {
         ((FuzzyMethodContract)this.member).paramMatchers.add(new ParameterClassMatcher(classMatcher, index));
         return this;
      }

      public Builder parameterCount(int expectedCount) {
         ((FuzzyMethodContract)this.member).paramCount = expectedCount;
         return this;
      }

      public Builder returnTypeVoid() {
         return this.returnTypeExact(Void.TYPE);
      }

      public Builder returnTypeExact(Class type) {
         ((FuzzyMethodContract)this.member).returnMatcher = FuzzyMatchers.matchExact(type);
         return this;
      }

      public Builder returnDerivedOf(Class type) {
         ((FuzzyMethodContract)this.member).returnMatcher = FuzzyMatchers.matchDerived(type);
         return this;
      }

      public Builder returnTypeMatches(AbstractFuzzyMatcher classMatcher) {
         ((FuzzyMethodContract)this.member).returnMatcher = classMatcher;
         return this;
      }

      public Builder exceptionExactType(Class type) {
         ((FuzzyMethodContract)this.member).exceptionMatchers.add(new ParameterClassMatcher(FuzzyMatchers.matchExact(type)));
         return this;
      }

      public Builder exceptionSuperOf(Class type) {
         ((FuzzyMethodContract)this.member).exceptionMatchers.add(new ParameterClassMatcher(FuzzyMatchers.matchSuper(type)));
         return this;
      }

      public Builder exceptionMatches(AbstractFuzzyMatcher classMatcher) {
         ((FuzzyMethodContract)this.member).exceptionMatchers.add(new ParameterClassMatcher(classMatcher));
         return this;
      }

      public Builder exceptionExactType(Class type, int index) {
         ((FuzzyMethodContract)this.member).exceptionMatchers.add(new ParameterClassMatcher(FuzzyMatchers.matchExact(type), index));
         return this;
      }

      public Builder exceptionSuperOf(Class type, int index) {
         ((FuzzyMethodContract)this.member).exceptionMatchers.add(new ParameterClassMatcher(FuzzyMatchers.matchSuper(type), index));
         return this;
      }

      public Builder exceptionMatches(AbstractFuzzyMatcher classMatcher, int index) {
         ((FuzzyMethodContract)this.member).exceptionMatchers.add(new ParameterClassMatcher(classMatcher, index));
         return this;
      }

      @Nonnull
      protected FuzzyMethodContract initialMember() {
         return new FuzzyMethodContract();
      }

      public FuzzyMethodContract build() {
         ((FuzzyMethodContract)this.member).prepareBuild();
         return FuzzyMethodContract.immutableCopy((FuzzyMethodContract)this.member);
      }
   }
}
