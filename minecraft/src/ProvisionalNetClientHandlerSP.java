package net.minecraft.src;

import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.util.*;
import org.lwjgl.input.Keyboard;

public class ProvisionalNetClientHandlerSP extends NetClientHandler
{
    public ProvisionalNetClientHandlerSP(Minecraft par1Minecraft)
    {
        super(par1Minecraft);
        this.playerInfoList = new ArrayList();
        this.currentServerMaxPlayers = 1;
    }

    @Override
    public void cleanup() {}

    /**
     * Processes the packets that have been read since the last call to this function.
     */
    @Override
    public void processReadPackets() {}

    @Override
    public void handleLogin(Packet1Login par1Packet1Login) {}

    @Override
    public void handleKickDisconnect(Packet255KickDisconnect par1Packet255KickDisconnect) {}

    @Override
    public void handleErrorMessage(String par1Str, Object par2ArrayOfObj[]) {}

    @Override
    public void disconnect() {}

    @Override
    public void handleCustomPayload(Packet250CustomPayload par1Packet250CustomPayload)
    {
        if ("MC|AdvCdm".equals(par1Packet250CustomPayload.channel)){
            Minecraft mc = Minecraft.getMinecraft();
            if (Minecraft.getMinecraft().theWorld.getWorldInfo().areCommandsAllowed() && mc.thePlayer.capabilities.isCreativeMode)
            {
                try
                {
                    DataInputStream datainputstream3 = new DataInputStream(new ByteArrayInputStream(par1Packet250CustomPayload.data));
                    int j = datainputstream3.readInt();
                    int l = datainputstream3.readInt();
                    int j1 = datainputstream3.readInt();
                    String s1 = Packet.readString(datainputstream3, 256);
                    TileEntity tileentity = mc.theWorld.getBlockTileEntity(j, l, j1);

                    if (tileentity != null && (tileentity instanceof TileEntityCommandBlock))
                    {
                        ((TileEntityCommandBlock)tileentity).setCommand(s1);
                        mc.theWorld.markBlockForUpdate(j, l, j1);
                        mc.thePlayer.sendChatToPlayer(ChatMessageComponent.createFromTranslationKey("Command set: "+s1));
                    }
                }
                catch (Exception exception3)
                {
                    exception3.printStackTrace();
                }
            }
            else
            {
                mc.thePlayer.sendChatToPlayer(ChatMessageComponent.createFromTranslationKey("advMode.notAllowed"));
            }
            return;
        }
    }

    /**
     * determine if it is a server handler
     */
    @Override
    public boolean isServerHandler()
    {
        return false;
    }

    @Override
    public INetworkManager getNetManager()
    {
        return null;
    }

    /**
     * Adds the packet to the send queue
     */
    @Override
    public void addToSendQueue(Packet par1Packet)
    {
        par1Packet.processPacket(this);
    }

    @Override
    public void handleAnimation(Packet18Animation par1Packet18Animation) {}

    @Override
    public void func_142031_a(Packet133TileEditorOpen par1Packet133TileEditorOpen) {}

    @Override
    public void func_110773_a(Packet44UpdateAttributes par1Packet44UpdateAttributes) {}

    @Override
    public void func_110774_a(Packet27PlayerInput par1Packet27PlayerInput) {}

    @Override
    public void handleFlying(Packet10Flying par1Packet10Flying) {}

    @Override
    public void handleClientCommand(Packet205ClientCommand packet205clientcommand)
    {
        if (packet205clientcommand.forceRespawn == 1)
        {
            Minecraft mc = Minecraft.getMinecraft();
            mc.displayGuiScreen(null);
            mc.respawn(mc.theWorld.isRemote, 0, true);
        }
    }
}
