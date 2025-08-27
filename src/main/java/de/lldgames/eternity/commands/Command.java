package de.lldgames.eternity.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public abstract class Command {
    public static final ArrayList<Command> COMMANDS = new ArrayList<>();
    public static final HashMap<Class<? extends Command>, Command> INSTANCES = new HashMap<>();

    private String name;
    private String[] aliases;
    protected String description = "default command description";
    protected Command(String name, String[] aliases){
        this.name = name;
        this.aliases = aliases;
    }

    /**
     * should end w/ newline, so use println
     */
    public void printHelp(){
        System.out.println(this.name+": "+this.description);
        System.out.println(" aka "+ Arrays.toString(aliases));
    }

    public boolean isCall(String callSeq){
        for(String alias: aliases){
            if(alias.equalsIgnoreCase(callSeq)) return true;
        }
        return false;
    }

    public abstract void execute(String[] params);

    public static void registerCMDs(){
        rc(new ExitCommand());
        rc(new StopCommand());
        rc(new StartCommand());
        rc(new HelpCommand());
        rc(new ListCommand());
    }
    private static void rc(Command c){
        registerCmd(c);
    }
    private static void registerCmd(Command c){
        COMMANDS.add(c);
        INSTANCES.put(c.getClass(), c);
    }

    /**
     * finds and calls a command based on tokenized user input
     * @param tokens the user input, should be system.in split for " "
     */
    public static void callCmd(String[] tokens){
        String cmd = tokens[0];
        for(Command c: COMMANDS){
            if(c.isCall(cmd)) c.execute(Arrays.copyOfRange(tokens, 1, tokens.length));
        }
    }
}
