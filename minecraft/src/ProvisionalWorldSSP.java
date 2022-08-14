package net.minecraft.src;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

public class ProvisionalWorldSSP extends WorldClient implements IBlockAccess
{
    /**
     * TreeSet of scheduled ticks which is used as a priority queue for the ticks
     */
	protected TreeSet scheduledTickTreeSet;

    /** Set of scheduled ticks (used for checking if a tick already exists) */
	protected Set scheduledTickSet;

    /** Entities marked for removal. */
	protected List entityRemoval;
    protected long cloudColour;

    /**
     * Contains a timestamp from when the World object was created. Is used in the session.lock file
     */
    public long lockTimestamp;
    protected int autosavePeriod;

    /**
     * Used to differentiate between a newly generated world and an already existing world.
     */
    public boolean isNewWorld;

    /**
     * A flag indicating whether or not all players in the world are sleeping.
     */
    protected boolean allPlayersSleeping;
    protected ArrayList collidingBoundingBoxes;
    protected boolean scanningTileEntities;

    /** number of ticks until the next random ambients play */
    protected int ambientTickCountdown;

    /**
     * entities within AxisAlignedBB excluding one, set and returned in getEntitiesWithinAABBExcludingEntity(Entity
     * var1, AxisAlignedBB var2)
     */
    protected List entitiesWithinAABBExcludingEntity;
    protected static final WeightedRandomChestContent bonusChestContent[];

    public double field_35467_J;
    public double field_35468_K;
    public double field_35465_L;

    /** true while the world is editing blocks */
    public boolean editingBlocks;

    protected final SpawnerAnimals animalSpawner = new SpawnerAnimals();

    public ProvisionalWorldSSP(ISaveHandler par1ISaveHandler, String par2Str, WorldProvider par3WorldProvider, WorldSettings par4WorldSettings, Profiler par5Profiler, ILogAgent par5ILogAgent)
    {
        super(par3WorldProvider, par1ISaveHandler, par4WorldSettings, par2Str, par5Profiler, par5ILogAgent);
        this.editingBlocks = false;
        this.scheduledTickTreeSet = new TreeSet();
        this.scheduledTickSet = new HashSet();
        this.entityRemoval = new ArrayList();
        this.cloudColour = 16777215L;
        this.lockTimestamp = System.currentTimeMillis();
        this.autosavePeriod = 40;
        this.isNewWorld = false;
        this.collidingBoundingBoxes = new ArrayList();
        this.ambientTickCountdown = this.rand.nextInt(12000);
        this.entitiesWithinAABBExcludingEntity = new ArrayList();
        this.worldInfo = new WorldInfo(par4WorldSettings, par2Str);
        par3WorldProvider.registerWorld(this);
        this.calculateInitialSkylight();
        this.calculateInitialWeather();
    }

    public ProvisionalWorldSSP(ProvisionalWorldSSP par1World, WorldProvider par2WorldProvider, Profiler par3Profiler, ILogAgent par4ILogAgent)
    {
    	super(par2WorldProvider, par1World.saveHandler, new WorldSettings(par1World.getWorldInfo()), par1World.getWorldInfo().getWorldName(), par3Profiler, par4ILogAgent);
        this.editingBlocks = false;
        this.scheduledTickTreeSet = new TreeSet();
        this.scheduledTickSet = new HashSet();
        this.entityRemoval = new ArrayList();
        this.cloudColour = 16777215L;
        this.lockTimestamp = System.currentTimeMillis();
        this.autosavePeriod = 40;
        this.isNewWorld = false;
        this.collidingBoundingBoxes = new ArrayList();
        this.ambientTickCountdown = this.rand.nextInt(12000);
        this.entitiesWithinAABBExcludingEntity = new ArrayList();
        this.lockTimestamp = par1World.lockTimestamp;
        this.worldInfo = new WorldInfo(par1World.worldInfo);
        par2WorldProvider.registerWorld(this);
        this.calculateInitialSkylight();
        this.calculateInitialWeather();
    }

    public ProvisionalWorldSSP(ISaveHandler par1ISaveHandler, String par2Str, WorldSettings par3WorldSettings, Profiler par4Profiler, ILogAgent par5ILogAgent)
    {
        this(par1ISaveHandler, par2Str, par3WorldSettings, (WorldProvider)null, par4Profiler, par5ILogAgent);
    }

    public ProvisionalWorldSSP(ISaveHandler par1ISaveHandler, String par2Str, WorldSettings par3WorldSettings, WorldProvider par4WorldProvider, Profiler par5Profiler, ILogAgent par6ILogAgent)
    {
    	super(par4WorldProvider, par1ISaveHandler, par3WorldSettings, par2Str, par5Profiler, par6ILogAgent);
        this.scheduledTickTreeSet = new TreeSet();
        this.scheduledTickSet = new HashSet();
        this.entityRemoval = new ArrayList();
        this.cloudColour = 16777215L;
        this.lockTimestamp = System.currentTimeMillis();
        this.autosavePeriod = 40;
        this.isNewWorld = false;
        this.collidingBoundingBoxes = new ArrayList();
        this.ambientTickCountdown = this.rand.nextInt(12000);
        this.entitiesWithinAABBExcludingEntity = new ArrayList();
        this.isNewWorld = par1ISaveHandler.loadWorldInfo() == null;

        this.provider.registerWorld(this);

        if (this.isNewWorld)
        {
            if (getClass() == ProvisionalWorldSSP.class)
            {
            	this.generateSpawnPoint(par3WorldSettings);
            }
        }

        this.calculateInitialSkylight();
        this.calculateInitialWeather();
    }

    /**
     * Creates the chunk provider for this world. Called in the constructor. Retrieves provider from worldProvider?
     */
    @Override
    protected IChunkProvider createChunkProvider()
    {
        IChunkLoader var1 = this.saveHandler.getChunkLoader(this.provider);
        return new ChunkProvider(this, var1, this.provider.createChunkGenerator());
    }

    /**
     * Finds an initial spawn location upon creating a new world
     */
    protected void generateSpawnPoint(WorldSettings par1WorldSettings)
    {
        if (!this.provider.canRespawnHere())
        {
            this.worldInfo.setSpawnPosition(0, this.provider.getAverageGroundLevel(), 0);
        }
        else
        {
            this.findingSpawnPoint = true;
            WorldChunkManager var2 = this.provider.worldChunkMgr;
            List var3 = var2.getBiomesToSpawnIn();
            Random var4 = new Random(this.getSeed());
            ChunkPosition var5 = var2.findBiomePosition(0, 0, 256, var3, var4);
            int var6 = 0;
            int var7 = this.provider.getAverageGroundLevel();
            int var8 = 0;

            if (var5 != null)
            {
                var6 = var5.x;
                var8 = var5.z;
            }
            else
            {
                System.out.println("Unable to find spawn biome");
            }

            int var9 = 0;

            while (!this.provider.canCoordinateBeSpawn(var6, var8))
            {
                var6 += var4.nextInt(64) - var4.nextInt(64);
                var8 += var4.nextInt(64) - var4.nextInt(64);
                ++var9;

                if (var9 == 1000)
                {
                    break;
                }
            }

            this.worldInfo.setSpawnPosition(var6, var7, var8);
            this.findingSpawnPoint = false;

            if (par1WorldSettings.isBonusChestEnabled())
            {
                this.createBonusChest();
            }
        }
    }

    /**
     * Creates the bonus chest in the world.
     */
    protected void createBonusChest()
    {
        WorldGeneratorBonusChest var1 = new WorldGeneratorBonusChest(this.bonusChestContent, 10);

        for (int var2 = 0; var2 < 10; ++var2)
        {
            int var3 = this.worldInfo.getSpawnX() + this.rand.nextInt(6) - this.rand.nextInt(6);
            int var4 = this.worldInfo.getSpawnZ() + this.rand.nextInt(6) - this.rand.nextInt(6);
            int var5 = this.getTopSolidOrLiquidBlock(var3, var4) + 1;

            if (var1.generate(this, this.rand, var3, var5, var4))
            {
                break;
            }
        }
    }

    /**
     * Gets the hard-coded portal location to use when entering this dimension
     */
    public ChunkCoordinates getEntrancePortalLocation()
    {
        return this.provider.getEntrancePortalLocation();
    }

    /**
     * Sets a new spawn location by finding an uncovered block at a random (x,z) location in the chunk.
     */
    @Override
    public void setSpawnLocation()
    {
        if (this.worldInfo.getSpawnY() <= 0)
        {
            this.worldInfo.setSpawnY(64);
        }

        int var1 = this.worldInfo.getSpawnX();
        int var2 = this.worldInfo.getSpawnZ();
        int var3 = 0;

        while (this.getFirstUncoveredBlock(var1, var2) == 0)
        {
            var1 += this.rand.nextInt(8) - this.rand.nextInt(8);
            var2 += this.rand.nextInt(8) - this.rand.nextInt(8);
            ++var3;

            if (var3 == 10000)
            {
                break;
            }
        }

        this.worldInfo.setSpawnX(var1);
        this.worldInfo.setSpawnZ(var2);
    }

