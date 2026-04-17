package org.hibernate.hql.internal.ast.tree;

import java.util.ArrayList;
import java.util.List;
import org.hibernate.param.ParameterSpecification;
import org.hibernate.sql.JoinFragment;

public class SqlFragment extends Node implements ParameterContainer {
   private JoinFragment joinFragment;
   private FromElement fromElement;
   private List embeddedParameters;

   public void setJoinFragment(JoinFragment joinFragment) {
      this.joinFragment = joinFragment;
   }

   public boolean hasFilterCondition() {
      return this.joinFragment.hasFilterCondition();
   }

   public void setFromElement(FromElement fromElement) {
      this.fromElement = fromElement;
   }

   public FromElement getFromElement() {
      return this.fromElement;
   }

   public void addEmbeddedParameter(ParameterSpecification specification) {
      if (this.embeddedParameters == null) {
         this.embeddedParameters = new ArrayList();
      }

      this.embeddedParameters.add(specification);
   }

   public boolean hasEmbeddedParameters() {
      return this.embeddedParameters != null && !this.embeddedParameters.isEmpty();
   }

   public ParameterSpecification[] getEmbeddedParameters() {
      return (ParameterSpecification[])this.embeddedParameters.toArray(new ParameterSpecification[this.embeddedParameters.size()]);
   }
}
