/* 
 * Copyright 2009 - 2017 Herb Bowie
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

  import com.powersurgepub.psutils2.files.*;
  import com.powersurgepub.psutils2.logging.*;
  import com.powersurgepub.psutils2.records.*;
	import com.powersurgepub.psutils2.ui.*;
  import com.powersurgepub.psutils2.values.*;
  import com.powersurgepub.psutils2.widgets.*;

  import java.io.*;
  import java.util.*;

 	import javafx.event.*;
  import javafx.scene.*;
  import javafx.scene.control.*;
 	import javafx.scene.layout.*;
  import javafx.scene.text.*;
  import javafx.stage.*;

/**
 A class for handling a template defining the fields used in a 
 Collection. 

 @author Herb Bowie
 */
public class NoteCollectionTemplate 
    implements 
      WindowToManage {
  
  private     File                collectionFolder;
  private     File                templateFile = null;
  
  private     FXUtils             fxUtils;
  private     Stage               templateStage;
  private     Scene               templateScene;
  private     GridPane            templatePane;
  private     Label               headingLabel;
  private     Label               collectionPathLabel;
  
  private     ArrayList<NoteCollectionField> fields;
  
  private     String              fileExt = "txt";
  
  private     boolean             quoteTemplate = false;
  
  private     Label               fileExtLabel;
  
  private     ComboBox<String>    fileExtComboBox;

  private     Button              okButton;
  private     Button              saveButton;
  
  private     TemplateFilter      templateFilter = new TemplateFilter();
  
  private     boolean             templateFound = false;
  private     NoteParms           templateParms = null;
  
  private     NoteCollectionField titleField 
      = new NoteCollectionField(NoteParms.TITLE_DEF);
  private     NoteCollectionField tagsField 
      = new NoteCollectionField(NoteParms.TAGS_DEF);
  private     NoteCollectionField linkField 
      = new NoteCollectionField(NoteParms.LINK_DEF);
  private     NoteCollectionField statusField 
      = new NoteCollectionField(NoteParms.STATUS_DEF);
  private     NoteCollectionField typeField 
      = new NoteCollectionField(NoteParms.TYPE_DEF);
  private     NoteCollectionField seqField 
      = new NoteCollectionField(NoteParms.SEQ_DEF);
  private     NoteCollectionField dateField 
      = new NoteCollectionField(NoteParms.DATE_DEF);
  private     NoteCollectionField recursField 
      = new NoteCollectionField(NoteParms.RECURS_DEF);
  private     NoteCollectionField authorField 
      = new NoteCollectionField(NoteParms.AUTHOR_DEF);
  private     NoteCollectionField ratingField 
      = new NoteCollectionField(NoteParms.RATING_DEF);
  private     NoteCollectionField indexField 
      = new NoteCollectionField(NoteParms.INDEX_DEF);
  private     NoteCollectionField codeField 
      = new NoteCollectionField(NoteParms.CODE_DEF);
  private     NoteCollectionField teaserField 
      = new NoteCollectionField(NoteParms.TEASER_DEF);
  private     NoteCollectionField bodyField 
      = new NoteCollectionField(NoteParms.BODY_DEF);
  private     NoteCollectionField dateAddedField
      = new NoteCollectionField (NoteParms.DATE_ADDED_DEF);
  
  /**
   A new Template object should be constructed for each new
   Collection. 
  
   @param collectionFolder The disk folder in which the Collection is
                           stored. 
  */
  public NoteCollectionTemplate(File collectionFolder) {
    this.collectionFolder = collectionFolder;
    buildListOfStandardFields();
    checkForTemplate();
    buildUI();
  }
  
  /**
   Build a new list of fields containing all the standard fields. 
  */
  private void buildListOfStandardFields() {
    fields = new ArrayList<>();
    fields.add(titleField);
    fields.add(tagsField);
    fields.add(linkField);
    fields.add(statusField);
    fields.add(typeField);
    fields.add(seqField);
    fields.add(dateField);
    fields.add(recursField);
    fields.add(authorField);
    fields.add(ratingField);
    fields.add(indexField);
    fields.add(codeField);
    fields.add(teaserField);
    fields.add(bodyField);
    fields.add(dateAddedField);
    
    titleField.setSelected(true);
    titleField.setDisable(true);
    
    bodyField.setSelected(true);
    bodyField.setDisable(true);
  }
  
  /**
   Check for the presence of a template file. 
  
   @return A set of note parameters for the collection, if a template was 
           found; otherwise null.
  */
  private void checkForTemplate() {
    templateFound = false;
    templateFile = null;
    boolean ok = true;
    templateParms = null;
    if (collectionFolder == null) {
      ok = false;
    }
    if (ok) {
      if ((! collectionFolder.exists())
          || (! collectionFolder.isDirectory())
          || (! collectionFolder.canRead())) {
        ok = false;
      }
    }
    String[] templates = null;
    if (ok) {
      templates = collectionFolder.list(templateFilter);
      if (templates == null || templates.length < 1) {
        ok = false;
      }
    }
    templateFile = null;
    FileName templateFileName = null;
    
    if (ok) {
      templateFile = new File(collectionFolder, templates[0]);
      templateFileName = new FileName(templateFile);
      String ext = templateFileName.getExt();
      if (ext.length() > 0) {
        fileExt = ext;
      }
      
      if (FileUtils.isGoodInputFile(templateFile)) {
        logNormal("Template file " + templateFile.toString() 
            + " found with following fields:");
      } else {
        ok = false;
      }
    }
    
    if (ok) {
      NoteIO templateIO = null;
      // Let the template fields define the record definition
      templateIO = new NoteIO(collectionFolder, NoteParms.NOTES_GENERAL_TYPE);
      templateIO.buildRecordDefinition(); 
      templateIO.setIsTemplate(true);
      try {
        Note templateNote = templateIO.getNote(templateFile, "");
        if (templateNote != null
            && templateIO.getRecDef().getNumberOfFields() > 0) {
          templateFound = true;
        }
      } catch (IOException e) {
        Logger.getShared().recordEvent(LogEvent.MEDIUM, 
            "I/O Error while attempting to read template file", false);
      }
      
      int highestFieldPosition = 0;
      boolean dateAddedExplicit = false;
      if (templateFound) {
        RecordDefinition recDef = templateIO.getRecDef();
        for (int i = 0; i < recDef.getNumberOfFields(); i++) {
          DataFieldDefinition fieldDef = recDef.getDef(i);
          if (fieldDef.getProperName().equalsIgnoreCase(NoteParms.AUTHOR_INFO)) {
            quoteTemplate = true;
          }
          if (fieldDef.getProperName().equalsIgnoreCase(NoteParms.DATE_ADDED_FIELD_NAME)) {
            dateAddedExplicit = true;
          }
          logNormal("  " + String.valueOf(i + 1) + ". " 
              + fieldDef.getProperName() + " ("
              + fieldDef.getCommonName() + ") type = " 
              + String.valueOf(fieldDef.getType()));
          
          int j = 0;
          boolean matched = false;
          NoteCollectionField nextField = null;
          while (j < fields.size()
              && (! matched)) {
            nextField = fields.get(j);
            matched = (fieldDef.equals(nextField.getDef()));
            if (! matched) {
              j++;
            }
          } // end searching for a match
          
          if (matched) {
            nextField.setDef(fieldDef);
            nextField.setSelected(true);
            highestFieldPosition = j;
          } else {
            NoteCollectionField customField = new NoteCollectionField(fieldDef);
            customField.setSelected(true);
            highestFieldPosition++;
            fields.add(highestFieldPosition, customField);
          }
          
        }
        templateParms = new NoteParms(NoteParms.DEFINED_TYPE);
        templateParms.setRecDef(templateIO.getRecDef());
        templateParms.setPreferredFileExt(fileExt);
        templateParms.setDateAddedExplicit(dateAddedExplicit);
        templateParms.setItemStatusConfig(templateIO.getNoteParms().getItemStatusConfig());
      } // end if template found
    }// end if ok
    if (! templateFound) {
      logNormal("Template file not found");
      tagsField.setSelected(true);
      linkField.setSelected(true);
    }
  }

  /**
   * Return the template file found, if any.
   * @return The template file found, or null if no template file found.
   */
  public File getTemplateFile() {
    if (templateFound) {
      return templateFile;
    } else {
      return null;
    }
  }
  
  /**
   Build the user interface
   */
	private void buildUI() {

    fxUtils = FXUtils.getShared();
    int rowCount = 0;

		templateStage = new Stage(StageStyle.UTILITY);
    templateStage.setTitle("Fields Template");
    templateStage.initModality(Modality.APPLICATION_MODAL);

		templatePane = new GridPane();
		fxUtils.applyStyle(templatePane);

		headingLabel = new Label("Field Options for");
		headingLabel.setTextAlignment(TextAlignment.CENTER);
		fxUtils.applyHeadingStyle(headingLabel);
		templatePane.add(headingLabel, 0, rowCount, 2, 1);
		headingLabel.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(headingLabel, Priority.ALWAYS);

		rowCount++;

		collectionPathLabel = new Label(collectionFolder.toString());
		templatePane.add(collectionPathLabel, 0, rowCount, 2, 1);
		collectionPathLabel.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(collectionPathLabel, Priority.ALWAYS);

		rowCount++;
    
    for (int i = 0; i < fields.size(); i++) {
      templatePane.add(fields.get(i).getCheckBox(), 0, rowCount, 2, 1);
      rowCount++;
    }
    
    fileExtLabel = new Label("Preferred File Extension:");
    templatePane.add(fileExtLabel, 0, rowCount, 2, 1);
    rowCount++;
    
    fileExtComboBox = new ComboBox<>();
    fileExtComboBox.getItems().addAll("txt", "md", "text", "markdown", "mdown", 
        "mkdown", "mdtext", "nnk", "notenik");
    fileExtComboBox.setEditable(true);
    fileExtComboBox.getSelectionModel().select(fileExt);
    templatePane.add(fileExtComboBox, 0, rowCount, 2, 1);
    rowCount++;

		okButton = new Button("OK");
    okButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        hideWindow();
		  } // end handle method
		}); // end event handler
		templatePane.add(okButton, 0, rowCount, 1, 1);
    
    saveButton = new Button("Save Template");
    saveButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent evt) {
        hideWindow();
        saveTemplate();
      }
    });
    templatePane.add(saveButton, 1, rowCount, 1, 1);
    
		rowCount++;
    
    templateScene = new Scene(templatePane);
    templateStage.setScene(templateScene);

  } // end method buildUI
  
  /**
   Generate a template file containing all supported note fields. 
  */
  public void saveTemplate() {
 
    RecordDefinition recDef = new RecordDefinition();
    for (NoteCollectionField nextField : fields) {
      if (nextField.isSelected()) {
        recDef.addColumn(nextField.getDef());
      }
    }

    Note templateNote = new Note(recDef);
    templateNote.setTitle("The unique title for this note");
    if (this.tagsField.isSelected()) {
      templateNote.setTags("One or more tags, separated by commas");
    }
    if (this.linkField.isSelected()) {
      templateNote.setLink("http://anyurl.com");
    }
    if (this.statusField.isSelected()) {
      templateNote.setStatus("One of a number of states");
    }
    if (this.typeField.isSelected()) {
      templateNote.setType("The type of note");
    }
    if (this.seqField.isSelected()) {
      templateNote.setSeq("Rev Letter or Version Number");
    }
    if (this.dateField.isSelected()) {
      StringDate today = new StringDate();
      today.set(StringDate.getTodayYMD());
      templateNote.setDate(today);
    }
    if (this.recursField.isSelected()) {
      templateNote.setRecurs("Every Week");
    }
    if (this.authorField.isSelected()) {
      templateNote.setAuthor("The Author of the Note");
    }
    if (this.ratingField.isSelected()) {
      templateNote.setRating("5");
    }
    if (this.indexField.isSelected()) {
      templateNote.setIndex("Index Term");
    }
    if (this.codeField.isSelected()) {
      templateNote.setCode("A block of programming code");
    }
    if (this.teaserField.isSelected()) {
      templateNote.setTeaser
        ("A brief sample of the note that will make people want to read more");
    }
    if (this.bodyField.isSelected()) {
      templateNote.setBody("The body of the note");
    }
    if (this.dateAddedField.isSelected()) {
      templateNote.setDateAdded(StringDate.getNowYMDHMS());
    }
    if (fileExtComboBox != null) {
      String selectedFileExt 
          = fileExtComboBox.getSelectionModel().getSelectedItem();
      if (selectedFileExt != null
          && selectedFileExt.length() > 0) {
        fileExt = selectedFileExt;
      }
    }
    File templateFile = new File(collectionFolder, "template." + fileExt);
    NoteIO templateIO = new NoteIO(collectionFolder);
    templateIO.save(templateNote, templateFile, true);
  }
  
  public NoteParms getNoteParms() {
    return templateParms;
  }
  
  public boolean isQuoteTemplate() {
    return quoteTemplate;
  }
  
  public void displayWindow() {
    WindowMenuManager.getShared().makeVisible(this);
  }
  
  public void hideWindow() {
    WindowMenuManager.getShared().hide(this);
  }
  
  public void logNormal (String msg) {
    Logger.getShared().recordEvent (LogEvent.NORMAL, msg, false);
  }
  
  public String getTitle() {
    return (templateStage.getTitle());
  }
  
  public void setVisible (boolean visible) {
    if (visible) {
      templateStage.show();
    } else {
      templateStage.hide();
    }
  }
  
  public void showAndWait() {
    templateStage.showAndWait();
  }
  
  public void toFront() {
    templateStage.toFront();
  }
  
  public double getWidth() {
    return templateStage.getWidth();
  }
  
  public double getHeight() {
    return templateStage.getHeight();
  }
  
  public void setLocation(double x, double y) {
    templateStage.setX(x);
    templateStage.setY(y);
  }

}
