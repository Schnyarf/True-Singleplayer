package net.minecraft.src;

public class CommandClientToggleDownfall extends CommandToggleDownfall
{
    @Override
    protected void toggleDownfall()
    {
        Minecraft.getMinecraft().theWorld.getWorldInfo().setRainTime(1);
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
