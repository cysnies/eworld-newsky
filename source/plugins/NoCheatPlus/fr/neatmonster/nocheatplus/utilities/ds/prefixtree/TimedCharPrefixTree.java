package fr.neatmonster.nocheatplus.utilities.ds.prefixtree;

import java.util.Arrays;
import java.util.List;

public class TimedCharPrefixTree extends CharPrefixTree {
   protected long ts;
   protected long[] timeInsertion = new long[200];
   protected final boolean access;
   protected boolean updateTime = false;
   protected int depth;
   protected float arrayGrowth = 1.3F;

   public TimedCharPrefixTree(PrefixTree.NodeFactory nodeFactory, PrefixTree.LookupEntryFactory resultFactory, boolean access) {
      super(nodeFactory, resultFactory);
      this.visit = true;
      this.access = access;
   }

   public TimedCharLookupEntry lookup(List keys, boolean create) {
      this.ts = System.currentTimeMillis();
      this.updateTime = this.access || create;
      this.depth = 0;
      return (TimedCharLookupEntry)super.lookup((List)keys, create);
   }

   protected void visit(TimedCharNode node) {
      if (this.depth == this.timeInsertion.length) {
         this.timeInsertion = Arrays.copyOf(this.timeInsertion, (int)((float)this.timeInsertion.length * this.arrayGrowth));
      }

      this.timeInsertion[this.depth] = node.ts;
      if (this.updateTime) {
         node.ts = this.ts;
      }

      ++this.depth;
   }

   protected void decorate(TimedCharLookupEntry result) {
      result.timeInsertion = Arrays.copyOf(this.timeInsertion, this.depth);
   }

   public static TimedCharPrefixTree newTimedCharPrefixTree(boolean access) {
      return new TimedCharPrefixTree(new PrefixTree.NodeFactory() {
         public final SimpleTimedCharNode newNode(SimpleTimedCharNode parent) {
            long ts;
            if (parent == null) {
               ts = System.currentTimeMillis();
            } else {
               ts = parent.ts;
            }

            return new SimpleTimedCharNode(ts);
         }
      }, new PrefixTree.LookupEntryFactory() {
         public final TimedCharLookupEntry newLookupEntry(SimpleTimedCharNode node, SimpleTimedCharNode insertion, int depth, boolean hasPrefix) {
            return new TimedCharLookupEntry(node, insertion, depth, hasPrefix);
         }
      }, access);
   }

   public static class TimedCharLookupEntry extends CharPrefixTree.CharLookupEntry {
      public long[] timeInsertion = null;

      public TimedCharLookupEntry(TimedCharNode node, TimedCharNode insertion, int depth, boolean hasPrefix) {
         super(node, insertion, depth, hasPrefix);
      }
   }

   public static class TimedCharNode extends CharPrefixTree.CharNode {
      public long ts = 0L;

      public TimedCharNode(long ts) {
         this.ts = ts;
      }
   }

   public static class SimpleTimedCharNode extends TimedCharNode {
      public SimpleTimedCharNode(long ts) {
         super(ts);
      }
   }
}
