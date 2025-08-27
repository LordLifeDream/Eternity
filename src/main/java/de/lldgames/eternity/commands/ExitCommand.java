package de.lldgames.eternity.commands;

public class ExitCommand extends Command{
    public ExitCommand(){
        super("Exit",
                new String[]{"exit", "close", "quit", "bye"}
                );
        this.description = "Exits the program.";
    }
    @Override
    public void execute(String[] params) {
        System.out.println("goodbye!");
        System.exit(0);
    }
}
