public class InvalidConfigFileException extends Exception {
    private String errorMessage; 

    public InvalidConfigFileException (String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getMessage () {
        return this.errorMessage;
    }
    
}
