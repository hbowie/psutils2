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

package com.powersurgepub.psutils2.markup;

/**
 * MarkupUtils.java
 *
 * Created on April 18, 2007, 7:16 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 *
 * @author hbowie
 */
public class MarkupUtils {
  
  public static final String ANCHOR   = "a";
  public static final String ITALICS  = "i";
  public static final String BOLD     = "b";
  public static final String CITE     = "cite";
  public static final String EMPHASIS = "em";
  public static final String BREAK    = "br";
  public static final String TEXT_FRAGMENT = "t";
  public static final String PARAGRAPH = "p";
  
  /** Creates a new instance of MarkupUtils */
  private MarkupUtils() {
  }
  
  public static boolean tagBreakBefore (String name) {
    boolean breakBefore = true;
    // Determine line breaks around tag
    if (name.equals (ANCHOR)
        || name.equals (ITALICS)
        || name.equals (BOLD)
        || name.equals (CITE)
        || name.equals (EMPHASIS)
        || name.equals (BREAK)
        || name.equals (TEXT_FRAGMENT)) {
      breakBefore = false;
    } 
    return breakBefore;
  }
  
  public static boolean tagBreakAfter (String name) {
    boolean breakAfter = true;
    // Determine line breaks around tag
    if (name.equals (ANCHOR)
        || name.equals (ITALICS)
        || name.equals (BOLD)
        || name.equals (CITE)
        || name.equals (EMPHASIS)
        || name.equals (TEXT_FRAGMENT)) {
      breakAfter = false;
    } 
    return breakAfter;
  }
  
  public static boolean tagEmpty (String name) {
    boolean emptyTag = false;
    if (name.equals (BREAK)
        || name.equals ("img")
        || name.equals ("hr")) {
      emptyTag = true;
    } 
    return emptyTag;
  }
  
  public static boolean tagIsTextFragment (String name) {
    return (name.equals (TEXT_FRAGMENT));
  }
  
  public static boolean tagIsAnchor (String name) {
    return (name.equals (ANCHOR));
  }
  
  public static boolean tagIsHeading (String name) {
    return (name.length() == 2 
        && name.charAt(0) == 'h'
        && name.charAt(1) >= '1'
        && name.charAt(1) <= '6');
  }

  public static boolean isBlockTag (String name) {
    return (name.equals(PARAGRAPH)
        || tagIsHeading(name));
  }
  
}
