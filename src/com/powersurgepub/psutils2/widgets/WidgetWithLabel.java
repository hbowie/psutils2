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

  import javafx.scene.*;
  import javafx.scene.control.*;
  import javafx.scene.layout.*;

/**
 An object containing the ui components for a single data field: label and widget.

 @author Herb Bowie
 */
public class WidgetWithLabel {
  
  private Region        label = null;
  private DataWidget    widget = null;

  /**
   Default constructor.
  */
  public WidgetWithLabel() {
    
  }
  
  public void setLabel(Region label) {
    this.label = label;
  }
  
  public Region getLabel() {
    return label;
  }
  
  public void setWidget(DataWidget widget) {
    this.widget = widget;
  }
  
  public DataWidget getWidget() {
    return widget;
  }
}
