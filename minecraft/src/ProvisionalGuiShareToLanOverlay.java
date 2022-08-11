package net.minecraft.src;

public class ProvisionalGuiShareToLanOverlay extends ProvisionalGuiOverlay
{
    public ProvisionalGuiShareToLanOverlay()
    {
        super(GuiShareToLan.class);
    }

    @Override
    public boolean shouldBeAdded()
    {
        return Minecraft.getMinecraft().enableSP;
    }

    @Override
    public boolean actionPerformed(GuiScreen gui, GuiButton button)
    {
        if (button.id == 101)
        {
            Minecraft mc = Minecraft.getMinecraft();
            mc.displayGuiScreen(null);
            mc.quitAndStartServer();
        }
        return true;
    }
}
