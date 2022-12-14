package net.minecraft.src;

import java.util.ArrayList;
import java.util.List;

public class PlayerControllerCreative extends PlayerController
{
    private static List<Block> creativeBlocksList;
    private int field_35647_c;

    public PlayerControllerCreative(Minecraft par1Minecraft)
    {
        super(par1Minecraft);
        this.isInTestMode = true;
    }

    /**
     * Enables creative abilities to the player
     */
    public static void enableAbilities(EntityPlayer par0EntityPlayer)
    {
        par0EntityPlayer.capabilities.allowFlying = true;
        par0EntityPlayer.capabilities.isCreativeMode = true;
        par0EntityPlayer.capabilities.disableDamage = true;
    }

    /**
     * Disables creative abilities to the player.
     */
    public static void disableAbilities(EntityPlayer par0EntityPlayer)
    {
        par0EntityPlayer.capabilities.allowFlying = false;
        par0EntityPlayer.capabilities.isFlying = false;
        par0EntityPlayer.capabilities.isCreativeMode = false;
        par0EntityPlayer.capabilities.disableDamage = false;
    }

    @Override
    public void setPlayerCapabilities(EntityPlayer par1EntityPlayer)
    {
        this.mc.setGameMode(EnumGameType.CREATIVE);

        for (int var2 = 0; var2 < 9; ++var2)
        {
            if (par1EntityPlayer.inventory.mainInventory[var2] == null)
            {
                par1EntityPlayer.inventory.mainInventory[var2] = new ItemStack(creativeBlocksList.get(var2));
            }
        }
    }

    /**
     * Called from a PlayerController when the player is hitting a block with an item in Creative mode. Args: Minecraft
     * instance, player controller, x, y, z, side
     */
    public static void clickBlockCreative(Minecraft par0Minecraft, PlayerController par1PlayerController, int par2, int par3, int par4, int par5)
    {
        if (par0Minecraft.thePlayer.getHeldItem() != null && (par0Minecraft.thePlayer.getHeldItem().getItem() instanceof ItemSword))
        {
        	return;
        }
        if (!par0Minecraft.theWorld.extinguishFire(par0Minecraft.thePlayer, par2, par3, par4, par5))
        {
            par1PlayerController.onPlayerDestroyBlock(par2, par3, par4, par5);
        }
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
            int var9 = par3ItemStack.getItemDamage();
            int var10 = par3ItemStack.stackSize;
            boolean var11 = par3ItemStack.tryPlaceItemIntoWorld(par1EntityPlayer, par2World, par4, par5, par6, par7, f, f1, f2);
            par3ItemStack.setItemDamage(var9);
            par3ItemStack.stackSize = var10;
            return var11;
        }
    }

    /**
     * Called by Minecraft class when the player is hitting a block with an item. Args: x, y, z, side
     */
    @Override
    public void clickBlock(int par1, int par2, int par3, int par4)
    {
        clickBlockCreative(this.mc, this, par1, par2, par3, par4);
        this.field_35647_c = 5;
    }

    /**
     * Called when a player damages a block and updates damage counters
     */
    @Override
    public void onPlayerDamageBlock(int par1, int par2, int par3, int par4)
    {
        --this.field_35647_c;

        if (this.field_35647_c <= 0)
        {
            this.field_35647_c = 5;
            clickBlockCreative(this.mc, this, par1, par2, par3, par4);
        }
    }

    /**
     * Resets current block damage and isHittingBlock
     */
    @Override
    public void resetBlockRemoving() {}

    @Override
    public boolean shouldDrawHUD()
    {
        return false;
    }

    /**
     * player reach distance = 4F
     */
    @Override
    public float getBlockReachDistance()
    {
        return 5.0F;
    }

    /**
     * Checks if the player is not creative, used for checking if it should break a block instantly
     */
    @Override
    public boolean isNotCreative()
    {
        return false;
    }

    /**
     * returns true if player is in creative mode
     */
    @Override
    public boolean isInCreativeMode()
    {
        return true;
    }

    /**
     * true for hitting entities far away.
     */
    @Override
    public boolean extendedReach()
    {
        return true;
    }

    @Override
    public boolean func_78763_f()
    {
        return false;
    }

    static
    {
        creativeBlocksList = new ArrayList<Block>();
        creativeBlocksList.add(Block.stone);
        creativeBlocksList.add(Block.cobblestone);
        creativeBlocksList.add(Block.brick);
        creativeBlocksList.add(Block.dirt);
        creativeBlocksList.add(Block.planks);
        creativeBlocksList.add(Block.wood);
        creativeBlocksList.add(Block.leaves);
        creativeBlocksList.add(Block.torchWood);
        creativeBlocksList.add(Block.stoneSingleSlab);
        creativeBlocksList.add(Block.glass);
        creativeBlocksList.add(Block.cobblestoneMossy);
        creativeBlocksList.add(Block.sapling);
        creativeBlocksList.add(Block.plantYellow);
        creativeBlocksList.add(Block.plantRed);
        creativeBlocksList.add(Block.mushroomBrown);
        creativeBlocksList.add(Block.mushroomRed);
        creativeBlocksList.add(Block.sand);
        creativeBlocksList.add(Block.gravel);
        creativeBlocksList.add(Block.sponge);
        creativeBlocksList.add(Block.cloth);
        creativeBlocksList.add(Block.oreCoal);
        creativeBlocksList.add(Block.oreIron);
        creativeBlocksList.add(Block.oreGold);
        creativeBlocksList.add(Block.blockIron);
        creativeBlocksList.add(Block.blockGold);
        creativeBlocksList.add(Block.bookShelf);
        creativeBlocksList.add(Block.tnt);
        creativeBlocksList.add(Block.obsidian);
    }
}
