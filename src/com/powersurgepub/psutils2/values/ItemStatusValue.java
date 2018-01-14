/*
 * Copyright 1999 - 2015 Herb Bowie
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
 Information about a particular Item State value. 

 @author Herb Bowie
 */
public class ItemStatusValue {
  
  /** Is this value available for use? */
  private boolean available = true;
  
  /** The standard label for this particular value. */
  private String  label = "";
  
  /** The first two characters of the label, in lower-case. */
  private String  firstTwo = "";
  
  public ItemStatusValue() {
    
  }
  
  public ItemStatusValue(String label) {
    set(label);
  }
  
  public ItemStatusValue(String label, boolean available) {
    set(label);
    this.available = available;
  }
  
  public void setAvailable(boolean available) {
    this.available = available;
  }
  
  public boolean isAvailable() {
    return available;
  }
  
  public void setLabel(String label) {
    set(label);
  }
  
  public void set(String label) {
    if (label.length() > 1) {
      this.label = label;
      firstTwo = label.substring(0, 2).toLowerCase();
    }
  }
  
  public String getLabel() {
    return label;
  }
  
  public String getNumberWithLabel(int i) {
    return (String.valueOf(i) + " - " + label);
  }
  
  public String toString() {
    return label;
  }
  
  public String getFirstTwo() {
    return firstTwo;
  }

}
