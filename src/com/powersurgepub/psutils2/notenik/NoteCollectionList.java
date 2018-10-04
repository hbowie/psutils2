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

  import java.util.*;

/**
 This is a simple list of all notes in the collection. 

 @author Herb Bowie
 */
public class NoteCollectionList {
  
  private ArrayList<Note>                     notes = null;
  
  /**
   Construct a new list. This need only be done once, when a collection is
   first opened. 
  */
  public NoteCollectionList() {
    notes = new ArrayList();
  }
  
  /**
   Add a new note to the end of the list. 
  
   @param newNote The note to be added.
  
   @return The position of the new note within the list. 
  */
  public int add(Note newNote) {
    int id = notes.size();
    newNote.setCollectionID(id);
    notes.add(newNote);
    return id;
  }
  
  /**
   Delete a note from the collection by setting its deleted flag to true. 
  
   @param positionToRemove An index indicating the position/id of the
                           note to be flagged for deletion. 
  */
  public void remove(int positionToRemove) {
    Note noteToRemove = get(positionToRemove);
    remove(noteToRemove);
  }
  
  /**
   Delete a note from the collection by setting its deleted flag to true. 
  
   @param noteToRemove The note to be flagged for deletion.
  */
  public boolean remove(Note noteToRemove) {
    boolean ok = false;
    if (noteToRemove != null) {
      noteToRemove.setDeleted(true);
      ok = true;
    }
    return ok;
  }
  
  /**
   Return the note at the indicated position in the list, or null, if
   the passed index is out of range. 
  
   @param index An index pointing to a position in the list. 
  
   @return The note at that position. 
  */
  public Note get(int index) {
    if (index < 0 || index >= notes.size()) {
      return null;
    } else {
      return notes.get(index);
    }
  }
  
  /**
   Return the number of notes in the list.
  
   @return Size of the list. 
  */
  public int size() {
    return notes.size();
  }

}
