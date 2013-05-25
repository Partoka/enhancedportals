package enhancedportals.tileentity;

import java.util.ArrayList;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumChatFormatting;
import alz.core.lib.WorldLocation;
import enhancedportals.EnhancedPortals;
import enhancedportals.lib.BlockIds;
import enhancedportals.lib.Localization;
import enhancedportals.portal.network.DialDeviceNetworkObject;

public class TileEntityDialDevice extends TileEntityEnhancedPortals
{
    public ArrayList<DialDeviceNetworkObject> destinationList;
    public int                                selectedDestination;
    public boolean                            active;

    String                                    oldModifierNetwork;
    String                                    oldModifierTexture;
    byte                                      oldModifierThickness;
    WorldLocation                             modifierLocation;

    public TileEntityDialDevice()
    {
        destinationList = new ArrayList<DialDeviceNetworkObject>();
        selectedDestination = -1;
        active = false;
    }

    public void processDiallingRequest(String network, EntityPlayer player, String texture, byte thickness, boolean particles, boolean sound)
    {
        if (worldObj.isRemote || active)
        {
            return;
        }

        outerloop: for (int x = -5; x < 5; x++)
        {
            for (int z = -5; z < 5; z++)
            {
                for (int y = -5; y < 5; y++)
                {
                    if (worldObj.getBlockId(xCoord + x, yCoord + y, zCoord + z) == BlockIds.PortalModifier)
                    {
                        if (!((TileEntityPortalModifier) worldObj.getBlockTileEntity(xCoord + x, yCoord + y, zCoord + z)).isAnyActive())
                        {
                            modifierLocation = new WorldLocation(xCoord + x, yCoord + y, zCoord + z, worldObj);
                            break outerloop;
                        }
                    }
                }
            }
        }

        if (modifierLocation == null)
        {
            if (player != null)
            {
                player.sendChatToPlayer(EnumChatFormatting.RED + Localization.localizeString("chat.noModifier"));
            }

            return;
        }

        if (EnhancedPortals.proxy.ModifierNetwork.hasNetwork(network))
        {
            if (modifierLocation.getBlockId() == BlockIds.PortalModifier)
            {
                TileEntityPortalModifier modifier = (TileEntityPortalModifier) modifierLocation.getTileEntity();

                oldModifierNetwork = modifier.network;
                oldModifierTexture = modifier.texture;
                oldModifierThickness = modifier.thickness;

                EnhancedPortals.proxy.ModifierNetwork.removeFromAllNetworks(modifierLocation);
                EnhancedPortals.proxy.ModifierNetwork.addToNetwork(network, modifierLocation);

                modifier.network = network;
                modifier.texture = texture;
                modifier.thickness = thickness;

                // TODO PacketDispatcher.sendPacketToAllAround(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5, 128, worldObj.provider.dimensionId, new PacketTEUpdate(modifier).getPacket());

                if (modifier.isAnyActive())
                {
                    modifier.removePortal();
                }

                if (modifier.createPortal())
                {
                    if (player != null)
                    {
                        player.sendChatToPlayer(EnumChatFormatting.GREEN + Localization.localizeString("chat.dialSuccess"));
                    }

                    worldObj.scheduleBlockUpdate(xCoord, yCoord, zCoord, BlockIds.DialHomeDevice, 760);
                    active = true;
                    // TODO PacketDispatcher.sendPacketToAllAround(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5, 128, worldObj.provider.dimensionId, new PacketTEUpdate(this).getPacket());
                }
                else
                {
                    modifier.network = oldModifierNetwork;

                    if (player != null)
                    {
                        player.sendChatToPlayer(EnumChatFormatting.RED + Localization.localizeString("chat.noPortal"));
                    }
                }
            }
            else if (player != null)
            {
                player.sendChatToPlayer(EnumChatFormatting.RED + Localization.localizeString("chat.noConnection"));
            }
        }
        else if (player != null)
        {
            player.sendChatToPlayer(EnumChatFormatting.RED + Localization.localizeString("chat.noConnection"));
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound)
    {
        super.readFromNBT(tagCompound);

        if (tagCompound.hasKey("OldNetwork"))
        {
            active = true;
            oldModifierNetwork = tagCompound.getString("OldNetwork");
            oldModifierThickness = tagCompound.getByte("OldThickness");
            modifierLocation = new WorldLocation(tagCompound.getInteger("mX"), tagCompound.getInteger("mY"), tagCompound.getInteger("mZ"), tagCompound.getInteger("mD"));
            oldModifierTexture = tagCompound.getString("OldTexture");
        }

        NBTTagList list = tagCompound.getTagList("Entries");

        for (int i = 0; i < list.tagCount(); i++)
        {
            NBTTagCompound tag = (NBTTagCompound) list.tagAt(i);

            String name = tag.getString("Name"), network = tag.getString("Network");
            byte thickness = tag.getByte("Thickness");
            boolean particle = tag.getBoolean("Particles"), sound = tag.getBoolean("Sounds");
            String texture = tag.getString("Texture");

            destinationList.add(new DialDeviceNetworkObject(name, network, texture, thickness, sound, particle));
        }
    }

    public void scheduledBlockUpdate()
    {
        if (modifierLocation == null)
        {
            active = false;
            // TODO PacketDispatcher.sendPacketToAllAround(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5, 128, worldObj.provider.dimensionId, new PacketTEUpdate(this).getPacket());
            return;
        }

        if (modifierLocation.getBlockId() == BlockIds.PortalModifier)
        {
            TileEntityPortalModifier modifier = (TileEntityPortalModifier) modifierLocation.getTileEntity();

            EnhancedPortals.proxy.ModifierNetwork.removeFromNetwork(modifier.network, modifierLocation);

            if (oldModifierNetwork != null && !oldModifierNetwork.equals(""))
            {
                EnhancedPortals.proxy.ModifierNetwork.addToNetwork(oldModifierNetwork, modifierLocation);
            }

            modifier.removePortal();
            modifier.network = oldModifierNetwork;
            modifier.texture = oldModifierTexture;
            modifier.thickness = oldModifierThickness;
            // TODO PacketDispatcher.sendPacketToAllAround(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5, 128, worldObj.provider.dimensionId, new PacketTEUpdate(modifier).getPacket());

            oldModifierNetwork = "";
            modifierLocation = null;
            oldModifierTexture = null;
            oldModifierThickness = 0;
            active = false;
            // TODO PacketDispatcher.sendPacketToAllAround(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5, 128, worldObj.provider.dimensionId, new PacketTEUpdate(this).getPacket());
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound)
    {
        super.writeToNBT(tagCompound);

        if (active)
        {
            tagCompound.setString("OldNetwork", oldModifierNetwork);
            tagCompound.setByte("OldThickness", oldModifierThickness);
            tagCompound.setInteger("mX", modifierLocation.xCoord);
            tagCompound.setInteger("mY", modifierLocation.yCoord);
            tagCompound.setInteger("mZ", modifierLocation.zCoord);
            tagCompound.setInteger("mD", modifierLocation.dimension);
            tagCompound.setString("OldTexture", oldModifierTexture);
        }

        NBTTagList list = new NBTTagList();

        for (int i = 0; i < destinationList.size(); i++)
        {
            NBTTagCompound tag = new NBTTagCompound();
            DialDeviceNetworkObject obj = destinationList.get(i);

            if (obj == null)
            {
                continue;
            }

            tag.setString("Name", obj.displayName);
            tag.setString("Network", obj.network);
            tag.setByte("Thickness", obj.thickness);
            tag.setBoolean("Particles", obj.particles);
            tag.setBoolean("Sounds", obj.sounds);
            tag.setString("Texture", obj.texture);

            list.appendTag(tag);
        }

        tagCompound.setTag("Entries", list);
    }
}
