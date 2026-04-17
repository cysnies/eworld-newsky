package org.hibernate.internal;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.hibernate.Filter;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.FilterDefinition;
import org.hibernate.type.Type;

public class FilterImpl implements Filter, Serializable {
   public static final String MARKER = "$FILTER_PLACEHOLDER$";
   private transient FilterDefinition definition;
   private String filterName;
   private Map parameters = new HashMap();

   void afterDeserialize(SessionFactoryImpl factory) {
      this.definition = factory.getFilterDefinition(this.filterName);
      this.validate();
   }

   public FilterImpl(FilterDefinition configuration) {
      this.definition = configuration;
      this.filterName = this.definition.getFilterName();
   }

   public FilterDefinition getFilterDefinition() {
      return this.definition;
   }

   public String getName() {
      return this.definition.getFilterName();
   }

   public Map getParameters() {
      return this.parameters;
   }

   public Filter setParameter(String name, Object value) throws IllegalArgumentException {
      Type type = this.definition.getParameterType(name);
      if (type == null) {
         throw new IllegalArgumentException("Undefined filter parameter [" + name + "]");
      } else if (value != null && !type.getReturnedClass().isAssignableFrom(value.getClass())) {
         throw new IllegalArgumentException("Incorrect type for parameter [" + name + "]");
      } else {
         this.parameters.put(name, value);
         return this;
      }
   }

   public Filter setParameterList(String name, Collection values) throws HibernateException {
      if (values == null) {
         throw new IllegalArgumentException("Collection must be not null!");
      } else {
         Type type = this.definition.getParameterType(name);
         if (type == null) {
            throw new HibernateException("Undefined filter parameter [" + name + "]");
         } else {
            if (values.size() > 0) {
               Class elementClass = values.iterator().next().getClass();
               if (!type.getReturnedClass().isAssignableFrom(elementClass)) {
                  throw new HibernateException("Incorrect type for parameter [" + name + "]");
               }
            }

            this.parameters.put(name, values);
            return this;
         }
      }
   }

   public Filter setParameterList(String name, Object[] values) throws IllegalArgumentException {
      return this.setParameterList(name, (Collection)Arrays.asList(values));
   }

   public Object getParameter(String name) {
      return this.parameters.get(name);
   }

   public void validate() throws HibernateException {
      for(String parameterName : this.definition.getParameterNames()) {
         if (this.parameters.get(parameterName) == null) {
            throw new HibernateException("Filter [" + this.getName() + "] parameter [" + parameterName + "] value not set");
         }
      }

   }
}
