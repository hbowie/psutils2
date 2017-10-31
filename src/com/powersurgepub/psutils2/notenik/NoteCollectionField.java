/* 
 * Copyright 2017 Herb Bowie
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

  import com.powersurgepub.psutils2.records.*;

  import javafx.scene.control.*;

/**
 One potential field that might be used in a Note Collection. 

 @author Herb Bowie
 */
public class NoteCollectionField {
  
  private CheckBox            checkBox = new CheckBox();
  private DataFieldDefinition fieldDef;
  
  public NoteCollectionField(DataFieldDefinition fieldDef) {
    checkBox.setSelected(false);
    setDef(fieldDef);
  }
  
  public NoteCollectionField() {
    checkBox.setSelected(false);
  }
  
  public void setDef(DataFieldDefinition fieldDef) {
    this.fieldDef = fieldDef;
    checkBox.setText(fieldDef.getProperName());
  }
  
  public boolean equals(NoteCollectionField field2) {
    return fieldDef.equals(field2.getDef());
  }
  
  public DataFieldDefinition getDef() {
    return fieldDef;
  }
  
  public void setSelected(boolean selected) {
    checkBox.setSelected(selected);
  }
  
  public boolean isSelected() {
    return checkBox.isSelected();
  }
  
  public CheckBox getCheckBox() {
    return checkBox;
  }
  
  public void setDisable(boolean disable) {
    checkBox.setDisable(disable);
  }

}
