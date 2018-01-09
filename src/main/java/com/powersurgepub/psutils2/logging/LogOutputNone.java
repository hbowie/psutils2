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

package com.powersurgepub.psutils2.logging;

/**
   A null output destination for log records. This class accepts
   log input but does not write any output. This class is intended to
   be used when the user has chosen the option of not seeing any
   log output.

   @author Herb Bowie 
 */

public class LogOutputNone 
    extends LogOutput {

  /**
     The "noarg" constructor. The resulting object requires no further
     modification. 
   */

  public LogOutputNone () {
    super();
  } // end LogOutputNone constructor

  /**
     Accepts a line of output but does not send it anywhere.

     @param line  line of data to be written to the log file.
   */

  public void writeLine (String line) {

  } // end writeLine method

  

  /**
     Returns the object as a String, with a standard identifier.

     @return the text "LogOutputNone".
   */

  public String toString () {

    return "LogOutputNone";

  } // end toString method



} // end LogOutputNone class

