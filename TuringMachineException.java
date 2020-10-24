public class TuringMachineException extends Exception {
    private String errorMessage; 
    
    public TuringMachineException (String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getMessage () {
        return this.errorMessage;
    }
}