    /**
     * spawns a player, load data from level.dat if needed and loads surrounding chunks
     */
    public void spawnPlayerWithLoadedChunks(EntityPlayer par1EntityPlayer)
    {
        try
        {
            NBTTagCompound var2 = this.worldInfo.getPlayerNBTTagCompound();

            if (var2 != null)
            {
                par1EntityPlayer.readFromNBT(var2);
                this.worldInfo.setPlayerNBTTagCompound((NBTTagCompound)null);
            }

            if (this.chunkProvider instanceof ChunkProviderLoadOrGenerate)
            {
                ChunkProviderLoadOrGenerate var3 = (ChunkProviderLoadOrGenerate)this.chunkProvider;
                int var4 = MathHelper.floor_float((float)((int)par1EntityPlayer.posX)) >> 4;
                int var5 = MathHelper.floor_float((float)((int)par1EntityPlayer.posZ)) >> 4;
                var3.setCurrentChunkOver(var4, var5);
            }

            this.spawnEntityInWorld(par1EntityPlayer);
        }
        catch (Exception var6)
        {
            var6.printStackTrace();
        }
    }

    /**
     * Saves the data for this World. If passed true, then only save up to 2 chunks, otherwise, save all chunks.
     */
    public void saveWorld(boolean par1, IProgressUpdate par2IProgressUpdate)
    {
        if (this.chunkProvider.canSave())
        {
            if (par2IProgressUpdate != null)
            {
                par2IProgressUpdate.displayProgressMessage("Saving level");
            }

            this.saveLevel();

            if (par2IProgressUpdate != null)
            {
                par2IProgressUpdate.resetProgresAndWorkingMessage("Saving chunks");
            }

            this.chunkProvider.saveChunks(par1, par2IProgressUpdate);
        }
    }

    /**
     * Saves the global data associated with this World
     */
    protected void saveLevel()
    {
        try
        {
            checkSessionLock();
        }
        catch(MinecraftException ex)
        {
            ex.printStackTrace();
        }
        this.saveHandler.saveWorldInfoAndPlayer(this.worldInfo, this.playerEntities);
        this.worldInfo.setSaveVersion(19133);
        this.mapStorage.saveAllData();
    }

    /**
     * Saves the world and all chunk data without displaying any progress message. If passed 0, then save player info
     * and metadata as well.
     */
    public boolean quickSaveWorld(int par1)
    {
        if (!this.chunkProvider.canSave())
        {
            return true;
        }
        else
        {
            if (par1 == 0)
            {
                this.saveLevel();
            }

            return this.chunkProvider.saveChunks(false, (IProgressUpdate)null);
        }
    }

    public void commandSetTime(long par1, boolean par2)
    {
        long var1 = par1 - this.worldInfo.getWorldTotalTime();

        for (Iterator iterator = this.scheduledTickSet.iterator(); iterator.hasNext();)
        {
            NextTickListEntry nextticklistentry = (NextTickListEntry)iterator.next();
            nextticklistentry.scheduledTime += var1;
        }

        if (par2)
        {
        	this.field_35467_J = 0D;
            this.field_35468_K = 0D;
        }

        this.setWorldTime(par1);
        this.func_82738_a(par1);
    }

    /**
     * Returns true if the block at the specified coordinates is empty
     */
    @Override
    public boolean isAirBlock(int par1, int par2, int par3)
    {
        return this.getBlockId(par1, par2, par3) == 0;
    }

    /**
     * Returns whether a block exists at world coordinates x, y, z
     */
    @Override
    public boolean blockExists(int par1, int par2, int par3)
    {
        return par2 >= 0 && par2 < 256 ? this.chunkExists(par1 >> 4, par3 >> 4) : false;
    }

    /**
     * Checks if any of the chunks within distance (argument 4) blocks of the given block exist
     */
    @Override
    public boolean doChunksNearChunkExist(int par1, int par2, int par3, int par4)
    {
        return this.checkChunksExist(par1 - par4, par2 - par4, par3 - par4, par1 + par4, par2 + par4, par3 + par4);
    }

