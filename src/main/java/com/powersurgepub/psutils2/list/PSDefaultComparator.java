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

package com.powersurgepub.psutils2.list;

  import java.util.*;

/**
 Everything is always equal. 

 @author Herb Bowie
 */
public class PSDefaultComparator     implements
      Comparator {
  
  public PSDefaultComparator() {
    
  }
  
  /**
   Compare the two events and return the result. This default comparator always
   determines any two objects to be equal. 
  
   @param obj1 The first item to be compared. 
   @param obj2 The second item to be compared. 
  
   @return -1 if the first item is lower than the second, 
           +1 of the first item is higher than the second, or
           zero if the two events have the same keys. 
  */
  public int compare (Object obj1, Object obj2) {
    
    int result = 0;
    return result;
  }
}
