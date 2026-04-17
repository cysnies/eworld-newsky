package org.hibernate.criterion;

public class Conjunction extends Junction {
   public Conjunction() {
      super(Junction.Nature.AND);
   }
}
