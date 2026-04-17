package org.hibernate.cfg;

import java.util.Map;
import org.dom4j.Element;
import org.hibernate.MappingException;
import org.hibernate.engine.ResultSetMappingDefinition;

public class ResultSetMappingSecondPass extends ResultSetMappingBinder implements QuerySecondPass {
   private Element element;
   private String path;
   private Mappings mappings;

   public ResultSetMappingSecondPass(Element element, String path, Mappings mappings) {
      this.element = element;
      this.path = path;
      this.mappings = mappings;
   }

   public void doSecondPass(Map persistentClasses) throws MappingException {
      ResultSetMappingDefinition definition = buildResultSetMappingDefinition(this.element, this.path, this.mappings);
      this.mappings.addResultSetMapping(definition);
   }
}
