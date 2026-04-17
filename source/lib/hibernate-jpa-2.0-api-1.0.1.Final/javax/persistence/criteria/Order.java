package javax.persistence.criteria;

public interface Order {
   Order reverse();

   boolean isAscending();

   Expression getExpression();
}