    /**
     * Checks between a min and max all the chunks inbetween actually exist. Args: minX, minY, minZ, maxX, maxY, maxZ
     */
    @Override
    public boolean checkChunksExist(int par1, int par2, int par3, int par4, int par5, int par6)
    {
        if (par5 >= 0 && par2 < 256)
        {
            par1 >>= 4;
            par3 >>= 4;
            par4 >>= 4;
            par6 >>= 4;

            for (int var7 = par1; var7 <= par4; ++var7)
            {
                for (int var8 = par3; var8 <= par6; ++var8)
                {
                    if (!this.chunkExists(var7, var8))
                    {
                        return false;
                    }
                }
            }

            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Returns whether a chunk exists at chunk coordinates x, y
     */
    @Override
    protected boolean chunkExists(int par1, int par2)
    {
        return this.chunkProvider.chunkExists(par1, par2);
    }

    /**
     * Returns a chunk looked up by block coordinates. Args: x, z
     */
    @Override
    public Chunk getChunkFromBlockCoords(int par1, int par2)
    {
        return this.getChunkFromChunkCoords(par1 >> 4, par2 >> 4);
    }

    /**
     * Returns back a chunk looked up by chunk coordinates Args: x, y
     */
    @Override
    public Chunk getChunkFromChunkCoords(int par1, int par2)
    {
        return this.chunkProvider.provideChunk(par1, par2);
    }

    /**
     * Returns the block's material.
     */
    @Override
    public Material getBlockMaterial(int par1, int par2, int par3)
    {
        int var4 = this.getBlockId(par1, par2, par3);
        return var4 == 0 ? Material.air : Block.blocksList[var4].blockMaterial;
    }

    /**
     * Marks the block as needing an update with the renderer. Args: x, y, z
     */
    @Override
    public void markBlockForUpdate(int par1, int par2, int par3)
    {
        for (int var1 = 0; var1< this.worldAccesses.size(); var1++)
        {
            ((IWorldAccess)this.worldAccesses.get(var1)).markBlockForUpdate(par1, par2, par3);
        }
    }

    /**
     * The block type change and need to notify other systems  Args: x, y, z, blockID
     */
    @Override
    public void notifyBlockChange(int par1, int par2, int par3, int par4)
    {
        this.markBlockForUpdate(par1, par2, par3);
        this.notifyBlocksOfNeighborChange(par1, par2, par3, par4);
    }

    /**
     * Notifies neighboring blocks that this specified block changed  Args: x, y, z, blockID
     */
    @Override
    public void notifyBlocksOfNeighborChange(int par1, int par2, int par3, int par4)
    {
        this.notifyBlockOfNeighborChange(par1 - 1, par2, par3, par4);
        this.notifyBlockOfNeighborChange(par1 + 1, par2, par3, par4);
        this.notifyBlockOfNeighborChange(par1, par2 - 1, par3, par4);
        this.notifyBlockOfNeighborChange(par1, par2 + 1, par3, par4);
        this.notifyBlockOfNeighborChange(par1, par2, par3 - 1, par4);
        this.notifyBlockOfNeighborChange(par1, par2, par3 + 1, par4);
    }

    /**
     * Notifies a block that one of its neighbor change to the specified type Args: x, y, z, blockID
     */
    @Override
    public void notifyBlockOfNeighborChange(int par1, int par2, int par3, int par4)
    {
        if (!this.editingBlocks && !this.isRemote)
        {
            Block var5 = Block.blocksList[this.getBlockId(par1, par2, par3)];

            if (var5 != null)
            {
                var5.onNeighborBlockChange(this, par1, par2, par3, par4);
            }
        }
    }

    /**
     * Checks if the specified block is able to see the sky
     */
    @Override
    public boolean canBlockSeeTheSky(int par1, int par2, int par3)
    {
        return this.getChunkFromChunkCoords(par1 >> 4, par3 >> 4).canBlockSeeTheSky(par1 & 15, par2, par3 & 15);
    }

    /**
     * ray traces all blocks, including non-collideable ones
     */
    @Override
    public MovingObjectPosition clip(Vec3 par1Vec3, Vec3 par2Vec3)
    {
        return this.rayTraceBlocks_do_do(par1Vec3, par2Vec3, false, false);
    }

    @Override
    public MovingObjectPosition clip(Vec3 par1Vec3, Vec3 par2Vec3, boolean par3)
    {
        return this.rayTraceBlocks_do_do(par1Vec3, par2Vec3, par3, false);
    }

    @Override
    public MovingObjectPosition rayTraceBlocks_do_do(Vec3 par1Vec3, Vec3 par2Vec3, boolean par3, boolean par4)
    {
        if (!Double.isNaN(par1Vec3.xCoord) && !Double.isNaN(par1Vec3.yCoord) && !Double.isNaN(par1Vec3.zCoord))
        {
            if (!Double.isNaN(par2Vec3.xCoord) && !Double.isNaN(par2Vec3.yCoord) && !Double.isNaN(par2Vec3.zCoord))
            {
                int var5 = MathHelper.floor_double(par2Vec3.xCoord);
                int var6 = MathHelper.floor_double(par2Vec3.yCoord);
                int var7 = MathHelper.floor_double(par2Vec3.zCoord);
                int var8 = MathHelper.floor_double(par1Vec3.xCoord);
                int var9 = MathHelper.floor_double(par1Vec3.yCoord);
                int var10 = MathHelper.floor_double(par1Vec3.zCoord);
                int var11 = this.getBlockId(var8, var9, var10);
                int var12 = this.getBlockMetadata(var8, var9, var10);
                Block var13 = Block.blocksList[var11];

                if ((!par4 || var13 == null || var13.getCollisionBoundingBoxFromPool(this, var8, var9, var10) != null) && var11 > 0 && var13.canCollideCheck(var12, par3))
                {
                    MovingObjectPosition var14 = var13.collisionRayTrace(this, var8, var9, var10, par1Vec3, par2Vec3);

                    if (var14 != null)
                    {
                        return var14;
                    }
                }

                var11 = 200;

                while (var11-- >= 0)
                {
                    if (Double.isNaN(par1Vec3.xCoord) || Double.isNaN(par1Vec3.yCoord) || Double.isNaN(par1Vec3.zCoord))
                    {
                        return null;
                    }

                    if (var8 == var5 && var9 == var6 && var10 == var7)
                    {
                        return null;
                    }

                    boolean var39 = true;
                    boolean var40 = true;
                    boolean var41 = true;
                    double var15 = 999.0D;
                    double var17 = 999.0D;
                    double var19 = 999.0D;

                    if (var5 > var8)
                    {
                        var15 = (double)var8 + 1.0D;
                    }
                    else if (var5 < var8)
                    {
                        var15 = (double)var8 + 0.0D;
                    }
                    else
                    {
                        var39 = false;
                    }

                    if (var6 > var9)
                    {
                        var17 = (double)var9 + 1.0D;
                    }
                    else if (var6 < var9)
                    {
                        var17 = (double)var9 + 0.0D;
                    }
                    else
                    {
                        var40 = false;
                    }

                    if (var7 > var10)
                    {
                        var19 = (double)var10 + 1.0D;
                    }
                    else if (var7 < var10)
                    {
                        var19 = (double)var10 + 0.0D;
                    }
                    else
                    {
                        var41 = false;
                    }

                    double var21 = 999.0D;
                    double var23 = 999.0D;
                    double var25 = 999.0D;
                    double var27 = par2Vec3.xCoord - par1Vec3.xCoord;
                    double var29 = par2Vec3.yCoord - par1Vec3.yCoord;
                    double var31 = par2Vec3.zCoord - par1Vec3.zCoord;

                    if (var39)
                    {
                        var21 = (var15 - par1Vec3.xCoord) / var27;
                    }

                    if (var40)
                    {
                        var23 = (var17 - par1Vec3.yCoord) / var29;
                    }

                    if (var41)
                    {
                        var25 = (var19 - par1Vec3.zCoord) / var31;
                    }

                    boolean var33 = false;
                    byte var42;

                    if (var21 < var23 && var21 < var25)
                    {
                        if (var5 > var8)
                        {
                            var42 = 4;
                        }
                        else
                        {
                            var42 = 5;
                        }

                        par1Vec3.xCoord = var15;
                        par1Vec3.yCoord += var29 * var21;
                        par1Vec3.zCoord += var31 * var21;
                    }
                    else if (var23 < var25)
                    {
                        if (var6 > var9)
                        {
                            var42 = 0;
                        }
                        else
                        {
                            var42 = 1;
                        }

                        par1Vec3.xCoord += var27 * var23;
                        par1Vec3.yCoord = var17;
                        par1Vec3.zCoord += var31 * var23;
                    }
                    else
                    {
                        if (var7 > var10)
                        {
                            var42 = 2;
                        }
                        else
                        {
                            var42 = 3;
                        }

                        par1Vec3.xCoord += var27 * var25;
                        par1Vec3.yCoord += var29 * var25;
                        par1Vec3.zCoord = var19;
                    }

                    Vec3 var34 = Vec3.createVectorHelper(par1Vec3.xCoord, par1Vec3.yCoord, par1Vec3.zCoord);
                    var8 = (int)(var34.xCoord = (double)MathHelper.floor_double(par1Vec3.xCoord));

                    if (var42 == 5)
                    {
                        --var8;
                        ++var34.xCoord;
                    }

                    var9 = (int)(var34.yCoord = (double)MathHelper.floor_double(par1Vec3.yCoord));

                    if (var42 == 1)
                    {
                        --var9;
                        ++var34.yCoord;
                    }

                    var10 = (int)(var34.zCoord = (double)MathHelper.floor_double(par1Vec3.zCoord));

                    if (var42 == 3)
                    {
                        --var10;
                        ++var34.zCoord;
                    }

                    int var35 = this.getBlockId(var8, var9, var10);
                    int var36 = this.getBlockMetadata(var8, var9, var10);
                    Block var37 = Block.blocksList[var35];

                    if ((!par4 || var37 == null || var37.getCollisionBoundingBoxFromPool(this, var8, var9, var10) != null) && var35 > 0 && var37.canCollideCheck(var36, par3))
                    {
                        MovingObjectPosition var38 = var37.collisionRayTrace(this, var8, var9, var10, par1Vec3, par2Vec3);

                        if (var38 != null)
                        {
                            return var38;
                        }
                    }
                }

                return null;
            }
            else
            {
                return null;
            }
        }
        else
        {
            return null;
        }
    }

    /**
     * Plays a sound at the entity's position. Args: entity, sound, unknown1, volume (relative to 1.0)
     */
    @Override
    public void playSoundAtEntity(Entity par1Entity, String par2Str, float par3, float par4)
    {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.enableSP)
        {
            float var1 = 16F;

            if (par3 > 1.0F)
            {
                var1 *= par3;
            }

            if (mc.renderViewEntity == null)
            {
                return;
            }
            if (mc.renderViewEntity.getDistanceSq(par1Entity.posX, par1Entity.posY - (double)par1Entity.yOffset, par1Entity.posZ) < (double)(var1 * var1))
            {
            	mc.sndManager.playSound(par2Str, (float)par1Entity.posX, (float)(par1Entity.posY - (double)par1Entity.yOffset), (float)par1Entity.posZ, par3, par4);
            }
            return;
        }
        for (int var2 = 0; var2 < worldAccesses.size(); var2++)
        {
            ((IWorldAccess)worldAccesses.get(var2)).playSound(par2Str, par1Entity.posX, par1Entity.posY - (double)par1Entity.yOffset, par1Entity.posZ, par3, par4);
        }
    }

    /**
     * Play a sound effect. Many many parameters for this function. Not sure what they do, but a classic call is :
     * (double)i + 0.5D, (double)j + 0.5D, (double)k + 0.5D, 'random.door_open', 1.0F, world.rand.nextFloat() * 0.1F +
     * 0.9F with i,j,k position of the block.
     */
    @Override
    public void playSoundEffect(double par1, double par3, double par5, String par7Str, float par8, float par9)
    {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.enableSP)
        {
            float var1 = 16F;

            if (par8 > 1.0F)
            {
                var1 *= par8;
            }

            if (mc.renderViewEntity == null)
            {
                return;
            }
            if (mc.renderViewEntity.getDistanceSq(par1, par3, par5) < (double)(var1 * var1))
            {
                mc.sndManager.playSound(par7Str, (float)par1, (float)par3, (float)par5, par8, par9);
            }
            return;
        }
        for (int var2 = 0; var2 < this.worldAccesses.size(); var2++)
        {
            ((IWorldAccess)this.worldAccesses.get(var2)).playSound(par7Str, par1, par3, par5, par8, par9);
        }
    }

    /**
     * Plays a record at the specified coordinates of the specified name. Args: recordName, x, y, z
     */
    @Override
    public void playRecord(String par1Str, int par2, int par3, int par4)
    {
        for (int var1 = 0; var1 < this.worldAccesses.size(); var1++)
        {
            ((IWorldAccess)this.worldAccesses.get(var1)).playRecord(par1Str, par2, par3, par4);
        }
    }

    /**
     * Spawns a particle.  Args particleName, x, y, z, velX, velY, velZ
     */
    @Override
    public void spawnParticle(String par1Str, double par2, double par4, double par6, double par8, double par10, double par12)
    {
        for (int var1 = 0; var1 < this.worldAccesses.size(); var1++)
        {
            ((IWorldAccess)this.worldAccesses.get(var1)).spawnParticle(par1Str, par2, par4, par6, par8, par10, par12);
        }
    }

    /**
     * adds a lightning bolt to the list of lightning bolts in this world.
     */
    @Override
    public boolean addWeatherEffect(Entity par1Entity)
    {
        this.weatherEffects.add(par1Entity);
        return true;
    }

    /**
     * Called to place all entities as part of a world
     */
    @Override
    public boolean spawnEntityInWorld(Entity par1Entity)
    {
        int var2 = MathHelper.floor_double(par1Entity.posX / 16.0D);
        int var3 = MathHelper.floor_double(par1Entity.posZ / 16.0D);
        boolean var4 = par1Entity.forceSpawn;

        if (par1Entity instanceof EntityPlayer)
        {
            var4 = true;
        }

        if (!var4 && !this.chunkExists(var2, var3))
        {
            return false;
        }
        else
        {
            if (par1Entity instanceof EntityPlayer)
            {
                EntityPlayer var5 = (EntityPlayer)par1Entity;
                this.playerEntities.add(var5);
                this.updateAllPlayersSleepingFlag();
            }

            this.getChunkFromChunkCoords(var2, var3).addEntity(par1Entity);
            this.loadedEntityList.add(par1Entity);
            this.onEntityAdded(par1Entity);
            return true;
        }
    }

    /**
     * Start the skin for this entity downloading, if necessary, and increment its reference counter
     */
    @Override
    protected void onEntityAdded(Entity par1Entity)
    {
        for (int var1 = 0; var1 < this.worldAccesses.size(); var1++)
        {
            ((IWorldAccess)this.worldAccesses.get(var1)).onEntityCreate(par1Entity);
        }
    }

    /**
     * Decrement the reference counter for this entity's skin image data
     */
    @Override
    protected void onEntityRemoved(Entity par1Entity)
    {
        for (int var1 = 0; var1 < this.worldAccesses.size(); var1++)
        {
            ((IWorldAccess)this.worldAccesses.get(var1)).onEntityDestroy(par1Entity);
        }
    }

