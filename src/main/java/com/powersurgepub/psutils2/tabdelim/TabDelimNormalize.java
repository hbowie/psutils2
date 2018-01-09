/*
 * Copyright 1999 - 2017 Herb Bowie
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.powersurgepub.psutils2.tabdelim;

  import com.powersurgepub.psutils2.logging.*;
  import com.powersurgepub.psutils2.records.*;

  import java.io.*;
  
/**
   An application that copies one tab-delimited file to another,
   normalizing the data in the process.
  
   @author Herb Bowie (<a href="mailto:herb@powersurgepub.com">
           herb@powersurgepub.com</a>)<br>
           of PowerSurge Publishing (<A href="http://www.powersurgepub.com">
           www.powersurgepub.com</a>)
  
 */
public class TabDelimNormalize {
  
  public static void main (String [] parms) 
      throws IOException {
    Logger log = new Logger (new LogOutput());
    log.setLogAllData (false);
    log.recordEvent 
      (new LogEvent 
        (LogEvent.NORMAL, "TabDelimNormalize main method beginning"));
    DataDictionary dict = new DataDictionary ();
    if (parms.length > 2) {
      try {
        TabDelimFile dictFile = new TabDelimFile (parms[2]);
        dict.load (dictFile);
      } catch (FileNotFoundException e) {
      }
    }
    TabDelimFile inFiles = new TabDelimFile (parms[0]);
    BoeingDocsNormalizer inFile1 = new BoeingDocsNormalizer (inFiles);
    inFile1.setLog (log);
    inFile1.setDataLogging (false);
    inFile1.openForInput (dict);
    System.out.println (inFile1.toString ());
    TabDelimFile outFile1 = new TabDelimFile (parms[1]);
    outFile1.setLog (log);
    outFile1.setDataLogging (false);
    outFile1.openForOutput (inFile1.getRecDef());
    DataRecord inRec;
    do {
      inRec = inFile1.nextRecordIn ();
      if (inRec != null) {
        outFile1.nextRecordOut (inRec);
      }
    } while (inRec != null);
    inFile1.close();
    outFile1.close();
    if (parms.length > 2) {
      TabDelimFile dictFile = new TabDelimFile (parms[2]);
      dict.store (dictFile);
    }
    log.recordEvent 
      (new LogEvent 
        (LogEvent.NORMAL, "TabDelimNormalize main method ending"));
  } // end main method
} // end TabDelimNormalize Class
