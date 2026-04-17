package fr.neatmonster.nocheatplus.checks.chat.analysis.engine.processors;

import fr.neatmonster.nocheatplus.checks.chat.analysis.MessageLetterCount;

public interface WordProcessor {
   String getProcessorName();

   float getWeight();

   float process(MessageLetterCount var1);

   void clear();
}
