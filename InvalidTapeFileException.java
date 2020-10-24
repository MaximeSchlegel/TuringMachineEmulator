public class InvalidTapeFileException extends Exception {
    private String errorMessage; 

    public InvalidTapeFileException (String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getMessage () {
        return this.errorMessage;
    }
    
}
