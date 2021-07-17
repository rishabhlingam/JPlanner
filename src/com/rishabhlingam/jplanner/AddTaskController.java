package com.rishabhlingam.jplanner;

import com.rishabhlingam.jplanner.model.TaskData;
import com.rishabhlingam.jplanner.model.TaskItem;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.time.LocalDate;

public class AddTaskController {
    @FXML
    private TextField descriptionTextField;
    @FXML
    private TextArea detailsTextArea;
    @FXML
    private DatePicker dueDatePicker;

    public void setDescription(String description) {
        descriptionTextField.setText(description);
    }

    public void setDetails(String details) {
        detailsTextArea.setText(details);
    }

    public void setDueDatePicker(LocalDate localDate) {
        dueDatePicker.setValue(localDate);
    }

    public TaskItem processResult(){
        String description = descriptionTextField.getText().trim();
        String details = detailsTextArea.getText().trim();
        LocalDate durDate = dueDatePicker.getValue();
        TaskItem newTask = new TaskItem(description, details, durDate);
        TaskData.getInstance().addTask( newTask );
        return newTask;
    }
}
