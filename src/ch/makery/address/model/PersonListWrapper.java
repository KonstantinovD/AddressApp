package ch.makery.address.model;

import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


/**
 * Supportive class for wrapping list of persons.
 * Used for saving list of person in XML format
 */
@XmlRootElement(name = "persons")
public class PersonListWrapper {

    private List persons;

    @XmlElement(name = "person")
    public List getPersons() {
        return persons;
    }

    public void setPersons(List persons) {
        this.persons = persons;
    }
}