    /**
     * Dismounts the entity (and anything riding the entity), sets the dead flag, and removes the player entity from the
     * player entity list. Called by the playerLoggedOut function.
     */
    @Override
    public void removeEntity(Entity par1Entity)
    {
        if (par1Entity.riddenByEntity != null)
        {
            par1Entity.riddenByEntity.mountEntity((Entity)null);
        }

        if (par1Entity.ridingEntity != null)
        {
            par1Entity.mountEntity((Entity)null);
        }

        par1Entity.setDead();

        if (par1Entity instanceof EntityPlayer)
        {
            this.playerEntities.remove((EntityPlayer)par1Entity);
            this.updateAllPlayersSleepingFlag();
        }
    }

    /**
     * Adds a IWorldAccess to the list of worldAccesses
     */
    @Override
    public void addWorldAccess(IWorldAccess par1IWorldAccess)
    {
        this.worldAccesses.add(par1IWorldAccess);
    }

    /**
     * Removes a worldAccess from the worldAccesses object
     */
    @Override
    public void removeWorldAccess(IWorldAccess par1IWorldAccess)
    {
        this.worldAccesses.remove(par1IWorldAccess);
    }

    /**
     * Returns a list of bounding boxes that collide with aabb excluding the passed in entity's collision. Args: entity,
     * aabb
     */
    @Override
    public List getCollidingBoundingBoxes(Entity par1Entity, AxisAlignedBB par2AxisAlignedBB)
    {
        this.collidingBoundingBoxes.clear();
        int var3 = MathHelper.floor_double(par2AxisAlignedBB.minX);
        int var4 = MathHelper.floor_double(par2AxisAlignedBB.maxX + 1.0D);
        int var5 = MathHelper.floor_double(par2AxisAlignedBB.minY);
        int var6 = MathHelper.floor_double(par2AxisAlignedBB.maxY + 1.0D);
        int var7 = MathHelper.floor_double(par2AxisAlignedBB.minZ);
        int var8 = MathHelper.floor_double(par2AxisAlignedBB.maxZ + 1.0D);

        for (int var9 = var3; var9 < var4; ++var9)
        {
            for (int var10 = var7; var10 < var8; ++var10)
            {
                if (this.blockExists(var9, 64, var10))
                {
                    for (int var11 = var5 - 1; var11 < var6; ++var11)
                    {
                        Block var12 = Block.blocksList[this.getBlockId(var9, var11, var10)];

                        if (var12 != null)
                        {
                            var12.addCollisionBoxesToList(this, var9, var11, var10, par2AxisAlignedBB, this.collidingBoundingBoxes, par1Entity);
                        }
                    }
                }
            }
        }

        double var14 = 0.25D;
        List var15 = this.getEntitiesWithinAABBExcludingEntity(par1Entity, par2AxisAlignedBB.expand(var14, var14, var14));

        for (int var16 = 0; var16< var15.size(); var16++)
        {
            AxisAlignedBB var13 = ((Entity)var15.get(var16)).getBoundingBox();

            if (var13 != null && var13.intersectsWith(par2AxisAlignedBB))
            {
                this.collidingBoundingBoxes.add(var13);
            }

            var13 = par1Entity.getCollisionBox((Entity)var15.get(var16));

            if (var13 != null && var13.intersectsWith(par2AxisAlignedBB))
            {
                this.collidingBoundingBoxes.add(var13);
            }
        }

        return this.collidingBoundingBoxes;
    }

    /**
     * calls calculateCelestialAngle
     */
    @Override
    public float getCelestialAngle(float par1)
    {
        if (Minecraft.timecontrol && this.provider.dimensionId == 0)
        {
            return super.getCelestialAngle(par1) + (float)(this.field_35467_J + (this.field_35468_K - this.field_35467_J) * (double)par1);
        }
        return super.getCelestialAngle(par1);
    }

    @Override
    public int getMoonPhase()
    {
        return this.provider.getMoonPhase(this.worldInfo.getWorldTotalTime());
    }

    /**
     * Return getCelestialAngle()*2*PI
     */
    @Override
    public float getCelestialAngleRadians(float par1)
    {
        float var2 = this.getCelestialAngle(par1);
        return var2 * (float)Math.PI * 2.0F;
    }

    /**
     * Schedules a tick to a block with a delay (Most commonly the tick rate)
     */
    @Override
    public void scheduleBlockUpdate(int par1, int par2, int par3, int par4, int par5)
    {
        scheduleBlockUpdateWithPriority(par1, par2, par3, par4, par5, 0);
    }

    @Override
    public void scheduleBlockUpdateWithPriority(int par1, int par2, int par3, int par4, int par5, int par6)
    {
        NextTickListEntry var6 = new NextTickListEntry(par1, par2, par3, par4);
        byte var7 = 8;

        if (this.scheduledUpdatesAreImmediate)
        {
            if (this.checkChunksExist(var6.xCoord - var7, var6.yCoord - var7, var6.zCoord - var7, var6.xCoord + var7, var6.yCoord + var7, var6.zCoord + var7))
            {
                int var8 = this.getBlockId(var6.xCoord, var6.yCoord, var6.zCoord);

                if (var8 == var6.blockID && var8 > 0)
                {
                    Block.blocksList[var8].updateTick(this, var6.xCoord, var6.yCoord, var6.zCoord, this.rand);
                }
            }
        }
        else
        {
            if (this.checkChunksExist(par1 - var7, par2 - var7, par3 - var7, par1 + var7, par2 + var7, par3 + var7))
            {
                if (par4 > 0)
                {
                    var6.setScheduledTime((long)par5 + this.worldInfo.getWorldTotalTime());
                    var6.setPriority(par6);
                }

                if (!this.scheduledTickSet.contains(var6))
                {
                    this.scheduledTickSet.add(var6);
                    this.scheduledTickTreeSet.add(var6);
                }
            }
        }
    }

    /**
     * Schedules a block update from the saved information in a chunk. Called when the chunk is loaded.
     */
    @Override
    public void scheduleBlockUpdateFromLoad(int par1, int par2, int par3, int par4, int par5, int par6)
    {
        NextTickListEntry var6 = new NextTickListEntry(par1, par2, par3, par4);

        if (par4 > 0)
        {
            var6.setScheduledTime((long)par5 + this.worldInfo.getWorldTotalTime());
        }

        if (!this.scheduledTickSet.contains(var6))
        {
            this.scheduledTickSet.add(var6);
            this.scheduledTickTreeSet.add(var6);
        }
    }

