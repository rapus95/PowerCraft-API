package powercraft.api.building;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRailBase;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemMinecart;
import net.minecraft.item.ItemReed;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayerFactory;
import powercraft.api.PC_Direction;
import powercraft.api.PC_Utils;
import powercraft.api.PC_Vec3I;
import powercraft.api.block.PC_AbstractBlockBase;


public final class PC_Build {
	
	private static List<PC_ISpecialHarvesting> specialHarvestings = new ArrayList<PC_ISpecialHarvesting>();
	
	public static void addSpecialHarvesting(PC_ISpecialHarvesting specialHarvesting){
		if(!specialHarvestings.contains(specialHarvesting)){
			specialHarvestings.add(specialHarvesting);
		}
	}
	
	public static class ItemStackSpawn{
		
		public PC_Vec3I pos;
		public ItemStack itemStack;
		
		public ItemStackSpawn(int x, int y, int z, ItemStack itemStack) {
			this.pos = new PC_Vec3I(x, y, z);
			this.itemStack = itemStack;
		}
		
		public ItemStackSpawn(PC_Vec3I pos, ItemStack itemStack) {
			this.pos = pos;
			this.itemStack = itemStack;
		}
		
	}
	
	public static PC_ISpecialHarvesting getSpecialHarvestingFor(World world, int x, int y, int z){
		for(int i=0; i<3; i++){
			for(PC_ISpecialHarvesting specialHarvesting:specialHarvestings){
				if(specialHarvesting.useFor(world, x, y, z, i)){
					return specialHarvesting;
				}
			}
		}
		return null;
	}
	
	public static PC_Harvest getHarvest(World world, PC_Vec3I pos, int usesLeft){
		return getHarvest(world, pos.x, pos.y, pos.z, usesLeft);
	}
	
	public static PC_Harvest getHarvest(World world, int x, int y, int z, int usesLeft){
		PC_ISpecialHarvesting specialHarvesting = getSpecialHarvestingFor(world, x, y, z);
		if(specialHarvesting!=null){
			return specialHarvesting.harvest(world, x, y, z, usesLeft);
		}
		Block block = PC_Utils.getBlock(world, x, y, z);
		if(block==null)
			return null;
		if(!canHarvest(world, x, y, z, block)){
			return null;
		}
		PC_Harvest harvest = new PC_Harvest();
		harvest.positions.add(new PC_Vec3I(x, y, z));
		return harvest;
	}
	
	public static List<ItemStackSpawn> harvestWithDropPos(World world, PC_Harvest harvest, int fortune){
		if(harvest==null){
			return null;
		}
		return harvestWithDropPos(world, harvest.positions, fortune);
	}
	
	public static List<ItemStackSpawn> harvestWithDropPos(World world, List<PC_Vec3I> positions, int fortune){
		List<ItemStackSpawn> drops = new ArrayList<ItemStackSpawn>();
		if(positions==null)
			return drops;
		for(PC_Vec3I position:positions){
			List<ItemStack> dropss = getDrops(world, position, fortune);
			for(ItemStack drop:dropss){
				drops.add(new ItemStackSpawn(position, drop));
			}
			PC_Utils.spawnBlockBreakParticles(world, position);
			PC_Utils.setAir(world, position);
		}
		return drops;
	}
	
	public static List<ItemStack> harvest(World world, PC_Harvest harvest, int fortune){
		return harvest(world, harvest.positions, fortune);
	}
	
	public static List<ItemStack> harvest(World world, List<PC_Vec3I> positions, int fortune){
		List<ItemStack> drops = new ArrayList<ItemStack>();
		if(positions==null)
			return drops;
		for(PC_Vec3I position:positions){
			drops.addAll(getDrops(world, position, fortune));
			PC_Utils.spawnBlockBreakParticles(world, position);
			PC_Utils.setAir(world, position);
		}
		return drops;
	}
	
	public static List<ItemStack> getDrops(World world, PC_Vec3I position, int fortune){
		Block block = PC_Utils.getBlock(world, position);
		if(block==null){
			return new ArrayList<ItemStack>();
		}
		List<ItemStack> list = block.getDrops(world, position.x, position.y, position.z, PC_Utils.getMetadata(world, position), fortune);
		if(list==null)
			return new ArrayList<ItemStack>();
		return list;
	}
	
	public static List<ItemStack> harvestEasy(World world, int x, int y, int z, int fortune){
		Block block = PC_Utils.getBlock(world, x, y, z);
		if(block==null)
			return null;
		if(!canHarvest(world, x, y, z, block)){
			return null;
		}
		List<ItemStack> drops = block.getDrops(world, x, y, z, PC_Utils.getMetadata(world, x, y, z), fortune);
		if(!world.isRemote)
			PC_Utils.setAir(world, x, y, z);
		return drops;
	}
	
	public static boolean canHarvest(World world, int x, int y, int z, Block block){
		if(block instanceof PC_AbstractBlockBase)
			return ((PC_AbstractBlockBase)block).canBeHarvested(world, x, y, z);
		return true;
	}
	
	public static boolean tryUseItem(World world, int x, int y, int z, PC_Direction dir, ItemStack itemStack){
		if(world instanceof WorldServer)
			return itemStack.getItem().onItemUse(itemStack, FakePlayerFactory.getMinecraft((WorldServer) world), world, x, y, z, dir.ordinal(), 0, 0, 0);
		return true;
	}
	
	public static boolean tryUseItem(World world, int x, int y, int z, ItemStack itemStack) {
		
		PC_Vec3I below = new PC_Vec3I(x, y-1, z);

		Block blockFront = PC_Utils.getBlock(world, x, y, z);
		
		Block block = blockFront;

		// try to put minecart
		if (itemStack.getItem() instanceof ItemMinecart) {
			
			if (BlockRailBase.func_150051_a(block)) {
				if (!world.isRemote) {
					world.spawnEntityInWorld(EntityMinecart.createMinecart(world, x + 0.5F, y + 0.5F, z + 0.5F, ((ItemMinecart) itemStack.getItem()).minecartType));
					itemStack.splitStack(1);
				}
				return true;
			}
		}

		// try to place front
		if (itemStack.getItem() instanceof ItemBlock) {
			
			ItemBlock item = ((ItemBlock) itemStack.getItem());

			if (item.field_150939_a.canPlaceBlockAt(world, x, y, z)) {
				return tryUseItem(world, x, y, z, PC_Direction.DOWN, itemStack);
			}

			return false;
		}

		// use on front block (usually bonemeal on crops)
		if (!PC_Utils.isBlockReplaceable(world, x, y, z) && !(itemStack.getItem() instanceof ItemReed)) {

			return tryUseItem(world, x, y, z, PC_Direction.DOWN, itemStack);
			
		}

		// use below
		if (PC_Utils.isBlockReplaceable(world, x, y, z) && !PC_Utils.isBlockReplaceable(world, below.x, below.y, below.z)) {
			
			return tryUseItem(world, below.x, below.y, below.z, PC_Direction.UP, itemStack);
			
		}

		return false;
	}
	
	private PC_Build(){
		PC_Utils.staticClassConstructor();
	}
	
}
