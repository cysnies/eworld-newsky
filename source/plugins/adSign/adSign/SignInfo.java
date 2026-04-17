package adSign;

public class SignInfo {
   private String owner;
   private long start;
   private int last;
   private String next;
   private int price;

   public String getOwner() {
      return this.owner;
   }

   public void setOwner(String owner) {
      this.owner = owner;
   }

   public long getStart() {
      return this.start;
   }

   public void setStart(long start) {
      this.start = start;
   }

   public int getLast() {
      return this.last;
   }

   public void setLast(int last) {
      this.last = last;
   }

   public String getNext() {
      return this.next;
   }

   public void setNext(String next) {
      this.next = next;
   }

   public int getPrice() {
      return this.price;
   }

   public void setPrice(int price) {
      this.price = price;
   }

   public static String save(SignInfo si) {
      String owner;
      if (si.owner == null) {
         owner = "@";
      } else {
         owner = si.owner;
      }

      String next;
      if (si.next == null) {
         next = "@";
      } else {
         next = si.next;
      }

      return owner + " " + si.start + " " + si.last + " " + next + " " + si.price;
   }

   public static SignInfo load(String data) {
      try {
         String[] ss = data.split(" ");
         String owner = ss[0];
         if (owner.equals("@")) {
            owner = null;
         }

         long start = Long.parseLong(ss[1]);
         int last = Integer.parseInt(ss[2]);
         String next = ss[3];
         if (next.equals("@")) {
            next = null;
         }

         int price = Integer.parseInt(ss[4]);
         SignInfo signInfo = new SignInfo();
         signInfo.setStart(start);
         signInfo.setOwner(owner);
         signInfo.setLast(last);
         signInfo.setNext(next);
         signInfo.setPrice(price);
         return signInfo;
      } catch (Exception var9) {
         return null;
      }
   }
}
