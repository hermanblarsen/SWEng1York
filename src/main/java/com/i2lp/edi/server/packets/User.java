package com.i2lp.edi.server.packets;

/**
 * Created by amriksadhra on 22/03/2017.
 */
public class User {


    protected String firstName;
    protected String secondName;
    protected String password;
    protected String userType;
    protected String emailAddress;
    protected boolean teacher;

    public User(){

    }

    public User(String firstName, String secondName, String emailAddress, String password, String userType) {
        super();
        this.firstName = firstName;
        this.secondName = secondName;
        this.emailAddress = emailAddress;
        this.password = password;
        this.userType = userType;
        if(userType.equals("teacher")) this.teacher=true;
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
