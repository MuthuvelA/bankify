package org.example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class User {
    private int id;
    private String username;
    private String encryptedPassword;
    private String fullname;
    private String mail;
    private String phoneno;
    private String accountno;

    public void setAccountno(String accountno){
        this.accountno=accountno;
    }
    public String getAccountno() {
        return accountno;
    }


    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    public void setEncryptedPassword(String encryptedPassword) {
        this.encryptedPassword=encryptedPassword;
    }

    public String getEncryptedPassword() {
        return encryptedPassword;
    }


}


