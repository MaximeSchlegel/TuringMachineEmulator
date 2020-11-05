public class Main {
    public static void main(String[] args) {
        assert args.length <= 4;

        String help = new String("Usage: The program expect the following arguments:\n"
                               + "  [Mandatory] | -machine [path] : path to the turing machine to emulate\n"
                               + "  [Optionnal] | -tape [path]    : path to initial tape state\n"
                               + "  [Optionnal] | -display        : the emulator will display detailed information during the excution\n"
                               + "  [Optionnal] | -debug          : the emulator will display the debug information\n");

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
                case "-h":
                    System.out.println(help);
                    return;
                default:
                    System.err.println(help);
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

        if (configFile.isEmpty()) {
            System.err.println(help);
            throw new IllegalArgumentException("No turing machine to emulate");
        }
        
        try {
            TuringMachine tm = new TuringMachine(configFile, tapeFile, display, debug);
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
