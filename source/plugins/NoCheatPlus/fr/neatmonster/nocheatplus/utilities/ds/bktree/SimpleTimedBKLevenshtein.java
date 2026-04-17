package fr.neatmonster.nocheatplus.utilities.ds.bktree;

import java.util.Collection;

public class SimpleTimedBKLevenshtein extends TimedBKLevenshtein {
   public SimpleTimedBKLevenshtein() {
      super(new BKModTree.NodeFactory() {
         public TimedBKLevenshtein.SimpleTimedLevenNode newNode(char[] value, TimedBKLevenshtein.SimpleTimedLevenNode parent) {
            return new TimedBKLevenshtein.SimpleTimedLevenNode(value);
         }
      }, new BKModTree.LookupEntryFactory() {
         public STBKLResult newLookupEntry(Collection nodes, TimedBKLevenshtein.SimpleTimedLevenNode match, int distance, boolean isNew) {
            return new STBKLResult(nodes, match, distance, isNew);
         }
      });
   }

   public static class STBKLResult extends BKModTree.LookupEntry {
      public STBKLResult(Collection nodes, TimedBKLevenshtein.SimpleTimedLevenNode match, int distance, boolean isNew) {
         super(nodes, match, distance, isNew);
      }
   }
}
