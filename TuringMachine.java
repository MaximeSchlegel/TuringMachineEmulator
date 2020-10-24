import java.util.ArrayList;
import java.util.HashMap;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;


public class TuringMachine {

    public static enum direction { RIGHT, LEFT}; // direction allow for the moves

    private int statesNb;  // number of state, represent the list of state (s0 ... s_nbState-1)
    private ArrayList<Integer> statesAccepting;  // list of the final state of the turring machine, stop when reaching one
    private ArrayList<Integer> statesRejecting;  // list of the final state of the turring machine, stop when reaching one

    private ArrayList<Integer> tapePositives;  // tape for the positvite indexes (include 0)
    private ArrayList<Integer> tapeNegatives;  // tape for the negative indexes (exclude 0)

    private HashMap<Integer, Triplet<Integer, Integer, direction>> transitions;  // (state, read) -> (nextState, write, move)
    
    private int currentState;  // current state of the turring machine (index in the list array
    private int currentIndex;  // current index of the reading head

    private int accepted=-1;
    private boolean debug, display;

    public int getFinalState() throws TuringMachineException {
        if (this.accepted==-1) throw new TuringMachineException("Try runnig the turing machine before accessing its final state");
        return this.currentState;
    }

    public boolean getAccepted() throws TuringMachineException {
        if (this.accepted==-1) throw new TuringMachineException("Try runnig the turing machine before accessing its accepting state");
        return this.accepted == 1;
    }

    private Triplet<Integer, Integer, direction> readToTransition(int read) {
        // convert a state and read to an int
        return this.transitions.get(this.statesNb * read + this.currentState);
    } 

    private int tapeRead() {
        if (this.currentIndex >= 0) {
            if(this.tapePositives.size() < this.currentIndex + 1) {
                // cell do not exist, create it
                this.tapePositives.add(0);
            }
            return this.tapePositives.get(this.currentIndex);
        } else {
            if(this.tapeNegatives.size() < -this.currentIndex) {
                // cell do not exist, create it
                this.tapeNegatives.add(0);
            }
            return this.tapeNegatives.get(-this.currentIndex - 1); // account for 0 on positive tape and change of sign
        }
    }

    private void tapeWrite(int toWrite) {
        // can't write before read so the cell must exist
        if (this.currentIndex >= 0) {
            this.tapePositives.set(this.currentIndex, toWrite);
        } else {
            this.tapeNegatives.set(-this.currentIndex - 1, toWrite);
        }
    }

    private void move (direction d) {
        if (d == direction.RIGHT) {
            this.currentIndex++;
        } else {
            this.currentIndex--;
        }
    }

    public void execute () {
        Integer read;
        Triplet <Integer, Integer, direction> transition;
        int i=0, is;
        StringBuilder transitionDiplay;

        if (this.debug || this.display) {
            System.out.println("Execution :");
            System.out.println("  Step  |  Tape Pos  |  Transition");
        }
        
        // iterate while it does not reach a final state
        while((! this.statesAccepting.contains(this.currentState))
              && (! this.statesRejecting.contains(this.currentState))) {
            
                transitionDiplay = new StringBuilder("( " + this.currentState + " ; ");
                is = this.currentState;

            read = this.tapeRead();
            transitionDiplay.append(read + " ) => ");
            
            try {
                transition = this.readToTransition(read);
                this.currentState = transition.getFirst();
                this.tapeWrite(transition.getSecond());
                this.move(transition.getThird());
                transitionDiplay.append(transition);

                if (this.debug || this.display) System.out.printf("  %4d  |  %-+8d  |  %s\n", i, this.currentIndex, transitionDiplay.toString());

            } catch (Exception e) {
                System.out.printf("Can't find the transition for: (state: %d ;read: %d)\n", this.currentState, read);
                System.out.printf("  Current Tape position: %d \n", this.currentIndex);
                System.out.print(("    "));
                for (i = -this.tapeNegatives.size(); i < this.tapePositives.size(); i++) 
                    System.out.printf((i<0) ? " %+4d " : " %-+4d ", i);
                System.out.print("\n    ");
                for (i = -this.tapeNegatives.size(); i < this.tapePositives.size(); i++) 
                    System.out.printf((i<0) ? " %3d  " : "  %-3d ", (i<0) ? this.tapeNegatives.get(-i - 1) : tapePositives.get(i));
                System.out.println();
                throw e;
            }
            
            i++;
        } 
        this.accepted = statesAccepting.contains(this.currentState) ? 1 : 0;
        if(this.debug || this.display) System.out.println("Done\n");

    }

    private void parseTape (Scanner tapeReader) throws InvalidTapeFileException {
        // parse the tape file to initialize the machine.
        // format expected: /int;/int/;.../int/
        // cell nb        :   0    1       n-1

        if (! tapeReader.hasNextLine()) throw new InvalidTapeFileException("File is empty");

        String line = tapeReader.nextLine();
        String[] lineSplit = line.split(";");
        int toAdd;

        for (int i=0; i < lineSplit.length; i++) {
            try {
                toAdd = Integer.parseInt(lineSplit[i]);
                this.tapePositives.add(toAdd);
            } catch (Exception e) {
                throw new InvalidTapeFileException("Can't parse cell nb " + i + " : " + lineSplit[i]);
            }
        }
    }

