package net.minecraft.src;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

public final class SpawnerAnimals
{
    /** The 17x17 area around the player where mobs can spawn */
    private HashMap eligibleChunksForSpawning = new HashMap();
    private static Map classToStringMapping;

    /**
     * Given a chunk, find a random position in it.
     */
    protected static ChunkPosition getRandomSpawningPointInChunk(World par0World, int par1, int par2)
    {
        Chunk var3 = par0World.getChunkFromChunkCoords(par1, par2);
        int var4 = par1 * 16 + par0World.rand.nextInt(16);
        int var5 = par2 * 16 + par0World.rand.nextInt(16);
        int var6 = par0World.rand.nextInt(var3 == null ? par0World.getActualHeight() : var3.getTopFilledSegment() + 16 - 1);
        return new ChunkPosition(var4, var6, var5);
    }

    /**
     * adds all chunks within the spawn radius of the players to eligibleChunksForSpawning. pars: the world,
     * hostileCreatures, passiveCreatures. returns number of eligible chunks.
     */
    public int findChunksForSpawning(WorldServer par1WorldServer, boolean par2, boolean par3, boolean par4)
    {
        if (!par2 && !par3)
        {
            return 0;
        }
        else
        {
            this.eligibleChunksForSpawning.clear();
            int var5;
            int var8;

            for (var5 = 0; var5 < par1WorldServer.playerEntities.size(); ++var5)
            {
                EntityPlayer var6 = (EntityPlayer)par1WorldServer.playerEntities.get(var5);
                int var7 = MathHelper.floor_double(var6.posX / 16.0D);
                var8 = MathHelper.floor_double(var6.posZ / 16.0D);
                byte var9 = 8;

                for (int var10 = -var9; var10 <= var9; ++var10)
                {
                    for (int var11 = -var9; var11 <= var9; ++var11)
                    {
                        boolean var12 = var10 == -var9 || var10 == var9 || var11 == -var9 || var11 == var9;
                        ChunkCoordIntPair var13 = new ChunkCoordIntPair(var10 + var7, var11 + var8);

                        if (!var12)
                        {
                            this.eligibleChunksForSpawning.put(var13, Boolean.valueOf(false));
                        }
                        else if (!this.eligibleChunksForSpawning.containsKey(var13))
                        {
                            this.eligibleChunksForSpawning.put(var13, Boolean.valueOf(true));
                        }
                    }
                }
            }

            var5 = 0;
            ChunkCoordinates var34 = par1WorldServer.getSpawnPoint();
            EnumCreatureType[] var35 = EnumCreatureType.values();
            var8 = var35.length;

            for (int var36 = 0; var36 < var8; ++var36)
            {
                EnumCreatureType var37 = var35[var36];

                if ((!var37.getPeacefulCreature() || par3) && (var37.getPeacefulCreature() || par2) && (!var37.getAnimal() || par4) && par1WorldServer.countEntities(var37.getCreatureClass()) <= var37.getMaxNumberOfCreature() * this.eligibleChunksForSpawning.size() / 256)
                {
                    Iterator var39 = this.eligibleChunksForSpawning.keySet().iterator();
                    label110:

                    while (var39.hasNext())
                    {
                        ChunkCoordIntPair var38 = (ChunkCoordIntPair)var39.next();

                        if (!((Boolean)this.eligibleChunksForSpawning.get(var38)).booleanValue())
                        {
                            ChunkPosition var40 = getRandomSpawningPointInChunk(par1WorldServer, var38.chunkXPos, var38.chunkZPos);
                            int var14 = var40.x;
                            int var15 = var40.y;
                            int var16 = var40.z;

                            if (!par1WorldServer.isBlockNormalCube(var14, var15, var16) && par1WorldServer.getBlockMaterial(var14, var15, var16) == var37.getCreatureMaterial())
                            {
                                int var17 = 0;
                                int var18 = 0;

                                while (var18 < 3)
                                {
                                    int var19 = var14;
                                    int var20 = var15;
                                    int var21 = var16;
                                    byte var22 = 6;
                                    SpawnListEntry var23 = null;
                                    EntityLivingData var24 = null;
                                    int var25 = 0;

                                    while (true)
                                    {
                                        if (var25 < 4)
                                        {
                                            label103:
                                            {
                                                var19 += par1WorldServer.rand.nextInt(var22) - par1WorldServer.rand.nextInt(var22);
                                                var20 += par1WorldServer.rand.nextInt(1) - par1WorldServer.rand.nextInt(1);
                                                var21 += par1WorldServer.rand.nextInt(var22) - par1WorldServer.rand.nextInt(var22);

                                                if (canCreatureTypeSpawnAtLocation(var37, par1WorldServer, var19, var20, var21))
                                                {
                                                    float var26 = (float)var19 + 0.5F;
                                                    float var27 = (float)var20;
                                                    float var28 = (float)var21 + 0.5F;

                                                    if (par1WorldServer.getClosestPlayer((double)var26, (double)var27, (double)var28, 24.0D) == null)
                                                    {
                                                        float var29 = var26 - (float)var34.posX;
                                                        float var30 = var27 - (float)var34.posY;
                                                        float var31 = var28 - (float)var34.posZ;
                                                        float var32 = var29 * var29 + var30 * var30 + var31 * var31;

                                                        if (var32 >= 576.0F)
                                                        {
                                                            if (var23 == null)
                                                            {
                                                                var23 = par1WorldServer.spawnRandomCreature(var37, var19, var20, var21);

                                                                if (var23 == null)
                                                                {
                                                                    break label103;
                                                                }
                                                            }

                                                            EntityLiving var41;

                                                            try
                                                            {
                                                                var41 = (EntityLiving)var23.entityClass.getConstructor(new Class[] {World.class}).newInstance(new Object[] {par1WorldServer});
                                                            }
                                                            catch (Exception var33)
                                                            {
                                                                var33.printStackTrace();
                                                                return var5;
                                                            }

                                                            var41.setLocationAndAngles((double)var26, (double)var27, (double)var28, par1WorldServer.rand.nextFloat() * 360.0F, 0.0F);

                                                            if (var41.getCanSpawnHere())
                                                            {
                                                                ++var17;
                                                                par1WorldServer.spawnEntityInWorld(var41);
                                                                var24 = var41.onSpawnWithEgg(var24);

                                                                if (var17 >= var41.getMaxSpawnedInChunk())
                                                                {
                                                                    continue label110;
                                                                }
                                                            }

                                                            var5 += var17;
                                                        }
                                                    }
                                                }

                                                ++var25;
                                                continue;
                                            }
                                        }

                                        ++var18;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            return var5;
        }
    }

    public int performSpawningSP(ProvisionalWorldSSP par0WorldServer, boolean par1, boolean par2)
    {
        if (!par1 && !par2)
        {
            return 0;
        }
        else
        {
            this.eligibleChunksForSpawning.clear();
            int var3;
            int var6;

            for (var3 = 0; var3 < par0WorldServer.playerEntities.size(); ++var3)
            {
                EntityPlayer var4 = (EntityPlayer)par0WorldServer.playerEntities.get(var3);
                int var5 = MathHelper.floor_double(var4.posX / 16.0D);
                var6 = MathHelper.floor_double(var4.posZ / 16.0D);
                byte var7 = 8;

                for (int var8 = -var7; var8 <= var7; ++var8)
                {
                    for (int var9 = -var7; var9 <= var7; ++var9)
                    {
                        boolean var10 = var8 == -var7 || var8 == var7 || var9 == -var7 || var9 == var7;
                        ChunkCoordIntPair var11 = new ChunkCoordIntPair(var8 + var5, var9 + var6);

                        if (!var10)
                        {
                        	this.eligibleChunksForSpawning.put(var11, Boolean.valueOf(false));
                        }
                        else if (!this.eligibleChunksForSpawning.containsKey(var11))
                        {
                        	this.eligibleChunksForSpawning.put(var11, Boolean.valueOf(true));
                        }
                    }
                }
            }

            var3 = 0;
            ChunkCoordinates var31 = par0WorldServer.getSpawnPoint();
            EnumCreatureType[] var32 = EnumCreatureType.values();
            var6 = var32.length;

            for (int var33 = 0; var33 < var6; ++var33)
            {
                EnumCreatureType var34 = var32[var33];

                if ((!var34.getPeacefulCreature() || par2) && (var34.getPeacefulCreature() || par1) && par0WorldServer.countEntities(var34.getCreatureClass()) <= var34.getMaxNumberOfCreature() * this.eligibleChunksForSpawning.size() / 256)
                {
                    Iterator var35 = this.eligibleChunksForSpawning.keySet().iterator();
                    label108:

                    while (var35.hasNext())
                    {
                        ChunkCoordIntPair var37 = (ChunkCoordIntPair)var35.next();

                        if (!((Boolean)this.eligibleChunksForSpawning.get(var37)).booleanValue())
                        {
                            ChunkPosition var36 = getRandomSpawningPointInChunk(par0WorldServer, var37.chunkXPos, var37.chunkZPos);
                            int var12 = var36.x;
                            int var13 = var36.y;
                            int var14 = var36.z;

                            if (!par0WorldServer.isBlockNormalCube(var12, var13, var14) && par0WorldServer.getBlockMaterial(var12, var13, var14) == var34.getCreatureMaterial())
                            {
                                int var15 = 0;
                                int var16 = 0;

                                while (var16 < 3)
                                {
                                    int var17 = var12;
                                    int var18 = var13;
                                    int var19 = var14;
                                    byte var20 = 6;
                                    SpawnListEntry var21 = null;
                                    EntityLivingData entitylivingdata = null;
                                    int var22 = 0;

                                    while (true)
                                    {
                                        if (var22 < 4)
                                        {
                                            label101:
                                            {
                                                var17 += par0WorldServer.rand.nextInt(var20) - par0WorldServer.rand.nextInt(var20);
                                                var18 += par0WorldServer.rand.nextInt(1) - par0WorldServer.rand.nextInt(1);
                                                var19 += par0WorldServer.rand.nextInt(var20) - par0WorldServer.rand.nextInt(var20);

                                                if (canCreatureTypeSpawnAtLocation(var34, par0WorldServer, var17, var18, var19))
                                                {
                                                    float var23 = (float)var17 + 0.5F;
                                                    float var24 = (float)var18;
                                                    float var25 = (float)var19 + 0.5F;

                                                    if (par0WorldServer.getClosestPlayer((double)var23, (double)var24, (double)var25, 24.0D) == null)
                                                    {
                                                        float var26 = var23 - (float)var31.posX;
                                                        float var27 = var24 - (float)var31.posY;
                                                        float var28 = var25 - (float)var31.posZ;
                                                        float var29 = var26 * var26 + var27 * var27 + var28 * var28;

                                                        if (var29 >= 576.0F)
                                                        {
                                                            if (var21 == null)
                                                            {
                                                                var21 = spawnRandomCreature(par0WorldServer, var34, var17, var18, var19);

                                                                if (var21 == null)
                                                                {
                                                                    break label101;
                                                                }
                                                            }

                                                            EntityLiving var38;

                                                            try
                                                            {
                                                                var38 = (EntityLiving)var21.entityClass.getConstructor(new Class[] {World.class}).newInstance(new Object[] {par0WorldServer});
                                                            }
                                                            catch (Exception var30)
                                                            {
                                                                var30.printStackTrace();
                                                                return var3;
                                                            }

                                                            var38.setLocationAndAngles((double)var23, (double)var24, (double)var25, par0WorldServer.rand.nextFloat() * 360.0F, 0.0F);

//                                                             if(!var38.allow(par0WorldServer.provider.dimensionId))
//                                                             {
//                                                                 continue;
//                                                             }
                                                            if (var38.getCanSpawnHere())
                                                            {
                                                                ++var15;
                                                                par0WorldServer.spawnEntityInWorld(var38);
                                                                entitylivingdata = var38.onSpawnWithEgg(entitylivingdata);

                                                                if (var15 >= var38.getMaxSpawnedInChunk())
                                                                {
                                                                    continue label108;
                                                                }
                                                            }

                                                            var3 += var15;
                                                        }
                                                    }
                                                }

                                                ++var22;
                                                continue;
                                            }
                                        }

                                        ++var16;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            return var3;
        }
    }

    /**
     * Returns whether or not the specified creature type can spawn at the specified location.
     */
    public static boolean canCreatureTypeSpawnAtLocation(EnumCreatureType par0EnumCreatureType, World par1World, int par2, int par3, int par4)
    {
        if (par0EnumCreatureType.getCreatureMaterial() == Material.water)
        {
            return par1World.getBlockMaterial(par2, par3, par4).isLiquid() && par1World.getBlockMaterial(par2, par3 - 1, par4).isLiquid() && !par1World.isBlockNormalCube(par2, par3 + 1, par4);
        }
        else if (!par1World.doesBlockHaveSolidTopSurface(par2, par3 - 1, par4))
        {
            return false;
        }
        else
        {
            int var5 = par1World.getBlockId(par2, par3 - 1, par4);
            return var5 != Block.bedrock.blockID && !par1World.isBlockNormalCube(par2, par3, par4) && !par1World.getBlockMaterial(par2, par3, par4).isLiquid() && !par1World.isBlockNormalCube(par2, par3 + 1, par4);
        }
    }

    /**
     * Called during chunk generation to spawn initial creatures.
     */
    public static void performWorldGenSpawning(World par0World, BiomeGenBase par1BiomeGenBase, int par2, int par3, int par4, int par5, Random par6Random)
    {
        List var7 = par1BiomeGenBase.getSpawnableList(EnumCreatureType.creature);

        if (!var7.isEmpty())
        {
            while (par6Random.nextFloat() < par1BiomeGenBase.getSpawningChance())
            {
                SpawnListEntry var8 = (SpawnListEntry)WeightedRandom.getRandomItem(par0World.rand, var7);
                EntityLivingData var9 = null;
                int var10 = var8.minGroupCount + par6Random.nextInt(1 + var8.maxGroupCount - var8.minGroupCount);
                int var11 = par2 + par6Random.nextInt(par4);
                int var12 = par3 + par6Random.nextInt(par5);
                int var13 = var11;
                int var14 = var12;

                for (int var15 = 0; var15 < var10; ++var15)
                {
                    boolean var16 = false;

                    for (int var17 = 0; !var16 && var17 < 4; ++var17)
                    {
                        int var18 = par0World.getTopSolidOrLiquidBlock(var11, var12);

                        if (canCreatureTypeSpawnAtLocation(EnumCreatureType.creature, par0World, var11, var18, var12))
                        {
                            float var19 = (float)var11 + 0.5F;
                            float var20 = (float)var18;
                            float var21 = (float)var12 + 0.5F;
                            EntityLiving var22;

                            try
                            {
                                var22 = (EntityLiving)var8.entityClass.getConstructor(new Class[] {World.class}).newInstance(new Object[] {par0World});
                            }
                            catch (Exception var24)
                            {
                                var24.printStackTrace();
                                continue;
                            }

                            var22.setLocationAndAngles((double)var19, (double)var20, (double)var21, par6Random.nextFloat() * 360.0F, 0.0F);
                            par0World.spawnEntityInWorld(var22);
                            var9 = var22.onSpawnWithEgg(var9);
                            var16 = true;
                        }

                        var11 += par6Random.nextInt(5) - par6Random.nextInt(5);

                        for (var12 += par6Random.nextInt(5) - par6Random.nextInt(5); var11 < par2 || var11 >= par2 + par4 || var12 < par3 || var12 >= par3 + par4; var12 = var14 + par6Random.nextInt(5) - par6Random.nextInt(5))
                        {
                            var11 = var13 + par6Random.nextInt(5) - par6Random.nextInt(5);
                        }
                    }
                }
            }
        }
    }

    private static SpawnListEntry spawnRandomCreature(World w, EnumCreatureType par1EnumCreatureType, int par2, int par3, int par4)
    {
//         if (w instanceof FakeWorldServer)
//         {
//             return null;
//         } Removed FakeWorldServer code - Schf
        List list2 = w.getChunkProvider().getPossibleCreatures(par1EnumCreatureType, par2, par3, par4);
        List list = new ArrayList();
        for (Object o : list2)
        {
            SpawnListEntry s = (SpawnListEntry)o;
            String str = (String)(classToStringMapping.get(s.entityClass));
            if (EntityLiving.allow(str, w.provider.dimensionId))
            {
                list.add(s);
            }
        }
        if (list == null || list.isEmpty())
        {
            return null;
        }
        return (SpawnListEntry)WeightedRandom.getRandomItem(w.rand, list);
    }

    private static List getSpawnableList(World w, BiomeGenBase par1BiomeGenBase, EnumCreatureType par2EnumCreatureType)
    {
        List list2 = par1BiomeGenBase.getSpawnableList(par2EnumCreatureType);
        List list = new ArrayList();
        for (Object o : list2)
        {
            SpawnListEntry s = (SpawnListEntry)o;
            String str = (String)(classToStringMapping.get(s.entityClass));
            if (EntityLiving.allow(str, w.provider.dimensionId))
            {
                list.add(s);
            }
        }
        return list;
    }

    static
    {
        try
        {
            java.lang.reflect.Field f = (EntityList.class).getDeclaredFields()[1];
            f.setAccessible(true);
            classToStringMapping = (Map)(f.get(null));
        }
        catch(Exception ex)
        {
            System.out.println(ex);
        }
    }
}
