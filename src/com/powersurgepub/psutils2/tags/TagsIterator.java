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

package com.powersurgepub.psutils2.tags;

/**

 A class that can iterate through a Tags object in a number of useful ways.

 */
public class TagsIterator {

  private Tags tags;

  /** Work fields. */
  private int       tagIndex     = 0;
  private int       lastTagIndex = 0;
  private int       lastTagEnd   = 0;
  private boolean   endOfTag    = false;

  public TagsIterator (Tags tags) {
    this.tags = tags;
  }

  public void reset () {
    tagIndex = 0;
    lastTagIndex = 0;
    lastTagEnd = 0;
    endOfTag = false;
  }

  /**
   Return next word, terminated by any kind of separator.

   @return Next word, or empty string if no more words.
   */
  public String nextWord () {
    if (hasNextWord()) {
      int start = Tags.indexOfNextWordStart 
          (tags.getTags(), tagIndex, tags.isSlashToSeparate());
      int end   = Tags.indexOfNextSeparator 
          (tags.getTags(), start, true, true, tags.isSlashToSeparate());
      tagIndex = end + 1;
      endOfTag = (end >= tags.length() || Tags.isTagSeparator (tags.charAt(end)));
      return (tags.substring (start, end));
    } else {
      endOfTag = true;
      return "";
    }
  }

  /**
   Indicate whether the last word returned represented the last level
   in a single tag.

   @return True if last word was the end of a tag, false otherwise.
   */
  public boolean isEndOfTag () {
    return endOfTag;
  }

  /**
   Return the next complete tag (1 or more levels) within the tags string.

   @return The next complete tag.
   */
  public String nextTag () {
    if (hasNextTag()) {
      lastTagIndex 
          = Tags.indexOfNextWordStart 
              (tags.getTags(), tagIndex, tags.isSlashToSeparate());
      lastTagEnd 
          = Tags.indexOfNextSeparator 
              (tags.getTags(), lastTagIndex, false, true, tags.isSlashToSeparate());
      // lastTagIndex = tagIndex;
      tagIndex = lastTagEnd + 1;
      return (tags.substring (lastTagIndex, lastTagEnd));
    } else {
      return "";
    }
  }

  /**
   Remove the last tag returned via nextTag, depending on lastTagIndex and
   lastTagEnd.
   */
  public void removeTag () {
    tags.removeTag (lastTagIndex, lastTagEnd);
    tagIndex = lastTagIndex;
    lastTagEnd = lastTagIndex;
  }

  public boolean hasNextWord () {
    if (tags == null) {
      return false;
    } else {
      return (tagIndex < tags.length() && tagIndex >= 0);
    }
  }

  public boolean hasNextTag () {
    if (tags == null) {
      return false;
    } else {
      return (tagIndex < tags.length() && tagIndex >= 0);
    }
  }

}
