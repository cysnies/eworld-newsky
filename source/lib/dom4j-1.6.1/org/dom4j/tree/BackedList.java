package org.dom4j.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.dom4j.IllegalAddException;
import org.dom4j.Node;

public class BackedList extends ArrayList {
   private List branchContent;
   private AbstractBranch branch;

   public BackedList(AbstractBranch branch, List branchContent) {
      this(branch, branchContent, branchContent.size());
   }

   public BackedList(AbstractBranch branch, List branchContent, int capacity) {
      super(capacity);
      this.branch = branch;
      this.branchContent = branchContent;
   }

   public BackedList(AbstractBranch branch, List branchContent, List initialContent) {
      super(initialContent);
      this.branch = branch;
      this.branchContent = branchContent;
   }

   public boolean add(Object object) {
      this.branch.addNode(this.asNode(object));
      return super.add(object);
   }

   public void add(int index, Object object) {
      int size = this.size();
      if (index < 0) {
         throw new IndexOutOfBoundsException("Index value: " + index + " is less than zero");
      } else if (index > size) {
         throw new IndexOutOfBoundsException("Index value: " + index + " cannot be greater than " + "the size: " + size);
      } else {
         int realIndex;
         if (size == 0) {
            realIndex = this.branchContent.size();
         } else if (index < size) {
            realIndex = this.branchContent.indexOf(this.get(index));
         } else {
            realIndex = this.branchContent.indexOf(this.get(size - 1)) + 1;
         }

         this.branch.addNode(realIndex, this.asNode(object));
         super.add(index, object);
      }
   }

   public Object set(int index, Object object) {
      int realIndex = this.branchContent.indexOf(this.get(index));
      if (realIndex < 0) {
         realIndex = index == 0 ? 0 : Integer.MAX_VALUE;
      }

      if (realIndex < this.branchContent.size()) {
         this.branch.removeNode(this.asNode(this.get(index)));
         this.branch.addNode(realIndex, this.asNode(object));
      } else {
         this.branch.removeNode(this.asNode(this.get(index)));
         this.branch.addNode(this.asNode(object));
      }

      this.branch.childAdded(this.asNode(object));
      return super.set(index, object);
   }

   public boolean remove(Object object) {
      this.branch.removeNode(this.asNode(object));
      return super.remove(object);
   }

   public Object remove(int index) {
      Object object = super.remove(index);
      if (object != null) {
         this.branch.removeNode(this.asNode(object));
      }

      return object;
   }

   public boolean addAll(Collection collection) {
      this.ensureCapacity(this.size() + collection.size());
      int count = this.size();

      for(Iterator iter = collection.iterator(); iter.hasNext(); --count) {
         this.add(iter.next());
      }

      return count != 0;
   }

   public boolean addAll(int index, Collection collection) {
      this.ensureCapacity(this.size() + collection.size());
      int count = this.size();

      for(Iterator iter = collection.iterator(); iter.hasNext(); --count) {
         this.add(index++, iter.next());
      }

      return count != 0;
   }

   public void clear() {
      for(Object object : this) {
         this.branchContent.remove(object);
         this.branch.childRemoved(this.asNode(object));
      }

      super.clear();
   }

   public void addLocal(Object object) {
      super.add(object);
   }

   protected Node asNode(Object object) {
      if (object instanceof Node) {
         return (Node)object;
      } else {
         throw new IllegalAddException("This list must contain instances of Node. Invalid type: " + object);
      }
   }
}
