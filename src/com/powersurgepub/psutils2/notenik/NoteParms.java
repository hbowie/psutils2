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

package com.powersurgepub.psutils2.notenik;

  import com.powersurgepub.psutils2.records.*;
  import com.powersurgepub.psutils2.strings.*;
  import com.powersurgepub.psutils2.ui.*;
  import com.powersurgepub.psutils2.values.*;
  import com.powersurgepub.psutils2.widgets.*;

  import java.text.*;
  import java.util.*;

  import javafx.geometry.*;
  import javafx.scene.control.*;
  import javafx.scene.layout.*;
  import javafx.scene.text.*;

/**
 A set of parameters to specify how a particular Note Collection is configured. 
 This object supplied a note collection type, a record definition, and a
 preferred file extension. 

 @author Herb Bowie
 */
public class NoteParms {
  
  public static final double      LABEL_MIN_WIDTH = 120;
  public static final double      LABEL_PREF_WIDTH = 160;
  
  /** The type of notes found in this collection. */
  private    int              noteType = NOTES_ONLY_TYPE;
  public static final int       NOTES_ONLY_TYPE     = 1;
  public static final int       NOTES_PLUS_TYPE     = 2;
  public static final int       NOTES_GENERAL_TYPE  = 3;
  public static final int       NOTES_INDEX_TYPE    = 4;
  public static final int       DEFINED_TYPE        = 5;
  public static final int       MARKDOWN_TYPE       = 6;
  public static final int       TAG_TYPE            = 7;
  public static final int       QUOTE_TYPE          = 8;
  public static final int       NOTES_EXPANDED_TYPE = 9;
  
  /** The record definition used for the notes in this collection. */
  private    RecordDefinition recDef = null;
  
  
  private             boolean   isTemplate          = false;
  
  public static final String  DEFAULT_FILE_EXT = "txt";
  private    String           preferredFileExt    = DEFAULT_FILE_EXT;
  
  private    boolean          metadataAsMarkdown = true;
  
  private    ItemStatusConfig itemStatusConfig = null;
  
  public static final String  FILENAME = "notenik.parms";
  
  public static final String  TITLE_FIELD_NAME  = "Title";
  public static final String  TITLE_COMMON_NAME = "title";
  public static final String  LINK_FIELD_NAME   = "Link";
  public static final String  LINK_COMMON_NAME  = "link";
  public static final String  TAGS_FIELD_NAME   = "Tags";
  public static final String  TAGS_COMMON_NAME  = "tags";
  public static final String  BODY_FIELD_NAME   = "Body";
  public static final String  BODY_COMMON_NAME  = "body";
  public static final String  AUTHOR_FIELD_NAME = "Author";
  public static final String  AUTHOR_COMMON_NAME = "author";
  public static final String  DATE_FIELD_NAME   = "Date";
  public static final String  DATE_COMMON_NAME  = "date";
  public static final String  STATUS_FIELD_NAME = "Status";
  public static final String  STATUS_COMMON_NAME = "status";
  public static final String  RATING_FIELD_NAME = "Rating";
  public static final String  RATING_COMMON_NAME = "rating";
  public static final String  RECURS_FIELD_NAME = "Recurs";
  public static final String  RECURS_COMMON_NAME = "recurs";
  public static final String  TEASER_FIELD_NAME = "Teaser";
  public static final String  TEASER_COMMON_NAME = "teaser";
  public static final String  TYPE_FIELD_NAME   = "Type";
  public static final String  TYPE_COMMON_NAME  = "type";
  public static final String  SEQ_FIELD_NAME    = "Seq";
  public static final String  SEQ_COMMON_NAME   = "seq";
  public static final String  INDEX_FIELD_NAME  = "Index";
  public static final String  INDEX_COMMON_NAME = "index";
  public static final String  MIN_SYS_VERSION_FIELD_NAME = "Minimum System Version";
  
