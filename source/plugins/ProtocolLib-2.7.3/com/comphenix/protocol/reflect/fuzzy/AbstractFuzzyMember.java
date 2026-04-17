package com.comphenix.protocol.reflect.fuzzy;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import java.lang.reflect.Member;
import java.util.Map;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;

public abstract class AbstractFuzzyMember extends AbstractFuzzyMatcher {
   protected int modifiersRequired;
   protected int modifiersBanned;
   protected Pattern nameRegex;
   protected AbstractFuzzyMatcher declaringMatcher;
   protected transient boolean sealed;

   protected AbstractFuzzyMember() {
      this.declaringMatcher = ClassExactMatcher.MATCH_ALL;
   }

   protected void prepareBuild() {
   }

   protected AbstractFuzzyMember(AbstractFuzzyMember other) {
      this.declaringMatcher = ClassExactMatcher.MATCH_ALL;
      this.modifiersRequired = other.modifiersRequired;
      this.modifiersBanned = other.modifiersBanned;
      this.nameRegex = other.nameRegex;
      this.declaringMatcher = other.declaringMatcher;
      this.sealed = true;
   }

   public int getModifiersRequired() {
      return this.modifiersRequired;
   }

   public int getModifiersBanned() {
      return this.modifiersBanned;
   }

   public Pattern getNameRegex() {
      return this.nameRegex;
   }

   public AbstractFuzzyMatcher getDeclaringMatcher() {
      return this.declaringMatcher;
   }

   public boolean isMatch(Member value, Object parent) {
      int mods = value.getModifiers();
      return (mods & this.modifiersRequired) == this.modifiersRequired && (mods & this.modifiersBanned) == 0 && this.declaringMatcher.isMatch(value.getDeclaringClass(), value) && this.isNameMatch(value.getName());
   }

   private boolean isNameMatch(String name) {
      return this.nameRegex == null ? true : this.nameRegex.matcher(name).matches();
   }

   protected int calculateRoundNumber() {
      if (!this.sealed) {
         throw new IllegalStateException("Cannot calculate round number during construction.");
      } else {
         return this.declaringMatcher.getRoundNumber();
      }
   }

   public String toString() {
      return this.getKeyValueView().toString();
   }

   protected Map getKeyValueView() {
      Map<String, Object> map = Maps.newLinkedHashMap();
      if (this.modifiersRequired != Integer.MAX_VALUE || this.modifiersBanned != 0) {
         map.put("modifiers", String.format("[required: %s, banned: %s]", getBitView(this.modifiersRequired, 16), getBitView(this.modifiersBanned, 16)));
      }

      if (this.nameRegex != null) {
         map.put("name", this.nameRegex.pattern());
      }

      if (this.declaringMatcher != ClassExactMatcher.MATCH_ALL) {
         map.put("declaring", this.declaringMatcher);
      }

      return map;
   }

   private static String getBitView(int value, int bits) {
      if (bits >= 0 && bits <= 31) {
         int snipped = value & (1 << bits) - 1;
         return Integer.toBinaryString(snipped);
      } else {
         throw new IllegalArgumentException("Bits must be a value between 0 and 32");
      }
   }

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (!(obj instanceof AbstractFuzzyMember)) {
         return false;
      } else {
         AbstractFuzzyMember<T> other = (AbstractFuzzyMember)obj;
         return this.modifiersBanned == other.modifiersBanned && this.modifiersRequired == other.modifiersRequired && FuzzyMatchers.checkPattern(this.nameRegex, other.nameRegex) && Objects.equal(this.declaringMatcher, other.declaringMatcher);
      }
   }

   public int hashCode() {
      return Objects.hashCode(new Object[]{this.modifiersBanned, this.modifiersRequired, this.nameRegex != null ? this.nameRegex.pattern() : null, this.declaringMatcher});
   }

   public abstract static class Builder {
      protected AbstractFuzzyMember member = this.initialMember();

      public Builder requireModifier(int modifier) {
         AbstractFuzzyMember var10000 = this.member;
         var10000.modifiersRequired |= modifier;
         return this;
      }

      public Builder requirePublic() {
         return this.requireModifier(1);
      }

      public Builder banModifier(int modifier) {
         AbstractFuzzyMember var10000 = this.member;
         var10000.modifiersBanned |= modifier;
         return this;
      }

      public Builder nameRegex(String regex) {
         this.member.nameRegex = Pattern.compile(regex);
         return this;
      }

      public Builder nameRegex(Pattern pattern) {
         this.member.nameRegex = pattern;
         return this;
      }

      public Builder nameExact(String name) {
         return this.nameRegex(Pattern.quote(name));
      }

      public Builder declaringClassExactType(Class declaringClass) {
         this.member.declaringMatcher = FuzzyMatchers.matchExact(declaringClass);
         return this;
      }

      public Builder declaringClassSuperOf(Class declaringClass) {
         this.member.declaringMatcher = FuzzyMatchers.matchSuper(declaringClass);
         return this;
      }

      public Builder declaringClassDerivedOf(Class declaringClass) {
         this.member.declaringMatcher = FuzzyMatchers.matchDerived(declaringClass);
         return this;
      }

      public Builder declaringClassMatching(AbstractFuzzyMatcher classMatcher) {
         this.member.declaringMatcher = classMatcher;
         return this;
      }

      @Nonnull
      protected abstract AbstractFuzzyMember initialMember();

      public abstract AbstractFuzzyMember build();
   }
}
