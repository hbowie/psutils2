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
 A mapping from the unique key for each note to the note itself. 

 @author Herb Bowie
 */
public class NoteCollectionMap {
  
  private Map<String, Note> mappedNotes;
  
  /**
   Build a new map. 
  */
  public NoteCollectionMap() {
    mappedNotes = new HashMap();
  }
  
  /**
   Add a new note to this map. 
  
   @param newNote The note to be added.
  
   @return True if successfully added, false if this map already contained
           an entry for this key. 
  */
  public boolean add(Note newNote) {
    String uniqueKey = newNote.getUniqueKey();
    if (mappedNotes.containsKey(uniqueKey)) {
      return false;
    } else {
      mappedNotes.put(uniqueKey, newNote);
      return true;
    }
  }
  
  /**
   Rmove the indicated note from this collection. 
  
   @param noteToRemove The note to be removed. 
  
   @return True if successfully removed, false if key mapping could not 
           be found. 
  */
  public boolean remove(Note noteToRemove) {
    return remove(noteToRemove.getUniqueKey());
  }
  
  /**
   Rmove the indicated note from this collection. 
  
   @param keyToRemove The key to be removed, along with its note. 
  
   @return True if successfully removed, false if key mapping could not 
           be found. 
  */
  public boolean remove(String keyToRemove) {
    Note removed = mappedNotes.remove(keyToRemove);
    return (removed != null);
  }
  
  /**
   Get a note based on its tile. 
  
   @param title The title of a note. 
  
   @return The note with that title, if any, otherwise null. 
  */
  public Note getFromTitle(String title) {
    String key = Note.makeUniqueKey(title);
    Note note = get(key);
    return note;
  }
  
  /**
   Get a note based on its unique key. 
  
   @param uniqueKey The desired key. 
  
   @return The note having the indicated key, or null if no note exists with
           this key. 
  */
  public Note get(String uniqueKey) {
    return mappedNotes.get(uniqueKey);
  }
  
  /**
   Does this mapping contain a note with this key?
  
   @param note The note of interest. 
  
   @return True if the mapping already contains a note with this key. 
  */
  public boolean contains(Note note) {
    return mappedNotes.containsKey(note.getUniqueKey());
  }
  
  /**
   Does this mapping contain a note with this key?
  
   @param uniqueKey The key of interest.
  
   @return True if note with this key already exists, false if no
           matching key could be found. 
  */
  public boolean contains(String uniqueKey) {
    return mappedNotes.containsKey(uniqueKey);
  }
  
  /**
   The number of mappedNotes contained in this mapping. 
  
   @return The number of mappedNotes contained in this mapping.
  */
  public int size() {
    return mappedNotes.size();
  }

}
