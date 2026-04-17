package org.hibernate.dialect.unique;

import org.hibernate.mapping.Column;
import org.hibernate.mapping.Table;
import org.hibernate.mapping.UniqueKey;

public interface UniqueDelegate {
   String applyUniqueToColumn(Column var1);

   String applyUniqueToColumn(org.hibernate.metamodel.relational.Column var1);

   String applyUniquesToTable(Table var1);

   String applyUniquesToTable(org.hibernate.metamodel.relational.Table var1);

   String applyUniquesOnAlter(UniqueKey var1, String var2, String var3);

   String applyUniquesOnAlter(org.hibernate.metamodel.relational.UniqueKey var1);

   String dropUniquesOnAlter(UniqueKey var1, String var2, String var3);

   String dropUniquesOnAlter(org.hibernate.metamodel.relational.UniqueKey var1);

   String uniqueConstraintSql(UniqueKey var1);

   String uniqueConstraintSql(org.hibernate.metamodel.relational.UniqueKey var1);
}
