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

/**
 An interface used by TextSelector to indicate that the user's text
 selection has been completed.

 com.powersurgepub.ui.TextSelector can be used to present the user with a
 popup list from which he or she can select a value.

 com.powersurgepub.ui.PopUpList provides the list that is displayed.

 com.powersurgepub.ui.TextHandler defines an interface for the class that is to
 be notified when text selection is complete.

 com.powersurgepub.psutils.ValueList is the class that provides the list
 from which the user will choose a value.
 
 @author Herb Bowie
 */
public interface TextHandler {
  
  public void textSelectionUpdated(String fieldName);

}
