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

package com.powersurgepub.psutils2.records;

/**
   A rule for formatting one or more strings into a standard format. 
   This class is meant to be extended by others. <p>
  
   @author Herb Bowie (<a href="mailto:herb@powersurgepub.com">
           herb@powersurgepub.com</a>)<br>
           of PowerSurge Publishing (<A href="http://www.powersurgepub.com/software/">
           www.powersurgepub.com/software</a>)

 */
public class DataFormatRule {

  /** ID for this DataFormatRule class. */
  public static final String DATA_FORMAT_RULE_CLASS_NAME = "DataFormatRule";
  
  /**
     Constructor doesn't do anything.
   */
  public DataFormatRule () {
    super ();
  }
  
  /**
     Return the string as input.
   */
  public String transform (String inData) {
    return inData;
  }
  
  /**
     Identify the class.
    
     @return String identifying the class name.
   */
  public String toString () {
    return DATA_FORMAT_RULE_CLASS_NAME;
  }
  
  /**
     Constructs and returns any one of a number of DataFormatRule objects,
     depending on the class name passed as a String value.
    
     @return New DataFormatRule object.
    
     @param className Name of the desired class.
    
     @throws IllegalArgumentException If the className is unknown.
   */
  public static DataFormatRule constructRule (String className) 
      throws IllegalArgumentException {
    if (className.equals (DataFormatRule.DATA_FORMAT_RULE_CLASS_NAME)
        || className.equals ("")) {
      return new DataFormatRule ();
    } else
    if (className.equals (AllCapsRule.ALL_CAPS_RULE_CLASS_NAME)) {
      return new AllCapsRule ();
    } else
    if (className.equals (LowerCaseRule.LOWER_CASE_RULE_CLASS_NAME)) {
      return new LowerCaseRule ();
    } else
    if (className.equals (InitialCapsRule.INITIAL_CAPS_RULE_CLASS_NAME)) {
      return new InitialCapsRule ();
    } else
    if (className.equals (CountryRule.COUNTRY_RULE_CLASS_NAME)) {
      return new CountryRule ();
    } else
    if (className.equals (USPhoneRule.US_PHONE_RULE_CLASS_NAME)) {
      return new USPhoneRule ();
    } else
    if (className.equals (USMobileRule.US_MOBILE_RULE_CLASS_NAME)) {
      return new USMobileRule (new USPhoneRule());
    } else
    if (className.equals (DateRule.DATE_RULE_CLASS_NAME)) {
      return new DateRule ();
    } else 
    if (className.equals (HyperlinkRule.HYPERLINK_RULE_CLASS_NAME)) {
      return new HyperlinkRule(); 
    } else {
      throw new IllegalArgumentException (className);
    }
  }

} // end of class DataFormatRule