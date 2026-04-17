package org.hibernate.engine.profile;

import java.util.HashMap;
import java.util.Map;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.type.BagType;
import org.hibernate.type.Type;
import org.jboss.logging.Logger;

public class FetchProfile {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, FetchProfile.class.getName());
   private final String name;
   private Map fetches = new HashMap();
   private boolean containsJoinFetchedCollection = false;
   private boolean containsJoinFetchedBag = false;
   private Fetch bagJoinFetch;

   public FetchProfile(String name) {
      this.name = name;
   }

   public void addFetch(Association association, String fetchStyleName) {
      this.addFetch(association, Fetch.Style.parse(fetchStyleName));
   }

   public void addFetch(Association association, Fetch.Style style) {
      this.addFetch(new Fetch(association, style));
   }

   public void addFetch(Fetch fetch) {
      String fetchAssociactionRole = fetch.getAssociation().getRole();
      Type associationType = fetch.getAssociation().getOwner().getPropertyType(fetch.getAssociation().getAssociationPath());
      if (associationType.isCollectionType()) {
         LOG.tracev("Handling request to add collection fetch [{0}]", fetchAssociactionRole);
         if (Fetch.Style.JOIN == fetch.getStyle()) {
            if (BagType.class.isInstance(associationType) && this.containsJoinFetchedCollection) {
               LOG.containsJoinFetchedCollection(fetchAssociactionRole);
               return;
            }

            if (this.containsJoinFetchedBag) {
               if (this.fetches.remove(this.bagJoinFetch.getAssociation().getRole()) != this.bagJoinFetch) {
                  LOG.unableToRemoveBagJoinFetch();
               }

               this.bagJoinFetch = null;
               this.containsJoinFetchedBag = false;
            }

            this.containsJoinFetchedCollection = true;
         }
      }

      this.fetches.put(fetchAssociactionRole, fetch);
   }

   public String getName() {
      return this.name;
   }

   public Map getFetches() {
      return this.fetches;
   }

   public Fetch getFetchByRole(String role) {
      return (Fetch)this.fetches.get(role);
   }

   public boolean isContainsJoinFetchedCollection() {
      return this.containsJoinFetchedCollection;
   }

   public boolean isContainsJoinFetchedBag() {
      return this.containsJoinFetchedBag;
   }
}
