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

  import com.powersurgepub.psutils2.notenik.*;
  import com.powersurgepub.psutils2.values.*;

  import javafx.beans.property.*;

/**
 A note wrapped in fields needed to sort the note within a TableView. 

 @author Herb Bowie
 */
public class SortedNote {
  
  private Note        note;
  private SortedDate  sortedDate;
  private SortedSeq   sortedSeq;
  
  private SimpleStringProperty done;
  private SimpleStringProperty title;
  private SimpleStringProperty sortKey;
  
  public SortedNote(Note note, NoteSortParm sortParm) {
    this.note = note;
    done = new SimpleStringProperty(this, "", "Done");
    title = new SimpleStringProperty(this, "", "Title");
    sortKey = new SimpleStringProperty(this, "", "SortKey");
    genKeys(sortParm);
  }
  
  public void genKeys(NoteSortParm sortParm) {
    done.set(note.getDone());
    sortedDate = new SortedDate(note.getDate());
    sortedSeq = new SortedSeq(note.getSeqValue());
    title.set(note.getTitle());
    sortKey.set(note.getSortKey(sortParm));
  }
  
  public Note getNote() {
    return note;
  }
  
  public int getStableListIndex() {
    return note.getCollectionID();
  }
  
  /**
   Return an "X" to mark items that are done. 
   
   @return an X if item is completed or canceled, or a space if not done. 
  */
  public String getDone() {
    return done.get();
  }
  
  public SimpleStringProperty doneProperty() {
    return done;
  }
  
  /**
   Return date in dd MMM yyyy format.
  
   @return date in dd MMM yyyy format. 
  */
  public SortedDate getDate() {
    return sortedDate;
  }
  
  public SortedSeq getSeq() {
    return sortedSeq;
  }
  
  public String getTitle() {
    return title.get();
  }
  
  public SimpleStringProperty titleProperty() {
    return title;
  }
  
  public String getSortKey () {
    return sortKey.get();
  }
  
  public SimpleStringProperty sortKeyProperty() {
    return sortKey;
  }

}
