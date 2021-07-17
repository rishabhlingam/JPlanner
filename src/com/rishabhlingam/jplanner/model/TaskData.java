package com.rishabhlingam.jplanner.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.List;

public class TaskData {
    private static TaskData instance = new TaskData();
    private static  String filename = "TaskListItems.txt";

    private ObservableList<TaskItem> taskItems;
    private DateTimeFormatter formatter;

    private TaskData(){
        formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    }

    public static TaskData getInstance(){
        if(instance == null){
            instance = new TaskData();
        }
        return instance;
    }

    public ObservableList<TaskItem> getTaskItems() {
        return taskItems;
    }

    public void  loadTasks() throws IOException {
        taskItems = FXCollections.observableArrayList();
        Path path = Paths.get(filename);
        BufferedReader br = Files.newBufferedReader(path);
        String input;
        try {
            while ((input = br.readLine()) != null){
                String[] s = input.split("\t");

                String description = s[0];
                String details = s[1];
                String dateString = s[2];

                LocalDate date = LocalDate.parse(dateString, formatter);
                TaskItem newTask = new TaskItem(description, details, date);
                taskItems.add(newTask);
            }
        } finally {
            if(br != null){
                br.close();
            }
        }
    }

    public void storeTasks() throws IOException {
        Path path = Paths.get(filename);
        BufferedWriter bw = Files.newBufferedWriter(path);
        try {
            Iterator<TaskItem> iterator = taskItems.listIterator();
            while (iterator.hasNext()){
                TaskItem taskItem = iterator.next();
                bw.write(String.format("%s\t%s\t%s",
                        taskItem.getDescription(),
                        taskItem.getDetails(),
                        taskItem.getDeadline().format(formatter)));

                bw.newLine();
            }
        } finally {
            if(bw != null){
                bw.close();
            }
        }
    }

    public void addTask(TaskItem item){
        taskItems.add(item);
    }

    public void deleteTask(TaskItem item) {
        taskItems.remove(item);
    }
}
