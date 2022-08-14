/* package net.minecraft.src;

import java.util.List;

public class ProvisionalGuiIngameMenuOverlay extends ProvisionalGuiOverlay
{
    private int updateCounter;
    private int updateCounter2;

    public ProvisionalGuiIngameMenuOverlay()
    {
        super(GuiIngameMenu.class);
    }

    @Override
    public boolean shouldBeAdded()
    {
        return Minecraft.getMinecraft().enableSP;
    }

    @Override
    public void onAdded(GuiScreen gui)
    {
        this.updateCounter = 0;
        this.updateCounter2 = 0;
    }

    @Override
    public void initGui(List buttonList, int width, int height)
    {
    	this.updateCounter2 = 0;
        this.renameButton(buttonList, 1, I18n.getString("menu.returnToMenu"));
        if (false) // Open to LAN button - Schf
        {
        	this.resizeButton(buttonList, 0, 200, 20);
            this.hideButton(buttonList, 7, false);
        }
        else
        {
        	this.disableButton(buttonList, 7, true);
        }
    }

    @Override
    public boolean actionPerformed(GuiScreen gui, GuiButton par1GuiButton)
    {
        Minecraft mc = Minecraft.getMinecraft();
        switch (par1GuiButton.id)
        {
            case 1:
                mc.statFileWriter.readStat(StatList.leaveGameStat, 1);
                mc.changeWorld1(null);
                mc.displayGuiScreen(new GuiMainMenu());
                return false;
            case 4:
                mc.displayGuiScreen(null);
                mc.setIngameFocus();
                mc.sndManager.resumeAllSounds(); //?
                return false;
        }
        return true;
    }

    @Override
    public void postUpdateScreen(GuiScreen gui)
    {
    	this.updateCounter++;
    }

    @Override
    public void postDrawScreen(GuiScreen gui, int x, int y, float f)
    {
        boolean flag = !((ProvisionalWorldSSP)Minecraft.getMinecraft().theWorld).quickSaveWorld(this.updateCounter2++);
        if (flag || this.updateCounter < 20)
        {
            float f2 = ((float)(this.updateCounter % 10) + f) / 10F;
            f2 = MathHelper.sin(f2 * (float)Math.PI * 2.0F) * 0.2F + 0.8F;
            int i = (int)(255F * f2);
            gui.drawString(gui.getFontRenderer(), "Saving level..", 8, gui.height - 16, i << 16 | i << 8 | i);
        }
    }
} */
