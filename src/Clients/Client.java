package Clients;

import java.io.Serializable;

public class Client implements Serializable {
    private String alias;
    private String message;

    public Client(){}

    public void setAlias(String alias) {this.alias = alias;}
    public void setMessage(String message) {
        this.message = message;
    }
    public String getAlias() {
        return alias;
    }
    public String getMessage() {return message;}
}
