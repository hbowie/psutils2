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

  import com.powersurgepub.psutils2.records.*;

  import java.io.*;
  
/**
   An application that prints a file of tab-delimited records. 
  
   @author Herb Bowie (<a href="mailto:herb@powersurgepub.com">
           herb@powersurgepub.com</a>)<br>
           of PowerSurge Publishing (<A href="http://www.powersurgepub.com/software/">
           www.powersurgepub.com/software</a>)
 */
public class TabDelimPrint {
  
  /**
     Prints a tab-delimited file whose name is passed as the first run-time
     parameter. 
   */
  public static void main (String [] parms) 
      throws IOException {
    DataDictionary dict = new DataDictionary ();
    System.out.println (parms[0]);
    TabDelimFile inFile1 = new TabDelimFile (parms[0]);
    System.out.println (inFile1.getPath());
    inFile1.openForInput (dict);
    System.out.println (inFile1.toString ());
    DataRecord inRec;
    do {
      inRec = inFile1.nextRecordIn ();
      if (inRec != null) {
        System.out.println ("Record number " 
          + Integer.toString(inFile1.getRecordNumber()));
        inRec.startWithFirstField ();
        while (inRec.hasMoreFields ()) {
          DataField field = inRec.nextField ();
          System.out.println (field.toString ());
        }
      }
    } while (inRec != null);
  }
}
