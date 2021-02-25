package net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper;

import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.items.ItemStackHandler;
import net.p3pp3rf1y.sophisticatedbackpacks.api.IBackpackUpgradeItem;
import net.p3pp3rf1y.sophisticatedbackpacks.api.IBackpackWrapper;
import net.p3pp3rf1y.sophisticatedbackpacks.api.IUpgradeAccessModifier;
import net.p3pp3rf1y.sophisticatedbackpacks.api.IUpgradeWrapper;
import net.p3pp3rf1y.sophisticatedbackpacks.api.UpgradeType;
import net.p3pp3rf1y.sophisticatedbackpacks.util.InventoryHelper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BackpackUpgradeHandler extends ItemStackHandler {
	public static final String UPGRADE_INVENTORY_TAG = "upgradeInventory";
	private final IBackpackWrapper backpackWrapper;
	private final Runnable backpackSaveHandler;
	private final Runnable onInvalidateUpgradeCaches;
	private final CompoundNBT contentsNbt;
	@Nullable
	private Runnable refreshCallBack = null;
	private final Map<Integer, IUpgradeWrapper> slotWrappers = new HashMap<>();
	private final Map<UpgradeType<? extends IUpgradeWrapper>, List<? extends IUpgradeWrapper>> typeWrappers = new HashMap<>();
	private boolean justSavingNbtChange = false;
	private boolean wrappersInitialized = false;
	@Nullable
	private IUpgradeWrapperAccessor wrapperAccessor = null;
	private boolean persistent = true;

	public BackpackUpgradeHandler(int numberOfUpgradeSlots, IBackpackWrapper backpackWrapper, CompoundNBT contentsNbt, Runnable backpackSaveHandler, Runnable onInvalidateUpgradeCaches) {
		super(numberOfUpgradeSlots);
		this.contentsNbt = contentsNbt;
		this.backpackWrapper = backpackWrapper;
		this.backpackSaveHandler = backpackSaveHandler;
		this.onInvalidateUpgradeCaches = onInvalidateUpgradeCaches;
		deserializeNBT(contentsNbt.getCompound(UPGRADE_INVENTORY_TAG));
	}

	@Override
	protected void onContentsChanged(int slot) {
		super.onContentsChanged(slot);
		if (persistent) {
			saveInventory();
			backpackSaveHandler.run();
		}
		if (!justSavingNbtChange) {
			refreshUpgradeWrappers();
		}
	}

	@Override
	public void setSize(int size) {
		super.setSize(stacks.size());
	}

	public void saveInventory() {
		contentsNbt.put(UPGRADE_INVENTORY_TAG, serializeNBT());
	}

	public void setPersistent(boolean persistent) {
		this.persistent = persistent;
	}

	public void setRefreshCallBack(Runnable refreshCallBack) {
		this.refreshCallBack = refreshCallBack;
	}

	public void removeRefreshCallback() {
		refreshCallBack = null;
	}

	private void initializeWrappers() {
		if (wrappersInitialized) {
			return;
		}
		wrappersInitialized = true;
		slotWrappers.clear();
		typeWrappers.clear();

		InventoryHelper.iterate(this, (slot, upgrade) -> {
			if (upgrade.isEmpty() || !(upgrade.getItem() instanceof IBackpackUpgradeItem<?>)) {
				return;
			}
			UpgradeType<?> type = ((IBackpackUpgradeItem<?>) upgrade.getItem()).getType();
			IUpgradeWrapper wrapper = type.create(backpackWrapper, upgrade, upgradeStack -> {
				justSavingNbtChange = true;
				setStackInSlot(slot, upgradeStack);
				justSavingNbtChange = false;
			});
			slotWrappers.put(slot, wrapper);
			if (wrapper.isEnabled()) {
				addTypeWrapper(type, wrapper);
			}
		});
	}

	private <T extends IUpgradeWrapper> void addTypeWrapper(UpgradeType<?> type, T wrapper) {
		//noinspection unchecked
		((List<T>) typeWrappers.computeIfAbsent(type, t -> new ArrayList<>())).add(wrapper);
	}

	public <T extends IUpgradeWrapper> List<T> getTypeWrappers(UpgradeType<T> type) {
		initializeWrappers();
		//noinspection unchecked
		return (List<T>) typeWrappers.getOrDefault(type, Collections.emptyList());
	}

	public <T extends IUpgradeWrapper> boolean hasUpgrade(UpgradeType<T> type) {
		return !getTypeWrappers(type).isEmpty();
	}

	public <T> List<T> getWrappersThatImplement(Class<T> upgradeClass) {
		initializeWrappers();
		return getWrapperAccessor().getWrappersThatImplement(upgradeClass);
	}

	private IUpgradeWrapperAccessor getWrapperAccessor() {
		if (wrapperAccessor == null) {
			IUpgradeWrapperAccessor accessor = new Accessor(this);
			for (IUpgradeAccessModifier upgrade : getListOfWrappersThatImplement(IUpgradeAccessModifier.class)) {
				accessor = upgrade.wrapAccessor(accessor);
			}
			wrapperAccessor = accessor;
		}
		return wrapperAccessor;
	}

	public <T> List<T> getWrappersThatImplementFromMainBackpack(Class<T> upgradeClass) {
		initializeWrappers();
		return getWrapperAccessor().getWrappersThatImplementFromMainBackpack(upgradeClass);
	}

	public <T> List<T> getListOfWrappersThatImplement(Class<T> uc) {
		List<T> ret = new ArrayList<>();
		for (IUpgradeWrapper wrapper : slotWrappers.values()) {
			if (wrapper.isEnabled() && uc.isInstance(wrapper)) {
				//noinspection unchecked
				ret.add((T) wrapper);
			}
		}
		return ret;
	}

	public Map<Integer, IUpgradeWrapper> getSlotWrappers() {
		initializeWrappers();
		return slotWrappers;
	}

	public void copyTo(BackpackUpgradeHandler otherHandler) {
		InventoryHelper.copyTo(this, otherHandler);
	}

	public void refreshUpgradeWrappers() {
		wrappersInitialized = false;
		if (wrapperAccessor != null) {
			wrapperAccessor.onBeforeDeconstruct();
			wrapperAccessor = null;
		}
		if (refreshCallBack != null) {
			refreshCallBack.run();
		}
		onInvalidateUpgradeCaches.run();
	}

	private static class Accessor implements IUpgradeWrapperAccessor {
		private final Map<Class<?>, List<?>> interfaceWrappers = new HashMap<>();

		private final BackpackUpgradeHandler upgradeHandler;

		public Accessor(BackpackUpgradeHandler upgradeHandler) {
			this.upgradeHandler = upgradeHandler;
		}

		@Override
		public <T> List<T> getWrappersThatImplement(Class<T> upgradeClass) {
			//noinspection unchecked
			return (List<T>) interfaceWrappers.computeIfAbsent(upgradeClass, upgradeHandler::getListOfWrappersThatImplement);
		}

		@Override
		public <T> List<T> getWrappersThatImplementFromMainBackpack(Class<T> upgradeClass) {
			//noinspection unchecked
			return (List<T>) interfaceWrappers.computeIfAbsent(upgradeClass, upgradeHandler::getListOfWrappersThatImplement);
		}
	}
}

