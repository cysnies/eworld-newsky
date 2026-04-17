package org.hibernate.metamodel.binding;

import org.hibernate.FetchMode;
import org.hibernate.engine.FetchStyle;
import org.hibernate.engine.FetchTiming;
import org.hibernate.engine.spi.CascadeStyle;

public interface AssociationAttributeBinding extends AttributeBinding {
   CascadeStyle getCascadeStyle();

   void setCascadeStyles(Iterable var1);

   FetchTiming getFetchTiming();

   void setFetchTiming(FetchTiming var1);

   FetchStyle getFetchStyle();

   void setFetchStyle(FetchStyle var1);

   /** @deprecated */
   @Deprecated
   FetchMode getFetchMode();
}
