package org.dom4j.swing;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.swing.tree.TreeNode;
import org.dom4j.Branch;
import org.dom4j.CharacterData;
import org.dom4j.Node;

public class BranchTreeNode extends LeafTreeNode {
   protected List children;

   public BranchTreeNode() {
   }

   public BranchTreeNode(Branch xmlNode) {
      super(xmlNode);
   }

   public BranchTreeNode(TreeNode parent, Branch xmlNode) {
      super(parent, xmlNode);
   }

   public Enumeration children() {
      return new Enumeration() {
         private int index = -1;

         public boolean hasMoreElements() {
            return this.index + 1 < BranchTreeNode.this.getChildCount();
         }

         public Object nextElement() {
            return BranchTreeNode.this.getChildAt(++this.index);
         }
      };
   }

   public boolean getAllowsChildren() {
      return true;
   }

   public TreeNode getChildAt(int childIndex) {
      return (TreeNode)this.getChildList().get(childIndex);
   }

   public int getChildCount() {
      return this.getChildList().size();
   }

   public int getIndex(TreeNode node) {
      return this.getChildList().indexOf(node);
   }

   public boolean isLeaf() {
      return this.getXmlBranch().nodeCount() <= 0;
   }

   public String toString() {
      return this.xmlNode.getName();
   }

   protected List getChildList() {
      if (this.children == null) {
         this.children = this.createChildList();
      }

      return this.children;
   }

   protected List createChildList() {
      Branch branch = this.getXmlBranch();
      int size = branch.nodeCount();
      List childList = new ArrayList(size);

      for(int i = 0; i < size; ++i) {
         Node node = branch.node(i);
         if (node instanceof CharacterData) {
            String text = node.getText();
            if (text == null) {
               continue;
            }

            text = text.trim();
            if (text.length() <= 0) {
               continue;
            }
         }

         childList.add(this.createChildTreeNode(node));
      }

      return childList;
   }

   protected TreeNode createChildTreeNode(Node xmlNode) {
      return (TreeNode)(xmlNode instanceof Branch ? new BranchTreeNode(this, (Branch)xmlNode) : new LeafTreeNode(this, xmlNode));
   }

   protected Branch getXmlBranch() {
      return (Branch)this.xmlNode;
   }
}
