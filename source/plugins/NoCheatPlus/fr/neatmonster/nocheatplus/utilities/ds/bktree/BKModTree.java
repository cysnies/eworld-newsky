package fr.neatmonster.nocheatplus.utilities.ds.bktree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class BKModTree {
   protected final NodeFactory nodeFactory;
   protected final LookupEntryFactory resultFactory;
   protected Node root = null;
   protected boolean visit = false;

   public BKModTree(NodeFactory nodeFactory, LookupEntryFactory resultFactory) {
      this.nodeFactory = nodeFactory;
      this.resultFactory = resultFactory;
   }

   public void clear() {
      this.root = null;
   }

   public LookupEntry lookup(Object value, int range, int seekMax, boolean create) {
      List<N> inRange = new LinkedList();
      if (this.root == null) {
         if (create) {
            this.root = this.nodeFactory.newNode(value, (Node)null);
            return this.resultFactory.newLookupEntry(inRange, this.root, 0, true);
         } else {
            return this.resultFactory.newLookupEntry(inRange, (Node)null, 0, false);
         }
      } else {
         List<N> open = new ArrayList();
         open.add(this.root);
         N insertion = (N)null;
         int insertionDist = 0;

         while(true) {
            N current = (N)((Node)open.remove(open.size() - 1));
            int distance = this.distance(current.value, value);
            if (this.visit) {
               this.visit(current, value, distance);
            }

            if (distance == 0) {
               return this.resultFactory.newLookupEntry(inRange, current, distance, false);
            }

            if (create && insertion == null && !current.hasChild(distance)) {
               insertion = current;
               insertionDist = distance;
            }

            label48: {
               if (Math.abs(distance) <= range) {
                  inRange.add(current);
                  if (seekMax > 0 && inRange.size() >= seekMax && (!create || insertion != null)) {
                     break label48;
                  }
               }

               current.getChildren(distance, range, open);
               if (!open.isEmpty()) {
                  continue;
               }
            }

            if (create && insertion != null) {
               current = (N)this.nodeFactory.newNode(value, insertion);
               insertion.putChild(insertionDist, current);
               return this.resultFactory.newLookupEntry(inRange, current, 0, true);
            }

            return this.resultFactory.newLookupEntry(inRange, (Node)null, 0, false);
         }
      }
   }

   protected void visit(Node node, Object value, int distance) {
   }

   public abstract int distance(Object var1, Object var2);

   public abstract static class Node {
      public Object value;

      public Node(Object value) {
         this.value = value;
      }

      public abstract Node putChild(int var1, Node var2);

      public abstract Node getChild(int var1);

      public abstract boolean hasChild(int var1);

      public abstract Collection getChildren(int var1, int var2, Collection var3);
   }

   public abstract static class MapNode extends Node {
      protected Map children = null;
      protected int maxIterate = 12;

      public MapNode(Object value) {
         super(value);
      }

      public HashMapNode putChild(int distance, HashMapNode child) {
         if (this.children == null) {
            this.children = this.newMap();
         }

         this.children.put(distance, child);
         return child;
      }

      public HashMapNode getChild(int distance) {
         return this.children == null ? null : (HashMapNode)this.children.get(distance);
      }

      public boolean hasChild(int distance) {
         return this.children == null ? false : this.children.containsKey(distance);
      }

      public Collection getChildren(int distance, int range, Collection nodes) {
         if (this.children == null) {
            return nodes;
         } else {
            if (this.children.size() > this.maxIterate) {
               for(int i = distance - range; i < distance + range + 1; ++i) {
                  N child = (N)((HashMapNode)this.children.get(i));
                  if (child != null) {
                     nodes.add(child);
                  }
               }
            } else {
               for(Integer key : this.children.keySet()) {
                  if (Math.abs(distance - key) <= range) {
                     nodes.add(this.children.get(key));
                  }
               }
            }

            return nodes;
         }
      }

      protected abstract Map newMap();
   }

   public static class HashMapNode extends MapNode {
      protected int initialCapacity = 4;
      protected float loadFactor = 0.75F;

      public HashMapNode(Object value) {
         super(value);
      }

      protected Map newMap() {
         return new HashMap(this.initialCapacity, this.loadFactor);
      }
   }

   public static class SimpleNode extends HashMapNode {
      public SimpleNode(Object content) {
         super(content);
      }
   }

   public static class LookupEntry {
      public final Collection nodes;
      public final Node match;
      public final int distance;
      public final boolean isNew;

      public LookupEntry(Collection nodes, Node match, int distance, boolean isNew) {
         this.nodes = nodes;
         this.match = match;
         this.distance = distance;
         this.isNew = isNew;
      }
   }

   public interface LookupEntryFactory {
      LookupEntry newLookupEntry(Collection var1, Node var2, int var3, boolean var4);
   }

   public interface NodeFactory {
      Node newNode(Object var1, Node var2);
   }
}
