package com.powersurgepub.psutils2.notenik;

import com.powersurgepub.psutils2.ui.ValueList;
import com.powersurgepub.psutils2.values.Author;
import com.powersurgepub.psutils2.values.Work;
import javafx.scene.control.ComboBox;

public class WorkList
    extends ValueList {

  /** Creates a new instance of TagsList */
  public WorkList(ComboBox<String> comboBox) {
    super(comboBox);
  }

  public WorkList() {
    super();
  }

  public void add(Note note) {
    if (note.hasWorkTitle()) {
      Work work = note.getWork();
      registerValue (work.getTitle());
    }
  }

  public void modify(Note note) {
    if (note.hasWorkTitle()) {
      Work work = note.getWork();
      registerValue (work.getTitle());
    }
  }

  public void remove(Note note) {
    // No need to do anything
  }
}
