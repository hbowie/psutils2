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

/**
 This is a reference from one index term to one page on which it is mentioned;

 @author Herb Bowie
 */
public class IndexPageRef {
  
  private IndexTerm term = null;
  private String    page = null;
  private String    anchor = null;
  
  public IndexPageRef() {
    
  }
  
  public IndexPageRef(IndexTerm term, String page, String anchor) {
    this.term = term;
    this.page = page;
    this.anchor = anchor;
  }
  
  public IndexTerm getTerm() {
    return term;
  }
  
  public void setTerm(IndexTerm term) {
    this.term = term;
  }
  
  public String getPage() {
    return page;
  }
  
  public void setPage(String page) {
    this.page = page;
  }
  
  public String getAnchor() {
    return anchor;
  }
  
  public void setAnchor(String anchor) {
    this.anchor = anchor;
  }
  
  /**
   Retrieve a string that can be used to sort a list of references for a term. 
  
   @return A string that can be used to sort a list of references for a term. 
  */
  public String getKey() {
    return (page.toLowerCase() + "#" + anchor.toLowerCase());
  }

}
