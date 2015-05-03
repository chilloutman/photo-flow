package ch.zhaw.photoflow.controller;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import ch.zhaw.photoflow.core.domain.Project;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class PopUpHandler extends AbstractController implements Initializable {

	Stage stage; 
	FXMLLoader root;
	
	@FXML
	private TextField textfieldProjectName;
	
	@FXML
	private TextArea textareaProjectDescription;
	
	@FXML
	private TextArea textareaProjectTags;
	
	@FXML
	private Button buttonCreateProject;
	
	@FXML
	private Button buttonCancelProject;
	
	
	public PopUpHandler()
	{		
		Dialog<Project> dialog = new Dialog<>();
		dialog.setTitle("Create Project");
		//dialog.setHeaderText("Look, a Custom Login Dialog");

		// Set the icon (must be included in the project).
		//dialog.setGraphic(new ImageView(this.getClass().getResource("login.png").toString()));

		// Set the button types.
		ButtonType createButtonType = new ButtonType("create", ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

		// Create the title, description and tags labels and fields.
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 150, 10, 10));

		TextField projectName = new TextField();
		projectName.setPromptText("Project Name");
		TextArea projectDescription = new TextArea();
		projectDescription.setPromptText("Project Description");
		TextArea projectTags = new TextArea();
		projectTags.setPromptText("Project Tags");

		grid.add(new Label("Project Name:"), 0, 0);
		grid.add(projectName, 1, 0);
		grid.add(new Label("Project Description:"), 0, 1);
		grid.add(projectDescription, 1, 1);
		grid.add(new Label("Project Tags:"), 0, 2);
		grid.add(projectTags, 1, 2);

		// Enable/Disable login button depending on whether a username was entered.
		Node loginButton = dialog.getDialogPane().lookupButton(createButtonType);
		loginButton.setDisable(true);

		// Do some validation (using the Java 8 lambda syntax).
		projectName.textProperty().addListener((observable, oldValue, newValue) -> {
		    loginButton.setDisable(newValue.trim().isEmpty());
		});

		dialog.getDialogPane().setContent(grid);

		// Request focus on the username field by default.
		Platform.runLater(() -> projectName.requestFocus());

		// Convert the result to a username-password-pair when the login button is clicked.
		dialog.setResultConverter(dialogButton -> {
		    if (dialogButton == createButtonType) {
		//        return new Pair<>(projectName.getText(), projectDescription.getText());
		    }
		    return null;
		});

		Optional<Project> result = dialog.showAndWait();

		result.ifPresent(Project -> {
			System.out.println("returned some values");
		   // System.out.println("Project Name = " + projectData.getValue() + ", Project Description = " + projectData.getValue());
		});
	
	}



	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
		System.out.println("init");
		
		   textfieldProjectName.setText("Project Name");
		
		   buttonCreateProject.setOnAction(this::create);
		   buttonCancelProject.setOnAction(this::cancel);

		
	}

	public void create(ActionEvent event)
	{
		System.out.println("created");
		stage.close();
	}
	
	public void cancel(ActionEvent event)
	{
		System.out.println("cancel");
		stage.close();
	}
}
