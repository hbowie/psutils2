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

package com.powersurgepub.psutils2.txmin;

	import com.powersurgepub.psutils2.records.*;
	import com.powersurgepub.psutils2.txbio.*;

  import java.io.*;

/**
 A PSTextMerge input module for reading HTML.

 @author Herb Bowie
 */
public class TextMergeInputHTML 
    extends TextMergeInputModule {
  
  public TextMergeInputHTML () {
    
    modifiers.add("");
    modifiers.add("html1");
    modifiers.add("html2");
    modifiers.add("html3");
    modifiers.add("html4");
    
    labels.add("No HTML");
    labels.add("HTML Bookmarks using Lists");
    labels.add("HTML Table");
    labels.add("HTML Bookmarks using Headings");
    labels.add("HTML Links");
    
    extensions.add("html");
    extensions.add("htm");
    extensions.add("xhtml");
  }
  
  /**
   Get the appropriate data source for this type of data. 
  
   @param chosenFile The file containing the data to be read.
  
   @return The desired data source.
  */
  public DataSource getDataSource(File chosenFile) {
    DataSource dataSource = null;
    switch (inputType) {
      case 1: 
        dataSource = new HTMLFile (chosenFile);
        break;
      case 2:
        dataSource = new HTMLTableFile (chosenFile);
        break;
      case 3:
        HTMLFile htmlIn = new HTMLFile (chosenFile);
        htmlIn.useHeadings();
        dataSource = htmlIn;
        break;
      case 4:
        dataSource = new HTMLLinksFile (chosenFile);
        break;
    }
    return dataSource;
  }

}
