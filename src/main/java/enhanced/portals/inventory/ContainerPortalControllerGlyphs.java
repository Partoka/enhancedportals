package enhanced.portals.inventory;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.nbt.NBTTagCompound;
import cpw.mods.fml.common.FMLCommonHandler;
import enhanced.base.client.gui.BaseGui;
import enhanced.base.inventory.BaseContainer;
import enhanced.portals.EnhancedPortals;
import enhanced.portals.client.gui.GuiPortalControllerGlyphs;
import enhanced.portals.network.GuiHandler;
import enhanced.portals.network.packet.PacketGuiData;
import enhanced.portals.portal.GlyphIdentifier;
import enhanced.portals.portal.PortalException;
import enhanced.portals.tile.TileController;

public class ContainerPortalControllerGlyphs extends BaseContainer {
    TileController controller;

    public ContainerPortalControllerGlyphs(TileController c, InventoryPlayer p) {
        super(null, p, GuiPortalControllerGlyphs.CONTAINER_SIZE + BaseGui.bufferSpace + BaseGui.playerInventorySize);
        controller = c;
        hideInventorySlots();
    }

    @Override
    public void handleGuiPacket(NBTTagCompound tag, EntityPlayer player) {
        if (tag.hasKey("uid"))
            try {
                controller.setIdentifierUnique(new GlyphIdentifier(tag.getString("uid")));
                player.openGui(EnhancedPortals.instance, GuiHandler.PORTAL_CONTROLLER_A, controller.getWorldObj(), controller.xCoord, controller.yCoord, controller.zCoord);
            } catch (PortalException e) {
                NBTTagCompound errorTag = new NBTTagCompound();
                errorTag.setInteger("error", 0);
                EnhancedPortals.instance.packetPipeline.sendTo(new PacketGuiData(errorTag), (EntityPlayerMP) player);
            }
        else if (tag.hasKey("error") && FMLCommonHandler.instance().getEffectiveSide().isClient())
            ((GuiPortalControllerGlyphs) Minecraft.getMinecraft().currentScreen).setWarningMessage();
    }
}