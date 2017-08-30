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

  import com.powersurgepub.psutils2.basic.*;
  import com.powersurgepub.psutils2.logging.*;

  import java.io.*;

  import javafx.scene.control.*;
  import javafx.scene.control.cell.*;

/**
  This class creates and maintains a TreeView of a list of Taggable items. <p>

  Note the following relationships between various classes involved in 
  producing a JavaFX TreeView of a taggable list. 

 TagsView creates the TreeView. It consists of TreeItem objects. 
 TagsNodeValue contains the tags data embedded in every TreeItem. There
    are three different types of TagsNodeValue objects. The rootNode points 
    to a source file where the taggable objects are stored on disk. Branch
    nodes contain tags. Leaf nodes actually contain taggable items. 
 Taggable identifies each taggable item. 

 @author Herb Bowie
 */
public class TagsView {
  
  public static final int         BELOW_THIS_NODE = 10;
  public static final int         ABOVE_THIS_NODE = -10;
  public static final int         AFTER_THIS_NODE = 1;
  public static final int         BEFORE_THIS_NODE = -1;
  public static final int         EQUALS_THIS_NODE = 0;
  
  private TreeView<TagsNodeValue> tagsView;
  
  private TreeItem<TagsNodeValue> rootNode;
  
  private TagsNodeValue           rootValue;
  
    // Show items before categories with identical category parents?
  private boolean                 itemsBeforeCategories = true;
  
  private TreeItem<TagsNodeValue> currentNode = null;
  private TreeItem<TagsNodeValue> nextNode = null;
  private TreeItem<TagsNodeValue> priorNode = null;
  
  public TagsView() {
    initTreeWithNoSource();
  }
  
  public TagsView(File source) {
    if (source == null) {
      initTreeWithNoSource();
    } else {    
      rootValue = new TagsNodeValue(source);
      rootNode = new TreeItem(rootValue);
      tagsView = new TreeView<TagsNodeValue>(rootNode);
    }
  }
  
  private void initTreeWithNoSource() {
    File unknown = new File (System.getProperty (GlobalConstants.USER_DIR), "???");
    rootValue = new TagsNodeValue(unknown);
    rootNode = new TreeItem(rootValue);
    tagsView = new TreeView<TagsNodeValue>(rootNode);
  }
  
  /**
   Set the data source, stored in the rootNode node of the tree. 
  
   @param source The file or folder from which the data is taken. 
  */
  public void setSource (File source) {
    rootValue.setSource(source);
  }

  /**
   Get the data source, stored in the rootNode node of the tree. 
  
   @return The file or folder from which the data is taken. 
  */
  public File getSource () {
    return (File)rootNode.getValue().getUserObject();
  }
  
  public TreeView getTreeView() {
    return tagsView;
  }
  
  /**
    Make sure this view is sorted in the proper sequence
    and contains only selected items.
   */
  public void sort (TaggableList list) {
    // Make sure each tagged is selected and in correct location
    for (int i = 0; i < list.size(); i++) {
      modify (list.get(i));
    }
  }
  
  /**
   *    Process a new Taggable that has just been modified 
   *    within the Taggables collection.
   *   
   *    @param tagged   Taggable just modified.
   */
  public void modify(Taggable tagged) {
    remove (tagged);
    add (tagged);
  }
  
  /**
   *    Process a taggable item to be deleted from the collection.
   *   
   *    @param tagged   Item to be deleted.
   */
  public void remove (Taggable tagged) {
    System.out.println("TagsView.remove");
    if (tagged == null) {
      System.out.println("  - tagged is null");
    }
    // selectItem (tagged);
    TreeItem<TagsNodeValue> treeNode = tagged.getTagsNode();
    if (treeNode == null) {
      System.out.println("  - tags node is null");
    }
    TagsNodeValue nodeNext = null;
    tagged.setTagsNode (null);
    while (treeNode != null) { 
      TagsNodeValue treeNodeValue = treeNode.getValue();
      TreeItem<TagsNodeValue> nextNode = treeNodeValue.getNextNodeForItem();
      TagsNodeValue nextNodeValue = nextNode.getValue();
      TreeItem<TagsNodeValue> parentNode = treeNode.getParent();
      if (parentNode != null) {
        parentNode.getChildren().remove(treeNode);
      }
      treeNode = nextNode;
    }
  }

