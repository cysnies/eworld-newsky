package org.hibernate.param;

public interface ExplicitParameterSpecification extends ParameterSpecification {
   int getSourceLine();

   int getSourceColumn();
}
