/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chat.server;

/**
 *
 * @author aquilest
 */
public class ServerException extends Exception {

    private static final long serialVersionUID = 1L;

    String message;

    public ServerException() {
    }

    public ServerException(String message) {
        this.message = message;
    }
    
    @Override
    public String getMessage() {
        return message;
    }
}
