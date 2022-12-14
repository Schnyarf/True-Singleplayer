package net.minecraft.src;

import java.io.IOException;
import java.util.List;

public class ChunkProviderLoadOrGenerate implements IChunkProvider
{
    /**
     * A completely empty Chunk, used by ChunkProviderLoadOrGenerate when there's no ChunkProvider.
     */
    private Chunk blankChunk;

    /** The parent IChunkProvider for this ChunkProviderLoadOrGenerate. */
    private IChunkProvider chunkProvider;

    /** The IChunkLoader used by this ChunkProviderLoadOrGenerate. */
    private IChunkLoader chunkLoader;

    /** An array of 1024 chunks. */
    private Chunk[] chunks;

    /** Reference to the World object. */
    private World worldObj;

    /** The last X position of a chunk that was returned from setRecordPlayingMessage */
    int lastQueriedChunkXPos;

    /** The last Z position of a chunk that was returned from setRecordPlayingMessage */
    int lastQueriedChunkZPosition;

    /** The last Chunk that was returned from setRecordPlayingMessage */
    private Chunk lastQueriedChunk;

    /** The current chunk the player is over */
    private int curChunkX;

    /** The current chunk the player is over */
    private int curChunkY;

    /**
     * This is the chunk that the player is currently standing over. Args: chunkX, chunkZ
     */
    public void setCurrentChunkOver(int par1, int par2)
    {
        this.curChunkX = par1;
        this.curChunkY = par2;
    }

    /**
     * Checks if the chunk coordinate could actually be stored within the chunk cache. Args: chunkX, chunkZ
     */
    public boolean canChunkExist(int par1, int par2)
    {
        byte var3 = 15;
        return par1 >= this.curChunkX - var3 && par2 >= this.curChunkY - var3 && par1 <= this.curChunkX + var3 && par2 <= this.curChunkY + var3;
    }

    /**
     * Checks to see if a chunk exists at x, y
     */
    @Override
    public boolean chunkExists(int par1, int par2)
    {
        if (!this.canChunkExist(par1, par2))
        {
            return false;
        }
        else if (par1 == this.lastQueriedChunkXPos && par2 == this.lastQueriedChunkZPosition && this.lastQueriedChunk != null)
        {
            return true;
        }
        else
        {
            int var3 = par1 & 31;
            int var4 = par2 & 31;
            int var5 = var3 + var4 * 32;
            return this.chunks[var5] != null && (this.chunks[var5] == this.blankChunk || this.chunks[var5].isAtLocation(par1, par2));
        }
    }

    /**
     * loads or generates the chunk at the chunk location specified
     */
    @Override
    public Chunk loadChunk(int par1, int par2)
    {
        return this.provideChunk(par1, par2);
    }

    /**
     * Will return back a chunk, if it doesn't exist and its not a MP client it will generates all the blocks for the
     * specified chunk from the map seed and chunk seed
     */
    @Override
    public Chunk provideChunk(int par1, int par2)
    {
        if (par1 == this.lastQueriedChunkXPos && par2 == this.lastQueriedChunkZPosition && this.lastQueriedChunk != null)
        {
            return this.lastQueriedChunk;
        }
        else if (!this.worldObj.findingSpawnPoint && !this.canChunkExist(par1, par2))
        {
            return this.blankChunk;
        }
        else
        {
            int var3 = par1 & 31;
            int var4 = par2 & 31;
            int var5 = var3 + var4 * 32;

            if (!this.chunkExists(par1, par2))
            {
                if (this.chunks[var5] != null)
                {
                    this.chunks[var5].onChunkUnload();
                    this.saveChunk(this.chunks[var5]);
                    this.saveExtraChunkData(this.chunks[var5]);
                }

                Chunk var6 = this.func_542_c(par1, par2);

                if (var6 == null)
                {
                    if (this.chunkProvider == null)
                    {
                        var6 = this.blankChunk;
                    }
                    else
                    {
                        var6 = this.chunkProvider.provideChunk(par1, par2);
                    }
                }

                this.chunks[var5] = var6;

                if (this.chunks[var5] != null)
                {
                    this.chunks[var5].onChunkLoad();
                }

                this.chunks[var5].populateChunk(this, this, par1, par2);
            }

            this.lastQueriedChunkXPos = par1;
            this.lastQueriedChunkZPosition = par2;
            this.lastQueriedChunk = this.chunks[var5];
            return this.chunks[var5];
        }
    }

    private Chunk func_542_c(int par1, int par2)
    {
        if (this.chunkLoader == null)
        {
            return this.blankChunk;
        }
        else
        {
            try
            {
                Chunk var3 = this.chunkLoader.loadChunk(this.worldObj, par1, par2);

                if (var3 != null)
                {
                    var3.lastSaveTime = this.worldObj.getTotalWorldTime();

                    if (this.chunkProvider != null)
                    {
                    	this.chunkProvider.recreateStructures(par1, par2);
                    }
                }

                return var3;
            }
            catch (Exception var4)
            {
                var4.printStackTrace();
                return this.blankChunk;
            }
        }
    }