    /**
     * Returns true if there are no solid, live entities in the specified AxisAlignedBB
     */
    @Override
    public boolean checkNoEntityCollision(AxisAlignedBB par1AxisAlignedBB)
    {
        List var2 = this.getEntitiesWithinAABBExcludingEntity((Entity)null, par1AxisAlignedBB);

        for (int var3 = 0; var3 < var2.size(); var3++)
        {
            Entity var4 = (Entity)var2.get(var3);

            if (!var4.isDead && var4.preventEntitySpawning)
            {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns if any of the blocks within the aabb are liquids. Args: aabb
     */
    @Override
    public boolean isAnyLiquid(AxisAlignedBB par1AxisAlignedBB)
    {
        int var2 = MathHelper.floor_double(par1AxisAlignedBB.minX);
        int var3 = MathHelper.floor_double(par1AxisAlignedBB.maxX + 1.0D);
        int var4 = MathHelper.floor_double(par1AxisAlignedBB.minY);
        int var5 = MathHelper.floor_double(par1AxisAlignedBB.maxY + 1.0D);
        int var6 = MathHelper.floor_double(par1AxisAlignedBB.minZ);
        int var7 = MathHelper.floor_double(par1AxisAlignedBB.maxZ + 1.0D);

        if (par1AxisAlignedBB.minX < 0.0D)
        {
            --var2;
        }

        if (par1AxisAlignedBB.minY < 0.0D)
        {
            --var4;
        }

        if (par1AxisAlignedBB.minZ < 0.0D)
        {
            --var6;
        }

        for (int var8 = var2; var8 < var3; ++var8)
        {
            for (int var9 = var4; var9 < var5; ++var9)
            {
                for (int var10 = var6; var10 < var7; ++var10)
                {
                    Block var11 = Block.blocksList[this.getBlockId(var8, var9, var10)];

                    if (var11 != null && var11.blockMaterial.isLiquid())
                    {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Returns whether or not the given bounding box is on fire or not
     */
    @Override
    public boolean isBoundingBoxBurning(AxisAlignedBB par1AxisAlignedBB)
    {
        int var2 = MathHelper.floor_double(par1AxisAlignedBB.minX);
        int var3 = MathHelper.floor_double(par1AxisAlignedBB.maxX + 1.0D);
        int var4 = MathHelper.floor_double(par1AxisAlignedBB.minY);
        int var5 = MathHelper.floor_double(par1AxisAlignedBB.maxY + 1.0D);
        int var6 = MathHelper.floor_double(par1AxisAlignedBB.minZ);
        int var7 = MathHelper.floor_double(par1AxisAlignedBB.maxZ + 1.0D);

        if (this.checkChunksExist(var2, var4, var6, var3, var5, var7))
        {
            for (int var8 = var2; var8 < var3; ++var8)
            {
                for (int var9 = var4; var9 < var5; ++var9)
                {
                    for (int var10 = var6; var10 < var7; ++var10)
                    {
                        int var11 = this.getBlockId(var8, var9, var10);

                        if (var11 == Block.fire.blockID || var11 == Block.lavaMoving.blockID || var11 == Block.lavaStill.blockID)
                        {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    /**
     * handles the acceleration of an object whilst in water. Not sure if it is used elsewhere.
     */
    @Override
    public boolean handleMaterialAcceleration(AxisAlignedBB par1AxisAlignedBB, Material par2Material, Entity par3Entity)
    {
        int var4 = MathHelper.floor_double(par1AxisAlignedBB.minX);
        int var5 = MathHelper.floor_double(par1AxisAlignedBB.maxX + 1.0D);
        int var6 = MathHelper.floor_double(par1AxisAlignedBB.minY);
        int var7 = MathHelper.floor_double(par1AxisAlignedBB.maxY + 1.0D);
        int var8 = MathHelper.floor_double(par1AxisAlignedBB.minZ);
        int var9 = MathHelper.floor_double(par1AxisAlignedBB.maxZ + 1.0D);

        if (!this.checkChunksExist(var4, var6, var8, var5, var7, var9))
        {
            return false;
        }
        else
        {
            boolean var10 = false;
            Vec3 var11 = Vec3.createVectorHelper(0.0D, 0.0D, 0.0D);

            for (int var12 = var4; var12 < var5; ++var12)
            {
                for (int var13 = var6; var13 < var7; ++var13)
                {
                    for (int var14 = var8; var14 < var9; ++var14)
                    {
                        Block var15 = Block.blocksList[this.getBlockId(var12, var13, var14)];

                        if (var15 != null && var15.blockMaterial == par2Material)
                        {
                            double var16 = (double)((float)(var13 + 1) - BlockFluid.getFluidHeightPercent(this.getBlockMetadata(var12, var13, var14)));

                            if ((double)var7 >= var16)
                            {
                                var10 = true;
                                var15.velocityToAddToEntity(this, var12, var13, var14, par3Entity, var11);
                            }
                        }
                    }
                }
            }

            if (var11.lengthVector() > 0.0D)
            {
                var11 = var11.normalize();
                double var18 = 0.014D;
                par3Entity.motionX += var11.xCoord * var18;
                par3Entity.motionY += var11.yCoord * var18;
                par3Entity.motionZ += var11.zCoord * var18;
            }

            return var10;
        }
    }

    /**
     * Returns true if the given bounding box contains the given material
     */
    @Override
    public boolean isMaterialInBB(AxisAlignedBB par1AxisAlignedBB, Material par2Material)
    {
        int var3 = MathHelper.floor_double(par1AxisAlignedBB.minX);
        int var4 = MathHelper.floor_double(par1AxisAlignedBB.maxX + 1.0D);
        int var5 = MathHelper.floor_double(par1AxisAlignedBB.minY);
        int var6 = MathHelper.floor_double(par1AxisAlignedBB.maxY + 1.0D);
        int var7 = MathHelper.floor_double(par1AxisAlignedBB.minZ);
        int var8 = MathHelper.floor_double(par1AxisAlignedBB.maxZ + 1.0D);

        for (int var9 = var3; var9 < var4; ++var9)
        {
            for (int var10 = var5; var10 < var6; ++var10)
            {
                for (int var11 = var7; var11 < var8; ++var11)
                {
                    Block var12 = Block.blocksList[this.getBlockId(var9, var10, var11)];

                    if (var12 != null && var12.blockMaterial == par2Material)
                    {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * checks if the given AABB is in the material given. Used while swimming.
     */
    @Override
    public boolean isAABBInMaterial(AxisAlignedBB par1AxisAlignedBB, Material par2Material)
    {
        int var3 = MathHelper.floor_double(par1AxisAlignedBB.minX);
        int var4 = MathHelper.floor_double(par1AxisAlignedBB.maxX + 1.0D);
        int var5 = MathHelper.floor_double(par1AxisAlignedBB.minY);
        int var6 = MathHelper.floor_double(par1AxisAlignedBB.maxY + 1.0D);
        int var7 = MathHelper.floor_double(par1AxisAlignedBB.minZ);
        int var8 = MathHelper.floor_double(par1AxisAlignedBB.maxZ + 1.0D);

        for (int var9 = var3; var9 < var4; ++var9)
        {
            for (int var10 = var5; var10 < var6; ++var10)
            {
                for (int var11 = var7; var11 < var8; ++var11)
                {
                    Block var12 = Block.blocksList[this.getBlockId(var9, var10, var11)];

                    if (var12 != null && var12.blockMaterial == par2Material)
                    {
                        int var13 = this.getBlockMetadata(var9, var10, var11);
                        double var14 = (double)(var10 + 1);

                        if (var13 < 8)
                        {
                            var14 = (double)(var10 + 1) - (double)var13 / 8.0D;
                        }

                        if (var14 >= par1AxisAlignedBB.minY)
                        {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    /**
     * Creates an explosion. Args: entity, x, y, z, strength
     */
    @Override
    public Explosion createExplosion(Entity par1Entity, double par2, double par4, double par6, float par8, boolean par9)
    {
        return this.newExplosion(par1Entity, par2, par4, par6, par8, false, par9);
    }

    /**
     * returns a new explosion. Does initiation (at time of writing Explosion is not finished)
     */
    @Override
    public Explosion newExplosion(Entity par1Entity, double par2, double par4, double par6, float par8, boolean par9, boolean par10)
    {
        Explosion var10 = new Explosion(this, par1Entity, par2, par4, par6, par8);
        var10.isFlaming = par9;
        var10.isSmoking = par10;
        var10.doExplosionA();
        var10.doExplosionB(true);
        return var10;
    }

    /**
     * Gets the percentage of real blocks within within a bounding box, along a specified vector.
     */
    @Override
    public float getBlockDensity(Vec3 par1Vec3, AxisAlignedBB par2AxisAlignedBB)
    {
        double var3 = 1.0D / ((par2AxisAlignedBB.maxX - par2AxisAlignedBB.minX) * 2.0D + 1.0D);
        double var5 = 1.0D / ((par2AxisAlignedBB.maxY - par2AxisAlignedBB.minY) * 2.0D + 1.0D);
        double var7 = 1.0D / ((par2AxisAlignedBB.maxZ - par2AxisAlignedBB.minZ) * 2.0D + 1.0D);
        int var9 = 0;
        int var10 = 0;

        for (float var11 = 0.0F; var11 <= 1.0F; var11 = (float)((double)var11 + var3))
        {
            for (float var12 = 0.0F; var12 <= 1.0F; var12 = (float)((double)var12 + var5))
            {
                for (float var13 = 0.0F; var13 <= 1.0F; var13 = (float)((double)var13 + var7))
                {
                    double var14 = par2AxisAlignedBB.minX + (par2AxisAlignedBB.maxX - par2AxisAlignedBB.minX) * (double)var11;
                    double var16 = par2AxisAlignedBB.minY + (par2AxisAlignedBB.maxY - par2AxisAlignedBB.minY) * (double)var12;
                    double var18 = par2AxisAlignedBB.minZ + (par2AxisAlignedBB.maxZ - par2AxisAlignedBB.minZ) * (double)var13;

                    if (this.clip(Vec3.createVectorHelper(var14, var16, var18), par1Vec3) == null)
                    {
                        ++var9;
                    }

                    ++var10;
                }
            }
        }

        return (float)var9 / (float)var10;
    }

    @Override
    public boolean extinguishFire(EntityPlayer par1EntityPlayer, int par2, int par3, int par4, int par5)
    {
        if (par5 == 0)
        {
            --par3;
        }

        if (par5 == 1)
        {
            ++par3;
        }

        if (par5 == 2)
        {
            --par4;
        }

        if (par5 == 3)
        {
            ++par4;
        }

        if (par5 == 4)
        {
            --par2;
        }

        if (par5 == 5)
        {
            ++par2;
        }

        if (this.getBlockId(par2, par3, par4) == Block.fire.blockID)
        {
            this.playAuxSFXAtEntity(par1EntityPlayer, 1004, par2, par3, par4, 0);
            this.setBlockToAir(par2, par3, par4);
            return true;
        }
        else
        {
            return false;
        }
    }

    public Entity func_4085_a(Class par1Class)
    {
        return null;
    }

    /**
     * This string is 'All: (number of loaded entities)' Viewable by press ing F3
     */
    @Override
    public String getDebugLoadedEntities()
    {
        return "All: " + this.loadedEntityList.size();
    }

    /**
     * Returns the name of the current chunk provider, by calling chunkprovider.makeString()
     */
    @Override
    public String getProviderName()
    {
        return this.chunkProvider.makeString();
    }

    /**
     * adds tile entity to despawn list (renamed from markEntityForDespawn)
     */
    @Override
    public void markTileEntityForDespawn(TileEntity par1TileEntity)
    {
        this.entityRemoval.add(par1TileEntity);
    }

    /**
     * Returns true if the block at the specified coordinates is an opaque cube. Args: x, y, z
     */
    @Override
    public boolean isBlockOpaqueCube(int par1, int par2, int par3)
    {
        Block var4 = Block.blocksList[this.getBlockId(par1, par2, par3)];
        return var4 == null ? false : var4.isOpaqueCube();
    }

    /**
     * Indicate if a material is a normal solid opaque cube.
     */
    @Override
    public boolean isBlockNormalCube(int par1, int par2, int par3)
    {
        return Block.isNormalCube(this.getBlockId(par1, par2, par3));
    }

    public void saveWorldIndirectly(IProgressUpdate par1IProgressUpdate)
    {
        this.saveWorld(true, par1IProgressUpdate);

        try
        {
            ThreadedFileIOBase.threadedIOInstance.waitForFinish();
        }
        catch (InterruptedException var3)
        {
            var3.printStackTrace();
        }
    }

    /**
     * Set which types of mobs are allowed to spawn (peaceful vs hostile).
     */
    @Override
    public void setAllowedSpawnTypes(boolean par1, boolean par2)
    {
        this.spawnHostileMobs = par1;
        this.spawnPeacefulMobs = par2;
    }

    /**
     * Runs a single tick for the world
     */
    @Override
    public void tick()
    {
    	this.field_35467_J = this.field_35468_K;
        this.field_35468_K += this.field_35465_L;
        this.field_35465_L *= 0.97999999999999998D;

        if (this.getWorldInfo().isHardcoreModeEnabled() && this.difficultySetting < 3)
        {
            this.difficultySetting = 3;
        }

        this.provider.worldChunkMgr.cleanupCache();
        this.updateWeather();
        long var2;

        if (this.isAllPlayersFullyAsleep())
        {
            boolean var1 = false;

            if (this.spawnHostileMobs && this.difficultySetting >= 1)
            {
                ;
            }

            if (!var1)
            {
            	if (getGameRules().getGameRuleBooleanValue("doDaylightCycle"))
                {
                    long l = this.worldInfo.getWorldTotalTime() + 24000L;
                    this.worldInfo.setWorldTime(l - l % 24000L);
                    this.func_82738_a(l - l % 24000L);
                    this.field_35467_J = 0D;
                    this.field_35468_K = 0D;
                }
                this.wakeUpAllPlayers();
            }
        }

        this.theProfiler.startSection("mobSpawner");
        if (getGameRules().getGameRuleBooleanValue("doMobSpawning"))
        {
            this.animalSpawner.performSpawningSP(this, this.spawnHostileMobs, this.spawnPeacefulMobs && this.worldInfo.getWorldTotalTime() % 400L == 0L);
        }
        this.theProfiler.endStartSection("chunkSource");
        this.chunkProvider.unloadQueuedChunks();
        int var4 = this.calculateSkylightSubtracted(1.0F);

        if (var4 != this.skylightSubtracted)
        {
            this.skylightSubtracted = var4;
        }

        this.worldInfo.incrementTotalWorldTime(this.worldInfo.getWorldTotalTime() + 1L);

        var2 = this.worldInfo.getWorldTotalTime() + 1L;

        if (var2 % (long)this.autosavePeriod == 0L)
        {
        	this.theProfiler.endStartSection("save");
            this.saveWorld(false, (IProgressUpdate)null);
        }

        if (getGameRules().getGameRuleBooleanValue("doDaylightCycle"))
        {
        	this.worldInfo.setWorldTime(this.worldInfo.getWorldTime() + 1L);
        }
        this.theProfiler.endStartSection("tickPending");
        this.tickUpdates(false);
        this.theProfiler.endStartSection("tickTiles");
        this.tickBlocksAndAmbiance();
        this.theProfiler.endStartSection("village");
        this.villageCollectionObj.tick();
        this.villageSiegeObj.tick();
        this.theProfiler.endSection();
    }

    /**
     * Called from World constructor to set rainingStrength and thunderingStrength
     */
    private void calculateInitialWeather()
    {
        if (this.worldInfo.isRaining())
        {
            this.rainingStrength = 1.0F;

            if (this.worldInfo.isThundering())
            {
                this.thunderingStrength = 1.0F;
            }
        }
    }

    /**
     * Updates all weather states.
     */
    @Override
    protected void updateWeather()
    {
        if (!this.provider.hasNoSky)
        {
            if (this.lastLightningBolt > 0)
            {
                --this.lastLightningBolt;
            }

            int var1 = this.worldInfo.getThunderTime();

            if (var1 <= 0)
            {
                if (this.worldInfo.isThundering())
                {
                    this.worldInfo.setThunderTime(this.rand.nextInt(12000) + 3600);
                }
                else
                {
                    this.worldInfo.setThunderTime(this.rand.nextInt(168000) + 12000);
                }
            }
            else
            {
                --var1;
                this.worldInfo.setThunderTime(var1);

                if (var1 <= 0)
                {
                    this.worldInfo.setThundering(!this.worldInfo.isThundering());
                }
            }

            int var2 = this.worldInfo.getRainTime();

            if (var2 <= 0)
            {
                if (this.worldInfo.isRaining())
                {
                    this.worldInfo.setRainTime(this.rand.nextInt(12000) + 12000);
                }
                else
                {
                    this.worldInfo.setRainTime(this.rand.nextInt(168000) + 12000);
                }
            }
            else
            {
                --var2;
                this.worldInfo.setRainTime(var2);

                if (var2 <= 0)
                {
                    this.worldInfo.setRaining(!this.worldInfo.isRaining());
                }
            }

            this.prevRainingStrength = this.rainingStrength;

            if (this.worldInfo.isRaining())
            {
                this.rainingStrength = (float)((double)this.rainingStrength + 0.01D);
            }
            else
            {
                this.rainingStrength = (float)((double)this.rainingStrength - 0.01D);
            }

            if (this.rainingStrength < 0.0F)
            {
                this.rainingStrength = 0.0F;
            }

            if (this.rainingStrength > 1.0F)
            {
                this.rainingStrength = 1.0F;
            }

            this.prevThunderingStrength = this.thunderingStrength;

            if (this.worldInfo.isThundering())
            {
                this.thunderingStrength = (float)((double)this.thunderingStrength + 0.01D);
            }
            else
            {
                this.thunderingStrength = (float)((double)this.thunderingStrength - 0.01D);
            }

            if (this.thunderingStrength < 0.0F)
            {
                this.thunderingStrength = 0.0F;
            }

            if (this.thunderingStrength > 1.0F)
            {
                this.thunderingStrength = 1.0F;
            }
        }
    }

    /**
     * Stops all weather effects.
     */
    private void clearWeather()
    {
        this.worldInfo.setRainTime(0);
        this.worldInfo.setRaining(false);
        this.worldInfo.setThunderTime(0);
        this.worldInfo.setThundering(false);
    }

    protected void func_48461_r()
    {
        this.activeChunkSet.clear();
        this.theProfiler.startSection("buildList");
        int var1;
        EntityPlayer var2;
        int var3;
        int var4;

        for (var1 = 0; var1 < this.playerEntities.size(); ++var1)
        {
            var2 = (EntityPlayer)this.playerEntities.get(var1);
            var3 = MathHelper.floor_double(var2.posX / 16.0D);
            var4 = MathHelper.floor_double(var2.posZ / 16.0D);
            byte var5 = 7;

            for (int var6 = -var5; var6 <= var5; ++var6)
            {
                for (int var7 = -var5; var7 <= var5; ++var7)
                {
                    this.activeChunkSet.add(new ChunkCoordIntPair(var6 + var3, var7 + var4));
                }
            }
        }

        this.theProfiler.endSection();

        if (this.ambientTickCountdown > 0)
        {
            --this.ambientTickCountdown;
        }

        this.theProfiler.startSection("playerCheckLight");

        if (!this.playerEntities.isEmpty())
        {
            var1 = this.rand.nextInt(this.playerEntities.size());
            var2 = (EntityPlayer)this.playerEntities.get(var1);
            var3 = MathHelper.floor_double(var2.posX) + this.rand.nextInt(11) - 5;
            var4 = MathHelper.floor_double(var2.posY) + this.rand.nextInt(11) - 5;
            int var8 = MathHelper.floor_double(var2.posZ) + this.rand.nextInt(11) - 5;
            this.updateAllLightTypes(var3, var4, var8);
        }

        this.theProfiler.endSection();
    }

    protected void func_48458_a(int par1, int par2, Chunk par3Chunk)
    {
    	this.theProfiler.endStartSection("tickChunk");
        par3Chunk.updateSkylight();
        this.theProfiler.endStartSection("moodSound");

        if (this.ambientTickCountdown == 0)
        {
            this.updateLCG = this.updateLCG * 3 + 1013904223;
            int var4 = this.updateLCG >> 2;
            int var5 = var4 & 15;
            int var6 = var4 >> 8 & 15;
            int var7 = var4 >> 16 & 127;
            int var8 = par3Chunk.getBlockID(var5, var7, var6);
            var5 += par1;
            var6 += par2;

            if (var8 == 0 && this.getFullBlockLightValue(var5, var7, var6) <= this.rand.nextInt(8) && this.getSavedLightValue(EnumSkyBlock.Sky, var5, var7, var6) <= 0)
            {
                EntityPlayer var9 = this.getClosestPlayer((double)var5 + 0.5D, (double)var7 + 0.5D, (double)var6 + 0.5D, 8.0D);

                if (var9 != null && var9.getDistanceSq((double)var5 + 0.5D, (double)var7 + 0.5D, (double)var6 + 0.5D) > 4.0D)
                {
                    this.playSoundEffect((double)var5 + 0.5D, (double)var7 + 0.5D, (double)var6 + 0.5D, "ambient.cave.cave", 0.7F, 0.8F + this.rand.nextFloat() * 0.2F);
                    this.ambientTickCountdown = this.rand.nextInt(12000) + 6000;
                }
            }
        }

        this.theProfiler.endStartSection("checkLight");
        par3Chunk.enqueueRelightChecks();
    }

    /**
     * plays random cave ambient sounds and runs updateTick on random blocks within each chunk in the vacinity of a
     * player
     */
    @Override
    protected void tickBlocksAndAmbiance()
    {
        this.func_48461_r();
        int var1 = 0;
        int var2 = 0;
        Iterator var3 = this.activeChunkSet.iterator();

        while (var3.hasNext())
        {
            ChunkCoordIntPair var4 = (ChunkCoordIntPair)var3.next();
            int var5 = var4.chunkXPos * 16;
            int var6 = var4.chunkZPos * 16;
            this.theProfiler.startSection("getChunk");
            Chunk var7 = this.getChunkFromChunkCoords(var4.chunkXPos, var4.chunkZPos);
            this.func_48458_a(var5, var6, var7);
            this.theProfiler.endStartSection("thunder");
            int var8;
            int var9;
            int var10;
            int var11;

            if (this.rand.nextInt(100000) == 0 && this.isRaining() && this.isThundering())
            {
                this.updateLCG = this.updateLCG * 3 + 1013904223;
                var8 = this.updateLCG >> 2;
                var9 = var5 + (var8 & 15);
                var10 = var6 + (var8 >> 8 & 15);
                var11 = this.getPrecipitationHeight(var9, var10);

                if (this.canLightningStrikeAt(var9, var11, var10))
                {
                    this.addWeatherEffect(new EntityLightningBolt(this, (double)var9, (double)var11, (double)var10));
                    this.lastLightningBolt = 2;
                }
            }

            this.theProfiler.endStartSection("iceandsnow");

            if (this.rand.nextInt(16) == 0)
            {
                this.updateLCG = this.updateLCG * 3 + 1013904223;
                var8 = this.updateLCG >> 2;
                var9 = var8 & 15;
                var10 = var8 >> 8 & 15;
                var11 = this.getPrecipitationHeight(var9 + var5, var10 + var6);

                if (this.isBlockFreezableNaturally(var9 + var5, var11 - 1, var10 + var6))
                {
                    this.setBlock(var9 + var5, var11 - 1, var10 + var6, Block.ice.blockID);
                }

                if (this.isRaining() && this.canSnowAt(var9 + var5, var11, var10 + var6))
                {
                    this.setBlock(var9 + var5, var11, var10 + var6, Block.snow.blockID);
                }
            }

            this.theProfiler.endStartSection("tickTiles");
            ExtendedBlockStorage[] var19 = var7.getBlockStorageArray();
            var9 = var19.length;

            for (var10 = 0; var10 < var9; ++var10)
            {
                ExtendedBlockStorage var20 = var19[var10];

                if (var20 != null && var20.getNeedsRandomTick())
                {
                    for (int var12 = 0; var12 < 3; ++var12)
                    {
                        this.updateLCG = this.updateLCG * 3 + 1013904223;
                        int var13 = this.updateLCG >> 2;
                        int var14 = var13 & 15;
                        int var15 = var13 >> 8 & 15;
                        int var16 = var13 >> 16 & 15;
                        int var17 = var20.getExtBlockID(var14, var16, var15);
                        ++var2;
                        Block var18 = Block.blocksList[var17];

                        if (var18 != null && var18.getTickRandomly())
                        {
                            ++var1;
                            var18.updateTick(this, var14 + var5, var16 + var20.getYLocation(), var15 + var6, this.rand);
                        }
                    }
                }
            }

            this.theProfiler.endSection();
        }
    }

    /**
     * checks to see if a given block is both water and is cold enough to freeze
     */
    @Override
    public boolean isBlockFreezable(int par1, int par2, int par3)
    {
        return this.canBlockFreeze(par1, par2, par3, false);
    }

    /**
     * checks to see if a given block is both water and has at least one immediately adjacent non-water block
     */
    @Override
    public boolean isBlockFreezableNaturally(int par1, int par2, int par3)
    {
        return this.canBlockFreeze(par1, par2, par3, true);
    }

    /**
     * checks to see if a given block is both water, and cold enough to freeze - if the par4 boolean is set, this will
     * only return true if there is a non-water block immediately adjacent to the specified block
     */
    @Override
    public boolean canBlockFreeze(int par1, int par2, int par3, boolean par4)
    {
        BiomeGenBase var5 = this.getBiomeGenForCoords(par1, par3);
        float var6 = var5.getFloatTemperature();

        if (var6 > 0.15F)
        {
            return false;
        }
        else
        {
            if (par2 >= 0 && par2 < 256 && this.getSavedLightValue(EnumSkyBlock.Block, par1, par2, par3) < 10)
            {
                int var7 = this.getBlockId(par1, par2, par3);

                if ((var7 == Block.waterStill.blockID || var7 == Block.waterMoving.blockID) && this.getBlockMetadata(par1, par2, par3) == 0)
                {
                    if (!par4)
                    {
                        return true;
                    }

                    boolean var8 = true;

                    if (var8 && this.getBlockMaterial(par1 - 1, par2, par3) != Material.water)
                    {
                        var8 = false;
                    }

                    if (var8 && this.getBlockMaterial(par1 + 1, par2, par3) != Material.water)
                    {
                        var8 = false;
                    }

                    if (var8 && this.getBlockMaterial(par1, par2, par3 - 1) != Material.water)
                    {
                        var8 = false;
                    }

                    if (var8 && this.getBlockMaterial(par1, par2, par3 + 1) != Material.water)
                    {
                        var8 = false;
                    }

                    if (!var8)
                    {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    /**
     * Tests whether or not snow can be placed at a given location
     */
    @Override
    public boolean canSnowAt(int par1, int par2, int par3)
    {
        BiomeGenBase var4 = this.getBiomeGenForCoords(par1, par3);
        float var5 = var4.getFloatTemperature();

        if (var5 > 0.15F)
        {
            return false;
        }
        else
        {
            if (par2 >= 0 && par2 < 256 && this.getSavedLightValue(EnumSkyBlock.Block, par1, par2, par3) < 10)
            {
                int var6 = this.getBlockId(par1, par2 - 1, par3);
                int var7 = this.getBlockId(par1, par2, par3);

                if (var7 == 0 && Block.snow.canPlaceBlockAt(this, par1, par2, par3) && var6 != 0 && var6 != Block.ice.blockID && Block.blocksList[var6].blockMaterial.blocksMovement())
                {
                    return true;
                }
            }

            return false;
        }
    }

    /**
     * Runs through the list of updates to run and ticks them
     */
    @Override
    public boolean tickUpdates(boolean par1)
    {
        int var2 = this.scheduledTickTreeSet.size();

        if (var2 != this.scheduledTickSet.size())
        {
            throw new IllegalStateException("TickNextTick list out of synch");
        }
        else
        {
            if (var2 > 1000)
            {
                var2 = 1000;
            }

            for (int var3 = 0; var3 < var2; ++var3)
            {
                NextTickListEntry var4 = (NextTickListEntry)this.scheduledTickTreeSet.first();

                if (!par1 && var4.scheduledTime > this.worldInfo.getWorldTotalTime())
                {
                    break;
                }

                this.scheduledTickTreeSet.remove(var4);
                this.scheduledTickSet.remove(var4);
                byte var5 = 8;

                if (this.checkChunksExist(var4.xCoord - var5, var4.yCoord - var5, var4.zCoord - var5, var4.xCoord + var5, var4.yCoord + var5, var4.zCoord + var5))
                {
                    int var6 = this.getBlockId(var4.xCoord, var4.yCoord, var4.zCoord);

                    if (var6 == var4.blockID && var6 > 0)
                    {
                        Block.blocksList[var6].updateTick(this, var4.xCoord, var4.yCoord, var4.zCoord, this.rand);
                    }
                }
            }

            return this.scheduledTickTreeSet.size() != 0;
        }
    }

    @Override
    public List getPendingBlockUpdates(Chunk par1Chunk, boolean par2)
    {
        ArrayList var3 = null;
        ChunkCoordIntPair var4 = par1Chunk.getChunkCoordIntPair();
        int var5 = var4.chunkXPos << 4;
        int var6 = var5 + 16;
        int var7 = var4.chunkZPos << 4;
        int var8 = var7 + 16;
        Iterator var9 = this.scheduledTickSet.iterator();

        while (var9.hasNext())
        {
            NextTickListEntry var10 = (NextTickListEntry)var9.next();

            if (var10.xCoord >= var5 && var10.xCoord < var6 && var10.zCoord >= var7 && var10.zCoord < var8)
            {
                if (par2)
                {
                    this.scheduledTickTreeSet.remove(var10);
                    var9.remove();
                }

                if (var3 == null)
                {
                    var3 = new ArrayList();
                }

                var3.add(var10);
            }
        }

        return var3;
    }

    /**
     * Randomly will call the random display update on a 1000 blocks within 16 units of the specified position. Args: x,
     * y, z
     */
    @Override
    public void doVoidFogParticles(int par1, int par2, int par3)
    {
        byte var4 = 16;
        Random var5 = new Random();

        for (int var6 = 0; var6 < 1000; ++var6)
        {
            int var7 = par1 + this.rand.nextInt(var4) - this.rand.nextInt(var4);
            int var8 = par2 + this.rand.nextInt(var4) - this.rand.nextInt(var4);
            int var9 = par3 + this.rand.nextInt(var4) - this.rand.nextInt(var4);
            int var10 = this.getBlockId(var7, var8, var9);

            if (var10 == 0 && this.rand.nextInt(8) > var8 && this.provider.getWorldHasVoidParticles())
            {
                this.spawnParticle("depthsuspend", (double)((float)var7 + this.rand.nextFloat()), (double)((float)var8 + this.rand.nextFloat()), (double)((float)var9 + this.rand.nextFloat()), 0.0D, 0.0D, 0.0D);
            }
            else if (var10 > 0)
            {
                Block.blocksList[var10].randomDisplayTick(this, var7, var8, var9, var5);
            }
        }
    }

    /**
     * Does nothing while unloading 100 oldest chunks
     */
    public void dropOldChunks()
    {
        while (this.chunkProvider.unloadQueuedChunks())
        {
            ;
        }
    }

    public EntityPlayer func_48456_a(double par1, double par3, double par5)
    {
        double var7 = -1.0D;
        EntityPlayer var9 = null;

        for (int var10 = 0; var10 < this.playerEntities.size(); ++var10)
        {
            EntityPlayer var11 = (EntityPlayer)this.playerEntities.get(var10);
            double var12 = var11.getDistanceSq(par1, var11.posY, par3);

            if ((par5 < 0.0D || var12 < par5 * par5) && (var7 == -1.0D || var12 < var7))
            {
                var7 = var12;
                var9 = var11;
            }
        }

        return var9;
    }

    /**
     * If on MP, sends a quitting packet.
     */
    @Override
    public void sendQuittingDisconnectingPacket() {}

    /**
     * Called when checking if a certain block can be mined or not. The 'spawn safe zone' check is located here.
     */
    @Override
    public boolean canMineBlock(EntityPlayer par1EntityPlayer, int par2, int par3, int par4)
    {
        return true;
    }

    /**
     * sends a Packet 38 (Entity Status) to all tracked players of that entity
     */
    @Override
    public void setEntityState(Entity par1Entity, byte par2) {}

    public void updateEntityList()
    {
        this.loadedEntityList.removeAll(this.unloadedEntityList);
        int var1;
        Entity var2;
        int var3;
        int var4;

        for (var1 = 0; var1 < this.unloadedEntityList.size(); ++var1)
        {
            var2 = (Entity)this.unloadedEntityList.get(var1);
            var3 = var2.chunkCoordX;
            var4 = var2.chunkCoordZ;

            if (var2.addedToChunk && this.chunkExists(var3, var4))
            {
                this.getChunkFromChunkCoords(var3, var4).removeEntity(var2);
            }
        }

        for (var1 = 0; var1 < this.unloadedEntityList.size(); ++var1)
        {
            this.onEntityRemoved((Entity)this.unloadedEntityList.get(var1));
        }

        this.unloadedEntityList.clear();

        for (var1 = 0; var1 < this.loadedEntityList.size(); ++var1)
        {
            var2 = (Entity)this.loadedEntityList.get(var1);

            if (var2.ridingEntity != null)
            {
                if (!var2.ridingEntity.isDead && var2.ridingEntity.riddenByEntity == var2)
                {
                    continue;
                }

                var2.ridingEntity.riddenByEntity = null;
                var2.ridingEntity = null;
            }

            if (var2.isDead)
            {
                var3 = var2.chunkCoordX;
                var4 = var2.chunkCoordZ;

                if (var2.addedToChunk && this.chunkExists(var3, var4))
                {
                    this.getChunkFromChunkCoords(var3, var4).removeEntity(var2);
                }

                this.loadedEntityList.remove(var1--);
                this.onEntityRemoved(var2);
            }
        }
    }

    /**
     * Updates the flag that indicates whether or not all players in the world are sleeping.
     */
    @Override
    public void updateAllPlayersSleepingFlag()
    {
        this.allPlayersSleeping = !this.playerEntities.isEmpty();
        Iterator var1 = this.playerEntities.iterator();

        while (var1.hasNext())
        {
            EntityPlayer var2 = (EntityPlayer)var1.next();

            if (!var2.isPlayerSleeping())
            {
                this.allPlayersSleeping = false;
                break;
            }
        }
    }

    /**
     * Wakes up all players in the world.
     */
    protected void wakeUpAllPlayers()
    {
        this.allPlayersSleeping = false;
        Iterator var1 = this.playerEntities.iterator();

        while (var1.hasNext())
        {
            EntityPlayer var2 = (EntityPlayer)var1.next();

            if (var2.isPlayerSleeping())
            {
                var2.wakeUpPlayer(false, false, true);
            }
        }

        this.clearWeather();
    }

    /**
     * Returns whether or not all players in the world are fully asleep.
     */
    public boolean isAllPlayersFullyAsleep()
    {
        if (this.allPlayersSleeping && !this.isRemote)
        {
            Iterator var1 = this.playerEntities.iterator();
            EntityPlayer var2;

            do
            {
                if (!var1.hasNext())
                {
                    return true;
                }

                var2 = (EntityPlayer)var1.next();
            }
            while (var2.isPlayerFullyAsleep());

            return false;
        }
        else
        {
            return false;
        }
    }

    @Override
    public void playSound(double par1, double par3, double par5, String par7Str, float par8, float par9, boolean par10)
    {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.enableSP)
        {
            float var1 = 16F;

            if (par8 > 1.0F)
            {
                var1 *= par8;
            }

            if (mc.renderViewEntity == null)
            {
                return;
            }
            if (mc.renderViewEntity.getDistanceSq(par1, par3, par5) < (double)(var1 * var1))
            {
                mc.sndManager.playSound(par7Str, (float)par1, (float)par3, (float)par5, par8, par9);
            }
            return;
        }
        for (int var2 = 0; var2 < this.worldAccesses.size(); var2++)
        {
            ((IWorldAccess)this.worldAccesses.get(var2)).playSound(par7Str, par1, par3, par5, par8, par9);
        }
    }

    /**
     * Gets a random mob for spawning in this world.
     */
    public SpawnListEntry spawnRandomCreature(EnumCreatureType par1EnumCreatureType, int par2, int par3, int par4)
    {
        List var1 = this.getChunkProvider().getPossibleCreatures(par1EnumCreatureType, par2, par3, par4);

        if (var1 == null || var1.isEmpty())
        {
            return null;
        }
        else
        {
            return (SpawnListEntry)WeightedRandom.getRandomItem(rand, var1);
        }
    }

    /**
     * Invalidates an AABB region of blocks from the receive queue, in the event that the block has been modified
     * client-side in the intervening 80 receive ticks.
     */
    @Override
    public void invalidateBlockReceiveRegion(int i, int j, int k, int l, int i1, int j1) {}

    @Override
    public void doPreChunk(int par1, int par2, boolean par3) {}

    /**
     * Add an ID to Entity mapping to entityHashSet
     */
    @Override
    public void addEntityToWorld(int par1, Entity par2Entity) {}

    /**
     * Lookup and return an Entity based on its ID
     */
    @Override
    public Entity getEntityByID(int par1)
    {
        return null;
    }

    @Override
    public Entity removeEntityFromWorld(int par1)
    {
        return null;
    }

    @Override
    public boolean setBlockAndMetadataAndInvalidate(int par1, int par2, int par3, int par4, int par5)
    {
        return false;
    }

    @Override
    public CrashReportCategory addWorldInfoToCrashReport(CrashReport par1CrashReport)
    {
        CrashReportCategory crashreportcategory = par1CrashReport.makeCategoryDepth("Affected level", 1);
        crashreportcategory.addCrashSection("Level name", this.worldInfo != null ? ((Object)(this.worldInfo.getWorldName())) : "????");
        crashreportcategory.addCrashSection("Chunk stats", this.chunkProvider.makeString());

        try
        {
        	this.worldInfo.addToCrashReport(crashreportcategory);
        }
        catch (Throwable throwable)
        {
            crashreportcategory.addCrashSectionThrowable("Level Data Unobtainable", throwable);
        }

        return crashreportcategory;
    }

//This method is very important! Without it you will be able to place blocks in yourself.
    @Override
    public boolean canPlaceEntityOnSide(int par1, int par2, int par3, int par4, boolean par5, int par6, Entity par7Entity, ItemStack par8ItemStack)
    {
        int var1 = getBlockId(par2, par3, par4);
        Block var2 = Block.blocksList[var1];
        Block var3 = Block.blocksList[par1];
        AxisAlignedBB axisalignedbb = var3.getCollisionBoundingBoxFromPool(this, par2, par3, par4);

        if (par5)
        {
            axisalignedbb = null;
        }

        if (axisalignedbb != null && !checkNoEntityCollision(axisalignedbb))
        {
            return false;
        }

        if (var2 != null && (var2 == Block.waterMoving || var2 == Block.waterStill || var2 == Block.lavaMoving || var2 == Block.lavaStill || var2 == Block.fire || var2.blockMaterial.isReplaceable()))
        {
        	var2 = null;
        }

        return par1 > 0 && var2 == null && var3.canPlaceBlockOnSide(this, par2, par3, par4, par6);
    }

    static
    {
        bonusChestContent = (new WeightedRandomChestContent[]
        {
            new WeightedRandomChestContent(Item.stick.itemID, 0, 1, 3, 10), new WeightedRandomChestContent(Block.planks.blockID, 0, 1, 3, 10), new WeightedRandomChestContent(Block.wood.blockID, 0, 1, 3, 10), new WeightedRandomChestContent(Item.axeStone.itemID, 0, 1, 1, 3), new WeightedRandomChestContent(Item.axeWood.itemID, 0, 1, 1, 5), new WeightedRandomChestContent(Item.pickaxeStone.itemID, 0, 1, 1, 3), new WeightedRandomChestContent(Item.pickaxeWood.itemID, 0, 1, 1, 5), new WeightedRandomChestContent(Item.appleRed.itemID, 0, 2, 3, 5), new WeightedRandomChestContent(Item.bread.itemID, 0, 2, 3, 3)
        });
    }
}
