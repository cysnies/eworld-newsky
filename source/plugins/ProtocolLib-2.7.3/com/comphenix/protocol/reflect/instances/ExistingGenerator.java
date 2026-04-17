package com.comphenix.protocol.reflect.instances;

import com.comphenix.protocol.reflect.FieldUtils;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.google.common.collect.Lists;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import javax.annotation.Nullable;

public class ExistingGenerator implements InstanceProvider {
   private Node root = new Node((Class)null, (Object)null, 0);

   private ExistingGenerator() {
   }

   public static ExistingGenerator fromObjectFields(Object object) {
      if (object == null) {
         throw new IllegalArgumentException("Object cannot be NULL.");
      } else {
         return fromObjectFields(object, object.getClass());
      }
   }

   public static ExistingGenerator fromObjectFields(Object object, Class type) {
      ExistingGenerator generator = new ExistingGenerator();
      if (object == null) {
         throw new IllegalArgumentException("Object cannot be NULL.");
      } else if (type == null) {
         throw new IllegalArgumentException("Type cannot be NULL.");
      } else if (!type.isAssignableFrom(object.getClass())) {
         throw new IllegalArgumentException("Type must be a superclass or be the same type.");
      } else {
         for(Field field : FuzzyReflection.fromClass(type, true).getFields()) {
            try {
               Object value = FieldUtils.readField(field, object, true);
               if (value != null) {
                  generator.addObject(field.getType(), value);
               }
            } catch (Exception var6) {
            }
         }

         return generator;
      }
   }

   public static ExistingGenerator fromObjectArray(Object[] values) {
      ExistingGenerator generator = new ExistingGenerator();

      for(Object value : values) {
         generator.addObject(value);
      }

      return generator;
   }

   private void addObject(Object value) {
      if (value == null) {
         throw new IllegalArgumentException("Value cannot be NULL.");
      } else {
         this.addObject(value.getClass(), value);
      }
   }

   private void addObject(Class type, Object value) {
      Node node = this.getLeafNode(this.root, type, false);
      node.setValue(value);
   }

   private Node getLeafNode(Node start, Class type, boolean readOnly) {
      Class<?>[] path = this.getHierachy(type);
      Node current = start;

      for(int i = 0; i < path.length; ++i) {
         Node next = this.getNext(current, path[i], readOnly);
         if (next == null && readOnly) {
            current = null;
            break;
         }

         current = next;
      }

      return current;
   }

   private Node getNext(Node current, Class clazz, boolean readOnly) {
      Node next = current.getChild(clazz);
      if (next == null && !readOnly) {
         next = current.addChild(new Node(clazz, (Object)null, current.getLevel() + 1));
      }

      if (next != null && !readOnly && !clazz.isInterface()) {
         for(Class clazzInterface : clazz.getInterfaces()) {
            this.getLeafNode(this.root, clazzInterface, readOnly).addChild(next);
         }
      }

      return next;
   }

   private Node getLowestLeaf(Node current) {
      Node candidate = current;

      for(Node child : current.getChildren()) {
         Node subtree = this.getLowestLeaf(child);
         if (subtree.getValue() != null && candidate.getLevel() < subtree.getLevel()) {
            candidate = subtree;
         }
      }

      return candidate;
   }

   private Class[] getHierachy(Class type) {
      LinkedList<Class<?>> levels;
      for(levels = Lists.newLinkedList(); type != null; type = type.getSuperclass()) {
         levels.addFirst(type);
      }

      return (Class[])levels.toArray(new Class[0]);
   }

   public Object create(@Nullable Class type) {
      Node node = this.getLeafNode(this.root, type, true);
      if (node != null) {
         node = this.getLowestLeaf(node);
      }

      return node != null ? node.getValue() : null;
   }

   private static final class Node {
      private Map children = new HashMap();
      private Class key;
      private Object value;
      private int level;

      public Node(Class key, Object value, int level) {
         this.key = key;
         this.value = value;
         this.level = level;
      }

      public Node addChild(Node node) {
         this.children.put(node.key, node);
         return node;
      }

      public int getLevel() {
         return this.level;
      }

      public Collection getChildren() {
         return this.children.values();
      }

      public Object getValue() {
         return this.value;
      }

      public void setValue(Object value) {
         this.value = value;
      }

      public Node getChild(Class clazz) {
         return (Node)this.children.get(clazz);
      }
   }
}
