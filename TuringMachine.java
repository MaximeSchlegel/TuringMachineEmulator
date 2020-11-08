import java.util.ArrayList;
import java.util.HashMap;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class TuringMachine {

    public static enum direction {
        RIGHT, LEFT
    }; // direction allowed for the moves

    private int statesNb; // number of state, represent the list of state (s0 ... s_nbState-1)
    private ArrayList<Integer> statesAccepting; // list of the final accepting state of the turring machine

    private ArrayList<Integer> tapePositives; // tape for the positvite indexes (include 0)
    private ArrayList<Integer> tapeNegatives; // tape for the negative indexes (exclude 0)

    private HashMap<Integer, Triplet<Integer, Integer, direction>> transitions; // store the transitions: (state, read)
                                                                                // -> (nextState, write, move)

    private int currentState; // current state of the turring machine
    private int currentIndex; // current index of the reading head

    private boolean ran, accepted;
    private boolean debug, display;

    public int getFinalState() throws TuringMachineException {
        if (!this.ran)
            throw new TuringMachineException("Try runnig the turing machine before accessing its final state");
        return this.currentState;
    }

    public boolean getAccepted() throws TuringMachineException {
        if (!this.ran)
            throw new TuringMachineException("Try runnig the turing machine before accessing its accepting state");
        return this.accepted;
    }

    private Triplet<Integer, Integer, direction> readToTransition(int read) {
        // return the transition link to the state, read couple
        return this.transitions.get(this.statesNb * read + this.currentState);
    }

    private int tapeRead() {
        if (this.currentIndex >= 0) {
            // if the cell does not exist, create it
            // as the machine read once every iteration it can't be more than 1 away form
            // the end of the array
            if (this.tapePositives.size() < this.currentIndex + 1)
                this.tapePositives.add(0);
            return this.tapePositives.get(this.currentIndex); // simply read the positive tape
        } else {
            // if the cell does not exist, create it
            if (this.tapeNegatives.size() < -this.currentIndex)
                this.tapeNegatives.add(0);
            return this.tapeNegatives.get(-this.currentIndex - 1); // account for 0 on positive tape and change of sign
        }
    }

    private void tapeWrite(int toWrite) {
        // can't write before read so the cell must exist
        if (this.currentIndex >= 0) {
            this.tapePositives.set(this.currentIndex, toWrite);
        } else {
            this.tapeNegatives.set(-this.currentIndex - 1, toWrite); // account for 0 on positive tape and change of
                                                                     // sign
        }
    }

    private void move(direction d) {
        if (d == direction.RIGHT) {
            this.currentIndex++;
        } else {
            this.currentIndex--;
        }
    }

    public void execute() {
        int read, i = 0;
        Triplet<Integer, Integer, direction> transition;
        StringBuilder transitionDiplay;

        if (this.debug || this.display) {
            System.out.println("Execution :");
            System.out.println("  Step  |  Tape Pos  |  Transition");
        }

        // Read the first cell to initialize the machine
        read = this.tapeRead();

        // iterate until the machine reach a state where no transition is define for the
        // current value
        while (this.transitions.containsKey(this.statesNb * read + this.currentState)) {

            transition = this.readToTransition(read); // get the transition for the current (state, read)
            
            if (this.debug || this.display) {
                // Display the transition
                transitionDiplay = new StringBuilder("( " + this.currentState + " ; ");
                transitionDiplay.append(read + " ) => ");
                transitionDiplay.append(transition);
                System.out.printf("  %4d  |  %-+8d  |  %s\n", (i++), this.currentIndex, transitionDiplay.toString());
            }

            this.currentState = transition.getFirst(); // update the state of the machine
            this.tapeWrite(transition.getSecond()); // write on the tape
            this.move(transition.getThird()); // move the reading head

            read = this.tapeRead(); // read the new cell
        }

        if (this.debug || this.display) {
            System.out.printf("Can't find the transition for: (state: %d ; read: %d)\n", this.currentState, read);
            System.out.printf("Current Tape position: %d \n", this.currentIndex);
            System.out.print(("    "));
            for (i = -this.tapeNegatives.size(); i < this.tapePositives.size(); i++)
                System.out.printf((i < 0) ? " %+4d " : " %-+4d ", i);
            System.out.print("\n    ");
            for (i = -this.tapeNegatives.size(); i < this.tapePositives.size(); i++)
                System.out.printf((i < 0) ? " %3d  " : "  %-3d ",
                        (i < 0) ? this.tapeNegatives.get(-i - 1) : tapePositives.get(i));
            System.out.println();
        }

        this.ran = true;
        this.accepted = statesAccepting.contains(this.currentState);
        if (this.debug || this.display)
            System.out.println("Done\n");
    }

    private void parseTape(Scanner tapeReader) throws InvalidTapeFileException {
        // parse the tape file to initialize the machine.
        // format expected: /int;/int/;...;/int/
        //        cell nb :   0    1        n-1

        if (!tapeReader.hasNextLine())
            throw new InvalidTapeFileException("File is empty");

        String line = tapeReader.nextLine();
        String[] lineSplit = line.split(";");
        int toAdd, i=0;

        while (i < this.tapePositives.size() && i < lineSplit.length) {
            // the initial position of the turing machine is offset, so we just rewrite the value
            try {
                toAdd = Integer.parseInt(lineSplit[i]);
                this.tapePositives.set(i, toAdd);
            } catch (Exception e) {
                throw new InvalidTapeFileException("Can't parse cell nb " + i + " : " + lineSplit[i]);
            }
            i++;
        }

        while (i < lineSplit.length && i < lineSplit.length) {
            // after the end of the pregenerated tape (by the offset), just append the value
            try {
                toAdd = Integer.parseInt(lineSplit[i]);
                this.tapePositives.add(toAdd);
            } catch (Exception e) {
                throw new InvalidTapeFileException("Can't parse cell nb " + i + " : " + lineSplit[i]);
            }
            i++;
        }
    }

    private void parseConfig (Scanner configReader) throws InvalidConfigFileException {
        // Input in the turing machine the configuration file

        if (! configReader.hasNextLine()) throw new InvalidConfigFileException("File is empty");

        String line;
        String[] lineSplited, initialState, finalState;
        int params=0, i=0, is, ns, r, w;;
        boolean intransitions = false;
        direction d;

        // read the config file while there is lines to read
        while (configReader.hasNextLine()) {
            line = configReader.nextLine();
            lineSplited = line.split(":|;");
            i++;

            if (intransitions) {
                // parse the transition lines
                if (line.endsWith(";")) intransitions = false; // we have read all the transitions
                
                // read the transitions for the turing machine, format expected: (/state/,/read/):(/nextState/,/write/,RIGHT/LEFT);
                if (! (lineSplited.length == 2)) throw new InvalidConfigFileException("Line " + i + " is invalid:\n"
                                                                                    + "      read: " + line + "\n"
                                                                                    + "    expect: (/state/,/read/):(/nextState/,/write/,RIGHT/LEFT)\n");

                initialState = lineSplited[0].split(",");
                if (! (initialState.length == 2)) throw new InvalidConfigFileException("Line " + i + " is invalid:\n"
                                                                                     + "      read: " + line + "\n"
                                                                                     + "    expect: (/state/,/read/):(/nextState/,/write/,RIGHT/LEFT)\n");
                finalState = lineSplited[1].split(",|;");
                if (! (finalState.length == 3)) throw new InvalidConfigFileException("Line " + i + " is invalid:\n"
                                                                                   + "      read: " + line + "\n"
                                                                                   + "    expect: (/state/,/read/):(/nextState/,/write/,RIGHT/LEFT)\n");
                
                is = Integer.parseInt(initialState[0].substring(1,initialState[0].length()));       // remove the initial parenthesis
                r = Integer.parseInt(initialState[1].substring(0,initialState[1].length() - 1));    // remove the final parenthesis
                ns = Integer.parseInt(finalState[0].substring(1,finalState[0].length()));           // remove the initial parenthesis
                w = Integer.parseInt(finalState[1]);                                                // just convert should be alright
                d = direction.valueOf(finalState[2].substring(0,finalState[2].length() - 1));       // remove the final parenthesis

                //populate the transition map
                this.transitions.put((this.statesNb * r + is), new Triplet<>(ns, w, d));
            } else {
                // parse all the other lines
            
                if (lineSplited.length < 1) throw new InvalidConfigFileException("Line " + i + " is invalid: \n"
                                                                                + "      read: " + line + "\n");
                
                switch (lineSplited[0]) {
                    case "state_number":
                        // expect: "state_number:/int/;"
                        if (! (lineSplited.length == 2)) throw new InvalidConfigFileException("Line " + i + " is invalid:\n"
                                                                                            + "      read: " + line + "\n"
                                                                                            + "    expect: state_number:/int/;\n");
                        this.statesNb = Integer.parseInt(lineSplited[1]);
                        params++;
                        break;
                    case "accepting_states":
                        // expect: "accepting_states:/int/,/int/,...,/int/;"
                        if (! (lineSplited.length == 2)) throw  new InvalidConfigFileException("Line " + i + " is invalid:\n"
                                                                                            + "      read: " + line + "\n"
                                                                                            + "    expect: accepting_states:/int/,/int/,...,/int/;\n");
                        String[] states = lineSplited[1].split(",");
                        for (String s : states) this.statesAccepting.add(Integer.parseInt(s));
                        params++;
                        break;
                    case "tape_offset":
                        // with field is optional it will offset the position of the turing machine on the tape
                        // check the format, expect: "tape_offset:/int/;"
                        if (! (lineSplited.length == 2)) throw  new InvalidConfigFileException("Line " + i + " is invalid:\n"
                                                                                            + "      read: " + line + "\n"
                                                                                            + "    expect: tape_offset:/int/;\n");
                        this.currentIndex = Integer.parseInt(lineSplited[1]);
                        for(int o=0; o <= this.currentIndex; o++) this.tapePositives.add(0);
                        break;
                    case "transitions":
                        // check the format, expect: "transitions:"
                        if (! (lineSplited.length == 1)) throw new InvalidConfigFileException("Line " + i + " is invalid:\n"
                                                                                            + "      read: " + line + "\n"
                                                                                            + "    expect: transitions:\n");
                        intransitions = true; // we are ready to parse transition
                        params++;
                        break;
                    default:
                        throw new InvalidConfigFileException("Line " + i + " is invalid: Unkown argument\n"
                                                        + "      read: " + line + "\n");
                }
            }
        }

        // they are 3 mandatory parameters so we check if we have parsed all of them
        if (params != 3) throw new InvalidConfigFileException("Wrong number of parameters");
    }

    public TuringMachine(String configPath, String tapePath, boolean display, boolean debug)
            throws FileNotFoundException, InvalidConfigFileException, InvalidTapeFileException {
        // initialise the turing machine and configure it from the config file given
        // give empty string if no file required

        // Init
        this.statesNb = 0;
        this.statesAccepting = new ArrayList<>();
        this.tapePositives = new ArrayList<>();
        this.tapeNegatives = new ArrayList<>();
        this.transitions = new HashMap<>();
        this.currentState = 0;
        this.currentIndex = 0;
        this.debug = debug;
        this.display = display;
        this.ran = false;
        this.accepted = false;

        if (this.debug)
            System.out.println("Successfully initialized");

        // Parse the given files
        if (!configPath.isEmpty()) {
            File configFile = new File(configPath);
            Scanner configReader = new Scanner(configFile);

            if (this.debug)
                System.out.println("Turing Machine config file: " + configFile);
            
            try {
                this.parseConfig(configReader);
                if (this.debug)
                    System.out.println("    Successfully Parsed");
            } catch (Exception e) {
                configReader.close();
                throw e;
            }

            configReader.close();
        }

        if (!tapePath.isEmpty()) {
            File tapeFile = new File(tapePath);
            Scanner tapeReader = new Scanner(tapeFile);

            if (this.debug)
                System.out.println("Turing Machine tape file: " + tapePath);

            try {
                this.parseTape(tapeReader);
                if (this.debug)
                    System.out.println("    Successfully Parsed");
            } catch (Exception e) {
                tapeReader.close();
                throw e;
            }

            tapeReader.close();
        }

        if (this.debug)
            System.out.println("Ready to Run\n");
    }
}