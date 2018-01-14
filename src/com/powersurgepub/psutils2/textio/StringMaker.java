/*
 * Copyright 1999 - 2015 Herb Bowie
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

  import java.io.*;

/**
 
 Writes characters and lines to a string field. Implements TextLineWriter.
 */
public class StringMaker 
    implements TextLineWriter {
  
  private String lineSep;
  
  StringBuffer str = new StringBuffer();
  
  /** Creates a new instance of StringMaker */
  public StringMaker() {
    lineSep = System.getProperty ("line.separator");
  }
  
  public void setLineSep(String lineSep) {
    this.lineSep = lineSep;
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
    // no need to do anything
    return true;
  }
  
  public boolean close () {
    // no need to do anything
    return true;
  }
  
  public boolean isOK () {
    return true;
  }
  
  public File getFile () {
    return null;
  }
  
  /**
   Returns the string made by the writer. 
  
   @return The resulting string. 
  */
  @Override
  public String toString () {
    return str.toString();
  }
  
  public String getDestination () {
    return ("String");
  }
  
}
