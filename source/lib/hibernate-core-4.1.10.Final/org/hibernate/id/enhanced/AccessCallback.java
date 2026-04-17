package org.hibernate.id.enhanced;

import org.hibernate.id.IntegralDataTypeHolder;

public interface AccessCallback {
   IntegralDataTypeHolder getNextValue();
}
