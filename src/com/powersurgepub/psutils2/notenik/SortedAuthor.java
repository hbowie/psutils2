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
 A wrapper around an Author, intended to be used in a TableView Column.

 @author Herb Bowie
 */
public class SortedAuthor
  implements
    Comparable {

  private Author author;

  public SortedAuthor(Author author) {
    this.author = author;
  }
  
  public int compareTo(Object obj2) {
    if (obj2 instanceof SortedAuthor) {
      SortedAuthor author2 = (SortedAuthor)obj2;
      return compareTo(author2);
    } else {
      return (getLastNameFirstForSort().compareTo(obj2.toString()));
    }
  }
  
  public int compareTo(SortedAuthor author2) {
    return (getLastNameFirstForSort().compareTo(author2.getLastNameFirstForSort()));
  }
  
  /**
   Get an author name to be used for sorting, with last names first.
  
   @return An author name to be used for sorting.
  */
  public String getLastNameFirstForSort() {
    if (author != null && author.hasData()) {
      return author.getCompleteNameLastNamesFirst();
    } else {
      return "";
    }
  }
  
  public String toString() {
    if (author != null && author.hasData()) {
      return author.toString();
    } else {
      return "";
    }
  }

}
