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
  A standard interface for a data element. A data element specifies both a
  value and an element definition. The definition consists of a simple name,
  a display name, a brief name, a column width, and a class indicating the
  preferred representation for the element. <p>

  Note that a data element is designed to be used as a potential cell in a
  JTable.

  @author Herb Bowie
 */
public interface DataElement {

  /**
   Sets the value of the data element from the element's preferred class form.

   @param value The value, as the element's preferred class.
   */
  public void setValue(Object value);

  /**
   Sets the value of the data element from a string representation.

   @param value A string representation of the desired value for the element.
   */
  public void setValue(String value);

  /**
   Obtain the value of the element in the form of the element's preferred class.

   @return The value of the element in the form of the
           element's preferred class.
   */
  public Object getValue();

  /**
   Get the class definition for the preferred form of the element.

   @return The class definition for the preferred form of the element.
   */
  public Class getElementClass();

  /**
   Obtain the simplest form of the data element's name. This is typically a
   name in all lower-case, without any spaces or punctuation.

   @return The simplest form of the data element's name. This is typically a
           name in all lower-case, without any spaces or punctuation.
   */
  public String getSimpleName();


  /**
   Obtain the form of the name intended for display to humans. This would
   typically include spaces between words and capitalization of the first
   letter of each word.

   @return The form of the name intended for display to humans. This would
           typically include spaces between words and capitalization of the
           first letter of each word.
   */
  public String getDisplayName();


  /**
   Obtain a human-readable name, but intended for display in spaces where
   minimal space is available.

   @return A human-readable name, but intended for display in spaces where
           minimal space is available.
   */
  public String getBriefName();


  /**
   Get the width of the column.

   @return The width of the column in a JTable.
   */
  public int getColumnWidth();

  /**
   Obtain the value of the data element as a string.

   @return The value of the data element as a string.
   */
  @Override
  public String toString();

}
