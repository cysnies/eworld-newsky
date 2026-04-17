package org.hibernate.hql.internal.ast;

import antlr.RecognitionException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.hibernate.QueryException;
import org.hibernate.internal.CoreMessageLogger;
import org.jboss.logging.Logger;

public class ErrorCounter implements ParseErrorHandler {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, ErrorCounter.class.getName());
   private List errorList = new ArrayList();
   private List warningList = new ArrayList();
   private List recognitionExceptions = new ArrayList();

   public void reportError(RecognitionException e) {
      this.reportError(e.toString());
      this.recognitionExceptions.add(e);
      LOG.error(e.toString(), e);
   }

   public void reportError(String message) {
      LOG.error(message);
      this.errorList.add(message);
   }

   public int getErrorCount() {
      return this.errorList.size();
   }

   public void reportWarning(String message) {
      LOG.debug(message);
      this.warningList.add(message);
   }

   private String getErrorString() {
      StringBuilder buf = new StringBuilder();
      Iterator iterator = this.errorList.iterator();

      while(iterator.hasNext()) {
         buf.append((String)iterator.next());
         if (iterator.hasNext()) {
            buf.append("\n");
         }
      }

      return buf.toString();
   }

   public void throwQueryException() throws QueryException {
      if (this.getErrorCount() > 0) {
         if (this.recognitionExceptions.size() > 0) {
            throw QuerySyntaxException.convert((RecognitionException)this.recognitionExceptions.get(0));
         } else {
            throw new QueryException(this.getErrorString());
         }
      } else {
         LOG.debug("throwQueryException() : no errors");
      }
   }
}
