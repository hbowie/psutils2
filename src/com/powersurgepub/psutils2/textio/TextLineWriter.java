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

package com.powersurgepub.psutils2.textio;

/**

 The most basic interface for writing characters and lines somewhere. 
 
 <p>The following classes implement this interface. </p>

 <ul>
   <li>ClipboardMaker - Writes lines to the System clipboard. </li>
   <li>Writes text lines to a local file. </li>
   <li>Writes characters and lines to a string field. </li>
 </ul>
 
 */
public interface TextLineWriter {  
  
  public boolean openForOutput ();
  
  public boolean write (String s);
  
  public boolean writeLine (String s);
  
  public boolean newLine ();
  
  public boolean flush ();
  
  public boolean close();
  
  public boolean isOK ();

  /**
   Return the file path, or other string identifying the output destination.

   @return The file path, or other string identifying the output destination.
   */
  public String getDestination ();

}
