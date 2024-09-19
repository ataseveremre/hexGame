package edu.erciyes.vp.hexgame;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import java.io.IOException;

public class GameStart extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(GameStart.class.getResource("Game-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 600);
        stage.setTitle("HexGame - BZ214 Visual Programming"); //pencere ismi
        stage.setScene(scene);
        stage.getIcons().add(new Image("file:src\\main\\resources\\images\\image.png")); //pencere için Icon
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}