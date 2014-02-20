package powercraft.api.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetHandler;
import powercraft.api.PC_ClientUtils;
import powercraft.api.gres.PC_Gres;
import powercraft.api.network.PC_Packet;
import powercraft.api.network.PC_PacketServerToClient;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PC_PacketOpenGresHandler extends PC_PacketServerToClient {

	private String guiOpenHandlerName;
	private int windowId;
	private NBTTagCompound nbtTagCompound;
	
	public PC_PacketOpenGresHandler(){
		
	}
	
	public PC_PacketOpenGresHandler(String guiOpenHandlerName, int windowId, NBTTagCompound nbtTagCompound) {
		this.guiOpenHandlerName = guiOpenHandlerName;
		this.windowId = windowId;
		this.nbtTagCompound = nbtTagCompound;
	}

	@Override
	@SideOnly(Side.CLIENT)
	protected PC_Packet doAndReply(INetHandler iNetHandler) {
		PC_Gres.openClientGui(PC_ClientUtils.mc().thePlayer, guiOpenHandlerName, windowId, nbtTagCompound);
		return null;
	}

	@Override
	protected void fromByteBuffer(ByteBuf buf) {
		windowId = buf.readInt();
		guiOpenHandlerName = readStringFromBuf(buf);
		nbtTagCompound = readNBTFromBuf(buf);
	}

	@Override
	protected void toByteBuffer(ByteBuf buf) {
		buf.writeInt(windowId);
		writeStringToBuf(buf, guiOpenHandlerName);
		writeNBTToBuf(buf, nbtTagCompound);
	}

}
