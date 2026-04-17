package org.dom4j;

import java.util.List;
import java.util.Map;
import org.jaxen.FunctionContext;
import org.jaxen.NamespaceContext;
import org.jaxen.VariableContext;

public interface XPath extends NodeFilter {
   String getText();

   boolean matches(Node var1);

   Object evaluate(Object var1);

   /** @deprecated */
   Object selectObject(Object var1);

   List selectNodes(Object var1);

   List selectNodes(Object var1, XPath var2);

   List selectNodes(Object var1, XPath var2, boolean var3);

   Node selectSingleNode(Object var1);

   String valueOf(Object var1);

   Number numberValueOf(Object var1);

   boolean booleanValueOf(Object var1);

   void sort(List var1);

   void sort(List var1, boolean var2);

   FunctionContext getFunctionContext();

   void setFunctionContext(FunctionContext var1);

   NamespaceContext getNamespaceContext();

   void setNamespaceContext(NamespaceContext var1);

   void setNamespaceURIs(Map var1);

   VariableContext getVariableContext();

   void setVariableContext(VariableContext var1);
}
