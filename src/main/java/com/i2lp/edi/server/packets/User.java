package com.i2lp.edi.server.packets;

/**
 * Created by amriksadhra on 22/03/2017.
 */
public class User {

    protected Integer userID;
    protected String firstName;
    protected String secondName;
    protected String password;
    protected String userType;
    protected String emailAddress;
    protected boolean teacher;

    public User(){

    }


    //Constructor used when adding Users
    public User(Integer userID, String firstName, String secondName, String emailAddress, String password, String userType) {
        super();
        this.userID = userID;
        this.firstName = firstName;
        this.secondName = secondName;
        this.emailAddress = emailAddress;
        this.password = password;
        this.userType = userType;
        if(userType.equals("teacher")) this.teacher=true;
    }

    //Constructor used when Authorizing Users
    public User(Integer userID, String firstName, String secondName, String emailAddress, String userType) {
        super();
        this.userID = userID;
        this.firstName = firstName;
        this.secondName = secondName;
        this.emailAddress = emailAddress;
        this.password = password;
        this.userType = userType;
        if(userType.equals("teacher")) this.teacher=true;
    }

    public Integer getUserID() {
        return userID;
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

    public void setUserID(Integer userID) {
        this.userID = userID;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setSecondName(String secondName) {
        this.secondName = secondName;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public void setTeacher(boolean teacher) {
        this.teacher = teacher;
    }

    public boolean isTeacher() {
        return teacher;
    }
}
