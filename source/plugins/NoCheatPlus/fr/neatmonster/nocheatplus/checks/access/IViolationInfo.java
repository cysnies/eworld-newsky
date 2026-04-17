package fr.neatmonster.nocheatplus.checks.access;

import fr.neatmonster.nocheatplus.actions.ParameterHolder;

public interface IViolationInfo extends ParameterHolder {
   double getAddedVl();

   double getTotalVl();

   boolean hasCancel();
}
