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
   This application copies a tab-delimited contacts file, 
   reformatting the data into standard formats as it goes,
   and separating it into two output files. The program requires
   three run-time parameters, and a fourth one is optional. They are:
   name of input file, name of first output file, name of second 
   output file, and name of data dictionary file. </p>
   
   This code is copyright (c) 1999-2000 by Herb Bowie of PowerSurge Publishing. 
   All rights reserved. <p>
   
   Version History: <ul><li>
      </ul>
  
   @author Herb Bowie (<a href="mailto:herb@powersurgepub.com">
           herb@powersurgepub.com</a>)<br>
           of PowerSurge Publishing (<A href="http://www.powersurgepub.com/software/">
           www.powersurgepub.com/software</a>)
  
   @version 00/06/12 - First created.
 */
public class TabDelimSplit {
  
  public static void main (String [] parms) 
      throws IOException { 
    Logger log = new Logger (new LogOutput());
    log.setLogAllData (false);
    log.recordEvent 
      (new LogEvent 
        (LogEvent.NORMAL, "TabDelimSplit main method beginning"));
        
    DataDictionary dict = new DataDictionary ();
    if (parms.length > 3) {
      try {
        TabDelimFile dictFile = new TabDelimFile (parms[3]);
        dict.load (dictFile);
      } catch (FileNotFoundException e) {
      }
    }
    
    TabDelimFile inFile1 = new TabDelimFile (parms[0]);
    inFile1.setLog (log);
    inFile1.setDataLogging (true);
    inFile1.openForInput (dict);
    
    TabDelimFile outFile1 = new TabDelimFile (parms[1]);
    outFile1.setLog (log);
    outFile1.setDataLogging (false);
    outFile1.openForOutput (inFile1.getRecDef());
    
    TabDelimFile outFile2 = new TabDelimFile (parms[2]);
    outFile2.setLog (log);
    outFile2.setDataLogging (false);
    outFile2.openForOutput (inFile1.getRecDef());

    String categories;
    DataRecord inRec;

    do {
      inRec = inFile1.nextRecordIn ();
      if (inRec != null) {
        categories = inRec.getFieldData("custom7").toLowerCase(); 
        if ((categories.indexOf ("family") > -1) 
          || (categories.indexOf ("co-worker") > -1) 
          || (categories.indexOf ("friend") > -1) 
          || (categories.indexOf ("client") > -1) 
          || (categories.indexOf ("christmas") > -1)) {
          outFile1.nextRecordOut (inRec);
        } 
        else
        if ((categories.indexOf ("publisher") > -1) 
          || (categories.indexOf ("bookstore") > -1)
          || (categories.indexOf ("retailer") > -1)
          || (categories.indexOf ("printer") > -1)
          || (categories.indexOf ("newspaper") > -1)
          || (categories.indexOf ("magazine") > -1)
          || (categories.indexOf ("distributor") > -1)
          || (categories.indexOf ("web site") > -1)
          || (categories.indexOf ("wholesaler") > -1)
          || (categories.indexOf ("galleys") > -1)
          || (categories.indexOf ("reviewer") > -1)
          || (categories.indexOf ("advertising") > -1)
          || (categories.indexOf ("abi form") > -1)
          || (categories.indexOf ("consultant") > -1)
          || (categories.indexOf ("radio show") > -1)
          || (categories.indexOf ("reference") > -1)
          || (categories.indexOf ("govt. office") > -1)
          || (categories.indexOf ("send book") > -1)
          || (categories.indexOf ("tv show") > -1)
          || (categories.indexOf ("ps interest") > -1)
          || (categories.indexOf ("book club") > -1)
          || (categories.indexOf ("abpa") > -1)
          || (categories.indexOf ("library") > -1)
          || (categories.indexOf ("ps customer") > -1)
          || (categories.indexOf ("journalist") > -1)
          ) {      
          outFile2.nextRecordOut (inRec);
        } else {
          outFile1.nextRecordOut (inRec);
        }
      } // end if not null
    } while (inRec != null);
    inFile1.close();
    outFile1.close();
    outFile2.close();
    if (parms.length > 3) {
      TabDelimFile dictFile = new TabDelimFile (parms[3]);
      dict.store (dictFile);
    }
    log.recordEvent 
      (new LogEvent 
        (LogEvent.NORMAL, "TabDelimSplit main method ending"));
  } // end main method
} // end TabDelimSplit class
