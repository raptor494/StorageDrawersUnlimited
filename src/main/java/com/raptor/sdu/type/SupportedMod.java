package com.raptor.sdu.type;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static com.raptor.sdu.SDUConfig.isValidModID;
import static java.util.Collections.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jaquadro.minecraft.storagedrawers.block.BlockDrawers;
import com.jaquadro.minecraft.storagedrawers.block.BlockTrim;
import com.raptor.sdu.SDUConfig;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.fml.ModList;

public class SupportedMod {
	static final List<SupportedMod> internal_modlist = new ArrayList<>();
	static final Map<String, SupportedMod> internal_modid_lookup = new HashMap<>();
	
	private final String modid;
	private final Set<String> aliases, incompatibleMods;
	private final Map<String, DrawerMaterial> drawerMaterials;
	
	private SupportedMod(String modid, Set<String> aliases, Set<String> incompatibleMods, Map<String, DrawerMaterial> drawerMaterials) {
		this.modid = modid;
		this.aliases = aliases;
		this.incompatibleMods = incompatibleMods;
		this.drawerMaterials = drawerMaterials;
		internal_modlist.add(this);
		internal_modid_lookup.compute(modid, (k, v) -> {
			if(v == null)
				return this;
			if(v.modid.equals(k))
				throw new IllegalStateException("Duplicate definition for mod " + k);
			return this;
		});
		for(String alias : aliases) {
			internal_modid_lookup.putIfAbsent(alias, this);
		}
	}
	
	public String getModID() {
		return modid;
	}
	
	public Set<String> getAliases() {
		return aliases;
	}
	
	public Set<String> getIncompatibleMods() {
		return incompatibleMods;
	}
	
	public Map<String, DrawerMaterial> getDrawerMaterialsMap() {
		return drawerMaterials;
	}
	
	public Collection<DrawerMaterial> getDrawerMaterials() {
		return drawerMaterials.values();
	}
	
	public Set<String> getDrawerMaterialNamesSet() {
		return drawerMaterials.keySet();
	}
	
	public @Nullable DrawerMaterial getDrawerMaterial(String name) {
		return drawerMaterials.get(name);
	}
	
	private boolean enabled = true;
	private boolean set_enabled = false;
	
	public boolean isEnabled() {
		if(!set_enabled) {
			set_enabled = true;
			if(SDUConfig.shouldForceAll()) {
				if(SDUConfig.getDisabledMods().contains(modid))
					return enabled = false;
				ModList modList = ModList.get();
				for(String alias : aliases) {
					if(!modList.isLoaded(alias) || SDUConfig.getDisabledMods().contains(alias))
						return enabled = false;
					if(SDUConfig.getForcedMods().contains(alias))
						return enabled = true;
				}
			}
			else {
				ModList modList = ModList.get();
				if(SDUConfig.getForcedMods().contains(modid))
					return enabled = true;
				if(SDUConfig.getDisabledMods().contains(modid))
					return enabled = false;
				if(!modList.isLoaded(modid)) {
					for(String alias : aliases) {
						if(!modList.isLoaded(alias) || SDUConfig.getDisabledMods().contains(alias))
							return enabled = false;
						if(SDUConfig.getForcedMods().contains(alias))
							return enabled = true;
					}
				}
				for(String modid : incompatibleMods) {
					if(modList.isLoaded(modid) || SDUConfig.getForcedMods().contains(modid))
						return enabled = false;
				}
			}
		}
		return enabled;
	}
	
	public Stream<Block> blocks() {
		return drawerMaterials.values().stream().flatMap(DrawerMaterial::blocks);
	}
	
	public Stream<Item> items() {
		return drawerMaterials.values().stream().flatMap(DrawerMaterial::items);
	}
	
	public Stream<BlockDrawers> drawers() {
		return drawerMaterials.values().stream().flatMap(DrawerMaterial::drawers);
	}
	
	public Stream<BlockDrawers> drawers_full_1() {
		return drawerMaterials.values().stream().map(material -> material.block_drawers_full_1);
	}
	
	public Stream<BlockDrawers> drawers_full_2() {
		return drawerMaterials.values().stream().map(material -> material.block_drawers_full_2);
	}
	
	public Stream<BlockDrawers> drawers_full_4() {
		return drawerMaterials.values().stream().map(material -> material.block_drawers_full_4);
	}
	
	public Stream<BlockDrawers> drawers_half_1() {
		return drawerMaterials.values().stream().map(material -> material.block_drawers_half_1);
	}
	
	public Stream<BlockDrawers> drawers_half_2() {
		return drawerMaterials.values().stream().map(material -> material.block_drawers_half_2);
	}
	
	public Stream<BlockDrawers> drawers_half_4() {
		return drawerMaterials.values().stream().map(material -> material.block_drawers_half_4);
	}
	
	public Stream<BlockTrim> trims() {
		return drawerMaterials.values().stream().map(material -> material.block_trim);
	}
	
	public Iterable<Block> getBlocksIterable() {
		return blocks()::iterator;
	}
	
	public Iterable<Item> getItemsIterable() {
		return items()::iterator;
	}
	
