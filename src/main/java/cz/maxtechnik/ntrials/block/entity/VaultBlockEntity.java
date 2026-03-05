package cz.maxtechnik.ntrials.block.entity;

import cz.maxtechnik.ntrials.NTrialsModCommonConfig;
import cz.maxtechnik.ntrials.init.other.NTrialsModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
public class VaultBlockEntity extends BlockEntity{
	private final Set<UUID> playersWhoOpened=new HashSet<>();
	private int animationTick=0;
	private boolean isAnimating=false;
	private List<ItemStack> pendingLoot=new ArrayList<>();
	private int lootDropIndex=0;
	private List<ItemStack> displayItems=new ArrayList<>();
	private int currentDisplayItemIndex=0;
	private int displayItemSwitchTick=0;
	private float itemRotation=0.0f;
	private String vaultTag="";
	private String lootTable="";
	public VaultBlockEntity(BlockPos pos,BlockState blockState){
		super(NTrialsModBlockEntities.VAULT_BLOCK_ENTITY.get(),pos,blockState);
	}
	// Zkontroluje zda hráč otevřel vault
	public boolean hasPlayerOpened(UUID playerUuid){
		return playersWhoOpened.contains(playerUuid);
	}
	// Přidá hráče který otevřel vault
	public void addPlayerWhoOpened(UUID playerUuid){
		playersWhoOpened.add(playerUuid);
		setChanged();
	}
	// Spustí animaci otevírání
	public void startAnimation(List<ItemStack> loot){
		this.isAnimating=true;
		this.animationTick=0;
		this.pendingLoot=new ArrayList<>(loot);
		this.lootDropIndex=0;
		setChanged();
	}
	// Tick animace
	public void tickAnimation(){
		if(isAnimating) animationTick++;
	}
	// Vrátí tick animace
	public int getAnimationTick(){
		return animationTick;
	}
	// Zkontroluje zda probíhá animace
	public boolean isAnimating(){
		return isAnimating;
	}
	// Vrátí pending loot
	public List<ItemStack> getPendingLoot(){
		return pendingLoot;
	}
	// Vrátí index dropu lootu
	public int getLootDropIndex(){
		return lootDropIndex;
	}
	// Zvýší index dropu lootu
	public void incrementLootDropIndex(){
		lootDropIndex++;
		setChanged();
	}
	// Zastaví animaci
	public void stopAnimation(){
		this.isAnimating=false;
		this.animationTick=0;
		this.pendingLoot.clear();
		this.lootDropIndex=0;
		setChanged();
	}
	// Nastaví zobrazované itemy
	public void setDisplayItems(List<ItemStack> items){
		this.displayItems=new ArrayList<>(items);
		this.currentDisplayItemIndex=0;
		this.displayItemSwitchTick=0;
		setChanged();
		if(level!=null&&!level.isClientSide) level.sendBlockUpdated(getBlockPos(),getBlockState(),getBlockState(),3);
	}
	// Vymaže zobrazované itemy
	public void clearDisplayItems(){
		this.displayItems.clear();
		this.currentDisplayItemIndex=0;
		this.displayItemSwitchTick=0;
		setChanged();
		if(level!=null&&!level.isClientSide) level.sendBlockUpdated(getBlockPos(),getBlockState(),getBlockState(),3);
	}
	// Tick rotace zobrazovaných itemů
	public void tickDisplayItem(){
		if(!displayItems.isEmpty()){
			itemRotation+=NTrialsModCommonConfig.vaultItemRotationSpeed;
			if(itemRotation>=360.0f) itemRotation=0.0f;
			displayItemSwitchTick++;
			if(displayItemSwitchTick>=NTrialsModCommonConfig.vaultDisplayItemSwitchInterval){
				displayItemSwitchTick=0;
				int oldIndex=currentDisplayItemIndex;
				currentDisplayItemIndex=(currentDisplayItemIndex+1)%displayItems.size();
				if(oldIndex!=currentDisplayItemIndex&&level!=null&&!level.isClientSide){
					level.sendBlockUpdated(getBlockPos(),getBlockState(),getBlockState(),3);
				}
			}
		}
	}
	// Vrátí aktuální zobrazovaný item
	public ItemStack getCurrentDisplayItem(){
		return displayItems.isEmpty()?ItemStack.EMPTY:displayItems.get(currentDisplayItemIndex);
	}
	// Vrátí rotaci itemu
	public float getItemRotation(){
		return itemRotation;
	}
	// Zkontroluje zda má zobrazované itemy
	public boolean hasDisplayItems(){
		return !displayItems.isEmpty();
	}
	// Vrátí tag vaultu
	public String getVaultTag(){
		return vaultTag;
	}
	// Nastaví tag vaultu
	public void setVaultTag(String tag){
		this.vaultTag=tag;
		setChanged();
	}
	// Vrátí loot tabulku
	public String getLootTable(){
		return lootTable;
	}
	// Nastaví loot tabulku
	public void setLootTable(String lootTable){
		this.lootTable=lootTable;
		setChanged();
	}
	// Vrátí update tag pro synchronizaci
	@Override
	public @NotNull CompoundTag getUpdateTag(){
		CompoundTag tag=super.getUpdateTag();
		this.saveAdditional(tag);
		return tag;
	}
	// Zpracuje update tag od klienta
	@Override
	public void handleUpdateTag(CompoundTag tag){
		super.handleUpdateTag(tag);
		this.load(tag);
	}
	// Vrátí update packet pro síťovou synchronizaci
	@Override
	public net.minecraft.network.protocol.Packet<net.minecraft.network.protocol.game.ClientGamePacketListener> getUpdatePacket(){
		return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
	}
	// Uloží data do NBT
	@Override
	protected void saveAdditional(@NotNull CompoundTag tag){
		super.saveAdditional(tag);
		ListTag playersTag=new ListTag();
		for(UUID uuid: playersWhoOpened) playersTag.add(StringTag.valueOf(uuid.toString()));
		tag.put("PlayersWhoOpened",playersTag);
		tag.putBoolean("IsAnimating",isAnimating);
		tag.putInt("AnimationTick",animationTick);
		tag.putInt("LootDropIndex",lootDropIndex);
		ListTag displayItemsTag=new ListTag();
		for(ItemStack stack: displayItems){
			CompoundTag itemTag=new CompoundTag();
			stack.save(itemTag);
			displayItemsTag.add(itemTag);
		}
		tag.put("DisplayItems",displayItemsTag);
		tag.putInt("CurrentDisplayItemIndex",currentDisplayItemIndex);
		tag.putFloat("ItemRotation",itemRotation);
		tag.putString("VaultTag",vaultTag);
		tag.putString("LootTable",lootTable);
	}
	// Načte data z NBT
	@Override
	public void load(@NotNull CompoundTag tag){
		super.load(tag);
		playersWhoOpened.clear();
		ListTag playersTag=tag.getList("PlayersWhoOpened",8);
		for(int i=0;i<playersTag.size();i++){
			try{
				playersWhoOpened.add(UUID.fromString(playersTag.getString(i)));
			}catch(IllegalArgumentException ignored){
			}
		}
		this.isAnimating=tag.getBoolean("IsAnimating");
		this.animationTick=tag.getInt("AnimationTick");
		this.lootDropIndex=tag.getInt("LootDropIndex");
		this.displayItems.clear();
		ListTag displayItemsTag=tag.getList("DisplayItems",10);
		for(int i=0;i<displayItemsTag.size();i++) displayItems.add(ItemStack.of(displayItemsTag.getCompound(i)));
		this.currentDisplayItemIndex=tag.getInt("CurrentDisplayItemIndex");
		this.itemRotation=tag.getFloat("ItemRotation");
		this.vaultTag=tag.getString("VaultTag");
		this.lootTable=tag.getString("LootTable");
	}
}
