package ch.makery.address.view;


import ch.makery.address.MainApp;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;

import java.io.File;

/**
 * Controller for root layout. Root layout provides base
 * layout of application, which contains menu and container
 * for the rest JavaFX elements
 */
public class RootLayoutController {

    private MainApp mainApp;

    public void setMainApp(MainApp mainApp){
        this.mainApp = mainApp;
    }

    /**
     * Creates an empty address book
     */
    @FXML
    private void handleNew(){
        mainApp.getPersonData().clear();
        mainApp.setPersonFilePath(null);
    }

    /**
     * Opens FileChooser to allow user to choose address book
     * for loading
     */
    @FXML
    private void handleOpen() {
        FileChooser fileChooser = new FileChooser();

        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                "XML files (*.xml)", "*.xml");
        fileChooser.getExtensionFilters().add(extFilter);

        //Show file load dialog
        File file = fileChooser.showOpenDialog(mainApp.getPrimaryStage());

        if (file != null) {
            mainApp.loadPersonDataFromFile(file);
        }
    }

    /**
     * Saves file to current opened file. If file isn't opened,
     * the "save as" dialog is showed
     */
    @FXML
    private void handleSave(){
        File personFile = mainApp.getPersonFilePath();
        if(personFile != null){
            mainApp.savePersonDataToFile(personFile);
        } else {
            handleSaveAs();
        }
    }

    /**
     * Opens FileChooser to allow user to choose
     * file to save the data
     */
    @FXML
    private void handleSaveAs() {
        FileChooser fileChooser = new FileChooser();

        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                "XML files (*.xml)", "*.xml");
        fileChooser.getExtensionFilters().add(extFilter);

        //show file save dialog
        File file = fileChooser.showSaveDialog(mainApp.getPrimaryStage());

        if (file != null) {
            //make sure it has the correct extension
            if (!file.getPath().endsWith(".xml")) {
                file = new File(file.getPath() + ".xml");
            }

            mainApp.savePersonDataToFile(file);
        }
    }

    /**
     * Opens "about" dialog
     */
    @FXML
    private void handleAbout(){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("AddressApp");
        alert.setHeaderText("About");
        alert.setContentText("Created by Konstantinov Daniil with the help of Marco Jacob tutorial");

        alert.showAndWait();
    }

    /**
     * Closes application
     */
    @FXML
    private void handleExit(){
        System.exit(0);
    }


}