  /**
   Add a taggable item to the tree model.

   @param tagged The taggable item to be added. 
   */
  public void add(Taggable tagged) {

    Tags tags = tagged.getTags();
    // System.out.println("TagsView.add tags to be added = " + tags.toString());
    TagsIterator iterator = new TagsIterator(tags);
    
    TreeItem<TagsNodeValue> lastNode = null;
    int tagIndex = 0;
    int nodesStored = 0;
    
    // Repeat for each tag
    while (iterator.hasNextTag() || nodesStored < 1) {
      nodesStored++;
      String nextTag = "";;
      if (iterator.hasNextTag()) {
        nextTag = iterator.nextTag();
      }
      // System.out.println ("  nextTag = " + nextTag);
      TagsNodeValue valueToAdd = new TagsNodeValue (tagged, tagIndex);
      TreeItem<TagsNodeValue> nodeToAdd = new TreeItem<TagsNodeValue>(valueToAdd);
      // System.out.println ("    TagsModel add " + String.valueOf(tagIndex)
      //     + " from " + tagged.getTags().toString());

      // Store this node so we can find it later by its index
      if (tagIndex == 0) {
        tagged.setTagsNode (nodeToAdd);
      } else {
        lastNode.getValue().setNextNodeForItem (nodeToAdd);
      }
      lastNode = nodeToAdd;

      // Now store it in the tree
      TreeItem<TagsNodeValue> currentNode = rootNode;
      TreeItem<TagsNodeValue> parentNode = rootNode;
      
      // levels = the number of levels in the new tagged item's tags
      int levels = tags.getLevels (tagIndex);
      // System.out.println ("      levels = " + String.valueOf(levels));
      boolean done = false;
      int compass = BELOW_THIS_NODE;
      // level is used to keep track of our current depth as we walk
      // through the tree. Note that the rootNode level is considered -1, and
      // the first level with real keys is considered 0. This allows level
      // to be used to pull the appropriate tag level out of the tags,
      // where zero would be the first level. 
      int level = -1;
      // child is used to keep track of our position within the
      // current set of children we are traversing.
      int child = 0;
      // Walk through the tree until we find the right location
      // for the tagged item to be added
      do {
        compass = compareNodes(nodeToAdd, currentNode);
        /* if (currentNode == null) {
            System.out.println ("        compass = " + String.valueOf(compass)
              + " for null node");
        } else {
          System.out.println ("        compass = " + String.valueOf(compass)
              + " for node type " + currentNode.toString());
        } */
        TreeItem<TagsNodeValue> newNode;
        switch (compass) {
          case (BELOW_THIS_NODE):
            level++;
            child = 0;
            if (currentNode.getChildren().size() > 0) {
              // System.out.println("          Branch 1: Below with children");
              // New tagged item should be below this one, and children already exist
              newNode = currentNode.getChildren().get(0);
              parentNode = currentNode;
              currentNode = newNode;
              // level++;
              // child = 0;
            } else {
              // New tagged should be below this one, but no children yet exist
              // level++;
              if (levels > level) {
                // Not yet at desired depth -- create a new tags node
                // System.out.println("          Branch 2: Adding new tags node");
                String levelCat = tags.getLevel (tagIndex, level);
                // System.out.println ("          Adding new tags node for tag "
                //     + String.valueOf(tagIndex)
                //     + ", level " + String.valueOf(level)
                //     + ": " + levelCat);
                TagsNodeValue newValue = new TagsNodeValue(levelCat);
                newNode = new TreeItem<TagsNodeValue>(newValue);
                // newNode = new TagsNodeValue (levelCat);
              } else {
                // we're at desired depth -- add the new tagged node
                // System.out.println("          Branch 3: Below with children");
                newNode = nodeToAdd;
                done = true;
              }
              newNode.getValue().setTagsLevel(level);
              currentNode.getChildren().add(newNode);
              // System.out.println ("          Adding new node " 
              //     + newNode.getValue().toLongerString());
              // System.out.println ("            Done? " + String.valueOf (done));
             
              parentNode = currentNode;
              currentNode = newNode;
              // level++;
              // child = 0;
            } // end go below but no children
            break;
          case (AFTER_THIS_NODE):
            // new node should be after this one -- keep going
            // System.out.println("          Branch 4: After current node");
            newNode = currentNode.nextSibling();
            currentNode = newNode;
            child++;
            break;
          case (BEFORE_THIS_NODE):
            // Don't go any farther -- put it here or add a new tags
            if (levels > level) {
              // System.out.println("          Branch 5: Before and below");
              String levelCat = tags.getLevel(tagIndex, level);
              TagsNodeValue newValue = new TagsNodeValue(levelCat);
              newNode = new TreeItem<TagsNodeValue>(newValue);
            } else {
              // System.out.println("          Branch 6: Adding new node here");
              newNode = nodeToAdd;
              done = true;
            }
            newNode.getValue().setTagsLevel(level);
            parentNode.getChildren().add(child, newNode);
            // System.out.println ("          Adding new node " +newNode.toString());
            // System.out.println ("            Done? " + String.valueOf (done));
            currentNode = newNode;
            break;
          case (EQUALS_THIS_NODE):
            // System.out.println("          Branch 7: Equals current node, adding here");
            newNode = nodeToAdd;
            newNode.getValue().setTagsLevel(level);
            parentNode.getChildren().add(child, newNode);
            currentNode = newNode;
            done = true;
            // System.out.println ("          Adding new node " +newNode.toString());
            // System.out.println ("            Done? " + String.valueOf (done));
            break;
          default:
            Logger.getShared().recordEvent (LogEvent.MAJOR,
                "Tags Model add -- hit default in switch", false);
            Logger.getShared().recordEvent (LogEvent.NORMAL, "Compass value = "
                + String.valueOf (compass), false);
            Logger.getShared().recordEvent (LogEvent.NORMAL, "itemNode = "
                + String.valueOf (nodeToAdd.getValue().getNodeType())  + " - "
                + nodeToAdd.getValue().toString(), false);
            Logger.getShared().recordEvent (LogEvent.NORMAL, "Current node = "
                + String.valueOf (currentNode.getValue().getNodeType())  + " - "
                + currentNode.toString(), false);
                break;
        } // end of compass result switch
      } while (! done);
      // setNextPrior();
      tagIndex++;
    } // end for each category assigned to tagged
  }
  
