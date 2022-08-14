package net.minecraft.src;

import java.util.List;

public class CommandClientGameRule extends CommandGameRule
{
    @Override
    public void processCommand(ICommandSender par1ICommandSender, String par2ArrayOfStr[])
    {
        if (par2ArrayOfStr.length == 2)
        {
            String s = par2ArrayOfStr[0];
            String s2 = par2ArrayOfStr[1];
            GameRules gamerules2 = func_82366_d();

            if (gamerules2.hasRule(s))
            {
                gamerules2.setOrCreateGameRule(s, s2);
                notifyAdmins(par1ICommandSender, "commands.gamerule.success", new Object[0]);
            }
            else
            {
                notifyAdmins(par1ICommandSender, "commands.gamerule.norule", new Object[]
                        {
                            s
                        });
            }

            return;
        }

        if (par2ArrayOfStr.length == 1)
        {
            String s1 = par2ArrayOfStr[0];
            GameRules gamerules1 = func_82366_d();

            if (gamerules1.hasRule(s1))
            {
                String s3 = gamerules1.getGameRuleStringValue(s1);
                par1ICommandSender.sendChatToPlayer(ChatMessageComponent.createFromText(s1).addText(" = ").addText(s3));
            }
            else
            {
                notifyAdmins(par1ICommandSender, "commands.gamerule.norule", new Object[]
                        {
                            s1
                        });
            }

            return;
        }

        if (par2ArrayOfStr.length == 0)
        {
            GameRules gamerules = func_82366_d();
            par1ICommandSender.sendChatToPlayer(ChatMessageComponent.createFromText(joinNiceString(gamerules.getRules())));
            return;
        }
        else
        {
            throw new WrongUsageException("commands.gamerule.usage", new Object[0]);
        }
    }

    private GameRules func_82366_d()
    {
        return Minecraft.getMinecraft().theWorld.getGameRules();
    }

    /**
     * Adds the strings available in this command to the given list of tab completion options.
     */
    @Override
    public List addTabCompletionOptions(ICommandSender par1ICommandSender, String par2ArrayOfStr[])
    {
        if (par2ArrayOfStr.length == 1)
        {
            return getListOfStringsMatchingLastWord(par2ArrayOfStr, func_82366_d().getRules());
        }

        if (par2ArrayOfStr.length == 2)
        {
            return getListOfStringsMatchingLastWord(par2ArrayOfStr, new String[]
                    {
                        "true", "false"
                    });
        }
        else
        {
            return null;
        }
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
