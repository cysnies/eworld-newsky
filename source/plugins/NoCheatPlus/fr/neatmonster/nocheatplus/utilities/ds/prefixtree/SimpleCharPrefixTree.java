package fr.neatmonster.nocheatplus.utilities.ds.prefixtree;

public class SimpleCharPrefixTree extends CharPrefixTree {
   public SimpleCharPrefixTree() {
      super(new PrefixTree.NodeFactory() {
         public final CharPrefixTree.SimpleCharNode newNode(CharPrefixTree.SimpleCharNode parent) {
            return new CharPrefixTree.SimpleCharNode();
         }
      }, new PrefixTree.LookupEntryFactory() {
         public final SimpleCharLookupEntry newLookupEntry(CharPrefixTree.SimpleCharNode node, CharPrefixTree.SimpleCharNode insertion, int depth, boolean hasPrefix) {
            return new SimpleCharLookupEntry(node, insertion, depth, hasPrefix);
         }
      });
   }

   public static class SimpleCharLookupEntry extends CharPrefixTree.CharLookupEntry {
      public SimpleCharLookupEntry(CharPrefixTree.SimpleCharNode node, CharPrefixTree.SimpleCharNode insertion, int depth, boolean hasPrefix) {
         super(node, insertion, depth, hasPrefix);
      }
   }
}
