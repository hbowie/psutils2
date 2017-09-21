/*
 * Copyright 2017 - 2017 Herb Bowie
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

  import javafx.event.*;
  import javafx.scene.control.*;
  import javafx.scene.input.*;
  

/**
 A parameter to indicate how the user would like to see the notes sorted. 

 @author Herb Bowie
 */
public class NoteSortParm {
  
  public static final int SORT_BY_TITLE         = 0;
  public static final int SORT_BY_SEQ_AND_TITLE = 1;
  public static final int SORT_TASKS_BY_DATE    = 2;
  public static final int SORT_TASKS_BY_SEQ     = 3;
  
  private NoteCollectionModel model = null;
  
  private int parm = 0;
  
  private Menu sortMenu = null;
  
  private String[] labels = {
    "Title",
    "Seq + Title",
    "Tasks by Date",
    "Tasks by Seq"
  };
  
  private String[] keys = {
    "1",
    "2",
    "3",
    "4"
  };
  
  private int maxPositionsToLeftOfDecimal = 0;
  private int maxPositionsToRightOfDecimal = 0;
  
  public NoteSortParm() {

  }
  
  public void setParm(int parm) {

    if (parm < 0 || parm >= labels.length) {
      // Do nothing
    } else {
      this.parm = parm;
      if (sortMenu != null) {
        int i = 0;
        while (i < labels.length) {
          CheckMenuItem item = (CheckMenuItem)sortMenu.getItems().get(i);
          if (i == parm) {
            item.setSelected(true);
          } else {
            item.setSelected(false);
          }
          i++;
        } // end scan of possible parms
      }
      if (model != null) {
        model.sortParmChanged();
      }
    }
  }
  
  public void setModel(NoteCollectionModel model) {
    this.model = model;
    resetSeqStats();
  }
  
  public void resetSeqStats() {
    maxPositionsToLeftOfDecimal = 0;
    maxPositionsToRightOfDecimal = 0;
  }
  
  /**
   Keep track of longest string to left of the decimal and longest
   string to the right of the decimal. 
  
   @param seq A single Sequence Data Value. 
  
   @return True if stats were adjusted, false if no adjustment necessary.
  */
  public boolean maintainSeqStats(DataValueSeq seq) {
    boolean statsAdjusted = false;
    if (seq != null) {
      if (seq.getPositionsToLeftOfDecimal() > maxPositionsToLeftOfDecimal) {
        statsAdjusted = true;
        maxPositionsToLeftOfDecimal = seq.getPositionsToLeftOfDecimal();
      }
      if (seq.getPositionsToRightOfDecimal() > maxPositionsToRightOfDecimal) {
        statsAdjusted = true;
        maxPositionsToRightOfDecimal = seq.getPositionsToRightOfDecimal();
      }
    }
    return statsAdjusted;
  }
  
  public int getMaxPositionsToLeftOfDecimal() {
    return maxPositionsToLeftOfDecimal;
  }
  
  public int getMaxPositionsToRightOfDecimal() {
    return maxPositionsToRightOfDecimal;
  }
  
  public int getParm() {
    return parm;
  }
  
  public String getParmLabel() {
    return labels[parm];
  }
  
  /**
   Set the Sort sortMenu that will be used to pick one of the sort options. 
  
   @param sortMenu The sort sortMenu to be used. 
  */
  public void setSortMenu(Menu sortMenu) {
    this.sortMenu = sortMenu;
    int menuItems = this.sortMenu.getItems().size();
    if (menuItems > 0) {
      this.sortMenu.getItems().remove(0, menuItems);
    }
    for (int i = 0; i < labels.length; i++) {
      CheckMenuItem sortOption = new CheckMenuItem(labels[i]);
      KeyCombination kc
        = new KeyCharacterCombination(keys[i], KeyCombination.SHORTCUT_DOWN);
      sortOption.setAccelerator(kc);
      sortOption.setUserData(labels[i]);
      sortOption.setOnAction(new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent evt) {
          actionPerformed(evt);
        }
      });
      if (i == 0) {
        sortOption.setSelected(true);
      } else {
        sortOption.setSelected(false);
      }
      sortMenu.getItems().add(sortOption);
    }
    
  }
  
  /**
   The user selected a sort option.
  
   @param evt The Event object generated by the action. 
  */
  public void actionPerformed(ActionEvent evt) {

    int i = 0;
    while (i < labels.length) {
      CheckMenuItem item = (CheckMenuItem)sortMenu.getItems().get(i);
      EventTarget target = evt.getTarget();
      if (target instanceof CheckMenuItem) {
        CheckMenuItem targetItem = (CheckMenuItem)target;
        String targetText = targetItem.getText();
        if (targetText.equals(labels[i])) {
          parm = i;
          item.setSelected(true);
        } else {
          item.setSelected(false);
        }
      }
      i++;
    } // end scan of possible parms
    if (model != null) {
      model.sortParmChanged();
    }
  } // end method actionPerformed
  
  /**
   Reset to default values, preparing for a new list. 
  */
  public void resetToDefaults() {
    parm = 0;
    if (sortMenu != null) {
      int i = 0;
      while (i < labels.length) {
        CheckMenuItem item = (CheckMenuItem)sortMenu.getItems().get(i);
        if (i == 0) {
          item.setSelected(true);
        } else {
          item.setSelected(false);
        }
        i++;
      }
    }
    model = null;
    maxPositionsToLeftOfDecimal = 0;
    maxPositionsToRightOfDecimal = 0;
  }

}
