package javax.persistence.criteria;

import java.util.List;
import javax.persistence.TupleElement;

public interface Selection extends TupleElement {
   Selection alias(String var1);

   boolean isCompoundSelection();

   List getCompoundSelectionItems();
}
