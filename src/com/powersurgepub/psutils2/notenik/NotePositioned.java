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

  import javafx.scene.control.*;

/**

 Consists of a Note object and information to position it within the
 Note Collection.

 */
public class NotePositioned {

  public static final int NAVIGATE_USING_LIST = 1;
  public static final int NAVIGATE_USING_TREE = 2;

  private   Note     note;
  /** This is the index to this note's location within 
      a sorted, filtered list of notes.  */
  private   int      index;
  private   TreeItem<TagsNodeValue> tagsNode;
  private   int      navigator = NAVIGATE_USING_LIST;
  private   boolean  newNote = true;
  
  public NotePositioned (NoteParms noteParms) {
    this.note = noteParms.createNewNote();
    this.index = -1;
    this.tagsNode = null;
  }

  public NotePositioned (RecordDefinition recDef) {
    this.note = new Note(recDef);
    this.index = -1;
    this.tagsNode = null;
  }

  public NotePositioned (Note note, int index) {
    this.note = note;
    this.index = index;
    this.tagsNode = note.getTagsNode();
    newNote = false;
  }

  public void setNote (Note note) {
    this.note = note;
    if (note != null && note.hasUniqueKey()) {
      newNote = false;
    }
  }

  public Note getNote () {
    return note;
  }

  /**
    Set the index to this note's location within 
    a sorted, filtered list of notes.
  
    @param index This note's location within a sorted, filtered list of notes.
  */
  public void setIndex (int index) {
    this.index = index;
  }

  public void incrementIndex (int increment) {
    this.index = index + increment;
  }

  public boolean hasValidIndex (NoteList notes) {
    return (index >= 0 && index < notes.size());
  }

  /**
    Get the index to this note's location within 
    a sorted, filtered list of notes.  
  
    @return This note's location within a sorted, filtered list of notes. 
  */
  public int getIndex () {
    return index;
  }

  public int getIndexForDisplay () {
    return (index + 1);
  }

  public void setTagsNode (TreeItem<TagsNodeValue> tagsNode) {
    this.tagsNode = tagsNode;
  }

  public TreeItem<TagsNodeValue> getTagsNode () {
    return tagsNode;
  }

  public void setNavigator (int navigator) {
    if (navigator == NAVIGATE_USING_TREE) {
      this.navigator = NAVIGATE_USING_TREE;
    } else {
      this.navigator = NAVIGATE_USING_LIST;
    }
  }

  public void setNavigatorToList (boolean useList) {
    this.navigator =
        useList ? NAVIGATE_USING_LIST : NAVIGATE_USING_TREE;
  }

  public int getNavigator () {
    return navigator;
  }

  public boolean navigateUsingList () {
    return (navigator == NAVIGATE_USING_LIST);
  }

  public boolean navigateUsingTree () {
    return (navigator == NAVIGATE_USING_TREE);
  }

  public void setNewNote (boolean newNote) {
    this.newNote = newNote;
  }

  public boolean isNewNote () {
    return newNote;
  }

}
