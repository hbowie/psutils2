/*
 * Copyright 1999 - 2018 Herb Bowie
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

import com.powersurgepub.psutils2.records.*;

import java.io.*;

/**
 A PSTextMerge input module for reading VCard files.

 @author Herb Bowie
 */
public class TextMergeInputVCard
    extends TextMergeInputModule {

  public TextMergeInputVCard() {
    
    modifiers.add("");
    modifiers.add("vcard");
    
    labels.add("No VCard");
    labels.add("VCard");

  }
  
    /**
   Get the appropriate data source for this type of data. 
  
   @param chosenFile The file containing the data to be read.
  
   @return The desired data source.
  */
  public DataSource getDataSource(File chosenFile) {
    DataSource dataSource = new VCardReader (chosenFile);
    return dataSource;
  }

}
