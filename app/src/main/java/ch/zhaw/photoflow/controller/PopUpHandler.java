package ch.zhaw.photoflow.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.fxml.FXML;
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
import ch.zhaw.photoflow.core.domain.Tag;

public class PopUpHandler extends AbstractController {

	private String name;
	private String desc;
	private List<Tag> tags;
	
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
	
	/**
	 * Default constructor creates a JavaFX 8 Dialog with three input fields for project details
	 */
	public PopUpHandler()
	{		
		Dialog<List<String>> dialog = new Dialog<>();
		dialog.setTitle("Create Project");

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

		// Enable/Disable create button depending on whether a project name was entered.
		Node loginButton = dialog.getDialogPane().lookupButton(createButtonType);
		loginButton.setDisable(true);

		// Check if project name entered and disable create button until then
		projectName.textProperty().addListener((observable, oldValue, newValue) -> {
		    loginButton.setDisable(newValue.trim().isEmpty());
		});

		dialog.getDialogPane().setContent(grid);

		// Request focus on the project name field by default.
		Platform.runLater(() -> projectName.requestFocus());

		// Convert the result to a an arraylist when create button is clicked.
		dialog.setResultConverter(dialogButton -> {
		    if (dialogButton == createButtonType) {
		    	List<String> vals = new ArrayList<>();
		    	vals.add(projectName.getText());
		    	vals.add(projectDescription.getText());
		    	vals.add(projectTags.getText());
		    	return vals;
		    }

		    return null;
		});

		Optional<List<String>> result = dialog.showAndWait();

		result.ifPresent(ArrayList -> {
			setName(ArrayList.get(0).toString());
			setDesc(ArrayList.get(1).toString());
			setTags(sliceTags(ArrayList.get(2).toString()));
		});
	
	}
	
	/**
	 * Cuts the received String in to single Tags and collects them in a List
	 * @param tagsEnBloque	A String containing Tags semicolon-separated
	 * @return	List of tags
	 */
	private List<Tag> sliceTags(String tagsEnBloque)
	{
		List<String> temp = Arrays.asList(tagsEnBloque.split("[;,\\n]"));
		
		return temp.stream().map(s -> s.trim()).map(s -> new Tag(s)).collect(Collectors.toList());
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public List<Tag> getTags() {
		return tags;
	}

	public void setTags(List<Tag> slicedTags) {
		this.tags = slicedTags;
	}
}
