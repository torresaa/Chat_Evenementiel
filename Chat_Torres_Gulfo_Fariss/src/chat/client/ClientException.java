/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chat.client;

/**
 *
 * @author aquilest
 */
public class ClientException extends Exception {

    private static final long serialVersionUID = 1L;

    String message;

    public ClientException() {
    }

    public ClientException(String message) {
        this.message = message;
    }
    
    @Override
    public String getMessage() {
        return message;
    }
    
}
