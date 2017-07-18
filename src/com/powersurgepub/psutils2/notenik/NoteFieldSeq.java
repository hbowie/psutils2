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

  import com.powersurgepub.psutils2.strings.*;

/**
 An object used to arrange Note fields into their normal sequence. 

 @author Herb Bowie
 */
public class NoteFieldSeq 
    implements Comparable {
  
  private int normalSeq = -1;
  private int fieldIndex = -1;
  
  public NoteFieldSeq() {
    
  }

  public NoteFieldSeq(int normalSeq, int fieldIndex) {
    this.normalSeq = normalSeq;
    this.fieldIndex = fieldIndex;
  }
  
  public int compareTo(Object obj2) {
    if (obj2 instanceof NoteFieldSeq) {
      NoteFieldSeq seq2 = (NoteFieldSeq)obj2;
      if (normalSeq < seq2.getNormalSeq()) {
        return -1;
      }
      else
      if (normalSeq > seq2.getNormalSeq()) {
        return 1;
      }
      else
      if (fieldIndex < seq2.getFieldIndex()) {
        return -1;
      }
      else
      if (fieldIndex > seq2.getFieldIndex()) {
        return 1;
      } else {
        return 0;
      }
    } else {
      return 1;
    }
  }
  
  public void setNormalSeq(CommonName commonName) {
    this.normalSeq = NoteParms.getNormalSeq(commonName);
  }
  
  public void setNormalSeq(int normalSeq) {
    this.normalSeq = normalSeq;
  }
  
  public int getNormalSeq() {
    return normalSeq;
  }
  
  public void setFieldIndex(int fieldIndex) {
    this.fieldIndex = fieldIndex;
  }
  
  public int getFieldIndex() {
    return fieldIndex;
  }
}