  public static final String  COMPLETE_PATH     = "Complete Path";
  public static final String  BASE_PATH         = "Base Path";
  public static final String  LOCAL_PATH        = "Local Path";
  public static final String  PATH_TO_TOP       = "Path to Top";
  public static final String  DEPTH             = "Depth";
  public static final String  FILE_NAME         = "File Name";
  public static final String  FILE_NAME_BASE    = "File Name Base";
  public static final String  FILE_EXT          = "File Ext";
  public static final String  LAST_MOD_DATE     = "Last Mod Date";
  public static final String  FILE_SIZE         = "File Size";
  public static final String  BREADCRUMBS       = "Breadcrumbs";
  public static final String  LINKED_TAGS       = "Linked Tags";
  public static final String  SINGLE_TAG        = "Tag";
  
  public static final String  BY                = "By";
  public static final String  CREATOR           = "Creator";
  public static final String  EVERY             = "Every";
  public static final String  KEYWORDS          = "Keywords";
  public static final String  CATEGORY          = "Category";
  public static final String  CATEGORIES        = "Categories";
  public static final String  URL               = "URL";
  public static final String  STATE             = "State";
  public static final String  PRIORITY          = "Priority";
  
  public static final String  AUTHOR_INFO       = "Author Info";
  public static final String  AUTHOR_LINK       = "Author Link";
  
  public static final String  WORK_TITLE        = "Work Title";
  public static final String  WORK_TYPE         = "Work Type";
  public static final String  WORK_MINOR_TITLE  = "Work Minor Title";
  public static final String  WORK_IDENTIFIER   = "Work ID";
  public static final String  WORK_PAGE_NUMBERS = "Work Pages";
  public static final String  WORK_RIGHTS       = "Work Rights";
  public static final String  WORK_RIGHTS_HOLDER = "Work Rights Holder";
  public static final String  PUBLISHER         = "Publisher";
  public static final String  PUBLISHER_CITY    = "Publisher City";
  public static final String  SEQUENCE          = "Sequence";
  
  public static final int     UNKNOWN_FIELD_SEQ = 50;
  
  public static final DataFieldDefinition TITLE_DEF 
      = new DataFieldDefinition(TITLE_FIELD_NAME);
  public static final DataFieldDefinition LINK_DEF 
      = new DataFieldDefinition(LINK_FIELD_NAME);
  public static final DataFieldDefinition TAGS_DEF
      = new DataFieldDefinition(TAGS_FIELD_NAME);
  public static final DataFieldDefinition BODY_DEF
      = new DataFieldDefinition(BODY_FIELD_NAME);
  public static final DataFieldDefinition AUTHOR_DEF
      = new DataFieldDefinition(AUTHOR_FIELD_NAME);
  public static final DataFieldDefinition DATE_DEF
      = new DataFieldDefinition(DATE_FIELD_NAME);
  public static final DataFieldDefinition STATUS_DEF
      = new DataFieldDefinition(STATUS_FIELD_NAME);
  public static final DataFieldDefinition RATING_DEF
      = new DataFieldDefinition(RATING_FIELD_NAME);
  public static final DataFieldDefinition TEASER_DEF
      = new DataFieldDefinition(TEASER_FIELD_NAME);
  public static final DataFieldDefinition TYPE_DEF
      = new DataFieldDefinition(TYPE_FIELD_NAME);
  public static final DataFieldDefinition SEQ_DEF
      = new DataFieldDefinition (SEQ_FIELD_NAME);
  public static final DataFieldDefinition INDEX_DEF
      = new DataFieldDefinition (INDEX_FIELD_NAME);
  public static final DataFieldDefinition RECURS_DEF
      = new DataFieldDefinition (RECURS_FIELD_NAME);
  
  public static final boolean  SLASH_TO_SEPARATE = false;
  
  public final static String   YMD_FORMAT_STRING = "yyyy-MM-dd";
  public final static String   MDY_FORMAT_STRING = "MM-dd-yyyy";
  public final static String   HUMAN_DATE_FORMAT_STRING = "EEE MMM d, yyyy";
  public final static String   STANDARD_FORMAT_STRING 
      = "yyyy-MM-dd'T'HH:mm:ssz";
  public final static String   
      COMPLETE_FORMAT_STRING = "EEEE MMMM d, yyyy KK:mm:ss aa zzz";
  
