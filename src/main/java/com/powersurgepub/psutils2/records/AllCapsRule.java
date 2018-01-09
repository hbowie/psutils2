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
   Formats a string into all capital letters. Good for formatting
   state codes, for example. <p>
   
   This code is copyright (c) 1999-2000 by Herb Bowie of PowerSurge Publishing. 
   All rights reserved. <p>
   
   Version History: <ul><li>
      </ul>
  
   @author Herb Bowie (<a href="mailto:herb@powersurgepub.com">
           herb@powersurgepub.com</a>)<br>
           of PowerSurge Publishing (<A href="http://www.powersurgepub.com/software/">
           www.powersurgepub.com/software</a>)
  
   @version 00/04/21 - Modified to be consistent with "The Elements of Java Style".
 */

public class AllCapsRule
  extends DataFormatRule {
  
  /** ID for this DataFormatRule descendent. */
  public static final String ALL_CAPS_RULE_CLASS_NAME = "AllCapsRule";
  
  /**
     Constructor doesn't do anything.
   */
  public AllCapsRule () {
    super ();
  }
  
  /**
     Transforms a string by converting all the letters to upper case.
    
     @return All upper-case letters.
    
     @param inData String to be transformed. Note that this is not 
                   stored as part of the AllCapsRule object.
   */
  public String transform (String inData) {
    return inData.toUpperCase();
  }
  
  /**
     Identify the class.
    
     @return String identifying the class name.
   */
  public String toString () {
    return ALL_CAPS_RULE_CLASS_NAME;
  }

}