  /**
    Compare two tags nodes and determine their relative locations
    in the tree. Note that this method is only expected to be used
    on item nodes. 
   
    @return Value indicating relationship of the new node (this node) to the
            current node location in the tree. 
            ABOVE_THIS_NODE = The new node should be higher in the tree than
                              the current node.
            BELOW_THIS_NODE = The new node should be below the
                              current node. 
            AFTER_THIS_NODE = The new node should be at the same level as this node,
                              but come after it.
            BEFORE_THIS_NODE = The new node should be at the same level as this
                               node, but come before it.
            EQUALS_THIS_NODE = The two nodes have equal keys. 
  
    @param node1 An item to be added to the tree. Should always be an 
                 item (aka leaf) node. 
   
    @param node2 Node already in the tree, to be compared to this one. May be 
                 null, or any kind of node (root, branch, or leaf).

   */
  public int compareNodes (
      TreeItem<TagsNodeValue> node1, 
      TreeItem<TagsNodeValue> node2) {
    
    if (node1 == null) {
      throw new IllegalArgumentException
        ("First node to be compared is null");
    }
    else
    if (node1.getValue().getNodeType() != TagsNodeValue.ITEM) {
      throw new IllegalArgumentException
        ("First node to be compared does not contain an Item");
    }
    
    TagsNodeValue value1 = node1.getValue();
    
    int levels1 = value1.getLevels();
    /*
    if (node2 == null) {
      System.out.println ("Comparing " 
          + String.valueOf (node1.getValue().getNodeType())
          + "-" + node1.getValue().toString()
          +"(" + String.valueOf(levels1)
          + ") to null");
    } else {
      System.out.println ("Comparing " 
          + String.valueOf (node1.getValue().getNodeType())
          + "-" + node1.getValue().toString()
          +" (" + String.valueOf(levels1)
          + ") to " + String.valueOf (node2.getValue().getNodeType())
          + "-" + node2.getValue().toString()
          + " (" + String.valueOf (node2.getValue().getLevels()) + ")");
    }
    */
    
    int result = 0;
    if (node2 == null) {
      result = BEFORE_THIS_NODE;
    } else {
      TagsNodeValue value2 = node2.getValue();
      int levels2 = value2.getLevels();
      int treeLevel2 = value2.getTreeLevel();
      // Since this new node is an item, it should go below its category node, hence
      // at its category level + 1.
      int treeLevel1 = levels1 + 1;
      int type2 = value2.getNodeType();
      switch (type2) {
        case (TagsNodeValue.ROOT):
          // we're at the top of the tree -- everything goes below this
          result = BELOW_THIS_NODE;
          break;
        case (TagsNodeValue.TAG):
          if (treeLevel2 > treeLevel1) {
            // Since this new node is an item, it should go below its category node, hence
            // at its category level + 1. If the category node we are now on is at a lower
            // level, then we need to go back up the tree to find the right position
            // for this new item. 
            result = ABOVE_THIS_NODE;
          }
          else
          if (treeLevel1 == treeLevel2) { 
            // If the category node we are now on is at the same level
            // as the item we are trying to place, then this item should go
            // after it (if items go after sub-categories at the same level).
            if (itemsBeforeCategories) {
              result = BEFORE_THIS_NODE; 
            } else {
              result = AFTER_THIS_NODE;
            }
          }
          else {
            // We are currently on a category node with a number of levels
            // less than or equal to the number of levels of the item
            // we are trying to place. 
            String levelTag1 = value1.getLevel (treeLevel2 - 1);
            /*
            System.out.println ("levelTag1 for "
                + node1.getValue().getTags().toString()
                + " @ " + String.valueOf(node1.getValue().getTagIndex())
                + ", " + String.valueOf(treeLevel2 - 1)
                + " = " + levelTag1);
            */
            
            result = levelTag1.compareToIgnoreCase(value2.toString());
            if (result > 0) {
              // New item's sub-category at this level is greater than
              // this category node's sub-category value. 
              result = AFTER_THIS_NODE;
            } 
            else
            if (result < 0) {
              // New item's sub-category at this level is less than
              // this category node's sub-category value. 
              result = BEFORE_THIS_NODE;
            } else {
              // Sub-categories at this level are equal, so item
              // goes below the matching category node. 
              result = BELOW_THIS_NODE;
            }
          }
          break;
        case (TagsNodeValue.ITEM):
          if (treeLevel2 > treeLevel1) {
            // if this item is at a higher (deeper) level than the one
            // that this new one should go at, then we need to go back
            // up the tree. 
            result = ABOVE_THIS_NODE;
          }
          else
          if (treeLevel2 == treeLevel1) { 
            // Item should go at this level
            result = value1.getTaggable().compareTo (value2.getTaggable());
            if (result > 0) {
              // New item sorts after the current item we are on
              result = AFTER_THIS_NODE;
            } 
            else
            if (result < 0) {
              // New item sorts before the current item we are on
              result = BEFORE_THIS_NODE;
            } else {
              // New item has an equal key with the current item we are on:
              // put the new item first. 
              result = BEFORE_THIS_NODE;
            }
          } else {
            // if this item is at a lower (shallower) level than the level
            // that this new one should go at, then put the new item
            // before this node.  
            if (itemsBeforeCategories) {
              result = AFTER_THIS_NODE;
            } else {
              result = BEFORE_THIS_NODE;
            }
          }
          break;
        default:
          // Unexpected node type -- should never arrive here
          break;
      }
    }
    // System.out.println ("compareToNode result = " + String.valueOf(result));
    return result;
  }
  