  public final static DateFormat YMD_FORMAT 
      = new SimpleDateFormat (YMD_FORMAT_STRING);
  public final static DateFormat MDY_FORMAT
      = new SimpleDateFormat (MDY_FORMAT_STRING);
  public final static DateFormat HUMAN_DATE_FORMAT
      = new SimpleDateFormat (HUMAN_DATE_FORMAT_STRING);
  public final static DateFormat COMPLETE_FORMAT
      = new SimpleDateFormat (COMPLETE_FORMAT_STRING);
  public final static DateFormat STANDARD_FORMAT
      = new SimpleDateFormat (STANDARD_FORMAT_STRING);

  static {
    TITLE_DEF.setType  (DataFieldDefinition.TITLE_TYPE);
    LINK_DEF.setType   (DataFieldDefinition.LINK_TYPE);
    TAGS_DEF.setType   (DataFieldDefinition.TAGS_TYPE);
    BODY_DEF.setType   (DataFieldDefinition.STRING_BUILDER_TYPE);
    AUTHOR_DEF.setType (DataFieldDefinition.STRING_TYPE);
    DATE_DEF.setType   (DataFieldDefinition.DATE_TYPE);
    STATUS_DEF.setType (DataFieldDefinition.STATUS_TYPE);
    RATING_DEF.setType (DataFieldDefinition.RATING_TYPE);
    TEASER_DEF.setType (DataFieldDefinition.STRING_BUILDER_TYPE);
    TYPE_DEF.setType   (DataFieldDefinition.STRING_TYPE);
    SEQ_DEF.setType    (DataFieldDefinition.SEQ_TYPE);
    INDEX_DEF.setType  (DataFieldDefinition.INDEX_TYPE);
    RECURS_DEF.setType (DataFieldDefinition.RECURS_TYPE);
  }
  
  public NoteParms () {
    itemStatusConfig = ItemStatusConfig.getShared();
  }
  
  public NoteParms (int noteType) {
    itemStatusConfig = ItemStatusConfig.getShared();
    this.noteType = noteType;
  }
  
  public void setNoteType (int noteType) {
    this.noteType = noteType;
  }
  
  public int getNoteType() {
    return noteType;
  }
  
  public boolean notesOnly() {
    return (noteType == NOTES_ONLY_TYPE);
  }
  
  public boolean notesExpanded() {
    return (noteType == NOTES_EXPANDED_TYPE);
  }
  
  public boolean notesPlus() {
    return (noteType == NOTES_PLUS_TYPE);
  }
  
  public boolean notesIndex() {
    return (noteType == NOTES_INDEX_TYPE);
  }
  
  public boolean notesGeneral() {
    return (noteType == NOTES_GENERAL_TYPE);
  }
  
  public boolean definedType() {
    return (noteType == DEFINED_TYPE);
  }
  
  public boolean markdownType() {
    return (noteType == MARKDOWN_TYPE);
  }
  
  public boolean tagType() {
    return (noteType == TAG_TYPE);
  }
  
  public boolean quoteType() {
    return (noteType == QUOTE_TYPE);
  }
  
  /**
   Indicate whether we are reading a template file. 
  
   @param isTemplate True if reading a template file. 
  */
  public void setIsTemplate(boolean isTemplate) {
    this.isTemplate = isTemplate;
  }
  
  /**
   Is this Note I/O instance being used to read a template file?
  
   @return True if we're reading a template file; false otherwise. 
  */
  public boolean isTemplate() {
    return isTemplate;
  }
  
  /**
   Get the Item Status Values to be used. 
  
   @return The Item Status Values to be used. 
  */
  public ItemStatusConfig getItemStatusConfig() {
    return itemStatusConfig;
  }
  
  /**
   Set the Item Status Values to be used. 
  
   @param itemStatusConfig The Item Status Values to be used. 
  */
  public void setItemStatusConfig(ItemStatusConfig itemStatusConfig) {
    this.itemStatusConfig = itemStatusConfig;
  }
  
  /**
   Adjust the status values with the passed string. 
  
   @param statusValues A string consisting of one or more integer + value pairs.
  */
  public void setItemStatusConfig(String statusValues) {
    itemStatusConfig.set(statusValues);
  }
  
  public void newRecordDefinition (DataDictionary dict) {
    if (noteType != DEFINED_TYPE) {
      recDef = new RecordDefinition(dict);
    }
  }
  
  public void setRecDef(RecordDefinition recDef) {
    setRecordDefinition(recDef);
  }
  
