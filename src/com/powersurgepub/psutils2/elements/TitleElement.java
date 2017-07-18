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

package com.powersurgepub.psutils2.elements;

/**
 A title data element.
 
 @author Herb Bowie.
 */
public class TitleElement 
    implements DataElement {

  public final static String  SIMPLE_NAME   = "title";
  public final static String  DISPLAY_NAME  = "Title";
  public final static String  BRIEF_NAME    = "Title";
  public final static int     COLUMN_WIDTH  = 60;

  private             String  title = "";

  public TitleElement() {
    
  }

  public TitleElement(String value) {
    title = value;
  }

  /**
   Sets the value of the data element from the element's preferred class form.

   @param value The value, as the element's preferred class.
   */
  public void setValue(Object value) {
    title = value.toString();
  }

  /**
   Sets the value of the data element from a string representation.

   @param value A string representation of the desired value for the element.
   */
  public void setValue(String value) {
    title = value;
  }

  /**
   Obtain the value of the element in the form of the element's preferred class.

   @return The value of the element in the form of the
           element's preferred class.
   */
  public Object getValue() {
    return title;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  /**
   Get the class definition for the preferred form of the element.

   @return The class definition for the preferred form of the element.
   */
  public Class getElementClass() {
    return title.getClass();
  }

  /**
   Obtain the simplest form of the data element's name. This is typically a
   name in all lower-case, without any spaces or punctuation.

   @return The simplest form of the data element's name. This is typically a
           name in all lower-case, without any spaces or punctuation.
   */
  public String getSimpleName() {
    return SIMPLE_NAME;
  }


  /**
   Obtain the form of the name intended for display to humans. This would
   typically include spaces between words and capitalization of the first
   letter of each word.

   @return The form of the name intended for display to humans. This would
           typically include spaces between words and capitalization of the
           first letter of each word.
   */
  public String getDisplayName() {
    return DISPLAY_NAME;
  }


  /**
   Obtain a human-readable name, but intended for display in spaces where
   minimal space is available.

   @return A human-readable name, but intended for display in spaces where
           minimal space is available.
   */
  public String getBriefName() {
    return BRIEF_NAME;
  }


  /**
   Get the width of the column.

   @return The width of the column in a JTable.
   */
  public int getColumnWidth() {
    return COLUMN_WIDTH;
  }

  /**
   Obtain the value of the data element as a string.

   @return The value of the data element as a string.
   */
  @Override
  public String toString() {
    return title;
  }



}