  public Taggable firstItem () {
    currentNode = getNextItem (rootNode, 1);
    if (currentNode.getValue().getNodeType() == TagsNodeValue.ITEM) {
      setNextPrior();
      return ((Taggable)currentNode.getValue().getUserObject());
    } else {
      return null;
    }
  }

  /**
   Get the first node in the tree. 
  
   @return the first node in the tree. 
  */
  public TreeItem<TagsNodeValue> firstNode () {
    currentNode = getNextNode(rootNode);
    setNextPrior();
    return currentNode;
  }
  
  public Taggable lastItem () {
    currentNode = getNextItem (rootNode, -1);
    if (currentNode.getValue().getNodeType() == TagsNodeValue.ITEM) {
      setNextPrior();
      return ((Taggable)currentNode.getValue().getUserObject());
    } else {
      return null;
    }
  }
  
  public Taggable nextItem () {
    if (nextNode.getValue().getNodeType() == TagsNodeValue.ITEM) {
      currentNode = nextNode;
      setNextPrior();
      return ((Taggable)currentNode.getValue().getUserObject());
    } else {
      return null;
    }
  }

  public Taggable priorItem () {
    if (priorNode.getValue().getNodeType() == TagsNodeValue.ITEM) {
      currentNode = priorNode;
      setNextPrior();
      return ((Taggable)currentNode.getValue().getUserObject());
    } else {
      return null;
    }
  }

