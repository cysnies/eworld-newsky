package org.hibernate.internal;

import java.util.ArrayList;
import java.util.List;
import org.hibernate.SessionFactory;
import org.hibernate.SessionFactoryObserver;

public class SessionFactoryObserverChain implements SessionFactoryObserver {
   private List observers;

   public void addObserver(SessionFactoryObserver observer) {
      if (this.observers == null) {
         this.observers = new ArrayList();
      }

      this.observers.add(observer);
   }

   public void sessionFactoryCreated(SessionFactory factory) {
      if (this.observers != null) {
         for(SessionFactoryObserver observer : this.observers) {
            observer.sessionFactoryCreated(factory);
         }

      }
   }

   public void sessionFactoryClosed(SessionFactory factory) {
      if (this.observers != null) {
         int size = this.observers.size();

         for(int index = size - 1; index >= 0; --index) {
            ((SessionFactoryObserver)this.observers.get(index)).sessionFactoryClosed(factory);
         }

      }
   }
}
