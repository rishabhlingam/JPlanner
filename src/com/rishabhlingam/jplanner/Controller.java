package com.rishabhlingam.jplanner;

import com.rishabhlingam.jplanner.model.TaskData;
import com.rishabhlingam.jplanner.model.TaskItem;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.util.Callback;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class Controller {
    private List<TaskItem> tasks;
    @FXML
    private ListView taskListView;
    @FXML
    private TextArea taskDescriptionTextArea;
    @FXML
    private Label dueDateLabel;
    @FXML
    private BorderPane mainBorderPane;
    @FXML
    private ContextMenu contextMenu;
    @FXML
    private ToggleButton filterToggleButton;
    private FilteredList<TaskItem> filteredList;
    private Predicate<TaskItem> allTasks;
    private Predicate<TaskItem> todayTasks;

    public void initialize() {
        createContextMenu();

        taskListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TaskItem>() {
            @Override
            public void changed(ObservableValue<? extends TaskItem> observableValue, TaskItem oldValue, TaskItem newValue) {
                if(newValue != null){
                    TaskItem taskItem = (TaskItem) taskListView.getSelectionModel().getSelectedItem();

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy");
                    dueDateLabel.setText( "Due date: " + formatter.format(taskItem.getDeadline()) );
                    taskDescriptionTextArea.setText( taskItem.getDetails() );
                }
            }
        });

        allTasks = new Predicate<TaskItem>() {
            @Override
            public boolean test(TaskItem item) {
                return true;
            }
        };
        todayTasks =  new Predicate<TaskItem>() {
            @Override
            public boolean test(TaskItem item) {
                return item.getDeadline().equals(LocalDate.now());
            }
        };

        filteredList = new FilteredList<>(TaskData.getInstance().getTaskItems(), allTasks);

        SortedList<TaskItem> sortedList = new SortedList<>(filteredList, new Comparator<TaskItem>() {
            @Override
            public int compare(TaskItem o1, TaskItem o2) {
                return o1.getDeadline().compareTo( o2.getDeadline() );
            }
        });

        taskListView.setItems(sortedList);
        taskListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        taskListView.getSelectionModel().selectFirst();

        taskListView.setCellFactory(new Callback<ListView<TaskItem>, ListCell<TaskItem>>() {
            @Override
            public ListCell<TaskItem> call(ListView<TaskItem> param) {
                ListCell<TaskItem> cell = new ListCell<>() {
                    @Override
                    protected void updateItem(TaskItem item, boolean b) {
                        super.updateItem(item, b);
                        if(b){
                            setText(null);
                        } else {
                            setText(item.getDescription());
                            if(item.getDeadline().equals(LocalDate.now())){
                                setTextFill(Color.RED);
                            } else if(item.getDeadline().isBefore(LocalDate.now())){
                                setTextFill(Color.FIREBRICK);
                            }
                        }
                    }
                };

                cell.emptyProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> observableValue, Boolean wasEmpty, Boolean isEmpty) {
                        if(isEmpty){
                            cell.setContextMenu(null);
                        } else {
                            cell.setContextMenu(contextMenu);
                        }
                    }
                });
                return cell;
            }
        });
    }

    @FXML
    public void showAddTaskDialog(){
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(mainBorderPane.getScene().getWindow());
        dialog.setTitle("Add Task");
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("AddTaskDialog.fxml"));
        try {
            dialog.getDialogPane().setContent( fxmlLoader.load() );
        } catch (IOException e){
            System.out.println(e.getMessage());
            return;
        }
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        Optional<ButtonType> result = dialog.showAndWait();
        if(result.isPresent() && result.get() == ButtonType.OK){
            AddTaskController controller = fxmlLoader.getController();
            TaskItem newTask = controller.processResult();
            taskListView.getSelectionModel().select(newTask);
        }
    }

    @FXML
    public void onToggleListener(){
        TaskItem item = (TaskItem) taskListView.getSelectionModel().getSelectedItem();
        if(filterToggleButton.isSelected()){
            filteredList.setPredicate(todayTasks);
            if(filteredList.isEmpty()){
                taskDescriptionTextArea.clear();
                dueDateLabel.setText("");
            } else if (filteredList.contains(item)){
                taskListView.getSelectionModel().select(item);
            } else {
                taskListView.getSelectionModel().selectFirst();
            }
        } else {
            filteredList.setPredicate(allTasks);
            taskListView.getSelectionModel().select(item);
        }
    }

    @FXML
    public void exitButtonListener(){
        Platform.exit();
    }

    public void deleteItem(TaskItem item){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Task");
        alert.setHeaderText("Delete task: " + item.getDescription());
        alert.setContentText("Are you sure ?");
        Optional<ButtonType> result = alert.showAndWait();

        if(result.isPresent() && result.get().equals(ButtonType.OK)){
            TaskData.getInstance().deleteTask(item);
        }
    }

    private void createContextMenu(){
        contextMenu = new ContextMenu();
        MenuItem deleteMenuItem = new MenuItem("Delete");
        deleteMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                TaskItem item = (TaskItem) taskListView.getSelectionModel().getSelectedItem();
                deleteItem(item);
            }
        });

        MenuItem editMenuItem = new MenuItem("edit");
        editMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                TaskItem item = (TaskItem) taskListView.getSelectionModel().getSelectedItem();

                Dialog<ButtonType> dialog = new Dialog<>();
                dialog.initOwner(mainBorderPane.getScene().getWindow());
                dialog.setTitle("Edit Task");
                FXMLLoader fxmlLoader = new FXMLLoader();
                fxmlLoader.setLocation(getClass().getResource("AddTaskDialog.fxml"));

                try {
                    dialog.getDialogPane().setContent( fxmlLoader.load() );
                } catch (IOException e){
                    System.out.println(e.getMessage());
                    return;
                }
                AddTaskController controller = fxmlLoader.getController();
                controller.setDescription( item.getDescription() );
                controller.setDetails( item.getDetails() );
                controller.setDueDatePicker( item.getDeadline() );

                dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
                dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
                Optional<ButtonType> result = dialog.showAndWait();

                if(result.isPresent() && result.get() == ButtonType.OK){
                    TaskData.getInstance().deleteTask(item);
                    TaskItem newTask = controller.processResult();
                    taskListView.getSelectionModel().select(newTask);
                }
            }
        });

        contextMenu.getItems().addAll(editMenuItem, deleteMenuItem);
    }
}