  public TreeItem<TagsNodeValue> firstItemNode () {
    TreeItem<TagsNodeValue> desiredNode = getNextItem (rootNode, 1);
    if (desiredNode != null
        && desiredNode.getValue().getNodeType() == TagsNodeValue.ITEM) {
      return desiredNode;
    } else {
      return null;
    }
  }
  
  public TreeItem<TagsNodeValue> lastItemNode () {
    TreeItem<TagsNodeValue> desiredNode = getNextItem (rootNode, -1);
    if (desiredNode.getValue().getNodeType() == TagsNodeValue.ITEM) {
      return desiredNode;
    } else {
      return null;
    }
  }

  public TreeItem<TagsNodeValue> nextItemNode 
      (TreeItem<TagsNodeValue> startingNode) {
    TreeItem<TagsNodeValue> desiredNode = getNextItem (startingNode, +1);
    if (desiredNode.getValue().getNodeType() == TagsNodeValue.ITEM) {
      return desiredNode;
    } else {
      return null;
    }
  }

  public TreeItem<TagsNodeValue> priorItemNode 
      (TreeItem<TagsNodeValue> startingNode) {
    TreeItem<TagsNodeValue> desiredNode = getNextItem (startingNode, -1);
    if (desiredNode.getValue().getNodeType() == TagsNodeValue.ITEM) {
      return desiredNode;
    } else {
      return null;
    }
  }
  
  /*
   private void checkCurrentNode (Taggable tagged) {
    if (currentNode == null) {
      selectItem (tagged);
    }
  } */
  
  public void selectItem(Taggable tagged) {
    currentNode = tagged.getTagsNode();
    setNextPrior();
  }
  
  private void setNextPrior () {
    if (currentNode != null) {
      nextNode = getNextItem (currentNode, 1);
      priorNode = getNextItem (currentNode, -1);
    }
  }

