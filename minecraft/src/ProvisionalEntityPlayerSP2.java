package net.minecraft.src;

import java.util.Random;

public class ProvisionalEntityPlayerSP2 extends EntityClientPlayerMP
{
    public static int armor = 3;
    public static int combat = 3;
    public static boolean sprint = true;
    public static int startitems = 0;
    public static boolean alertWolves = false;

    private MouseFilter field_71162_ch;
    private MouseFilter field_71160_ci;
    private MouseFilter field_71161_cj;

    public ProvisionalEntityPlayerSP2(Minecraft par1Minecraft, World par2World, Session par3Session, int par4)
    {
        super(par1Minecraft, par2World, par3Session, new ProvisionalNetClientHandlerSP(par1Minecraft));
        this.sprintToggleTimer = 0;
        this.sprintingTicksLeft = 0;
        this.field_71162_ch = new MouseFilter();
        this.field_71160_ci = new MouseFilter();
        this.field_71161_cj = new MouseFilter();
        this.mc = par1Minecraft;
        this.dimension = par4;
    }

    @Override
    public void incrementStat(StatBase par1StatBase, int par2) {}

    /**
     * Sends a chat message from the player. Args: chatMessage
     */
    public void sendChatMessage(String par1Str)
    {
        if (par1Str.startsWith("/"))
        {
            this.mc.getCommandManager().executeCommand(this, par1Str.substring(1));
        }
        else
        {
            this.mc.ingameGUI.getChatGUI().printChatMessage("<" + this.username + "> " + par1Str);
        }
    }

    @Override
    public void respawnPlayer()
    {
    	this.mc.respawn(false, 0, false);
    }

    @Override
    public void travelToDimension(int par1)
    {
        if (this.worldObj.isRemote)
        {
            return;
        }

        if (this.dimension == 1 && par1 == 1)
        {
        	this.triggerAchievement(AchievementList.theEnd2);
            this.mc.displayGuiScreen(new GuiWinGame());
        }
        else
        {
        	this.triggerAchievement(AchievementList.theEnd);
            this.mc.sndManager.playSoundFX("portal.travel", 1.0F, rand.nextFloat() * 0.4F + 0.8F);
            this.mc.usePortal(1);
        }
    }

    /**
     * Tries to moves the entity by the passed in displacement. Args: x, y, z
     */
    @Override
    public void moveEntity(double par1, double par3, double par5)
    {
        super.moveEntity(par1, par3, par5);
    }

    @Override
    public void updateEntityActionState()
    {
    	super.updateEntityActionState();
        this.moveStrafing = movementInput.moveStrafe;
        this.moveForward = movementInput.moveForward;
        this.isJumping = movementInput.jump;
        this.prevRenderArmYaw = renderArmYaw;
        this.prevRenderArmPitch = renderArmPitch;
        this.renderArmPitch += (double)(rotationPitch - renderArmPitch) * 0.5D;
        this.renderArmYaw += (double)(rotationYaw - renderArmYaw) * 0.5D;
    }

    /**
     * Returns whether the entity is in a local (client) world
     */
    @Override
    public boolean isClientWorld()
    {
        return true;
    }

    /**
     * Gets the player's field of view multiplier. (ex. when flying)
     */
    @Override
    public float getFOVMultiplier()
    {
        float f = 1.0F;

        if (this.capabilities.isFlying)
        {
            f *= 1.1F;
        }

        AttributeInstance attributeinstance = getEntityAttribute(SharedMonsterAttributes.movementSpeed);
        f = (float)((double)f * ((attributeinstance.getAttributeValue() / (double)capabilities.getWalkSpeed() + 1.0D) / 2D));

        if (isUsingItem() && getItemInUse().itemID == Item.bow.itemID)
        {
            int i = getItemInUseDuration();
            float f1 = (float)i / 20F;

            if (f1 > 1.0F)
            {
                f1 = 1.0F;
            }
            else
            {
                f1 *= f1;
            }

            f *= 1.0F - f1 * 0.15F;
        }

        return f;
    }

    /**
     * sets current screen to null (used on escape buttons of GUIs)
     */
    @Override
    public void closeScreen()
    {
    	this.openContainer = inventoryContainer;
        this.mc.displayGuiScreen(null);
    }

    /**
     * Displays the GUI for editing a sign. Args: tileEntitySign
     */
    @Override
    public void displayGUIEditSign(TileEntity par1TileEntity)
    {
        if (par1TileEntity instanceof TileEntitySign)
        {
        	this.mc.displayGuiScreen(new GuiEditSign((TileEntitySign)par1TileEntity));
        }
        else if (par1TileEntity instanceof TileEntityCommandBlock)
        {
        	this.mc.displayGuiScreen(new GuiCommandBlock((TileEntityCommandBlock)par1TileEntity));
        }
    }

