package org.hibernate.loader.entity;

import java.util.Collections;
import org.hibernate.FetchMode;
import org.hibernate.LockOptions;
import org.hibernate.MappingException;
import org.hibernate.engine.spi.CascadeStyle;
import org.hibernate.engine.spi.CascadingAction;
import org.hibernate.engine.spi.LoadQueryInfluencers;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.loader.AbstractEntityJoinWalker;
import org.hibernate.persister.entity.OuterJoinLoadable;
import org.hibernate.type.AssociationType;

public class CascadeEntityJoinWalker extends AbstractEntityJoinWalker {
   private final CascadingAction cascadeAction;

   public CascadeEntityJoinWalker(OuterJoinLoadable persister, CascadingAction action, SessionFactoryImplementor factory) throws MappingException {
      super(persister, factory, LoadQueryInfluencers.NONE);
      this.cascadeAction = action;
      StringBuilder whereCondition = this.whereString(this.getAlias(), persister.getIdentifierColumnNames(), 1).append(persister.filterFragment(this.getAlias(), Collections.EMPTY_MAP));
      this.initAll(whereCondition.toString(), "", LockOptions.READ);
   }

   protected boolean isJoinedFetchEnabled(AssociationType type, FetchMode config, CascadeStyle cascadeStyle) {
      return (type.isEntityType() || type.isCollectionType()) && (cascadeStyle == null || cascadeStyle.doCascade(this.cascadeAction));
   }

   protected boolean isTooManyCollections() {
      return countCollectionPersisters(this.associations) > 0;
   }

   public String getComment() {
      return "load " + this.getPersister().getEntityName();
   }
}
