package de.lldgames.eternity.commands;

public class HelpCommand extends Command{
    public HelpCommand(){
        super("Help", new String[]{"help", "h", "?"});
        this.description = "prints a list of commands";
    }
    @Override
    public void execute(String[] params) {
        System.out.println("--total commands: " + Command.COMMANDS.size()+"---");
        Command.COMMANDS.forEach(Command::printHelp);
    }
}
