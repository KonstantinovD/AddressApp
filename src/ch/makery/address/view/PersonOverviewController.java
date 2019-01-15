package ch.makery.address.view;

import ch.makery.address.util.DateUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import ch.makery.address.MainApp;
import ch.makery.address.model.Person;

public class PersonOverviewController {
    @FXML
    private TableView<Person> personTable;
    @FXML
    //first type is the type of the TableView generic type, second is the type of the content in all cells in this TableColumn
    private TableColumn<Person, String> firstNameColumn;
    @FXML
    private TableColumn<Person, String> lastNameColumn;

    @FXML
    private Label firstNameLabel;
    @FXML
    private Label lastNameLabel;
    @FXML
    private Label streetLabel;
    @FXML
    private Label postalCodeLabel;
    @FXML
    private Label cityLabel;
    @FXML
    private Label birthdayLabel;

    //Reference to the main app
    private MainApp mainApp;

    /**
     * Constructor
     * Constructor is called earlier than the initialize() method
     */
    public PersonOverviewController(){

    }

    /**
     * Class-controller initializing. This method is automatically called
     * after loading of fxml file
     */
    @FXML
    private void initialize(){
        //Table of recipients initialization (which has 2 columns)
        //using LAMBDAS
        firstNameColumn.setCellValueFactory(cellData -> cellData.getValue().firstNameProperty());
        lastNameColumn.setCellValueFactory(cellData -> cellData.getValue().lastNameProperty());

        //clear details about the person
        showPersonDetails(null);

        //adding Listener to personTable
        //listen choosing changes and show
        //person details in case of choosing
        personTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showPersonDetails(newValue));
    }

    /**
     * Is called by the main application to give a reference back to itself.
     *
     * @param mainApp
     */
    public void setMainApp(MainApp mainApp){
        this.mainApp = mainApp;

        //Add observable list data to the table
        personTable.setItems(mainApp.getPersonData());
    }

    /**
     * Fills all text fields and shows details about the person
     * In case null is received, all text fields are cleared
     *
     * @param person the person or null
     */
    private void showPersonDetails(Person person){
        if(person != null){
            //fill the labels with the data from the Person object
            firstNameLabel.setText(person.getFirstName());
            lastNameLabel.setText(person.getLastName());
            streetLabel.setText(person.getStreet());
            postalCodeLabel.setText(Integer.toString(person.getPostalCode()));
            cityLabel.setText(person.getCity());
            birthdayLabel.setText(DateUtil.format(person.getBirthday()));
        }else{
            // Person is null, remove all the text
            firstNameLabel.setText("");
            lastNameLabel.setText("");
            streetLabel.setText("");
            postalCodeLabel.setText("");
            cityLabel.setText("");
            birthdayLabel.setText("");
        }
    }

    /**
     * Called when user presses "Delete" button
     */
    @FXML
    private void handleDeletePerson(){
        int selectedIndex = personTable.getSelectionModel().getSelectedIndex();
        if(selectedIndex >= 0) {
            personTable.getItems().remove(selectedIndex);
        }else{
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.initOwner(mainApp.getPrimaryStage());
            alert.setTitle("No Selection!");
            alert.setHeaderText("No Person Selected");
            alert.setContentText("Please, select a person in the table");

            alert.showAndWait();//wait for user closing it
        }
    }
    /**
     * Called when the user click the "new" button. Opens a dialog to edit
     * details for a new person
     */
    @FXML
    private void handleNewPerson(){
        Person tempPerson = new Person();
        boolean okClicked = mainApp.showPersonEditDialog(tempPerson);
        if(okClicked){
            mainApp.getPersonData().add(tempPerson);
        }
    }

    /**
     * Called when the user clicks the "edit" button. Opens a dialog to edit
     * details for the selected person
     */
    @FXML
    private void handleEditPerson(){
        Person selectedPerson = personTable.getSelectionModel().getSelectedItem();
        if(selectedPerson != null) {
            boolean okCLicked = mainApp.showPersonEditDialog(selectedPerson);
            if (okCLicked) {
                showPersonDetails(selectedPerson);
            }
        }else{
            //nothing selected
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.initOwner(mainApp.getPrimaryStage());
            alert.setTitle("No Selection");
            alert.setHeaderText("No Person Selected");
            alert.setContentText("Please select a person in the table.");

            alert.showAndWait();
        }
    }

}
