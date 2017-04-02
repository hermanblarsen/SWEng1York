package server.packets;

/**
 * Created by amriksadhra on 22/03/2017.
 */
public class User {


    public String firstName;
    public String secondName;
    public String password;
    public String userType;
    public String emailAddress;

    public User(){

    }




    public User(String firstName, String secondName, String emailAddress, String password, String userType) {
        super();
        this.firstName = firstName;
        this.secondName = secondName;
        this.emailAddress = emailAddress;
        this.password = password;
        this.userType = userType;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getSecondName() {
        return secondName;
    }

    public String getPassword() {
        return password;
    }

    public String getUserType() {
        return userType;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

}
