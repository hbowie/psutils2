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

package com.powersurgepub.psutils2.records;

  import com.powersurgepub.psutils2.strings.*;
  
/**
   A rule for formatting a hyperlink field. <p>
   
   This code is copyright (c) 2001 by Herb Bowie of PowerSurge Publishing. 
   All rights reserved. <p>
   
   Version History: <ul><li>
      </ul>
  
   @author Herb Bowie (<a href="mailto:herb@powersurgepub.com">
           herb@powersurgepub.com</a>)<br>
           of PowerSurge Publishing (<A href="http://www.powersurgepub.com/software/">
           www.powersurgepub.com/software</a>)
  
   @version 2001/01/26 - Written to handle hyperlinks in Boeing documents.
 */
public class HyperlinkRule
  extends DataFormatRule {
  
  /** Characters to be scanned for. */
  private static final String STOPPERS = "/\\ ";
  
  /** ID for this DataFormatRule descendent. */
  public static final String HYPERLINK_RULE_CLASS_NAME = "HyperlinkRule";
  
  /** Character to be used to replace spaces in the input string. */
  private static final String HYPERLINK_SPACE_STRING = "%20";
  
  /**
     Constructs a hyperlink formatting rule with default values.
   */
  public HyperlinkRule () {
    super ();
  }
  
  /**
     Transforms a hyperlink string into a standard format.
    
     @return Date string in standard format.
    
     @param inData Hyperlink string as it may have come from an MS Office
                   document.
   */
  public String transform (String inData) {
    StringBuilder work = new StringBuilder ("");
    StringScanner scan = new StringScanner (inData);
    int startWord = 0;
    int endWord = 0;
    String word = "";
    char stopper = ' ';
    SuperString wordss;
    while (scan.moreChars()) {
      startWord = scan.getIndex();
      scan.stopAtChars (STOPPERS);
      endWord = scan.getIndex();
      word = scan.substring (startWord, endWord);
      stopper = scan.getNextChar();
      if ((work.length() == 0) && (word.length() > 0)) {
        wordss = new SuperString (word);
        if ((wordss.startsWithIgnoreCase ("http"))
          || (wordss.startsWithIgnoreCase ("file"))
          || (wordss.endsWithIgnoreCase (":"))) {
          // if we've already got a good prefix, then don't need to do anything
        } 
        else {
          if ((wordss.startsWithIgnoreCase ("www."))
            || (wordss.endsWithIgnoreCase (".com"))
            || (wordss.endsWithIgnoreCase (".org"))
            || (wordss.endsWithIgnoreCase (".net"))) {
            work.append ("http://");
          }
          else
          if (wordss.startsWithIgnoreCase ("nt-")) {
            work.append ("file://");
          } 
        } // end if not a good URL prefix
      } // end if just starting to build output string
      work.append (word);
      if (work.length() > 0) {
        if (scan.moreChars()) {
          if (stopper == ' ') {
            work.append (HYPERLINK_SPACE_STRING);
          }
          else {
            work.append ("/");
          } // end stopper not a space
        } // end if more characters to scan
      } // end if string being built already contains something
      if (scan.moreChars()) {
        scan.incrementIndex();
      } // end more characters
    } // end while more characters   
    return work.toString();
  } // end method transform

  /**
     Return the name of the class.
    
     @return The name of the class.
   */
  public String toString () {
    return HYPERLINK_RULE_CLASS_NAME;
  }
  
  /** 
     Test the class with standard test cases.
   */
  public static void test () {
    
    HyperlinkRule rule = new HyperlinkRule();
    System.out.println ("Testing " + rule.toString());
    testRule (rule, "nt-mes-20/a b/word.doc");
    testRule (rule, "//nt-mes-20/a b/word.doc");
    testRule (rule, "www.powersurgepub.com/software");
    testRule (rule, "//www.powersurgepub.com/software");
    testRule (rule, "http://www.powersurgepub.com");
    testRule (rule, "file://nt-mes-20\\busSupport\\Information Technology\\home.htm");
    testRule (rule, "home.html");
  }
  
  /**
     Convert one hyperlink and display input and output.
    
     @param inRule An instance of HyperlinkRule.
    
     @param inString A link to be converted.
   */
  public static void testRule (HyperlinkRule inRule, String inString) {
  
    System.out.println 
      (inString + " becomes " 
        + (String)inRule.transform(inString));
  }

} // end of class HyperlinkRule