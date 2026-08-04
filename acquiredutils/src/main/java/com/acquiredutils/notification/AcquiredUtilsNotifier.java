package com.acquiredutils.notification;

import com.acquiredutils.config.AcquiredUtilsConfig;
import com.acquiredutils.config.ConfigManager;
import com.acquiredutils.rarity.RarityDetector;
import com.acquiredutils.rarity.RarityType;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class AcquiredUtilsNotifier {
    private static final List<Notification> notifications = new CopyOnWriteArrayList<>();
    private static final Map<String, Long> recentItems = new ConcurrentHashMap<>();
    private static final long AGGREGATION_WINDOW_MS = 1500;
    private static final long DEDUPLICATION_WINDOW_MS = 300;

    public static void init() {
    }

    public static void onItemAcquired(ItemStack stack) {
        if (stack.isEmpty()) return;

        RarityType rarity = RarityDetector.detectRarity(stack);
        if (!isRarityEnabled(rarity)) return;

        String dedupKey = stack.getItem().toString() + ":" + rarity.name() + ":" + stack.getCount();
        long now = System.currentTimeMillis();

        Long lastSeen = recentItems.get(dedupKey);
        if (lastSeen != null && now - lastSeen < DEDUPLICATION_WINDOW_MS) {
            return;
        }
        recentItems.put(dedupKey, now);

        synchronized (notifications) {
            for (Notification existing : notifications) {
                if (existing.shouldAggregateWith(stack, rarity)) {
                    if (now - existing.getLastInternalUpdateTime() < AGGREGATION_WINDOW_MS) {
                        existing.merge(stack.getCount());
                        return;
                    }
                }
            }

            notifications.add(new Notification(stack, rarity, stack.getCount()));
        }
    }

    public static void tick() {
        notifications.removeIf(Notification::isExpired);

        long now = System.currentTimeMillis();
        recentItems.entrySet().removeIf(entry -> now - entry.getValue() > DEDUPLICATION_WINDOW_MS * 2);
    }

    public static List<Notification> getNotifications() {
        return notifications;
    }

    private static boolean isRarityEnabled(RarityType rarity) {
        AcquiredUtilsConfig config = ConfigManager.get();
        return switch (rarity) {
            case COMMON -> config.showCommon;
            case UNCOMMON -> config.showUncommon;
            case RARE -> config.showRare;
            case EPIC -> config.showEpic;
            case LEGENDARY -> config.showLegendary;
            case MYTHIC -> config.showMythic;
        };
    }
}
