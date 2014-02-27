package powercraft.api.gres;


import powercraft.api.PC_RectI;
import powercraft.api.PC_Vec2I;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class PC_GresButton extends PC_GresComponent {

	private static final String textureName = "Button";


	public PC_GresButton(String title) {

		setText(title);
		fontColors[0] = 0xe0e0e0;
		fontColors[1] = 0xffffa0;
		fontColors[2] = 0xffffa0;
		fontColors[3] = 0x7a7a7a;
	}


	@Override
	protected PC_Vec2I calculateMinSize() {
		PC_Vec2I size = fontRenderer.getStringSize(text);
		return getTextureMinSize(textureName).max(size.x + 6, size.y + 6);
	}


	@Override
	protected PC_Vec2I calculateMaxSize() {

		return new PC_Vec2I(-1, -1);
	}


	@Override
	protected PC_Vec2I calculatePrefSize() {
		PC_Vec2I size = fontRenderer.getStringSize(text);
		return calculateMinSize().max(new PC_Vec2I(size.x + 3, size.y + 3));
	}


	@Override
	protected void paint(PC_RectI scissor, double scale, int displayHeight, float timeStamp) {

		drawTexture(textureName, 0, 0, rect.width, rect.height);
		drawString(text, 3, mouseDown ? 4 : 3, rect.width - 6, rect.height - 6, PC_GresAlign.H.CENTER, PC_GresAlign.V.CENTER, true);
	}

}
