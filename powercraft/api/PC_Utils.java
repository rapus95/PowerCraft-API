package powercraft.api;

import io.netty.buffer.ByteBuf;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.management.InstanceAlreadyExistsException;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings.GameType;
import net.minecraftforge.common.util.ForgeDirection;
import powercraft.api.block.PC_AbstractBlockBase;
import powercraft.api.block.PC_Block;
import powercraft.api.block.PC_BlockTileEntity;
import powercraft.api.block.PC_TileEntity;
import powercraft.api.inventory.PC_IInventory;
import powercraft.api.inventory.PC_IInventoryProvider;
import powercraft.api.inventory.PC_WrapperInventory;
import powercraft.api.item.PC_Item;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;

public class PC_Utils {
	
	private static PC_Utils INSTANCE;
	
	public static final int BLOCK_NOTIFY = 1, BLOCK_UPDATE = 2, BLOCK_ONLY_SERVERSIDE = 4;
	
	PC_Utils() throws InstanceAlreadyExistsException {
		if(INSTANCE!=null){
			throw new InstanceAlreadyExistsException();
		}
		INSTANCE = this;
	}
	
	public static <T> T as(Object obj, Class<T> c){
		if(obj!=null && c.isAssignableFrom(obj.getClass())){
			return c.cast(obj);
		}
		return null;
	}
	
	public static TileEntity getTileEntity(IBlockAccess world, int x, int y, int z){
		return world.getTileEntity(x, y, z);
	}
	
	public static <T> T getTileEntity(IBlockAccess world, int x, int y, int z, Class<T> c){
		return as(world.getTileEntity(x, y, z), c);
	}
	
	public static Block getBlock(IBlockAccess world, int x, int y, int z){
		return world.getBlock(x, y, z);
	}
	
	public static <T> T getBlock(IBlockAccess world, int x, int y, int z, Class<T> c) {
		return as(world.getBlock(x, y, z), c);
	}

	public static Block getBlock(String name){
		return getBlock(name, Block.class);
	}
	
	public static <T> T getBlock(String modId, String name, Class<T> c){
		return as(Block.blockRegistry.getObject(modId+":"+name), c);
	}
	
	public static <T> T getBlock(String name, Class<T> c){
		return as(Block.blockRegistry.getObject(name), c);
	}
	
	public static boolean setBlock(World world, int x, int y, int z, Block block, int metadata, int flag){
		return world.setBlock(x, y, z, block, metadata, flag);
	}
	
	public static boolean setBlock(World world, int x, int y, int z, Block block, int metadata){
		return setBlock(world, x, y, z, block, metadata, BLOCK_NOTIFY | BLOCK_UPDATE);
	}
	
	public static boolean setBlock(World world, int x, int y, int z, Block block){
		return setBlock(world, x, y, z, block, 0);
	}
	
	public static int getMetadata(IBlockAccess world, int x, int y, int z) {
		return world.getBlockMetadata(x, y, z);
	}
	
	public static boolean setMetadata(World world, int x, int y, int z, int metadata) {
		return setMetadata(world, x, y, z, metadata, BLOCK_NOTIFY | BLOCK_UPDATE);
	}
	
	public static boolean setMetadata(World world, int x, int y, int z, int metadata, int flag) {
		return world.setBlockMetadataWithNotify(x, y, z, metadata, flag);
	}
	
	public static Item getItem(ItemStack itemStack) {
		return itemStack.getItem();
	}
	
	public static <T> T getItem(ItemStack itemStack, Class<T> c) {
		return as(getItem(itemStack), c);
	}
	
	public static Item getItem(String name){
		return getItem(name, Item.class);
	}
	
	public static <T> T getItem(String modId, String name, Class<T> c){
		return as(Item.itemRegistry.getObject(modId+":"+name), c);
	}
	
	public static <T> T getItem(String name, Class<T> c){
		return as(Item.itemRegistry.getObject(name), c);
	}
	
	public static PC_Direction getSidePosition(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
		return getSidePosition(world, x, y, z, PC_Direction.fromSide(side));
	}
	
	public static PC_Direction getSidePosition(IBlockAccess world, int x, int y, int z, int side) {
		return getSidePosition(world, x, y, z, PC_Direction.fromSide(side));
	}
	
