package org.hibernate.cfg;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.dom4j.Attribute;
import org.dom4j.Element;
import org.hibernate.MappingException;
import org.hibernate.engine.ResultSetMappingDefinition;
import org.hibernate.engine.spi.NamedSQLQueryDefinition;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.StringHelper;
import org.jboss.logging.Logger;

public class NamedSQLQuerySecondPass extends ResultSetMappingBinder implements QuerySecondPass {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, NamedSQLQuerySecondPass.class.getName());
   private Element queryElem;
   private String path;
   private Mappings mappings;

   public NamedSQLQuerySecondPass(Element queryElem, String path, Mappings mappings) {
      this.queryElem = queryElem;
      this.path = path;
      this.mappings = mappings;
   }

   public void doSecondPass(Map persistentClasses) throws MappingException {
      String queryName = this.queryElem.attribute("name").getValue();
      if (this.path != null) {
         queryName = this.path + '.' + queryName;
      }

      boolean cacheable = "true".equals(this.queryElem.attributeValue("cacheable"));
      String region = this.queryElem.attributeValue("cache-region");
      Attribute tAtt = this.queryElem.attribute("timeout");
      Integer timeout = tAtt == null ? null : Integer.valueOf(tAtt.getValue());
      Attribute fsAtt = this.queryElem.attribute("fetch-size");
      Integer fetchSize = fsAtt == null ? null : Integer.valueOf(fsAtt.getValue());
      Attribute roAttr = this.queryElem.attribute("read-only");
      boolean readOnly = roAttr != null && "true".equals(roAttr.getValue());
      Attribute cacheModeAtt = this.queryElem.attribute("cache-mode");
      String cacheMode = cacheModeAtt == null ? null : cacheModeAtt.getValue();
      Attribute cmAtt = this.queryElem.attribute("comment");
      String comment = cmAtt == null ? null : cmAtt.getValue();
      List<String> synchronizedTables = new ArrayList();
      Iterator tables = this.queryElem.elementIterator("synchronize");

      while(tables.hasNext()) {
         synchronizedTables.add(((Element)tables.next()).attributeValue("table"));
      }

      boolean callable = "true".equals(this.queryElem.attributeValue("callable"));
      Attribute ref = this.queryElem.attribute("resultset-ref");
      String resultSetRef = ref == null ? null : ref.getValue();
      NamedSQLQueryDefinition namedQuery;
      if (StringHelper.isNotEmpty(resultSetRef)) {
         namedQuery = new NamedSQLQueryDefinition(queryName, this.queryElem.getText(), resultSetRef, synchronizedTables, cacheable, region, timeout, fetchSize, HbmBinder.getFlushMode(this.queryElem.attributeValue("flush-mode")), HbmBinder.getCacheMode(cacheMode), readOnly, comment, HbmBinder.getParameterTypes(this.queryElem), callable);
      } else {
         ResultSetMappingDefinition definition = buildResultSetMappingDefinition(this.queryElem, this.path, this.mappings);
         namedQuery = new NamedSQLQueryDefinition(queryName, this.queryElem.getText(), definition.getQueryReturns(), synchronizedTables, cacheable, region, timeout, fetchSize, HbmBinder.getFlushMode(this.queryElem.attributeValue("flush-mode")), HbmBinder.getCacheMode(cacheMode), readOnly, comment, HbmBinder.getParameterTypes(this.queryElem), callable);
      }

      if (LOG.isDebugEnabled()) {
         LOG.debugf("Named SQL query: %s -> %s", namedQuery.getName(), namedQuery.getQueryString());
      }

      this.mappings.addSQLQuery(queryName, namedQuery);
   }
}
