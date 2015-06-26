package enhanced.portals.block;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import enhanced.base.block.BlockBase;
import enhanced.base.utilities.ConnectedTexturesDetailed;
import enhanced.portals.EnhancedPortals;
import enhanced.portals.utility.Reference.EPMod;

public class BlockDecorBorderedQuartz extends BlockBase {
    ConnectedTexturesDetailed connectedTextures;

    public BlockDecorBorderedQuartz(String n) {
        super(EPMod.ID, n, Material.rock, EnhancedPortals.instance.creativeTab, 3f);
        connectedTextures = new ConnectedTexturesDetailed(BlockFrame.connectedTextures, this, -1);
    }

    @Override
    public IIcon getIcon(IBlockAccess blockAccess, int x, int y, int z, int f) {
        return connectedTextures.getIconForSide(blockAccess, x, y, z, f);
    }

    @Override
    public IIcon getIcon(int side, int meta) {
        return connectedTextures.getBaseIcon();
    }

    @Override
    public void registerBlockIcons(IIconRegister iir) {

    }
}
