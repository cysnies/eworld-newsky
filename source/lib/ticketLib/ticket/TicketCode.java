package ticket;

public class TicketCode {
   private long id;
   private String code;
   private int status;
   private String user;
   private long createTime;
   private long useTime;

   public TicketCode() {
   }

   public TicketCode(String code, long createTime) {
      this.code = code;
      this.createTime = createTime;
   }

   public long getId() {
      return this.id;
   }

   public void setId(long id) {
      this.id = id;
   }

   public String getCode() {
      return this.code;
   }

   public void setCode(String code) {
      this.code = code;
   }

   public int getStatus() {
      return this.status;
   }

   public void setStatus(int status) {
      this.status = status;
   }

   public String getUser() {
      return this.user;
   }

   public void setUser(String user) {
      this.user = user;
   }

   public long getCreateTime() {
      return this.createTime;
   }

   public void setCreateTime(long createTime) {
      this.createTime = createTime;
   }

   public long getUseTime() {
      return this.useTime;
   }

   public void setUseTime(long useTime) {
      this.useTime = useTime;
   }
}
