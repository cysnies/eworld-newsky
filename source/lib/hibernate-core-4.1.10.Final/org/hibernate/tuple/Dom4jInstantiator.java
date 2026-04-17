package org.hibernate.tuple;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import org.dom4j.Element;
import org.hibernate.internal.util.xml.XMLHelper;
import org.hibernate.mapping.Component;
import org.hibernate.mapping.PersistentClass;

public class Dom4jInstantiator implements Instantiator {
   private final String nodeName;
   private final HashSet isInstanceNodeNames = new HashSet();

   public Dom4jInstantiator(Component component) {
      this.nodeName = component.getNodeName();
      this.isInstanceNodeNames.add(this.nodeName);
   }

   public Dom4jInstantiator(PersistentClass mappingInfo) {
      this.nodeName = mappingInfo.getNodeName();
      this.isInstanceNodeNames.add(this.nodeName);
      if (mappingInfo.hasSubclasses()) {
         Iterator itr = mappingInfo.getSubclassClosureIterator();

         while(itr.hasNext()) {
            PersistentClass subclassInfo = (PersistentClass)itr.next();
            this.isInstanceNodeNames.add(subclassInfo.getNodeName());
         }
      }

   }

   public Object instantiate(Serializable id) {
      return this.instantiate();
   }

   public Object instantiate() {
      return XMLHelper.generateDom4jElement(this.nodeName);
   }

   public boolean isInstance(Object object) {
      return object instanceof Element ? this.isInstanceNodeNames.contains(((Element)object).getName()) : false;
   }
}
