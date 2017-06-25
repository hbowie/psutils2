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

  import javafx.scene.control.*;

/**
  An implementation of Taggable that can be used in testing.
 */
public class TaggedItem
    implements Comparable, Taggable {

  private String   title = "";
  private Tags     tags = new Tags();
  private TreeItem<TagsNodeValue> tagsNode = null;

  private TagsIterator iterator = new TagsIterator (tags);

  public TaggedItem () {

  }

  public TaggedItem (String title) {
    this.title = title;
  }

  public void merge (TaggedItem item2) {

    // Merge titles
    if (item2.getTitle().length() > getTitle().length()) {
      setTitle (item2.getTitle());
    }

    // Merge tags
    tags.merge (item2.getTags());

  }


  public String getKey () {
    return title;
  }

  public int compareTo (Object obj2) {
    int comparison = -1;
    if (obj2.getClass().getSimpleName().equals ("URLPlus")) {
      TaggedItem urlPlus2 = (TaggedItem)obj2;
      comparison = this.getKey().compareToIgnoreCase(urlPlus2.getKey());
    }
    return comparison;
  }

  public boolean equals (Object obj2) {
    return (obj2.getClass().getSimpleName().equals ("URLPlus")
        && this.toString().equalsIgnoreCase (obj2.toString()));
  }

  public void setTitle (String title) {
    this.title 
        = 
          (title.trim());
  }

  public String getTitle () {
    return title;
  }

  public boolean equalsTitle (String title2) {
    return title.equals (title2.trim());
  }

  public void setTags (String tagString) {
    tags.set 
          (tagString.trim());
  }

  public void flattenTags () {
    tags.flatten();
  }

  public void lowerCaseTags () {
    tags.makeLowerCase();
  }

  public Tags getTags () {
    return tags;
  }

  public String getTagsAsString () {
    return tags.toString();
  }

  public boolean equalsTags (String tags2) {
    return tags.toString().equals (tags2.trim());
  }

  /**
   Start iteration through the list of tagsCount assigned to this item.
   */
  public void startTagIteration () {
    iterator = new TagsIterator (tags);
  }

  public String nextWord () {
    return iterator.nextWord();
  }

  public boolean isEndOfTag () {
    return iterator.isEndOfTag();
  }

  public boolean hasNextWord () {
    return iterator.hasNextWord();
  }

  public boolean hasNextTag () {
    return iterator.hasNextTag();
  }

  public String nextTag () {
    return iterator.nextTag();
  }


  
  public void setTagsNode (TreeItem<TagsNodeValue> tagsNode) {
    this.tagsNode = tagsNode;
  }

  public TreeItem<TagsNodeValue> getTagsNode () {
    return tagsNode;
  }

  public String toString () {
    return title;
  }

}
