package org.hibernate.collection.internal;

import java.io.Serializable;
import java.util.List;
import org.dom4j.Element;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.type.Type;
import org.hibernate.type.XmlRepresentableType;

/** @deprecated */
@Deprecated
public class PersistentMapElementHolder extends PersistentIndexedElementHolder {
   public PersistentMapElementHolder(SessionImplementor session, Element element) {
      super(session, element);
   }

   public PersistentMapElementHolder(SessionImplementor session, CollectionPersister persister, Serializable key) throws HibernateException {
      super(session, persister, key);
   }

   public void initializeFromCache(CollectionPersister persister, Serializable disassembled, Object owner) throws HibernateException {
      Type elementType = persister.getElementType();
      Type indexType = persister.getIndexType();
      String indexNodeName = getIndexAttributeName(persister);
      Serializable[] cached = disassembled;
      int i = 0;

      while(i < cached.length) {
         Object index = indexType.assemble(cached[i++], this.getSession(), owner);
         Object object = elementType.assemble(cached[i++], this.getSession(), owner);
         Element subelement = this.element.addElement(persister.getElementNodeName());
         elementType.setToXMLNode(subelement, object, persister.getFactory());
         String indexString = ((XmlRepresentableType)indexType).toXMLString(index, persister.getFactory());
         setIndex(subelement, indexNodeName, indexString);
      }

   }

   public Serializable disassemble(CollectionPersister persister) throws HibernateException {
      Type elementType = persister.getElementType();
      Type indexType = persister.getIndexType();
      String indexNodeName = getIndexAttributeName(persister);
      List elements = this.element.elements(persister.getElementNodeName());
      int length = elements.size();
      Serializable[] result = new Serializable[length * 2];

      Object object;
      for(int i = 0; i < length * 2; result[i++] = elementType.disassemble(object, this.getSession(), (Object)null)) {
         Element elem = (Element)elements.get(i / 2);
         object = elementType.fromXMLNode(elem, persister.getFactory());
         String indexString = getIndex(elem, indexNodeName, i);
         Object index = ((XmlRepresentableType)indexType).fromXMLString(indexString, persister.getFactory());
         result[i++] = indexType.disassemble(index, this.getSession(), (Object)null);
      }

      return result;
   }
}