  public void setRecordDefinition(RecordDefinition recDef) {
    this.recDef = recDef;
  }
  
  public Note createNewNote() {
    if (recDef == null) {
      buildRecordDefinition();
    }
    Note note = new Note(recDef);
    
    for (int i = 0; i < recDef.getNumberOfFields(); i++) {
      DataFieldDefinition fieldDef = recDef.getDef(i);
      String commonName = fieldDef.getCommonName().toString();
      note.setField(commonName, "");
    }
    
    return note;
  }
  
  /**
   Return the current record definition; if one doesn't yet exist, then 
   build one based on the current note type. 
  
   @return The record definition. 
  */
  public RecordDefinition getRecDef() {
    return getRecordDefinition();
  }
  
  /**
   Return the current record definition; if one doesn't yet exist, then 
   build one based on the current note type. 
  
   @return The record definition. 
  */
  public RecordDefinition getRecordDefinition() {
    if (recDef == null) {
      buildRecordDefinition();
    }
    return recDef;
  }
  
  /**
   Populate a record definition that already exists. 
  
   @param recDef The existing record definition. 
  
   @return The populated record definition, based on the note type. 
  */
  public RecordDefinition buildRecordDefinition(RecordDefinition recDef) {
    this.recDef = recDef;
    return buildRecordDefinition();
  }
  
  /**
   Build the record definition from scratch, based on the note type, after
   clearing out any columns already defined. 
  
   @return The new record definition. 
  */
  public RecordDefinition buildRecordDefinition() {

    if (recDef == null) {
      recDef = new RecordDefinition();
    }
    if (noteType == DEFINED_TYPE) {
      // Leave it alone
    } else {
      recDef.clear();
      if (noteType == NOTES_ONLY_TYPE 
          || noteType == NOTES_PLUS_TYPE
          || noteType == NOTES_INDEX_TYPE) {
        recDef.addColumn(TITLE_DEF);
        recDef.addColumn(TAGS_DEF);
        recDef.addColumn(LINK_DEF);
        recDef.addColumn(BODY_DEF);
      }
      if (noteType == NOTES_EXPANDED_TYPE
          || noteType == NOTES_INDEX_TYPE) {
        recDef.addColumn(TITLE_DEF);
        recDef.addColumn (AUTHOR_DEF);
        recDef.addColumn(DATE_DEF);
        recDef.addColumn(RECURS_DEF);
        recDef.addColumn(STATUS_DEF);
        recDef.addColumn(TYPE_DEF);
        recDef.addColumn(SEQ_DEF);
        recDef.addColumn(TAGS_DEF);
        recDef.addColumn(LINK_DEF);
        recDef.addColumn(RATING_DEF);
        recDef.addColumn(INDEX_DEF);
        recDef.addColumn(TEASER_DEF);
        recDef.addColumn(BODY_DEF);
      }
      if (noteType == MARKDOWN_TYPE 
          || noteType == TAG_TYPE) {
        recDef.addColumn(TITLE_DEF);
        recDef.addColumn (COMPLETE_PATH);
        recDef.addColumn (BASE_PATH);
        recDef.addColumn (LOCAL_PATH);
        recDef.addColumn (PATH_TO_TOP);
        recDef.addColumn (DEPTH);
        recDef.addColumn (FILE_NAME);
        recDef.addColumn (FILE_NAME_BASE);
        recDef.addColumn (FILE_EXT);
        recDef.addColumn (LAST_MOD_DATE);
        recDef.addColumn (FILE_SIZE);
        recDef.addColumn (AUTHOR_DEF);
        recDef.addColumn (DATE_DEF);
        recDef.addColumn (STATUS_DEF);
        recDef.addColumn (BREADCRUMBS);
        switch (noteType) {
          case MARKDOWN_TYPE:
            recDef.addColumn(TAGS_DEF);
            recDef.addColumn (LINKED_TAGS);
            break;
          case TAG_TYPE:
            recDef.addColumn (SINGLE_TAG);
            break;
        }
      }
      if (noteType == QUOTE_TYPE) {
        recDef.addColumn(TITLE_DEF);
        recDef.addColumn(TAGS_DEF);
        recDef.addColumn(AUTHOR_DEF);
        recDef.addColumn(AUTHOR_INFO);
        recDef.addColumn(AUTHOR_LINK);
        recDef.addColumn(WORK_TITLE);
        recDef.addColumn(WORK_TYPE);
        recDef.addColumn(DATE_DEF);
        recDef.addColumn(WORK_MINOR_TITLE);
        recDef.addColumn(LINK_DEF);
        recDef.addColumn(WORK_IDENTIFIER);
        recDef.addColumn(WORK_PAGE_NUMBERS);
        recDef.addColumn(WORK_RIGHTS);
        recDef.addColumn(WORK_RIGHTS_HOLDER);
        recDef.addColumn(PUBLISHER);
        recDef.addColumn(PUBLISHER_CITY);
        recDef.addColumn(BODY_DEF);
      }
    }

    return recDef;
  }
  
