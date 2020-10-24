public class Main {
    public static void main(String[] args) {
        assert args.length <= 4;

        String configFile="", tapeFile="";
        Boolean debug=false, display=false;
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-machine": {
                    i++;
                    configFile = args[i];
                    break;}
                case "-tape":
                    i++;
                    tapeFile = args[i];
                    break;
                case "-debug":
                    debug = true;
                    break;
                case "-display":
                    display = true;
                    break;
                default:
                    throw new IllegalArgumentException("Not a valid argument: " + args[i]);
            }    
        }

        if (debug) {
            System.out.println("Selected Options:");
            System.out.println("  ConfigPath: " + ((! configFile.isEmpty()) ? configFile : "Disable"));
            System.out.println("    TapePath: " + ((! tapeFile.isEmpty()) ? tapeFile : "Disable"));
            System.out.println("     Display: " + display);
            System.out.println("       Debug: " + debug);
            System.out.println();
        }
        

        try {
            TuringMachine tm = new TuringMachine(configFile,tapeFile, display, debug);
            tm.execute();
            System.out.println("The Turing machine ended in state: s" + tm.getFinalState());
            System.out.println("The input is " + (tm.getAccepted() ? "accepted":"rejected"));
        } catch (Exception e){
            System.out.println();
            System.out.println(e.getMessage());
            System.out.println();
            e.printStackTrace();
        }
    }
}
