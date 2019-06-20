package com.raptor.sdu.item;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;

import com.jaquadro.minecraft.chameleon.resources.IItemMeshMapper;
import com.jaquadro.minecraft.storagedrawers.StorageDrawers;
import com.jaquadro.minecraft.storagedrawers.api.storage.EnumBasicDrawer;
import com.jaquadro.minecraft.storagedrawers.block.BlockStandardDrawers;
import com.jaquadro.minecraft.storagedrawers.config.ConfigManager;
import com.jaquadro.minecraft.storagedrawers.core.ModBlocks;
import com.jaquadro.minecraft.storagedrawers.item.ItemDrawers;
import com.mojang.realmsclient.gui.ChatFormatting;
import com.raptor.sdu.SDUnlimited;
import com.raptor.sdu.block.BlockUnlimitedDrawers;
import com.raptor.sdu.type.DrawerMaterial;
import com.raptor.sdu.type.Mod;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemUnlimitedDrawers extends ItemDrawers implements IItemMeshMapper {

	public final Mod mod;
	public final DrawerMaterial material;

	public ItemUnlimitedDrawers(BlockUnlimitedDrawers block) {
		super(block);
		this.mod = block.mod;
		this.material = block.material;
		setRegistryName(block.getRegistryName());
		setHasSubtypes(true);
	}

	@Override
	public int getMetadata(int damage) {
		return damage;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(@Nonnull ItemStack itemStack, @Nullable World world, List<String> list, ITooltipFlag advanced) {
		list.add(I18n.format("storagedrawers.material", I18n.format("storagedrawers.material." + this.mod + '.' + this.material)));
		if(advanced.isAdvanced())
			list.add(I18n.format("storagedrawers.mod", mod.getModName()));
		list.add(I18n.format("storagedrawers.drawers.description", getCapacityForBlock(itemStack)));
		
		if(itemStack.hasTagCompound() && itemStack.getTagCompound().hasKey("tile"))
			list.add(ChatFormatting.YELLOW + I18n.format("storagedrawers.drawers.sealed"));
	}
	
	private int getCapacityForBlock (@Nonnull ItemStack itemStack) {
        ConfigManager config = StorageDrawers.config;
        Block block = Block.getBlockFromItem(itemStack.getItem());

        if (block instanceof BlockStandardDrawers) {
            EnumBasicDrawer info = EnumBasicDrawer.byMetadata(itemStack.getMetadata());
            switch (info) {
                case FULL1:
                    return config.getBlockBaseStorage("fulldrawers1");
                case FULL2:
                    return config.getBlockBaseStorage("fulldrawers2");
                case FULL4:
                    return config.getBlockBaseStorage("fulldrawers4");
                case HALF2:
                    return config.getBlockBaseStorage("halfdrawers2");
                case HALF4:
                    return config.getBlockBaseStorage("halfdrawers4");
                default:
                    return 0;
            }
        }
        else if (block == ModBlocks.compDrawers) {
            return config.getBlockBaseStorage("compDrawers");
        }

        return 0;
    }

	@Override
	public String getTranslationKey(@Nonnull ItemStack stack) {
		return super.getTranslationKey() + "." + EnumBasicDrawer.byMetadata(stack.getMetadata()).getUnlocalizedName();
	}

	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
		if(tab == getCreativeTab()) {
			ConfigManager config = StorageDrawers.config;
			for(EnumBasicDrawer type : EnumBasicDrawer.values()) {
				if(config.isBlockEnabled(type.getUnlocalizedName()))
					items.add(new ItemStack(this, 1, type.getMetadata()));
			}
		}
	}

	@Override
	public List<Pair<ItemStack, ModelResourceLocation>> getMeshMappings() {
		return Arrays.stream(EnumBasicDrawer.values())
				.map(type -> Pair.of(
						new ItemStack(this, 1, type.getMetadata()),
						new ModelResourceLocation(
								SDUnlimited.MODID + ":basicdrawers_" + type.getName() + "_" + mod + "_" + material,
								"inventory")))
				.collect(Collectors.toList());
	}

}
