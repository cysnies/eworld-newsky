package lib;

import java.util.ArrayList;
import java.util.List;

public class DebtUser {
   private long id;
   private String name;
   private int debt;
   private List log;

   public DebtUser() {
   }

   public DebtUser(String name) {
      this.name = name;
      this.debt = 0;
      this.log = new ArrayList();
   }

   public long getId() {
      return this.id;
   }

   public void setId(long id) {
      this.id = id;
   }

   public String getName() {
      return this.name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public int getDebt() {
      return this.debt;
   }

   public void setDebt(int debt) {
      this.debt = debt;
   }

   public List getLog() {
      return this.log;
   }

   public void setLog(List log) {
      this.log = log;
   }
}
