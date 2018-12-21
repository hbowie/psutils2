/*
 * Copyright 2010 - 2017 Herb Bowie
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

  import java.text.*;
  import java.util.*;

/**
 Representation of a full or partial date as a string, with constituent fields
 going from major (year) to minor. 

 @author Herb Bowie
 */
public class StringDate 
    implements
        Comparable,
        DataValue {
  
  public static final String[] DERIVED_SUFFIX = {
    
  };
  
  public static final     String[] MONTH_NAMES = {
      "January",
      "February",
      "March",
      "April",
      "May",
      "June",
      "July",
      "August",
      "September",
      "October",
      "November",
      "December"
  };
  
  public static final     String[] DAY_OF_WEEK_NAMES = {
    "Sunday",
    "Monday",
    "Tuesday",
    "Wednesday",
    "Thursday",
    "Friday",
    "Saturday"
  };
  
  public     static final     SimpleDateFormat  SHORT_FORMAT 
      = new SimpleDateFormat("EEE MMM dd");
  public     static final     SimpleDateFormat  YMD_FORMAT   
      = new SimpleDateFormat("yyyy-MM-dd");
  public     static final     SimpleDateFormat  YM_FORMAT    
      = new SimpleDateFormat("yyyy-MM");
  public     static final     SimpleDateFormat  READABLE_FORMAT
      = new SimpleDateFormat("EEE dd-MMM-yyyy");
  public     static final     SimpleDateFormat  COMMON_FORMAT
      = new SimpleDateFormat("dd MMM yyyy");
  public     static final     SimpleDateFormat  YMDHMS_FORMAT
      = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  public     static final     String            NEXT_YEAR = "next year";
  private    static final     Calendar          TODAY = Calendar.getInstance();
  private    static final     String            TODAY_YMD;
  private    static final     String            TODAY_YM;
  private    static final     int               CURRENT_YEAR;
  private    static final     int               CURRENT_MONTH;
  
  private    String           strDate = null;
  
  private    StringBuilder    year1 = new StringBuilder();
  private    StringBuilder    year2 = new StringBuilder();
  private    StringBuilder    opYear = new StringBuilder();
  
  private    boolean          nextYear = false;
  
  private    StringBuilder    word = new StringBuilder();
  private    boolean          numbers = false;
  private    boolean          letters = false;
  private    boolean          colon = false;
  private    boolean          lookingForTime = false;
  private    boolean          startOfDateRangeCompleted = false;
  
  private    String           yyyy = "";
  private    String           mm = "";
  private    String           dd = "";
  private    StringBuilder    start = new StringBuilder();
  private    StringBuilder    end = new StringBuilder();
  
  static {
    CURRENT_YEAR = TODAY.get(Calendar.YEAR);
    CURRENT_MONTH = TODAY.get(Calendar.MONTH);
    TODAY_YMD = YMD_FORMAT.format(TODAY.getTime());
    TODAY_YM  = YM_FORMAT.format(TODAY.getTime());
  }
  
  /**
   Return today's date in Year-Month-Day format. 
  
   @return Current date in yyyy-mm-dd format. 
  */
  public static String getTodayYMD() {
    return TODAY_YMD;
  }

  /**
    Get today's date and time in a sortable format.

    @return String representing current date and time in a sortable format.
   */
  public static String getNowYMDHMS() {
    Calendar now = Calendar.getInstance();
    return YMDHMS_FORMAT.format(now.getTime());
  }
  
  /**
   Return today's date in Year-Month format. 
  
  @return Current date in yyyy-mm format. 
  */
  public static String getTodayYM() {
    return TODAY_YM;
  }
  
  /**
   Return today's date in a format such as "13 Apr 2017". 
  
   @return Today's date, formatted. 
  */
  public static String getTodayCommon() {
    return COMMON_FORMAT.format(TODAY.getTime());
  }
  
  /**
   See if the passed string matches the name of one of the days of the week.
  
   @param str A complete or partial name of a day of week, in either upper or 
              lower case. 
  
   @return 1 - 7 to represent Sunday through Saturday, if a match was found; 
           otherwise something less than 1. 
  
  */
  public static int matchDayOfWeek(String str) {
    int result = -1;
    int i = 0;
    int len = str.length();
    String match = str.toLowerCase();
    while (i < 7 && result < 0) {
      String dayOfWeekName = DAY_OF_WEEK_NAMES[i];
      if (len <= dayOfWeekName.length()
          && match.equalsIgnoreCase(dayOfWeekName.substring(0, len))) {
        result = i + 1;
      } // end if match
      i++;
    } // end while looking for match
    return result;
  }
  
  public StringDate() {
    strDate = getShort();
  }
  
  /**
   Parse a field containing an operating year.
  
   @param years This could be a single year, or a range containing two
                consecutive years. If a range, months of July or later
                will be assumed to belong to the earlier year, with months
                of June or earlier will be assumed to belong to the 
                later year. 
  
   @return True if an operating year was found in the passed String.
  */
  public boolean parseOpYear (String years) {

    year1 = new StringBuilder();
    year2 = new StringBuilder();

    int i = 0;
    int yearCount = 0;
    while (i < years.length()) {
      char c = years.charAt(i);
      if (Character.isDigit(c)) {
        if (yearCount == 0 && year1.length() < 4) {
          year1.append(c);
        } 
        else 
        if (yearCount == 1 && year2.length() < 4) {
          year2.append(c);
        }
      } 
      else
      if (year1.length() > 0 && yearCount == 0) {
        yearCount++;
      }
      else
      if (year2.length() > 0 && yearCount == 1) {
        yearCount++;
      }
      i++;
    } // end while more chars to examine
    
    // Assume 21st century, if not specified
    if (year1.length() == 2) {
      year1.insert(0, "20");
    }
    if (year2.length() == 2) {
      year2.insert(0, "20");
    }

    opYear = new StringBuilder();
    if (year1.length() > 0) {
      opYear.append(year1);
      if (year2.length() > 0) {
        opYear.append(" - ");
        opYear.append(year2);
      }
    }
    
    return (year1.length() == 4);
  }
  
  /**
   If the status of an item is "Next Year", then adjust the year to be 
   the following year. 
  
   @param nextYearStr The status of an item. If it says "Next Year", then 
                    adjust the year to be a nextYear year. 
  */
  public void setNextYear(String nextYearStr) {
    String nextYearLower = nextYearStr.toLowerCase();
    if (nextYearLower.indexOf(NEXT_YEAR) >= 0) {
      setNextYear(true);
    } 
    else {
      setNextYear(false);
    } 
  }
  
  /**
   If this is a nextYear item, then adjust the year to be a nextYear 
   year. 
  
   @param nextYear If true, then adjust the year to be a nextYear year. 
  */
  public void setNextYear(boolean nextYear) {
    this.nextYear = nextYear;
  }
  
  /**
   Returns the operating year, which could be a single year, or a range
   of consecutive years. 
  
   @return Operating year. 
  */
  public String getOpYear() {
    return opYear.toString();
  }
  
  /**
   Set the value from a String. 
  
   @param value The value as a string. 
  */
  public void set(String value) {
    parse(value);
  }
  
  public void set(Date date) {
    parse(COMMON_FORMAT.format(date));
  }
  
  /**
   Parse a human-readable date string.
  
   @param when Human-readable date string.
  */
  public void parse(StringBuilder when) {
    parse(when.toString());
  }
  
  /**
   Parse a human-readable date string.
  
   @param when Human-readable date string. 
  */
  public void parse(String when) {
    
    strDate = when;

    yyyy = "";
    mm = "";
    dd = "";
    start.setLength(0);
    end.setLength(0);
    lookingForTime = false;
    startOfDateRangeCompleted = false;

    int i = 0;
    char c = ' ';
    char lastChar = ' ';
    resetWhenWord();

    // Examine one character at a time, building words as we go. 
    while (i <= when.length()) {
      
      // Get next character
      lastChar = c;
      if (i < when.length()) {
        c = when.charAt(i);
      } else {
        c = ' ';
      }
      
      // Now parse into words
      if (numbers && c == ':') {
        lookingForTime = true;
        colon = true;
        word.append(c);
      }
      else
      if (Character.isDigit(c)) {
        if (letters) {
          processWhenLetters();
        }
        numbers = true;
        word.append(c);
      }
      else
      if (Character.isLetter(c)) {
        if (numbers) {
          processWhenNumbers();
        }
        letters = true;
        word.append(c);
      } else {
        // Not a digit or a letter, so interpret as punctuation or white space. 
        if (letters && word.length() > 0) {
          processWhenLetters();
        }
        else
        if (numbers && word.length() > 0) {
          processWhenNumbers();
          if (c == ',' && dd.length() > 0) {
            lookingForTime = true;
          }
        }
        if (c == '-'
            && lastChar == ' '
            && mm.length() > 0
            && dd.length() > 0
            && (! lookingForTime)) {
          startOfDateRangeCompleted = true;
        }
      }
      i++;
    } // end while more length characters

    // Fill in year if not explicitly stated
    if (yyyy.length() == 0 && mm.length() > 0) {
      int month = Integer.parseInt(mm);
      if (year2.length() > 0 && month < 7) {
        yyyy = year2.toString();
      } else {
        yyyy = year1.toString();
      }
      int year = 2012;
      try {
        year = Integer.parseInt(yyyy);
      } catch (NumberFormatException e) {
        // do nothing
      }
      while (nextYear
          && ((year < CURRENT_YEAR)
            || (year == CURRENT_YEAR && month <= CURRENT_MONTH))) {
        year++;
        yyyy = zeroPad(year, 4);
      }
    }
    
    // System.out.println(when + " parsed as" +
    //     " mm: " + mm +
    //     " dd: " + dd +
    //     " yyyy: " + yyyy);
  } // end parse method
  
  /**
   End of a string of letters -- process it now. 
   */
  private void processWhenLetters() {
    if (word.toString().equalsIgnoreCase("today")) {
      strDate = TODAY_YMD;
      yyyy = TODAY_YMD.substring(0, 4);
      mm = TODAY_YMD.substring(5, 7);
      dd = TODAY_YMD.substring(8, 10);
    }
    else
    if (word.toString().equalsIgnoreCase("at")
        || word.toString().equalsIgnoreCase("from")) {
      lookingForTime = true;
    }
    else
    if (word.toString().equalsIgnoreCase("am")
        || word.toString().equalsIgnoreCase("pm")) {
      if (end.length() > 0) {
        end.append(" ");
        end.append(word);
      } 
      else 
      if (start.length() > 0) {
        start.append(" ");
        start.append(word);
      }
    } 
    else
    if (mm.length() > 0
        && dd.length() > 0) {
      // Don't overlay the first month, if a range was supplied
    } else {
      boolean found = false;
      int m = 0;
      while (m < MONTH_NAMES.length && (! found)) {
        if (MONTH_NAMES[m].toLowerCase().startsWith(word.toString().toLowerCase())) {
          found = true;
        } else {
          m++;
        }
      }
      if (found) {
        if (mm.length() > 0) {
          StringBuilder temp = new StringBuilder(mm);
          dd = temp.toString();
        }
        mm = zeroPad(m + 1, 2);
      }
    } // end if word might be a month
    resetWhenWord();
  }
  
  public static String zeroPad(int in, int desiredLength) {
    return zeroPad(String.valueOf(in), desiredLength);
  }
  
  public static String zeroPad(String in, int desiredLength) {
    StringBuilder padded = new StringBuilder(in);
    while (padded.length() < desiredLength) {
      padded.insert(0, " ");
    }
    while (padded.length() > desiredLength) {
      padded.deleteCharAt(0);
    }
    for (int i = 0; i < padded.length(); i++) {
      if (Character.isWhitespace(padded.charAt(i))) {
        padded.replace(i, i + 1, "0");
      }
    }
    return padded.toString();
  }
  
  /**
   End of a number string -- process it now. 
   */
  private void processWhenNumbers() {
    int number = 0;
    if (! colon) {
      try {
        number = Integer.parseInt(word.toString());
      } catch (NumberFormatException e) {
        // Number too large to be part of a date
      }
    }
    if (number > 1000) {
      yyyy = word.toString();
    }
    else
    if (lookingForTime) {
      // Let's use the number as part of the time of day
      if (start.length() == 0) {
        start.append(word);
      } else {
        end.append(word);
      }
    }
    else
    if (startOfDateRangeCompleted) {
      // Let's not overwrite the start of the range with an ending date
    } else {
      // Let's use the number as part of a date
      if (mm.length() == 0
          && number >= 1
          && number <= 12) {
        mm = word.toString();
      }
      else
      if (dd.length() == 0
          && number >= 1
          && number <= 31) {
        dd = word.toString();
      }
      else
      if (yyyy.length() == 0) {
        if (number > 1900) {
          // OK as-is
        }
        else
        if (number > 9) {
          word.insert(0, "20");
        }
        else {
          word.insert(0, "200");
        }
        yyyy = word.toString();
      }
      /* Following logic replaced on Aug 15, 2015
      if (number > 31
          || (mm.length() > 0 && dd.length() > 0) && word.length() > 3) {
        yyyy = word.toString();
      }
      else
      if (number > 12
          || mm.length() > 0) {
        dd = word.toString();
      } 
      else
      if (number > 0) {
        mm = word.toString();
      }
      */
    }
    resetWhenWord();
  }
  
  private void resetWhenWord() {
    word.setLength(0);
    numbers = false;
    letters = false;
    colon = false;
  }
  
  /**
   Subtract 1 from the year. 
   */
  public void decrementYear() {
    try {
      int year = Integer.parseInt(yyyy);
      year--;
      yyyy = String.format("%d", year);
    } catch (NumberFormatException e) {
      // Skip it      
    }
  }
  
  /**
   Does this value have any data stored in it? 
  
   @return True if data, false if empty. 
  */
  public boolean hasData() {
    return (strDate != null && strDate.length() > 0);
  }
  
  /** 
   Converts the value to a String.
  
   @return the value as a string. 
  */
  public String toString() {
    return strDate;
  }
  
  public int compareTo(Object obj2) {
    return (getYMD().compareTo(obj2.toString()));
  }
  
  public int compareTo(StringDate strDate2) {
    return (getYMD().compareTo(strDate2.getYMD()));
  }
  
  /**
     Compares this data value to another and indicates which is greater.
    
     @return Zero if the fields are equal, negative if this field is less than value2,
             or positive if this field is greater than value2.
    
     @param  value2 Another data value to be compared to this one.
   */
  public int compareTo(DataValue value2) {
    return (getYMD().compareTo(value2.toString()));
  }
  
  /**
   If this date is a definite date, with a year, month and day of month,
   and it's before today, then return true. 
  
   @return True if this is a definite date less than today, false otherwise. 
  */
  public boolean isInThePast() {
    String ymd = getYMD();
    return (ymd.length() > 9 && ymd.compareTo(TODAY_YMD) < 0);
  }
  
  /**
   Get a date to be used for sorting, with blank dates sorting after 
   non-blank dates. 
  
   @return A date string to be used for sorting. 
  */
  public String getYMDforSort() {
    if (hasData()) {
      return getYMD();
    } else {
      return "9999-12-31";
    }
  }
  
  /**
   Return the parsed date in a year-month-day format.
  
   @return Parsed date in y-m-d format. 
  */
  public String getYMD() {
    StringBuilder ymd = new StringBuilder();
    if (yyyy.length() > 0) {
      ymd.append(yyyy);
      if (mm.length() > 0) {
        ymd.append("-");
        ymd.append(zeroPad(mm, 2));
        if (dd.length() > 0) {
          ymd.append("-");
          ymd.append(zeroPad(dd, 2));
        }
      }
    }
    return ymd.toString();
  }
  
  /**
   Get a short but easily human-readable version of the date, consisting 
   of a three-letter abbreviation for the day of the week, a three-letter
   abbreviation for the month plus the day of the month (year is assumed to 
   be known or understood by the user).
  
   @return A short, easily human-readable version of the date. 
  */
  public String getShort() {

    Calendar cal = getCalendar();
    
    if (cal != null) {
      return SHORT_FORMAT.format(cal.getTime());
    } else {
      StringBuilder shortDate = new StringBuilder();
      try {
        int month = Integer.parseInt(mm);
        if (month >= 1 && month <= 12) {
          shortDate.append(MONTH_NAMES[month - 1].substring(0, 3));
          shortDate.append(" ");
          shortDate.append(dd);
          return shortDate.toString();
        } else {
          return "";
        }
      } catch (NumberFormatException e) {
        return "";
      }
    }
  }
  
  public String getReadable() {
    Calendar cal = getCalendar();
    if (cal == null) {
      return getYMD();
    } else {
      return READABLE_FORMAT.format(cal.getTime());
    }
  } 
  
  /**
   Format the date in a dd MMM yyyy format, using as many elements of the date
   as are available. 
  
   @return Date in dd MMM yyyy format. 
  */
  public String getCommon() {
    StringBuilder str = new StringBuilder();
    
    if (dd.length() > 1) {
      str.append(dd);
      str.append(" ");
    } 
    else
    if (dd.length() == 1) {
      str.append("0");
      str.append(dd);
      str.append(" ");
    }
    
    if (mm.length() > 0) {
      try {
        int mmIndex = Integer.parseInt(mm);
        if (mmIndex > 0 && mmIndex <= 12) {
          String mmName = MONTH_NAMES[mmIndex - 1];
          if (mmName.length() >= 3) {
            str.append(mmName.substring(0, 3));
            str.append(" ");
          }
        }
      } catch (NumberFormatException e) {
        // leave month out
      }
    }
    
    str.append(yyyy);
    return str.toString();
  }
  
  public Date getDate() {
    Calendar cal = getCalendar();
    if (cal == null) {
      return null;
    } else {
      return cal.getTime();
    }
  }
  
  /**
   Bump this date up by one day and return the result as a simple string. Does
   not modify the date stored within this object. 
  
   @return A string representation of the bumped date, or null, if we don't 
           actually have a completely good date to start with. 
  */
  public String increment() {
    String resultDate = null;
    Calendar workCal = getCalendar();
    if (workCal != null) {
      workCal.add(Calendar.DATE, 1);
      StringDate workStr = new StringDate();
      workStr.set(workCal.getTime());
      resultDate = workStr.toString();
    }
    return resultDate;
  }
  
  /**
   Return the date as a Calendar object, if we have a complete good date. 
  
   @return A Calendar object, or null if we don't have a complete good date. 
  */
  public Calendar getCalendar() {

    Calendar cal = Calendar.getInstance();
    int goodDateParts = 0;
    
    if (yyyy.length() > 0) {
      try {
        cal.set(Calendar.YEAR, Integer.parseInt(yyyy));
        goodDateParts++;
      } catch (NumberFormatException e) {
        // No joy
      }
    }
    
    if (mm.length() > 0) {
      try {
        cal.set(Calendar.MONTH, Integer.parseInt(mm) - 1);
        goodDateParts++;
      } catch (NumberFormatException e) {
        // Bummer
      }
    }
    
    if (dd.length() > 0) {
      try {
        cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dd));
        goodDateParts++;
      } catch (NumberFormatException e) {
        // No good
      }
    }
    
    if (goodDateParts >= 3) {
      return cal;
    } else {
      return null;
    }
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
