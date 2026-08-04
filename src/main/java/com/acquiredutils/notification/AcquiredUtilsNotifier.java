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
    private static final long AGGREGATION_WINDOW = 1500;
    private static final long DEDUP_WINDOW = 300;
    public static void init() {}
    public static void onItemAcquired(ItemStack stack) {
        if (stack.isEmpty()) return;
        RarityType rarity = RarityDetector.detectRarity(stack);
        if (!isEnabled(rarity)) return;
        String key = stack.getItem().toString() + ":" + rarity.name() + ":" + stack.getCount();
        long now = System.currentTimeMillis();
        Long last = recentItems.get(key);
        if (last != null && now - last < DEDUP_WINDOW) return;
        recentItems.put(key, now);
        synchronized (notifications) {
            for (Notification n : notifications) {
                if (n.shouldAggregateWith(stack, rarity) && now - n.getLastInternalUpdateTime() < AGGREGATION_WINDOW) {
                    n.merge(stack.getCount());
                    return;
                }
            }
            notifications.add(new Notification(stack, rarity, stack.getCount()));
        }
    }
    public static void tick() {
        notifications.removeIf(Notification::isExpired);
        long now = System.currentTimeMillis();
        recentItems.entrySet().removeIf(e -> now - e.getValue() > DEDUP_WINDOW * 2);
    }
    public static List<Notification> getNotifications() { return notifications; }
    private static boolean isEnabled(RarityType r) {
        AcquiredUtilsConfig c = ConfigManager.get();
        return switch (r) {
            case COMMON -> c.showCommon; case UNCOMMON -> c.showUncommon;
            case RARE -> c.showRare; case EPIC -> c.showEpic;
            case LEGENDARY -> c.showLegendary; case MYTHIC -> c.showMythic;
        };
    }
}