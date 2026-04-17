package com.comphenix.protocol.reflect.fuzzy;

import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.MethodInfo;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class FuzzyClassContract extends AbstractFuzzyMatcher {
   private final ImmutableList fieldContracts;
   private final ImmutableList methodContracts;
   private final ImmutableList constructorContracts;
   private final ImmutableList baseclassContracts;
   private final ImmutableList interfaceContracts;

   public static Builder newBuilder() {
      return new Builder();
   }

   private FuzzyClassContract(Builder builder) {
      this.fieldContracts = ImmutableList.copyOf(builder.fieldContracts);
      this.methodContracts = ImmutableList.copyOf(builder.methodContracts);
      this.constructorContracts = ImmutableList.copyOf(builder.constructorContracts);
      this.baseclassContracts = ImmutableList.copyOf(builder.baseclassContracts);
      this.interfaceContracts = ImmutableList.copyOf(builder.interfaceContracts);
   }

   public ImmutableList getFieldContracts() {
      return this.fieldContracts;
   }

   public ImmutableList getMethodContracts() {
      return this.methodContracts;
   }

   public ImmutableList getConstructorContracts() {
      return this.constructorContracts;
   }

   public ImmutableList getBaseclassContracts() {
      return this.baseclassContracts;
   }

   public ImmutableList getInterfaceContracts() {
      return this.interfaceContracts;
   }

   protected int calculateRoundNumber() {
      return this.combineRounds(new Integer[]{this.findHighestRound(this.fieldContracts), this.findHighestRound(this.methodContracts), this.findHighestRound(this.constructorContracts), this.findHighestRound(this.interfaceContracts), this.findHighestRound(this.baseclassContracts)});
   }

   private int findHighestRound(Collection list) {
      int highest = 0;

      for(AbstractFuzzyMatcher matcher : list) {
         highest = this.combineRounds(highest, matcher.getRoundNumber());
      }

      return highest;
   }

   public boolean isMatch(Class value, Object parent) {
      FuzzyReflection reflection = FuzzyReflection.fromClass(value, true);
      return (this.fieldContracts.size() == 0 || this.processContracts(reflection.getFields(), value, this.fieldContracts)) && (this.methodContracts.size() == 0 || this.processContracts(MethodInfo.fromMethods((Collection)reflection.getMethods()), value, this.methodContracts)) && (this.constructorContracts.size() == 0 || this.processContracts(MethodInfo.fromConstructors(value.getDeclaredConstructors()), value, this.constructorContracts)) && (this.baseclassContracts.size() == 0 || this.processValue(value.getSuperclass(), parent, this.baseclassContracts)) && (this.interfaceContracts.size() == 0 || this.processContracts(Arrays.asList(value.getInterfaces()), (Class)parent, this.interfaceContracts));
   }

   private boolean processContracts(Collection values, Object parent, List matchers) {
      boolean[] accepted = new boolean[matchers.size()];
      int count = accepted.length;

      for(Object value : values) {
         int index = this.processValue(value, parent, accepted, matchers);
         if (index >= 0) {
            accepted[index] = true;
            --count;
         }

         if (count == 0) {
            return true;
         }
      }

      return count == 0;
   }

   private boolean processValue(Object value, Object parent, List matchers) {
      for(int i = 0; i < matchers.size(); ++i) {
         if (((AbstractFuzzyMatcher)matchers.get(i)).isMatch(value, parent)) {
            return true;
         }
      }

      return false;
   }

   private int processValue(Object value, Object parent, boolean[] accepted, List matchers) {
      for(int i = 0; i < matchers.size(); ++i) {
         if (!accepted[i]) {
            AbstractFuzzyMatcher<T> matcher = (AbstractFuzzyMatcher)matchers.get(i);
            if (matcher.isMatch(value, parent)) {
               return i;
            }
         }
      }

      return -1;
   }

   public String toString() {
      Map<String, Object> params = Maps.newLinkedHashMap();
      if (this.fieldContracts.size() > 0) {
         params.put("fields", this.fieldContracts);
      }

      if (this.methodContracts.size() > 0) {
         params.put("methods", this.methodContracts);
      }

      if (this.constructorContracts.size() > 0) {
         params.put("constructors", this.constructorContracts);
      }

      if (this.baseclassContracts.size() > 0) {
         params.put("baseclasses", this.baseclassContracts);
      }

      if (this.interfaceContracts.size() > 0) {
         params.put("interfaces", this.interfaceContracts);
      }

      return "{\n  " + Joiner.on(", \n  ").join(params.entrySet()) + "\n}";
   }

   public static class Builder {
      private List fieldContracts = Lists.newArrayList();
      private List methodContracts = Lists.newArrayList();
      private List constructorContracts = Lists.newArrayList();
      private List baseclassContracts = Lists.newArrayList();
      private List interfaceContracts = Lists.newArrayList();

      public Builder field(AbstractFuzzyMatcher matcher) {
         this.fieldContracts.add(matcher);
         return this;
      }

      public Builder field(FuzzyFieldContract.Builder builder) {
         return this.field((AbstractFuzzyMatcher)builder.build());
      }

      public Builder method(AbstractFuzzyMatcher matcher) {
         this.methodContracts.add(matcher);
         return this;
      }

      public Builder method(FuzzyMethodContract.Builder builder) {
         return this.method((AbstractFuzzyMatcher)builder.build());
      }

      public Builder constructor(AbstractFuzzyMatcher matcher) {
         this.constructorContracts.add(matcher);
         return this;
      }

      public Builder constructor(FuzzyMethodContract.Builder builder) {
         return this.constructor((AbstractFuzzyMatcher)builder.build());
      }

      public Builder baseclass(AbstractFuzzyMatcher matcher) {
         this.baseclassContracts.add(matcher);
         return this;
      }

      public Builder baseclass(Builder builder) {
         return this.baseclass((AbstractFuzzyMatcher)builder.build());
      }

      public Builder interfaces(AbstractFuzzyMatcher matcher) {
         this.interfaceContracts.add(matcher);
         return this;
      }

      public Builder interfaces(Builder builder) {
         return this.interfaces((AbstractFuzzyMatcher)builder.build());
      }

      public FuzzyClassContract build() {
         Collections.sort(this.fieldContracts);
         Collections.sort(this.methodContracts);
         Collections.sort(this.constructorContracts);
         Collections.sort(this.baseclassContracts);
         Collections.sort(this.interfaceContracts);
         return new FuzzyClassContract(this);
      }
   }
}
