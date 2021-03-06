package enhanced.portals.portal.dial;

import java.util.Arrays;

import org.lwjgl.input.Keyboard;

import enhanced.base.network.packet.PacketGuiData;
import enhanced.base.utilities.Localisation;
import enhanced.portals.EnhancedPortals;
import enhanced.portals.Reference.EPGuis;
import enhanced.portals.Reference.EPMod;
import enhanced.portals.Reference.Locale;
import enhanced.portals.network.ProxyClient;
import enhanced.portals.network.packet.PacketRequestGui;
import enhanced.portals.portal.GlyphIdentifier;
import enhanced.portals.portal.PortalTextureManager;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

public class GuiDialingEdit extends GuiDialingAdd {
    boolean receivedData = false;

    public GuiDialingEdit(TileDialingDevice d, EntityPlayer p) {
        super(new ContainerDialingEdit(d, p.inventory), CONTAINER_SIZE);
        dial = d;
        name = Localisation.get(EPMod.ID, Locale.GUI_DIALLING_DEVICE);
        setHidePlayerInventory();
        allowUserInput = true;
        Keyboard.enableRepeatEvents(true);

        if (ProxyClient.saveTexture == null)
            ProxyClient.saveTexture = new PortalTextureManager();
    }

    @Override
    public void initGui() {
        if (ProxyClient.saveName == null) {
            ProxyClient.saveName = "";
            ProxyClient.saveGlyph = new GlyphIdentifier();
            ProxyClient.saveTexture = new PortalTextureManager();
        } else
            receivedData = true;

        super.initGui();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (receivedData) {
            super.mouseClicked(mouseX, mouseY, mouseButton);

            if (mouseX >= guiLeft + 7 && mouseX <= guiLeft + 168 && mouseY >= guiTop + 52 && mouseY < guiTop + 70) {
                isEditing = true;
                EnhancedPortals.instance.packetPipeline.sendToServer(new PacketRequestGui(dial, EPGuis.DIALING_DEVICE_E));
            }
        }
    }

    @Override
    protected void keyTyped(char par1, int par2) {
        if (receivedData)
            super.keyTyped(par1, par2);
        else if (par2 == 1 || par2 == mc.gameSettings.keyBindInventory.getKeyCode())
            mc.thePlayer.closeScreen();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int par1, int par2) {
        super.drawGuiContainerForegroundLayer(par1, par2);

        if (!receivedData) // Just in case the users connection is very slow
        {
            drawRect(0, 0, xSize, ySize, 0xCC000000);
            String s = Localisation.get(EPMod.ID, Locale.GUI_WAITING_FOR_DATA_FROM_SERVER);
            getFontRenderer().drawSplitString(s, xSize / 2 - getFontRenderer().getStringWidth(s) / 2, ySize / 2 - getFontRenderer().FONT_HEIGHT / 2, xSize, 0xFF0000);
        }

        if (par1 >= guiLeft + 7 && par1 <= guiLeft + 168 && par2 >= guiTop + 52 && par2 < guiTop + 70)
            drawHoveringText(Arrays.asList(new String[] { Localisation.get(EPMod.ID, Locale.GUI_CLICK_TO_MODIFY) }), par1 - guiLeft, par2 - guiTop, getFontRenderer());
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 0)
            EnhancedPortals.instance.packetPipeline.sendToServer(new PacketRequestGui(dial, EPGuis.DIALING_DEVICE_A));
        else if (button.id == 1) // save
        {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setInteger("id", ProxyClient.editingID);
            tag.setString("name", text.getText());
            tag.setString("uid", ProxyClient.saveGlyph.getGlyphString());
            ProxyClient.saveTexture.writeToNBT(tag, "texture");
            EnhancedPortals.instance.packetPipeline.sendToServer(new PacketGuiData(tag));
        } else if (button.id == 100) {
            isEditing = true;
            EnhancedPortals.instance.packetPipeline.sendToServer(new PacketRequestGui(dial, EPGuis.TEXTURE_DIALING_EDIT_A));
        } else if (button.id == 101) {
            isEditing = true;
            EnhancedPortals.instance.packetPipeline.sendToServer(new PacketRequestGui(dial, EPGuis.TEXTURE_DIALING_EDIT_B));
        } else if (button.id == 102) {
            isEditing = true;
            EnhancedPortals.instance.packetPipeline.sendToServer(new PacketRequestGui(dial, EPGuis.TEXTURE_DIALING_EDIT_C));
        }
    }

    public void receivedData() {
        receivedData = true;
        text.setText(ProxyClient.saveName);
        display.setIdentifier(ProxyClient.saveGlyph);
    }
}
