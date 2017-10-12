/*
 * Copyright 2017 - 2017 Herb Bowie
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

/**
 A string describing how a date is to recur at regular intervals. 

 @author Herb Bowie
 */

  import java.util.*;

public class RecursValue
    implements
        DataValue {
  
  public static final String[] DERIVED_SUFFIX = {
    
  };
  
  private StringBuilder   recurs = new StringBuilder();
  
  private             int interval = 0;
  public static final int   HALF      = -1;
  
  private             int unit = 0;
  public static final int   NONE      = 0;
  public static final int   DAYS      = 1;
  public static final int   WEEKS     = 2;
  public static final int   MONTHS    = 3;
  public static final int   QUARTERS  = 4;
  public static final int   YEARS     = 5;
  
  private             boolean weekdays = false;
  
  private             int dayOfWeek = 0;
  
  private             int dayOfMonth = 0;
  
  private             int weekOfMonth = 0;
  
  private             int sequence = 0;
  
  /**
   Constructor with no input. 
   */
  public RecursValue() {
    
  }
  
  /**
   Constructor with initial value. 
  
   @param recurs String describing how the date is to recur. 
  */
  public RecursValue(String str) {
    set(str);
  }
  
  /**
   Parse the recurs string and extract the relevant values. 
  
   @param recurs String describing how the date is to recur. 
  */
  public void set (String str) {

    recurs = new StringBuilder();
    interval = 0;
    unit = -1;
    weekdays = false;
    int i = 0;
    while (i < str.length()) {
      
      // Process the next word
      i = skipWhiteSpace(i, str);
      RecursWord recursWord = new RecursWord(i, str);
      i = recursWord.getEndOfWord();
      if (recursWord.length() > 0) {
        String word = recursWord.getWord();
        if (recurs.length() > 0) {
          recurs.append(" ");
        }
        recurs.append(word);
        
        if (recursWord.isNumber()) {
          interval = recursWord.getNumber();
        }
        else
        if (word.equalsIgnoreCase("of")) {
          sequence = interval;
          interval = 1;
        }
        if (word.equalsIgnoreCase("every")) {
          // Skip it
        }
        else
        if (word.equalsIgnoreCase("day") 
            || word.equalsIgnoreCase("days") 
            || word.equalsIgnoreCase("daily")) {
          unit = DAYS;
        }
        else
        if (word.equalsIgnoreCase("weekday")
            || word.equalsIgnoreCase("weekdays")) {
          unit = DAYS;
          weekdays = true;
        }
        else
        if (word.equalsIgnoreCase("week") 
            || word.equalsIgnoreCase("weeks") 
            || word.equalsIgnoreCase("weekly")) {
          unit = WEEKS;
        }
        else
        if (word.equalsIgnoreCase("month") 
            || word.equalsIgnoreCase("months")
            || word.equalsIgnoreCase("monthly")) {
          if (sequence > 0) {
            dayOfMonth = sequence;
            sequence = 0;
          }
          unit = MONTHS;
        }
        else
        if (word.equalsIgnoreCase("quarter") 
            || word.equalsIgnoreCase("quarters")
            || word.equalsIgnoreCase("quarterly")) {
          unit = QUARTERS;
        }
        else
        if (word.equalsIgnoreCase("year") 
            || word.equalsIgnoreCase("years")
            || word.equalsIgnoreCase("annual")
            || word.equalsIgnoreCase("annually")
            || word.equalsIgnoreCase("yearly")) {
          unit = YEARS;
        } else {
          int possibleDayOfWeek = StringDate.matchDayOfWeek(word);
          if (possibleDayOfWeek > 0) {
            dayOfWeek = possibleDayOfWeek;
            if (unit <= NONE) {
              unit = WEEKS;
            }
          }
        }
        
      } // end if we have a word to process
      i = skipWhiteSpace (i, str);
      
    } // end of input string
    
    if (unit > NONE && interval == 0) {
      interval = 1;
    }
    
  } // end of set method
  
  /**
   Skip the next patch of white space. 
  
   @param startingIndex Where to start looking. 
   @param str The string to look at. 
  
   @return The first position greater than or equal to the starting index
           that contains something other than white space (or the end of the
           string, if there are no more significant characters to be found).
  */
  public static int skipWhiteSpace(int startingIndex, String str) {
    int i = startingIndex;
    while (i < str.length() && Character.isWhitespace(str.charAt(i))) {
      i++;
    }
    return i;
  }
  
  /**
     Compares this data value to another and indicates which is greater.
    
     @return Zero if the fields are equal, negative if this field is less than value2,
             or positive if this field is greater than value2.
    
     @param  value2 Another data value to be compared to this one.
   */
  public int compareTo(DataValue value2) {
    
    if (value2 instanceof RecursValue) {
      return toString().compareTo(value2.toString());
    } else {
      return toString().compareTo(value2.toString());
    }
  }
  
  /**
   Increment a date by the specified recurs value. 
  
   @param strDate The starting date. 
  
   @return A string representing the new date. 
  */
  public String recur(StringDate strDate) {
    StringDate workStr = strDate;
    Calendar workCal = workStr.getCalendar();
    boolean bumped = false;
    switch (unit) {
      case DAYS:
        workCal.add(Calendar.DATE, interval);
        bumped = true;
        if (weekdays) {
          int dow = workCal.get(Calendar.DAY_OF_WEEK);
          while (dow == Calendar.SATURDAY || dow == Calendar.SUNDAY) {
            workCal.add(Calendar.DATE, 1);
            dow = workCal.get(Calendar.DAY_OF_WEEK);
          }
        }
        break;
      case WEEKS:
        workCal.add(Calendar.DATE, interval * 7);
        bumped = true;
        break;
      case MONTHS:
        workCal.add(Calendar.MONTH, interval);
        bumped = true;
        break;
      case QUARTERS:
        workCal.add(Calendar.MONTH, interval * 3);
        bumped = true;
        break;
      case YEARS:
        workCal.add(Calendar.YEAR, interval);
        bumped = true;
        break;
      default:
        break;
    }
    if (! bumped) {
      workCal.add(Calendar.DATE, 1);
    }
    if (dayOfMonth > 0) {
      workCal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
    }
    if (dayOfWeek > 0) {
      int dow = workCal.get(Calendar.DAY_OF_WEEK);
      while (dow != dayOfWeek) {
        workCal.add(Calendar.DATE, 1);
        dow = workCal.get(Calendar.DAY_OF_WEEK);
      } 
    }
    return StringDate.COMMON_FORMAT.format(workCal.getTime());
  }
  
  /**
   Does this value have any data stored in it? 
  
   @return True if data, false if empty. 
  */
  public boolean hasData() {
    return (recurs != null && recurs.length() > 0 
        && interval != 0 && unit > NONE);
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
  
  public String toString() {
    return recurs.toString();
  }
  
  /**
   Identify how many other fields can be derived from this one. 
  
   @return The possible number of derived fields. 
  */
  public int getNumberOfDerivedFields() {
    return DERIVED_SUFFIX.length;
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
  
  public void display() {
    System.out.println("RecursValue.display");
    System.out.println("  Parsed String = " + recurs.toString());
    System.out.println("  Interval      = " + String.valueOf(interval));
    System.out.println("  Unit          = " + String.valueOf(unit));
  }

}
