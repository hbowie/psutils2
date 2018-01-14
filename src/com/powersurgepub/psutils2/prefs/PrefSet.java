/*
 * Copyright 2017 Herb Bowie
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

package com.powersurgepub.psutils2.prefs;

  import com.powersurgepub.psutils2.files.*;

  import javafx.scene.layout.*;

/**
 A set of related user preferences. 

 @author Herb Bowie
 */
public interface PrefSet {
  
  /**
   Get the title for this set of preferences. 
  
   @return The title for this set of preferences. 
  */
  public String getTitle();
  
  /**
   Get a JavaFX Pane presenting all the preferences in this set to the user. 
  
   @return The JavaFX Pane containing Controls allowing the user to update
           all the preferences in this set. 
  */
  public Pane getPane();
  
  /**
   Set the File Spec whose prefs are to be modified, if any. 
  
   @param fileSpec 
  */
  public void setFileSpec(FileSpec fileSpec);
  
  /**
   Save all of these preferences to disk, so that they can be restored
   for the user at a later time. 
  */
  public void save();
  
}
