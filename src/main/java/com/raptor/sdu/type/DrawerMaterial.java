package com.raptor.sdu.type;

import static net.minecraftforge.oredict.OreDictionary.WILDCARD_VALUE;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

import com.jaquadro.minecraft.storagedrawers.StorageDrawers;
import com.raptor.sdu.block.BlockUnlimitedDrawers;
import com.raptor.sdu.block.BlockUnlimitedTrim;
import com.raptor.sdu.item.ItemUnlimitedDrawers;
import com.raptor.sdu.item.ItemUnlimitedTrim;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;	

public class DrawerMaterial implements IStringSerializable {
	private Mod mod;
	private String name;
	private BlockUnlimitedDrawers drawersBlock;
	private ItemUnlimitedDrawers drawersItem;
	private BlockUnlimitedTrim trimBlock;
	private ItemUnlimitedTrim trimItem;
	public AbstractItemStack[] planks;
	public AbstractItemStack[] slabs;
	private boolean isGrass = false;
	
	DrawerMaterial(Mod mod, String name, AbstractItemStack[] planks, AbstractItemStack[] slabs) {
		this.mod = mod;
		this.name = name;
		this.planks = planks;
		this.slabs = slabs;
	}
	
	public boolean isCraftable() {
		return planks.length != 0 && slabs.length != 0;
	}

	@Override
	public String getName() {
		return name;
	}

	public BlockUnlimitedDrawers getDrawersBlock() {
		return drawersBlock;
	}

	public ItemUnlimitedDrawers getDrawerItem() {
		return drawersItem;
	}
	
	public BlockUnlimitedTrim getTrimBlock() {
		return trimBlock;
	}
	
	public ItemUnlimitedTrim getTrimItem() {
		return trimItem;
	}
	
	public Iterable<Block> asBlockIterable() {
		return this::blockIterator;
	}
	
	public Iterable<Item> asItemIterable() {
		return this::itemIterator;
	}
	
	public Iterator<Block> blockIterator() {
		return drawersBlock == null? emptyIterator() : trimBlock == null? new SingleIterator<>(drawersBlock) : new DoubleIterator<>(drawersBlock, trimBlock);
	}
	
