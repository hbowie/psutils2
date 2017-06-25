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

package com.powersurgepub.psutils2.records;

  import com.powersurgepub.psutils2.basic.*;
  import com.powersurgepub.psutils2.index.*;
  import com.powersurgepub.psutils2.logging.*;
  import com.powersurgepub.psutils2.strings.*;
  import com.powersurgepub.psutils2.tags.*;
  import com.powersurgepub.psutils2.values.*;
  import com.powersurgepub.psutils2.widgets.*;

/**
 A class containing factory methods to create values and widgets of various types.

 @author Herb Bowie
 */
public class DataFactory {
  
  /**
   Make an appropriate data value field for the field type. 
  
   @param def The definition for the desired field. 
  
   @return The appropriate data value. 
  */
  public static DataValue makeDataValue(DataFieldDefinition def) {
    return makeDataValue(def.getType());
  }
  
  /**
   Make an appropriate data value field for the field type. 
  
   @param dataFieldType The desired field type. 
  
   @return The appropriate data value. 
  */
  public static DataValue makeDataValue(int dataFieldType) {
    
    DataValue value;
    
    switch (dataFieldType) {
      
      // Title
      case DataFieldDefinition.TITLE_TYPE:
        Title title = new Title();
        value = title;
        break;
        
      // Tags
      case DataFieldDefinition.TAGS_TYPE:
        Tags tags = new Tags();
        value = tags;
        break;
        
      // Link  
      case DataFieldDefinition.LINK_TYPE:
        Link link = new Link();
        value = link;
        break;
        
      // Complex text field  
      case DataFieldDefinition.STRING_BUILDER_TYPE:
        DataValueStringBuilder builder = new DataValueStringBuilder();
        value = builder;
        break;
        
      // Author
      case DataFieldDefinition.AUTHOR_TYPE:
        Author author = new Author();
        value = author;
        break;
        
      // Date
      case DataFieldDefinition.DATE_TYPE:
        StringDate date = new StringDate();
        value = date;
        break;
        
      // Rating
      case DataFieldDefinition.RATING_TYPE:
        Rating rating = new Rating();
        value = rating;
        break;
        
      // Seq
      case DataFieldDefinition.SEQ_TYPE:
        DataValueSeq seq = new DataValueSeq();
        value = seq;
        break;
        
      // Index
      case DataFieldDefinition.INDEX_TYPE:
        IndexPageValue ix = new IndexPageValue();
        value = ix;
        break;
        
      // Recurs
      case DataFieldDefinition.RECURS_TYPE:
        RecursValue recurs = new RecursValue();
        value = recurs;
        break;
      // Simple string
      case DataFieldDefinition.LABEL_TYPE: 
      case DataFieldDefinition.DEFAULT_TYPE:
      case DataFieldDefinition.STRING_TYPE: 
      default:
        DataValueString str = new DataValueString();
        value = str;
        break;
    }
    
    return value;
  }
  
  /**
   Make a UI widget appropriate to the field type. 
  
   @param dataFieldType The type of field. 
  
   @return The appropriate widget.
  */
  public static DataWidget makeDataWidget(int dataFieldType) {
    
    DataWidget widget;
    
    switch (dataFieldType) {
      
      // Tags
      case DataFieldDefinition.TAGS_TYPE:
        TextSelector tagsTextSelector = new TextSelector();
        widget = tagsTextSelector;
        break;
        
      // Link  
      case DataFieldDefinition.LINK_TYPE:
        ScrollingTextArea linkText 
            = new ScrollingTextArea(60, 2, true, false);
        widget = linkText;
        break;
        
      // Complex text field  
      case DataFieldDefinition.STRING_BUILDER_TYPE:
        ScrollingTextArea scrollingText 
            = new ScrollingTextArea(60, 5, true, true);
        widget = scrollingText;
        break;
        
      // Label field (display but no data entry) 
      case DataFieldDefinition.LABEL_TYPE:
        LabelWidget labelWidget = new LabelWidget();
        widget = labelWidget;
        break;
        
      // Date Editor
      case DataFieldDefinition.DATE_TYPE:
        DateWidget dateWidget = new DateWidget();
        widget = dateWidget;
        break;
        
      // Single-line Text Field  
      case DataFieldDefinition.DEFAULT_TYPE:
      case DataFieldDefinition.STRING_TYPE:
      case DataFieldDefinition.TITLE_TYPE:  
      case DataFieldDefinition.SEQ_TYPE:
      case DataFieldDefinition.INDEX_TYPE:
      case DataFieldDefinition.RECURS_TYPE:
      default:
        OneLiner oneLiner = new OneLiner();
        widget = oneLiner;
        break;
    }
    return widget;
  }

}
