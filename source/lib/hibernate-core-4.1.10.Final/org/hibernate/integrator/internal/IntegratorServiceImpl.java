package org.hibernate.integrator.internal;

import java.util.LinkedHashSet;
import org.hibernate.cfg.beanvalidation.BeanValidationIntegrator;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.integrator.spi.IntegratorService;
import org.hibernate.service.classloading.spi.ClassLoaderService;
import org.jboss.logging.Logger;

public class IntegratorServiceImpl implements IntegratorService {
   private static final Logger LOG = Logger.getLogger(IntegratorServiceImpl.class.getName());
   private final LinkedHashSet integrators = new LinkedHashSet();

   public IntegratorServiceImpl(LinkedHashSet providedIntegrators, ClassLoaderService classLoaderService) {
      this.addIntegrator(new BeanValidationIntegrator());

      for(Integrator integrator : providedIntegrators) {
         this.addIntegrator(integrator);
      }

      for(Integrator integrator : classLoaderService.loadJavaServices(Integrator.class)) {
         this.addIntegrator(integrator);
      }

   }

   private void addIntegrator(Integrator integrator) {
      LOG.debugf("Adding Integrator [%s].", integrator.getClass().getName());
      this.integrators.add(integrator);
   }

   public Iterable getIntegrators() {
      return this.integrators;
   }
}
