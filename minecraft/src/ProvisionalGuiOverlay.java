package net.minecraft.src;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class ProvisionalGuiOverlay
{
    private static List<ProvisionalGuiOverlay> list;
    private Class guiClass;

    public ProvisionalGuiOverlay(Class c)
    {
        this.guiClass = c;
        this.list.add(this);
    }

    public void unregisterOverlay()
    {
    	this.list.remove(this);
    }

    public static void addOverlays(GuiScreen gui)
    {
        for (ProvisionalGuiOverlay overlay : list)
        {
            if (overlay.guiClass.isInstance(gui) && overlay.shouldBeAdded())
            {
                gui.overlays.add(overlay);
                overlay.onAdded(gui);
            }
        }
    }

    public boolean shouldBeAdded()
    {
        return true;
    }

    public void onAdded(GuiScreen gui) {}

    public void initGui(List buttonList, int width, int height) {}

    public boolean actionPerformed(GuiScreen gui, GuiButton button)
    {
        return true;
    }

    protected void moveButton(List buttonList, int i, int x, int y)
    {
        GuiButton b = this.getButtonById(buttonList, i);
        b.xPosition = x;
        b.yPosition = y;
    }

    protected void hideButton(List buttonList, int i, boolean show)
    {
    	this.getButtonById(buttonList, i).drawButton = show;
    }

    protected void disableButton(List buttonList, int i, boolean enable)
    {
    	this.getButtonById(buttonList, i).enabled = enable;
    }

    protected void resizeButton(List buttonList, int i, int x, int y)
    {
        GuiButton b = this.getButtonById(buttonList, i);
        try
        {
            Field width = (GuiButton.class).getDeclaredFields()[1];
            Field height = (GuiButton.class).getDeclaredFields()[2];
            width.setAccessible(true);
            height.setAccessible(true);
            width.set(b, x);
            height.set(b, y);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    protected void renameButton(List buttonList, int i, String newName)
    {
    	this.getButtonById(buttonList, i).displayString = newName;
    }

    private GuiButton getButtonById(List buttonList, int i)
    {
        for (Object o : buttonList)
        {
            GuiButton b = (GuiButton)o;
            if (b.id == i)
            {
                return b;
            }
        }
        return null;
    }

    public boolean preDrawScreen(GuiScreen gui, int x, int y, float f)
    {
        return true;
    }

    public void postDrawScreen(GuiScreen gui, int x, int y, float f) {}

    public boolean preUpdateScreen(GuiScreen gui)
    {
        return true;
    }

    public String applyStringOverrides(String str, int x, int y, int width, int height)
    {
        return str;
    }

    public void postUpdateScreen(GuiScreen gui) {}

    static
    {
    	list = new ArrayList<ProvisionalGuiOverlay>();
    }
}