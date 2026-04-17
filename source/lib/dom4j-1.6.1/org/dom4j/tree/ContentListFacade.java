package org.dom4j.tree;

import java.util.AbstractList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.dom4j.IllegalAddException;
import org.dom4j.Node;

public class ContentListFacade extends AbstractList {
   private List branchContent;
   private AbstractBranch branch;

   public ContentListFacade(AbstractBranch branch, List branchContent) {
      this.branch = branch;
      this.branchContent = branchContent;
   }

   public boolean add(Object object) {
      this.branch.childAdded(this.asNode(object));
      return this.branchContent.add(object);
   }

   public void add(int index, Object object) {
      this.branch.childAdded(this.asNode(object));
      this.branchContent.add(index, object);
   }

   public Object set(int index, Object object) {
      this.branch.childAdded(this.asNode(object));
      return this.branchContent.set(index, object);
   }

   public boolean remove(Object object) {
      this.branch.childRemoved(this.asNode(object));
      return this.branchContent.remove(object);
   }

   public Object remove(int index) {
      Object object = this.branchContent.remove(index);
      if (object != null) {
         this.branch.childRemoved(this.asNode(object));
      }

      return object;
   }

   public boolean addAll(Collection collection) {
      int count = this.branchContent.size();

      for(Iterator iter = collection.iterator(); iter.hasNext(); ++count) {
         this.add(iter.next());
      }

      return count == this.branchContent.size();
   }

   public boolean addAll(int index, Collection collection) {
      int count = this.branchContent.size();

      for(Iterator iter = collection.iterator(); iter.hasNext(); --count) {
         this.add(index++, iter.next());
      }

      return count == this.branchContent.size();
   }

   public void clear() {
      for(Object object : this) {
         this.branch.childRemoved(this.asNode(object));
      }

      this.branchContent.clear();
   }

   public boolean removeAll(Collection c) {
      for(Object object : c) {
         this.branch.childRemoved(this.asNode(object));
      }

      return this.branchContent.removeAll(c);
   }

   public int size() {
      return this.branchContent.size();
   }

   public boolean isEmpty() {
      return this.branchContent.isEmpty();
   }

   public boolean contains(Object o) {
      return this.branchContent.contains(o);
   }

   public Object[] toArray() {
      return this.branchContent.toArray();
   }

   public Object[] toArray(Object[] a) {
      return this.branchContent.toArray(a);
   }

   public boolean containsAll(Collection c) {
      return this.branchContent.containsAll(c);
   }

   public Object get(int index) {
      return this.branchContent.get(index);
   }

   public int indexOf(Object o) {
      return this.branchContent.indexOf(o);
   }

   public int lastIndexOf(Object o) {
      return this.branchContent.lastIndexOf(o);
   }

   protected Node asNode(Object object) {
      if (object instanceof Node) {
         return (Node)object;
      } else {
         throw new IllegalAddException("This list must contain instances of Node. Invalid type: " + object);
      }
   }

   protected List getBackingList() {
      return this.branchContent;
   }
}
