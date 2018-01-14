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

package com.powersurgepub.psutils2.txmin;

	import com.powersurgepub.psutils2.notenik.*;
	import com.powersurgepub.psutils2.records.*;

  import java.io.*;

/**
 A PSTextMerge input module for reading Notenik files.

 @author Herb Bowie
 */
public class TextMergeInputNotenik 
    extends TextMergeInputModule {
  
  public TextMergeInputNotenik () {
    
    modifiers.add("");
    modifiers.add("notenik");
    modifiers.add("notenik+");
    modifiers.add("notenik-general");
    modifiers.add("notenik-index");
    modifiers.add("notenik-defined");
    
    labels.add("No Notenik");
    labels.add("Notenik Notes");
    labels.add("Notenik Notes Plus");
    labels.add("Notenik General");
    labels.add("Notenik Index");
    labels.add("Notenik Defined");
  }
  
  /**
   Is this input module interested in processing the specified file?
  
   @param candidate The file being considered. 
  
   @return True if the input module thinks this file is worth processing,
           otherwise false. 
  */
  public boolean isInterestedIn(File candidate) {
    return NoteIO.isInterestedIn(candidate);
  }
  
  /**
   Get the appropriate data source for this type of data. 
  
   @param chosenFile The file containing the data to be read.
  
   @return The desired data source.
  */
  public DataSource getDataSource(File chosenFile) {
    DataSource dataSource;
    if (inputType == NoteParms.NOTES_INDEX_TYPE) {
      dataSource = new NoteIndexIO(chosenFile, inputType);
    } else {
      NoteIO noteIO = new NoteIO(chosenFile, inputType);
      NoteCollectionTemplate template = new NoteCollectionTemplate(chosenFile);
      NoteParms templateParms = template.getNoteParms();
      if (templateParms != null) {
        noteIO = new NoteIO (chosenFile, templateParms);
      }
      dataSource = noteIO;
    }
    return dataSource;
  }

}
