package net.minecraft.src;

import java.util.List;

public class ProvisionalGuiMainMenuOverlay extends ProvisionalGuiOverlay
{
    public ProvisionalGuiMainMenuOverlay()
    {
        super(GuiMainMenu.class);
    }

    @Override
    public boolean actionPerformed(GuiScreen gui, GuiButton par1GuiButton)
    {
        if (par1GuiButton.id == 11)
        {
            Minecraft mc = Minecraft.getMinecraft();
            mc.enableSP = mc.useSP;
            if (mc.enableSP)
            {
                mc.playerController = new PlayerControllerDemo(mc);
                mc.startWorldSSP("Demo_World", "Demo_World", DemoWorldServer.demoWorldSettings);
                mc.displayGuiScreen(null);
            }
            return !mc.enableSP;
        }
        return true;
    }
}
