package org.hibernate.cfg;

import java.io.Serializable;
import java.util.Map;
import org.hibernate.MappingException;

public interface SecondPass extends Serializable {
   void doSecondPass(Map var1) throws MappingException;
}