  public static int getNormalSeq(CommonName commonName) {
    if (isTitle(commonName)) {
      return 0;
    }
    else
    if (isAuthor(commonName)) {
      return 1;
    } 
    else
    if (isDate(commonName)) {
      return 2;
    }
    else
    if (isRecurs(commonName)) {
      return 3;
    }
    else
    if (isStatus(commonName)) {
      return 4;
    }
    else
    if (isType(commonName)) {
      return 5;
    }
    else
    if (isSeq(commonName)) {
      return 6;
    }
    else
    if (isTags(commonName)) {
      return 7;
    }
    else
    if (isLink(commonName)) {
      return 8;
    } 
    else
    if (isRating(commonName)) {
      return 9;
    }
    else
    if (isIndex(commonName)) {
       return 10;
    }
    else
    if (isTeaser(commonName)) {
      return 98;
    }
    else
    if (isBody(commonName)) {
      return 99;
    }
    else {
      return UNKNOWN_FIELD_SEQ;
    }
  }
  
  /**
   Check to see if the passed field name is valid for this type of note. 
  
   @param fieldName A potential field name. 
  
   @return A DataFieldDefinition for the field, or null, if the field name
           is not valid. 
  */
  public DataFieldDefinition checkForFieldName(String fieldName) {
    
    if (fieldName.length() > 48) {
      return null;
    }
    
    CommonName commonName = new CommonName (fieldName);
    
    // If this is a url, then don't confuse it with a field name.
    if (commonName.getCommonForm().endsWith("http")
         || commonName.getCommonForm().endsWith("https")
         || commonName.getCommonForm().endsWith("ftp")
         || commonName.getCommonForm().endsWith("mailto")) {
      return null;
    }
    
    // If the potential field name contains a comma, then ignore it
    int commaPosition = fieldName.indexOf(",");
    if (commaPosition >= 0) {
      return null;
    }
    
    // Check for most basic note fields. 
    if (isTitle(commonName)) {
      return TITLE_DEF;
    }
    if (isTags(commonName)) {
      return TAGS_DEF;
    }
    if (isLink(commonName)) {
      return LINK_DEF;
    }
    if (isBody(commonName)) {
      return BODY_DEF;
    }
    
    if (notesOnly()) {
      return null;
    }
    
    if (isAuthor(commonName)) {
      return AUTHOR_DEF;
    }
    if (isDate(commonName)) {
      return DATE_DEF;
    }
    if (isRecurs(commonName)) {
      return RECURS_DEF;
    }
    if (isStatus(commonName)) {
      return STATUS_DEF;
    }
    if (isTeaser(commonName)) {
      return TEASER_DEF;
    }
    if (isRating(commonName)) {
      return RATING_DEF;
    }
    if (isType(commonName)) {
      return TYPE_DEF;
    }
    if (isSeq(commonName)) {
      return SEQ_DEF;
    }
    if (isIndex(commonName)) {
      return INDEX_DEF;
    }
    
    if (notesExpanded()) {
      return null;
    }
    
    int columnNumber = recDef.getColumnNumber(commonName);
    if (columnNumber < 0) {
      if (definedType()) {
        return null;
      } else {
        DataFieldDefinition newDef = new DataFieldDefinition(fieldName);
        recDef.addColumn(newDef);
        return newDef;
      }
    } else {
      return recDef.getDef(columnNumber);
    }
    
  }
  
  public static boolean isTitle(CommonName commonName) {
    return (commonName.getCommonForm().equals(TITLE_COMMON_NAME));
  }
  
