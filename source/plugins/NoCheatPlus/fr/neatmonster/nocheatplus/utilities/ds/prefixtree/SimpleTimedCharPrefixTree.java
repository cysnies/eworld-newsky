package fr.neatmonster.nocheatplus.utilities.ds.prefixtree;

public class SimpleTimedCharPrefixTree extends TimedCharPrefixTree {
   public SimpleTimedCharPrefixTree(boolean access) {
      super(new PrefixTree.NodeFactory() {
         public final TimedCharPrefixTree.SimpleTimedCharNode newNode(TimedCharPrefixTree.SimpleTimedCharNode parent) {
            long ts;
            if (parent == null) {
               ts = System.currentTimeMillis();
            } else {
               ts = parent.ts;
            }

            return new TimedCharPrefixTree.SimpleTimedCharNode(ts);
         }
      }, new PrefixTree.LookupEntryFactory() {
         public final SimpleTimedCharLookupEntry newLookupEntry(TimedCharPrefixTree.SimpleTimedCharNode node, TimedCharPrefixTree.SimpleTimedCharNode insertion, int depth, boolean hasPrefix) {
            return new SimpleTimedCharLookupEntry(node, insertion, depth, hasPrefix);
         }
      }, access);
   }

   public static class SimpleTimedCharLookupEntry extends TimedCharPrefixTree.TimedCharLookupEntry {
      public SimpleTimedCharLookupEntry(TimedCharPrefixTree.SimpleTimedCharNode node, TimedCharPrefixTree.SimpleTimedCharNode insertion, int depth, boolean hasPrefix) {
         super(node, insertion, depth, hasPrefix);
      }
   }
}
