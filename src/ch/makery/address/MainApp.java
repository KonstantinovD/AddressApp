package ch.makery.address;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.prefs.Preferences;

import ch.makery.address.model.Person;
import ch.makery.address.model.PersonListWrapper;
import ch.makery.address.view.PersonEditDialogController;
import ch.makery.address.view.PersonOverviewController;
import ch.makery.address.view.RootLayoutController;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.image.Image;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.stream.*;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import ch.makery.address.model.PersonListWrapper;

public class MainApp extends Application{

    private Stage primaryStage;
    private BorderPane rootLayout;//RootLayout - our main Pane for all app. It is described in RootLayout.fxml

    /**
     *
     * List of recipient (ObservableList)
     */
    private ObservableList<Person> personData = FXCollections.observableArrayList();

    /**
     * Constructor
     */
    public MainApp(){
        //add some data as a sample
        personData.add(new Person("Hans", "Muster"));
        personData.add(new Person("Ruth", "Mueller"));
        personData.add(new Person("Heinz", "Kurz"));
        personData.add(new Person("Cornelia", "Meier"));
        personData.add(new Person("Werner", "Meyer"));
        personData.add(new Person("Lydia", "Kunz"));
        personData.add(new Person("Anna", "Best"));
        personData.add(new Person("Stefan", "Meier"));
        personData.add(new Person("Martin", "Mueller"));
    }

    /**
     *Returns list of recipients (ObservableList)
     */
    public ObservableList<Person> getPersonData() {
        return personData;
    }


    @Override
    public void start(Stage primaryStage) throws java.lang.Exception {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("AddressApp");

        //set application icon
        this.primaryStage.getIcons().add(new Image("file:resources/images/addressbook_32.png"));

        initRootLayout();

        showPersonOverview();
    }

    /**
     * Initializing of root layout
     */
    public void initRootLayout(){
        try{
            //load root class from fxml file
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/RootLayout.fxml"));
            rootLayout = (BorderPane) loader.load();
            //display a root layout scene
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);

            //give controller the access to mainApp
            RootLayoutController controller = loader.getController();
            controller.setMainApp(this);

            primaryStage.show();
        }catch(IOException ex){
            ex.printStackTrace();
        }

