package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import algorithm.ART1;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Controller {

    @FXML
    public TextArea textArea;

    @FXML
    public Slider slider;

    @FXML
    public TextField textField;

    @FXML
    public Label valueLabel;

    @FXML
    public TextField clusterNumberTextField;

    @FXML
    private GridPane gridPane;

    private ObservableList<Rectangle> rectangleObservableList;
    private ART1 art1;

    private final int MAX_SELECTED_FILES_NUMBER = 30;
    private final String DATA_DIRECTORY_NAME = "/data";
    private String dataPath;

    public void initialize() {
        art1 = new ART1();
        textField.setPromptText("Name: ");
        clusterNumberTextField.setPromptText("Number: ");
        rectangleObservableList = FXCollections.observableArrayList();
        gridPane.setHgap(10);
        gridPane.setVgap(10);

        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 9; j++) {
                Rectangle rectangle = new Rectangle(55, 55, Color.WHITE);
                rectangle.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                    if (rectangle.getFill() == Color.WHITE)
                        rectangle.setFill(Color.BLACK);
                    else
                        rectangle.setFill(Color.WHITE);
                });
                rectangleObservableList.add(rectangle);
                gridPane.add(rectangle, i, j);
            }
        }

        slider.valueProperty().addListener((ov, old_val, new_val) -> {
            double number = Math.round(new_val.doubleValue() * 100);
            number = number / 100;
            art1.setVIGILANCE(number);
            valueLabel.setText("Value: " + number);
        });

        try {
            Files.createDirectories(Paths.get(DATA_DIRECTORY_NAME));
        } catch (IOException e) {
            e.printStackTrace();
        }
        dataPath = Paths.get(".").toAbsolutePath().normalize().toString() + DATA_DIRECTORY_NAME;
    }

    public void saveButtonOnAction(ActionEvent actionEvent) {
        StringBuilder output = new StringBuilder();

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 7; j++) {

                if (rectangleObservableList.get(j * 9 + i).getFill() == Color.WHITE)
                    output.append("0");
                else
                    output.append("1");
            }
            output.append("\n");
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(dataPath));
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
        fileChooser.getExtensionFilters().add(extFilter);
        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            try {
                PrintWriter writer;
                writer = new PrintWriter(file);
                writer.println(output);
                writer.close();
            } catch (Exception e) {
                System.out.println("Error while saving a file!");
                e.printStackTrace();
            }
        }
    }

    public void openButtonOnAction(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(dataPath));
        fileChooser.setTitle("Select .txt file with data");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("TXT Files", "*.txt"));
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            try (BufferedReader br = new BufferedReader(new FileReader(selectedFile.getAbsolutePath()))) {
                StringBuilder sb = new StringBuilder();
                String line = br.readLine();

                while (line != null) {
                    sb.append(line);
                    line = br.readLine();
                }
                String everything = sb.toString();
                int number = 0;

                for (int i = 0; i < 9; i++) {
                    for (int j = 0; j < 7; j++) {
                        if (everything.charAt(number) == '1')
                            rectangleObservableList.get(j * 9 + i).setFill(Color.BLACK);
                        else
                            rectangleObservableList.get(j * 9 + i).setFill(Color.WHITE);
                        number++;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("File selection cancelled");
        }
    }

    public void clearButtonOnAction(ActionEvent actionEvent) {
        for (Rectangle rectangle : rectangleObservableList) {
            rectangle.setFill(Color.WHITE);
        }
    }

    public void train_bt_onAction(ActionEvent actionEvent) {
        if (isBoardFilled()) {
            if (!textField.getText().isEmpty() && !art1.getMembership().keySet().contains(textField.getText())) {
                art1.train(getDataFromMatrix(), textField.getText());
                textArea.clear();
                textArea.setText(art1.getClustersString());
            } else if (art1.getMembership().keySet().contains(textField.getText())) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText("Input not valid!");
                alert.setContentText("This name already exists");
                alert.showAndWait();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText("Input not valid!");
                alert.setContentText("Write name for character");
                alert.showAndWait();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Board is empty!");
            alert.setContentText("Fill the board");
            alert.showAndWait();
        }
    }

    public void trainAll_bt_onAction(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(dataPath));
        fileChooser.setTitle("Select .txt files with data");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("TXT Files", "*.txt"));
        List<File> fileList = fileChooser.showOpenMultipleDialog(null);

        if (fileList != null) {
            if (fileList.size() < MAX_SELECTED_FILES_NUMBER) {

                for (File file : fileList) {
                    try (BufferedReader br = new BufferedReader(new FileReader(file.getAbsolutePath()))) {
                        StringBuilder sb = new StringBuilder();
                        String line = br.readLine();

                        while (line != null) {
                            sb.append(line);
                            line = br.readLine();
                        }
                        String everything = sb.toString();

                        Integer[] letter = new Integer[63];
                        for (int i = 0; i < 63; i++) {
                            if (everything.charAt(i) == '0')
                                letter[i] = 0;
                            else
                                letter[i] = 1;
                        }

                        String fileName = file.getName();
                        if (fileName.length() > 4 && fileName.endsWith(".txt")) {
                            fileName = fileName.substring(0, fileName.length() - 4);
                        }
                        art1.train(letter, fileName);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                textArea.setText(art1.getClustersString());
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText("Too many files selected!");
                alert.setContentText("You can only choose " + MAX_SELECTED_FILES_NUMBER + " files at the same time");
                alert.showAndWait();
            }
        } else {
            System.out.println("File selection cancelled");
        }
    }


    public void test_bt_onAction(ActionEvent actionEvent) {
        if (isBoardFilled()) {
            Integer clusterNumber = art1.test(getDataFromMatrix());
            textArea.appendText("\n" + "Cluster for character: " + clusterNumber);
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Board is empty!");
            alert.setContentText("Fill the board");
            alert.showAndWait();
        }
    }

    public Integer[] getDataFromMatrix() {
        Integer[] letter = new Integer[63];
        int counter = 0;

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 7; j++) {
                if (rectangleObservableList.get(j * 9 + i).getFill() == Color.WHITE)
                    letter[counter] = 0;
                else
                    letter[counter] = 1;
                counter++;
            }
        }
        return letter;
    }

    public void untrain_bt_OnAction(ActionEvent actionEvent) {
        textArea.clear();
        art1 = new ART1();
        double number = Math.round(slider.getValue() * 100);
        number = number / 100;
        art1.setVIGILANCE(number);
    }

    public boolean isBoardFilled() {
        return rectangleObservableList.stream()
                .anyMatch(rectangle -> rectangle.getFill() != Color.WHITE);
    }

    public void loadCluster_bt_onAction(ActionEvent actionEvent) {

        try {
            int clusterNumber = Integer.parseInt(clusterNumberTextField.getText());
            double[] value = art1.getTw(clusterNumber);
            int number = 0;

            for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 7; j++) {
                    if (value[number] == 0.0)
                        rectangleObservableList.get(j * 9 + i).setFill(Color.WHITE);
                    else
                        rectangleObservableList.get(j * 9 + i).setFill(Color.BLACK);
                    number++;
                }
            }
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Wrong number of cluster!");
            alert.showAndWait();
        }
    }
}