package org.simulation;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application{

    public static void main(String[] args){
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        try{
            Parent root = FXMLLoader.load(Main.class.getClassLoader().getResource("MainWindowInterface.fxml"));
            stage.setScene(new Scene(root,800,600));
            stage.setResizable(false);
            stage.setTitle("Simulacion de lotes");
            stage.show();
        }catch(Exception e){
            e.printStackTrace();
        }
    } 
}
