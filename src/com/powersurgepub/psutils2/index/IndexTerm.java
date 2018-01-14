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
 
 @author Herb Bowie
 */
public class IndexTerm {
  
  /** The term being indexed. */
  private String term;
  
  /** An optional link for the term. */
  private String link;
  
  private ArrayList<IndexPageRef> refs = new ArrayList<IndexPageRef>();
  
  public IndexTerm (String term) {
    this.term = term;
  }
  
  /**
   Store the term being indexed. 
  
   @param term The term being indexed. 
  */
  public void setTerm(String term) {
    this.term = term;
  }
  
  /**
   Get the term being indexed. 
  
   @return The term being indexed. 
  */
  public String getTerm() {
    return term;
  }
  
  /**
   Get the term in all lower-case letters (useful for sorting).
  
   @return The term in all lower-case letters. 
  */
  public String getTermLower() {
    return term.toLowerCase();
  }
  
  /**
   Return the key to be used for this term. 
  
   @return The key to be used for this term. 
  */
  public String getKey() {
    return getTermLower() + getTerm();
  }
  
  /**
   Return an optional link associated with this term. 
  
   @return An optional link associated with this term. 
  */
  public String getLink() {
    return link;
  }
  
  /**
   Set an optional link associated with this term.
  
   @param link An optional link associated with this term.
  */
  public void setLink(String link) {
    this.link = link;
  }
  
  public void addRef(IndexPageRef ref) {
    int i = 0;
    while (i < refs.size()
        && ref.getKey().compareTo(refs.get(i).getKey()) > 0) {
      i++;
    }
    if (i < refs.size()) {
      refs.add(i, ref);
    } else {
      refs.add(ref);
    }
  }
  
  public int getRefSize() {
    return refs.size();
  }
  
  public IndexPageRef getRef(int i) {
    return refs.get(i);
  }

}
