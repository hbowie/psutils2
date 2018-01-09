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
 
 @author Herb Bowie
 */
public class LinkWidget
    extends TextArea 
      implements
        DataWidget {

  /**
   Default constructor with no 
  */
  public LinkWidget() {
    this.setPrefColumnCount(60);
    this.setPrefRowCount(3);
    this.setWrapText(true);
  }
  
  public TextArea getTextArea() {
    return this;
  }

}