	public Iterator<Item> itemIterator() {
		return drawersItem == null? emptyIterator() : trimItem == null? new SingleIterator<>(drawersItem) : new DoubleIterator<>(drawersItem, trimItem);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> Iterator<T> emptyIterator() {
		return (Iterator<T>)EmptyIterator.EMPTY_ITERATOR;
	}
	
	private static class EmptyIterator<T> implements Iterator<T> {

		private EmptyIterator() {}
		
		private static final Iterator<?> EMPTY_ITERATOR = new EmptyIterator<>();
		
		@Override
		public boolean hasNext() {
			return false;
		}

		@Override
		public T next() {
			throw new NoSuchElementException("next");
		}
		
	}
	
	private static class SingleIterator<T> implements Iterator<T> {
		private final T value;
		private boolean hasNext = true;
		
		SingleIterator(T value) {
			this.value = value;
		}

		@Override
		public boolean hasNext() {
			return hasNext;
		}

		@Override
		public T next() {
			if(hasNext) { 
				hasNext = false;
				return value;
			}
			throw new NoSuchElementException("next");
		}
		
	}
	
	private static class DoubleIterator<T> implements Iterator<T> {
		private final T first, second;
		private int count = 0;
		
		DoubleIterator(T first, T second) {
			this.first = first;
			this.second = second;
		}
		
		@Override
		public boolean hasNext() {
			return count < 2;
		}

		@Override
		public T next() {
			T result;
			switch(count) {
			case 0:
				result = first;
				break;
			case 1:
				result = second;
				break;
			default:
				throw new NoSuchElementException("next");
			}
			count++;
			return result;
		}
		
	}

	public Mod getMod() {
		return mod;
	}
	
	public String toString() {
		return getName();
	}
	
	void init() {
		if(drawersBlock == null) {
			drawersBlock = new BlockUnlimitedDrawers(this);
			trimBlock = new BlockUnlimitedTrim(this);
			if(isGrass) {
				drawersBlock.setMadeOfGrass();
				trimBlock.setMadeOfGrass();
			}
		}
		if(drawersItem == null) {
			drawersItem = new ItemUnlimitedDrawers(drawersBlock);
			trimItem = new ItemUnlimitedTrim(trimBlock);
		}
	}
	
	public static interface Builder {
		Optional<DrawerMaterial> build(Mod mod);
	}
	
	public static class MaterialReference implements Builder {
		private DrawerMaterial referenced;
		
		public MaterialReference(DrawerMaterial material) {
			this.referenced = Objects.requireNonNull(material);
		}
		
		public Optional<DrawerMaterial> build(Mod mod) {
			return referenced.getMod().isEnabled()? Optional.empty() : Optional.of(referenced);
		}
	}
	
	public static class BuilderImpl implements Builder {
		private List<AbstractItemStack> planks = new ArrayList<>();
		private List<AbstractItemStack> slabs = new ArrayList<>();
		private final String name;
		private boolean isGrass = false;
		
		public BuilderImpl(String name) {
			this.name = name;
		}
		
		public DrawerMaterial.BuilderImpl setGrassy() {
			isGrass = true;
			return this;
		}
		
		public DrawerMaterial.BuilderImpl planks(String id) {
			return planks(id, WILDCARD_VALUE);
		}
		
		public DrawerMaterial.BuilderImpl planks(String id, int meta) {
			planks.add(new AbstractItemStackImpl(id, meta));
			if(meta != WILDCARD_VALUE && Loader.isModLoaded("unlimitedchiselworks"))
				planks.add(new UnlimitedChiselWorksItemStack(id, meta));
			return this;
		}
		
		public DrawerMaterial.BuilderImpl slab(String id) {
			return slab(id, WILDCARD_VALUE);
		}
		
		public DrawerMaterial.BuilderImpl slab(String id, int meta) {
			slabs.add(new AbstractItemStackImpl(id, meta));
			return this;
		}
		
		public Optional<DrawerMaterial> build(Mod mod) {
			DrawerMaterial material = new DrawerMaterial(mod, name, planks.toArray(new AbstractItemStack[planks.size()]), slabs.toArray(new AbstractItemStack[slabs.size()]));
			material.isGrass = isGrass;
			return Optional.of(material);
		}	
		
	}
	
	public static interface AbstractItemStack {
		ItemStack toItemStack(Mod mod);
	}
	
	public static class UnlimitedChiselWorksItemStack implements AbstractItemStack {
		private String name;
		private int meta;
		
		UnlimitedChiselWorksItemStack(String item, int meta) {
			this.name = item;
			this.meta = meta;
		}
		
		public ItemStack toItemStack(Mod mod) {
			int i = name.indexOf(':');
			String modid;
			if(i < 0) {
				modid = mod.modid;
			} else {
				modid = name.substring(0, i);
				name = name.substring(i+1);
			}
			return new ItemStack(Item.REGISTRY.getObject(new ResourceLocation("unlimitedchiselworks", "chisel_planks_oak_" + modid + "_" + name.replaceAll("[^a-z_A-Z]", "_") + "_" + (meta == WILDCARD_VALUE? 0 : meta))));
		}
		
		public String toString() {
			return "unlimitedchiselworks(" + name + ")";
		}
		
		public int hashCode() {
			return Objects.hash(name, meta);
		}
		
		public boolean equals(Object obj) {
			if(this == obj) return true;
			if(!(obj instanceof UnlimitedChiselWorksItemStack)) return false;
			UnlimitedChiselWorksItemStack item = (UnlimitedChiselWorksItemStack)obj;
			return meta == item.meta && name.equals(item.name);
		}
	}
	
	public static class AbstractItemStackImpl implements AbstractItemStack {
		private String modid;
		private String id;
		private int meta;
		
		AbstractItemStackImpl(String id, int meta) {
			int i = id.indexOf(':');
			if(i >= 0) {
				this.modid = id.substring(0, i);
				this.id = id.substring(i+1);
			} else {
				this.modid = null;
				this.id = id;
			}
			this.meta = meta;
		}
		
		public ItemStack toItemStack(Mod mod) {
			return new ItemStack(Item.REGISTRY.getObject(new ResourceLocation(modid == null? mod.modid : modid, id)), 1, meta);
		}
		
		public String toString() {
			return (modid == null? "" : modid+":") + id + (meta == WILDCARD_VALUE? "(*)" : "(" + meta + ")");
		}
		
		public int hashCode() {
			return Objects.hash(id, meta, modid);
		}
		
		public boolean equals(Object obj) {
			if(this == obj) return true;
			if(!(obj instanceof AbstractItemStackImpl)) return false;
			AbstractItemStackImpl item = (AbstractItemStackImpl)obj;
			return id.equals(item.id) && meta == item.meta && Objects.equals(modid, item.modid);
		}
	}
}