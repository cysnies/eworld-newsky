package fr.neatmonster.nocheatplus.utilities.ds.bktree;

public class TimedBKLevenshtein extends BKLevenshtein {
   public TimedBKLevenshtein(BKModTree.NodeFactory nodeFactory, BKModTree.LookupEntryFactory resultFactory) {
      super(nodeFactory, resultFactory);
   }

   public static class TimedLevenNode extends BKLevenshtein.LevenNode {
      public long ts;

      public TimedLevenNode(char[] value) {
         super(value);
         this.ts = System.currentTimeMillis();
      }

      public TimedLevenNode(char[] value, long ts) {
         super(value);
         this.ts = ts;
      }
   }

   public static class SimpleTimedLevenNode extends TimedLevenNode {
      public SimpleTimedLevenNode(char[] value) {
         super(value);
      }

      public SimpleTimedLevenNode(char[] value, long ts) {
         super(value, ts);
      }
   }
}