    private void parseConfig (Scanner configReader) throws InvalidConfigFileException {
        // Input in the turing machine the configuration file

        if (! configReader.hasNextLine()) throw new InvalidConfigFileException("File is empty");

        String line;
        String[] lineSplited;


        line = configReader.nextLine();
        lineSplited = line.split(":|;");        
        // check the format of line one, expect: "state_number:/int/;"
        if (! (lineSplited.length == 2)) throw new InvalidConfigFileException("Line 1 is invalid: Wrong Number of token");
        if (! lineSplited[0].equals("state_number")) throw new InvalidConfigFileException("Line 1 is invalid: Wrong field");

        this.statesNb = Integer.parseInt(lineSplited[1]);

        line = configReader.nextLine();
        lineSplited = line.split(":|;");        
        // check the format of line two, expect: "accepting_states:/int/,/int/,...,/int/;"
        if (! (lineSplited.length == 2)) throw new InvalidConfigFileException("Line 2 is invalid: Wrong Number of token");
        if (! lineSplited[0].equals("accepting_states")) throw new InvalidConfigFileException("Line 2 is invalid: Wrong field");
        
        String[] states = lineSplited[1].split(",");
        for (String i : states) this.statesAccepting.add(Integer.parseInt(i));

        line = configReader.nextLine();
        lineSplited = line.split(":|;");        
        // check the format of line three, expect: "rejecting_states:/int/,/int/,...,/int/;"
        if (! (lineSplited.length == 2)) throw new InvalidConfigFileException("Line 3 is invalid: Wrong Number of token");
        if (! lineSplited[0].equals("rejecting_states")) throw new InvalidConfigFileException("Line 3 is invalid: Wrong field");
        
        states = lineSplited[1].split(",");
        for (String i : states) this.statesRejecting.add(Integer.parseInt(i));


        line = configReader.nextLine();
        lineSplited = line.split(":|;");        
        // check the format of line four, expect: "transitions:"
        if (! (lineSplited.length == 1)) throw new InvalidConfigFileException("Line 4 is invalid: Wrong Number of token");
        if (! lineSplited[0].equals("transitions")) throw new InvalidConfigFileException("Line 4 is invalid: Wrong field");
        
        int is, ns, r, w, i = 0;
        String[] initialState, finalState;
        direction d;
        while (configReader.hasNextLine()) {
            line = configReader.nextLine();
            lineSplited = line.split(":|;");
            // read the transitions for the turing machine, format expected: (/state/,/read/):(/nextState/,/write/,RIGHT/LEFT);
            if (! (lineSplited.length == 2)) throw new InvalidConfigFileException("Line " + (i+5) + " is invalid: Wrong Number of token");
            
            initialState = lineSplited[0].split(",");
            if (! (initialState.length == 2)) throw new InvalidConfigFileException("Line " + (i+5) + " is invalid: Inavlid transition start");
            
            finalState = lineSplited[1].split(",|;");
            if (! (finalState.length == 3)) throw new InvalidConfigFileException("Line " + (i+5) + " is invalid: Inavlid transition end");
            
            is = Integer.parseInt(initialState[0].substring(1,initialState[0].length()));  // remove the initial parenthesis
            r = Integer.parseInt(initialState[1].substring(0,initialState[1].length() - 1));  // remove the final parenthesis
            ns = Integer.parseInt(finalState[0].substring(1,finalState[0].length())); // remove the initial parenthesis
            w = Integer.parseInt(finalState[1]);  // just convert should be alright
            d = direction.valueOf(finalState[2].substring(0,finalState[2].length() - 1));  // remove the final parenthesis
            //populate the transition map
            this.transitions.put((this.statesNb * r + is), new Triplet<>(ns, w, d));
            
        }
    }

    public TuringMachine (String configPath, String tapePath, boolean display, boolean debug) throws FileNotFoundException, InvalidConfigFileException, InvalidTapeFileException {
        // initialise the turing machine and configure it from the config file given
        // give empty string if no file required
        
        // Init
        this.statesNb = 0;
        this.statesAccepting = new ArrayList<>();
        this.statesRejecting = new ArrayList<>();
        this.tapePositives = new ArrayList<>();
        this.tapeNegatives = new ArrayList<>();
        this.transitions = new HashMap<>();
        this.currentState = 0;
        this.currentIndex = 0;
        this.debug = debug;
        this.display = display;

        if (this.debug) System.out.println("Successfully initialized");

        // Parse the given files
        if (! configPath.isEmpty()){
            File configFile = new File(configPath);
            Scanner configReader = new Scanner(configFile);    
            
            if (this.debug) System.out.println("Turing Machine config file: " + configFile);
            
            try {
                this.parseConfig(configReader);
                if (this.debug) System.out.println("    Successfully Parsed");
            } catch (Exception e) {
                configReader.close();
                throw e;
            }

            
        }
        
        if (! tapePath.isEmpty()) {
            File tapeFile = new File(tapePath);
            Scanner tapeReader = new Scanner(tapeFile); 
            
            if (this.debug) System.out.println("Turing Machine tape file: " + tapePath); 

            try {
                this.parseTape(tapeReader);
                if (this.debug) System.out.println("    Successfully Parsed");
            } catch (Exception e) {
                tapeReader.close();
                throw e;
            }


        }

        if(this.debug) System.out.println("Ready to Run\n");
    }
}