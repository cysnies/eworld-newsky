package org.hibernate.engine.spi;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.hibernate.Filter;
import org.hibernate.UnknownProfileException;
import org.hibernate.internal.FilterImpl;
import org.hibernate.type.Type;

public class LoadQueryInfluencers implements Serializable {
   public static LoadQueryInfluencers NONE = new LoadQueryInfluencers();
   private final SessionFactoryImplementor sessionFactory;
   private String internalFetchProfile;
   private Map enabledFilters;
   private Set enabledFetchProfileNames;

   public LoadQueryInfluencers() {
      this((SessionFactoryImplementor)null, Collections.emptyMap(), Collections.emptySet());
   }

   public LoadQueryInfluencers(SessionFactoryImplementor sessionFactory) {
      this(sessionFactory, new HashMap(), new HashSet());
   }

   private LoadQueryInfluencers(SessionFactoryImplementor sessionFactory, Map enabledFilters, Set enabledFetchProfileNames) {
      this.sessionFactory = sessionFactory;
      this.enabledFilters = enabledFilters;
      this.enabledFetchProfileNames = enabledFetchProfileNames;
   }

   public SessionFactoryImplementor getSessionFactory() {
      return this.sessionFactory;
   }

   public String getInternalFetchProfile() {
      return this.internalFetchProfile;
   }

   public void setInternalFetchProfile(String internalFetchProfile) {
      if (this.sessionFactory == null) {
         throw new IllegalStateException("Cannot modify context-less LoadQueryInfluencers");
      } else {
         this.internalFetchProfile = internalFetchProfile;
      }
   }

   public boolean hasEnabledFilters() {
      return this.enabledFilters != null && !this.enabledFilters.isEmpty();
   }

   public Map getEnabledFilters() {
      for(Filter filter : this.enabledFilters.values()) {
         filter.validate();
      }

      return this.enabledFilters;
   }

   public Set getEnabledFilterNames() {
      return Collections.unmodifiableSet(this.enabledFilters.keySet());
   }

   public Filter getEnabledFilter(String filterName) {
      return (Filter)this.enabledFilters.get(filterName);
   }

   public Filter enableFilter(String filterName) {
      FilterImpl filter = new FilterImpl(this.sessionFactory.getFilterDefinition(filterName));
      this.enabledFilters.put(filterName, filter);
      return filter;
   }

   public void disableFilter(String filterName) {
      this.enabledFilters.remove(filterName);
   }

   public Object getFilterParameterValue(String filterParameterName) {
      String[] parsed = parseFilterParameterName(filterParameterName);
      FilterImpl filter = (FilterImpl)this.enabledFilters.get(parsed[0]);
      if (filter == null) {
         throw new IllegalArgumentException("Filter [" + parsed[0] + "] currently not enabled");
      } else {
         return filter.getParameter(parsed[1]);
      }
   }

   public Type getFilterParameterType(String filterParameterName) {
      String[] parsed = parseFilterParameterName(filterParameterName);
      FilterDefinition filterDef = this.sessionFactory.getFilterDefinition(parsed[0]);
      if (filterDef == null) {
         throw new IllegalArgumentException("Filter [" + parsed[0] + "] not defined");
      } else {
         Type type = filterDef.getParameterType(parsed[1]);
         if (type == null) {
            throw new InternalError("Unable to locate type for filter parameter");
         } else {
            return type;
         }
      }
   }

   public static String[] parseFilterParameterName(String filterParameterName) {
      int dot = filterParameterName.indexOf(46);
      if (dot <= 0) {
         throw new IllegalArgumentException("Invalid filter-parameter name format");
      } else {
         String filterName = filterParameterName.substring(0, dot);
         String parameterName = filterParameterName.substring(dot + 1);
         return new String[]{filterName, parameterName};
      }
   }

   public boolean hasEnabledFetchProfiles() {
      return this.enabledFetchProfileNames != null && !this.enabledFetchProfileNames.isEmpty();
   }

   public Set getEnabledFetchProfileNames() {
      return this.enabledFetchProfileNames;
   }

   private void checkFetchProfileName(String name) {
      if (!this.sessionFactory.containsFetchProfileDefinition(name)) {
         throw new UnknownProfileException(name);
      }
   }

   public boolean isFetchProfileEnabled(String name) throws UnknownProfileException {
      this.checkFetchProfileName(name);
      return this.enabledFetchProfileNames.contains(name);
   }

   public void enableFetchProfile(String name) throws UnknownProfileException {
      this.checkFetchProfileName(name);
      this.enabledFetchProfileNames.add(name);
   }

   public void disableFetchProfile(String name) throws UnknownProfileException {
      this.checkFetchProfileName(name);
      this.enabledFetchProfileNames.remove(name);
   }
}
