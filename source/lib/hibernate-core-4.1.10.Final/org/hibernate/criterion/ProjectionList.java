package org.hibernate.criterion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.internal.util.collections.ArrayHelper;
import org.hibernate.type.Type;

public class ProjectionList implements EnhancedProjection {
   private List elements = new ArrayList();

   protected ProjectionList() {
   }

   public ProjectionList create() {
      return new ProjectionList();
   }

   public ProjectionList add(Projection proj) {
      this.elements.add(proj);
      return this;
   }

   public ProjectionList add(Projection projection, String alias) {
      return this.add(Projections.alias(projection, alias));
   }

   public Type[] getTypes(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
      List types = new ArrayList(this.getLength());

      for(int i = 0; i < this.getLength(); ++i) {
         Type[] elemTypes = this.getProjection(i).getTypes(criteria, criteriaQuery);
         ArrayHelper.addAll(types, elemTypes);
      }

      return ArrayHelper.toTypeArray(types);
   }

   public String toSqlString(Criteria criteria, int loc, CriteriaQuery criteriaQuery) throws HibernateException {
      StringBuilder buf = new StringBuilder();

      for(int i = 0; i < this.getLength(); ++i) {
         Projection proj = this.getProjection(i);
         buf.append(proj.toSqlString(criteria, loc, criteriaQuery));
         loc += getColumnAliases(loc, criteria, criteriaQuery, proj).length;
         if (i < this.elements.size() - 1) {
            buf.append(", ");
         }
      }

      return buf.toString();
   }

   public String toGroupSqlString(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
      StringBuilder buf = new StringBuilder();

      for(int i = 0; i < this.getLength(); ++i) {
         Projection proj = this.getProjection(i);
         if (proj.isGrouped()) {
            buf.append(proj.toGroupSqlString(criteria, criteriaQuery)).append(", ");
         }
      }

      if (buf.length() > 2) {
         buf.setLength(buf.length() - 2);
      }

      return buf.toString();
   }

   public String[] getColumnAliases(int loc) {
      List result = new ArrayList(this.getLength());

      for(int i = 0; i < this.getLength(); ++i) {
         String[] colAliases = this.getProjection(i).getColumnAliases(loc);
         ArrayHelper.addAll(result, colAliases);
         loc += colAliases.length;
      }

      return ArrayHelper.toStringArray((Collection)result);
   }

   public String[] getColumnAliases(int loc, Criteria criteria, CriteriaQuery criteriaQuery) {
      List result = new ArrayList(this.getLength());

      for(int i = 0; i < this.getLength(); ++i) {
         String[] colAliases = getColumnAliases(loc, criteria, criteriaQuery, this.getProjection(i));
         ArrayHelper.addAll(result, colAliases);
         loc += colAliases.length;
      }

      return ArrayHelper.toStringArray((Collection)result);
   }

   public String[] getColumnAliases(String alias, int loc) {
      for(int i = 0; i < this.getLength(); ++i) {
         String[] result = this.getProjection(i).getColumnAliases(alias, loc);
         if (result != null) {
            return result;
         }

         loc += this.getProjection(i).getColumnAliases(loc).length;
      }

      return null;
   }

   public String[] getColumnAliases(String alias, int loc, Criteria criteria, CriteriaQuery criteriaQuery) {
      for(int i = 0; i < this.getLength(); ++i) {
         String[] result = getColumnAliases(alias, loc, criteria, criteriaQuery, this.getProjection(i));
         if (result != null) {
            return result;
         }

         loc += getColumnAliases(loc, criteria, criteriaQuery, this.getProjection(i)).length;
      }

      return null;
   }

   private static String[] getColumnAliases(int loc, Criteria criteria, CriteriaQuery criteriaQuery, Projection projection) {
      return projection instanceof EnhancedProjection ? ((EnhancedProjection)projection).getColumnAliases(loc, criteria, criteriaQuery) : projection.getColumnAliases(loc);
   }

   private static String[] getColumnAliases(String alias, int loc, Criteria criteria, CriteriaQuery criteriaQuery, Projection projection) {
      return projection instanceof EnhancedProjection ? ((EnhancedProjection)projection).getColumnAliases(alias, loc, criteria, criteriaQuery) : projection.getColumnAliases(alias, loc);
   }

   public Type[] getTypes(String alias, Criteria criteria, CriteriaQuery criteriaQuery) {
      for(int i = 0; i < this.getLength(); ++i) {
         Type[] result = this.getProjection(i).getTypes(alias, criteria, criteriaQuery);
         if (result != null) {
            return result;
         }
      }

      return null;
   }

   public String[] getAliases() {
      List result = new ArrayList(this.getLength());

      for(int i = 0; i < this.getLength(); ++i) {
         String[] aliases = this.getProjection(i).getAliases();
         ArrayHelper.addAll(result, aliases);
      }

      return ArrayHelper.toStringArray((Collection)result);
   }

   public Projection getProjection(int i) {
      return (Projection)this.elements.get(i);
   }

   public int getLength() {
      return this.elements.size();
   }

   public String toString() {
      return this.elements.toString();
   }

   public boolean isGrouped() {
      for(int i = 0; i < this.getLength(); ++i) {
         if (this.getProjection(i).isGrouped()) {
            return true;
         }
      }

      return false;
   }
}
