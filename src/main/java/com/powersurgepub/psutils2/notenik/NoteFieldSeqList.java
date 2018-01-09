/*
 * Copyright 2015 - 2016 Herb Bowie
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

  import java.util.*;

/**
 A list of note fields, maintained in the order in which they should be saved
 to disk. 

 @author Herb Bowie
 */
public class NoteFieldSeqList {
  
  private ArrayList<NoteFieldSeq> fields = new ArrayList<NoteFieldSeq>();
  
  public NoteFieldSeqList() {
    
  }
  
  /**
   Add all of a note's fields to the sequenced list. 
  
   @param note The note whose fields are to be added. 
  */
  public void addAll(Note note) {
    for (int i = 0; i < note.getNumberOfFields(); i++) {
      DataField nextField = note.getField(i);
      add(nextField.getDef().getCommonName(), i);
    }
  }
  
  /**
   Add another sequence entry to the list. 
  
   @param common The common name for the field. 
   @param fieldIndex The index position of the field within the note. 
  */
  public void add(CommonName common, int fieldIndex) {
    NoteFieldSeq seq = new NoteFieldSeq();
    seq.setNormalSeq(common);
    seq.setFieldIndex(fieldIndex);
    add(seq);
  }
  
  /**
   Add another sequence entry to the list. 
  
   @param seq A sequenced note field. 
  */
  public void add(NoteFieldSeq seq) {
    
    if (fields.isEmpty()) {
      fields.add(seq);
    }
    else
    if (fields.size() > 0
        && seq.compareTo(fields.get(fields.size() - 1)) > 0) {
      fields.add(seq);
    } else {
      int i = 0;
      int result = 1;
      while (i < fields.size() && result > 0) {
        NoteFieldSeq seq2 = fields.get(i);
        result = seq.compareTo(seq2);
        if (result == 0) {
          if (seq.getNormalSeq() == NoteParms.UNKNOWN_FIELD_SEQ) {
            fields.add(i, seq);
          } else {
            // Do nothing -- this shouldn't happen
          }
        }
        else
        if (result > 0) {
          i++;
        } else {
          fields.add(i, seq);
        }
      } // end while looking through existing list
      if (i >= fields.size()) {
        fields.add(seq);
      }
    } // end if latest addition doesn't belong at the end of the list
  } // end method add
  
  public DataField getField(Note note, int i) {
    return getDataField(note, i);
  }
  
  /**
   Get the next note data field in the proper sequence. 
  
   @param note The note we are working with. 
   @param i    The index into the sequenced list. 
  
   @return     The next note data field, in the proper sequence. 
  */
  public DataField getDataField(Note note, int i) {
    NoteFieldSeq seq = getNoteFieldSeq(i);
    return (note.getField(seq.getFieldIndex()));
  }
  
  /**
   Get the note field sequence from the position indicated by the index. 
  
   @param i A index into the internal list maintained by this class. 
  
   @return The note field sequence at the given index position. 
  */
  public NoteFieldSeq getNoteFieldSeq(int i) {
    return fields.get(i);
  }
  
  public int getNumberOfFields() {
    return size();
  }
  
  /**
   Get the number of sequence fields stored in the list. 
  
   @return The size of the list. 
  */
  public int size() {
    return fields.size();
  }

}