  public static boolean isType(CommonName commonName) {
    return (commonName.getCommonForm().equals(TYPE_COMMON_NAME));
  }
  
  public static boolean isSeq(CommonName commonName) {
    return (commonName.getCommonForm().equals(SEQ_COMMON_NAME)
        || commonName.getCommonForm().equalsIgnoreCase(SEQUENCE)
        || commonName.getCommonForm().equalsIgnoreCase("rev")
        || commonName.getCommonForm().equalsIgnoreCase("revision")
        || commonName.getCommonForm().equalsIgnoreCase("version"));
  }
  
  public static boolean isAuthor(CommonName commonName) {
    return (commonName.getCommonForm().equals(AUTHOR_COMMON_NAME)
        || commonName.getCommonForm().equalsIgnoreCase(BY)
        || commonName.getCommonForm().equalsIgnoreCase(CREATOR));
  }
  
  public static boolean isLink(CommonName commonName) {
    return (commonName.getCommonForm().equals(LINK_COMMON_NAME)
        || commonName.getCommonForm().equalsIgnoreCase(URL));
  }
  
  public static boolean isTags(CommonName commonName) {
    return (commonName.getCommonForm().equals(TAGS_COMMON_NAME)
        || commonName.getCommonForm().equalsIgnoreCase(KEYWORDS)
        || commonName.getCommonForm().equalsIgnoreCase(CATEGORY)
        || commonName.getCommonForm().equalsIgnoreCase(CATEGORIES));
  }
  
  public static boolean isDate(CommonName commonName) {
    return (commonName.getCommonForm().equals(DATE_COMMON_NAME));
  }
  
  public static boolean isRecurs(CommonName commonName) {
    return (commonName.getCommonForm().equals(RECURS_COMMON_NAME)
        || commonName.getCommonForm().equalsIgnoreCase(EVERY)
        || (commonName.getCommonForm().startsWith(RECURS_COMMON_NAME)
            && commonName.getCommonForm().endsWith(EVERY.toLowerCase())));
  }
  
  public static boolean isStatus(CommonName commonName) {
    return (commonName.getCommonForm().equals(STATUS_COMMON_NAME));
  }
  
  public static boolean isRating(CommonName commonName) {
    return (commonName.getCommonForm().equals(RATING_COMMON_NAME)
        || commonName.getCommonForm().equalsIgnoreCase(PRIORITY));
  }
  
  public static boolean isIndex(CommonName commonName) {
    return (commonName.getCommonForm().equals(INDEX_COMMON_NAME));
  }
  
  public static boolean isTeaser(CommonName commonName) {
    return (commonName.getCommonForm().equals(TEASER_COMMON_NAME));
  }
  
  public static boolean isBody(CommonName commonName) {
    return (commonName.getCommonForm().equals(BODY_COMMON_NAME));
  }
  
  /**
   Pass any metadata lines to the markdown parser as well. 
  
   @param metadataAsMarkdown True if metadata lines should appear as part
                             of output HTML, false otherwise. 
  */
  public void setMetadataAsMarkdown (boolean metadataAsMarkdown) {
    this.metadataAsMarkdown = metadataAsMarkdown;
  }
  
  public boolean treatMetadataAsMarkdown() {
    return metadataAsMarkdown;
  }
  
  public void setPreferredFileExt(String preferredFileExt) {
    this.preferredFileExt = preferredFileExt;
  }
  
  public String getPreferredFileExt() {
    return preferredFileExt;
  }
  
