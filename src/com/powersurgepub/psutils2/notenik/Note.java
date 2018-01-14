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

  import com.powersurgepub.psutils2.files.*;
  import com.powersurgepub.psutils2.index.*;
  import com.powersurgepub.psutils2.links.*;
  import com.powersurgepub.psutils2.records.*;
  import com.powersurgepub.psutils2.strings.*;
  import com.powersurgepub.psutils2.tags.*;
  import com.powersurgepub.psutils2.ui.*;
  import com.powersurgepub.psutils2.values.*;
  import com.powersurgepub.psutils2.widgets.*;

  import java.io.*;
  import java.text.*;
  import java.util.*;

  import javafx.scene.control.*;

/**
 A Note.

 @author Herb Bowie
 */
public class Note 
    extends
      DataRecord
    implements 
      Comparable, 
      Taggable, 
      ItemWithURL {
  
  private int           collectionID = -1;
  private boolean       deleted = false;
  
  private RecordDefinition recDef;
  
  /** This is a readable file name derived from the Note's title. */
  private String        fileName = "";
  
  /** The disk location at which this note is stored. */
  private FileName      diskLocation = null;
  
  private Date          lastModDate;
  private String        lastModDateStr;
  
  private boolean       synced = false;
  
  private TreeItem<TagsNodeValue> tagsNode = null;
  
  private Title               titleValue = null;
  private DataField           titleField = null;
  
  private DataValueString     typeValue   = null;
  private DataField           typeField   = null;
  private boolean             typeAdded   = false;
  
  private DataValueSeq        seqValue   = null;
  private DataField           seqField   = null;
  private boolean             seqAdded   = false;
  
  private Author              authorValue = null;
  private DataField           authorField = null;
  private boolean             authorAdded = false;
  
  private ItemStatus          statusValue = null;
  private DataField           statusField = null;
  private boolean             statusAdded = false;
  
  private StringDate          dateValue = null;
  private DataField           dateField = null;
  private boolean             dateAdded = false;
  
  private RecursValue         recursValue = null;
  private DataField           recursField = null;
  private boolean             recursAdded = false;

  private Link                linkValue = null;
  private DataField           linkField = null;
  private boolean             linkAdded = false;
  
  private Tags                tagsValue = null;
  private DataField           tagsField = null;
  private boolean             tagsAdded = false;
  
  private Rating              ratingValue = null;
  private DataField           ratingField = null;
  private boolean             ratingAdded = false;
  
  private IndexPageValue      indexValue = null;
  private DataField           indexField = null;
  private boolean             indexAdded = false;
  
  private DataValueStringBuilder codeValue = null;
  private DataField           codeField;
  private boolean             codeAdded = false;
  
  private DataValueStringBuilder teaserValue = null;
  private DataField           teaserField;
  private boolean             teaserAdded = false;
  
  private DataValueStringBuilder bodyValue = null;
  private DataField           bodyField;
  private boolean             bodyAdded = false;
  
  public static final String    UP_ONE_FOLDER   = "../";
  
  private    SimpleDateFormat   dateFormat 
      = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss zzz");
  
  public Note(RecordDefinition recDef) {
    this.recDef = recDef;
    initNoteFields();
    setLastModDateToday();
  }
  
  public Note(RecordDefinition recDef, String title) {
    this.recDef = recDef;
    initNoteFields();
    setTitle(title);
    setLastModDateToday();
  }
  
  public Note(RecordDefinition recDef, String title, String body) {
    this.recDef = recDef;
    initNoteFields();
    setTitle(title);
    setBody(body);
    setLastModDateToday();
  }
  
  /**
   Copy an existing note to create a new one with the same data. 
  
   @param fromNote The existing note to be copied.
  */
  public Note(Note fromNote) {
    this.recDef = fromNote.getRecDef();
    initNoteFields();
    setLastModDateToday();
    for (int i = 0; i < fromNote.getNumberOfFields(); i++) {
      
      DataField fromField = fromNote.getField(i);
      DataValue fromValue = fromField.getDataValue();
      DataFieldDefinition fromDef = fromField.getDef();
      CommonName fromCommon = fromDef.getCommonName();
      
      if (NoteParms.isTitle(fromCommon)) {
        setTitle(fromValue.toString());
      }
      else
      if (NoteParms.isType(fromCommon)) {
        setType(fromValue.toString());
      }
      else
      if (NoteParms.isSeq(fromCommon)) {
        setSeq(fromValue.toString());
      }
      else
      if (NoteParms.isAuthor(fromCommon)) {
        setAuthor(fromValue.toString());
      }
      else
      if (NoteParms.isDate(fromCommon)) {
        setDate(fromValue.toString());
      }
      else
      if (NoteParms.isRecurs(fromCommon)) {
        setRecurs(fromValue.toString());
      }
      else
      if (NoteParms.isStatus(fromCommon)) {
        setStatus(fromValue.toString());
      }
      else
      if (NoteParms.isLink(fromCommon)) {
        setLink(fromValue.toString());
      }
      else
      if (NoteParms.isTags(fromCommon)) {
        setTags(fromValue.toString());
      }
      else
      if (NoteParms.isRating(fromCommon)) {
        setRating(fromValue.toString());
      }
      else
      if (NoteParms.isIndex(fromCommon)) {
        setIndex(fromValue.toString());
      }
      else
      if (NoteParms.isCode(fromCommon)) {
        setCode(fromValue.toString());
      }
      else
      if (NoteParms.isTeaser(fromCommon)) {
        setTeaser(fromValue.toString());
      }
      else {
        DataValue toValue = DataFactory.makeDataValue(fromDef);
        toValue.set(fromValue.toString());
        DataField toField = new DataField(fromDef, toValue);
        storeField (recDef, toField);
      }
    } // end for each from field
  }
  
  private void initNoteFields() {
    
    // Build the Title field
    titleValue = new Title();
    titleField = new DataField(NoteParms.TITLE_DEF, titleValue);
    storeField (recDef, titleField);
    // addField(titleField);
    
    // Build the Author field
    authorValue = new Author();
    authorField = new DataField(NoteParms.AUTHOR_DEF, authorValue);
    authorAdded = false;
    
    // Build the Type field
    typeValue = new DataValueString();
    typeField = new DataField(NoteParms.TYPE_DEF, typeValue);
    typeAdded = false;
    
    // Build the Seq field
    seqValue = new DataValueSeq();
    seqField = new DataField(NoteParms.SEQ_DEF, seqValue);
    seqAdded = false;
    
    // Build the Status field
    statusValue = new ItemStatus();
    statusField = new DataField(NoteParms.STATUS_DEF, statusValue);
    statusAdded = false;
    
    // Build the Date field
    dateValue = new StringDate();
    dateField = new DataField(NoteParms.DATE_DEF, dateValue);
    dateAdded = false;
    
    // Build the Recurs field
    recursValue = new RecursValue();
    recursField = new DataField(NoteParms.RECURS_DEF, recursValue);
    recursAdded = false;
    
    // Build the Link field
    linkValue = new Link();
    linkField = new DataField(NoteParms.LINK_DEF, linkValue);
    linkAdded = false;
    
    // Build the Tags field
    tagsValue = new Tags();
    tagsField = new DataField(NoteParms.TAGS_DEF, tagsValue);
    tagsAdded = false;
    
    // Build the Rating/Priority field
    ratingValue = new Rating();
    ratingField = new DataField(NoteParms.RATING_DEF, ratingValue);
    ratingAdded = false;
    
    // Build the Index field
    indexValue = new IndexPageValue();
    indexField = new DataField(NoteParms.INDEX_DEF, indexValue);
    indexAdded = false;
    
    // Build the Code field
    codeValue = new DataValueStringBuilder();
    codeField = new DataField(NoteParms.CODE_DEF, codeValue);
    codeAdded = false;
    
    // Build the Teaser field
    teaserValue = new DataValueStringBuilder();
    teaserField = new DataField(NoteParms.TEASER_DEF, teaserValue);
    
    // Build the body field
    bodyValue = new DataValueStringBuilder();
    bodyField = new DataField(NoteParms.BODY_DEF, bodyValue);
    bodyAdded = false;
  }
  
  public RecordDefinition getRecDef() {
    return recDef;
  }
  
  /**
   Make an appropriate data value field for the field type. 
  
   @param def The definition for the desired field. 
  
   @return The appropriate data value. 
  */
  public static DataValue makeDataValue(DataFieldDefinition def) {
    
    return DataFactory.makeDataValue(def.getType());
  }
  
  public boolean equals (Object obj2) {
    boolean eq = false;
    if (obj2 != null
        && obj2 instanceof Note) {
      Note note2 = (Note)obj2;
      eq = (this.getUniqueKey().equalsIgnoreCase (note2.getUniqueKey()));
    }
    return eq;
  }
  
  public boolean equalsUniqueKey(String key2) {
    if (key2 == null) {
      return false;
    } 
    else
    if (key2.trim().length() == 0) {
      return false;
    } else {
      return (this.getUniqueKey().equalsIgnoreCase(key2));
    }
  }
  
  /**
   Compare this Note object to another, using the titles for comparison.
  
   @param The second object to compare to this one.
  
   @return A number less than zero if this object is less than the second,
           a number greater than zero if this object is greater than the second,
           or zero if the two titles are equal (ignoring case differences).
   */
  public int compareTo (Object obj2) {
    int comparison = -1;
    if (obj2 instanceof Note) {
      Note note2 = (Note)obj2;
      comparison = this.getUniqueKey().compareToIgnoreCase(note2.getUniqueKey());
    }
    return comparison;
  }
  
  /**
   Compare this Note object to another, using the titles for comparison.
  
   @param The second object to compare to this one.
  
   @return A number less than zero if this object is less than the second,
           a number greater than zero if this object is greater than the second,
           or zero if the two titles are equal (ignoring case differences).
   */
  public int compareTo (Note note2) {
    int comparison = -1;
      comparison = this.getUniqueKey().compareToIgnoreCase(note2.getUniqueKey());

    return comparison;
  }
  
  public boolean hasUniqueKey() {
    return (getUniqueKey() != null
        && getUniqueKey().length() > 0);
  }
  
  /**
   Get a key that uniquely identifies this note. 
  
   @return A unique key based on the note's title. 
  */
  public String getUniqueKey() {
    return titleValue.getLowerHyphens();
  }
  
  /**
   Given a title string, return a unique key for a note with that tile. 
  
   @param title The title of a note. 
  
   @return The unique key for a note with that tile.  
  */
  public static String makeUniqueKey(String title) {
    return StringUtils.makeFileName(title, false);
  }
  
  /**
   Return a string that can be used to sequence this note in a list of other
   notes, 
  
   @param parm Indicates the type of sort the user has requested. 
  
   @return The string containing this note's current sort key. 
  */
  public String getSortKey (NoteSortParm parm) {

    switch (parm.getParm()) {
      case NoteSortParm.SORT_TASKS_BY_DATE:
        return (
            getDone() +
            getDateYMDforSort() + 
            seqValue.toPaddedString('0', 8, '0', 4) + 
            titleValue.getLowerHyphens());
      case NoteSortParm.SORT_TASKS_BY_SEQ:
        return (
            getDone() +
            seqValue.toPaddedString('0', 8, '0', 4) + 
            getDateYMDforSort() + 
            titleValue.getLowerHyphens());
      case NoteSortParm.SORT_BY_SEQ_AND_TITLE:
        return (
            seqValue.toPaddedString('0', 8, '0', 4) + 
            titleValue.getLowerHyphens());
      default:
        return 
            titleValue.getLowerHyphens();
    }
  }
  
  public String getSortKeyToDisplay (NoteSortParm parm) {

    switch (parm.getParm()) {
      case NoteSortParm.SORT_TASKS_BY_DATE:
        return (
            getDone() + " " +
            getDateYMD() + " " +
            seqValue.toPaddedString
               (' ', parm.getMaxPositionsToLeftOfDecimal(), 
                ' ', parm.getMaxPositionsToRightOfDecimal()) + "  " +  
            titleValue.toString());
      case NoteSortParm.SORT_TASKS_BY_SEQ:
        return (
            getDone() + " " +
              seqValue.toPaddedString
               (' ', parm.getMaxPositionsToLeftOfDecimal(), 
                ' ', parm.getMaxPositionsToRightOfDecimal()) + "  " + 
            getDateYMD() + " " + 
            titleValue.toString());
      case NoteSortParm.SORT_BY_SEQ_AND_TITLE:
        return (seqValue.toPaddedString
          (' ', parm.getMaxPositionsToLeftOfDecimal(), 
           ' ', parm.getMaxPositionsToRightOfDecimal()) 
            + "  " + titleValue.toString());
      default:
        return titleValue.toString();
    }
  }
  
  public void merge (Note note2) {

    // Merge URLs
    if (note2.hasLink()) {
      setLink (note2.getLink());
    }

    // Merge titles
    if (note2.getTitle().length() > getTitle().length()) {
      setTitle (note2.getTitle());
    }

    // Merge tags
    getTags().merge (note2.getTags());

    // Merge comments
    if (getBody().equals(note2.getBody())) {
      // do nothing
    }
    else
    if (note2.getBody().length() == 0) {
      // do nothing
    }
    else
    if (getBody().length() == 0) {
      setBody (note2.getBody());
    } else {
      setBody (getBody() + " " + note2.getBody());
    }
  }
  
  /**
   Set the appropriate note field, depending on the field name. 
  
   @param fieldName The name of the field to be set. 
   @param data The value to be used. 
  */
  public void setField(String fieldName, String data) {
    
    DataFieldDefinition fieldDef = new DataFieldDefinition(fieldName);
    String commonName = fieldDef.getCommonName().getCommonForm();

    if (commonName.equals(NoteParms.TITLE_COMMON_NAME)) {
      setTitle(data);
    }
    else
    if (commonName.equals(NoteParms.AUTHOR_COMMON_NAME)
        || commonName.equalsIgnoreCase(NoteParms.BY)
        || commonName.equalsIgnoreCase(NoteParms.CREATOR)) {
      setAuthor(data);
    }
    else
    if (commonName.equalsIgnoreCase(NoteParms.TYPE_COMMON_NAME)) {
      setType(data);
    }
    else
    if (commonName.equalsIgnoreCase(NoteParms.SEQ_COMMON_NAME)) {
      setSeq(data);
    }
    else
    if (commonName.equals((NoteParms.STATUS_COMMON_NAME))) {
      setStatus(data);
    }
    else
    if (commonName.equals(NoteParms.DATE_COMMON_NAME)) {
      setDate(data);
    }
    else
    if (commonName.equals(NoteParms.RECURS_COMMON_NAME)) {
      setRecurs(data);
    }
    else
    if (commonName.equals(NoteParms.LINK_COMMON_NAME)) {
      setLink(data);
    }
    else
    if (commonName.equals(NoteParms.TAGS_COMMON_NAME)) {
      setTags(data);
    }
    else
    if (commonName.equals(NoteParms.CODE_COMMON_NAME)) {
      setCode(data);
    }
    else
    if (commonName.equals(NoteParms.TEASER_COMMON_NAME)) {
      setTeaser(data);
    }
    else
    if (commonName.equals(NoteParms.BODY_COMMON_NAME)) {
      setBody(data);
    } else {
      fieldDef.setType(DataFieldDefinition.STRING_BUILDER_TYPE);
      DataValueStringBuilder dataValue = new DataValueStringBuilder(data);
      DataField dataField = new DataField (fieldDef, dataValue);
      storeField(recDef, dataField);
    }
  }
  
  /**
   Set the note's title, and also create a readable file name from it. 
  
   @param title The title for the note. 
  */
  public void setTitle(String title) {
    titleValue.set(title);
		if (title == null) {
			fileName = "";
		}
		else
		if (title.length() == 0) {
			fileName = "";
		} else {
			fileName = StringUtils.makeReadableFileName (title);
		}
  }
  
  public boolean equalsTitle (String title2) {

    return titleValue.toString().equals (title2.trim());
  }
  
  public boolean hasTitle() {
    return titleValue.hasData();
  }
  
  public String getTitle() {
    return titleValue.toString();
  }
  
	/**
	 Return the file name in which this item should be stored.
	
	 @return The file name to be used, without a file extension. This is a 
   readable file name derived from the Note's title.
	 */
	public String getFileName() {
    return fileName;
	}
  
  public void setAuthor(String author) {

    authorValue.set(author);
    if (! authorAdded) {
      storeField (recDef, authorField);
      authorAdded = true;
    }
  }
  
  public void setAuthor(Author author) {
    authorValue.set(author.toString());
    if (! authorAdded) {
      storeField (recDef, authorField);
      authorAdded = true;
    }
  }
  
  public boolean hasAuthor() {
    return (authorAdded && authorValue != null && authorValue.hasData());
  }
  
  public Author getAuthor() {
    return authorValue;
  }
  
  public String getAuthorAsString() {
    if (authorAdded && authorValue != null) {
      return authorValue.toString();
    } else {
      return "";
    }
  }
  
  public void setType(String type) {
    typeValue.set(type);
    if (! typeAdded) {
      storeField (recDef, typeField);
      typeAdded = true;
    }
  }
  
  public boolean hasType() {
    return (typeAdded && typeValue != null && typeValue.hasData());
  }
  
  public String getType() {
    return typeValue.toString();
  }
  
  public String getTypeAsString() {
    return typeValue.toString();
  }
  
  public void setSeq(String seq) {
    seqValue.set(seq);
    if (! seqAdded) {
      storeField (recDef, seqField);
      seqAdded = true;
    }
  }
  
  public boolean hasSeq() {
    return (seqAdded && seqValue != null && seqValue.hasData());
  }
  
  public String getSeq() {
    return seqValue.toString();
  }
  
  public String getSeqAsString() {
    return seqValue.toString();
  }
  
  public DataValueSeq getSeqValue() {
    return seqValue;
  }
  
  /**
   Increment the sequence value (whether numeric or alphabetic) by one.
  
   @param onLeft Are we incrementing the integer (to the left of the decimal, 
                 if any?). 
  */
  public void incrementSeq(boolean onLeft) {
    if (! this.hasSeq()) {
      this.setSeq("0");
    }
    seqValue.increment(onLeft);
  }
  
  public void setStatus(String status) {
    statusValue.set(status);
    if (! statusAdded) {
      storeField (recDef, statusField);
      statusAdded = true;
    }
  }
  
  public void setStatus(ItemStatus status) {
    statusValue.set(status.toString());
    if (! statusAdded) {
      storeField (recDef, statusField);
      statusAdded = true;
    }
  }
  
  public void setStatus(int status) {
    statusValue.setStatus(status);
    if (! statusAdded) {
      storeField (recDef, statusField);
      statusAdded = true;
    }
  }
  
  public boolean hasStatus() {
    return (statusAdded && statusValue != null && statusValue.hasData());
  }
  
  public ItemStatus getStatus() {
    return statusValue;
  }
  
  public String getStatusAsString() {
    if (hasStatus()) {
      return statusValue.toString();
    } else {
      return "";
    }
  }
  
  /**
   Return an "X" to mark items that are done. 
   
   @return an X if item is completed or canceled, or a space if not done. 
  */
  public String getDone() {
    if (hasStatus()) {
      return statusValue.getDone();
    } else {
      return ItemStatus.NOT_DONE_YET;
    }
  }
  
  public void setDate(Date date) {
    setDate(dateFormat.format(date));
  }
  
  public void setDate(String date) {

    dateValue.set(date);
    if (! dateAdded) {
      storeField (recDef, dateField);
      dateAdded = true;
    }
  }
  
  public void setDate(StringDate date) {
    dateValue.set(date.toString());
    if (! dateAdded) {
      storeField (recDef, dateField);
      dateAdded = true;
    }
  }
  
  public boolean hasDate() {
    return (dateAdded && dateValue != null && dateValue.hasData());
  }
  
  public StringDate getDate() {
    return dateValue;
  }
  
  public String getDateAsString() {
    if (dateAdded && dateValue != null) {
      return dateValue.toString();
    } else {
      return "";
    }
  }
  
  public String getDateYMD() {
    if (dateAdded && dateValue != null && dateValue.hasData()) {
      return dateValue.getYMD();
    } else {
      return "          ";
    }
  }
  
  /**
   Get a date to be used for sorting, with blank dates sorting after 
   non-blank dates. 
  
   @return A date string to be used for sorting. 
  */
  public String getDateYMDforSort() {
    if (dateAdded && dateValue != null && dateValue.hasData()) {
      return dateValue.getYMD();
    } else {
      return "9999-12-31";
    }
  }
  
  /**
   Return date in dd MMM yyyy format.
  
   @return date in dd MMM yyyy format. 
  */
  public String getDateCommon() {
    if (dateAdded && dateValue != null && dateValue.hasData()) {
      return dateValue.getCommon();
    } else {
      return "";
    }
  }
  
  public String getReadableDate() {
    if (dateAdded && dateValue != null && dateValue.hasData()) {
      return dateValue.getReadable();
    } else {
      return "";
    }
  }
  
  public void setRecurs(String recurs) {

    recursValue.set(recurs);
    if (! recursAdded) {
      storeField (recDef, recursField);
      recursAdded = true;
    }
  }
  
  public void setRecurs(RecursValue recurs) {
    
    recursValue.set(recurs.toString());
    if (! recursAdded) {
      storeField (recDef, recursField);
      recursAdded = true;
    }
 
  }
  
  public boolean hasRecurs() {
    return (recursAdded && recursValue != null && recursValue.hasData());
  }
  
  public RecursValue getRecurs() {
    return recursValue;
  }
  
  public String getRecursAsString() {
    if (recursAdded && recursValue != null) {
      return recursValue.toString();
    } else {
      return "";
    }
  }
  
  public void setTags(String tags) {
    tagsValue.set(tags);
    if (! tagsAdded) {
      storeField (recDef, tagsField);
      // addField(tagsField);
      tagsAdded = true;
    }
  }
  
  /**
   Do we have any tags?
  
   @return True if we have a tags field, and the field is not null, 
           and the tags aren't blank; false otherwise. 
  */
  public boolean hasTags() {
    if (tagsAdded && tagsValue != null) {
      return (! tagsValue.areBlank());
    } else {
      return false;
    }
  }
  
  public Tags getTags() {
    return tagsValue;
  }
  
  public String getTagsAsString() {
    if (tagsAdded && tagsValue != null) {
      return tagsValue.toString();
    } else {
      return "";
    }
  }
  
  public void setTagsNode (TreeItem<TagsNodeValue> tagsNode) {
    this.tagsNode = tagsNode;
  }

  public TreeItem<TagsNodeValue> getTagsNode () {
    return tagsNode;
  }
  
  public void flattenTags () {
    tagsValue.flatten();
  }

  public void lowerCaseTags () {
    tagsValue.makeLowerCase();
  }
  
  public void setRating(String rating) {
    ratingValue.set(rating);
    if (! ratingAdded) {
      storeField (recDef, ratingField);
      ratingAdded = true;
    }
  }
  
  public void setIndex(String index) {
    indexValue.append(index);
    if (! indexAdded) {
      storeField (recDef, indexField);
      indexAdded = true;
    }
  }
  
  public boolean hasIndex() {
    return (indexAdded && indexValue != null && indexValue.hasData());
  }
  
  public IndexPageValue getIndex() {
    return indexValue;
  } 
  
  public String getIndexAsString() {
    if (indexAdded && indexValue != null) {
      return indexValue.toString();
    } else {
      return "";
    }
  }
  
  public void setLink(File file) {
    setLink(StringUtils.fileToLink(file));
  }
  
  public void setLink(String link) {

    linkValue.set(link);
    if (! linkAdded) {
      storeField (recDef, linkField);
      // addField(linkField);
      linkAdded = true;
    }
  }
  
  public void setLink(Link link) {
    linkValue.set(link.toString());
    if (! linkAdded) {
      storeField (recDef, linkField);
      // addField(linkField);
      linkAdded = true;
    }
  }
  
  public boolean hasLink() {
    return (linkAdded && linkValue != null && linkValue.hasLink());
  }
  
  public boolean blankLink () {
    return ((! linkAdded) || linkValue == null || (linkValue.blankLink()));
  }
  
  public Link getLink() {
    return linkValue;
  }
  
  public File getLinkAsFile() {
    if (linkAdded && linkValue != null) {
      return linkValue.getLinkAsFile();
    } else {
      return null;
    }
  }
  
  public String getLinkAsString() {
    if (linkAdded && linkValue != null) {
      return linkValue.getURLasString();
    } else {
      return "";
    }
  }
  
  public boolean isLinkToMacApp() {
    if (! linkAdded) {
      return false;
    }
    else
    if (linkValue == null) {
      return false;
    } else {
      String link = linkValue.getURLasString();
      if (link.endsWith(".app")) {
        return true;
      }
      else
      if (link.endsWith(".app/")) {
        return true;
      } else {
        return false;
      }
    }
  }
  
  public boolean equalsTags (String tags2) {
    return tagsValue.toString().equals (tags2.trim());
  }
  
  public String getURLasString () {
    if (linkAdded && linkValue != null) {
      return linkValue.getURLasString();
    } else {
      return "";
    }
  }
  
  public void setCode(String code) {
    codeValue.set(code);
    if (! codeAdded) {
      storeField (recDef, codeField);
      codeAdded = true;
    }
  }
  
  public void appendLineToCode(String line) {
    codeValue.appendLine(line);
    if (! codeAdded) {
      storeField (recDef, codeField);
      codeAdded = true;
    }
  }
  
  public boolean hasCode() {
    if (codeAdded && codeValue != null) {
      return (codeValue.toString().length() > 0);
    } else {
      return false;
    }
  }
  
  public String getCode() {
    if (hasCode()) {
      return codeValue.toString();
    } else {
      return "";
    }
  }
  
  public DataValueStringBuilder getCodeAsDataValue() {
    return codeValue;
  }
  
  public void setTeaser(String teaser) {
    teaserValue.set(teaser);
    if (! teaserAdded) {
      storeField (recDef, teaserField);
      teaserAdded = true;
    }
  }
  
  public void appendLineToTeaser(String line) {
    teaserValue.appendLine(line);
    if (! teaserAdded) {
      storeField (recDef, teaserField);
      teaserAdded = true;
    }
  }
  
  public boolean hasTeaser() {
    if (teaserAdded && teaserValue != null) {
      return (teaserValue.toString().length() > 0);
    } else {
      return false;
    }
  }
  
  public String getTeaser() {
    if (hasTeaser()) {
      return teaserValue.toString();
    } else {
      return "";
    }
  }
  
  public DataValueStringBuilder getTeaserAsDataValue() {
    return teaserValue;
  }
  
  public void setBody(String body) {
    bodyValue.set(body);
    if (! bodyAdded) {
      storeField (recDef, bodyField);
      // addField(bodyField);
      bodyAdded = true;
    }
  }
  
  public void appendLineToBody(String line) {
    bodyValue.appendLine(line);
    if (! bodyAdded) {
      storeField (recDef, bodyField);
      // addField(bodyField);
      bodyAdded = true;
    }
  }
  
  public boolean hasBody() {
    if (bodyAdded && bodyValue != null) {
      return (bodyValue.toString().length() > 0);
    } else {
      return false;
    }
  }
  
  public String getBody() {
    if (bodyAdded && bodyValue != null) {
      return bodyValue.toString();
    } else {
      return "";
    }
  }
  
  public DataValueStringBuilder getBodyAsDataValue() {
    return bodyValue;
  }
  
  /**
   Set the disk location at which this item is stored.
 
   @param diskLocation The path to the disk location at which this item
                       is stored.
  */
  public void setDiskLocation (String diskLocation) {
    this.diskLocation = new FileName(diskLocation);
  }
 
  /**
   Set the disk location at which this item is stored.
 
   @param diskLocationFile The disk location at which this item is stored.
  */
  public void setDiskLocation (File diskLocationFile) {
    try {
      this.diskLocation = new FileName(diskLocationFile.getCanonicalPath());
    } catch (java.io.IOException e) {
      this.diskLocation = new FileName(diskLocationFile.getAbsolutePath());
    }
  }
  
  public void extractDiskLocationInfo(String homePath) {
    
    // Let's populate some fields based on the file name 
    String fileNameBase;
    String  fileExt = "";
    String localPath;
    int depth = 0;
    StringBuilder tagsPath = new StringBuilder();
    StringBuilder pathToTop = new StringBuilder();
    ArrayList<String> parents = new ArrayList();
    StringBuilder breadcrumbs = new StringBuilder();
    
    // Get location of file extension and file name
    int period = diskLocation.length();
    int slash = -1;
    int i = diskLocation.length() - 1;
    while (i >= 0 && slash < 0) {
      if (diskLocation.charAt(i) == '.'
          && period == diskLocation.length()) {
        period = i;
      } 
      else
      if (diskLocation.charAt(i) == '/' ||
          diskLocation.charAt(i) == '\\') {
        slash = i;
      }
      i--;
    }
    int localPathStart = 0;
    if (diskLocation.startsWith(homePath)) {
      localPathStart = homePath.length();
    }
    if (diskLocation.charAt(localPathStart) == '/' ||
        diskLocation.charAt(localPathStart) == '\\') {
      localPathStart++;
    }
    // Let's get as much info as we can from the file name or URL
    if (slash > localPathStart) {
      localPath = diskLocation.substring(localPathStart, slash) + '/';
    } else {
      localPath = "";
    }
    
    int lastSlash = 0;
    if (lastSlash < localPath.length()
        && (localPath.charAt(0) == '/'
          || localPath.charAt(0) == '\\')) {
      lastSlash++;
    }
    while (lastSlash < localPath.length()) {
      depth++;
      tagsPath.append(UP_ONE_FOLDER);
      pathToTop.append(UP_ONE_FOLDER);
      int nextSlash = localPath.indexOf("/", lastSlash);
      if (nextSlash < 0) {
        nextSlash = localPath.indexOf("\\", lastSlash);
      }
      if (nextSlash < 0) {
        nextSlash = localPath.length();
      }
      parents.add(localPath.substring(lastSlash, nextSlash));
      lastSlash = nextSlash;
      lastSlash++;
    }
    tagsPath.append("tags/");
    
    fileName = diskLocation.substring(slash + 1);
    fileNameBase = diskLocation.substring(slash + 1, period);
    fileExt = diskLocation.substring(period + 1);
    
    // Now let's build breadcrumbs to higher-level index pages
    int parentIndex = 0;
    int parentStop = parents.size() - 1;
    while (parentIndex < parentStop) {
      addBreadcrumb (breadcrumbs, parents, parents.size() - parentIndex, parentIndex);
      parentIndex++;
    }
    if (! fileNameBase.equalsIgnoreCase("index")) {
      addBreadcrumb (breadcrumbs, parents, 0, parentIndex);
    } 
    
    if (! hasTitle()) {
      setTitle(diskLocation.substring(slash + 1, period));
    }
    
    File diskLocationFile = diskLocation.getFile();
    if (diskLocationFile != null && diskLocationFile.exists()) {
      lastModDate = new Date (diskLocationFile.lastModified());
      lastModDateStr = dateFormat.format(lastModDate);
    }
    
  }
  
  /**
   Add another bread crumb level. 
  
   @param breadcrumbs The starting bread crumbs, to which the latest will be added. 
   @param levels      The number of levels upwards to point to. 
   @param parentIndex The parent to point to.
  
   @return The bread crumbs after the latest addition. 
  */
  private StringBuilder addBreadcrumb (
      StringBuilder breadcrumbs, 
      ArrayList<String> parents,
      int levels, 
      int parentIndex) {
    
    if (breadcrumbs.length() > 0) {
      breadcrumbs.append(" &gt; ");
    }
    breadcrumbs.append("<a href=\"");
    for (int i = 0; i < levels; i++) {
      breadcrumbs.append(UP_ONE_FOLDER);
    }
    breadcrumbs.append("index.html");
    breadcrumbs.append("\">");
    if (parentIndex < 0 || parentIndex >= parents.size()) {
      breadcrumbs.append("Home");
    } else {
      breadcrumbs.append(
          StringUtils.wordDemarcation(parents.get(parentIndex), " ", 1, 1, -1));
    }
    breadcrumbs.append("</a>");
    return breadcrumbs;
  }
  
  /**
   Let us know if the current file name at which the note is stored is
   inconsistent with the note's title. 
  
   @return True if the note has a title and a disk location, but they
           are not consistent. 
  */
  public boolean hasInconsistentDiskLocation() {
    return (hasTitle()
        && hasDiskLocation()
        && (! getFileName().equalsIgnoreCase(getDiskLocationBase())));
  }
 
  /**
   Indicate whether the item has a disk location.
 
   @return True if we've got a disk location, false otherwise.
  */
  public boolean hasDiskLocation() {
    return (diskLocation != null
        && diskLocation.length() > 0);
  }
 
  /**
   Return the disk location at which this item is stored.
 
   @return The disk location at which this item is stored.
  */
  public String getDiskLocation () {
    if (diskLocation == null) {
      return "";
    } else {
      return diskLocation.toString();
    }
  }
  
  public String getDiskLocationBase() {
    if (diskLocation == null) {
      return "";
    } else {
      return diskLocation.getBase();
    }
  }
  
  /**
   Return the file extension of the disk location, if one has been identified. 
  
   @return The file extension of the disk location, if one has been identified.
           If a file extension is not available, then return null.
  */
  public String getDiskFileExt() {
    String ext = "";
    if (diskLocation != null) {
      ext = diskLocation.getExt();
    }
    if (ext != null && ext.length() > 0) {
      return ext;
    } else {
      return null;
    }
  }
  
  public void setLastModDateStandard (String date) {
    setLastModDate (NoteParms.STANDARD_FORMAT, date);
  }
    
  public void setLastModDateYMD (String date) {
    setLastModDate (NoteParms.YMD_FORMAT, date);
  }
  
  /**
     Sets the last mod date for this item.
 
     @param  fmt  A DateFormat instance to be used to parse the following string.
     @param  date String representation of a date.
   */
  public void setLastModDate (DateFormat fmt, String date) {
    
    try {
      setLastModDate (fmt.parse (date));
    } catch (ParseException e) {
      System.out.println ("Note.setLastModDate to " + date + " with " + fmt
          + " -- Parse Exception");
    }

  } // end method
  
  /**
    Sets the last mod date to today's date. 
   */
  public void setLastModDateToday () {
    setLastModDate (new GregorianCalendar().getTime());
  }
  
  /**
     Sets the due date for this item.
 
     @param  date Date representation of a date.
   */
  public void setLastModDate (Date date) {
    
    lastModDate = date;

  } // end method
  
  /**
     Gets the due date for this item, formatted as a string.
 
     @return  String representation of a date.
     @param   fmt  A DateFormat instance to be used to format the date as a string.

   */
  public String getLastModDate (DateFormat fmt) {
    
    return fmt.format (lastModDate);

  } // end method
  
  /**
     Gets the due date for this item, formatted as a string 
     in yyyy/mm/dd format.
 
     @return  String representation of a date in yyyy/mm/dd format.
   */
  public String getLastModDateYMD () {
    
    return NoteParms.YMD_FORMAT.format (lastModDate);

  } // end method
  
  public String getLastModDateStandard () {
    
    return NoteParms.STANDARD_FORMAT.format (lastModDate);
  }
  
  /**
     Gets the due date for this item.
 
     @return  date Date representation of a date.
   */
  public Date getLastModDate () {
    
    return lastModDate;

  } // end method
  
  public void setSynced(boolean synced) {
    this.synced = synced;
  }
  
  public boolean isSynced() {
    return synced;
  }
  
  public String toString() {
    return titleValue.toString();
  }
  
  /**
   Set the unique ID of this note within its collection. This is the position
   of this note within the basic list contained in NoteCollectionList. 
  
   @param collectionID This note's unique ID within its collection. 
  */
  public void setCollectionID(int collectionID) {
    this.collectionID = collectionID;
  }
  
  /**
   Obtain the unique ID of this note within its collection. This is the position
   of this note within the basic list contained in NoteCollectionList. 
  
   @return This note's unique ID within its collection.
  */
  public int getCollectionID() {
    return collectionID;
  }
  
  /**
   Has this note been marked for deletion?
   
   @param deleted Indicated whether this note has been flagged for 
                  deletion. 
  */
  public void setDeleted(boolean deleted) {
    this.deleted = deleted;
  }
  
  /**
   Has this note been marked for deletion?
  
   @return True if flagged for deletion, false if still active. 
  */
  public boolean isDeleted() {
    return deleted;
  }

}
