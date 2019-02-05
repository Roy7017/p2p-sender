package server;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

	@Override
    public void start(Stage primaryStage) throws Exception{
		//non-static fxml loader used to obtain controller
		FXMLLoader loader = new FXMLLoader(getClass().getResource("sample.fxml"));

		//loads fxml scene from fxml file
		Parent root = loader.load();
		Controller controller = (Controller)loader.getController();
		controller.setPrimaryStage(primaryStage);
        primaryStage.setTitle("P2P File Sender");
        primaryStage.setScene(new Scene(root, 500, 500));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
