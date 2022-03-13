package com.github.neapovil.deathrecap;

import java.text.DecimalFormat;
import java.util.Collection;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionType;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public final class DeathRecap extends JavaPlugin implements Listener
{
    private static DeathRecap instance;

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

    public static DeathRecap getInstance()
    {
        return instance;
    }

    @EventHandler
    private void playerDeath(PlayerDeathEvent event)
    {
        if (event.getPlayer().getKiller() == null)
        {
            return;
        }

        if (!(event.getPlayer().getKiller() instanceof Player))
        {
            return;
        }

        final HoverEvent<Component> hoverevent = HoverEvent.showText(this.getRecap(event.getPlayer()));
        final HoverEvent<Component> hoverevent1 = HoverEvent.showText(this.getRecap(event.getPlayer().getKiller()));

        final Component built = Component.text("BATTLE RECAP ! ", NamedTextColor.GREEN).decoration(TextDecoration.BOLD, true)
                .append(Component.text(event.getPlayer().getName(), NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.BOLD, false).hoverEvent(hoverevent))
                .append(Component.text(" - ", NamedTextColor.GRAY))
                .append(Component.text(event.getPlayer().getKiller().getName(), NamedTextColor.LIGHT_PURPLE).hoverEvent(hoverevent1));

        this.getServer().broadcast(built);
    }

    private final String getDurability(ItemStack itemStack)
    {
        if (itemStack == null)
        {
            return "Missing";
        }

        final short max = itemStack.getType().getMaxDurability();

        return String.valueOf(max - ((Damageable) itemStack.getItemMeta()).getDamage() + " / " + max);
    }

    private final PotionType getPotionType(ItemStack itemStack)
    {
        return ((PotionMeta) itemStack.getItemMeta()).getBasePotionData().getType();
    }

    private final Component getRecap(Player player)
    {
        final String helmet = this.getDurability(player.getInventory().getHelmet());
        final String chestplate = this.getDurability(player.getInventory().getChestplate());
        final String leggings = this.getDurability(player.getInventory().getLeggings());
        final String boots = this.getDurability(player.getInventory().getBoots());

        final Collection<? extends ItemStack> splash = player.getInventory().all(Material.SPLASH_POTION).values();
        final Collection<? extends ItemStack> drink = player.getInventory().all(Material.POTION).values();

        final long instantheal = splash.stream().filter(i -> this.getPotionType(i).equals(PotionType.INSTANT_HEAL)).count();
        final long speed = splash.stream().filter(i -> this.getPotionType(i).equals(PotionType.SPEED)).count();
        final long speed1 = drink.stream().filter(i -> this.getPotionType(i).equals(PotionType.SPEED)).count();
        final long fire = splash.stream().filter(i -> this.getPotionType(i).equals(PotionType.FIRE_RESISTANCE)).count();
        final long fire1 = drink.stream().filter(i -> this.getPotionType(i).equals(PotionType.FIRE_RESISTANCE)).count();

        final String s = (new DecimalFormat("0.00")).format(player.getHealth());
        final String s1 = (new DecimalFormat("0.00")).format(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());

        return Component.text(player.getName() + "'s recap", NamedTextColor.LIGHT_PURPLE)
                .append(Component.text("\n\u2764 ", NamedTextColor.RED))
                .append(Component.text(s, NamedTextColor.WHITE))
                .append(Component.text(" / ", NamedTextColor.GRAY))
                .append(Component.text("\u2764 ", NamedTextColor.RED))
                .append(Component.text(s1, NamedTextColor.WHITE))
                .append(Component.text("\n\n< -- Armor -- >", NamedTextColor.DARK_AQUA))
                .append(Component.text("\nHelmet: " + helmet, NamedTextColor.GRAY))
                .append(Component.text("\nChestplate: " + chestplate, NamedTextColor.GRAY))
                .append(Component.text("\nLeggings: " + leggings, NamedTextColor.GRAY))
                .append(Component.text("\nBoots: " + boots, NamedTextColor.GRAY))
                .append(Component.text("\n\n< -- Potions -- >", NamedTextColor.DARK_AQUA))
                .append(Component.text("\nInstant Heal: " + instantheal, NamedTextColor.GRAY))
                .append(Component.text("\nSpeed: " + (speed + speed1), NamedTextColor.GRAY))
                .append(Component.text("\nFire Resistance: " + (fire + fire1), NamedTextColor.GRAY));
    }
}
