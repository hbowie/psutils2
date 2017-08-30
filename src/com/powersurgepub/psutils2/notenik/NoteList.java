/*
 * Copyright 2009 - 2017 Herb Bowie
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
  import com.powersurgepub.psutils2.tags.*;
  import com.powersurgepub.psutils2.ui.*;
  import com.powersurgepub.psutils2.values.*;

  import java.io.*;
  import java.util.*;

  import javafx.collections.*;
  import javafx.scene.control.*;
  import javafx.scene.control.cell.*;

/**
 A list of notes.
 */
public class NoteList
    implements 
      // PSList, 
      TaggableList {

  private String                      title = "Note List";
  private RecordDefinition            recDef = null;
  // private List<Note>      notes = new ArrayList<Note>();

  // private int             findIndex = -1;
  // private boolean         findMatch = false;

  private TagsList                    tagsList = new TagsList();
  private TagsView                    tagsView = new TagsView();
  
  /*
   New Lists added to allow multiple sequences to be maintained. 
  */
  
  /** 
   New notes are added to the end of notes. Notes are never removed from
   this list. 
  */
  private ArrayList<Note>                     notes = null;
  private ObservableList<SortedNote>          sortedNotes = null;
  private NoteSortParm                        sortParm = new NoteSortParm();
  private TableView<SortedNote>               noteTable = null;
  private TableColumn<SortedNote, String>     doneColumn = null;
  private TableColumn<SortedNote, SortedDate> dateColumn = null;
  private TableColumn<SortedNote, SortedSeq>  seqColumn = null;
  private TableColumn<SortedNote, String>     titleColumn = null;
  
  /**
   Items in this list contain an index pointing back to a note in notes. 
   This list is sequenced by each note's unique key, and duplicates are
   not allowed. 
  */
  private List<Integer>               uniqueKeys = new ArrayList<Integer>();
  
  private int                         uniqueIndex = -1;
  private boolean                     uniqueMatch = false;
  

  /**
   Items in this list contain an index pointing back to a note in notes. 
   This list is sequenced by each note's sort key, and duplicates are allowed.
  */
  /*
  private List<Integer>               sortKeys   = new ArrayList<Integer>();
  */
  private int                         sortIndex = -1;
  private boolean                     sortMatch = false;
  
  


  /**
   Construct a new Note List. 
  
   @param recDef A passed record definition to be used. 
  */
  public NoteList (RecordDefinition recDef) {
    notes = new ArrayList();
    sortedNotes = FXCollections.observableArrayList();
    this.recDef = recDef;
    tagsList.registerValue("");
    createTable();
  }
  
  public void setTable(TableView noteTable) {
    this.noteTable = noteTable;
  }
  
  public TableView getTable() {
    return noteTable;
  }
  
  /**
   Pass in the Sort Parameter which will control how the key used for the 
   sorted list. 
  
   @param sortParm 
   */
  public void setSortParm(NoteSortParm sortParm) {
    this.sortParm = sortParm;
    sortParm.setList(this);
    adjustTableStructure();
    // fireTableDataChanged();
  }
  
  public NoteSortParm getSortParm() {
    return sortParm;
  }
  
  public RecordDefinition getRecDef() {
    return recDef;
  }

  public TagsList getTagsList () {
    return tagsList;
  }

  public TagsView getTagsModel () {
    return tagsView;
  }

  public File getSource () {
    return tagsView.getSource();
  }

  public void setSource (File source) {
    tagsView.setSource(source);
  }
  
  /*
   Build the table view, configuring its columns based on the sort parameters
   provided, and connecting the notes table to it. 
  */
  
  /**
   Recreate the list of sorted keys when the sort parameters change. 
  */
  public void sortParmChanged() {
    sortedNotes = FXCollections.observableArrayList();
    for (int i = 0; i < notes.size(); i++) {
      addSortedNote(i);
    }
    adjustTableStructure();
    // fireTableDataChanged();
  }
  
  private void adjustTableStructure() {
    while (noteTable.getColumns().size() > 0) {
      noteTable.getColumns().remove(0);
    }
    addColumns();
  }
  
  public void createTable() {
    noteTable = new TableView<SortedNote>(sortedNotes);
    noteTable.setItems(sortedNotes);
    addColumns();
    // noteTable.setSortPolicy(null);
  }
  
  private void addColumns() {
    buildColumns();
    switch (sortParm.getParm()) {
      case NoteSortParm.SORT_TASKS_BY_DATE:
        noteTable.getColumns().addAll
          (doneColumn, dateColumn, seqColumn, titleColumn);
        noteTable.getSortOrder().addAll
          (doneColumn, dateColumn, seqColumn, titleColumn);
        break;
      case NoteSortParm.SORT_TASKS_BY_SEQ:
        noteTable.getColumns().addAll
          (doneColumn, seqColumn, dateColumn, titleColumn);
        noteTable.getSortOrder().addAll
          (doneColumn, seqColumn, dateColumn, titleColumn);
        break;
      case NoteSortParm.SORT_BY_SEQ_AND_TITLE:
        noteTable.getColumns().addAll(seqColumn, titleColumn);
        noteTable.getSortOrder().addAll(seqColumn, titleColumn);
        break;
      case NoteSortParm.SORT_BY_TITLE:
        noteTable.getColumns().addAll(titleColumn);
        noteTable.getSortOrder().addAll(titleColumn);
        break;
      default:
        break;
    }
  }
  
  /**
   Build all the columns we might ever use to display a table of notes. 
  */
  private void buildColumns() {
    
    // Build the Done Column
    doneColumn = new TableColumn<SortedNote, String>("X");
    doneColumn.setPrefWidth(20);
    doneColumn.setMaxWidth(30);
    // doneColumn.setSortable(false);
    doneColumn.setCellValueFactory(
        new PropertyValueFactory<SortedNote, String>("done")
    );

    // Build the Date Columns
    dateColumn = new TableColumn<SortedNote, SortedDate>("Date");
    dateColumn.setPrefWidth(120);
    dateColumn.setMaxWidth(160);
    // dateColumn.setSortable(false);
    dateColumn.setCellValueFactory(
        new PropertyValueFactory<SortedNote, SortedDate>("date")
    );

    // Build the Seq Column
    seqColumn = new TableColumn<SortedNote, SortedSeq>("Seq");
    seqColumn.setPrefWidth(60);
    seqColumn.setMaxWidth(120);
    // seqColumn.setSortable(false);
    seqColumn.setCellValueFactory(
        new PropertyValueFactory<SortedNote, SortedSeq>("seq")
    );

    // Build the Title Column
    titleColumn 
        = new TableColumn<SortedNote, String>("Title");
    titleColumn.setPrefWidth(400);
    // titleColumn.setSortable(false);
    titleColumn.setCellValueFactory(
        new PropertyValueFactory<SortedNote, String>("title")
    );

  }
  
  /**
   Increment the sequence value for the passed note and any notes with 
   equal sequences.
  
   @param position The note at which to start. 
  
   @return New Sequence Number for Starting Note
  */
  public String incrementSeq(
      NotePositioned position, 
      NoteIO noteIO,
      FolderSyncPrefsData folderSyncPrefs) {
    
    boolean incrementing = true;
    boolean incrementingOnLeft = true;
    int index = position.getIndex();
    Note incNote = getNoteFromSortIndex(index);
    if (incNote.getSeqValue().getPositionsToRightOfDecimal() > 0) {
      incrementingOnLeft = false;
    }
    incNote.incrementSeq(incrementingOnLeft);
    String newSeq = incNote.getSeq();
    saveNote(incNote, noteIO, folderSyncPrefs);
    sortParm.maintainSeqStats(incNote.getSeqValue());
    DataValueSeq lastSeq = incNote.getSeqValue();
    index++;
    while (incrementing && index < sortedNotes.size()) {
      incNote = getNoteFromSortIndex(index);
      int result = incNote.getSeqValue().compareTo(lastSeq);
      if (result > 0) {
         if (incrementingOnLeft) {
           result = incNote.getSeqValue().getLeft().compareTo
               (lastSeq.getLeft());
         }
      }
      if (result > 0) {
        incrementing = false;
      } else {
        incNote.incrementSeq(incrementingOnLeft);
        saveNote(incNote, noteIO, folderSyncPrefs);
        lastSeq = incNote.getSeqValue();
        sortParm.maintainSeqStats(lastSeq);
        incrementing = true;
      }
      index++;
    } // end while incrementing
    // fireTableDataChanged();
    return newSeq;
  }
  
  /**
   Saves a newNote in its primary location and in its sync folder, if specified. 
  
   @param note The newNote to be saved. 
  */
  private boolean saveNote(
      Note note, 
      NoteIO noteIO, 
      FolderSyncPrefsData folderSyncPrefs) {
    try {
      noteIO.save(note, true);
      if (folderSyncPrefs.getSync()) {
        noteIO.saveToSyncFolder(
            folderSyncPrefs.getSyncFolder(), 
            folderSyncPrefs.getSyncPrefix(), 
            note);
        note.setSynced(true);
      }
      return true;
    } catch (IOException e) {
      ioException(e);
      return false;
    }
  }
  
  private void ioException(IOException e) {
    Trouble.getShared().report("I/O Exception", "Trouble");
  }

  /**
   Add a new note to the list. If a note with the same key (title) already
   exists in the list, the the note to be added will instead be merged with
   the existing note. 
   
   @param newNote
   @return A positioned note composed of the resulting note and an index
           pointing to its resulting position in the list.
   */
  public NotePositioned add (Note newNote) {
    
    return add2(newNote);
    
    /*
    Note resultingNote = newNote;
    boolean merged = false;
    
    if (notes.isEmpty()) {
      // If this is the first note being added to the collection, simply add it
      notes.add (newNote);
      findIndex = 0;
    }
    else
    if (get(notes.size() - 1).compareTo(newNote) < 0) {
      // If the new Note has a key higher than the highest item in the
      // collection, simply add the new Note to the end
      // (more efficient if an input file happens to be pre-sorted).
      findIndex = notes.size();
      notes.add (newNote);
    } else {
      findInternal (newNote);
      if (findMatch) {
        get(findIndex).merge(newNote);
        resultingNote = get(findIndex);
        merged = true;
      } else {
        notes.add (findIndex, newNote);
      }
    }
    
    if (merged) {
      tagsList.modify  (resultingNote);
      tagsView.modify (resultingNote);
    } else {
      tagsList.add  (resultingNote);
      tagsView.add (resultingNote);
    }

    return new NotePositioned (resultingNote, findIndex);
    */
  } // end add method
  
  /**
   Add a new note to the list. If a note with the same key (title) already
   exists in the list, the the note to be added will instead be merged with
   the existing note. 
   
   @param newNote
   @return A positioned note composed of the resulting note and an index
           pointing to its resulting position in the list.
   */
  public NotePositioned add2 (Note newNote) {

    // System.out.println("NoteList.add2 adding Note with title = " + newNote.getTitle());
    
    Note resultingNote = newNote;
    boolean merged = false;
    int noteIndex = -1;
    
    // System.out.println("NoteList.add2 before addUniqueKey");
    addUniqueKey(newNote, notes.size());
    
    // System.out.println("NoteList.add2 before sortParm.maintainSeqStats");
    sortParm.maintainSeqStats(newNote.getSeqValue());
    // System.out.println("  - unique match? " + String.valueOf(uniqueMatch));
    
    if (uniqueMatch) {
      resultingNote = getNoteFromUniqueIndex(uniqueIndex);
      resultingNote.merge(newNote);
      merged = true;
      noteIndex = getNoteIndexFromUniqueIndex (uniqueIndex);
    } else {
      noteIndex = notes.size();
      notes.add (newNote);
      addSortedNote (noteIndex);
    }
    // System.out.println("  - merged? " + String.valueOf(merged));
    // System.out.println("NoteList.add2 before tagsList calls");
    if (merged) {
      tagsList.modify  (resultingNote);
      tagsView.modify (resultingNote);
    } else {
      // System.out.println("NoteList.add2 before tagsList add");
      tagsList.add  (resultingNote);
      // System.out.println("NoteList.add2 before tagsView add");
      tagsView.add (resultingNote);
    }
    // System.out.println("NoteList.add2 about to create NotePositioned");
    NotePositioned notePositioned = new NotePositioned(resultingNote, sortIndex);
    // System.out.println("NoteList.add2 NotePositioned created");
    return notePositioned;
  } // end add method

  public NotePositioned modify (NotePositioned modNote) {
    return modify2(modNote);
    
    /*
    tagsList.modify(modNote.getNote());
    tagsView.modify(modNote.getNote());
    return modNote;
    */
  }

  /**
   Removes the passed note, if it exists in the collection.

   @param position A position containing the note to be removed.
   
   @return A position for the next note following the one just removed.
   */
  public NotePositioned remove (NotePositioned position) {
    
    return remove2(position);
    
    /*
    int oldIndex = find (position.getNote());
    NotePositioned newPosition = position;
    if (findMatch) {
      newPosition = next (position);
      tagsView.remove (position.getNote());
      tagsList.remove (position.getNote());
      notes.remove(oldIndex);
    }
    return newPosition;
    */
  }
  
  /**
   Removes the passed note, if it exists in the collection.

   @param noteToRemove The note to be removed.
   
   @return True if note found and removed; false otherwise.
   */
  public boolean remove (Note noteToRemove) {
    
    return remove2(noteToRemove);
    
    /*
    int oldIndex = find (noteToRemove);
    if (findMatch) {
      tagsView.remove (noteToRemove);
      tagsList.remove (noteToRemove);
      notes.remove(oldIndex);
    }
    return findMatch;
    */
  }

  /**
   Find the passed note in the list of sorted keys. 
  
   @param findNote The note to be found. 
  
   @return The index to the Note's position in the sorted table, 
           or -1 if not found. 
  */
  public int find (Note findNote) {
    findSortInternal (findNote);
    if (sortMatch) {
      return sortIndex;
    } else {
      return -1;
    }
  }
  

  /**
   Find the appropriate insertion point or match point in the note list,
   and use findIndex and findMatch to return the results.

   @param findNote Note we are looking for.
   */
  /*
  private void findInternal (Note findNote) {
    int low = 0;
    int high = notes.size() - 1;
    findIndex = 0;
    findMatch = false;
    while (high >= low
        && findMatch == false
        && findIndex < notes.size()) {
      int diff = high - low;
      int split = diff / 2;
      findIndex = low + split;
      int compare = get(findIndex).compareTo(findNote);
      if (compare == 0) {
        // found an exact match
        findMatch = true;
      }
      else
      if (compare < 0) {
        // note from list is less than the one we're looking for
        findIndex++;
        low = findIndex;
      } else {
        // note from list is greater than the one we're looking for
        if (high > findIndex) {
          high = findIndex;
        } else {
          high = findIndex - 1;
        }
      }
    } // end while looking for right position
  } // end find method
  */
  
  /*
   New methods using new tables. 
  */
  

  
  public NotePositioned modify2 (NotePositioned modPosition) {
    
    Note modNote = modPosition.getNote();
    sortParm.maintainSeqStats(modNote.getSeqValue());
    String modKey = modNote.getSortKey(sortParm);
    int index = modPosition.getIndex();
    boolean moved = false;
    while (index > 0
        && (modKey.compareTo(getNoteFromSortIndex(index - 1).getSortKey(sortParm)) < 0)) {
      SortedNote sorted = sortedNotes.remove(index);
      index--;
      sortedNotes.add(index, sorted);
      moved = true;
    }
    if (! moved) {
      int lastEntry =  sortedNotes.size() - 1;
      while (index < lastEntry
          && (modKey.compareTo(getNoteFromSortIndex(index + 1).getSortKey(sortParm)) > 0)) {
        SortedNote sorted = sortedNotes.remove(index);
        index++;
        sortedNotes.add(index, sorted);
        moved = true;
      }
    }
    
    if (moved) {
      modPosition.setIndex(index);
      // fireTableDataChanged();
    }
    
    tagsList.modify(modNote);
    tagsView.modify(modNote);
    
    return modPosition;
  }
  
  public boolean atEnd(NotePositioned position) {
    return (position != null && (position.getIndex() == (sortedNotes.size() - 1)));
  }
  
  /**
   Remove the passed note from the new tables. 
  
   @param position
   @return 
  */
  public NotePositioned remove2 (NotePositioned position) {
    
    // Note that we are removing the note from the key tables, 
    // but not from the actual note table, so as not to disturb
    // indices pointing back to notes in this table. 
    
    NotePositioned newPosition = position;
    
    boolean found = remove2 (position.getNote());
    if (found) {
      newPosition = next (position);
    }
    
    /*
    if (found) {
      newPosition = next (position);
      tagsView.remove (position.getNote());
      tagsList.remove (position.getNote());
    } */
    return newPosition;
  }
  
  public boolean remove2 (Note noteToRemove) {

    findUniqueInternal(noteToRemove);
    if (uniqueMatch) {
      uniqueKeys.remove(uniqueIndex);
      tagsView.remove (noteToRemove);
      tagsList.remove (noteToRemove);
    }
    
    findSortInternal(noteToRemove);
    if (sortMatch) {
      sortedNotes.remove(sortIndex);
    }
    
    // fireTableDataChanged();
    
    return sortMatch;
  }
  
  /**
   Use the passed index to retrieve a note from the list of sorted, filtered
   notes. 
  
   @param index An index to the list of sorted, filtered notes. 
  
   @return The corresponding note, or null for an invalid index.  
  */
  public Note get2 (int index) {
    if (index >= 0 && index < sortedNotes.size()) {
      return getNoteFromSortIndex(index);
    } else {
      return null;
    }
  } // end method get (int)
  
  /**
   Adds the unique key of a note to the list of unique keys, if it does not
   already exist. 
  
   @param newNote The note being added. 
   @param index   The position in the notes list at which the note would be 
                  added, if it has a new unique key. 
   @return The position in the unique keys list at which the note as 
           added, or merge. 
   */
  public int addUniqueKey (Note newNote, int index) {

    boolean merged = false;
    
    Integer unique = new Integer(index);
    
    uniqueMatch = false;
    
    if (uniqueKeys.isEmpty()) {
      // If this is the first note being added to the collection, simply add it
      uniqueKeys.add (unique);
      uniqueIndex = 0;
    }
    else
    if (compareUnique(uniqueKeys.size() - 1, newNote) < 0) {
      // If the new Note has a key higher than the highest item in the
      // collection, simply add the new Note to the end
      // (more efficient if an input file happens to be pre-sorted).
      uniqueIndex = uniqueKeys.size();
      uniqueKeys.add (unique);
    } else {
      findUniqueInternal (newNote);
      if (uniqueMatch) {
        getNoteFromUniqueIndex(uniqueIndex).merge(newNote);
        merged = true;
      } else {
        uniqueKeys.add (uniqueIndex, unique);
      }
    }
    
    return uniqueIndex;
    
  } // end add method
  
  /**
   Find the appropriate insertion point or match point in the uniqueKeys list,
   and use uniqueIndex and uniqueMatch to return the results.

   @param findNote Note we are looking for.
   */
  private void findUniqueInternal (Note findNote) {
    int low = 0;
    int high = uniqueKeys.size() - 1;
    uniqueIndex = 0;
    uniqueMatch = false;
    while (high >= low
        && uniqueMatch == false
        && uniqueIndex < notes.size()) {
      int diff = high - low;
      int split = diff / 2;
      uniqueIndex = low + split;
      int compare = compareUnique (uniqueIndex, findNote);
      if (compare == 0) {
        // found an exact match
        uniqueMatch = true;
      }
      else
      if (compare < 0) {
        // note from list is less than the one we're looking for
        uniqueIndex++;
        low = uniqueIndex;
      } else {
        // note from list is greater than the one we're looking for
        if (high > uniqueIndex) {
          high = uniqueIndex;
        } else {
          high = uniqueIndex - 1;
        }
      }
    } // end while looking for right position
  } // end findUniqueInternal method
  
  /**
   Compare the given note to the note represented by the passed location in the 
   uniqueKeys list. 
  
   @param index Index pointing to an entry in the uniqueKeys list. 
   @param note2 The note to be compared. 
   @return Value less than zero if the indexed note is less than the passed note,
           greater than zero if the indexed note is greater than the passed note,
           or zero if the two notes have the same unique keys. 
  */
  private int compareUnique(int index, Note note2) {
    Note note1 = getNoteFromUniqueIndex (index);
    String key1 = note1.getUniqueKey();
    String key2 = note2.getUniqueKey();
    return (key1.compareTo(key2));
  }
  
  private Note getNoteFromUniqueIndex (int uniqueIndexIn) {
    return notes.get(getNoteIndexFromUniqueIndex(uniqueIndexIn));
  }
  
  private int getNoteIndexFromUniqueIndex (int uniqueIndexIn) {
    Integer notePointer = uniqueKeys.get(uniqueIndexIn);
    return notePointer.intValue();
  }
  
  /**
   Find the appropriate insertion point or match point in the sortKeys list,
   and use sortIndex to return the results.

   @param findNote Note we are looking for.
   */
  private void findSortInternal (Note findNote) {
    int low = 0;
    int high = sortedNotes.size() - 1;
    sortIndex = 0;
    sortMatch = false;
    while (high >= low
        && sortMatch == false
        && sortIndex < notes.size()) {
      int diff = high - low;
      int split = diff / 2;
      sortIndex = low + split;
      int compare = compareSort (sortIndex, findNote);
      if (compare == 0) {
        // found an exact match
        sortMatch = true;
      }
      else
      if (compare < 0) {
        // note from list is less than the one we're looking for
        sortIndex++;
        low = sortIndex;
      } else {
        // note from list is greater than the one we're looking for
        if (high > sortIndex) {
          high = sortIndex;
        } else {
          high = sortIndex - 1;
        }
      }
    } // end while looking for right position
  } // end findsortInternal method
  
  private int compareSort(int index, SortedNote sorted2) {
    return compareSort(index, sorted2.getNote());
  }
  
  /**
   Compare the given note to the note represented by the passed location in the 
   sortKeys list. 
  
   @param index Index pointing to an entry in the sortKeys list. 
   @param note2 The note to be compared. 
   @return Value less than zero if the indexed note is less than the passed note,
           greater than zero if the indexed note is greater than the passed note,
           or zero if the two notes have the same sort keys. 
  */
  private int compareSort(int index, Note note2) {
    SortedNote sorted1 = sortedNotes.get(index);
    String key1 = sorted1.getSortKey(sortParm);
    String key2 = note2.getSortKey(sortParm);
    int compare = key1.compareTo(key2);
    
    // System.out.println("    compareSort");
    // System.out.println("      sort key 1: " + key1);
    // System.out.println("      sort key 2: " + key2);
    // System.out.println("        result = " + String.valueOf(compare));
    
    return (compare);
  }
  
  private Note getNoteFromSortIndex (int sortIndexIn) {
    return sortedNotes.get(sortIndexIn).getNote();
  }
  
  private int getNoteIndexFromSortIndex (int sortIndexIn) {
    return sortedNotes.get(sortIndexIn).getStableListIndex();
  }
  
  /* ===================================================================
   * The following methods generate a new note position relative to
   * an existing one. 
   * =================================================================== */
  
  public NotePositioned first (NotePositioned position) {
    if (position.navigateUsingList()) {
      return firstUsingList ();
    } else {
      return firstUsingTree ();
    }
  }
  
  public NotePositioned last (NotePositioned position) {
    if (position.navigateUsingList()) {
      return lastUsingList ();
    } else {
      return lastUsingTree ();
    }
  }

  public NotePositioned next (NotePositioned position) {
    NotePositioned nextPosition;
    if (position.navigateUsingList()) {
      nextPosition = nextUsingList (position);
    } else {
      nextPosition = nextUsingTree (position);
    }
    if (nextPosition == null) {
      return first(position);
    } else {
      return nextPosition;
    }
  }

  public NotePositioned prior (NotePositioned position) {
    if (position.navigateUsingList()) {
      return priorUsingList (position);
    } else {
      return priorUsingTree (position);
    }
  }

  public NotePositioned firstUsingList () {
    return positionUsingListIndex (0);
  }

  public NotePositioned lastUsingList () {
    return positionUsingListIndex (size() - 1);
  }

  public NotePositioned nextUsingList (NotePositioned position) {
    return (positionUsingListIndex (position.getIndex() + 1));
  }

  public NotePositioned priorUsingList (NotePositioned position) {
    return (positionUsingListIndex (position.getIndex() - 1));
  }

  public NotePositioned positionUsingListIndex (int index) {
    if (index < 0) {
      index = 0;
    }
    if (index >= size()) {
      index = size() - 1;
    }
    NotePositioned position = new NotePositioned(recDef);
    position.setIndex (index);
    position.setNavigator (NotePositioned.NAVIGATE_USING_LIST);
    if (index >= 0) {
      position.setNote (get (index));
      position.setTagsNode (position.getNote().getTagsNode());
    }
    return position;
  }

  public NotePositioned firstUsingTree () {
    return positionUsingNode (tagsView.firstItemNode());
  }

  public NotePositioned lastUsingTree () {
    return positionUsingNode (tagsView.lastItemNode());
  }

  public NotePositioned nextUsingTree (NotePositioned position) {
    if (position.getTagsNode() == null) {
      return null;
    } else {
      return positionUsingNode
          (tagsView.nextItemNode(position.getTagsNode()));
    }
  }

  public NotePositioned priorUsingTree (NotePositioned position) {
    if (position.getTagsNode() == null) {
      return null;
    } else {
      return positionUsingNode
          (tagsView.priorItemNode(position.getTagsNode()));
    }
  }

  /**
   Identify the Note's position, using a Tags Node as input. 
  
   @param node The Tags Node containing the Note. 
  
   @return Note with positioning data. 
  */
  public NotePositioned positionUsingNode (TreeItem<TagsNodeValue> node) {
    if (node == null) {
      return null;
    } else {
      NotePositioned position = new NotePositioned(recDef);
      Note note = (Note)node.getValue().getTaggable();
      position.setNote (note);
      position.setTagsNode (node);
      findSortInternal (position.getNote());
      position.setIndex (sortIndex);
      position.setNavigator (NotePositioned.NAVIGATE_USING_TREE);
      return position;
    }
  }

  /**
   Get a note from the list using an index to the sorted list. 
  
   @param index An index to the sorted list. 
  
   @return The desired Note, or null if index out of bounds. 
  */
  public Note get (int index) {
    if (index >= 0 && index < size()) {
      return getNoteFromSortIndex(index);
    } else {
      return null;
    }
  } // end method get (int)
  
  /*
  public Note getUnfiltered (int index) {
    if (index >= 0 && index < notes.size()) {
      return (Note)notes.get(index);
    } else {
      return null;
    }
  } // end method get (int)
  */
  
  public void setTitle (String title) {
    this.title = title;
  }

  public String getTitle () {
    return title;
  }

  public int size() {
    return sortedNotes.size();
  }
  
  public int totalSize() {
    return sortedNotes.size();
  }
  
  /**
   Add a new sorted note to the list of sorted, filtered notes. 
   
   @param index Pointing to the position in the Notes List containing the 
                note to be added. 
   */
  private void addSortedNote (int index) {

    Note newNote = notes.get(index);
    SortedNote sorted = new SortedNote(newNote, index);
    
    if (sortedNotes.isEmpty()) {
      // If this is the first note being added to the collection, simply add it
      sortedNotes.add (sorted);
      sortIndex = 0;
    }
    else
    if (compareSort(sortedNotes.size() - 1, newNote) < 0) {
      // If the new Note has a key higher than the highest item in the
      // collection, simply add the new Note to the end
      // (more efficient if an input file happens to be pre-sorted).
      sortIndex = sortedNotes.size();
      sortedNotes.add (sorted);
    } else {
      findSortInternal (newNote);
      sortedNotes.add(sortIndex, sorted);
    }
    
  } // end addSortedNote method
  
  /** 
   Return the number of columns in the table. 
  
   @return The number of columns in the table. 
   */
  public int getColumnCount () {
    switch (sortParm.getParm()) {
      case NoteSortParm.SORT_TASKS_BY_DATE:
      case NoteSortParm.SORT_TASKS_BY_SEQ:
        return 4;
      case NoteSortParm.SORT_BY_SEQ_AND_TITLE:
        return 2;
      case NoteSortParm.SORT_BY_TITLE:
      default:
        return 1;
    }
  }
  
  public Class getColumnClass (int columnIndex) {
    return String.class;
  }
  
  public String getColumnName (int columnIndex) {
    switch (sortParm.getParm()) {
      case NoteSortParm.SORT_TASKS_BY_DATE:
        switch (columnIndex) {
          case 0: return "X";
          case 1: return "Date";
          case 2: return "Seq";
          case 3: return "Title";
          default: return "";
        }
      case NoteSortParm.SORT_TASKS_BY_SEQ:
        switch (columnIndex) {
          case 0: return "X";
          case 1: return "Seq";
          case 2: return "Date";
          case 3: return "Title";
          default: return "";
        }
      case NoteSortParm.SORT_BY_SEQ_AND_TITLE:
        switch (columnIndex) {
          case 0: return "Seq";
          case 1: return "Title";
          default: return "";
        }
      case NoteSortParm.SORT_BY_TITLE:
        switch (columnIndex) {
          case 0: return "Title";
          default: return "";
        }
      default:
        return "";
    }
  }

  /**
  Returns the number of rows in the table. 
  
  @return The number of rows in the table
  */
  public int getRowCount() {
    return size();
  }
  
  /**
   Return the cell value to be displayed at the given column and row. 
  
   @param rowIndex The desired row, where the first is at zero. 
   @param columnIndex The desired column, where the first is at zero. 
   @return A string to be displayed in the table cell. 
  */
  public String getValueAt (int rowIndex, int columnIndex) {
    // Note row = get(rowIndex);
    Note row = get2(rowIndex);
    if (row == null) {
      return "";
    } else {
      switch (sortParm.getParm()) {
        case NoteSortParm.SORT_TASKS_BY_SEQ:
          switch (columnIndex) {
            case 0: return row.getDone();
            case 1: return row.getSeq();
            case 2: return row.getDateCommon();
            case 3: return row.getTitle();
            default: return "";
          }
        case NoteSortParm.SORT_TASKS_BY_DATE:
          switch (columnIndex) {
            case 0: return row.getDone();
            case 1: return row.getDateCommon();
            case 2: return row.getSeq();
            case 3: return row.getTitle();
            default: return "";
          }
        case NoteSortParm.SORT_BY_SEQ_AND_TITLE:
          switch (columnIndex) {
            case 0: return row.getSeq();
            case 1: return row.getTitle();
            default: return "";
          }
        case NoteSortParm.SORT_BY_TITLE:
          switch (columnIndex) {
            case 0: return row.getTitle();
            default: return "";
          }
        default:
          return "";
      }
    } // end if good row
  } // end method getValueAt

} // end NoteList class
