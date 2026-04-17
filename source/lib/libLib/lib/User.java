package lib;

public class User {
   private long id;
   private String name;

   public User() {
   }

   public User(String name) {
      this.name = name;
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

   public int hashCode() {
      return (int)this.id;
   }

   public boolean equals(Object obj) {
      User houseUser = (User)obj;
      return houseUser.id == this.id;
   }
}
