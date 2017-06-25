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

package com.powersurgepub.psutils2.textio;

  import com.powersurgepub.psutils2.basic.*;

/**
 Implements the TextLineReader interface with an input String.

 @author Herb Bowie
 */
public class StringLineReader 
    implements TextLineReader {
  
  public static final String HTML_EOL = "<!--EOL-->";
  
  private             String  s;
  private             int     i     = 0;
  private             boolean atEnd = false;
  
  /**
   Constructor.
  
   @param s The string to be used as input. 
  */
  public StringLineReader(String s) {
    this.s = s;
  }
 
  /**
   Ready the input source to be read. 
   
   @return 
  */
  public boolean open () {
    i = 0;
    atEnd = false;
    return true;
  }
  
  /**
   Read the next line from the input String. End of line characters or Strings
   are not returned. Line endings are denoted by a Line Feed or Carriage Return
   (each optionally followed by the other), or an HTML comment enclosing an
   EOL marker: <!--EOL-->.
   
   @return The next line, or null if end of file.
  */
  public String readLine () {
    int start = i;
    while (i < s.length()
        && s.charAt(i) != GlobalConstants.CARRIAGE_RETURN
        && s.charAt(i) != GlobalConstants.LINE_FEED
        && (! match(s, i, HTML_EOL))) {
      i++;
    }
    int end = i;
    
    if (i >= s.length()) {
      // No need to further adjust index
    }
    else
    if (s.charAt(i) == GlobalConstants.CARRIAGE_RETURN) {
      i++;
      if (match(s, i, GlobalConstants.LINE_FEED_STRING)) {
        i++;
      }
    }
    else
    if (s.charAt(i) == GlobalConstants.LINE_FEED) {
      i++;
      if (match(s, i, GlobalConstants.CARRIAGE_RETURN_STRING)) {
        i++;
      }
    } else {
      i = i + HTML_EOL.length();
    }
    
    // See if we have anything to return
    if (start >= s.length()) {
      atEnd = true;
      return "";
    } else {
      return s.substring(start, end);
    }
  }
  
  private boolean match (String s, int i, String sMatch) {
    int j = i + sMatch.length();
    if (i >= s.length() || j > s.length()) {
      return false;
    } else {
      return (s.substring(i, j).equalsIgnoreCase(sMatch));
    }
  }
  
  public boolean close() {
    return true;
  }
  
  public boolean isOK () {
    return true;
  }
  
  public boolean isAtEnd() {
    return atEnd;
  }
  
  public int length() {
    return s.length();
  }

}
