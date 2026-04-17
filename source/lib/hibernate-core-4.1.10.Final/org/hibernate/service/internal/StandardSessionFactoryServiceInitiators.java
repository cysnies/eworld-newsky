package org.hibernate.service.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.hibernate.engine.spi.CacheInitiator;
import org.hibernate.event.service.internal.EventListenerServiceInitiator;
import org.hibernate.service.spi.SessionFactoryServiceInitiator;
import org.hibernate.stat.internal.StatisticsInitiator;

public class StandardSessionFactoryServiceInitiators {
   public static List LIST = buildStandardServiceInitiatorList();

   private static List buildStandardServiceInitiatorList() {
      List<SessionFactoryServiceInitiator> serviceInitiators = new ArrayList();
      serviceInitiators.add(EventListenerServiceInitiator.INSTANCE);
      serviceInitiators.add(StatisticsInitiator.INSTANCE);
      serviceInitiators.add(CacheInitiator.INSTANCE);
      return Collections.unmodifiableList(serviceInitiators);
   }
}
