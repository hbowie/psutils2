/*
 * Copyright 2014 - 2017 Herb Bowie
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


/**
 A standard interface for a user interface widget for displaying and possibly
 entering String data. 

 @author Herb Bowie
 */
public interface DataWidget {
  
  /**
   Get the data entered by the user, represented as a String. 
  
   @return A string representing data entered by the user. 
  */
  public String getText();
  
  /**
   Display a string using this widget.
  
   @param t The string to be displayed. 
  */
  public void setText(String t);
  
}
