/*
 * Copyright 2014 - 2017 Herb Bowie
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

package com.powersurgepub.psutils2.widgets;

  import javafx.scene.control.*;
  import javafx.scene.layout.*;

/**
 A check box widget.

 @author Herb Bowie
 */
public class CheckBoxWidget
    extends HBox
      implements
        DataWidget {

  private CheckBox                checkBox;

  private String                  selectedString = "true";
  private String                  unselectedString = "false";

  public CheckBoxWidget() {

    super();
    checkBox = new CheckBox();
    getChildren().add(checkBox);
  }

  public void setStringValues(String selected, String unselected) {
    selectedString = selected;
    unselectedString = unselected;
  }

  public boolean isSelected() { return checkBox.isSelected(); }

  public void setSelected(boolean sel) { checkBox.setSelected(sel); }

  /**
   Get the data entered by the user, represented as a String.

   @return A string representing data entered by the user.
   */
  public String getText() {
    if (checkBox.isSelected()) {
      return selectedString;
    } else {
      return unselectedString;
    }
  }

  /**
   Set the checkbox selection based on the text passed.

   @param t The string indicating selection status.
   */
  public void setText(String t) {
    String lowerCase = t.toLowerCase();
    char firstChar = ' ';
    if (t.length() > 0) {
      firstChar = lowerCase.charAt(0);
    }
    if (lowerCase.equalsIgnoreCase(selectedString)) {
      checkBox.setSelected(true);
    } else if (lowerCase.equalsIgnoreCase(unselectedString)) {
      checkBox.setSelected(false);
    } else if (firstChar == 't' || firstChar == 'y' || firstChar == '9' || firstChar == 'd') {
      checkBox.setSelected(true);
    } else if (firstChar == 'f' || firstChar == 'n' || firstChar == '0' || firstChar == 'o') {
      checkBox.setSelected(false);
    }
  }

}
