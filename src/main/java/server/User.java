package server;

/**
 * Created by amriksadhra on 22/03/2017.
 */
public class User {


    String firstName;
    String secondName;
    String loginName;
    String password;
    Boolean isTeacher;

    public User(){

    }

    public User(String firstName, String secondName, String loginName, String password, boolean isTeacher) {
        super();
        this.firstName = firstName;
        this.secondName = secondName;
        this.loginName = loginName;
        this.password = password;
        this.isTeacher = isTeacher;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getSecondName() {
        return secondName;
    }

    public String getLoginName() {
        return loginName;
    }

    public String getPassword() {
        return password;
    }

    public Boolean getTeacher() {
        return isTeacher;
    }
}
