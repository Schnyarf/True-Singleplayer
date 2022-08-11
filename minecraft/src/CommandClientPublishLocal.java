package net.minecraft.src;

import net.minecraft.server.MinecraftServer;

public class CommandClientPublishLocal extends CommandServerPublishLocal
{
    @Override
    public void processCommand(ICommandSender par1ICommandSender, String par2ArrayOfStr[])
    {
        Minecraft.getMinecraft().quitAndStartServer();
        String s = MinecraftServer.getServer().shareToLAN(EnumGameType.SURVIVAL, false);
        ChatMessageComponent c = ChatMessageComponent.createFromTranslationKey("commands.publish.failed");
        if (s != null){
            c = ChatMessageComponent.createFromTranslationWithSubstitutions("commands.publish.started", new Object[]{s});
        }
        par1ICommandSender.sendChatToPlayer(c);
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