  /**
   Gets the next/prior item.
   @param startNode The reference node from which we're starting.
   @param increment A positive number will get the next node; a negative
                    number will get the prior node. Tag nodes are skipped. 
   @return          The next/prior item node.
   */
  private TreeItem<TagsNodeValue> getNextItem 
      (TreeItem<TagsNodeValue> startNode, int increment) {
    TreeItem<TagsNodeValue> currNode = startNode;
    TreeItem<TagsNodeValue> nextNode;
    boolean childrenExhausted = false;
    // Keep grabbing the next node until we have one that is an Item
    // or the Root (not a Category).
    do {
      if ((currNode.getValue().getNodeType() == TagsNodeValue.ITEM)
          || (childrenExhausted)
          || (currNode.getChildren().size() == 0)) {
        if (increment >= 0) {
          nextNode = currNode.nextSibling();
        } else {
          nextNode = currNode.previousSibling();
        }
        if (nextNode == null) {
          nextNode = currNode.getParent();
          if (nextNode == null) {
            Logger.getShared().recordEvent (LogEvent.MAJOR, "Current node "
                + String.valueOf (currNode.getValue().getNodeType())  + " - "
                + currNode.toString()
                + " has no parent!", false);
          }
          childrenExhausted = true;
        } else {
          childrenExhausted = false;
        }
      } else {
        // look for children 
        if (increment >= 0) {
          nextNode = currNode.getChildren().get(0);
        } else {
          nextNode = currNode.getChildren().get(currNode.getChildren().size() - 1);
        }
      }
      if (nextNode == null) {
        Logger.getShared().recordEvent (LogEvent.MAJOR,
            "TagsModel  getNextItem -- null tags node",
            false);
        Logger.getShared().recordEvent (LogEvent.NORMAL, "Starting node = "
            + String.valueOf (startNode.getValue().getNodeType())  + " - "
            + startNode.toString(), false);
        Logger.getShared().recordEvent (LogEvent.NORMAL, "Current node = "
            + String.valueOf (currNode.getValue().getNodeType())  + " - "
            + currNode.toString(), false);
      }
      currNode = nextNode;
    } while (currNode != null
        && currNode.getValue().getNodeType() == TagsNodeValue.TAG);
    return currNode;
  } // end method

  /**
   Get the rootNode of the tree.

   @return The rootNode node.
   */
  public TagsNodeValue getRootValue() {
    return rootValue;
  }
  
  public TreeItem<TagsNodeValue> getRootNode() {
    return rootNode;
  }

  /**
   Get the next node following the current one, in a depth-first progression
   through the tree. Will ensure that the treeLevel property is accurately set. 

   @param startingNode The node we're starting with.
   @return The next node, or null if we've traversed the entire tree.
   */
  public TreeItem<TagsNodeValue> getNextNode 
    (TreeItem<TagsNodeValue> startingNode) {

    boolean noMoreSiblings = true;
    TreeItem<TagsNodeValue> currNode = startingNode;
    TreeItem<TagsNodeValue> nextUp = null;
    int level = currNode.getValue().getTreeLevel();

    if (currNode.getChildren().size() > 0) {
      nextUp = currNode.getChildren().get(0);
      nextUp.getValue().setChildLevel(level);
    } else {
      nextUp = currNode.nextSibling();
      if (nextUp != null) {
        nextUp.getValue().setSiblingLevel(level);
      }
      noMoreSiblings = (nextUp == null);
      while (level > 0 && noMoreSiblings) {
        nextUp = currNode.getParent();
        if (nextUp != null) {
          nextUp.getValue().setParentLevel(currNode.getValue().getTreeLevel());
        }
        currNode = nextUp;
        level = currNode.getValue().getTreeLevel();
        nextUp = currNode.nextSibling();
        if (nextUp != null) {
          nextUp.getValue().setSiblingLevel(level);
        }
        noMoreSiblings = (nextUp == null);
      } // While looking for next sibling at a higher level
    } // end if node has no children
    return nextUp;
  }
  
	/**
	   Returns the object in string form.
	  
	   @return Name of this class.
	 */
	public String toString() {
    StringBuffer work = new StringBuffer();
    TreeItem<TagsNodeValue> onDeckCircle = getRootNode();
    TreeItem<TagsNodeValue> node = null;
    boolean noMoreSiblings = true;
    int level = 0;
    while (onDeckCircle != null) {
      node = onDeckCircle;

      // Add the current node's information to the concatenated output string
      level = node.getValue().getTreeLevel();
      for (int i = 0; i < level; i++) {
        work.append ("  ");
      }
      work.append (node.getValue().getUserObject().toString());
      work.append ("\n");

      onDeckCircle = getNextNode (node);
    } // end while more nodes
    return work.toString();
	}

}
