package org.hibernate.metamodel.source.binder;

import java.util.List;
import org.hibernate.metamodel.relational.ForeignKey;

public interface PluralAttributeKeySource {
   List getValueSources();

   String getExplicitForeignKeyName();

   ForeignKey.ReferentialAction getOnDeleteAction();

   String getReferencedEntityAttributeName();
}
