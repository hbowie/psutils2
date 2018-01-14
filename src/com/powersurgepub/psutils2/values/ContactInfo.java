/*
 * Copyright 2015 - 2015 Herb Bowie
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

  import com.powersurgepub.psutils2.basic.*;

/**
 A string containing name and contact info. Assumes United States info. 

 @author Herb Bowie
 */
public class ContactInfo 
    implements
        DataValue{
  
  public static final String NULL = "null";
  
  public static final String[] DERIVED_SUFFIX = {
    
  };
  
  private String value = null;
  private int    i     = 0;
  
  private               StringBuilder   word = new StringBuilder();
  private               String          lastWord = "";
  
  private               char            c = ' ';
  private               int             charType = 0;
  private static final  int               DELIMITER   = 0;
  private static final  int               PUNCTUATION = 1;
  private static final  int               DIGIT       = 2;
  private static final  int               LETTER      = 3;
  private static final  int               END         = 9;
  
  private               StringBuilder   field = new StringBuilder();
  private               int             numberOfDigits = 0;
  private               int             numberOfLetters = 0;
  private               int             numberOfAts = 0;
  private               char            lastDelim = ' ';
  private               int             fieldType = 0;
  private static final  int               NAME    = 0;
  private static final  int               ADDRESS = 1;
  private static final  int               CITY    = 2;
  private static final  int               STATE   = 3;
  private static final  int               ZIPCODE = 4;
  private static final  int               PHONE   = 5;
  private static final  int               EMAIL   = 6;
  
  private               String            name = "";
  private               String            address = "";
  private               String            city = "";
  private               String            state = "";
  private               String            zipCode = "";
  private               String            phone = "";
  private               String            email = "";
  
  public ContactInfo() {
    
  }
  
  public ContactInfo (String value) {
    if (! value.equalsIgnoreCase(NULL)) {
      this.value = value;
      parse();
    }
  }
  
  /**
   Set the value from a String. 
  
   @param value The value as a string. 
  */
  public void set(String value) {
    if (! value.equalsIgnoreCase(NULL)) {
      this.value = value;
      name = "";
      address = "";
      city = "";
      state = "";
      zipCode = "";
      phone = "";
      email = "";
      parse();
    }
  }
  
  /**
   Break the contact info line down into its constituent pieces. 
  */
  private void parse() {
    
    // System.out.println(" ");
    // System.out.println("ContactInfo.parse: " + value);
    
    i = 0;
    fieldType = NAME;
    field = new StringBuilder();
    lastDelim = ' ';
    getNextChar();
    while (i < value.length()) {
      // Parse next field
      
      // Skip past any leading delimiters, such as spaces
      while (charType == DELIMITER) {
        getNextChar();
      }
      
      // Build the next word
      word = new StringBuilder();
      numberOfDigits = 0;
      numberOfLetters = 0;
      numberOfAts = 0;
      while (charType != DELIMITER && charType != END) {
        word.append(c);
        if (charType == DIGIT) {
          numberOfDigits++;
        }
        else
        if (charType == LETTER) {
          numberOfLetters++;
        }
        else
        if (c == '@') {
          numberOfAts++;
        }
        getNextChar();
      } // end while building next word
      
      // Determine what to do with the word
      
      // See if we have a legitimate usState code
      USState usState = USState.UNKNOWN;
      if (numberOfLetters == 2
          && word.length() == 2) {
        usState = USState.valueOfAbbreviation(word.toString());
      }
      
      // Check for the word 'at' after the name and before the address
      if (fieldType == NAME 
          && field.length() > 0
          && word.toString().equalsIgnoreCase("at")) {
        processField();
        fieldType = ADDRESS;
      }
      else
        
      // Check for start of a street address
      if (numberOfDigits > 0
          && numberOfDigits == word.length()
          && fieldType == NAME
          && (! lastWord.equalsIgnoreCase("room"))) {
        processField();
        field.append(word);
        fieldType = ADDRESS;
      }
      else   
        
      // Check for a zip code
      if ((numberOfDigits == 5 
            && word.length() == 5)
          || (numberOfDigits == 9 
            && word.length() == 10 
            && numberOfLetters == 0)) {
        processField();
        field.append(word);
        fieldType = ZIPCODE;
        processField();
      }
      else
        
      // Check for a state code
      if (usState != USState.UNKNOWN
          && fieldType >= CITY) {
        processField();
        field.append(word);
        fieldType = STATE;
        processField();
      }
      else
        
      // Check for a state code
      if (usState != USState.UNKNOWN
          && fieldType == NAME
          && field.length() > 2
          && state.length() == 0
          && lastDelim == ',') {
        processField();
        field.append(word);
        fieldType = STATE;
        processField();
      }
      else
        
      // Check for a phone number
      if (numberOfDigits == 10
          && word.length() > 10
          && word.length() <= 13) {
        processField();
        field.append(word);
        fieldType = PHONE;
        processField();
      }
      else
        
      // Check for an e-mail address
      if (numberOfAts == 1) {
        processField();
        field.append(word);
        fieldType = EMAIL;
        processField();
      }
      else
        
      // Check for Suite or Apt
      if (fieldType == CITY
          && address.length() > 0
          && field.length() == 0
          && (word.toString().equalsIgnoreCase("suite")
           || word.toString().equalsIgnoreCase("apt")
           || word.toString().equals("#"))) {
        field = new StringBuilder(address);
        lastDelim = ',';
        fieldType = ADDRESS;
        appendWordToField();
      }
      else
        
      // Check for delimiter
      if (c == ','
          && fieldType == ADDRESS) {
        appendWordToField();
        processField();
      } 
      else
        
      // Add new word to existing field
      {
        appendWordToField();
      }
    } // end while more characters to parse
    processField();
    // System.out.println("  name:    " + name);
    // System.out.println("  address: " + address);
    // System.out.println("  city:    " + city);
    // System.out.println("  state:   " + state);
    // System.out.println("  zip:     " + zipCode);
    // System.out.println("  email:   " + email);
    // System.out.println("  phone:   " + phone);
    // System.out.println("  map url: " + getMapURL());
  } // end method parse
  
  /**
   Get the next character and determine its type.
  */
  private void getNextChar() {
    if (i >= 0 && i < value.length()) {
      c = value.charAt(i);
      if (Character.isDigit(c)) {
        charType = DIGIT;
      }
      else
      if (Character.isLetter(c)) {
        charType = LETTER;
      }
      else
      if (Character.isWhitespace(c)) {
        charType = DELIMITER;
      }
      else
      if (c == ',' || c == ';' 
          || c == '[' || c == ']'
          || c == '<' || c == '>') {
        charType = DELIMITER;
      } else {
        charType = PUNCTUATION;
      }
    } else {
      c = ' ';
      charType = END;
    }
    i++;
  }
  
  private void appendWordToField() {
    if (word.length() > 0) {
      if (field.length() > 0) {
        field.append(lastDelim);
        if (lastDelim != ' ') {
          field.append(' ');
        }
      }
      field.append(word);
      lastWord = word.toString();
      lastDelim = c;
    }
  }
  
  private void processField() {
    if (field.length() > 0) {
      switch (fieldType) {
        case NAME:
          name = field.toString();
          break;
        case ADDRESS:
          address = field.toString();
          break;
        case CITY:
          city = field.toString();
          break;
        case STATE:
          state = field.toString();
          break;
        case ZIPCODE:
          zipCode = field.toString();
          break;
        case PHONE:
          phone = field.toString();
          break;
        case EMAIL:
          email = field.toString();
          break;
      }
      fieldType++;
    } // end if we have a field to process
    field = new StringBuilder();
    lastDelim = ' ';
  }
  
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
      return value;
    }
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
  
  /**
   Indicate the number of variant forms of the field that are available. 
  
   @return Number of variant forms available. 
  */
  public int getNumberOfVariants() {
    return 8;
  }
  
  /**
   Return the name of one of the variant forms of the field. This can be 
   appended to the primary field name as a suffix. 
  
   @param v The desired variant number, starting with 0.
  
   @return The requested variant name. 
  */
  public String getVariantName (int v) {
    switch (v) {
      case 0:
        return "Name";
      case 1:
        return "Address";
      case 2:
        return "City";
      case 3:
        return "State";
      case 4:
        return "Zip";
      case 5:
        return "Phone";
      case 6:
        return "Email";
      case 7:
        return "MapURL";
      default:
        return "";
    }
  }
  
  /**
   Return the value of one of the variant forms of the field.
  
   @param v The desired variant number, starting with 0. 
  
   @return The requested variant field value.
  */
  public String getVariantValue (int v) {
    switch (v) {
      case 0:
        return name;
      case 1:
        return address;
      case 2:
        return city;
      case 3:
        return state;
      case 4:
        return zipCode;
      case 5:
        return phone;
      case 6:
        return email;
      case 7:
        return getMapURL();
      default:
        return "";
    }
  }
  
  public String getName() {
    return name;
  }
  
  public String getAddress() {
    return address;
  }
  
  public String getCity() {
    return city;
  }
  
  public String getState() {
    return state;
  }
  
  public String getZipCode() {
    return zipCode;
  }
  
  public String getPhone() {
    return phone;
  }
  
  public String getEmail() {
    return email;
  }
  
  public String getMapURL() {
    StringBuilder url = new StringBuilder();
    if (address != null && address.length() > 0) {
      url.append("https://www.google.com/maps/place/");
      mapsAppend(url, address);
      mapsAppend(url, city);
      mapsAppend(url, state);
      mapsAppend(url, zipCode);
    }
    return url.toString();
  }
  
  private void mapsAppend(StringBuilder builder, String str) {
    if (str != null && str.length() > 0) {
      if (builder.charAt(builder.length() - 1) != '/'
          && builder.charAt(builder.length() - 1) != '+') {
        builder.append('+');
      }
      for (int j = 0; j < str.length(); j++) {
        char s = str.charAt(j);
        if (Character.isDigit(s)
            || Character.isLetter(s)) {
          builder.append(s);
        }
        else
        if (builder.charAt(builder.length() - 1) != '+') {
          builder.append('+');
        }
      } // end for each char
    } // end if we have something to append
  } // end method mapsAppend
  
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