    /**
     * Displays the GUI for interacting with a book.
     */
    @Override
    public void displayGUIBook(ItemStack par1ItemStack)
    {
        Item item = par1ItemStack.getItem();

        if (item == Item.writtenBook)
        {
        	this.mc.displayGuiScreen(new GuiScreenBook(this, par1ItemStack, false));
        }
        else if (item == Item.writableBook)
        {
        	this.mc.displayGuiScreen(new GuiScreenBook(this, par1ItemStack, true));
        }
    }

    /**
     * Displays the GUI for interacting with a chest inventory. Args: chestInventory
     */
    @Override
    public void displayGUIChest(IInventory par1IInventory)
    {
    	this.mc.displayGuiScreen(new GuiChest(this.inventory, par1IInventory));
    }

    /**
     * Displays the crafting GUI for a workbench.
     */
    @Override
    public void displayGUIWorkbench(int par1, int par2, int par3)
    {
    	this.mc.displayGuiScreen(new GuiCrafting(this.inventory, this.worldObj, par1, par2, par3));
    }

    @Override
    public void displayGUIEnchantment(int par1, int par2, int par3, String par4Str)
    {
    	this.mc.displayGuiScreen(new GuiEnchantment(this.inventory, this.worldObj, par1, par2, par3, par4Str));
    }

    /**
     * Displays the furnace GUI for the passed in furnace entity. Args: tileEntityFurnace
     */
    @Override
    public void displayGUIFurnace(TileEntityFurnace par1TileEntityFurnace)
    {
    	this.mc.displayGuiScreen(new GuiFurnace(this.inventory, par1TileEntityFurnace));
    }

    /**
     * Displays the GUI for interacting with a brewing stand.
     */
    @Override
    public void displayGUIBrewingStand(TileEntityBrewingStand par1TileEntityBrewingStand)
    {
    	this.mc.displayGuiScreen(new GuiBrewingStand(this.inventory, par1TileEntityBrewingStand));
    }

    /**
     * Displays the dipsenser GUI for the passed in dispenser entity. Args: TileEntityDispenser
     */
    @Override
    public void displayGUIDispenser(TileEntityDispenser par1TileEntityDispenser)
    {
    	this.mc.displayGuiScreen(new GuiDispenser(this.inventory, par1TileEntityDispenser));
    }

    @Override
    public void displayGUIMerchant(IMerchant par1IMerchant, String par2Str)
    {
    	this.mc.displayGuiScreen(new GuiMerchant(this.inventory, par1IMerchant, this.worldObj, par2Str));
    }

    @Override
    public void displayGUIHopper(TileEntityHopper par1TileEntityHopper)
    {
    	this.mc.displayGuiScreen(new GuiHopper(this.inventory, par1TileEntityHopper));
    }

    @Override
    public void displayGUIHopperMinecart(EntityMinecartHopper par1EntityMinecartHopper)
    {
    	this.mc.displayGuiScreen(new GuiHopper(this.inventory, par1EntityMinecartHopper));
    }

    @Override
    public void displayGUIHorse(EntityHorse par1EntityHorse, IInventory par2IInventory)
    {
    	this.mc.displayGuiScreen(new GuiScreenHorseInventory(this.inventory, par2IInventory, par1EntityHorse));
    }

    /**
     * Called when the player performs a critical hit on the Entity. Args: entity that was hit critically
     */
    @Override
    public void onCriticalHit(Entity par1Entity)
    {
    	this.mc.effectRenderer.addEffect(new EntityCrit2FX(this.mc.theWorld, par1Entity));
    }

    @Override
    public void onEnchantmentCritical(Entity par1Entity)
    {
        EntityCrit2FX entitycrit2fx = new EntityCrit2FX(this.mc.theWorld, par1Entity, "magicCrit");
        this.mc.effectRenderer.addEffect(entitycrit2fx);
    }

    /**
     * Called whenever an item is picked up from walking over it. Args: pickedUpEntity, stackSize
     */
    @Override
    public void onItemPickup(Entity par1Entity, int par2)
    {
    	this.mc.effectRenderer.addEffect(new EntityPickupFX(this.mc.theWorld, par1Entity, this, -0.5F));
    }

    /**
     * Returns if this entity is sneaking.
     */
    @Override
    public boolean isSneaking()
    {
        return this.movementInput.sneak && !this.sleeping;
    }

