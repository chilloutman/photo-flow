package ch.zhaw.photoflow;

import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class Main extends Application{

  public static void main(String[] args) {
    System.out.println("hi");
    
    
    launch(Main.class, args);
  }
  
  
  @Override
	public void start(Stage primaryStage) throws Exception {
		try {
			AnchorPane page = (AnchorPane) FXMLLoader.load(Main.class.getResource("view/main_gui.fxml"));
          Scene scene = new Scene(page);
          primaryStage.setScene(scene);
          primaryStage.setTitle("Photo Flow");
          primaryStage.show();
      } catch (Exception ex) {
          Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
      	System.out.println("I failed :-(");
      }
		
	}

}
