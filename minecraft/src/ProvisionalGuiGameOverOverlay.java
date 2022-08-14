/*package net.minecraft.src;

import java.util.List;

public class ProvisionalGuiGameOverOverlay extends ProvisionalGuiOverlay
{
    public ProvisionalGuiGameOverOverlay()
    {
        super(GuiGameOver.class);
    }

    @Override
    public boolean shouldBeAdded()
    {
        return Minecraft.getMinecraft().enableSP;
    }

    @Override
    public void initGui(List buttonList, int width, int height)
    {
        if (Minecraft.getMinecraft().theWorld.getWorldInfo().isHardcoreModeEnabled())
        {
        	this.renameButton(buttonList, 1, I18n.getString("deathScreen.deleteWorld"));
        }
    }

    @Override
    public boolean actionPerformed(GuiScreen gui, GuiButton par1GuiButton)
    {
        Minecraft mc = Minecraft.getMinecraft();
        if (par1GuiButton.id == 1)
        {
            boolean hardcore = mc.theWorld.getWorldInfo().isHardcoreModeEnabled();
            if (hardcore)
            {
                String s = mc.theWorld.getSaveHandler().getWorldDirectoryName();
                mc.exitToMainMenu("Deleting world");
                ISaveFormat isaveformat = mc.getSaveLoader();
                isaveformat.flushCache();
                isaveformat.deleteWorldDirectory(s);
                mc.displayGuiScreen(new GuiMainMenu());
            }
            return !hardcore;
        }
        else if (par1GuiButton.id == 2)
        {
            mc.changeWorld1(null);
            mc.displayGuiScreen(new GuiMainMenu());
            return false;
        }
        return true;
    }
} */
