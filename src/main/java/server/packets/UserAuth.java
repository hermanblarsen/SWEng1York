package server.packets;

/**
 * Created by amriksadhra on 23/03/2017.
 */
public class UserAuth {
    String userToLogin;
    String password;

    public UserAuth(){
    }

    public UserAuth(String userToLogin, String password){
        super();
        this.userToLogin = userToLogin;
        this.password = password;
    }


    public String getUserToLogin() {
        return userToLogin;
    }

    public String getPassword() {
        return password;
    }
}
