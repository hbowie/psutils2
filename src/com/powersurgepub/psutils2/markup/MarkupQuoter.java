/*
 * MarkupQuoter.java
 *
 * Created on March 15, 2007, 6:08 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

/*
 * Copyright 1999 - 2013 Herb Bowie
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

package com.powersurgepub.psutils2.markup;

/**
 *
 * @author hbowie
 */
public class MarkupQuoter {
  
  private   boolean                 firstText = false;
  private   MarkupElement           lastText = null;
  private   boolean                 lastParagraph = false;
  
  /** Creates a new instance of MarkupQuoter */
  public MarkupQuoter() {
  }
  
  public void setFirstText (boolean firstText) {
    this.firstText = firstText;
  }
  
  public boolean isFirstText () {
    return firstText;
  }
  
  public void setLastText (MarkupElement lastText) {
    this.lastText = lastText;
  }
  
  public MarkupElement getLastText () {
    return lastText;
  }
  
  public void setLastParagraph (boolean lastParagraph) {
    this.lastParagraph = lastParagraph;
  }
  
  public boolean isLastParagraph () {
    return lastParagraph;
  }
  
}
