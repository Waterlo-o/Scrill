package com.example.scrill;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1100, 800);
        stage.setMinWidth(810);
        stage.setMinHeight(600);
        stage.setTitle("Scrill");
        stage.getIcons().add(new Image(getClass().getResourceAsStream("Icons/icon.png")));
        stage.setScene(scene);
        stage.initStyle(javafx.stage.StageStyle.UNDECORATED);
        ResizeHelper.addResizeListener(stage);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}