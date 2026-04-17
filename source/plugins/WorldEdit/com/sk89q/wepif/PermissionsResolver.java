package com.sk89q.wepif;

public interface PermissionsResolver extends PermissionsProvider {
   void load();

   String getDetectionMessage();
}
