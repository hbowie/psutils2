/*
 * Copyright 1999 - 2017 Herb Bowie
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

  import java.io.*;

  import javafx.scene.control.*;

/**
   One node on a tree of objects that is organized by category. <p>
  
   @author Herb Bowie (<a href="mailto:herb@powersurgepub.com">
           herb@powersurgepub.com</a>)<br>
           of PowerSurge Publishing 
           (<a href="http://www.powersurgepub.com">
           www.powersurgepub.com</a>)
  
 */
public class TagsNodeValue {
      
  private int             nodeType;
  public static final int   ROOT = 0;
  public static final int   TAG  = 1;
  public static final int   ITEM = 2;
  
  /** The next node for this taggable item. */
  private TreeItem<TagsNodeValue> nextNodeForItem = null;
  
  // Show items before categories with identical category parents?
  private boolean         itemsBeforeCategories = true;
  
  private Object          object = null;
  
  private int             tagIndex = 0;
  
  /**
   the number of levels above this node -- the distance from the root to 
   this node. If this node is the root, returns 0.
  */
  private int             treeLevel = 0;
  
  /** 
    Creates a root node. 
   */
  public TagsNodeValue(File source) {
    // super (source, true);
    nodeType = ROOT;
    object = source;
  }
  
  /** 
    Creates a category node. 
   */
  public TagsNodeValue (String category) {
    // super (category, true);
    nodeType = TAG;
    object = category;
  }
  
  /**
   Creates a node that stores a taggable item.

   @param tagged   The taggable item to be stored within the node.

   @param tagIndex An index pointing to the tag that is to be stored
                   with this node.
   */
  public TagsNodeValue (Taggable tagged, int tagIndex) {
    // super (tagged, false);
    nodeType = ITEM;
    this.tagIndex = tagIndex;
    object = tagged;
  }

  public int getTagIndex () {
    return tagIndex;
  }

  public void setSource (File source) {
    if (nodeType == ROOT) {
      this.setUserObject(source);
    }
  }
  
  public void setUserObject(Object object) {
    this.object = object;
  }
  
  public Object getUserObject() {
    return object;
  }
  
  public void setNextNodeForItem (TreeItem<TagsNodeValue> nextNodeForItem) {
    this.nextNodeForItem = nextNodeForItem;
  }
  
  public boolean hasNextNodeForItem () {
    return nextNodeForItem != null;
  }
  
  public TreeItem<TagsNodeValue> getNextNodeForItem () {
    return nextNodeForItem;
  }
  
  public int getNodeType() {
    return nodeType;
  }

  /**
   Get the requested level within the specified tag for this node.

   @param levelIndex An index pointing to the requested level within the tag
                     for this node.
   
   @return The requested level within the specified tag for this node, if an
           item node, otherwise an empty string.
   */
  public String getLevel (int levelIndex) {
    if (nodeType == ITEM) {
      if (levelIndex >= 0 && levelIndex < getLevels()) {
        return getTags().getLevel(tagIndex, levelIndex);
      } else {
        return "";
      }
    } else {
      return "";
    }
  }

  /**
   Returns the number of levels for the given tag for an item node.
   @return The number of levels for the given tag at the index
           specified for this node, for an item node; returns 0 for
           any other type of node.
   */
  public int getLevels () {
    if (nodeType == ITEM) {
      return getTags().getLevels (tagIndex);
    } else {
      return 0;
    }
  }

  /**
   Returns the tags associated with a taggable item for an item node type.
   @return The tags associated with the taggable item for an item node,
           returns null for other node types.
   */
  public Tags getTags () {
    if (nodeType == ITEM) {
      return getTaggable().getTags();
    } else {
      return null;
    }
  }

  /**
   Return the tags as a String, whether the node type is an item or tags.

   @return Tags as a string.
   */
  public String getTagsAsString () {
    if (nodeType == ITEM) {
      return ((Taggable)getUserObject()).getTags().toString();
    }
    else
    if (nodeType == TAG) {
      return getUserObject().toString();
      /*
      StringBuilder tags = new StringBuilder();
      TagsNodeValue tagNode = this;
      while (tagNode != null && tagNode.getNodeType() == TAG) {
        if (tags.length() > 0) {
          tags.insert(0, Tags.PREFERRED_LEVEL_SEPARATOR);
        }
        tags.insert(0, (String)tagNode.getUserObject());
        tagNode = (TagsNodeValue)tagNode.getParent();
      }
      return tags.toString(); */
    } else {
      return "";
    }
  }

  /**
   Returns the taggable item for an item node.

   @return The taggable item for an item node, returns null for other
           node types.
   */
  public Taggable getTaggable () {
    if (nodeType == ITEM) {
      return (Taggable)getUserObject();
    } else {
      return null;
    }
  }
  
  /**
   Sets the level of this node in the tree. 
  
   @param treeLevel The distance from the root to this node. 
  */
  public void setTreeLevel(int treeLevel) {
    this.treeLevel = treeLevel;
  }
  
  public void setChildLevel(int parentLevel) {
    this.treeLevel = parentLevel + 1;
  }
  
  public void setSiblingLevel(int siblingLevel) {
    this.treeLevel = siblingLevel;
  }
  
  public void setParentLevel(int childLevel) {
    this.treeLevel = childLevel - 1;
  }
  
  /**
   Gets the level of this node in the tree. 
  
   @return The distance from the root to this node. 
  */
  public int getTreeLevel() {
    return treeLevel;
  }
  
  /**
   Used for tree navigation. Since tag levels are numbered starting at 0, 
   and since the first level tags will reside just below the tree's root, 
   this method returns the level of the node, but adjusted so that it 
   will match the corresponding tags index. 
  
  @return the level of the node within the tree, where the root node is 
          considered to be at -1, and the first level with real keys
          is considered to be at level 0. This allows level
          to be used to pull the appropriate tag level out of the tags,
          where zero would be the first level. 
  */
  public int getTagsLevel() {
    return treeLevel - 1;
  }
  
  /**
   Sets the tree level of this node based on the tags level. Based on tags
   level numbering, the root node is at -1, whereas using tree level numbering,
   the root node is at 0. 
  
   @param tagsLevel One less than the tree level. 
  */
  public void setTagsLevel(int tagsLevel) {
    this.treeLevel = tagsLevel + 1;
  }

  public String toLongerString() {
    return ("Node type = " + String.valueOf (this.getNodeType())
        + " tree level = " + String.valueOf (this.getTreeLevel())
        + " " + toString());
  }
  
  public void display() {
    System.out.println("TagsNodeValue data display");
    System.out.println("  Node Type = " + String.valueOf(nodeType));
    if (nextNodeForItem == null) {
      System.out.println("  Next Node for Item = null");
    }
    System.out.println("  " + getUserObject().toString());
  }

  /**
   Returns the title of this node. This value is displayed in the Tree Model
   for the collection.

   @return Title of this node.
   */
  public String toString() {
    return (this.getUserObject().toString());
  }
  
}
