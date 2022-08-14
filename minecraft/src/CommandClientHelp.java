package net.minecraft.src;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CommandClientHelp extends CommandHelp
{
    @Override
    protected List getSortedPossibleCommands(ICommandSender par1ICommandSender)
    {
    	return Minecraft.getMinecraft().getCommandManager().getPossibleCommands(par1ICommandSender);
    }

    @Override
    protected Map getCommands()
    {
        return Minecraft.getMinecraft().getIntegratedServer().getCommandManager().getCommands();
    }

    /**
     * Returns true if the given command sender is allowed to use this command.
     */
    @Override
    public boolean canCommandSenderUseCommand(ICommandSender par1ICommandSender)
    {
        return true;
    }
}