  /**
   Create a new Swing component that can be used to display and optionally
   update a data field.
  
   @param fieldDef The definition of the type of field.
   @param grid     The GridPane object to be used to place this component.
   @param row      The row number on which this field is to be place. 
  
   @return         The data widget that was created. 
  */
  public WidgetWithLabel getWidgetWithLabel(
      DataFieldDefinition fieldDef,
      GridPane grid,
      int row) {
    
    // Define needed fields
    int fieldType = fieldDef.getType();
    String fieldName = fieldDef.getProperName();
    Label label = new Label(fieldName + ": ");
    label.setAlignment(Pos.TOP_LEFT);
    label.setTextAlignment(TextAlignment.LEFT);
    WidgetWithLabel widgetWithLabel = new WidgetWithLabel();
    widgetWithLabel.setLabel(label);
    
    // Return label and field appropriate for field type
    switch (fieldType) {
      
      // Tags
      case DataFieldDefinition.TAGS_TYPE:
        TextSelector tagsTextSelector = new TextSelector();
        tagsTextSelector.setEditable(true);
        label.setLabelFor(tagsTextSelector);
        grid.add(label, 0, row, 1, 1);
        grid.add(tagsTextSelector, 1, row, 1, 1);
        GridPane.setHgrow(tagsTextSelector, Priority.ALWAYS);
        widgetWithLabel.setLabel(label);
        widgetWithLabel.setWidget(tagsTextSelector); 
        break;
        
      // Link  
      case DataFieldDefinition.LINK_TYPE:
        LinkLabel linkLabel = new LinkLabel(fieldName + ": ");
        LinkWidget linkText = new LinkWidget();
        linkLabel.setLinkTextArea(linkText);
        grid.add(linkLabel, 0, row);
        grid.add(linkText, 1, row);
        GridPane.setHgrow(linkText, Priority.ALWAYS);
        widgetWithLabel.setLabel(linkLabel);
        widgetWithLabel.setWidget(linkText);
        break;
        
      // Status  
      case DataFieldDefinition.STATUS_TYPE:
        ComboBoxWidget statusText = new ComboBoxWidget();
        label.setLabelFor(statusText);
        itemStatusConfig.populateComboBox(statusText);
        grid.add(label, 0, row);
        grid.add(statusText, 1, row);
        GridPane.setHgrow(statusText, Priority.ALWAYS);
        widgetWithLabel.setLabel(label);
        widgetWithLabel.setWidget(statusText);
        break;
        
      // Complex text field  
      case DataFieldDefinition.STRING_BUILDER_TYPE:
        ScrollingTextArea scrollingText;
        if (fieldName.equals(BODY_FIELD_NAME)) {
          scrollingText = new ScrollingTextArea(60, 20, true, true);
          scrollingText.setMaxHeight(Double.MAX_VALUE);
        } else {
          scrollingText = new ScrollingTextArea(60, 5, true, true);
        }
        label.setLabelFor(scrollingText);
        grid.add(label, 0, row);
        grid.add(scrollingText, 1, row);
        GridPane.setHgrow(scrollingText, Priority.ALWAYS);
        widgetWithLabel.setLabel(label);
        widgetWithLabel.setWidget(scrollingText);
        break;
        
      // Label field (display but no data entry) 
      case DataFieldDefinition.LABEL_TYPE:
        LabelWidget labelWidget = new LabelWidget();
        label.setLabelFor(labelWidget);
        grid.add(label, 0, row);
        grid.add(labelWidget, 1, row);
        GridPane.setHgrow(labelWidget, Priority.ALWAYS);
        widgetWithLabel.setLabel(label);
        widgetWithLabel.setWidget(labelWidget);
        break;
        
      // Date field
      case DataFieldDefinition.DATE_TYPE:
        DateWidget dateWidget = new DateWidget();
        label.setLabelFor(dateWidget);
        grid.add(label, 0, row);
        grid.add(dateWidget, 1, row);
        GridPane.setHgrow(dateWidget, Priority.ALWAYS);
        widgetWithLabel.setLabel(label);
        widgetWithLabel.setWidget(dateWidget);
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
        label.setLabelFor(oneLiner);
        grid.add(label, 0, row);
        grid.add(oneLiner, 1, row);
        GridPane.setHgrow(oneLiner, Priority.ALWAYS);
        widgetWithLabel.setLabel(label);
        widgetWithLabel.setWidget(oneLiner);
        break;
    }
    
    widgetWithLabel.getLabel().setMinWidth(LABEL_MIN_WIDTH);
    widgetWithLabel.getLabel().setPrefWidth(LABEL_PREF_WIDTH);
    widgetWithLabel.getLabel().setMaxWidth(Double.MAX_VALUE);
    
    // Return the results
    return widgetWithLabel;
  }
  
  public void display() {
    System.out.println(" ");
    System.out.println("NoteParms.display");
    System.out.println("Note Type = " + String.valueOf(noteType));
    recDef.display();
  }

}
