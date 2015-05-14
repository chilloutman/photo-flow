package ch.zhaw.photoflow.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import ch.zhaw.photoflow.core.domain.Project;
import ch.zhaw.photoflow.core.domain.Tag;

/**
 * Displays a dialog to create a new project.
 */
public class CreateProjectController extends PhotoFlowController {

	private Project project;

	/**
	 * Default constructor creates a JavaFX 8 Dialog with three input fields for project details
	 */
	public CreateProjectController() {
		// Create the title, description and tags labels and fields.
		TextField projectName = new TextField();
		projectName.setPromptText("Project Name");
		TextArea projectDescription = new TextArea();
		projectDescription.setMaxHeight(100);
		projectDescription.setPromptText("Project Description");
		TextArea projectTags = new TextArea();
		projectTags.setMaxHeight(50);
		projectTags.setPromptText("Project Tags");
		
		
		GridPane grid = new GridPane();
		grid.setPadding(new Insets(25));
		grid.setHgap(10);
		grid.setVgap(10);
		
		grid.add(new Label("Project Name:"), 0, 0);
		grid.add(projectName, 1, 0);
		grid.add(new Label("Project Description:"), 0, 1);
		grid.add(projectDescription, 1, 1);
		grid.add(new Label("Project Tags:"), 0, 2);
		grid.add(projectTags, 1, 2);
		
		Dialog<List<String>> dialog = new Dialog<>();
		dialog.setTitle("Create Project");
		dialog.initModality(Modality.NONE);

		// Set the button types.
		ButtonType createButtonType = new ButtonType("create", ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);
		
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

		result.ifPresent(r -> {
			project = Project.newProject(p -> {
				p.setName(r.get(0).toString());
				p.setDescription(r.get(1).toString());
				p.setTags(sliceTags(r.get(2).toString()));
			});
		});
	
	}

	/**
	 * Cuts the received String in to single Tags and collects them in a List
	 * @param tagsEnBloque	A String containing Tags semicolon-separated
	 * @return List of tags
	 */
	private List<Tag> sliceTags(String tagsEnBloque) {
		List<String> tags = Arrays.asList(tagsEnBloque.split("[;,\\n]"));
		return tags.stream().map(s -> s.trim()).map(s -> new Tag(s)).collect(Collectors.toList());
	}
	
	/**
	 * @return The project being created. May be {@code null}.
	 */
	public Project getProject() {
		return project;
	}

}
