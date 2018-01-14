/*
 * Copyright 2010 - 2017 Herb Bowie
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

  import com.powersurgepub.psutils2.values.*;

/**
 A wrapper around a DataValueSeq, intended to be used in a TableView Column. 

 @author Herb Bowie
 */
public class SortedSeq 
  implements
    Comparable {
  
  private DataValueSeq seq;
  
  public SortedSeq(DataValueSeq seq) {
    this.seq = seq;
  }
  
  public int compareTo(Object obj2) {
    if (obj2 instanceof SortedSeq) {
      SortedSeq seq2 = (SortedSeq)obj2;
      return compareTo(seq2);
    } else {
      return (getSeqForSort().compareTo(obj2.toString()));
    }
  }
  
  public int compareTo(SortedSeq date2) {
    return (getSeqForSort().compareTo(date2.getSeqForSort()));
  }
  
  /**
   Get a date to be used for sorting, with blank dates sorting after 
   non-blank dates. 
  
   @return A date string to be used for sorting. 
  */
  public String getSeqForSort() {
    if (seq != null && seq.hasData()) {
      return seq.toPaddedString('0', 8, '0', 4);
    } else {
      return " ";
    }
  }
  
  public String toString() {
    if (seq != null && seq.hasData()) {
      return seq.toString();
    } else {
      return "";
    }
  }

}
