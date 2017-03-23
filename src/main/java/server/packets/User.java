package server.packets;

/**
 * Created by amriksadhra on 22/03/2017.
 */
public class User {


    public String firstName;
    public String secondName;
    public String loginName;
    public String password;
    public String teacherStatus;

    public User(){

    }

    public User(String firstName, String secondName, String loginName, String password, boolean teacherStatus) {
        super();
        this.firstName = firstName;
        this.secondName = secondName;
        this.loginName = loginName;
        this.password = password;
        this.teacherStatus = String.valueOf(teacherStatus);
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
        return Boolean.valueOf(teacherStatus);
    }
}
