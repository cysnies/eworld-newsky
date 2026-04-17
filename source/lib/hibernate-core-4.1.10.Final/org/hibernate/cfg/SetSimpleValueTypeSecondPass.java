package org.hibernate.cfg;

import java.util.Map;
import org.hibernate.MappingException;
import org.hibernate.cfg.annotations.SimpleValueBinder;

public class SetSimpleValueTypeSecondPass implements SecondPass {
   SimpleValueBinder binder;

   public SetSimpleValueTypeSecondPass(SimpleValueBinder val) {
      this.binder = val;
   }

   public void doSecondPass(Map persistentClasses) throws MappingException {
      this.binder.fillSimpleValue();
   }
}
