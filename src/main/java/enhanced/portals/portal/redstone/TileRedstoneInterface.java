package enhanced.portals.portal.redstone;

import java.util.Random;

import buildcraft.api.tools.IToolWrench;
import enhanced.base.utilities.BlockPos;
import enhanced.portals.Reference.EPBlocks;
import enhanced.portals.Reference.EPGuis;
import enhanced.portals.network.GuiHandler;
import enhanced.portals.portal.GlyphElement;
import enhanced.portals.portal.controller.TileController;
import enhanced.portals.portal.dial.TileDialingDevice;
import enhanced.portals.portal.frame.TileFrame;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

public class TileRedstoneInterface extends TileFrame {
    // Determines if this instance of a RS interface is Output or Input.
    public boolean isOutput = false;
    public byte state = 0, previousRedstoneState = 0;
    byte timeUntilOff = 0, timer = 0;
    static int TPS = 20;
    public static byte MAX_INPUT_STATE = 8, MAX_OUTPUT_STATE = 8;

    @Override
    public boolean activate(EntityPlayer player, ItemStack stack) {
        if (player.isSneaking())
            return false;

        if (stack != null) {
            if (stack.getItem() instanceof IToolWrench) {
                if (getPortalController() != null && getPortalController().isFinalized) {
                    GuiHandler.openGui(player, this, EPGuis.REDSTONE_INTERFACE);
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public void addDataToPacket(NBTTagCompound tag) {

    }

    @Override
    public boolean canUpdate() {
        return true;
    }

    int getHighestPowerState() {
        byte highest = 0;

        for (int i = 0; i < 6; i++) {
            BlockPos c = BlockPos.offset(getBlockPos(), ForgeDirection.getOrientation(i));
            byte power = (byte) getWorldObj().getIndirectPowerLevelTo(c.getX(), c.getY(), c.getZ(), i);

            if (power > highest)
                highest = power;
        }

        return highest;
    }

    public int isProvidingPower(int side) {
        if (timeUntilOff != 0)
            return 15;

        return 0;
    }

    private void notifyNeighbors() {
        worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, EPBlocks.frame);

        for (int i = 0; i < 6; i++) {
            ForgeDirection d = ForgeDirection.getOrientation(i);
            worldObj.notifyBlocksOfNeighborChange(xCoord + d.offsetX, yCoord + d.offsetY, zCoord + d.offsetZ, EPBlocks.frame);
        }
    }

    @Override
    public void onDataPacket(NBTTagCompound tag) {

    }

    public void onEntityTeleport(Entity entity) {
        // Check if this RS Interface block is an Output block.
        if (isOutput) {
            // Check to see if the entity in the portal is any entity (state 4), a player (state 5), animal (state 6), or "mob" (state 7)
            if (state == 4 || state == 5 && entity instanceof EntityPlayer || state == 6 && entity instanceof EntityAnimal || state == 7 && entity instanceof EntityMob)
                timeUntilOff = (byte) TPS;

            notifyNeighbors();
        }
    }

    public void onNeighborBlockChange(Block b) {
        if (timer != 0) return;
        timer++;
        if (!isOutput && !worldObj.isRemote) {
            TileController controller = getPortalController();

            if (controller == null)
                return;

            boolean hasDialler = controller.diallingDevices.size() > 0;
            int redstoneInputState = getHighestPowerState();

            // Input: Remove portal on signal
            if (state == 1) {
                if (redstoneInputState > 0 && controller.isPortalActive())
                    controller.deconstructConnection();
                else if (redstoneInputState == 0 && !controller.isPortalActive())
                    controller.constructConnection();
            }
            // Input: Remove Portal on Pulse
            else if (state == 3 && redstoneInputState > 0 && previousRedstoneState == 0 && controller.isPortalActive())
                controller.deconstructConnection();
            else if (!hasDialler) // These require no dialler
            {
                // Input: Create Portal on Signal
                if (state == 0) {
                    if (redstoneInputState > 0 && !controller.isPortalActive())
                        controller.constructConnection();
                    else if (redstoneInputState == 0 && controller.isPortalActive())
                        controller.deconstructConnection();
                }
                // Input: Create Portal on Pulse
                else if (state == 2 && redstoneInputState > 0 && previousRedstoneState == 0 && !controller.isPortalActive())
                    controller.constructConnection();
            } else {
                TileDialingDevice dialler = controller.getDialDeviceRandom();

                if (dialler == null)
                    return;

                int glyphCount = dialler.glyphList.size();

                if (glyphCount == 0)
                    return;

                // Input: Dial specific identifier
                if (state == 4 && redstoneInputState > 0 && !controller.isPortalActive()) {
                    if (redstoneInputState - 1 < glyphCount) {
                        GlyphElement e = dialler.glyphList.get(redstoneInputState - 1);
                        controller.constructConnection(e.identifier, e.texture, null);
                    }
                }
                // Input: Dial specific identifier II
                else if (state == 5) {
                    if (redstoneInputState > 0 && !controller.isPortalActive()) {
                        if (redstoneInputState - 1 < glyphCount) {
                            GlyphElement e = dialler.glyphList.get(redstoneInputState - 1);
                            controller.constructConnection(e.identifier, e.texture, null);
                        }
                    } else if (redstoneInputState == 0 && controller.isPortalActive())
                        controller.deconstructConnection();
                }
                // Input: Dial random identifier
                else if (state == 6 && redstoneInputState > 0 && !controller.isPortalActive()) {
                    GlyphElement e = dialler.glyphList.get(new Random().nextInt(glyphCount));
                    controller.constructConnection(e.identifier, e.texture, null);
                }
                // Input: Dial random identifier II
                else if (state == 7)
                    if (redstoneInputState > 0 && !controller.isPortalActive()) {
                        GlyphElement e = dialler.glyphList.get(new Random().nextInt(glyphCount));
                        controller.constructConnection(e.identifier, e.texture, null);
                    } else if (redstoneInputState == 0 && controller.isPortalActive())
                        controller.deconstructConnection();
            }
        }
    }

    public void onPortalCreated() {
        if (isOutput) {
            // Output: Portal Created
            if (state == 0)
                timeUntilOff = (byte) TPS;
            else if (state == 2)
                timeUntilOff = -1;
            else if (state == 3)
                timeUntilOff = 0;

            notifyNeighbors();
        }
    }

    public void onPortalRemoved() {
        if (isOutput) {
            // Output: Portal Removed
            if (state == 1)
                timeUntilOff = (byte) TPS;
            else if (state == 2)
                timeUntilOff = 0;
            else if (state == 3)
                timeUntilOff = -1;

            notifyNeighbors();
        }
    }

    @Override
    public void writeToGui(ByteBuf buffer) {
        buffer.writeBoolean(isOutput);
        buffer.writeByte(state);
    }

    @Override
    public void readFromGui(ByteBuf buffer) {
        isOutput = buffer.readBoolean();
        setState(buffer.readByte());
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        isOutput = tagCompound.getBoolean("output");
        state = tagCompound.getByte("state");
        previousRedstoneState = tagCompound.getByte("previousRedstoneState");
        timeUntilOff = tagCompound.getByte("timeUntilOff");
    }

    public void setState(byte newState) {
        state = newState;

        if (timeUntilOff != 0) {
            timeUntilOff = 0;
            notifyNeighbors();
        }
    }

    @Override
    public void updateEntity() {
        super.updateEntity();

        if (isOutput)
            if (timeUntilOff > 1)
                timeUntilOff--;
            else if (timeUntilOff == 1) {
                timeUntilOff--;
                notifyNeighbors(); // Make sure we update our neighbors
            }
        
        if (timer != 0)
            timer--;
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setBoolean("output", isOutput);
        tagCompound.setByte("state", state);
        tagCompound.setByte("previousRedstoneState", previousRedstoneState);
        tagCompound.setByte("timeUntilOff", timeUntilOff);
    }
}
