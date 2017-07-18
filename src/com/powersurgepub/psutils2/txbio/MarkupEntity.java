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

package com.powersurgepub.psutils2.txbio;

/**
 This class is used to represent a single HTML entity.
 */
public class MarkupEntity {

  private String name;
  private int    number;
  private String replacement;

  public MarkupEntity (String name, int number, String replacement) {
    this.name = name;
    this.number = number;
    this.replacement = replacement;
  }

  public String getName () {
    return name;
  }

  public boolean equalsName (String name2) {
    return (name.equalsIgnoreCase (name2));
  }

  public int getNumber () {
    return number;
  }

  public boolean equalsNumber (int number2) {
    return (number == number2);
  }

  public String getReplacement () {
    return replacement;
  }
}
