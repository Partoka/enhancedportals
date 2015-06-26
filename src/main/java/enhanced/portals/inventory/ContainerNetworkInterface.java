package enhanced.portals.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.nbt.NBTTagCompound;
import enhanced.base.client.gui.BaseGui;
import enhanced.base.inventory.BaseContainer;
import enhanced.base.network.packet.PacketGuiData;
import enhanced.portals.EnhancedPortals;
import enhanced.portals.client.gui.GuiNetworkInterface;
import enhanced.portals.portal.GlyphIdentifier;
import enhanced.portals.tile.TileController;

public class ContainerNetworkInterface extends BaseContainer {
    TileController controller;
    int connectedPortals = -100;
    String oldGlyphs = "EMPTY";

    public ContainerNetworkInterface(TileController c, InventoryPlayer p) {
        super(null, p, GuiNetworkInterface.CONTAINER_SIZE + BaseGui.bufferSpace + BaseGui.playerInventorySize);
        controller = c;
        hideInventorySlots();
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        int cPortals = controller.getHasIdentifierNetwork() ? EnhancedPortals.proxy.networkManager.getNetworkSize(controller.getIdentifierNetwork()) : -1;
        String glyphs = controller.getIdentifierNetwork() == null ? "" : controller.getIdentifierNetwork().getGlyphString();

        for (int i = 0; i < crafters.size(); i++) {
            ICrafting icrafting = (ICrafting) crafters.get(i);

            if (cPortals != connectedPortals)
                icrafting.sendProgressBarUpdate(this, 0, cPortals);

            if (!glyphs.equals(oldGlyphs)) {
                NBTTagCompound t = new NBTTagCompound();
                t.setString("nid", glyphs);
                EnhancedPortals.instance.packetPipeline.sendTo(new PacketGuiData(t), (EntityPlayerMP) icrafting);
            }
        }

        oldGlyphs = glyphs;
        connectedPortals = cPortals;
    }

    @Override
    public void handleGuiPacket(NBTTagCompound tag, EntityPlayer player) {
        if (tag.hasKey("nid"))
            controller.setNID(new GlyphIdentifier(tag.getString("nid")));
    }

    @Override
    public void updateProgressBar(int id, int val) {
        if (id == 0)
            controller.connectedPortals = val;
    }
}