    /**
     * Updates health locally.
     */
    @Override
    public void setPlayerSPHealth(float par1)
    {
        float f = getHealth() - par1;

        if (f <= 0.0F)
        {
            setHealth(par1);

            if (f < 0.0F)
            {
            	this.hurtResistantTime = this.maxHurtResistantTime / 2;
            }
        }
        else
        {
        	this.lastDamage = f;
            this.setHealth(getHealth());
            this.hurtResistantTime = this.maxHurtResistantTime;
            this.damageEntity(DamageSource.generic, f);
            this.hurtTime = this.maxHurtTime = 10;
        }
    }

    /**
     * Add a chat message to the player
     */
    @Override
    public void addChatMessage(String par1Str)
    {
    	this.mc.ingameGUI.getChatGUI().addTranslatedMessage(par1Str, new Object[0]);
    }

    /**
     * Adds a value to a statistic field.
     */
    @Override
    public void addStat(StatBase par1StatBase, int par2)
    {
        if (par1StatBase == null)
        {
            return;
        }

        if (par1StatBase.isAchievement())
        {
            Achievement achievement = (Achievement)par1StatBase;

            if (achievement.parentAchievement == null || this.mc.statFileWriter.hasAchievementUnlocked(achievement.parentAchievement))
            {
                if (!this.mc.statFileWriter.hasAchievementUnlocked(achievement))
                {
                	this.mc.guiAchievement.queueTakenAchievement(achievement);
                }

                this.mc.statFileWriter.readStat(par1StatBase, par2);
            }
        }
        else
        {
        	this.mc.statFileWriter.readStat(par1StatBase, par2);
        }
    }

    private boolean isBlockTranslucent(int par1, int par2, int par3)
    {
        return this.worldObj.isBlockNormalCube(par1, par2, par3);
    }

    /**
     * Adds velocity to push the entity out of blocks at the specified x, y, z position Args: x, y, z
     */
    @Override
    protected boolean pushOutOfBlocks(double par1, double par3, double par5)
    {
        int i = MathHelper.floor_double(par1);
        int j = MathHelper.floor_double(par3);
        int k = MathHelper.floor_double(par5);
        double d = par1 - (double)i;
        double d1 = par5 - (double)k;

        if (this.isBlockTranslucent(i, j, k) || this.isBlockTranslucent(i, j + 1, k))
        {
            boolean flag = !this.isBlockTranslucent(i - 1, j, k) && !this.isBlockTranslucent(i - 1, j + 1, k);
            boolean flag1 = !this.isBlockTranslucent(i + 1, j, k) && !this.isBlockTranslucent(i + 1, j + 1, k);
            boolean flag2 = !this.isBlockTranslucent(i, j, k - 1) && !this.isBlockTranslucent(i, j + 1, k - 1);
            boolean flag3 = !this.isBlockTranslucent(i, j, k + 1) && !this.isBlockTranslucent(i, j + 1, k + 1);
            byte byte0 = -1;
            double d2 = 9999D;

            if (flag && d < d2)
            {
                d2 = d;
                byte0 = 0;
            }

            if (flag1 && 1.0D - d < d2)
            {
                d2 = 1.0D - d;
                byte0 = 1;
            }

            if (flag2 && d1 < d2)
            {
                d2 = d1;
                byte0 = 4;
            }

            if (flag3 && 1.0D - d1 < d2)
            {
                double d3 = 1.0D - d1;
                byte0 = 5;
            }

            float f = 0.1F;

            if (byte0 == 0)
            {
            	this.motionX = -f;
            }

            if (byte0 == 1)
            {
            	this.motionX = f;
            }

            if (byte0 == 4)
            {
            	this.motionZ = -f;
            }

            if (byte0 == 5)
            {
            	this.motionZ = f;
            }
        }

        return false;
    }

    /**
     * Set sprinting switch for Entity.
     */
    @Override
    public void setSprinting(boolean par1)
    {
        if (!this.sprint)
        {
            par1 = false;
        }
        super.setSprinting(par1);
    }

    /**
     * Sets the current XP, total XP, and level number.
     */
    @Override
    public void setXPStats(float par1, int par2, int par3)
    {
    	this.experience = par1;
        this.experienceTotal = par2;
        this.experienceLevel = par3;
    }

    @Override
    public void sendChatToPlayer(ChatMessageComponent par1ChatMessageComponent)
    {
    	this.mc.ingameGUI.getChatGUI().printChatMessage((par1ChatMessageComponent.toStringWithFormatting(true)));
    }

