package house;

public class HouseUser {
   private long id;
   private String name;
   private int x;
   private int z;

   public HouseUser() {
   }

   public HouseUser(String name, int x, int z) {
      this.name = name;
      this.x = x;
      this.z = z;
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

   public int getX() {
      return this.x;
   }

   public void setX(int x) {
      this.x = x;
   }

   public int getZ() {
      return this.z;
   }

   public void setZ(int z) {
      this.z = z;
   }

   public int hashCode() {
      return (int)this.id;
   }

   public boolean equals(Object obj) {
      HouseUser houseUser = (HouseUser)obj;
      return houseUser.id == this.id;
   }
}
