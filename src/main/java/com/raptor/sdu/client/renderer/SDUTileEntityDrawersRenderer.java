package com.raptor.sdu.client.renderer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jaquadro.minecraft.storagedrawers.block.tile.TileEntityDrawers;
import com.jaquadro.minecraft.storagedrawers.client.renderer.TileEntityDrawersRenderer;

import net.minecraft.client.renderer.BufferBuilder;

public class SDUTileEntityDrawersRenderer extends TileEntityDrawersRenderer {

	private static final Logger logger = LogManager.getLogger(SDUTileEntityDrawersRenderer.class);
	
	@Override
	public void render(TileEntityDrawers tile, double x, double y, double z, float partialTickTime, int destroyStage) {
		logger.info("rendering " + tile.getBlockState() + " at (" + x + "," + y + "," + z + ")");
		super.render(tile, x, y, z, partialTickTime, destroyStage);
	}

	@Override
	public void renderTileEntityFast(TileEntityDrawers te, double x, double y, double z, float partialTicks, int destroyStage, BufferBuilder buffer) {
		logger.info("rendering " + te.getBlockState() + " at (" + x + "," + y + "," + z + ")");
		super.renderTileEntityFast(te, x, y, z, partialTicks, destroyStage, buffer);
	}

	
	
}
