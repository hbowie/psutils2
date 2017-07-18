/*
 * Copyright 1999 - 2013 Herb Bowie
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

package com.powersurgepub.psutils2.markup;

  import com.powersurgepub.psutils2.textio.*;

/**
 *
 * Writes characters and lines to a string field. 
 */
public class MarkupStringMaker 
    implements TextLineWriter {
  
  private String lineSep;
  
  StringBuffer str = new StringBuffer();
  
  /** Creates a new instance of StringMaker */
  public MarkupStringMaker() {
    lineSep = System.getProperty ("line.separator");
  }
  
  public boolean openForOutput () {
    str = new StringBuffer();
    return true;
  }
  
  public boolean writeLine (String s) {
    write (s);
    newLine();
    return true;
  }
  
  public boolean write (String s) {
    str.append (s);
    return true;
  }
  
  public boolean newLine () {
    str.append (lineSep);
    return true;
  }
  
  public boolean flush () {
    return true;
  }
  
  public boolean close () {
    return true;
  }
  
  public String toString () {
    return str.toString();
  }
  
  /**
   Return the file path, or other string identifying the output destination.

   @return The file path, or other string identifying the output destination.
   */
  public String getDestination () {
    return toString();
  }
  
  public boolean isOK () {
    return true;
  }
  
}
