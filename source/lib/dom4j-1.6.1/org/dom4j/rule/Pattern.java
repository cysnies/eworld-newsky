package org.dom4j.rule;

import org.dom4j.Node;
import org.dom4j.NodeFilter;

public interface Pattern extends NodeFilter {
   short ANY_NODE = 0;
   short NONE = 9999;
   short NUMBER_OF_TYPES = 14;
   double DEFAULT_PRIORITY = (double)0.5F;

   boolean matches(Node var1);

   double getPriority();

   Pattern[] getUnionPatterns();

   short getMatchType();

   String getMatchesNodeName();
}
