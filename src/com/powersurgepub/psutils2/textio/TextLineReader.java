/*
 * Copyright 1999 - 2014 Herb Bowie
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
 The most basic interface for reading lines from somewhere.

  <p>The following classes implement this interface. </p>

 <ul>
   <li>FileLineReader - Reads lines from a file. </li>
 </ul>
 */
public interface TextLineReader {
  
  /**
   Open for input.
  
   @return True if everything opened OK. 
  */
  public boolean open ();
  
  /**
     Returns the next line from the text file. <p>
     
     If the text file has not yet been opened, then the first execution of this 
     method will automatically attempt to open the file. When the end of the file 
     is encountered, an empty String will be returned as the next line, the 
     atEnd variable will be turned on, and the file will be closed. 
     
     @return    The next line in the file (or an empty string at end of file).
   */
  public String readLine ();
  
  /**
   Close the input file. 
  
   @return True if no exceptions. 
  */
  public boolean close();
  
  /**
   Any exceptions so far?
  
   @return True if no exceptions; false otherwise. 
  */
  public boolean isOK ();
  
  /**
   Indicates whether the file has reached its end.
    
   @return    True if file is at end, false if we haven't yet hit the end.
   */
  public boolean isAtEnd();
  
  /**
   Return the file path or URL in the form of a string.
  
   @return The file path or URL. 
  */
  public String toString();

}
