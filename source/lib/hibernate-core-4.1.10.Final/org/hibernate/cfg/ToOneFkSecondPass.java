package org.hibernate.cfg;

import java.util.Iterator;
import java.util.Map;
import org.hibernate.AnnotationException;
import org.hibernate.AssertionFailure;
import org.hibernate.MappingException;
import org.hibernate.cfg.annotations.TableBinder;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.mapping.Component;
import org.hibernate.mapping.KeyValue;
import org.hibernate.mapping.ManyToOne;
import org.hibernate.mapping.OneToOne;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.ToOne;

public class ToOneFkSecondPass extends FkSecondPass {
   private boolean unique;
   private Mappings mappings;
   private String path;
   private String entityClassName;

   public ToOneFkSecondPass(ToOne value, Ejb3JoinColumn[] columns, boolean unique, String entityClassName, String path, Mappings mappings) {
      super(value, columns);
      this.mappings = mappings;
      this.unique = unique;
      this.entityClassName = entityClassName;
      this.path = entityClassName != null ? path.substring(entityClassName.length() + 1) : path;
   }

   public String getReferencedEntityName() {
      return ((ToOne)this.value).getReferencedEntityName();
   }

   public boolean isInPrimaryKey() {
      if (this.entityClassName == null) {
         return false;
      } else {
         PersistentClass persistentClass = this.mappings.getClass(this.entityClassName);
         Property property = persistentClass.getIdentifierProperty();
         if (this.path == null) {
            return false;
         } else if (property != null) {
            return this.path.startsWith(property.getName() + ".");
         } else {
            if (this.path.startsWith("id.")) {
               KeyValue valueIdentifier = persistentClass.getIdentifier();
               String localPath = this.path.substring(3);
               if (valueIdentifier instanceof Component) {
                  Iterator it = ((Component)valueIdentifier).getPropertyIterator();

                  while(it.hasNext()) {
                     Property idProperty = (Property)it.next();
                     if (localPath.startsWith(idProperty.getName())) {
                        return true;
                     }
                  }
               }
            }

            return false;
         }
      }
   }

   public void doSecondPass(Map persistentClasses) throws MappingException {
      if (this.value instanceof ManyToOne) {
         ManyToOne manyToOne = (ManyToOne)this.value;
         PersistentClass ref = (PersistentClass)persistentClasses.get(manyToOne.getReferencedEntityName());
         if (ref == null) {
            throw new AnnotationException("@OneToOne or @ManyToOne on " + StringHelper.qualify(this.entityClassName, this.path) + " references an unknown entity: " + manyToOne.getReferencedEntityName());
         }

         BinderHelper.createSyntheticPropertyReference(this.columns, ref, (PersistentClass)null, manyToOne, false, this.mappings);
         TableBinder.bindFk(ref, (PersistentClass)null, this.columns, manyToOne, this.unique, this.mappings);
         if (!manyToOne.isIgnoreNotFound()) {
            manyToOne.createPropertyRefConstraints(persistentClasses);
         }
      } else {
         if (!(this.value instanceof OneToOne)) {
            throw new AssertionFailure("FkSecondPass for a wrong value type: " + this.value.getClass().getName());
         }

         ((OneToOne)this.value).createForeignKey();
      }

   }
}