    /**
     * Deals damage to the entity. If its a EntityPlayer then will take damage from the armor first and then health
     * second with the reduced value. Args: damageAmount
     */
    @Override
    protected void damageEntity(DamageSource par1DamageSource, float par2)
    {
        if (!par1DamageSource.isUnblockable() && this.isBlocking())
        {
            par2 = 1 + (int)par2 >> 1;
        }

        {
            par2 = this.applyArmorCalculations(par1DamageSource, par2);
        }
        par2 = this.applyPotionDamageCalculations(par1DamageSource, par2);
        this.addExhaustion(par1DamageSource.getHungerDamage());
        if (this.armor==2)
        {
            par2 = this.applyArmorCalculations(par1DamageSource, par2);
            par2 = this.applyPotionDamageCalculations(par1DamageSource, par2);
        }
        this.setHealth(this.getHealth() - par2);
    }

    @Override
    public void sendPlayerAbilities() {}

    /**
     * Called when the entity is attacked.
     */
    @Override
    public boolean attackEntityFrom(DamageSource par1DamageSource, float par2)
    {
        if (this.capabilities.disableDamage && !par1DamageSource.canHarmInCreative())
        {
            return false;
        }

        this.entityAge = 0;

        if (this.getHealth() <= 0)
        {
            return false;
        }

        if (this.isPlayerSleeping() && !this.worldObj.isRemote)
        {
        	this.wakeUpPlayer(true, true, false);
        }

        Entity entity = par1DamageSource.getEntity();

        if (par1DamageSource.isDifficultyScaled())
        {
            if (this.worldObj.difficultySetting == 0)
            {
                par2 = 0;
            }

            if (this.worldObj.difficultySetting == 1)
            {
                par2 = par2 / 2 + 1;
            }

            if (this.worldObj.difficultySetting == 3)
            {
                par2 = (par2 * 3) / 2;
            }
        }

        if (par2 == 0)
        {
            return false;
        }

        Entity entity1 = par1DamageSource.getEntity();

        if ((entity1 instanceof EntityArrow) && ((EntityArrow)entity1).shootingEntity != null)
        {
            entity1 = ((EntityArrow)entity1).shootingEntity;
        }

//        if (alertWolves && entity1 instanceof EntityLiving)
//        {
//            alertWolves((EntityLiving)entity1, false);
//        } Not sure what this did, but my recollection of why I commented this out is that it was creating errors. - Schf

        this.addStat(StatList.damageTakenStat, Math.round(par2 * 10F));
        if (this.worldObj.isRemote)
        {
            return false;
        }

        this.entityAge = 0;

        if (this.getHealth() <= 0)
        {
            return false;
        }

        if (par1DamageSource.isFireDamage() && this.isPotionActive(Potion.fireResistance))
        {
            return false;
        }

        this.limbSwingAmount = 1.5F;
        boolean flag = true;

        if ((float)this.hurtResistantTime > (float)this.maxHurtResistantTime / 2.0F)
        {
            if (par2 <= this.lastDamage)
            {
                return false;
            }

            this.damageEntity(par1DamageSource, par2 - this.lastDamage);
            this.lastDamage = par2;
            flag = false;
        }
        else
        {
        	this.lastDamage = par2;
            this.prevHealth = this.getHealth();
            this.hurtResistantTime = this.maxHurtResistantTime;
            this.damageEntity(par1DamageSource, par2);
            this.hurtTime = this.maxHurtTime = 10;
        }

        this.attackedAtYaw = 0.0F;

        if (entity != null)
        {
            if (entity instanceof EntityLiving)
            {
            	this.setRevengeTarget((EntityLiving)entity);
            } 
            if (entity instanceof EntityPlayer)
            {
            	this.recentlyHit = 60;
                this.attackingPlayer = (EntityPlayer)entity;
            }
            else if (entity instanceof EntityWolf)
            {
                EntityWolf entitywolf = (EntityWolf)entity;

                if (entitywolf.isTamed())
                {
                	this.recentlyHit = 60;
                    this.attackingPlayer = null;
                }
            } 
        }

        if (flag)
        {
        	this.worldObj.setEntityState(this, (byte)2);

            if (par1DamageSource != DamageSource.drown && !par1DamageSource.getDamageType().equals("explosionOld"))
            {
            	this.setBeenAttacked();
            }

            if (entity != null)
            {
                double d = entity.posX - posX;
                double d1;

                for (d1 = entity.posZ - posZ; d * d + d1 * d1 < 0.0001D; d1 = (Math.random() - Math.random()) * 0.01D)
                {
                    d = (Math.random() - Math.random()) * 0.01D;
                }

                this.attackedAtYaw = (float)((Math.atan2(d1, d) * 180D) / Math.PI) - this.rotationYaw;
                this.knockBack(entity, par2, d, d1);
            }
            else
            {
            	this.attackedAtYaw = (int)(Math.random() * 2D) * 180;
            }
        }

        if (this.getHealth() <= 0)
        {
            if (flag)
            {
            	this.playSound(this.getDeathSound(), this.getSoundVolume(), this.getSoundPitch());
            }

            this.onDeath(par1DamageSource);
        }
        else if (flag)
        {
        	this.playSound(this.getHurtSound(), this.getSoundVolume(), this.getSoundPitch());
        }

        return true;
    }

