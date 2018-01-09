/*
 * Copyright 2016 - 2016 Herb Bowie
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

package com.powersurgepub.psutils2.textio;

/**
 Utility methods for dealing with text files.

 @author Herb Bowie
 */
public class TextUtils {
  
  /**
   Copy a text file from an input file to an output file. 
  
   @param in  The TextLineReader to use as input. 
   @param out The TextLineWriter to use as output. 
  
   @return True if everything went OK; false otherwise. 
  */
  public static boolean copyFile(TextLineReader in, TextLineWriter out) {
    boolean ok = true;
    ok = in.open();
    if (ok) {
      ok = out.openForOutput();
    }
    String line = "";
    if (ok) {
      line = in.readLine();
    }
    while (ok && in.isOK() && (! in.isAtEnd())) {
      ok = out.writeLine(line);
      line = in.readLine();
    }
    if (ok) {
      ok = in.close();
    }
    if (ok) {
      ok = out.close();
    }
    return ok;
  }

}
