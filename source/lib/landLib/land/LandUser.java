package land;

public class LandUser {
   private long id;
   private String name;
   private int maxLands;

   public LandUser() {
   }

   public LandUser(String name, int maxLands) {
      this.name = name;
      this.maxLands = maxLands;
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

   public int getMaxLands() {
      return this.maxLands;
   }

   public void setMaxLands(int maxLands) {
      this.maxLands = maxLands;
   }

   public int hashCode() {
      return (int)this.id;
   }

   public boolean equals(Object obj) {
      LandUser landUser = (LandUser)obj;
      return landUser.id == this.id;
   }
}
