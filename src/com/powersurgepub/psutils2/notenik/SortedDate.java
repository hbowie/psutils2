/*
 * Copyright 2010 - 2017 Herb Bowie
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

package com.powersurgepub.psutils2.notenik;

  import com.powersurgepub.psutils2.values.*;

/**
 A wrapper around a StringDate, intended to be used in a TableView Column. 

 @author Herb Bowie
 */
public class SortedDate 
  implements
    Comparable {
  
  private StringDate strDate;
  
  public SortedDate(StringDate strDate) {
    this.strDate = strDate;
  }
  
  public int compareTo(Object obj2) {
    if (obj2 instanceof SortedDate) {
      SortedDate date2 = (SortedDate)obj2;
      return compareTo(date2);
    } else {
      return (getYMDforSort().compareTo(obj2.toString()));
    }
  }
  
  public int compareTo(SortedDate date2) {
    return (getYMDforSort().compareTo(date2.getYMDforSort()));
  }
  
  /**
   Get a date to be used for sorting, with blank dates sorting after 
   non-blank dates. 
  
   @return A date string to be used for sorting. 
  */
  public String getYMDforSort() {
    if (strDate != null && strDate.hasData()) {
      return strDate.getYMD();
    } else {
      return "9999-12-31";
    }
  }
  
  public String toString() {
    if (strDate != null && strDate.hasData()) {
      return strDate.getCommon();
    } else {
      return "";
    }
  }

}
