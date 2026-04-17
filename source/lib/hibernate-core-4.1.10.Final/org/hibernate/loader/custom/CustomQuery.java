package org.hibernate.loader.custom;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface CustomQuery {
   String getSQL();

   Set getQuerySpaces();

   Map getNamedParameterBindPoints();

   List getCustomQueryReturns();
}
