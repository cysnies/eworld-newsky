package com.mysql.jdbc.profiler;

import com.mysql.jdbc.Extension;

public interface ProfilerEventHandler extends Extension {
   void consumeEvent(ProfilerEvent var1);
}
