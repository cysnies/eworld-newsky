package org.hibernate.mapping;

import org.hibernate.FetchMode;

public interface Fetchable {
   FetchMode getFetchMode();

   void setFetchMode(FetchMode var1);

   boolean isLazy();

   void setLazy(boolean var1);
}
