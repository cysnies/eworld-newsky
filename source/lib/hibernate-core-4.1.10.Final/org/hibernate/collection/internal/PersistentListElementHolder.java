package org.hibernate.collection.internal;

import java.io.Serializable;
import java.util.List;
import org.dom4j.Element;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.type.IntegerType;
import org.hibernate.type.Type;

/** @deprecated */
@Deprecated
public class PersistentListElementHolder extends PersistentIndexedElementHolder {
   public PersistentListElementHolder(SessionImplementor session, Element element) {
      super(session, element);
   }

   public PersistentListElementHolder(SessionImplementor session, CollectionPersister persister, Serializable key) throws HibernateException {
      super(session, persister, key);
   }

   public void initializeFromCache(CollectionPersister persister, Serializable disassembled, Object owner) throws HibernateException {
      Type elementType = persister.getElementType();
      String indexNodeName = getIndexAttributeName(persister);
      Serializable[] cached = disassembled;

      for(int i = 0; i < cached.length; ++i) {
         Object object = elementType.assemble(cached[i], this.getSession(), owner);
         Element subelement = this.element.addElement(persister.getElementNodeName());
         elementType.setToXMLNode(subelement, object, persister.getFactory());
         setIndex(subelement, indexNodeName, Integer.toString(i));
      }

   }

   public Serializable disassemble(CollectionPersister persister) throws HibernateException {
      Type elementType = persister.getElementType();
      String indexNodeName = getIndexAttributeName(persister);
      List elements = this.element.elements(persister.getElementNodeName());
      int length = elements.size();
      Serializable[] result = new Serializable[length];

      for(int i = 0; i < length; ++i) {
         Element elem = (Element)elements.get(i);
         Object object = elementType.fromXMLNode(elem, persister.getFactory());
         Integer index = (Integer)IntegerType.INSTANCE.fromString(getIndex(elem, indexNodeName, i));
         result[index] = elementType.disassemble(object, this.getSession(), (Object)null);
      }

      return result;
   }
}
