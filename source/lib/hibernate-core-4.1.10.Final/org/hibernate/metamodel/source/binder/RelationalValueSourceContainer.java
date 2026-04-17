package org.hibernate.metamodel.source.binder;

import java.util.List;

public interface RelationalValueSourceContainer {
   boolean areValuesIncludedInInsertByDefault();

   boolean areValuesIncludedInUpdateByDefault();

   boolean areValuesNullableByDefault();

   List relationalValueSources();
}
