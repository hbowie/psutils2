/*
 * Copyright 2013 - 2017 Herb Bowie
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

  import com.powersurgepub.psutils2.env.*;
  import com.powersurgepub.psutils2.strings.*;
  import com.powersurgepub.psutils2.ui.*;

  import java.io.*;
  import java.net.*;

  import javafx.event.*;
  import javafx.geometry.*;
  import javafx.scene.control.*;
  import javafx.scene.layout.*;
  import javafx.stage.*;

/**
 A control that can serve as a label for a hyperlink, while also offering some
 associated capabilities made available via buttons

 @author Herb Bowie
 */
public class LinkLabel 
    extends GridPane {
  
  private ComboBox              linkComboBox = new ComboBox();
  private TextArea              linkText     = null;
  private LinkTweakerInterface  linkTweaker = null;
  private Window                window = null;
  
  /**
   Creates new form LinkLabel
   */
  public LinkLabel() {
    buildUI();
  }
  
  public LinkLabel (String labelText) {
    buildUI();
    setLabelText(labelText);
  }
  
  private void buildUI() {

    linkComboBox.getItems().addAll("Link:", "Tweak...", "Disk File...", "Launch..." );
    linkComboBox.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        linkComboBoxActionPerformed(evt);
      }
    });
    this.setPadding(new Insets(0, 0, 4, 4));
    add(linkComboBox, 0, 0, 1, 1);
  }
  
  public void setLabelText (String labelText) {
    linkComboBox.getItems().remove(0);
    linkComboBox.getItems().add(0, labelText);
    linkComboBox.getSelectionModel().select(0);
  }
  
  public String getLabelText() {
    String linkLabel = (String)linkComboBox.getItems().get(0);
    return linkLabel;
  }
  
  public void setLinkTextArea (TextArea linkText) {
    this.linkText = linkText;
  }
  
  public void setLinkTweaker (LinkTweakerInterface linkTweaker) {
    this.linkTweaker = linkTweaker;
  }
  
  public void setFrame (Window window) {
    this.window = window;
  }
  
  public void tweak() {
    if (linkText != null
        && linkText.getText().length() > 0
        && linkTweaker != null
        && linkTweaker instanceof WindowToManage) {
      linkTweaker.setLink(linkText.getText(), getLabelText());
      // linkTweaker.setLocation(
      //   linkComboBox.getX() + 60,
      //   this.getY() + 60);
      WindowToManage linkTweakerWindow = (WindowToManage)linkTweaker;
      WindowMenuManager.getShared().makeVisible(linkTweakerWindow);
    }
  }
  
  public void selectFromDisk() {
    if (linkText != null) {
      FileChooser chooser = new FileChooser ();
      chooser.setTitle ("Select File as URL");
      String syncFolderStr = null;
      File syncFolder = null;
      File homeDir = Home.getShared().getUserHome();
      if (homeDir != null) {
        chooser.setInitialDirectory (homeDir);
      }
      File result = chooser.showOpenDialog (window);
      if (result != null) {
        try {
          String webPage = result.toURI().toURL().toString();
          String tweaked = StringUtils.tweakAnyLink(webPage, false, false, false, "");
          linkText.setText (tweaked);
        } catch (MalformedURLException e) {
          // do nothing
        }
      }    
    }
  }
  
  public void launch() {
    if (linkText != null) {
      Home.getShared().openURL(linkText.getText());
    }
  }
  
  private void linkComboBoxActionPerformed(ActionEvent evt) {                                             

    int action = linkComboBox.getSelectionModel().getSelectedIndex();
    switch (action) {
      case 1: 
        tweak();
        break;
      case 2:
        selectFromDisk();
        break;
      case 3:
        launch();
        break;
      default:
        break;
    }
    linkComboBox.getSelectionModel().select(0);
  }

}
