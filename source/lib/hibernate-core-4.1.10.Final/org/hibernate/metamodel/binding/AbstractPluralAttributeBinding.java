package org.hibernate.metamodel.binding;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.hibernate.AssertionFailure;
import org.hibernate.FetchMode;
import org.hibernate.engine.FetchStyle;
import org.hibernate.engine.FetchTiming;
import org.hibernate.engine.spi.CascadeStyle;
import org.hibernate.metamodel.domain.PluralAttribute;
import org.hibernate.metamodel.relational.Table;
import org.hibernate.metamodel.relational.TableSpecification;
import org.hibernate.persister.collection.CollectionPersister;

public abstract class AbstractPluralAttributeBinding extends AbstractAttributeBinding implements PluralAttributeBinding {
   private final CollectionKey collectionKey = new CollectionKey(this);
   private final AbstractCollectionElement collectionElement;
   private Table collectionTable;
   private FetchTiming fetchTiming;
   private FetchStyle fetchStyle;
   private int batchSize = -1;
   private CascadeStyle cascadeStyle;
   private boolean orphanDelete;
   private Caching caching;
   private boolean inverse;
   private boolean mutable = true;
   private Class collectionPersisterClass;
   private String where;
   private String orderBy;
   private boolean sorted;
   private Comparator comparator;
   private String comparatorClassName;
   private String customLoaderName;
   private CustomSQL customSqlInsert;
   private CustomSQL customSqlUpdate;
   private CustomSQL customSqlDelete;
   private CustomSQL customSqlDeleteAll;
   private String referencedPropertyName;
   private final Map filters = new HashMap();
   private final Set synchronizedTables = new HashSet();

   protected AbstractPluralAttributeBinding(AttributeBindingContainer container, PluralAttribute attribute, CollectionElementNature collectionElementNature) {
      super(container, attribute);
      this.collectionElement = this.interpretNature(collectionElementNature);
   }

   private AbstractCollectionElement interpretNature(CollectionElementNature collectionElementNature) {
      switch (collectionElementNature) {
         case BASIC:
            return new BasicCollectionElement(this);
         case COMPOSITE:
            return new CompositeCollectionElement(this);
         case ONE_TO_MANY:
            return new OneToManyCollectionElement(this);
         case MANY_TO_MANY:
            return new ManyToManyCollectionElement(this);
         case MANY_TO_ANY:
            return new ManyToAnyCollectionElement(this);
         default:
            throw new AssertionFailure("Unknown collection element nature : " + collectionElementNature);
      }
   }

   public PluralAttribute getAttribute() {
      return (PluralAttribute)super.getAttribute();
   }

   public boolean isAssociation() {
      return this.collectionElement.getCollectionElementNature() == CollectionElementNature.MANY_TO_ANY || this.collectionElement.getCollectionElementNature() == CollectionElementNature.MANY_TO_MANY || this.collectionElement.getCollectionElementNature() == CollectionElementNature.ONE_TO_MANY;
   }

   public TableSpecification getCollectionTable() {
      return this.collectionTable;
   }

   public void setCollectionTable(Table collectionTable) {
      this.collectionTable = collectionTable;
   }

   public CollectionKey getCollectionKey() {
      return this.collectionKey;
   }

   public AbstractCollectionElement getCollectionElement() {
      return this.collectionElement;
   }

   public CascadeStyle getCascadeStyle() {
      return this.cascadeStyle;
   }

   public void setCascadeStyles(Iterable cascadeStyles) {
      List<CascadeStyle> cascadeStyleList = new ArrayList();

      for(CascadeStyle style : cascadeStyles) {
         if (style != CascadeStyle.NONE) {
            cascadeStyleList.add(style);
         }

         if (style == CascadeStyle.DELETE_ORPHAN || style == CascadeStyle.ALL_DELETE_ORPHAN) {
            this.orphanDelete = true;
         }
      }

      if (cascadeStyleList.isEmpty()) {
         this.cascadeStyle = CascadeStyle.NONE;
      } else if (cascadeStyleList.size() == 1) {
         this.cascadeStyle = (CascadeStyle)cascadeStyleList.get(0);
      } else {
         this.cascadeStyle = new CascadeStyle.MultipleCascadeStyle((CascadeStyle[])cascadeStyleList.toArray(new CascadeStyle[cascadeStyleList.size()]));
      }

   }

   public boolean isOrphanDelete() {
      return this.orphanDelete;
   }

   public FetchMode getFetchMode() {
      return this.getFetchStyle() == FetchStyle.JOIN ? FetchMode.JOIN : FetchMode.SELECT;
   }

   public FetchTiming getFetchTiming() {
      return this.fetchTiming;
   }

   public void setFetchTiming(FetchTiming fetchTiming) {
      this.fetchTiming = fetchTiming;
   }

   public FetchStyle getFetchStyle() {
      return this.fetchStyle;
   }

   public void setFetchStyle(FetchStyle fetchStyle) {
      this.fetchStyle = fetchStyle;
   }

   public String getCustomLoaderName() {
      return this.customLoaderName;
   }

   public void setCustomLoaderName(String customLoaderName) {
      this.customLoaderName = customLoaderName;
   }

   public CustomSQL getCustomSqlInsert() {
      return this.customSqlInsert;
   }

   public void setCustomSqlInsert(CustomSQL customSqlInsert) {
      this.customSqlInsert = customSqlInsert;
   }

   public CustomSQL getCustomSqlUpdate() {
      return this.customSqlUpdate;
   }

   public void setCustomSqlUpdate(CustomSQL customSqlUpdate) {
      this.customSqlUpdate = customSqlUpdate;
   }

   public CustomSQL getCustomSqlDelete() {
      return this.customSqlDelete;
   }

   public void setCustomSqlDelete(CustomSQL customSqlDelete) {
      this.customSqlDelete = customSqlDelete;
   }

   public CustomSQL getCustomSqlDeleteAll() {
      return this.customSqlDeleteAll;
   }

   public void setCustomSqlDeleteAll(CustomSQL customSqlDeleteAll) {
      this.customSqlDeleteAll = customSqlDeleteAll;
   }

   public Class getCollectionPersisterClass() {
      return this.collectionPersisterClass;
   }

   public void setCollectionPersisterClass(Class collectionPersisterClass) {
      this.collectionPersisterClass = collectionPersisterClass;
   }

   public Caching getCaching() {
      return this.caching;
   }

   public void setCaching(Caching caching) {
      this.caching = caching;
   }

   public String getOrderBy() {
      return this.orderBy;
   }

   public void setOrderBy(String orderBy) {
      this.orderBy = orderBy;
   }

   public String getWhere() {
      return this.where;
   }

   public void setWhere(String where) {
      this.where = where;
   }

   public boolean isInverse() {
      return this.inverse;
   }

   public void setInverse(boolean inverse) {
      this.inverse = inverse;
   }

   public boolean isMutable() {
      return this.mutable;
   }

   public void setMutable(boolean mutable) {
      this.mutable = mutable;
   }

   public int getBatchSize() {
      return this.batchSize;
   }

   public void setBatchSize(int batchSize) {
      this.batchSize = batchSize;
   }

   public String getReferencedPropertyName() {
      return this.referencedPropertyName;
   }

   public boolean isSorted() {
      return this.sorted;
   }

   public Comparator getComparator() {
      return this.comparator;
   }

   public void setComparator(Comparator comparator) {
      this.comparator = comparator;
   }

   public String getComparatorClassName() {
      return this.comparatorClassName;
   }

   public void addFilter(String name, String condition) {
      this.filters.put(name, condition);
   }

   public Map getFilterMap() {
      return this.filters;
   }
}