    /**
     * Save extra data associated with this Chunk not normally saved during autosave, only during chunk unload.
     * Currently unused.
     */
    private void saveExtraChunkData(Chunk par1Chunk)
    {
        if (this.chunkLoader != null)
        {
            try
            {
                this.chunkLoader.saveExtraChunkData(this.worldObj, par1Chunk);
            }
            catch (Exception var3)
            {
                var3.printStackTrace();
            }
        }
    }

    /**
     * Save a given Chunk, recording the time in lastSaveTime
     */
    private void saveChunk(Chunk par1Chunk)
    {
        if (this.chunkLoader != null)
        {
            try
            {
                par1Chunk.lastSaveTime = this.worldObj.getTotalWorldTime();
                this.chunkLoader.saveChunk(this.worldObj, par1Chunk);
            }
            catch (Exception var3)
            {
                var3.printStackTrace();
            }
        }
    }

    /**
     * Populates chunk with ores etc etc
     */
    @Override
    public void populate(IChunkProvider par1IChunkProvider, int par2, int par3)
    {
        Chunk var4 = this.provideChunk(par2, par3);

        if (!var4.isTerrainPopulated)
        {
            var4.isTerrainPopulated = true;

            if (this.chunkProvider != null)
            {
                this.chunkProvider.populate(par1IChunkProvider, par2, par3);
                var4.setChunkModified();
            }
        }
    }

    /**
     * Two modes of operation: if passed true, save all Chunks in one go.  If passed false, save up to two chunks.
     * Return true if all chunks have been saved.
     */
    @Override
    public boolean saveChunks(boolean par1, IProgressUpdate par2IProgressUpdate)
    {
        int var3 = 0;
        int var4 = 0;
        int var7;

        if (par2IProgressUpdate != null)
        {
            for (var7 = 0; var7 < this.chunks.length; ++var7)
            {
                if (this.chunks[var7] != null && chunks[var7].needsSaving(par1))
                {
                    ++var4;
                }
            }
        }

        int var10 = 0;
        for (int var12 = 0; var12 < this.chunks.length; ++var12)
        {
            if (this.chunks[var12] != null)
            {
                if (par1)
                {
                    this.saveExtraChunkData(this.chunks[var12]);
                }

                if (this.chunks[var12].needsSaving(par1))
                {
                    this.saveChunk(this.chunks[var12]);
                    this.chunks[var12].isModified = false;
                    ++var3;

                    if (var3 == 2 && !par1)
                    {
                        return false;
                    }

                    if (par2IProgressUpdate != null)
                    {
                        ++var10;

                        if (var10 % 10 == 0)
                        {
                            par2IProgressUpdate.setLoadingProgress(var10 * 100 / var4);
                        }
                    }
                }
            }
        }

        if (par1)
        {
            if (this.chunkLoader == null)
            {
                return true;
            }

            this.chunkLoader.saveExtraData();
        }

        return true;
    }

    /**
     * Unloads the 100 oldest chunks from memory, due to a bug with chunkSet.add() never being called it thinks the list
     * is always empty and will not remove any chunks.
     */
    @Override
    public boolean unloadQueuedChunks()
    {
        if (this.chunkLoader != null)
        {
            this.chunkLoader.chunkTick();
        }

        return this.chunkProvider.unloadQueuedChunks();
    }

    /**
     * Returns if the IChunkProvider supports saving.
     */
    @Override
    public boolean canSave()
    {
        return true;
    }

    /**
     * Converts the instance data to a readable string.
     */
    @Override
    public String makeString()
    {
        return "ChunkCache: " + this.chunks.length;
    }

    /**
     * Returns a list of creatures of the specified type that can spawn at the given location.
     */
    @Override
    public List getPossibleCreatures(EnumCreatureType par1EnumCreatureType, int par2, int par3, int par4)
    {
        return this.chunkProvider.getPossibleCreatures(par1EnumCreatureType, par2, par3, par4);
    }

    /**
     * Returns the location of the closest structure of the specified type. If not found returns null.
     */
    @Override
    public ChunkPosition findClosestStructure(World par1World, String par2Str, int par3, int par4, int par5)
    {
        return this.chunkProvider.findClosestStructure(par1World, par2Str, par3, par4, par5);
    }

    @Override
    public int getLoadedChunkCount()
    {
        return 0;
    }

    @Override
    public void recreateStructures(int par1, int par2)
    {
    }

    @Override
    public void saveExtraData()
    {
    }
}
