package org.hibernate.criterion;

public class Disjunction extends Junction {
   protected Disjunction() {
      super(Junction.Nature.OR);
   }
}
