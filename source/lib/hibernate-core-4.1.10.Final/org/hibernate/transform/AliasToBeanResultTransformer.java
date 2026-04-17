package org.hibernate.transform;

import java.util.Arrays;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.property.ChainedPropertyAccessor;
import org.hibernate.property.PropertyAccessor;
import org.hibernate.property.PropertyAccessorFactory;
import org.hibernate.property.Setter;

public class AliasToBeanResultTransformer extends AliasedTupleSubsetResultTransformer {
   private final Class resultClass;
   private boolean isInitialized;
   private String[] aliases;
   private Setter[] setters;

   public AliasToBeanResultTransformer(Class resultClass) {
      if (resultClass == null) {
         throw new IllegalArgumentException("resultClass cannot be null");
      } else {
         this.isInitialized = false;
         this.resultClass = resultClass;
      }
   }

   public boolean isTransformedValueATupleElement(String[] aliases, int tupleLength) {
      return false;
   }

   public Object transformTuple(Object[] tuple, String[] aliases) {
      try {
         if (!this.isInitialized) {
            this.initialize(aliases);
         } else {
            this.check(aliases);
         }

         Object result = this.resultClass.newInstance();

         for(int i = 0; i < aliases.length; ++i) {
            if (this.setters[i] != null) {
               this.setters[i].set(result, tuple[i], (SessionFactoryImplementor)null);
            }
         }

         return result;
      } catch (InstantiationException var5) {
         throw new HibernateException("Could not instantiate resultclass: " + this.resultClass.getName());
      } catch (IllegalAccessException var6) {
         throw new HibernateException("Could not instantiate resultclass: " + this.resultClass.getName());
      }
   }

   private void initialize(String[] aliases) {
      PropertyAccessor propertyAccessor = new ChainedPropertyAccessor(new PropertyAccessor[]{PropertyAccessorFactory.getPropertyAccessor((Class)this.resultClass, (String)null), PropertyAccessorFactory.getPropertyAccessor("field")});
      this.aliases = new String[aliases.length];
      this.setters = new Setter[aliases.length];

      for(int i = 0; i < aliases.length; ++i) {
         String alias = aliases[i];
         if (alias != null) {
            this.aliases[i] = alias;
            this.setters[i] = propertyAccessor.getSetter(this.resultClass, alias);
         }
      }

      this.isInitialized = true;
   }

   private void check(String[] aliases) {
      if (!Arrays.equals(aliases, this.aliases)) {
         throw new IllegalStateException("aliases are different from what is cached; aliases=" + Arrays.asList(aliases) + " cached=" + Arrays.asList(this.aliases));
      }
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         AliasToBeanResultTransformer that = (AliasToBeanResultTransformer)o;
         if (!this.resultClass.equals(that.resultClass)) {
            return false;
         } else {
            return Arrays.equals(this.aliases, that.aliases);
         }
      } else {
         return false;
      }
   }

   public int hashCode() {
      int result = this.resultClass.hashCode();
      result = 31 * result + (this.aliases != null ? Arrays.hashCode(this.aliases) : 0);
      return result;
   }
}
