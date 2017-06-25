/*
 * Copyright 2016 - 2016 Herb Bowie
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

package com.powersurgepub.psutils2.index;

  import java.util.*;

/**
 This is a collection of index terms. 

 @author Herb Bowie
 */
public class IndexCollection {
  
  public final static char SEP        = ';';
  public final static char ANCHOR     = '#';
  public final static char LINK_START = '(';
  public final static char LINK_END   = ')';
  
  private SortedMap<String, IndexTerm> collection = new TreeMap<String, IndexTerm>();
  private Set       sortedSet;
  
  public IndexCollection() {
    
  }
  
  public void clear() {
    collection.clear();
  }
  
  public boolean containsKey(IndexTerm term) {
    return containsKey(term.getKey());
  }
  
  public boolean containsKey(String key) {
    return collection.containsKey(key);
  }
  
  public IndexTerm get(IndexTerm term) {
    return get(term.getKey());
  }
  
  public IndexTerm get(String key) {
    return (IndexTerm)collection.get(key);
  }
  
  public boolean isEmpty() {
    return collection.isEmpty();
  }
  
  public IndexTerm put (IndexTerm term) {
    IndexTerm returnTerm = (IndexTerm)collection.put(term.getKey(), term);
    return returnTerm;
  }
  
  /**
   Add another page value to the collection. 
  
   @param page  The page that has the value. 
   @param value The index value to be added. 
  */
  public void add(String page, IndexPageValue value) {
    
    int i = 0;
    while (i < value.length()) {
      i = nextTermEntry(page, value, i);
    }
  }
  
  /**
  Parse the page value, extracting the next term entry and adding it to 
  the collection. 
  
  @param page The page being indexed. 
  @param value The value(s) to be indexed.
  @param i The starting index to the next entry to be extracted. 
  
  @return A new value for the index being used to parse the value(s).
  */
  private int nextTermEntry(String page, IndexPageValue value, int i) {
    
    int j = i;
    int startTerm = j;
    int endTerm = -1;
    int startAnchor = -1;
    int endAnchor = -1;
    int startLink = -1;
    int endLink = -1;
    int parenDepth = 0;
    boolean endOfTerm = false;
    char c = ' ';
    while (! endOfTerm) {
      if (j < value.length()) {
        c = value.charAt(j);
      } else {
        c = SEP;
      }
      if (c == LINK_START) {
        if (parenDepth == 0) {
          startLink = j + 1;
          if (endTerm < 0) {
            endTerm = j - 1;
          }
          else
          if (startAnchor > 0 && endAnchor < 0) {
            endAnchor = j - 1;
          }
        }
        parenDepth++;
      }
      else
      if (c == LINK_END) {
        if (parenDepth == 1) {
          if (startLink > 0) {
            endLink = j - 1;
          }
        }
        parenDepth--;
      }
      else
      if (c == ANCHOR) {
        startAnchor = j + 1;
        if (endTerm < 0) {
          endTerm = j - 1;
        }
      }
      else
      if (c == SEP) {
        endOfTerm = true;
        if (endTerm < 0) {
          endTerm = j - 1;
        }
        else
        if (startAnchor > 0 && endAnchor < 0) {
          endAnchor = j - 1;
        }
        else
        if (startLink > 0 && endLink < 0) {
          endLink = j - 1;
        }
      }
      j++;
    }

    String term = subTrim(value, startTerm, endTerm);
    String link = subTrim(value, startLink, endLink);
    String anchor = subTrim(value, startAnchor, endAnchor);

    if (term != null && term.length() > 0) {
      IndexTerm newTerm = new IndexTerm(term);
      IndexTerm indexTerm = get(newTerm);
      if (indexTerm == null) {
        indexTerm = newTerm;
        put(newTerm);
      }
      
      if (link != null && link.length() > 0) {
        indexTerm.setLink(link);
      }
      
      IndexPageRef ref = new IndexPageRef(indexTerm, page, anchor);
      indexTerm.addRef(ref); 
    }

    return j;
  }
  
  private String subTrim(IndexPageValue value, int start, int end) {
    int s = start;
    int e = end;
    while (s < value.length()
        && Character.isWhitespace(value.charAt(s))) {
      s++;
    }
    while (e > 0
        && (e > value.length()
          || Character.isWhitespace(value.charAt(e)))) {
      e--;
    }
    if (s >= 0 && e >= s) {
      return value.substring(s, e + 1);
    } else {
      return "";
    }
  }
  
  public IndexTerm remove(IndexTerm term) {
    return remove (term.getKey());
  }
  
  public IndexTerm remove (String key) {
    return (IndexTerm)collection.remove(key);
  }
  
  public int size() {
    return collection.size();
  }
  
  public Iterator iterator() {
    sortedSet = collection.entrySet();
    return sortedSet.iterator();
  }

}
