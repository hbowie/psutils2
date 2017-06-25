/*
 * Copyright 1999 - 2017 Herb Bowie
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

package com.powersurgepub.psutils2.widgets;

  import com.powersurgepub.psutils2.values.*;

/**
 * An interface for using the Date Widget. 

 * @author Herb Bowie
 */
public interface DateWidgetOwner {
  
  /**
   To be called whenever the date is modified by DateWidget.
   */
  public void dateModified (String date);
  
  /**
   Does this date have an associated rule for recurrence?
   */
  public boolean canRecur();
  
  /**
   Provide a text string describing the recurrence rule, that can
   be used as a tool tip.
   */
  public String getRecurrenceRule();
  
  /**
   Apply the recurrence rule to the date.
  
   @param date Starting date.
  
   @return New date. 
   */
  public String recur (StringDate date);
  
  /**
   Apply the recurrence rule to the date.
  
   @param date Starting date.
  
   @return New date. 
   */
  public String recur (String date);
  
}
