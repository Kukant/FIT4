package Enviroment;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();

        try {
            primaryStage.getIcons().add(new Image("https://image.flaticon.com/icons/svg/201/201551.svg"));
            Scene scene = new Scene(root,1230,615);
            scene.getStylesheets().add(getClass().getResource("./../Resources/application.css").toExternalForm());
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch(Exception e) {
            e.printStackTrace();
        }

        root.setCenter(new RootLayout());
    }

    public static void main(String[] args) {
        launch(args);
    }
}