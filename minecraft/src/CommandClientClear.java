package net.minecraft.src;

import java.util.List;

public class CommandClientClear extends CommandClearInventory
{
    @Override
    public void processCommand(ICommandSender par1ICommandSender, String par2ArrayOfStr[])
    {
    	ProvisionalEntityPlayerSP2 var3 = ClientCommandManager.getPlayer(par1ICommandSender, par2ArrayOfStr.length == 3 ? par2ArrayOfStr[0] : null);
        int var4 = par2ArrayOfStr.length < 2 ? -1 : parseIntWithMin(par1ICommandSender, par2ArrayOfStr[1], 1);
        int var5 = par2ArrayOfStr.length < 3 ? -1 : parseIntWithMin(par1ICommandSender, par2ArrayOfStr[2], 0);
        int var6 = var3.inventory.clearInventory(var4, var5);
        var3.inventoryContainer.detectAndSendChanges();
        this.notifyAdmins(par1ICommandSender, "commands.clear.success", new Object[] {var3.getEntityName(), Integer.valueOf(var6)});
    }

    /**
     * Returns true if the given command sender is allowed to use this command.
     */
    @Override
    public boolean canCommandSenderUseCommand(ICommandSender par1ICommandSender)
    {
        return Minecraft.getMinecraft().theWorld.getWorldInfo().areCommandsAllowed();
    }
}
