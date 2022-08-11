package net.minecraft.src;

public class PlayerController extends PlayerControllerMP
{
    /** A reference to the Minecraft object. */
    protected final Minecraft mc;
    public boolean isInTestMode = false;

    public PlayerController(Minecraft par1Minecraft)
    {
    	super(par1Minecraft, null);
        this.mc = par1Minecraft;
    }

    @Override
    public boolean func_78763_f()
    {
        return true;
    }

    @Override
    public void setGameType(EnumGameType par1EnumGameType) {}

    /**
     * GuiEnchantment uses this during multiplayer to tell PlayerControllerMP to send a packet indicating the
     * enchantment action the player has taken.
     */
    @Override
    public void sendEnchantPacket(int par1, int par2) {}

    /**
     * Called by Minecraft class when the player is hitting a block with an item. Args: x, y, z, side
     */
    @Override
    public void clickBlock(int var1, int var2, int var3, int var4) {}

    /**
     * Called when a player completes the destruction of a block
     */
    @Override
    public boolean onPlayerDestroyBlock(int par1, int par2, int par3, int par4)
    {
        World var5 = this.mc.theWorld;
        Block var6 = Block.blocksList[var5.getBlockId(par1, par2, par3)];

        if (var6 == null)
        {
            return false;
        }
        else
        {
            var5.playAuxSFX(2001, par1, par2, par3, var6.blockID + (var5.getBlockMetadata(par1, par2, par3) << 12));
            int var7 = var5.getBlockMetadata(par1, par2, par3);
            boolean var8 = var5.setBlock(par1, par2, par3, 0, 0, 3);

            if (var8)
            {
                var6.onBlockDestroyedByPlayer(var5, par1, par2, par3, var7);
            }

            return var8;
        }
    }

    /**
     * Called when a player damages a block and updates damage counters
     */
    @Override
    public void onPlayerDamageBlock(int var1, int var2, int var3, int var4) {}

    /**
     * Resets current block damage and isHittingBlock
     */
    @Override
    public void resetBlockRemoving() {}

    public void setPartialTime(float par1) {}

    /**
     * player reach distance = 4F
     */
    @Override
    public float getBlockReachDistance()
    {
    	return 5F; // Removed old reach distance variance - Schf
    }

    /**
     * Notifies the server of things like consuming food, etc...
     */
    @Override
    public boolean sendUseItem(EntityPlayer par1EntityPlayer, World par2World, ItemStack par3ItemStack)
    {
        int var4 = par3ItemStack.stackSize;
        ItemStack var5 = par3ItemStack.useItemRightClick(par2World, par1EntityPlayer);

        if (var5 == par3ItemStack && (var5 == null || var5.stackSize == var4))
        {
            return false;
        }
        else
        {
            par1EntityPlayer.inventory.mainInventory[par1EntityPlayer.inventory.currentItem] = var5;

            if (var5.stackSize == 0)
            {
                par1EntityPlayer.inventory.mainInventory[par1EntityPlayer.inventory.currentItem] = null;
            }

            return true;
        }
    }

    @Override
    public void func_78752_a(ItemStack par1ItemStack) {}

    /**
     * Flips the player around. Args: player
     */
    @Override
    public void flipPlayer(EntityPlayer par1EntityPlayer) {}

    @Override
    public void updateController() {}

    @Override
    public boolean shouldDrawHUD()
    {
    	return true;
    }

    @Override
    public void setPlayerCapabilities(EntityPlayer par1EntityPlayer)
    {
    	this.mc.setGameMode(EnumGameType.SURVIVAL);
    }

    /**
     * Handles a players right click
     */
    @Override
    public boolean onPlayerRightClick(EntityPlayer var1, World var2, ItemStack var3, int var4, int var5, int var6, int var7, Vec3 var8)
    {
    	return false;
    }

    @Override
    public EntityClientPlayerMP func_78754_a(World par1World)
    {
        try
        {
            Object o = this.mc.playerClass.getDeclaredConstructor(new Class[]
            {
            	Minecraft.class, World.class, Session.class, Integer.TYPE
            }).
            newInstance(new Object[]
            {
            	this.mc, par1World, this.mc.getSession(), par1World.provider.dimensionId
            });
            return (ProvisionalEntityPlayerSP2)o;
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        return null;
//         return new EntityPlayerSP2(mc, par1World, mc.getSession(), par1World.provider.dimensionId);
    }

    /**
     * Interacts with an entity
     */
    @Override
    public boolean func_78768_b(EntityPlayer par1EntityPlayer, Entity par2Entity)
    {
    	return par1EntityPlayer.interactWith(par2Entity);
    }

    /**
     * Attacks an entity
     */
    @Override
    public void attackEntity(EntityPlayer par1EntityPlayer, Entity par2Entity)
    {
        par1EntityPlayer.attackTargetEntityWithCurrentItem(par2Entity);
    }

    @Override
    public ItemStack windowClick(int par1, int par2, int par3, int par4, EntityPlayer par5EntityPlayer)
    {
        return par5EntityPlayer.openContainer.slotClick(par2, par3, par4, par5EntityPlayer);
    }

    public boolean func_35643_e()
    {
        return false;
    }

    @Override
    public void onStoppedUsingItem(EntityPlayer par1EntityPlayer)
    {
        par1EntityPlayer.stopUsingItem();
    }

    /**
     * Checks if the player is not creative, used for checking if it should break a block instantly
     */
    @Override
    public boolean isNotCreative()
    {
        return true;
    }

    /**
     * returns true if player is in creative mode
     */
    @Override
    public boolean isInCreativeMode()
    {
        return false;
    }

    /**
     * true for hitting entities far away.
     */
    @Override
    public boolean extendedReach()
    {
        return false;
    }

    /**
     * Used in PlayerControllerMP to update the server with an ItemStack in a slot.
     */
    @Override
    public void sendSlotPacket(ItemStack par1ItemStack, int par2)
    {
        EntityPlayer playerEntity = Minecraft.getMinecraft().thePlayer;
        if (Minecraft.getMinecraft().playerController.isInCreativeMode())
        {
            boolean var1 = par1ItemStack == null || par1ItemStack.itemID < Item.itemsList.length && par1ItemStack.itemID >= 0 && Item.itemsList[par1ItemStack.itemID] != null;
            boolean var2 = par1ItemStack == null || par1ItemStack.getItemDamage() >= 0 && par1ItemStack.getItemDamage() >= 0 && par1ItemStack.stackSize <= 64 && par1ItemStack.stackSize > 0;

            if (par2 >= 1 && par2 < 36 + InventoryPlayer.getHotbarSize() && var1 && var2)
            {
                if (par1ItemStack == null)
                {
                    playerEntity.inventoryContainer.putStackInSlot(par2, null);
                }
                else
                {
                    playerEntity.inventoryContainer.putStackInSlot(par2, par1ItemStack);
                }

                playerEntity.inventoryContainer.setPlayerIsPresent(playerEntity, true);
            }
            else if (par2 < 0 && var1 && var2)
            {
                EntityItem entityitem = playerEntity.dropPlayerItem(par1ItemStack);

                if (entityitem != null)
                {
                    entityitem.setAgeToCreativeDespawnTime();
                }
            }
        }
    }

    public void func_35639_a(ItemStack par1ItemStack) {}

    public static void clickBlockCreative(Minecraft par0Minecraft, PlayerControllerMP par1PlayerControllerMP, int par2, int par3, int par4, int par5) {}
}
