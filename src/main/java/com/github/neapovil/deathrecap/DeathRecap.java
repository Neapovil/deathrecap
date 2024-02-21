package com.github.neapovil.deathrecap;

import java.text.DecimalFormat;
import java.util.function.Function;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.java.JavaPlugin;

import net.kyori.adventure.text.minimessage.MiniMessage;

public final class DeathRecap extends JavaPlugin implements Listener
{
    private static DeathRecap instance;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    @Override
    public void onEnable()
    {
        instance = this;

        this.getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable()
    {
    }

    public static DeathRecap instance()
    {
        return instance;
    }

    @EventHandler
    private void playerDeath(PlayerDeathEvent event)
    {
        if (!(event.getPlayer().getKiller() instanceof Player))
        {
            return;
        }

        final String hover = this.recap(event.getPlayer());
        final String hover1 = this.recap(event.getPlayer().getKiller());

        final String string = "<green><bold>BATTLE RECAP ! <light_purple><!bold><hover:show_text:\"%s\">%s</hover> <gray>vs <light_purple><hover:show_text:\"%s\">%s</hover>"
                .formatted(
                        hover,
                        event.getPlayer().getName(),
                        hover1,
                        event.getPlayer().getKiller().getName());

        this.getServer().broadcast(this.miniMessage.deserialize(string));
    }

    private String recap(Player player)
    {
        final StringBuilder stringbuilder = new StringBuilder();

        stringbuilder.append("<light_purple>" + player.getName() + "'s recap");

        final String health = (new DecimalFormat("0.00")).format(player.getHealth());
        final String maxhealth = (new DecimalFormat("0.00")).format(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());

        stringbuilder.append("\n<red>\u2764<white>%s <gray>/ <red>\u2764<white>%s".formatted(health, maxhealth));
        stringbuilder.append("\n\n<dark_aqua><underlined>Armor</underlined>");

        final Function<ItemStack, String> durability = itemStack -> {
            if (itemStack == null)
            {
                return "N/a";
            }

            final short max = itemStack.getType().getMaxDurability();

            return String.valueOf(max - ((Damageable) itemStack.getItemMeta()).getDamage() + " / " + max);
        };

        final String helmet = durability.apply(player.getInventory().getHelmet());
        final String chestplate = durability.apply(player.getInventory().getChestplate());
        final String leggings = durability.apply(player.getInventory().getLeggings());
        final String boots = durability.apply(player.getInventory().getBoots());

        stringbuilder.append("\n<gray>Helmet: " + helmet);
        stringbuilder.append("\n<gray>Chestplate: " + chestplate);
        stringbuilder.append("\n<gray>Leggings: " + leggings);
        stringbuilder.append("\n<gray>Boots: " + boots);

        stringbuilder.append("\n\n<dark_aqua><underlined>Potions</underlined>");

        long instantheal = 0;
        long speed = 0;
        long fire = 0;

        for (ItemStack i : player.getInventory())
        {
            if (i == null)
            {
                continue;
            }

            if (!(i.getItemMeta() instanceof PotionMeta potionmeta))
            {
                continue;
            }

            switch (potionmeta.getBasePotionData().getType())
            {
            case INSTANT_HEAL:
                instantheal++;
                break;
            case SPEED:
                speed++;
                break;
            case FIRE_RESISTANCE:
                fire++;
                break;
            default:
                break;
            }
        }

        stringbuilder.append("\n<gray>Instant Heal: " + instantheal);
        stringbuilder.append("\n<gray>Speed: " + speed);
        stringbuilder.append("\n<gray>Fire Resistance: " + fire);

        return stringbuilder.toString();
    }
}
