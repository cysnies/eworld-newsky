package org.hibernate.dialect;

import org.hibernate.sql.ANSIJoinFragment;
import org.hibernate.sql.JoinFragment;

public class Oracle10gDialect extends Oracle9iDialect {
   public JoinFragment createOuterJoinFragment() {
      return new ANSIJoinFragment();
   }
}
