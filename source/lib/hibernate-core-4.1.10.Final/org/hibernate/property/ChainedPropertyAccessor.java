package org.hibernate.property;

import org.hibernate.PropertyNotFoundException;

public class ChainedPropertyAccessor implements PropertyAccessor {
   final PropertyAccessor[] chain;

   public ChainedPropertyAccessor(PropertyAccessor[] chain) {
      this.chain = chain;
   }

   public Getter getGetter(Class theClass, String propertyName) throws PropertyNotFoundException {
      Getter result = null;

      for(int i = 0; i < this.chain.length; ++i) {
         PropertyAccessor candidate = this.chain[i];

         try {
            result = candidate.getGetter(theClass, propertyName);
            return result;
         }
      }

      throw new PropertyNotFoundException("Could not find getter for " + propertyName + " on " + theClass);
   }

   public Setter getSetter(Class theClass, String propertyName) throws PropertyNotFoundException {
      Setter result = null;

      for(int i = 0; i < this.chain.length; ++i) {
         PropertyAccessor candidate = this.chain[i];

         try {
            result = candidate.getSetter(theClass, propertyName);
            return result;
         }
      }

      throw new PropertyNotFoundException("Could not find setter for " + propertyName + " on " + theClass);
   }
}
