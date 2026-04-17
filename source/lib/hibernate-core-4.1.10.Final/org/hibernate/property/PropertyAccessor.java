package org.hibernate.property;

import org.hibernate.PropertyNotFoundException;

public interface PropertyAccessor {
   Getter getGetter(Class var1, String var2) throws PropertyNotFoundException;

   Setter getSetter(Class var1, String var2) throws PropertyNotFoundException;
}
