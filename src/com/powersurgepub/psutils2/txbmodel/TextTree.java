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

package com.powersurgepub.psutils2.txbmodel;

  import java.util.*;

  import javafx.scene.control.*;

/**
 This class represents a tree of text nodes. The class extends 
 DefaultTreeModel and leverages its basic tree structure. It may also be  
 used as a tree model for a JTree interface.

 @author Herb Bowie
 */
public class TextTree {
  
  private TreeView<TextData>        tree;
  
  private TextData                  rootData;
  
  private TreeItem<TextData>        root;
  
  private ArrayList<TocEntry>       tocEntries = new ArrayList();
  
  /** 
   Creates a new instance of TextTree. 
   */
  public TextTree (TextData rootData) {
    this.rootData = rootData;
    root = new TreeItem<>(rootData);
    tree = new TreeView<>(root);
  }
  
  public TextTree() {
    rootData = new TextData();
    root = new TreeItem<>(rootData);
    tree = new TreeView<>(root);
  }
  
  public TreeItem<TextData> createNode(String type, String text) {
    TextData nodeData;
    if (text == null) {
      nodeData = new TextData("");
    } else {
      nodeData = new TextData(text);
    }
    nodeData.setType(type);
    nodeData.setTree(this);
    TreeItem<TextData> node = new TreeItem<>(nodeData);
    nodeData.setNode(node);
    return node;
  }
  
  public TreeView<TextData> getTreeView() {
    return tree;
  }
  
  public TreeItem<TextData> getTextRoot () {
    return root;
  }
  
  public void addTocEntry (TocEntry tocEntry) {
    tocEntries.add(tocEntry);
  }
  
  public boolean hasTocEntries () {
    return (tocEntries.size() > 0);
  }
  
  public int getTocSize() {
    return tocEntries.size();
  }
  
  public TocEntry getTocEntry(int index) {
    if (index < 0 || index >= tocEntries.size()) {
      return null;
    } else {
      return tocEntries.get(index);
    }
  }

}
