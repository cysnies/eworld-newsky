package org.hibernate.metamodel.source.hbm;

import java.util.List;
import org.hibernate.internal.jaxb.mapping.hbm.EntityElement;
import org.hibernate.metamodel.source.LocalBindingContext;
import org.hibernate.metamodel.source.MetaAttributeContext;

public interface HbmBindingContext extends LocalBindingContext {
   boolean isAutoImport();

   MetaAttributeContext getMetaAttributeContext();

   String determineEntityName(EntityElement var1);

   void processFetchProfiles(List var1, String var2);
}
