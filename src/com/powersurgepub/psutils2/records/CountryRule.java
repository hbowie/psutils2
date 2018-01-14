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

  import com.powersurgepub.psutils2.strings.*;
  
/**
   Formats a string by ensuring that the first letter of each word
   is capitalized, but other letters are lower-case, except for common
   country abbreviations, such as "USA". Intended for formatting names
   of countries. <p>
   
   This code is copyright (c) 1999-2000 by Herb Bowie of PowerSurge Publishing. 
   All rights reserved. <p>
   
   Version History: <ul><li>
      </ul>
  
   @author Herb Bowie (<a href="mailto:herb@powersurgepub.com">
           herb@powersurgepub.com</a>)<br>
           of PowerSurge Publishing (<A href="http://www.powersurgepub.com/software/">
           www.powersurgepub.com/software</a>)
  
   @version 00/05/22 - Cloned from AllCapsRule.
 */
public class CountryRule
  extends DataFormatRule {
  
  /** ID for this DataFormatRule descendent. */
  public static final String COUNTRY_RULE_CLASS_NAME = "CountryRule";
  
  /**
     Constructor doesn't do anything.
   */
  public CountryRule () {
    super ();
  }
  
  /**
     Transforms a string by converting initial letters of each word to upper-case,
     and other letters to lower-case, except for common Country abbreviations, such
     as "USA", which are converted to all caps.
    
     @return String With proper capitalization.
    
     @param inData String to be transformed. Note that this is not 
                   stored as part of this object.
   */
  public String transform (String inData) {
    if (inData.equalsIgnoreCase ("usa") || inData.equalsIgnoreCase ("us")) {
      return "USA";
    } else 
    if (inData.equalsIgnoreCase ("uk")) {
      return "UK";
    } else {
      return StringUtils.initialCaps (inData);
    }
  }
    
  public String toString () {
    return COUNTRY_RULE_CLASS_NAME;
  }
  
  /** 
     Test the class with standard test cases.
   */
  public static void test () {
    
    CountryRule rule = new CountryRule();
    System.out.println ("Testing " + rule.toString());
    testRule (rule, "Usa");
    testRule (rule, "uSa");
    testRule (rule, "us");
    testRule (rule, "US");
    testRule (rule, "usa");
    testRule (rule, "england");
    testRule (rule, "FRANCE");
    testRule (rule, "uk");
  }
  
  /**
     Convert one country and display input and output.
    
     @param inRule An instance of CountryRule.
    
     @param inString A country to be converted.
   */
  public static void testRule (CountryRule inRule, String inString) {
  
    System.out.println 
      (inString + " becomes " + (String)inRule.transform(inString));
  }

}