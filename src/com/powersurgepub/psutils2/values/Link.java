/*
 * Copyright 2009 - 2017 Herb Bowie
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

package com.powersurgepub.psutils2.values;

  import com.powersurgepub.psutils2.strings.*;
  import java.io.*;

/**
 A Link (aka URL).

 @author Herb Bowie
 */
public class Link 
    implements
        DataValue {
  
  public static final String[] DERIVED_SUFFIX = {
    
  };
  
  /** 
    Part 1 of the url, delimited by a colon and zero or more slashes 
    (as in "http://" or "mailto:") 
   */
  private String   linkPart1 = "http://";
  
  /** The user name part of an e-mail address, if one is present. */
  private String   linkPart2 = "www.";
  
  /** */
  private String   linkPart3 = "";
  
  /** Anything following the domain name, except for a trailing slash. */
  private String   linkPart4 = "";
  
  /** An ending slash, if one is present. */
  private String   linkPart5 = "";
  
  public Link() {
    
  }
  
  /**
   Set this link to a URL pointing to the given file. 
  
   @param file The file to be linked to. 
  */
  public void set(File file) {
    set(StringUtils.fileToLink(file));
  }
  
  public void set(String urlString) {
    setLink(urlString);
  }
  
  public void setLink (String urlString) {

    // Look for part 1 of the url, delimited by a colon and zero or more
    // slashes (as in "http://" or "mailto:")
    linkPart1 = "";
    int i = 0;
    while (i < urlString.length()
        && i <= 8
        && urlString.charAt (i) != ':') {
      i++;
    }
    if (i < urlString.length()
        && urlString.charAt (i) == ':') {
      i++;
      while ((i) < urlString.length()
          && urlString.charAt (i) == '/') {
        i++;
      }
      linkPart1 = urlString.substring (0, i);
    } else {
      i = 0;
    }
    // Look for parts 2 and 3 of the url, including the domain name
    linkPart2 = "";
    linkPart3 = "";
    int start = i;
    int atSign = -1;
    int firstPeriod = -1;
    int periodCount = 0;
    while (i < urlString.length()
        && urlString.charAt(i) != '/') {
      if (urlString.charAt(i) == '@') {
        atSign = i;
      }
      else
      if (urlString.charAt(i) == '.') {
        periodCount++;
        if (periodCount == 1) {
          firstPeriod = i;
        } // end if first period
      } // end if a period
      i++;
    } // end while scanning for slash to end part 3
    int end = start;
    if (atSign > start) {
      end = atSign + 1;
    }
    else
    if (firstPeriod > start
        && periodCount > 1) {
      end = firstPeriod + 1;
    }
    if (end > start) {
      linkPart2 = urlString.substring (start, end);
      start = end;
    }
    linkPart3 = urlString.substring (start, i);
    // Look for part 4 of the url, following the domain name
    linkPart4 = "";
    linkPart5 = "";
    if (i < urlString.length()) {
      if (urlString.charAt(urlString.length() - 1) == '/') {
        linkPart4 = urlString.substring (i, urlString.length() - 1);
        linkPart5 = "/";
      } else {
        linkPart4 = urlString.substring (i, urlString.length());
      }
    }
  } // end setURL method
  
  /**
   Does this value have any data stored in it? 
  
   @return True if data, false if empty. 
  */
  public boolean hasData() {
    return (hasLink());
  }
  
  public String toString() {
    return getLink();
  }
  
  /**
     Compares this data value to another and indicates which is greater.
    
     @return Zero if the fields are equal, negative if this field is less than value2,
             or positive if this field is greater than value2.
    
     @param  value2 Another data value to be compared to this one.
   */
  public int compareTo(DataValue value2) {
    return toString().compareTo(value2.toString());
  }
  
  public File getLinkAsFile() {
    String linkStr = StringUtils.tweakAnyLink(getLink(), true, false, false, "");
    StringBuilder fileStr = new StringBuilder(linkStr);
    if (linkStr.startsWith("file:")) {
      fileStr.delete(0, 5);
    }
    while (fileStr.length() > 1
        && fileStr.charAt(1) == '/') {
      fileStr.deleteCharAt(0);
    }
    File file = new File(fileStr.toString());
    return file;
  }
  
  public String getURLasString () {
    return getLink();
  }
  
  public String getLink () {
    return linkPart1 + linkPart2 + linkPart3 + linkPart4 + linkPart5;
  }
  
  public String getLinkPart1 () {
    return linkPart1;
  }

  public String getLinkPart2 () {
    return linkPart2;
  }

  public String getLinkPart3 () {
    return linkPart3;
  }

  public String getLinkPart4 () {
    return linkPart4;
  }

  public String getLinkPart5 () {
    return linkPart5;
  }

  public boolean hasLink () {
    return (linkPart3.length() > 0);
  }

  public boolean blankLink () {
    return (linkPart3.length() == 0);
  }

  public String getLinkKey () {
    return linkPart3 + linkPart1 + linkPart2 + linkPart4;
  }
  
  /**
   Identify how many other fields can be derived from this one. 
  
   @return The possible number of derived fields. 
  */
  public int getNumberOfDerivedFields() {
    return DERIVED_SUFFIX.length;
  }
  
  /**
   Return a suffix that will uniquely identify this derivation. The suffix 
   need not, and should not, begin with a hyphen or any other punctuation. 
  
   @param d An index value indicating which of the possible derived fields
            is desired. 
  
   @return The suffix identifying the requested derived field, or null if 
           the index is out of range of the possible fields. 
  */
  public String getDerivedSuffix(int d) {
    if (d < 0 || d >= getNumberOfDerivedFields()) {
      return null;
    } else {
      return DERIVED_SUFFIX [d];
    }
  }
  
  /**
   Return the derived field, in String form. 
  
   @param d An index value indicating which of the possible derived fields
            is desired. 
  
   @return The derived field requested, or null if the index is out of range
           of the possible fields. 
  */
  public String getDerivedValue(int d) {
    switch (d) {
      case 0:
      default:
        return null;
    }
  }

}