	public Collection<Block> getBlocks() {
		return Collections.unmodifiableCollection(this.blocks().collect(Collectors.toList()));
	}
	
	public Collection<Item> getItems() {
		return Collections.unmodifiableCollection(this.items().collect(Collectors.toList()));
	}
	
	public @Nullable DrawerMaterial getDefaultMaterial() {
		return drawerMaterials.isEmpty()? null : drawerMaterials.values().iterator().next();
	}
	
	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder {
		private String modid;
		private Set<String> aliases, incompatibleMods;
		private List<DrawerMaterial.Creator> drawerMaterials;
		
		public List<DrawerMaterial.Creator> getDrawerMaterials() {
			return drawerMaterials == null? Collections.emptyList() : Collections.unmodifiableList(drawerMaterials);
		}
		
		public Builder modid(String modid) {
			if(built != null) {
				throw new IllegalStateException("The mod has already been built");
			}
			if(!isValidModID(modid)) {
				throw new IllegalArgumentException("'" + modid + "' is not a valid mod ID");
			}
			if(this.modid == null) {
				this.modid = modid;
			}
			else {
				if(modid.equals(this.modid)) {
					throw new IllegalArgumentException("duplicate mod id alias '" + modid + "'");
				}
				if(incompatibleMods != null && incompatibleMods.contains(modid)) {
					throw new IllegalArgumentException("one of the mod's aliases was listed in its incompatible mod ids list");
				}
				if(aliases == null) {
					aliases = newHashSet();
				}
				if(!aliases.add(modid)) {
					throw new IllegalArgumentException("duplicate mod id alias '" + modid + "'");
				}
			}
			return this;
		}
		
		public Builder incompatibleMod(String modid) {
			if(built != null) {
				throw new IllegalStateException("The mod has already been built");
			}
			if(this.modid != null && modid.equals(this.modid)) {
				throw new IllegalArgumentException("the mod's own mod id was listed in its incompatible mod ids list");
			}
			if(aliases != null && aliases.contains(modid)) {
				throw new IllegalArgumentException("one of the mod's aliases was listed in its incompatible mod ids list");
			}
			if(incompatibleMods == null) {
				incompatibleMods = newHashSet();
			}
			if(!incompatibleMods.add(modid)) {
				throw new IllegalArgumentException("duplicate incompatible mod id '" + modid + "'");
			}
			return this;
		}
		
		public Builder drawerMaterial(DrawerMaterial.Creator materialBuilder) {
			if(built != null) {
				throw new IllegalStateException("The mod has already been built");
			}
			if(drawerMaterials == null) {
				drawerMaterials = newArrayList();
			}
			if(!(materialBuilder instanceof DrawerMaterial.Builder || materialBuilder instanceof DrawerMaterial.MemoizedCreator)) {
				materialBuilder = new DrawerMaterial.MemoizedCreator(materialBuilder);
			}
			drawerMaterials.add(materialBuilder);
			return this;
		}
		
		private SupportedMod built;
		private boolean isBuilding = false;
		
		private static final Logger logger = LogManager.getLogger(SupportedMod.Builder.class);
		
		public SupportedMod build() {
			if(built != null) {
				return built;
			}
			if(isBuilding) {
				throw new IllegalStateException("circular reference detected!");
			}
			this.isBuilding = true;
			
			if(modid == null) {
				throw new IllegalStateException("No mod id was given");
			}
			if(drawerMaterials == null || drawerMaterials.isEmpty()) {
				throw new IllegalStateException("No drawer materials were given");
			}
			
			Set<String> aliasesOut, incompatibleModsOut;
			Map<String, DrawerMaterial> drawerMaterialsOut;
			
			if(aliases == null) {
				aliasesOut = emptySet();
			}
			else {
				switch(aliases.size()) {
				case 0:
					aliasesOut = emptySet();
					break;
				case 1:
					aliasesOut = singleton(aliases.iterator().next());
					break;
				default:
					aliasesOut = unmodifiableSet(aliases);
					break;
				}
			}
			
			if(incompatibleMods == null) {
				incompatibleModsOut = emptySet();
			}
			else {
				switch(incompatibleMods.size()) {
				case 0:
					incompatibleModsOut = emptySet();
					break;
				case 1:
					incompatibleModsOut = singleton(incompatibleMods.iterator().next());
					break;
				default:
					incompatibleModsOut = unmodifiableSet(incompatibleMods);
					break;
				}
			}
			
			if(drawerMaterials.size() == 1) {
				DrawerMaterial mat = drawerMaterials.get(0).build();
				drawerMaterialsOut = singletonMap(mat.getName(), mat);			
			}
			else {
				drawerMaterialsOut = unmodifiableMap(drawerMaterials.stream()
										.map(DrawerMaterial.Creator::build)
										.collect(Collectors.toMap(DrawerMaterial::getName, x -> x)));
			}
			
			logger.debug("Created storage drawers material mod " + modid + " with " + drawerMaterialsOut.size() + " materials");
			
			built = new SupportedMod(modid, aliasesOut, incompatibleModsOut, drawerMaterialsOut);
			
			for(DrawerMaterial material : drawerMaterialsOut.values()) {
				material.link(built);
			}
			
			return built;
		}
		
	}
	
}
