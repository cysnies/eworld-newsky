package com.comphenix.protocol.reflect.fuzzy;

import com.google.common.primitives.Ints;

public abstract class AbstractFuzzyMatcher implements Comparable {
   private Integer roundNumber;

   public abstract boolean isMatch(Object var1, Object var2);

   protected abstract int calculateRoundNumber();

   public final int getRoundNumber() {
      return this.roundNumber == null ? this.roundNumber = this.calculateRoundNumber() : this.roundNumber;
   }

   protected final int combineRounds(int roundA, int roundB) {
      if (roundA == 0) {
         return roundB;
      } else {
         return roundB == 0 ? roundA : Math.max(roundA, roundB);
      }
   }

   protected final int combineRounds(Integer... rounds) {
      if (rounds.length < 2) {
         throw new IllegalArgumentException("Must supply at least two arguments.");
      } else {
         int reduced = this.combineRounds(rounds[0], rounds[1]);

         for(int i = 2; i < rounds.length; ++i) {
            reduced = this.combineRounds(reduced, rounds[i]);
         }

         return reduced;
      }
   }

   public int compareTo(AbstractFuzzyMatcher obj) {
      return obj instanceof AbstractFuzzyMatcher ? Ints.compare(this.getRoundNumber(), obj.getRoundNumber()) : -1;
   }

   public AbstractFuzzyMatcher inverted() {
      // $FF: Couldn't be decompiled
   }

   public AbstractFuzzyMatcher and(AbstractFuzzyMatcher other) {
      // $FF: Couldn't be decompiled
   }

   public AbstractFuzzyMatcher or(AbstractFuzzyMatcher other) {
      // $FF: Couldn't be decompiled
   }
}
