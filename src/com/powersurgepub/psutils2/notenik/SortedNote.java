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

/**
 
 @author Herb Bowie
 */
public class SortedNote {
  
  private Note        note;
  private SortedDate  sortedDate;
  private SortedSeq   sortedSeq;
  private int         stableListIndex = -1;
  
  public SortedNote(Note note, int stableListIndex) {
    this.note = note;
    sortedDate = new SortedDate(note.getDate());
    sortedSeq = new SortedSeq(note.getSeqValue());
    this.stableListIndex = stableListIndex;
  }
  
  public Note getNote() {
    return note;
  }
  
  public int getStableListIndex() {
    return stableListIndex;
  }
  
  /**
   Return an "X" to mark items that are done. 
   
   @return an X if item is completed or canceled, or a space if not done. 
  */
  public String getDone() {
    return note.getDone();
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
    return note.getTitle();
  }
  
  public String getSortKey (NoteSortParm parm) {
    return note.getSortKey(parm);
  }

}
