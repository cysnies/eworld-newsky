package landMain;

import java.io.File;
import java.util.List;
import land.EnterTip;
import land.Land;
import land.LandCmd;
import land.LandSpawn;
import land.LandUser;
import land.LeaveTip;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistryBuilder;

public class Dao {
   private SessionFactory sessionFactory;

   public Dao(LandMain landMain) {
      Configuration config = (new Configuration()).configure(new File(landMain.getDataFolder() + File.separator + "hibernate.cfg.xml"));
      ServiceRegistryBuilder builder = new ServiceRegistryBuilder();
      builder.applySettings(config.getProperties());
      this.sessionFactory = config.buildSessionFactory(builder.buildServiceRegistry());
   }

   public void close() {
      this.sessionFactory.close();
   }

   public void addLand(Land land) {
      Session session = this.sessionFactory.openSession();
      session.beginTransaction();
      session.saveOrUpdate(land);
      session.getTransaction().commit();
      session.close();
   }

   public List getAllLands() {
      Session session = this.sessionFactory.openSession();
      session.beginTransaction();
      List<Land> result = session.createQuery("from Land").list();
      session.getTransaction().commit();
      session.close();
      return result;
   }

   public void addEnterLandTip(EnterTip enterTip) {
      Session session = this.sessionFactory.openSession();
      session.beginTransaction();
      session.saveOrUpdate(enterTip);
      session.getTransaction().commit();
      session.close();
   }

   public void removeEnterLandTip(EnterTip enterTip) {
      Session session = this.sessionFactory.openSession();
      session.beginTransaction();
      session.delete(enterTip);
      session.getTransaction().commit();
      session.close();
   }

   public List getAllEnterTips() {
      Session session = this.sessionFactory.openSession();
      session.beginTransaction();
      List<EnterTip> result = session.createQuery("from EnterTip").list();
      session.getTransaction().commit();
      session.close();
      return result;
   }

   public void addLeaveLandTip(LeaveTip leaveTip) {
      Session session = this.sessionFactory.openSession();
      session.beginTransaction();
      session.saveOrUpdate(leaveTip);
      session.getTransaction().commit();
      session.close();
   }

   public void removeLeaveLandTip(LeaveTip leaveTip) {
      Session session = this.sessionFactory.openSession();
      session.beginTransaction();
      session.delete(leaveTip);
      session.getTransaction().commit();
      session.close();
   }

   public List getAllLeaveTips() {
      Session session = this.sessionFactory.openSession();
      session.beginTransaction();
      List<LeaveTip> result = session.createQuery("from LeaveTip").list();
      session.getTransaction().commit();
      session.close();
      return result;
   }

   public List getAllLandUsers() {
      Session session = this.sessionFactory.openSession();
      session.beginTransaction();
      List<LandUser> result = session.createQuery("from LandUser").list();
      session.getTransaction().commit();
      session.close();
      return result;
   }

   public void addLandUser(LandUser landUser) {
      Session session = this.sessionFactory.openSession();
      session.beginTransaction();
      session.saveOrUpdate(landUser);
      session.getTransaction().commit();
      session.close();
   }

   public void remove(Land land) {
      Session session = this.sessionFactory.openSession();
      session.beginTransaction();
      session.delete(land);
      session.getTransaction().commit();
      session.close();
   }

   public List getAllLandSpawns() {
      Session session = this.sessionFactory.openSession();
      session.beginTransaction();
      List<LandSpawn> result = session.createQuery("from LandSpawn").list();
      session.getTransaction().commit();
      session.close();
      return result;
   }

   public List getAllLandCmds() {
      Session session = this.sessionFactory.openSession();
      session.beginTransaction();
      List<LandCmd> result = session.createQuery("from LandCmd").list();
      session.getTransaction().commit();
      session.close();
      return result;
   }

   public void addLandSpawn(LandSpawn landSpawn) {
      Session session = this.sessionFactory.openSession();
      session.beginTransaction();
      session.saveOrUpdate(landSpawn);
      session.getTransaction().commit();
      session.close();
   }

   public void addLandCmd(LandCmd landCmd) {
      Session session = this.sessionFactory.openSession();
      session.beginTransaction();
      session.saveOrUpdate(landCmd);
      session.getTransaction().commit();
      session.close();
   }

   public void removeLandSpawn(LandSpawn landSpawn) {
      Session session = this.sessionFactory.openSession();
      session.beginTransaction();
      session.delete(landSpawn);
      session.getTransaction().commit();
      session.close();
   }

   public void removeLandCmd(LandCmd landCmd) {
      Session session = this.sessionFactory.openSession();
      session.beginTransaction();
      session.delete(landCmd);
      session.getTransaction().commit();
      session.close();
   }
}
