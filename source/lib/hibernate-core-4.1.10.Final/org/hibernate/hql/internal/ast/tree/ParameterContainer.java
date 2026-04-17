package org.hibernate.hql.internal.ast.tree;

import org.hibernate.param.ParameterSpecification;

/** @deprecated */
public interface ParameterContainer {
   void setText(String var1);

   void addEmbeddedParameter(ParameterSpecification var1);

   boolean hasEmbeddedParameters();

   ParameterSpecification[] getEmbeddedParameters();
}
