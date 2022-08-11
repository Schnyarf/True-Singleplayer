package net.minecraft.src;

public class PlayerControllerSP extends PlayerController
{
    private int curBlockX = -1;
    private int curBlockY = -1;
    private int curBlockZ = -1;
    private float curBlockDamage = 0.0F;
    private float prevBlockDamage = 0.0F;
    private float blockDestroySoundCounter = 0.0F;
    private int blockHitWait = 0;

    public PlayerControllerSP(Minecraft par1Minecraft)
    {
        super(par1Minecraft);
    }

    /**
     * Flips the player around. Args: player
     */
    @Override
    public void flipPlayer(EntityPlayer par1EntityPlayer)
    {
        par1EntityPlayer.rotationYaw = -180.0F;
    }

    @Override
    public boolean shouldDrawHUD()
    {
        return true;
    }

    /**
     * Called when a player completes the destruction of a block
     */
    @Override
    public boolean onPlayerDestroyBlock(int par1, int par2, int par3, int par4)
    {
        int var5 = this.mc.theWorld.getBlockId(par1, par2, par3);
        int var6 = this.mc.theWorld.getBlockMetadata(par1, par2, par3);
        boolean var7 = super.onPlayerDestroyBlock(par1, par2, par3, par4);
        ItemStack var8 = this.mc.thePlayer.getCurrentEquippedItem();
        boolean var9 = this.mc.thePlayer.canHarvestBlock(Block.blocksList[var5]);

        if (var8 != null)
        {
            var8.onBlockDestroyed(this.mc.theWorld, var5, par1, par2, par3, this.mc.thePlayer);

            if (var8.stackSize == 0)
            {
//                 itemstack.onItemDestroyedByUse(mc.thePlayer);
                this.mc.thePlayer.destroyCurrentEquippedItem();
            }
        }

        if (var7 && var9)
        {
            Block.blocksList[var5].harvestBlock(this.mc.theWorld, this.mc.thePlayer, par1, par2, par3, var6);
        }

        return var7;
    }

    /**
     * Called by Minecraft class when the player is hitting a block with an item. Args: x, y, z, side
     */
    @Override
    public void clickBlock(int par1, int par2, int par3, int par4)
    {
        if (this.mc.thePlayer.isCurrentToolAdventureModeExempt(par1, par2, par3))
        {
            this.mc.theWorld.extinguishFire(this.mc.thePlayer, par1, par2, par3, par4);
            int var5 = this.mc.theWorld.getBlockId(par1, par2, par3);

            if (var5 > 0 && this.curBlockDamage == 0.0F)
            {
                Block.blocksList[var5].onBlockClicked(this.mc.theWorld, par1, par2, par3, this.mc.thePlayer);
            }

            if (var5 > 0 && Block.blocksList[var5].getPlayerRelativeBlockHardness(this.mc.thePlayer, this.mc.thePlayer.worldObj, par1, par2, par3) >= 1.0F)
            {
                this.onPlayerDestroyBlock(par1, par2, par3, par4);
            }
        }
    }

    /**
     * Resets current block damage and isHittingBlock
     */
    @Override
    public void resetBlockRemoving()
    {
        this.curBlockDamage = 0.0F;
        this.blockHitWait = 0;
        this.mc.theWorld.destroyBlockInWorldPartially(this.mc.thePlayer.entityId, this.curBlockX, this.curBlockY, this.curBlockZ, -1);
    }

    /**
     * Called when a player damages a block and updates damage counters
     */
    @Override
    public void onPlayerDamageBlock(int par1, int par2, int par3, int par4)
    {
        if (this.blockHitWait > 0)
        {
            --this.blockHitWait;
        }
        else
        {
            if (par1 == this.curBlockX && par2 == this.curBlockY && par3 == this.curBlockZ)
            {
                int var5 = this.mc.theWorld.getBlockId(par1, par2, par3);

                if (!this.mc.thePlayer.isCurrentToolAdventureModeExempt(par1, par2, par3))
                {
                    return;
                }

                if (var5 == 0)
                {
                    return;
                }

                Block var6 = Block.blocksList[var5];
                this.curBlockDamage += var6.getPlayerRelativeBlockHardness(this.mc.thePlayer, this.mc.thePlayer.worldObj, par1, par2, par3);

                if (this.blockDestroySoundCounter % 4.0F == 0.0F && var6 != null)
                {
                    this.mc.sndManager.playSound(var6.stepSound.getStepSound(), (float)par1 + 0.5F, (float)par2 + 0.5F, (float)par3 + 0.5F, (var6.stepSound.getVolume() + 1.0F) / 8.0F, var6.stepSound.getPitch() * 0.5F);
                }

                ++this.blockDestroySoundCounter;

                if (this.curBlockDamage >= 1.0F)
                {
                    this.onPlayerDestroyBlock(par1, par2, par3, par4);
                    this.curBlockDamage = 0.0F;
                    this.prevBlockDamage = 0.0F;
                    this.blockDestroySoundCounter = 0.0F;
                    this.blockHitWait = 5;
                }
                this.mc.theWorld.destroyBlockInWorldPartially(this.mc.thePlayer.entityId, this.curBlockX, this.curBlockY, this.curBlockZ, (int)(this.curBlockDamage * 10F) - 1);
            }
            else
            {
                this.curBlockDamage = 0.0F;
                this.prevBlockDamage = 0.0F;
                this.blockDestroySoundCounter = 0.0F;
                this.curBlockX = par1;
                this.curBlockY = par2;
                this.curBlockZ = par3;
            }
        }
    }

    @Override
    public void updateController()
    {
        this.prevBlockDamage = this.curBlockDamage;
        this.mc.sndManager.playRandomMusicIfReady();
    }

    /**
     * Handles a players right click
     */
    @Override
    public boolean onPlayerRightClick(EntityPlayer par1EntityPlayer, World par2World, ItemStack par3ItemStack, int par4, int par5, int par6, int par7, Vec3 par8Vec3)
    {
        int var8 = par2World.getBlockId(par4, par5, par6);

        float f = (float)par8Vec3.xCoord - (float)par4;
        float f1 = (float)par8Vec3.yCoord - (float)par5;
        float f2 = (float)par8Vec3.zCoord - (float)par6;

        if (!par1EntityPlayer.isSneaking() || par1EntityPlayer.getHeldItem() == null)
        {
        	if (var8 > 0 && Block.blocksList[var8].onBlockActivated(par2World, par4, par5, par6, par1EntityPlayer, par7, f, f1, f2))
            {
                return true;
            }
        }

        if (par3ItemStack == null)
        {
            return false;
        }
        else
        {
            return par3ItemStack.tryPlaceItemIntoWorld(par1EntityPlayer, par2World, par4, par5, par6, par7, f, f1, f2);
        }
    }
}
