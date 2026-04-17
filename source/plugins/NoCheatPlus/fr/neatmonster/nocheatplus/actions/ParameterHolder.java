package fr.neatmonster.nocheatplus.actions;

public interface ParameterHolder extends ActionData {
   String getParameter(ParameterName var1);

   void setParameter(ParameterName var1, String var2);

   boolean needsParameters();
}
