package net.minecraft.src;

import org.lwjgl.input.Keyboard;

public class PlayerControllerDemo extends PlayerControllerSP
{
    private boolean field_55296_c = false;
    private boolean field_55294_d = false;
    private int field_55295_e = 0;
    private int field_55293_f = 0;

    public PlayerControllerDemo(Minecraft par1Minecraft)
    {
        super(par1Minecraft);
    }

    @Override
    public void updateController()
    {
        super.updateController();
        ++this.field_55293_f;
        long var1 = this.mc.theWorld.getTotalWorldTime();
        long var3 = var1 / 24000L + 1L;

        if (!this.field_55296_c && this.field_55293_f > 20)
        {
            this.field_55296_c = true;
            this.mc.displayGuiScreen(new GuiScreenDemo());
        }

        this.field_55294_d = var1 > 120500L;

        if (this.field_55294_d)
        {
            ++this.field_55295_e;
        }

        if (var1 % 24000L == 500L)
        {
            if (var3 <= 6L)
            {
                this.mc.ingameGUI.getChatGUI().addTranslatedMessage("demo.day." + var3); // var3 used to be followed by ', null', which seemed to have just been creating a warning - Schf
            }
        }
        else if (var3 == 1L)
        {
            GameSettings var5 = this.mc.gameSettings;
            String var7 = null;

            if (var1 == 100L)
            {
                var7 = I18n.getString("demo.help.movement");
                var7 = String.format(var7, new Object[] {Keyboard.getKeyName(var5.keyBindForward.keyCode), Keyboard.getKeyName(var5.keyBindLeft.keyCode), Keyboard.getKeyName(var5.keyBindBack.keyCode), Keyboard.getKeyName(var5.keyBindRight.keyCode)});
            }
            else if (var1 == 175L)
            {
                var7 = I18n.getString("demo.help.jump");
                var7 = String.format(var7, new Object[] {Keyboard.getKeyName(var5.keyBindJump.keyCode)});
            }
            else if (var1 == 250L)
            {
                var7 = I18n.getString("demo.help.inventory");
                var7 = String.format(var7, new Object[] {Keyboard.getKeyName(var5.keyBindInventory.keyCode)});
            }

            if (var7 != null)
            {
                this.mc.ingameGUI.getChatGUI().addTranslatedMessage(var7); // var7 used to be followed by ', null', which seemed to have just been creating a warning - Schf
            }
        }
        else if (var3 == 5L && var1 % 24000L == 22000L)
        {
            this.mc.ingameGUI.getChatGUI().addTranslatedMessage("demo.day.warning"); // "demo.day.warning" used to be followed by ', null', which seemed to have just been creating a warning - Schf
        }
    }

    private void func_55292_j()
    {
        if (this.field_55295_e > 100)
        {
            this.mc.ingameGUI.getChatGUI().addTranslatedMessage("demo.reminder"); // "demo.reminder" used to be followed by ', null', which seemed to have just been creating a warning - Schf
            this.field_55295_e = 0;
        }
    }

    /**
     * Called by Minecraft class when the player is hitting a block with an item. Args: x, y, z, side
     */
    @Override
    public void clickBlock(int par1, int par2, int par3, int par4)
    {
        if (this.field_55294_d)
        {
            this.func_55292_j();
        }
        else
        {
            super.clickBlock(par1, par2, par3, par4);
        }
    }

    /**
     * Called when a player damages a block and updates damage counters
     */
    @Override
    public void onPlayerDamageBlock(int par1, int par2, int par3, int par4)
    {
        if (!this.field_55294_d)
        {
            super.onPlayerDamageBlock(par1, par2, par3, par4);
        }
    }

    /**
     * Called when a player completes the destruction of a block
     */
    @Override
    public boolean onPlayerDestroyBlock(int par1, int par2, int par3, int par4)
    {
        return this.field_55294_d ? false : super.onPlayerDestroyBlock(par1, par2, par3, par4);
    }

    /**
     * Notifies the server of things like consuming food, etc...
     */
    @Override
    public boolean sendUseItem(EntityPlayer par1EntityPlayer, World par2World, ItemStack par3ItemStack)
    {
        if (this.field_55294_d)
        {
            this.func_55292_j();
            return false;
        }
        else
        {
            return super.sendUseItem(par1EntityPlayer, par2World, par3ItemStack);
        }
    }

    /**
     * Handles a players right click
     */
    @Override
    public boolean onPlayerRightClick(EntityPlayer par1EntityPlayer, World par2World, ItemStack par3ItemStack, int par4, int par5, int par6, int par7, Vec3 par8Vec3)
    {
        if (this.field_55294_d)
        {
            this.func_55292_j();
            return false;
        }
        else
        {
            return super.onPlayerRightClick(par1EntityPlayer, par2World, par3ItemStack, par4, par5, par6, par7, par8Vec3);
        }
    }

    /**
     * Attacks an entity
     */
    @Override
    public void attackEntity(EntityPlayer par1EntityPlayer, Entity par2Entity)
    {
        if (this.field_55294_d)
        {
            this.func_55292_j();
        }
        else
        {
            super.attackEntity(par1EntityPlayer, par2Entity);
        }
    }
}
