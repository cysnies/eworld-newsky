package org.hibernate.collection.internal;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.dom4j.Element;
import org.hibernate.AssertionFailure;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.loader.CollectionAliases;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.type.Type;
import org.hibernate.type.XmlRepresentableType;

/** @deprecated */
@Deprecated
public abstract class PersistentIndexedElementHolder extends AbstractPersistentCollection {
   protected Element element;

   public PersistentIndexedElementHolder(SessionImplementor session, Element element) {
      super(session);
      this.element = element;
      this.setInitialized();
   }

   protected static String getIndex(Element element, String indexNodeName, int i) {
      return indexNodeName != null ? element.attributeValue(indexNodeName) : Integer.toString(i);
   }

   protected static void setIndex(Element element, String indexNodeName, String index) {
      if (indexNodeName != null) {
         element.addAttribute(indexNodeName, index);
      }

   }

   protected static String getIndexAttributeName(CollectionPersister persister) {
      String node = persister.getIndexNodeName();
      return node == null ? null : node.substring(1);
   }

   public Serializable getSnapshot(CollectionPersister persister) throws HibernateException {
      Type elementType = persister.getElementType();
      String indexNode = getIndexAttributeName(persister);
      List elements = this.element.elements(persister.getElementNodeName());
      HashMap snapshot = new HashMap(elements.size());

      for(int i = 0; i < elements.size(); ++i) {
         Element elem = (Element)elements.get(i);
         Object value = elementType.fromXMLNode(elem, persister.getFactory());
         Object copy = elementType.deepCopy(value, persister.getFactory());
         snapshot.put(getIndex(elem, indexNode, i), copy);
      }

      return snapshot;
   }

   public Collection getOrphans(Serializable snapshot, String entityName) throws HibernateException {
      return Collections.EMPTY_LIST;
   }

   public PersistentIndexedElementHolder(SessionImplementor session, CollectionPersister persister, Serializable key) throws HibernateException {
      super(session);
      Element owner = (Element)session.getPersistenceContext().getCollectionOwner(key, persister);
      if (owner == null) {
         throw new AssertionFailure("null owner");
      } else {
         String nodeName = persister.getNodeName();
         if (".".equals(nodeName)) {
            this.element = owner;
         } else {
            this.element = owner.element(nodeName);
            if (this.element == null) {
               this.element = owner.addElement(nodeName);
            }
         }

      }
   }

   public boolean isWrapper(Object collection) {
      return this.element == collection;
   }

   public boolean equalsSnapshot(CollectionPersister persister) throws HibernateException {
      Type elementType = persister.getElementType();
      String indexNode = getIndexAttributeName(persister);
      HashMap snapshot = (HashMap)this.getSnapshot();
      List elements = this.element.elements(persister.getElementNodeName());
      if (snapshot.size() != elements.size()) {
         return false;
      } else {
         for(int i = 0; i < snapshot.size(); ++i) {
            Element elem = (Element)elements.get(i);
            Object old = snapshot.get(getIndex(elem, indexNode, i));
            Object current = elementType.fromXMLNode(elem, persister.getFactory());
            if (elementType.isDirty(old, current, this.getSession())) {
               return false;
            }
         }

         return true;
      }
   }

   public boolean isSnapshotEmpty(Serializable snapshot) {
      return ((HashMap)snapshot).isEmpty();
   }

   public boolean empty() {
      return !this.element.elementIterator().hasNext();
   }

   public Object readFrom(ResultSet rs, CollectionPersister persister, CollectionAliases descriptor, Object owner) throws HibernateException, SQLException {
      Object object = persister.readElement(rs, owner, descriptor.getSuffixedElementAliases(), this.getSession());
      Type elementType = persister.getElementType();
      SessionFactoryImplementor factory = persister.getFactory();
      String indexNode = getIndexAttributeName(persister);
      Element elem = this.element.addElement(persister.getElementNodeName());
      elementType.setToXMLNode(elem, object, factory);
      Type indexType = persister.getIndexType();
      Object indexValue = persister.readIndex(rs, descriptor.getSuffixedIndexAliases(), this.getSession());
      String index = ((XmlRepresentableType)indexType).toXMLString(indexValue, factory);
      setIndex(elem, indexNode, index);
      return object;
   }

   public Iterator entries(CollectionPersister persister) {
      Type elementType = persister.getElementType();
      String indexNode = getIndexAttributeName(persister);
      List elements = this.element.elements(persister.getElementNodeName());
      int length = elements.size();
      List result = new ArrayList(length);

      for(int i = 0; i < length; ++i) {
         Element elem = (Element)elements.get(i);
         Object object = elementType.fromXMLNode(elem, persister.getFactory());
         result.add(new IndexedValue(getIndex(elem, indexNode, i), object));
      }

      return result.iterator();
   }

   public void beforeInitialize(CollectionPersister persister, int anticipatedSize) {
   }

   public boolean isDirectlyAccessible() {
      return true;
   }

   public Object getValue() {
      return this.element;
   }

   public Iterator getDeletes(CollectionPersister persister, boolean indexIsFormula) throws HibernateException {
      Type indexType = persister.getIndexType();
      HashMap snapshot = (HashMap)this.getSnapshot();
      HashMap deletes = (HashMap)snapshot.clone();
      deletes.keySet().removeAll(((HashMap)this.getSnapshot(persister)).keySet());
      ArrayList deleteList = new ArrayList(deletes.size());

      for(Object o : deletes.entrySet()) {
         Map.Entry me = (Map.Entry)o;
         Object object = indexIsFormula ? me.getValue() : ((XmlRepresentableType)indexType).fromXMLString((String)me.getKey(), persister.getFactory());
         if (object != null) {
            deleteList.add(object);
         }
      }

      return deleteList.iterator();
   }

   public boolean needsInserting(Object entry, int i, Type elementType) throws HibernateException {
      HashMap snapshot = (HashMap)this.getSnapshot();
      IndexedValue iv = (IndexedValue)entry;
      return iv.value != null && snapshot.get(iv.index) == null;
   }

   public boolean needsUpdating(Object entry, int i, Type elementType) throws HibernateException {
      HashMap snapshot = (HashMap)this.getSnapshot();
      IndexedValue iv = (IndexedValue)entry;
      Object old = snapshot.get(iv.index);
      return old != null && elementType.isDirty(old, iv.value, this.getSession());
   }

   public Object getIndex(Object entry, int i, CollectionPersister persister) {
      String index = ((IndexedValue)entry).index;
      Type indexType = persister.getIndexType();
      return ((XmlRepresentableType)indexType).fromXMLString(index, persister.getFactory());
   }

   public Object getElement(Object entry) {
      return ((IndexedValue)entry).value;
   }

   public Object getSnapshotElement(Object entry, int i) {
      return ((HashMap)this.getSnapshot()).get(((IndexedValue)entry).index);
   }

   public boolean entryExists(Object entry, int i) {
      return entry != null;
   }

   public static final class IndexedValue {
      String index;
      Object value;

      IndexedValue(String index, Object value) {
         this.index = index;
         this.value = value;
      }
   }
}
