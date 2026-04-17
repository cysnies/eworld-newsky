package fr.neatmonster.nocheatplus.utilities.ds.prefixtree;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrefixTree {
   protected final NodeFactory nodeFactory;
   protected final LookupEntryFactory resultFactory;
   protected Node root;
   protected boolean visit;

   public PrefixTree(NodeFactory nodeFactory, LookupEntryFactory resultFactory) {
      this.nodeFactory = nodeFactory;
      this.root = nodeFactory.newNode((Node)null);
      this.resultFactory = resultFactory;
   }

   public LookupEntry lookup(Object[] keys) {
      return this.lookup(keys, false);
   }

   public LookupEntry lookup(List keys) {
      return this.lookup(keys, false);
   }

   public LookupEntry lookup(Object[] keys, boolean create) {
      return this.lookup(Arrays.asList(keys), create);
   }

   public LookupEntry lookup(List keys, boolean create) {
      boolean visit = this.visit;
      N insertion = (N)this.root;
      int depth = 0;
      N current = (N)this.root;
      boolean hasPrefix = false;

      for(Object key : keys) {
         N child = (N)current.getChild(key);
         if (child == null) {
            if (!create) {
               break;
            }

            N temp = (N)this.nodeFactory.newNode(current);
            current = (N)current.putChild(key, temp);
         } else {
            current = child;
            insertion = child;
            ++depth;
            if (child.isEnd) {
               hasPrefix = true;
            }

            if (visit) {
               this.visit(child);
            }
         }
      }

      N node = (N)null;
      if (create) {
         node = current;
         current.isEnd = true;
      } else if (depth == keys.size()) {
         node = current;
      }

      L result = (L)this.resultFactory.newLookupEntry(node, insertion, depth, hasPrefix);
      this.decorate(result);
      return result;
   }

   protected void visit(Node node) {
   }

   protected void decorate(LookupEntry result) {
   }

   public boolean feed(List keys) {
      L result = (L)this.lookup(keys, true);
      return result.insertion == result.node;
   }

   public boolean feed(Object[] keys) {
      return this.feed(Arrays.asList(keys));
   }

   public boolean hasPrefix(List keys) {
      return this.lookup(keys, false).hasPrefix;
   }

   public boolean hasPrefix(Object[] keys) {
      return this.hasPrefix(Arrays.asList(keys));
   }

   public boolean isPrefix(List keys) {
      return this.lookup(keys, false).depth == keys.size();
   }

   public boolean isPrefix(Object[] keys) {
      return this.isPrefix(Arrays.asList(keys));
   }

   public boolean matches(List keys) {
      L result = (L)this.lookup(keys, false);
      return result.node == result.insertion && result.insertion.isEnd;
   }

   public boolean matches(Object[] keys) {
      return this.matches(Arrays.asList(keys));
   }

   public void clear() {
      this.root = this.nodeFactory.newNode((Node)null);
   }

   public static PrefixTree newPrefixTree() {
      return new PrefixTree(new NodeFactory() {
         public final SimpleNode newNode(SimpleNode parent) {
            return new SimpleNode();
         }
      }, new LookupEntryFactory() {
         public final LookupEntry newLookupEntry(SimpleNode node, SimpleNode insertion, int depth, boolean hasPrefix) {
            return new LookupEntry(node, insertion, depth, hasPrefix);
         }
      });
   }

   public static class Node {
      protected int minCap = 4;
      public boolean isEnd = false;
      public Map children = null;

      public Node getChild(Object key) {
         return this.children == null ? null : (Node)this.children.get(key);
      }

      public Node putChild(Object key, Node child) {
         if (this.children == null) {
            this.children = new HashMap(this.minCap);
         }

         this.children.put(key, child);
         return child;
      }
   }

   public static class SimpleNode extends Node {
   }

   public static class LookupEntry {
      public final Node node;
      public final Node insertion;
      public final int depth;
      public final boolean hasPrefix;

      public LookupEntry(Node node, Node insertion, int depth, boolean hasPrefix) {
         this.node = node;
         this.insertion = insertion;
         this.depth = depth;
         this.hasPrefix = hasPrefix;
      }
   }

   public interface LookupEntryFactory {
      LookupEntry newLookupEntry(Node var1, Node var2, int var3, boolean var4);
   }

   public interface NodeFactory {
      Node newNode(Node var1);
   }
}
