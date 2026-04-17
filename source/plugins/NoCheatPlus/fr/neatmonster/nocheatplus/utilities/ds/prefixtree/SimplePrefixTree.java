package fr.neatmonster.nocheatplus.utilities.ds.prefixtree;

public class SimplePrefixTree extends PrefixTree {
   public SimplePrefixTree() {
      // $FF: Couldn't be decompiled
   }

   public static class SimpleLookupEntry extends PrefixTree.LookupEntry {
      public SimpleLookupEntry(PrefixTree.SimpleNode node, PrefixTree.SimpleNode insertion, int depth, boolean hasPrefix) {
         super(node, insertion, depth, hasPrefix);
      }
   }
}