	public static PC_Direction getSidePosition(IBlockAccess world, int x, int y, int z, PC_Direction side){
		Block block = getBlock(world, x, y, z);
		if(block instanceof PC_AbstractBlockBase){
			if(((PC_AbstractBlockBase)block).canRotate(world, x, y, z)){
				PC_3DRotation rotation = ((PC_AbstractBlockBase)block).getRotation(world, x, y, z);
				if(rotation!=null){
					return rotation.getSidePosition(side);
				}
			}
		}
		return PC_Direction.UNKNOWN;
	}
	
	public static int getRotationMetadata(int metadata, Entity entity) {
		int rot = MathHelper.floor_double((double)(entity.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
		return (rot<<2) | (metadata & 3);
	}

	public static AxisAlignedBB rotateAABB(IBlockAccess world, int x, int y, int z, AxisAlignedBB box) {
		Block block = getBlock(world, x, y, z);
		if(block instanceof PC_AbstractBlockBase){
			if(((PC_AbstractBlockBase)block).canRotate(world, x, y, z)){
				if(((PC_AbstractBlockBase)block).canRotate(world, x, y, z)){
					PC_3DRotation rotation = ((PC_AbstractBlockBase)block).getRotation(world, x, y, z);
					if(rotation!=null){
						return rotation.rotateBox(box);
					}
				}
			}
		}
		return box;
	}

	public static boolean rotateBlock(World world, int x, int y, int z, PC_Direction side) {
		Block block = getBlock(world, x, y, z);
		if(block instanceof PC_Block){
			if(((PC_AbstractBlockBase)block).canRotate(world, x, y, z)){
				if(block instanceof PC_Block){
					int metadata = getMetadata(world, x, y, z);
					int rotation = (metadata>>2)&0x3;
					if(side==PC_Direction.UP){
						rotation++;
						if(rotation>3)
							rotation=0;
					}else if(side==PC_Direction.DOWN){
						rotation--;
						if(rotation<0)
							rotation=3;
					}else{
						return false;
					}
					setMetadata(world, x, y, z, rotation<<2 | (metadata & 3));
				}else if(block instanceof PC_BlockTileEntity){
					PC_TileEntity te = getTileEntity(world, x, y, z, PC_TileEntity.class);
					if(te!=null){
						PC_3DRotation rotation = te.get3DRotation();
						if(rotation!=null){
							rotation = rotation.rotateAround(side);
							if(rotation!=null){
								return te.set3DRotation(rotation);
							}
						}
					}
				}
			}
		}
		return false;
	}
	
	public static ForgeDirection[] getValidRotations(World world, int x, int y, int z) {
		Block block = getBlock(world, x, y, z);
		if(block instanceof PC_Block){
			if(((PC_AbstractBlockBase)block).canRotate(world, x, y, z)){
				if(block instanceof PC_Block){
					return new ForgeDirection[]{ForgeDirection.UP, ForgeDirection.DOWN};
				}else if(block instanceof PC_BlockTileEntity){
					PC_TileEntity te = getTileEntity(world, x, y, z, PC_TileEntity.class);
					if(te!=null){
						PC_3DRotation rotation = te.get3DRotation();
						if(rotation!=null){
							return rotation.getValidRotations();
						}
					}
				}
			}
		}
		return null;
	}

	public static boolean canPlaceEntityOnSide(World world, int x, int y, int z, PC_Direction side, Block block, Entity entity, ItemStack itemStack) {
		Block block1 = PC_Utils.getBlock(world, x, y, z);
		AxisAlignedBB box = null;
		if(block instanceof PC_AbstractBlockBase){
			if(((PC_AbstractBlockBase)block).canRotate()){
				box = ((PC_AbstractBlockBase)block).getMainCollisionBoundingBoxPre(world, x, y, z);
				if(box!=null){
					if(block instanceof PC_Block){
						int md = getRotationMetadata(0, entity)>>2;
						PC_3DRotation rotation = new PC_3DRotationY(md);
						box = rotation.rotateBox(box);
					}else if(block instanceof PC_BlockTileEntity){
						PC_3DRotation rotation = new PC_3DRotationFull(side, entity);
						box = rotation.rotateBox(box);
					}
					box.offset(x, y, z);
				}
			}
		}else{
			box = block.getCollisionBoundingBoxFromPool(world, x, y, z);
		}
        return box != null && !world.checkNoEntityCollision(box, entity) ? false : (block1.getMaterial() == Material.circuits && block == Blocks.anvil ? true : block1.isReplaceable(world, x, y, z) && block.canReplace(world, x, y, z, side.ordinal(), itemStack));
	}

	public static void dropInventoryContent(IInventory inventory, World world, double x, double y, double z) {
		if (!world.isRemote && world.getGameRules().getGameRuleBooleanValue("doTileDrops")) {
			int size = inventory.getSizeInventory();
			for (int i = 0; i < size; i++) {
				if(inventory instanceof PC_IInventory){
					if(!((PC_IInventory)inventory).canDropStack(i))
						continue;
				}
				ItemStack itemStack = inventory.getStackInSlot(i);
				if (itemStack != null) {
					inventory.setInventorySlotContents(i, null);
					PC_Utils.spawnItem(world, x, y, z, itemStack);
				}
			}
		}
	}

	public static void loadInventoryFromNBT(IInventory inventory, NBTTagCompound nbtTagCompound, String key) {

		NBTTagList list = nbtTagCompound.getTagList(key, 10);
		for (int i = 0; i < list.tagCount(); i++) {
			NBTTagCompound nbtTagCompound2 = list.getCompoundTagAt(i);
			inventory.setInventorySlotContents(nbtTagCompound2.getInteger("slot"), ItemStack.loadItemStackFromNBT(nbtTagCompound2));
		}
	}


	public static void saveInventoryToNBT(IInventory inventory, NBTTagCompound nbtTagCompound, String key) {

		NBTTagList list = new NBTTagList();
		int size = inventory.getSizeInventory();
		for (int i = 0; i < size; i++) {
			ItemStack itemStack = inventory.getStackInSlot(i);
			if (itemStack != null) {
				NBTTagCompound nbtTagCompound2 = new NBTTagCompound();
				nbtTagCompound2.setInteger("slot", i);
				itemStack.writeToNBT(nbtTagCompound2);
				list.appendTag(nbtTagCompound2);
			}
		}
		nbtTagCompound.setTag(key, list);
	}

	public static int getSlotStackLimit(IInventory inventory, int i){
		if(inventory instanceof PC_IInventory){
			((PC_IInventory)inventory).getSlotStackLimit(i);
		}
		return inventory.getInventoryStackLimit();
	}

	public static void onTick(IInventory inventory, World world) {
		int size = inventory.getSizeInventory();
		for(int i=0; i<size; i++){
			ItemStack itemStack = inventory.getStackInSlot(i);
			if(itemStack!=null){
				Item item = itemStack.getItem();
				if(item instanceof PC_Item){
					((PC_Item)item).onTick(itemStack, world, inventory, i);
				}
			}
		}
	}
	
	public static IInventory getInventoryFromEntity(Entity entity) {
		if(entity instanceof PC_IInventoryProvider){
			return ((PC_IInventoryProvider)entity).getInventory();
		}else if(entity instanceof IInventory){
			return (IInventory)entity;
		}else if(entity instanceof EntityPlayer){
			return ((EntityPlayer)entity).inventory;
		}else if(entity instanceof EntityLiving){
			return new PC_WrapperInventory(((EntityLiving)entity).getLastActiveItems());
		}
		return null;
	}
	
	public static void spawnItem(World world, double x, double y, double z, ItemStack itemStack){
		if (!world.isRemote && world.getGameRules().getGameRuleBooleanValue("doTileDrops") && itemStack != null) {
			spawnItemChecked(world, x, y, z, itemStack);
		}
	}
	
	public static void spawnItems(World world, double x, double y, double z, List<ItemStack> itemStacks){
		if (!world.isRemote && world.getGameRules().getGameRuleBooleanValue("doTileDrops") && itemStacks != null) {
			for (ItemStack itemStack : itemStacks) {
				if(itemStack!=null){
					spawnItemChecked(world, x, y, z, itemStack);
				}
			}
		}
	}

	private static void spawnItemChecked(World world, double x, double y, double z, ItemStack itemStack){
		float f = 0.7F;
		double d0 = (world.rand.nextFloat() * f) + (1.0F - f) * 0.5;
		double d1 = (world.rand.nextFloat() * f) + (1.0F - f) * 0.5;
		double d2 = (world.rand.nextFloat() * f) + (1.0F - f) * 0.5;
		EntityItem entityitem = new EntityItem(world, x + d0, y + d1, z + d2, itemStack);
		entityitem.delayBeforeCanPickup = 10;
		world.spawnEntityInWorld(entityitem);
	}
	
	public static void spawnEntity(World world, Entity entity) {
		if (!world.isRemote){
			world.spawnEntityInWorld(entity);
		}
	}
	
	public static File getPowerCraftFile(String directory, String f) {
		File file = INSTANCE.iGetPowerCraftFile();
		if (!file.exists()) file.mkdir();
		if (directory != null) {
			file = new File(file, directory);
			if (!file.exists()) file.mkdir();
		}
		return new File(file, f);
	}


	public static MinecraftServer mcs() {
		return MinecraftServer.getServer();
	}


	public static ItemStack getSmeltingResult(ItemStack item) {
		return FurnaceRecipes.smelting().getSmeltingResult(item);
	}


	public static int getRedstoneValue(World world, int x, int y, int z) {
		return world.getStrongestIndirectPower(x, y, z);
	}
	

	public static GameType getGameTypeFor(EntityPlayer player) {
		return INSTANCE.iGetGameTypeFor(player);
	}


	public static boolean isCreativ(EntityPlayer entityPlayer) {
		return getGameTypeFor(entityPlayer).isCreative();
	}


	public static void notifyBlockOfNeighborChange(World world, int x, int y, int z, Block neightbor) {
		Block block = getBlock(world, x, y, z);
		if (block != null) {
			block.onNeighborBlockChange(world, x, y, z, neightbor);
		}
	}

	File iGetPowerCraftFile() {
		return mcs().getFile("PowerCraft");
	}


	GameType iGetGameTypeFor(EntityPlayer player) {

		return ((EntityPlayerMP) player).theItemInWorldManager.getGameType();
	}

	public static ResourceLocation getResourceLocation(PC_Module module, String file) {

		return new ResourceLocation(module.getMetadata().modId.toLowerCase(), file);
	}

	public static boolean isOP(String username) {
		return mcs().getConfigurationManager().getOps().contains(username);
	}

	public static PC_Side getSide() {
		return INSTANCE.iGetSide();
	}
	
	PC_Side iGetSide(){
		return PC_Side.SERVER;
	}
	
	static void markThreadAsServer(){
		INSTANCE.iMarkThreadAsServer();
	}
	
	void iMarkThreadAsServer(){
		
	}
	
	public static boolean isServer(){
		return getSide()==PC_Side.SERVER;
	}
	
	public static boolean isClient(){
		return getSide()==PC_Side.CLIENT;
	}

	public static PC_Module getActiveModule() {
		ModContainer container = Loader.instance().activeModContainer();
		Object mod = container.getMod();
		if(mod instanceof PC_Module){
			return (PC_Module) mod;
		}
		return null;
	}
	
	public static ModContainer getActiveMod(){
		return Loader.instance().activeModContainer();
	}

	public static PC_Direction getEntityMovement2D(Entity entity) {
		double mx = entity.motionX;
		double mz = entity.motionZ;
		if(Math.abs(mx)>Math.abs(mz)){
			if(mx>0){
				return PC_Direction.EAST;
			}else{
				return PC_Direction.WEST;
			}
		}else{
			if(mz>0){
				return PC_Direction.SOUTH;
			}else{
				return PC_Direction.NORTH;
			}
		}
	}

	public static NBTTagCompound getNBTTagOf(Entity entity) {
		NBTTagCompound tag = entity.getEntityData();
		if(tag.hasKey("PowerCraft")){
			return tag.getCompoundTag("PowerCraft");
		}else{
			NBTTagCompound pctag = new NBTTagCompound();
			tag.setTag("PowerCraft", pctag);
			return pctag;
		}
	}

	public static EntityPlayer getClientPlayer() {
		return INSTANCE.iGetClientPlayer();
	}
	
	EntityPlayer iGetClientPlayer() {
		return null;
	}

	public static void writeStringToBuf(ByteBuf buf, String string) {
		buf.writeShort(string.length());
		for(int j=0; j<string.length(); j++){
			buf.writeChar(string.charAt(j));
		}
	}
	
	public static String readStringFromBuf(ByteBuf buf) {
		char[] chars = new char[buf.readUnsignedShort()];
		for(int i=0; i<chars.length; i++){
			chars[i] = buf.readChar();
		}
		return new String(chars);
	}
	
	private static MessageDigest digest;
	
	static{
		try {
			digest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static String getMD5(String s){
		return new String(digest.digest(s.getBytes()));
	}
	
}