        //Tries to load the last opened file with persons
        File file = getPersonFilePath();
        if(file != null){
            loadPersonDataFromFile(file);
        }
    }

    /**
     * shows data about persons
     */
    public void showPersonOverview(){
        try{
            //load data about persons
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/PersonOverview.fxml"));
            AnchorPane personOverview = (AnchorPane) loader.load();

            //set personOverview to the center of rootLayout (BorderPane)
            rootLayout.setCenter(personOverview);

            //Give the controller access to the main app
            PersonOverviewController controller = loader.getController();
            controller.setMainApp(this);

        }catch(IOException ex){
            ex.printStackTrace();
        }
    }

    /**
     * Opens a dialog to edit details for the specified person. If the user
     * clicks OK, the changes are saved into the provided person object and true
     * is returned.
     *
     * @param person the person object to be edited
     * @return true if the user clicked OK, false otherwise.
     */
    public boolean showPersonEditDialog(Person person) {
        try {
            // Загружаем fxml-файл и создаём новую сцену
            // для всплывающего диалогового окна.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/PersonEditDialog.fxml"));
            AnchorPane page = (AnchorPane) loader.load();

            // Создаём диалоговое окно Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit Person");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(page);

            /**scene.getStylesheets().add((getClass().getResource("view/DarkTheme.css")).toExternalForm());*/

            dialogStage.setScene(scene);

            // Передаём адресата в контроллер.
            PersonEditDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setPerson(person);

            // Отображаем диалоговое окно и ждём, пока пользователь его не закроет
            dialogStage.showAndWait();

            return controller.isOkClicked();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * returns preference of file of persons, that is,
     * the last opened file. Preference is stored in the
     * (реестр), which is specific for current OS. In case
     * preference file isn't found, null is returned
     * @return
     */
    public File getPersonFilePath(){
        Preferences prefs = Preferences.userNodeForPackage(MainApp.class);
        String filePath = prefs.get("filePath", null); //def == default
        if(filePath != null){
            return new File(filePath);
        }else{
            return null;
        }
    }

    /**
     * Specifies path for current loaded file. This path
     * is saved n the
     * (реестр), which is specific for current OS.
     *
     * @param file - file or null to delete path
     */
    public void setPersonFilePath(File file){
        Preferences prefs = Preferences.userNodeForPackage(MainApp.class);
        if(file != null){
            prefs.put("filePath", file.getPath());

            //updating scene title
            primaryStage.setTitle("AddressApp - " + file.getName());
        } else {
            //updating scene title
            primaryStage.setTitle("AddressApp");
        }
    }

    /**
     * Loads info about persons from specified file.
     * Current info about persons will be changed
     *
     * @param file
     */
    public void loadPersonDataFromFile(File file) {
        try {
            personData.clear();

            XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
            Person pers = null;
            XMLEvent xmlEvent = null;
            StartElement startElement = null;

            try {
                XMLEventReader xmlEventReader = xmlInputFactory.createXMLEventReader(new FileInputStream(file.getAbsolutePath()));
                while(xmlEventReader.hasNext()){
                    xmlEvent = xmlEventReader.nextEvent();
                    if (xmlEvent.isStartElement()){
                        startElement = xmlEvent.asStartElement();

                        if(startElement.getName().getLocalPart().equals("person")){
                            pers = new Person();

                            //loadXMLPlainTextElement(xmlEventReader);

                            pers.setBirthday(LocalDate.parse(loadXMLPlainTextElement(xmlEventReader)));
                            pers.setCity(loadXMLPlainTextElement(xmlEventReader));
                            pers.setFirstName(loadXMLPlainTextElement(xmlEventReader));
                            pers.setLastName(loadXMLPlainTextElement(xmlEventReader));
                            pers.setPostalCode(Integer.parseInt(loadXMLPlainTextElement(xmlEventReader)));
                            pers.setStreet(loadXMLPlainTextElement(xmlEventReader));

                            personData.add(pers);
                        }

                    }

                }

            } catch (FileNotFoundException e1){
                //e1.printStackTrace();

                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("File not found");
                alert.showAndWait();
            }
            catch(XMLStreamException e) {
                //e.printStackTrace();

                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Could not load data");
                alert.setContentText("Could not load data from file:\n" + file.getPath());

                alert.showAndWait();
            }

            personData.addAll();

            //Saving path to file in the (реестр)
            setPersonFilePath(file);

        } catch (java.lang.Exception ex) {
            //ex.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Could not load data");
            alert.setContentText("Could not load data from file:\n" + file.getPath());

            alert.showAndWait();
        }
    }

    /**
     * Reads plain text element in form <element>content</element> and returns "content".
     * @param xmlEventReader
     * @return {@code String} contains "content"
     * @throws XMLStreamException
     */
    private String loadXMLPlainTextElement(XMLEventReader xmlEventReader) throws XMLStreamException{
        String data;

        xmlEventReader.nextEvent();//element start
        data = (xmlEventReader.nextEvent().asCharacters().getData());//element text
        xmlEventReader.nextEvent();//element end

        return data;
    }

    /**
     * Saves current info about persons to specified file
     *
     * @param file
     */
    public void savePersonDataToFile(File file){
        try{
            OutputStream os = new FileOutputStream(file);
            XMLStreamWriter writer = null;

            try {
                XMLOutputFactory outputFactory = XMLOutputFactory.newFactory();
                writer = outputFactory.createXMLStreamWriter(os, "utf-8");
                writeXMLPerson(writer, personData);
            }
            finally { if (writer != null) writer.close(); }


       }catch(java.lang.Exception ex){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Could not load data");
            alert.setContentText("Could not load data from file:\n" + file.getPath());

            alert.showAndWait();
        }
    }

    /**
     * Parses Person objects data and writes to file.
     * @param writer XML writer connected with file which receives data about persons
     * @param personData persons to write to file
     * @throws XMLStreamException
     */
    private void writeXMLPerson(XMLStreamWriter writer, ObservableList<Person> personData) throws XMLStreamException {
        writer.writeStartDocument("utf-8", "1.0");
        writer.writeStartElement("persons");

        for(Person pers : personData){
            //  <person>
            writer.writeStartElement("person");

            writer.writeStartElement("birthday");
            writer.writeCharacters(pers.getBirthday().toString());
            writer.writeEndElement();

            writer.writeStartElement("city");
            writer.writeCharacters(pers.getCity());
            writer.writeEndElement();

            writer.writeStartElement("firstName");
            writer.writeCharacters(pers.getFirstName());
            writer.writeEndElement();

            writer.writeStartElement("lastName");
            writer.writeCharacters(pers.getLastName());
            writer.writeEndElement();

            writer.writeStartElement("postalCode");
            writer.writeCharacters(String.valueOf(pers.getPostalCode()));
            writer.writeEndElement();

            writer.writeStartElement("street");
            writer.writeCharacters(pers.getStreet());
            writer.writeEndElement();

            //  </person>
            writer.writeEndElement();
        }

        writer.writeEndElement();

    }


    /**
     * returns the main scene
     * @return
     */
    public Stage getPrimaryStage(){
        return primaryStage;
    }

    public static void main(String[] args){
        launch(args);
    }
}


