package net.minecraft.src;

import java.io.File;
import java.util.List;

public interface ISaveHandler // JAD decomps this as each non-mod function being 'public abstract' while fernflower just doesn't. I'm using fernflower with public abstracts added, hopefully this doesn't cause problems
{
    /**
     * Loads and returns the world info
     */
    public abstract WorldInfo loadWorldInfo();

    /**
     * Checks the session lock to prevent save collisions
     */
    public abstract void checkSessionLock() throws MinecraftException;

    /**
     * Returns the chunk loader with the provided world provider
     */
    public abstract IChunkLoader getChunkLoader(WorldProvider var1);

    /**
     * saves level.dat and backs up the existing one to level.dat_old
     */
    void saveWorldInfoAndPlayer(WorldInfo var1, List var2);

    /**
     * Saves the given World Info with the given NBTTagCompound as the Player.
     */
    public abstract void saveWorldInfoWithPlayer(WorldInfo var1, NBTTagCompound var2);

    /**
     * Saves the passed in world info.
     */
    public abstract void saveWorldInfo(WorldInfo var1);

    /**
     * returns null if no saveHandler is relevent (eg. SMP)
     */
    public abstract IPlayerFileData getSaveHandler();

    /**
     * Called to flush all changes to disk, waiting for them to complete.
     */
    public abstract void flush();

    /**
     * Gets the file location of the given map
     */
    public abstract File getMapFileFromName(String var1);

    /**
     * Returns the name of the directory where world information is saved.
     */
    public abstract String getWorldDirectoryName();
}
