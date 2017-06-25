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

  import com.powersurgepub.psutils2.basic.*;
  import com.powersurgepub.psutils2.strings.*;
  
/**
   A rule for formatting a date
   field into a standard format. <p>
   
  
   @author Herb Bowie (<a href="mailto:herb@powersurgepub.com">
           herb@powersurgepub.com</a>)<br>
           of PowerSurge Publishing (<A href="http://www.powersurgepub.com/software/">
           www.powersurgepub.com/software</a>)
  
 */
public class DateRule
  extends DataFormatRule {
  
  /** ID for this DataFormatRule descendent. */
  public static final String DATE_RULE_CLASS_NAME = "DateRule";
  
  /** Character to be used as a separator in the date string. */
  private    String     slashString = "/";
  
  /** 
     Input date format, defining sequence of day, month and year fields. 
     Default is month, day, and then year. 
   */
  private    String     inFormat  = "mdy";
  
  /** 
     Output date format, defining sequence of day, month and year fields. 
     Default is month, day and 2-digit year. 
   */
  private    String     outFormat = "mdy";
  
  /**
     Constructs a date formatting rule with default values.
   */
  public DateRule () {
    super ();
  }
  
  /**
     Sets the expected sequence of month, day and year fields in the 
     input dates.
    
     @param inFormat Expected input date format, consisting of some 
                     combination of d for day, m for month, y for a 
                     two-digit year, or c for a four-digit year.
   */
  public void setInFormat (String inFormat) {
    this.inFormat = inFormat.toLowerCase();
  }
  
  /**
     Sets the expected sequence of month, day and year fields in the 
     output dates.
    
     @param outFormat Expected output date format, consisting of some 
                      combination of d for day, m for month, y for a 
                      two-digit year, or c for a four-digit year.
   */
  public void setOutFormat (String outFormat) {
    this.outFormat = outFormat.toLowerCase();
  }
  
  /**
     Transforms a date string into a standard format.
    
     @return Date string in standard format (mm/dd/yy).
    
     @param inData Date string in some form of month/day/year format,
                   with or without separators.
   */
  public String transform (String inData) {
    String inString = inData.trim();
    if (inString.equals ("")) {
      return inString;
    } else {
      StringScanner scan = new StringScanner (inString);
      char mdy   = ' ';
      int  year  = 0;
      int  month = 0;
      int  day   = 0;
      for (int i = 0; i < 3; i++) {
        mdy = inFormat.charAt(i);
        switch (mdy) {
          case 'm': 
            month = scan.extractInteger (12);
            break;
          case 'd':
            day   = scan.extractInteger (31);
            break;
          case 'y':
          case 'c':
            year  = scan.extractInteger (3000);
            break;
        } // end switch (mdy)
      } // end for
   
      StringBuilder outString = new StringBuilder ();
      for (int i = 0; i < 3; i++) {
        if (i > 0) {
          outString.append (slashString);
        }
        mdy = outFormat.charAt(i);
        switch (mdy) {
          case 'm': 
            outString.append (StringUtils.stringFromInt (month, 2));
            break;
          case 'd':
            outString.append (StringUtils.stringFromInt (day, 2));
            break;
          case 'y':
            outString.append (StringUtils.stringFromInt (year, 2));
            break;
          case 'c':
            if (year < 100) {
              if (year < GlobalConstants.PIVOT_YEAR) {
                year = 2000 + year;
              }
              else {
                year = 1900 + year;
              }
            } // end if
            outString.append (StringUtils.stringFromInt (year, 4));
            break;
          } // end switch
        } // end for loop
      return outString.toString();
    } // end else 
  } // end method transform

  /**
     Return the name of the class.
    
     @return The name of the class.
   */
  public String toString () {
    return DATE_RULE_CLASS_NAME;
  }
  
  /** 
     Test the class with standard test cases.
   */
  public static void test () {
    
    DateRule rule = new DateRule();
    System.out.println ("Testing " + rule.toString());
    testRule (rule, "050551", "mdy", "mdy");
    testRule (rule, "5/5/51", "mdy", "mdy");
    testRule (rule, "9/1/25", "mdy", "mdy");
    testRule (rule, "10/2/54", "mdy", "mdy");
    testRule (rule, "5-05-51", "mdy", "mdy");
    testRule (rule, "5-5-1951", "mdy", "mdy");
    testRule (rule, "02.01.87", "mdy", "cmd");
    testRule (rule, "87-02-01", "ymd", "cmd");
    testRule (rule, "", "mdy", "mdy");
  }
  
  /**
     Convert one date and display input and output.
    
     @param inRule An instance of DateRule.
    
     @param inString A date to be converted.
    
     @param inFormat Format for input dates.
     @param outFormat Format for output dates.
   */
  public static void testRule (DateRule inRule, String inString,
      String inFormat, String outFormat) {
  
    inRule.setInFormat (inFormat);
    inRule.setOutFormat (outFormat);
    System.out.println 
      (inString + " (" + inFormat + ") becomes " 
        + (String)inRule.transform(inString) + " (" + outFormat + ")");
  }

} // end of class DateRule