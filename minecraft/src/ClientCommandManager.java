package net.minecraft.src;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ClientCommandManager extends CommandHandler implements IAdminCommand
{
    private Set commandSet2;

    public ClientCommandManager()
    {
        this.registerCommand(new CommandClientTime());
        this.registerCommand(new CommandClientGameMode());
        this.registerCommand(new CommandClientDifficulty());
        this.registerCommand(new CommandClientDefaultGameMode());
        this.registerCommand(new CommandClientKill());
        this.registerCommand(new CommandClientToggleDownfall());
        this.registerCommand(new CommandClientWeather());
        this.registerCommand(new CommandClientExperience());
        this.registerCommand(new CommandClientTp());
        this.registerCommand(new CommandClientGive());
        this.registerCommand(new CommandClientEffect());
        this.registerCommand(new CommandClientEnchant());
        this.registerCommand(new CommandClientEmote());
        this.registerCommand(new CommandClientShowSeed());
        this.registerCommand(new CommandClientHelp());
        this.registerCommand(new CommandClientDebug());
        this.registerCommand(new CommandClientSay());
        this.registerCommand(new CommandClientSetSpawnpoint());
        this.registerCommand(new CommandClientGameRule());
        this.registerCommand(new CommandClientClear());
//         this.registerCommand(new ServerCommandTestFor());
//         this.registerCommand(new ServerCommandScoreboard());
        this.registerCommand(new CommandClientPublishLocal());
//        Minecraft.getMinecraft().addCommandsSP(this);
        CommandBase.setAdminCommander(this);
    }

    @Override
    public void notifyAdmins(ICommandSender par1ICommandSender, int par2, String par3Str, Object ... par4ArrayOfObj)
    {
        par1ICommandSender.sendChatToPlayer(ChatMessageComponent.createFromTranslationWithSubstitutions(par3Str, par4ArrayOfObj));
    }

    /**
     * adds the command and any aliases it has to the internal map of available commands
     */
    public ICommand unregisterCommand(String par1Str)
    {
        Map var1 = getCommands();
        ICommand var2 = ((ICommand)var1.get(par1Str));
        var1.remove(par1Str);
        List var3 = var2.getCommandAliases();
        if (var3 != null)
        {
            Iterator var4 = var3.iterator();
            do
            {
                if (!var4.hasNext())
                {
                    break;
                }
                String var5 = (String)var4.next();
                var1.remove(var5);
            }
            while (true);
        }
        getCommandSet().remove(var2);
        return var2;
    }

    private Set getCommandSet()
    {
        if (this.commandSet2 != null)
        {
            return this.commandSet2;
        }
        Field var1 = (CommandHandler.class).getDeclaredFields()[1];
        var1.setAccessible(true);
        try
        {
            return (Set)var1.get(this);
        }
        catch(Exception var2)
        {
        	var2.printStackTrace();
            return this.commandSet2;
        }
    }

    public static final ProvisionalEntityPlayerSP2 getPlayer(ICommandSender par1ICommandSender, String par2Str)
    {
        EntityPlayer var1 = Minecraft.getMinecraft().thePlayer;
        if (par1ICommandSender instanceof EntityPlayer)
        {
        	var1 = (EntityPlayer)par1ICommandSender;
        }
        if (par2Str == null || par2Str.equals(var1.getEntityName()))
        {
            return (ProvisionalEntityPlayerSP2)var1;
        }
        EntityPlayerMP var2 = PlayerSelector.matchOnePlayer(par1ICommandSender, par2Str);
        if (var2 != null && var2.getEntityName().equals(var1.getEntityName()))
        {
            return (ProvisionalEntityPlayerSP2)var1;
        }
        throw new PlayerNotFoundException();
    }
}
