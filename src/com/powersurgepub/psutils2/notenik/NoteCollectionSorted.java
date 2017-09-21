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

  import javafx.collections.*;
  import javafx.scene.control.*;
  import javafx.scene.control.cell.*;

/**
 This class maintains a sorted, filtered ObservableList of notes, along with 
 a TableView using that list as a model. The sort sequence and columns of the 
 TableView are changed based on the NoteSortParm settings. Note, however, 
 that sort keys should always be unique, since they always include the note's 
 title, and the title itself should always be unique within a collection. 

 @author Herb Bowie
 */
public class NoteCollectionSorted {
  
  private NoteSortParm                        sortParm = null;
  private NoteCollectionList                  notes = null;
  
  private ObservableList<SortedNote>          sortedNotes = null;
  private int                                 sortIndex = -1;
  private boolean                             sortMatch = false;
  
  private TableView<SortedNote>               noteTable = null;
  private TableColumn<SortedNote, String>     doneColumn = null;
  private TableColumn<SortedNote, SortedDate> dateColumn = null;
  private TableColumn<SortedNote, SortedSeq>  seqColumn = null;
  private TableColumn<SortedNote, String>     titleColumn = null;
  
  /**
   Construct the object. 
  
   @param sortParm The sort parm to be used.
  */
  public NoteCollectionSorted(NoteSortParm sortParm) {
    this.sortParm = sortParm;
    sortedNotes = FXCollections.observableArrayList();
    buildColumns();
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
   Given a Sort Parm and a basic list of notes, create and populate the 
   sorted list and its associated table. Note that whenever we have a 
   new list of notes, or new / modified sort parms, both the observable 
   list and the table view will be created from scratch. 
   
   @param notes    The list of notes to be used. 
  */
  public void genSortedList(NoteCollectionList notes) {
    
    this.notes = notes;
    
    sortedNotes = FXCollections.observableArrayList();
    for (int i = 0; i < notes.size(); i++) {
      Note nextNote = notes.get(i);
      add(nextNote);
    }
    
    genNoteTable();
  }
  
  /**
   Generate the Table View for the Notes.
  */
  public void genNoteTable() {
    noteTable = new TableView<>(sortedNotes);
    noteTable.setItems(sortedNotes);
    addColumns();
  }
  
  /**
   Add the appropriate columns, depending on the chosen sort parms. 
  */
  private void addColumns() {

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
   Add a new sorted note to the list of sorted, filtered notes. 
   
   @param index Pointing to the position in the Notes List containing the 
                note to be added. 
   */
  void add (Note newNote) {

    SortedNote sorted = new SortedNote(newNote, sortParm);
    
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
    
  } // end add method
  
  /**
   Remove the given note.
  
   @param noteToRemove The note to be removed. 
  
   @return True if removed, false if not found. 
  */
  boolean remove (Note noteToRemove) {
    System.out.println("NoteCollectionSorted.remove note with title = " 
        + noteToRemove.getTitle());
    return remove(noteToRemove.getSortKey(sortParm));
  }
  
  /**
   Remove the note having the given sort key. 
  
   @param sortKey A sort key identifying a note. 
  
   @return True if note found and removed, false otherwise. 
  */
  boolean remove (String sortKey) {
    System.out.println("NoteCollectionSorted.remove sork key = " 
        + sortKey);
    findSortInternal(sortKey);
    if (sortMatch) {
      return remove(sortIndex);
    } else {
      System.out.println("NoteCollectionSorted.remove: "
          + sortKey + " Not found");
      return false;
    }
  }
  
  /**
   Remove the SortedNote occupying the indicated index. 
  
   @param i The index to the note to be removed.
  
   @return True if note found and removed, false otherwise;
  */
  boolean remove (int i) {
    System.out.println("NoteCollectionSorted.remove index =  " 
        + String.valueOf(i));
    if (i < 0 || i >= sortedNotes.size()) {
      return false;
    } else {
      SortedNote removedNote = sortedNotes.remove(i);
      if (removedNote == null) {
        return false;
      } else {
        return true;
      }
    }
  }
  
  /**
   Find the sorted note whose sort key matches that of the passed note. 
  
   @param findNote The note we're looking for. 
  
   @return The SortedNote with a matching key, or null if not match. 
  */
  public SortedNote getSortedNote(Note findNote) {
    findSortInternal(findNote);
    if (sortMatch) {
      return sortedNotes.get(sortIndex);
    } else {
      return null;
    }
  }
  
  /**
   Get the requested note from the sorted list. 
  
   @param i An index into the sorted list. 
  
   @return The note at this index position, or null, if the index is out
           of range. 
  */
  public Note get(int i) {
    if (i < 0 || i >= sortedNotes.size()) {
      return null;
    } else {
      return sortedNotes.get(i).getNote();
    }
  }
  
  
  /**
   Find the position of the given note within the sorted list.
  
   @param note The note we're looking for. 
  
   @return The position of the note within the sorted list, or -1
           if no matching sort key found. 
  */
  public int findSorted(Note note) {
    return findSorted(note.getSortKey(sortParm));
  }
  
  /**
   Find the position of the note with the given sort key within the sorted list. 
  
   @param findKey The sort key of the note we're looking for. 
  
   @return The position of the note within the sorted list, or -1
           if no matching sort key found. 
  */
  public int findSorted(String findKey) {
    findSortInternal(findKey);
    if (sortMatch) {
      return sortIndex;
    } else {
      return -1;
    }
  }
  
  /**
   Find the appropriate insertion point or match point in the sortKeys list,
   and use sortIndex and sortMatch to return the results.

   @param findNote Note we are looking for.
   */
  private void findSortInternal (Note findNote) {
    findSortInternal(findNote.getSortKey(sortParm));
  } // end findsortInternal method
  
  /**
   Find the appropriate insertion point or match point in the sortKeys list,
   and use sortIndex and sortMatch to return the results.

   @param findKey Sort key we are looking for. 
   */
  private void findSortInternal (String findKey) {

    int low = 0;
    int high = sortedNotes.size() - 1;
    sortIndex = 0;
    sortMatch = false;
    while (high >= low
        && sortMatch == false
        && sortIndex < sortedNotes.size()) {
      int diff = high - low;
      int split = diff / 2;
      sortIndex = low + split;
      int compare = compareSort (sortIndex, findKey);
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
  
  /**
   Compare the note at the given index to the sort key for a second note. 
  
   @param index   An index pointing to a note in the sorted list. 
   @param sorted2 A second sorted note. 
  
   @return Value less than zero if the indexed note is less than the passed note,
           greater than zero if the indexed note is greater than the passed note,
           or zero if the two notes have the same sort keys.
  */
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
    String key2 = note2.getSortKey(sortParm);
    return compareSort(index, key2);
  }
  
  /**
   Compare the note at the given index to a sort key for another note. 
  
   @param index Index pointing to an entry in the sorted list of notes. 
  
   @param key2 The sort key for a second note. 
  
   @return Value less than zero if the indexed note is less than the passed note,
           greater than zero if the indexed note is greater than the passed note,
           or zero if the two notes have the same sort keys. 
  */
  private int compareSort(int index, String key2) {

    SortedNote sorted1 = sortedNotes.get(index);
    String key1 = sorted1.getSortKey();
    int compare = key1.compareTo(key2);
    return (compare);
  }
  
  /**
   Get the number of notes in the sorted list. 
  
   @return The size of the sorted list. 
  */
  int size() {
    return sortedNotes.size();
  }
  
  /**
   Return a table view that can be used to view the sorted list. 
  
   @return A table view for the list. 
  */
  public TableView getTableView() {
    return noteTable;
  }

}
