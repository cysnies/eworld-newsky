package house;

import java.io.File;
import java.util.List;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistryBuilder;

public class Dao {
   private SessionFactory sessionFactory;

   public Dao(House house) {
      Configuration config = (new Configuration()).configure(new File(house.getDataFolder() + File.separator + "hibernate.cfg.xml"));
      ServiceRegistryBuilder builder = new ServiceRegistryBuilder();
      builder.applySettings(config.getProperties());
      this.sessionFactory = config.buildSessionFactory(builder.buildServiceRegistry());
   }

   public void close() {
      this.sessionFactory.close();
   }

   public void addHouseUser(HouseUser houseUser) {
      Session session = this.sessionFactory.openSession();
      session.beginTransaction();
      session.saveOrUpdate(houseUser);
      session.getTransaction().commit();
      session.close();
   }

   public List getAllHouseUsers() {
      Session session = this.sessionFactory.openSession();
      session.beginTransaction();
      List<HouseUser> result = session.createQuery("from HouseUser").list();
      session.getTransaction().commit();
      session.close();
      return result;
   }
}