    @Override
    public void heal(float par1)
    {
        float f = this.getHealth();

        if (f > 0.0F)
        {
        	this.setHealth(f + par1);
        }
    }

    /**
     * Called to update the entity's position/logic.
     */
    @Override
    public void onUpdate()
    {
        super.onUpdate();
    }

    @Override
    public void sendMotionUpdates() {}

    /**
     * Called when player presses the drop item key
     */
    @Override
    public EntityItem dropOneItem(boolean par1)
    {
        return this.dropPlayerItemWithRandomChoice(this.inventory.decrStackSize(this.inventory.currentItem, !par1 || this.inventory.getCurrentItem() == null ? 1 : this.inventory.getCurrentItem().stackSize), false);
    }

    /**
     * Joins the passed in entity item with the world. Args: entityItem
     */
    @Override
    protected void joinEntityItemWithWorld(EntityItem entityitem)
    {
    	this.worldObj.spawnEntityInWorld(entityitem);
    }

    /**
     * Gets the pitch of living sounds in living entities.
     */
    @Override
    protected float getSoundPitch()
    {
        if (this.isChild())
        {
            return (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.5F;
        }
        else
        {
            return (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F;
        }
    }

    @Override
    public void onLivingUpdate()
    {
        super.onLivingUpdate();
        if (this.inPortal)
        {
            if (this.timeInPortal >= 1.0F)
            {
                if (!this.worldObj.isRemote && this.mc.enableSP)
                {
                	this.timeUntilPortal = 10;
                	this.mc.sndManager.playSoundFX("portal.travel", 1.0F, this.rand.nextFloat() * 0.4F + 0.8F);
                    byte byte0 = 0;

                    if (this.dimension == -1)
                    {
                        byte0 = 0;
                    }
                    else
                    {
                        byte0 = -1;
                    }

                    this.mc.usePortal(byte0);
                    this.triggerAchievement(AchievementList.portal);
                }
            }
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.readFromNBT(par1NBTTagCompound);
        this.prevPosY += 1.6200000047683716D;
        this.lastTickPosY += 1.6200000047683716D;
        this.posY += 1.6200000047683716D;
        this.setPosition(this.posX, this.posY, this.posZ);
    }

    @Override
    public void writeToNBT(NBTTagCompound par1NBTTagCompound)
    {
    	this.posY -= 1.6200000047683716D;
        super.writeToNBT(par1NBTTagCompound);
        this.posY += 1.6200000047683716D;
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound nbttagcompound)
    {
        super.readEntityFromNBT(nbttagcompound);
        if (nbttagcompound.hasKey("Riding"))
        {
            Entity entity = EntityList.createEntityFromNBT(nbttagcompound.getCompoundTag("Riding"), worldObj);
            if (entity != null)
            {
                entity.forceSpawn = true;
                this.worldObj.spawnEntityInWorld(entity);
                this.mountEntity(entity);
                entity.forceSpawn = false;
            }
        }
    }

    @Override
    public boolean isRidingHorse()
    {
        return this.ridingEntity != null && (this.ridingEntity instanceof EntityHorse);
    }

    @Override
    protected void func_110318_g()
    {
        if (isRidingHorse())
        {
            ((EntityHorse)this.ridingEntity).setJumpPower((int)(this.getHorseJumpPower() * 100F));
        }
    }

    @Override
    public void func_110322_i()
    {
        if (isRidingHorse())
        {
            ((EntityHorse)this.ridingEntity).openGUI(this);
        }
    }

    @Override
    public void mountEntity(Entity par1Entity)
    {
        super.mountEntity(par1Entity);
        if (this.ridingEntity != null)
        {
            GameSettings gamesettings = this.mc.gameSettings;
            this.mc.ingameGUI.func_110326_a(I18n.getStringParams("mount.onboard", new Object[]
            {
                GameSettings.getKeyDisplayString(gamesettings.keyBindSneak.keyCode)
            }), false);
        }
    }
}
