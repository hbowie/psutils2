/*
 * Copyright 2012 - 2015 Herb Bowie
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

package com.powersurgepub.psutils2.clubplanner;

	import com.powersurgepub.psutils2.list.*;
	import com.powersurgepub.psutils2.records.*;
	import com.powersurgepub.psutils2.strings.*;
	import com.powersurgepub.psutils2.tags.*;

  import java.io.*;
  import java.util.*;

  import javafx.scene.control.*;
 
/**
 A single event, or other item to be tracked by the club. <p>
 
    This item class definition generated by PSTextMerge using: <p>
 
     template:  item-class.java <p>
     data file: /Users/hbowie/Java/projects/nbproj/clubplanner/javagen/fields.xls

 @author Herb Bowie
 */
public class EventNote
    implements
      // Generated by PSTextMerge using template taggable-implements.java.
      // No taggable fields
            Comparable,
            PSItem
			 {

  private static final RecordDefinition recDef;


  /*
   Following code generated by PSTextMerge using:
 
     template:  variable-definitions.java
     data file: /Users/hbowie/Java/projects/nbproj/clubplanner/javagen/fields.xls
   */
 

  /**
   Date on which note was made.
   */
  private String noteFor = null;
 
  public static final String NOTE_FOR_FIELD_NAME = "Note For";
 
  public static final String NOTE_FOR_COLUMN_NAME = "Note For";
 
  public static final String NOTE_FOR_COMMON_NAME = "notefor";
 
  public static final int NOTE_FOR_COLUMN_INDEX = 0;
 
  public static final int NOTE_FOR_COLUMN_WIDTH = 10;
 

  /**
   A full or partial date in year, month, day sequence.
   */
  private String noteForYmd = null;
 
  public static final String NOTE_FOR_YMD_FIELD_NAME = "Note For YMD";
 
  public static final String NOTE_FOR_YMD_COLUMN_NAME = "Note YMD";
 
  public static final String NOTE_FOR_YMD_COMMON_NAME = "noteforymd";
 
  public static final int NOTE_FOR_YMD_COLUMN_INDEX = 1;
 
  public static final int NOTE_FOR_YMD_COLUMN_WIDTH = 8;
 

  /**
   Person from whom note came.
   */
  private String noteFrom = null;
 
  public static final String NOTE_FROM_FIELD_NAME = "Note From";
 
  public static final String NOTE_FROM_COLUMN_NAME = "Note From";
 
  public static final String NOTE_FROM_COMMON_NAME = "notefrom";
 
  public static final int NOTE_FROM_COLUMN_INDEX = 2;
 
  public static final int NOTE_FROM_COLUMN_WIDTH = 40;
 

  /**
   Medium by which note was communicated.
   */
  private String noteVia = null;
 
  public static final String NOTE_VIA_FIELD_NAME = "Note Via";
 
  public static final String NOTE_VIA_COLUMN_NAME = "Note Via";
 
  public static final String NOTE_VIA_COMMON_NAME = "notevia";
 
  public static final int NOTE_VIA_COLUMN_INDEX = 3;
 
  public static final int NOTE_VIA_COLUMN_WIDTH = 10;
 

  /**
   Note itself.
   */
  private String note = null;
 
  public static final String NOTE_FIELD_NAME = "Note";
 
  public static final String NOTE_COLUMN_NAME = "Note";
 
  public static final String NOTE_COMMON_NAME = "note";
 
  public static final int NOTE_COLUMN_INDEX = 4;
 
  public static final int NOTE_COLUMN_WIDTH = 40;
 

  /**
   Note reformatted as HTML.
   */
  private String noteAsHtml = null;
 
  public static final String NOTE_AS_HTML_FIELD_NAME = "Note as HTML";
 
  public static final String NOTE_AS_HTML_COLUMN_NAME = "Note as HTML";
 
  public static final String NOTE_AS_HTML_COMMON_NAME = "noteashtml";
 
  public static final int NOTE_AS_HTML_COLUMN_INDEX = 5;
 
  public static final int NOTE_AS_HTML_COLUMN_WIDTH = 50;
 

  public static final int COLUMN_COUNT = 6;


  private boolean modified = false;
 
  private String  diskLocation = "";
 
  private Comparator comparator = new EventNoteDefaultComparator();

  /**
   Static initializer.
   */
  static {
    DataDictionary dict = new DataDictionary();
    recDef = new RecordDefinition (dict);
    for (int i = 0; i < COLUMN_COUNT; i++) {
      recDef.addColumn (getColumnName(i));
    }
  }

  /**
   A constructor without any arguments.
   */
  public EventNote() {

  }
 
  /**
   Get the comparator to be used;
   */
  public Comparator getComparator() {
    return comparator;
  }
 
  /**
   Set the comparator to be used.
   */
  public void setComparator (Comparator comparator) {
    this.comparator = comparator;
  }
 
  /**
   Determine if this item has a key that is equal to the passed
   item.

   @param  obj2        The second object to be compared to this one.
   @param  comparator  The comparator to be used to make the comparison.
   @return True if the keys are equal.
   */
  public boolean equals (Object obj2, Comparator comparator) {
    return (this.compareTo (obj2, comparator) == 0);
  }
 
  /**
   Determine if this item has a key that is equal to the passed
   item.

   @param  obj2  The second object to be compared to this one.
   @return True if the keys are equal.
   */
  public boolean equals (Object obj2) {
    return (this.compareTo (obj2) == 0);
  }
 
  /**
   Compare this ClubEvent object to another, using the key field(s) for comparison.
 
   @param The second object to compare to this one.
 
   @return A number less than zero if this object is less than the second,
           a number greater than zero if this object is greater than the second,
           or zero if the two item's keys are equal.
   */
  public int compareTo (Object obj2, Comparator comparator) {
    if (comparator == null) {
      return -1;
    }
    return comparator.compare (this, obj2);
  }
 
  /**
   Compare this ClubEvent object to another, using the key field(s) for comparison.
 
   @param The second object to compare to this one.
 
   @return A number less than zero if this object is less than the second,
           a number greater than zero if this object is greater than the second,
           or zero if the two item's keys are equal.
   */
  public int compareTo (Object obj2) {
    if (comparator == null) {
      return -1;
    }
    return comparator.compare (this, obj2);
  }
 
  public void resetModified() {
    setModified (false);
  }
 
  public void setModified (boolean modified) {
    this.modified = modified;
  }
 
  public boolean isModified() {
    return modified;
  }
 
  /**
   Set the disk location at which this item is stored.
 
   @param diskLocation The path to the disk location at which this item
                       is stored.
  */
  public void setDiskLocation (String diskLocation) {
    this.diskLocation = diskLocation;
  }
 
  /**
   Set the disk location at which this item is stored.
 
   @param diskLocationFile The disk location at which this item is stored.
  */
  public void setDiskLocation (File diskLocationFile) {
    try {
      this.diskLocation = diskLocationFile.getCanonicalPath();
    } catch (java.io.IOException e) {
      this.diskLocation = diskLocationFile.getAbsolutePath();
    }
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
    return diskLocation;
  }
 
  /**
   Find a common name (no punctuation, all lower-case) that starts with
   the passed string, if one exists.
 
   @param possibleFieldName The potential field name we're looking for. This
                            will be converted to a common name before it's
                            compared to the common forms of the field names.
 
   @return The index pointing to the first matching common name that was found,
           or -1 if no match was found.
  */
  public static int commonNameStartsWith (String possibleFieldName) {
    int i = 0;
    boolean fieldMatch = false;
    String possibleCommonName = StringUtils.commonName (possibleFieldName);
    while (i < COLUMN_COUNT && (! fieldMatch)) {
      if (getCommonName(i).startsWith(possibleCommonName)) {
        fieldMatch = true;
      } else {
        i++;
      }
    } // end while looking for field name match
    if (fieldMatch) {
      return i;
    } else {
      return -1;
    }
  }

  /**
   Return a record definition for the ClubEvent.
 
   @return A record definition using a new dictionary.
  */
  public static RecordDefinition getRecDef() {
    return recDef;
  }
 
  /**
   Return a standard data rec using the variables belonging to this object.
 
   @return A generic data record.
  */
  public DataRecord getDataRec() {
    DataRecord dataRec = new DataRecord();
    for (int i = 0; i < COLUMN_COUNT; i++) {
      Object columnValue = getColumnValue(i);
      String columnValueStr = "";
      if (columnValue != null) {
        columnValueStr = columnValue.toString();
      }
      DataField nextField = new DataField(recDef, i, columnValueStr);
      nextField.setDataRaw(columnValueStr);
      int dataRecFieldNumber = dataRec.addField(nextField);
      // int dataRecFieldNumber = dataRec.addField(recDef, columnValueStr);
    }
    return dataRec;
  }
 

  /*
   Following code generated by PSTextMerge using:
 
     template:  duplicate.java
     data file: /Users/hbowie/Java/projects/nbproj/clubplanner/javagen/fields.xls
   */
 
  /**
     Duplicates this item, making a deep copy.
   */
  public EventNote duplicate () {
    EventNote newEventNote = new EventNote();
		String noteForStr = new String(getNoteForAsString());
		newEventNote.setNoteFor(noteForStr);
		String noteFromStr = new String(getNoteFromAsString());
		newEventNote.setNoteFrom(noteFromStr);
		String noteViaStr = new String(getNoteViaAsString());
		newEventNote.setNoteVia(noteViaStr);
		String noteStr = new String(getNoteAsString());
		newEventNote.setNote(noteStr);
		return newEventNote;
  }

  /*
   Following code generated by PSTextMerge using:
 
     template:  haskey-method.java
     data file: /Users/hbowie/Java/projects/nbproj/clubplanner/javagen/fields.xls
   */
 
  /**
   Determine if this item has a key.

   @return True if this item has a key.
   */
  public boolean hasKey () {

    boolean hasKey = false;

		if (getNoteForYmd() == null) {
			// No key here
		}
		else
		if (getNoteForYmd().toString() == "") {
			// No key here
		} else {
			hasKey = true;
		}

		if (getNoteFrom() == null) {
			// No key here
		}
		else
		if (getNoteFrom().toString() == "") {
			// No key here
		} else {
			hasKey = true;
		}

		if (getNoteVia() == null) {
			// No key here
		}
		else
		if (getNoteVia().toString() == "") {
			// No key here
		} else {
			hasKey = true;
		}

    return hasKey;
  }
 
  /**
    Check for a search string within the given Club Event item.
 
    This method generated by PSTextMerge using template find.java.
 
    @param findLower   The search string in all lower case.
    @param findUpper   The search string in all upper case.
 
    @return True if this item contains the search string
            in one of its searchable fields.

   */
  public boolean find (String findLower, String findUpper) {

    boolean found = false;
    int fieldStart = -1;

    return found;
  }

  /**
    Return a string value representing the given item.
 
    This method generated by PSTextMerge using template toString.java.
 
    @return The string by which this item shall be known.

   */
  public String toString() {

    StringBuilder str = new StringBuilder();

    return str.toString();
  }

  /*
   Following code generated by PSTextMerge using:
 
     template:  merge.java
     data file: /Users/hbowie/Java/projects/nbproj/clubplanner/javagen/fields.xls
   */
 
  /**
     Merges the contents of a second item into this one.
   */
  public void merge (EventNote eventNote2) {
    Object obj2;
    String str2;
    obj2 = eventNote2.getNoteFor();
    if (obj2 == null) {
      // No value available -- leave current value as-is
    } else {
      str2 = obj2.toString();
      if (str2.equals ("")) {
        // No value available -- leave current value as-is
      } else {
        setNoteFor ((String)obj2);
      }
    }
    obj2 = eventNote2.getNoteForYmd();
    if (obj2 == null) {
      // No value available -- leave current value as-is
    } else {
      str2 = obj2.toString();
      if (str2.equals ("")) {
        // No value available -- leave current value as-is
      } else {
        setNoteForYmd ((String)obj2);
      }
    }
    obj2 = eventNote2.getNoteFrom();
    if (obj2 == null) {
      // No value available -- leave current value as-is
    } else {
      str2 = obj2.toString();
      if (str2.equals ("")) {
        // No value available -- leave current value as-is
      } else {
        setNoteFrom ((String)obj2);
      }
    }
    obj2 = eventNote2.getNoteVia();
    if (obj2 == null) {
      // No value available -- leave current value as-is
    } else {
      str2 = obj2.toString();
      if (str2.equals ("")) {
        // No value available -- leave current value as-is
      } else {
        setNoteVia ((String)obj2);
      }
    }
    obj2 = eventNote2.getNote();
    if (obj2 == null) {
      // No value available -- leave current value as-is
    } else {
      str2 = obj2.toString();
      if (str2.equals ("")) {
        // No value available -- leave current value as-is
      } else {
        setNote ((String)obj2);
      }
    }
    obj2 = eventNote2.getNoteAsHtml();
    if (obj2 == null) {
      // No value available -- leave current value as-is
    } else {
      str2 = obj2.toString();
      if (str2.equals ("")) {
        // No value available -- leave current value as-is
      } else {
        setNoteAsHtml ((String)obj2);
      }
    }
  }

  /*
   Following code generated by PSTextMerge using:
 
     template:  setColumnValue.java
     data file: /Users/hbowie/Java/projects/nbproj/clubplanner/javagen/fields.xls
   */
 
 
 /**
  Sets the column value indicated by the given column index.
 
  @param columnIndex An integer indicating the desired column whose value is to
                     be set.
  @param columnValue A string representing the value to be set.
  */
  public void setColumnValue (int columnIndex, String columnValue) {
    switch (columnIndex) {
      case NOTE_FOR_COLUMN_INDEX:
          setNoteFor (columnValue);
          break;
      case NOTE_FOR_YMD_COLUMN_INDEX:
          setNoteForYmd (columnValue);
          break;
      case NOTE_FROM_COLUMN_INDEX:
          setNoteFrom (columnValue);
          break;
      case NOTE_VIA_COLUMN_INDEX:
          setNoteVia (columnValue);
          break;
      case NOTE_COLUMN_INDEX:
          setNote (columnValue);
          break;
      case NOTE_AS_HTML_COLUMN_INDEX:
          setNoteAsHtml (columnValue);
          break;
    }
  }

  /*
   Following code generated by PSTextMerge using:
 
     template:  getColumnValue.java
     data file: /Users/hbowie/Java/projects/nbproj/clubplanner/javagen/fields.xls
   */
 
  /**
     Returns the value at the given column index.
   */
  public Object getColumnValue (int columnIndex) {
    switch (columnIndex) {
      case NOTE_FOR_COLUMN_INDEX:
          return noteFor;
      case NOTE_FOR_YMD_COLUMN_INDEX:
          return noteForYmd;
      case NOTE_FROM_COLUMN_INDEX:
          return noteFrom;
      case NOTE_VIA_COLUMN_INDEX:
          return noteVia;
      case NOTE_COLUMN_INDEX:
          return note;
      case NOTE_AS_HTML_COLUMN_INDEX:
          return noteAsHtml;
      default: return null;
    }
  }

  /*
   Following code generated by PSTextMerge using:
 
     template:  getColumnName.java
     data file: /Users/hbowie/Java/projects/nbproj/clubplanner/javagen/fields.xls
   */
 
  /**
     Returns the field name for the given column index.
   */
  public static String getColumnName (int columnIndex) {
    switch (columnIndex) {
      case NOTE_FOR_COLUMN_INDEX:
          return NOTE_FOR_COLUMN_NAME;
      case NOTE_FOR_YMD_COLUMN_INDEX:
          return NOTE_FOR_YMD_COLUMN_NAME;
      case NOTE_FROM_COLUMN_INDEX:
          return NOTE_FROM_COLUMN_NAME;
      case NOTE_VIA_COLUMN_INDEX:
          return NOTE_VIA_COLUMN_NAME;
      case NOTE_COLUMN_INDEX:
          return NOTE_COLUMN_NAME;
      case NOTE_AS_HTML_COLUMN_INDEX:
          return NOTE_AS_HTML_COLUMN_NAME;
      default: return null;
    }
  }

  /*
   Following code generated by PSTextMerge using:
 
     template:  getCommonName.java
     data file: /Users/hbowie/Java/projects/nbproj/clubplanner/javagen/fields.xls
   */
 
  /**
     Returns the common name (all lower case, no word separators)
     for the given column index.
   */
  public static String getCommonName (int columnIndex) {
    switch (columnIndex) {
      case NOTE_FOR_COLUMN_INDEX:
          return NOTE_FOR_COMMON_NAME;
      case NOTE_FOR_YMD_COLUMN_INDEX:
          return NOTE_FOR_YMD_COMMON_NAME;
      case NOTE_FROM_COLUMN_INDEX:
          return NOTE_FROM_COMMON_NAME;
      case NOTE_VIA_COLUMN_INDEX:
          return NOTE_VIA_COMMON_NAME;
      case NOTE_COLUMN_INDEX:
          return NOTE_COMMON_NAME;
      case NOTE_AS_HTML_COLUMN_INDEX:
          return NOTE_AS_HTML_COMMON_NAME;
      default: return null;
    }
  }

  /*
   Following code generated by PSTextMerge using:
 
     template:  getColumnWidth.java
     data file: /Users/hbowie/Java/projects/nbproj/clubplanner/javagen/fields.xls
   */
 
  /**
     Returns the field name for the given column index.
   */
  public static int getColumnWidth (int columnIndex) {
    switch (columnIndex) {
      case NOTE_FOR_COLUMN_INDEX:
          return NOTE_FOR_COLUMN_WIDTH;
      case NOTE_FOR_YMD_COLUMN_INDEX:
          return NOTE_FOR_YMD_COLUMN_WIDTH;
      case NOTE_FROM_COLUMN_INDEX:
          return NOTE_FROM_COLUMN_WIDTH;
      case NOTE_VIA_COLUMN_INDEX:
          return NOTE_VIA_COLUMN_WIDTH;
      case NOTE_COLUMN_INDEX:
          return NOTE_COLUMN_WIDTH;
      case NOTE_AS_HTML_COLUMN_INDEX:
          return NOTE_AS_HTML_COLUMN_WIDTH;
      default: return 20;
    }
  }

  /*
   Following code generated by PSTextMerge using:
 
     template:  getColumnClass.java
     data file: /Users/hbowie/Java/projects/nbproj/clubplanner/javagen/fields.xls
   */
 
  /**
     Returns the class of the field at the given column index.
   */
  public static Class getColumnClass (int columnIndex) {
    switch (columnIndex) {
      case NOTE_FOR_COLUMN_INDEX:
          return String.class;
      case NOTE_FOR_YMD_COLUMN_INDEX:
          return String.class;
      case NOTE_FROM_COLUMN_INDEX:
          return String.class;
      case NOTE_VIA_COLUMN_INDEX:
          return String.class;
      case NOTE_COLUMN_INDEX:
          return String.class;
      case NOTE_AS_HTML_COLUMN_INDEX:
          return String.class;
      default: return null;
    }
  }

  /*
   Following code generated by PSTextMerge using:
 
     template:  isMarkdownFormat.java
     data file: /Users/hbowie/Java/projects/nbproj/clubplanner/javagen/fields.xls
   */
 
  /**
     Indicates whether the field at the given column index should be in Markdown format.
   */
  public static boolean isMarkdownFormat (int columnIndex) {
    switch (columnIndex) {
      case NOTE_COLUMN_INDEX:
          return true;
      default: return false;
    }
  }

  /*
   Following code generated by PSTextMerge using:
 
     template:  variable-methods.java
     data file: /Users/hbowie/Java/projects/nbproj/clubplanner/javagen/fields.xls
   */
 
 
  /**
     Sets the note for for this event note.
 
     @param  noteFor The note for for this event note.
   */
  public void setNoteFor (String noteFor) {
    this.noteFor = noteFor;
    setModified (true);
  }

  /**
    Returns the note for for this event note as a string.
 
    @return The note for for this event note as a string.
   */
  public String getNoteForAsString () {
    if (hasNoteFor()) {
      return getNoteFor().toString();
    } else {
      return "";
    }
  }

  /**
    Determines if the note for for this event note is null.
 
    @return True if the note for for this event note is not null.
   */
  public boolean hasNoteFor () {
    return (noteFor != null);
  }

  /**
    Determines if the note for for this event note
    is null or is empty.
 
    @return True if the note for for this event note
    is not null and not empty.
   */
  public boolean hasNoteForWithData () {
    return (noteFor != null && noteFor.length() > 0);
  }

  /**
    Returns the note for for this event note.
 
    @return The note for for this event note.
   */
  public String getNoteFor () {
    return noteFor;
  }
 
  /**
     Sets the note for ymd for this event note.
 
     @param  noteForYmd The note for ymd for this event note.
   */
  public void setNoteForYmd (String noteForYmd) {
    this.noteForYmd = noteForYmd;
    setModified (true);
  }

  /**
    Returns the note for ymd for this event note as a string.
 
    @return The note for ymd for this event note as a string.
   */
  public String getNoteForYmdAsString () {
    if (hasNoteForYmd()) {
      return getNoteForYmd().toString();
    } else {
      return "";
    }
  }

  /**
    Determines if the note for ymd for this event note is null.
 
    @return True if the note for ymd for this event note is not null.
   */
  public boolean hasNoteForYmd () {
    return (noteForYmd != null);
  }

  /**
    Determines if the note for ymd for this event note
    is null or is empty.
 
    @return True if the note for ymd for this event note
    is not null and not empty.
   */
  public boolean hasNoteForYmdWithData () {
    return (noteForYmd != null && noteForYmd.length() > 0);
  }

  /**
    Returns the note for ymd for this event note.
 
    @return The note for ymd for this event note.
   */
  public String getNoteForYmd () {
    return noteForYmd;
  }
 
  /**
     Sets the note from for this event note.
 
     @param  noteFrom The note from for this event note.
   */
  public void setNoteFrom (String noteFrom) {
    this.noteFrom = noteFrom;
    setModified (true);
  }

  /**
    Returns the note from for this event note as a string.
 
    @return The note from for this event note as a string.
   */
  public String getNoteFromAsString () {
    if (hasNoteFrom()) {
      return getNoteFrom().toString();
    } else {
      return "";
    }
  }

  /**
    Determines if the note from for this event note is null.
 
    @return True if the note from for this event note is not null.
   */
  public boolean hasNoteFrom () {
    return (noteFrom != null);
  }

  /**
    Determines if the note from for this event note
    is null or is empty.
 
    @return True if the note from for this event note
    is not null and not empty.
   */
  public boolean hasNoteFromWithData () {
    return (noteFrom != null && noteFrom.length() > 0);
  }

  /**
    Returns the note from for this event note.
 
    @return The note from for this event note.
   */
  public String getNoteFrom () {
    return noteFrom;
  }
 
  /**
     Sets the note via for this event note.
 
     @param  noteVia The note via for this event note.
   */
  public void setNoteVia (String noteVia) {
    this.noteVia = noteVia;
    setModified (true);
  }

  /**
    Returns the note via for this event note as a string.
 
    @return The note via for this event note as a string.
   */
  public String getNoteViaAsString () {
    if (hasNoteVia()) {
      return getNoteVia().toString();
    } else {
      return "";
    }
  }

  /**
    Determines if the note via for this event note is null.
 
    @return True if the note via for this event note is not null.
   */
  public boolean hasNoteVia () {
    return (noteVia != null);
  }

  /**
    Determines if the note via for this event note
    is null or is empty.
 
    @return True if the note via for this event note
    is not null and not empty.
   */
  public boolean hasNoteViaWithData () {
    return (noteVia != null && noteVia.length() > 0);
  }

  /**
    Returns the note via for this event note.
 
    @return The note via for this event note.
   */
  public String getNoteVia () {
    return noteVia;
  }
 
  /**
     Sets the note for this event note.
 
     @param  note The note for this event note.
   */
  public void setNote (String note) {
    this.note = note;
    setModified (true);
  }

  /**
    Returns the note for this event note as a string.
 
    @return The note for this event note as a string.
   */
  public String getNoteAsString () {
    if (hasNote()) {
      return getNote().toString();
    } else {
      return "";
    }
  }

  /**
    Determines if the note for this event note is null.
 
    @return True if the note for this event note is not null.
   */
  public boolean hasNote () {
    return (note != null);
  }

  /**
    Determines if the note for this event note
    is null or is empty.
 
    @return True if the note for this event note
    is not null and not empty.
   */
  public boolean hasNoteWithData () {
    return (note != null && note.length() > 0);
  }

  /**
    Returns the note for this event note.
 
    @return The note for this event note.
   */
  public String getNote () {
    return note;
  }
 
  /**
     Sets the note as html for this event note.
 
     @param  noteAsHtml The note as html for this event note.
   */
  public void setNoteAsHtml (String noteAsHtml) {
    this.noteAsHtml = noteAsHtml;
    setModified (true);
  }

  /**
    Returns the note as html for this event note as a string.
 
    @return The note as html for this event note as a string.
   */
  public String getNoteAsHtmlAsString () {
    if (hasNoteAsHtml()) {
      return getNoteAsHtml().toString();
    } else {
      return "";
    }
  }

  /**
    Determines if the note as html for this event note is null.
 
    @return True if the note as html for this event note is not null.
   */
  public boolean hasNoteAsHtml () {
    return (noteAsHtml != null);
  }

  /**
    Determines if the note as html for this event note
    is null or is empty.
 
    @return True if the note as html for this event note
    is not null and not empty.
   */
  public boolean hasNoteAsHtmlWithData () {
    return (noteAsHtml != null && noteAsHtml.length() > 0);
  }

  /**
    Returns the note as html for this event note.
 
    @return The note as html for this event note.
   */
  public String getNoteAsHtml () {
    return noteAsHtml;
  }

  /**
   Return the number of columns.
   */
  public static int getColumnCount() {
    return COLUMN_COUNT;
  }

  /*
   Following code generated by PSTextMerge using:
 
     template:  taggable-methods.java
     data file: /Users/hbowie/Java/projects/nbproj/clubplanner/javagen/fields.xls
   */
 

  /**
   Does this class have a Tags field?
 
   @return True if so, false if not.
   */
  public static boolean isClassTagged() {
    return false;
  }
 
  /**
   Return the tags assigned to this taggable item.
 
   @return The tags assigned.
   */
  public Tags getTags () {
    // No Tags field for this item
    return null;
  }
 
  /**
   Flatten all the tags for this item, separating each level/word into its own
   first-level tag.
   */
  public void flattenTags() {
    // No Tags field for this item
  }

  /**
   Convert the tags to all lower-case letters.
   */
  public void lowerCaseTags (){
    // No Tags field for this item
  }
 
  /**
   Set the first TagsNode occurrence for this Taggable item. This is stored
   in a TagsModel occurrence.

   @param tagsNode The tags node to be stored.
   */
  public void setTagsNode (TreeItem<TagsNodeValue> tagsNode) {
    // No Tags field for this item
  }

  /**
   Return the first TagsNode occurrence for this Taggable item. These nodes
   are stored in a TagsModel occurrence.

   @return The tags node stored.
   */
  public TreeItem<TagsNodeValue> getTagsNode () {
    // No Tags field for this item
    return null;
  }

}