package powercraft.api.item;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import powercraft.api.PowerCraft;

/**
 * @author James
 * Powercraft's item
 */
public class PC_Item extends net.minecraft.item.Item{
		
	@SuppressWarnings("unused") // The Icon icon is an unused item Icon icon for icon with iconship
	private Icon icon;
	private String iconS;
	
	/**
	 * @param par1 Item ID
	 * @param name The item name
	 */
	public PC_Item(int par1, String name) {
		super(par1);
		this.itemName = name;
	}

	@SuppressWarnings("deprecation")
	@Override
	public final ItemStack onItemRightClick(net.minecraft.item.ItemStack i, net.minecraft.world.World w, net.minecraft.entity.player.EntityPlayer p){
		return onItemRightClick(new PC_ItemStack(i.itemID, i.stackSize, i.getItemDamage()), w.getWorldInfo().getVanillaDimension(), p.posX, p.posY, p.posZ).getStack();
	}
	
	@SuppressWarnings("javadoc")
	public static PC_ItemStack onItemRightClick(PC_ItemStack i, @SuppressWarnings("unused") int dim, @SuppressWarnings("unused") double x, @SuppressWarnings("unused") double y, @SuppressWarnings("unused") double z){
		return i;
	}
	
	/**
	 * The name of the item
	 */
	public String itemName = "";
	
	/**
	 * Call this, after the block is COMPLETE.
	 */
	public final void register(){
		PowerCraft.pc.itemsList.addItem(this);
	}
	
	/**
	 * Set the texture path
	 * @param path The texture path
	 */
	public final void setTexture(String path){
		this.iconS = path;
	}
	
	/**
	 * @param ir The icon register
	 */
	@Override
	public final void registerIcons(IconRegister ir){
		this.icon = ir.registerIcon(this.iconS);
	}
}
