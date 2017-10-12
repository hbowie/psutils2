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

package com.powersurgepub.psutils2.values;

/**
 A data value interpreted as a sequence number. Such a value may contain letters
 and digits and one or more periods or hyphens. 

 @author Herb Bowie
 */
public class DataValueSeq

    implements
        DataValue {
  
  public static final String[] DERIVED_SUFFIX = {
    
  };
  
  public static final String DIGITS = "0123456789";
  public static final String LETTERS = " abcdefghijklmnopqrstuvwxyz";
  
  private StringBuilder   value = new StringBuilder();
  private int             positionOfFirstDecimal = -1;
  private int             positionOfLastDecimal = -1;
  private char            padChar = ' '; 
  private int             positionsToLeftOfDecimal = 0;
  private int             positionsToRightOfDecimal = 0;
  private boolean         digits = false;
  private boolean         letters = false;
  private boolean         uppercase = true;
  private char            punctuation = '.';
  
  /**
   Constructor with no passed value. 
  */
  public DataValueSeq() {
    
  }
  
  public DataValueSeq(DataValueSeq value) {
    set(value);
  }
  
  /**
   Constructor with a passed value. 
  
   @param value A sequence value. 
  */
  public DataValueSeq(String value) {
    set(value);
  }
  
  public void set(DataValueSeq value) {
    set(value.toString());
  }
  
  /**
   Set the value from a String. 
  
   @param value The value as a string. 
  */
  public void set(String value) {
    
    positionOfFirstDecimal = -1;
    positionOfLastDecimal = -1;
    padChar = ' ';
    punctuation = ' ';
    positionsToLeftOfDecimal = 0;
    positionsToRightOfDecimal = 0;
    digits = false;
    letters = false;
    uppercase = true;
    this.value = new StringBuilder();
    
    int i = 0;
    while (i < value.length()) {
      char c = value.charAt(i);
      if (c != '.' && c != '-' && positionOfFirstDecimal < 0) {
        positionsToLeftOfDecimal++;
      }
      if (c == '0' && this.value.length() == 0) {
        padChar = c;
      }
      else
      if (Character.isWhitespace(c)) {
        // drop spaces and other white space
      }
      else
      if (Character.isAlphabetic(c)) {
        this.value.append(c);
        letters = true;
        if (Character.isLowerCase(c)) {
          uppercase = false;
        }
      }
      else
      if (Character.isDigit(c)) {
        this.value.append(c);
        digits = true;
        if (positionOfLastDecimal >= 0) {
          positionsToRightOfDecimal++;
        }
      }
      else
      if (c == '.' || c == '-') {
        if (positionOfFirstDecimal < 0) {
          if (this.value.length() == 0) {
            this.value.append('0');
          }
          positionOfFirstDecimal = this.value.length();
          punctuation = c;
        }
        positionOfLastDecimal = this.value.length();
        positionsToRightOfDecimal = 0;
        this.value.append(c);
      }
      i++;
    }
    
  } // end of set method
  
  /**
   Increment the sequence value (whether numeric or alphabetic) by one.
  
   @param onLeft Are we incrementing the integer (to the left of the decimal, 
                 if any?). 
  */
  public void increment(boolean onLeft) {

    int i = value.length() - 1;
    if (onLeft && positionOfFirstDecimal >= 0) {
      i = positionOfFirstDecimal - 1;
    }
    if (hasData()) {
      boolean carryon = true;
      char c = ' ';
      while (carryon) {

        if (i < 0) {
          if (digits) {
            c = '0';
          } else {
            c = ' ';
          }
          value.insert(0, c);
          i = 0;
          positionsToLeftOfDecimal++;
          if (positionOfFirstDecimal >= 0) {
            positionOfFirstDecimal++;
          }
          if (positionOfLastDecimal >= 0) {
            positionOfLastDecimal++;
          }
        } else {
          c = value.charAt(i);
        }
        int j = 0;
        boolean found = false;
        if (Character.isDigit(c)) {
          while ((! found) && j < DIGITS.length()) {
            if (c == DIGITS.charAt(j)) {
              found = true;
              j++;
              if (j < DIGITS.length()) {
                c = DIGITS.charAt(j);
                value.setCharAt(i, c);
                carryon = false;
              } else {
                j = 0;
                c = DIGITS.charAt(0);
                value.setCharAt(i, c);
              } // end if we're carrying 
            } else {
              j++;
            } // end of examining this possible match
          } // end while looking for a matching character
        }
        else
        if (Character.isAlphabetic(c)) {
          if (Character.isUpperCase(c)) {
            c = Character.toLowerCase(c);
          }
          while ((! found) && j < LETTERS.length()) {
            if (c == LETTERS.charAt(j)) {
              found = true;
              j++;
              if (j < LETTERS.length()) {
                c = LETTERS.charAt(j);
                if (uppercase) {
                  c = Character.toUpperCase(c);
                }
                value.setCharAt(i, c);
                carryon = false;
              } else {
                j = 1;
                c = LETTERS.charAt(1);
                if (uppercase) {
                  c = Character.toUpperCase(c);
                }
                value.setCharAt(i, c);
              } // end if we're carrying 
            } else {
              j++;
            } // end of examining this possible match
          } // end while looking for a matching character
        } // end of alpha character
        i--;
      } // End of incrementing and carrying
    }
    
  } // end of increment method
  
  public int length() {
    if (hasData()) {
      return value.length();
    } else {
      return 0;
    }
  }
  
  /**
   Does this value have any data stored in it? 
  
   @return True if data, false if empty. 
  */
  public boolean hasData() {
    return (value != null && value.length() > 0);
  }
  
  /** 
   Converts the value to a String.
  
   @return the value as a string. 
  */
  public String toString() {
    if (value == null) {
      return "";
    } else {
      return value.toString();
    }
  }
  
  public String getLeft() {
    if (positionOfFirstDecimal < 0) {
      positionOfFirstDecimal = value.length();
    }
    if (positionOfFirstDecimal == 0) {
      return "";
    } else {
      return value.substring(0, positionOfFirstDecimal);
    }
  }
  
  public int getPositionsToLeftOfDecimal() {
    return positionsToLeftOfDecimal;
  }
  
  public int getPositionsToRightOfDecimal() {
    return positionsToRightOfDecimal;
  }
  
  /**
   Returns a padded string, for the purposes of comparison. 
  
   @return Padded on the left. 
  */
  public String toPaddedString() {
    char padChar = ' ';
    if (digits) {
      padChar = '0';
    }
    return toPaddedString(padChar, 8, padChar, 4);
  }
  
  /**
   Returns a padded string, for purposes of comparison.
  
   @param leftChar  Character to use to pad out to the left. 
   @param left      Number of positions to pad to the left 
                    of the decimal point.
   @param rightChar Character to use to pad out to the right. 
   @param right     Number of positions to pad to the right 
                    of the decimal point.
  
   @return          Sequence value, with padding. 
  */
  public String toPaddedString(char leftChar, int left, char rightChar, int right) {
    StringBuilder padded = new StringBuilder(value);
    int positionsToLeft = value.length();
    if (positionOfFirstDecimal >= 0) {
      positionsToLeft = positionOfFirstDecimal;
    }
    while (positionsToLeft < left) {
      padded.insert(0, leftChar);
      positionsToLeft++;
    }
    int positionsToRight = 0;
    if (positionOfLastDecimal >= 0) {
      positionsToRight = value.length() - positionOfLastDecimal - 1;
    }
    if (right > 0) {
      if (positionOfLastDecimal < 0) {
        padded.append('.');
      }
      while (positionsToRight < right) {
        padded.append(rightChar);
        positionsToRight++;
      }
    }
    return padded.toString();
  }
  
  /**
     Compares this data value to another and indicates which is greater.
    
     @return Zero if the fields are equal, negative if this field is less than value2,
             or positive if this field is greater than value2.
    
     @param  value2 Another data value to be compared to this one.
   */
  public int compareTo(DataValue value2) {
    
    if (value2 instanceof DataValueSeq) {
      DataValueSeq seq2 = (DataValueSeq)value2;
      return toPaddedString().compareTo(seq2.toPaddedString());
    } else {
      return toString().compareTo(value2.toString());
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
