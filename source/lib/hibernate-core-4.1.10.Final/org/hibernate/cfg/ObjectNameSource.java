package org.hibernate.cfg;

public interface ObjectNameSource {
   String getExplicitName();

   String getLogicalName();
}
