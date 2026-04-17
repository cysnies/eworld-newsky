package org.hibernate.cfg;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import org.hibernate.MappingException;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.mapping.Collection;
import org.hibernate.mapping.IndexedCollection;
import org.hibernate.mapping.OneToMany;
import org.hibernate.mapping.Selectable;
import org.hibernate.mapping.Value;
import org.jboss.logging.Logger;

public abstract class CollectionSecondPass implements SecondPass {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, CollectionSecondPass.class.getName());
   Mappings mappings;
   Collection collection;
   private Map localInheritedMetas;

   public CollectionSecondPass(Mappings mappings, Collection collection, Map inheritedMetas) {
      this.collection = collection;
      this.mappings = mappings;
      this.localInheritedMetas = inheritedMetas;
   }

   public CollectionSecondPass(Mappings mappings, Collection collection) {
      this(mappings, collection, Collections.EMPTY_MAP);
   }

   public void doSecondPass(Map persistentClasses) throws MappingException {
      LOG.debugf("Second pass for collection: %s", this.collection.getRole());
      this.secondPass(persistentClasses, this.localInheritedMetas);
      this.collection.createAllKeys();
      if (LOG.isDebugEnabled()) {
         String msg = "Mapped collection key: " + columns(this.collection.getKey());
         if (this.collection.isIndexed()) {
            msg = msg + ", index: " + columns(((IndexedCollection)this.collection).getIndex());
         }

         if (this.collection.isOneToMany()) {
            msg = msg + ", one-to-many: " + ((OneToMany)this.collection.getElement()).getReferencedEntityName();
         } else {
            msg = msg + ", element: " + columns(this.collection.getElement());
         }

         LOG.debug(msg);
      }

   }

   public abstract void secondPass(Map var1, Map var2) throws MappingException;

   private static String columns(Value val) {
      StringBuilder columns = new StringBuilder();
      Iterator iter = val.getColumnIterator();

      while(iter.hasNext()) {
         columns.append(((Selectable)iter.next()).getText());
         if (iter.hasNext()) {
            columns.append(", ");
         }
      }

      return columns.toString();
   }
}
