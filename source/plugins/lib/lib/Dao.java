package lib;

import java.io.File;
import java.util.List;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistryBuilder;

public class Dao {
   private SessionFactory sessionFactory;

   public Dao(Lib lib) {
      Configuration config = (new Configuration()).configure(new File(lib.getDataFolder() + File.separator + "hibernate.cfg.xml"));
      ServiceRegistryBuilder builder = new ServiceRegistryBuilder();
      builder.applySettings(config.getProperties());
      this.sessionFactory = config.buildSessionFactory(builder.buildServiceRegistry());
   }

   public void close() {
      this.sessionFactory.close();
   }

   public List getAllUsers() {
      Session session = this.sessionFactory.openSession();
      session.beginTransaction();
      List<User> list = session.createQuery("from User").list();
      session.getTransaction().commit();
      session.close();
      return list;
   }

   public void addOrUpdateUser(User user) {
      Session session = this.sessionFactory.openSession();
      session.beginTransaction();
      session.saveOrUpdate(user);
      session.getTransaction().commit();
      session.close();
   }

   public List getAllDebtUsers() {
      Session session = this.sessionFactory.openSession();
      session.beginTransaction();
      List<DebtUser> list = session.createQuery("from DebtUser").list();
      session.getTransaction().commit();
      session.close();
      return list;
   }

   public void addOrUpdateDebtUser(DebtUser user) {
      Session session = this.sessionFactory.openSession();
      session.beginTransaction();
      session.saveOrUpdate(user);
      session.getTransaction().commit();
      session.close();
   }
}
