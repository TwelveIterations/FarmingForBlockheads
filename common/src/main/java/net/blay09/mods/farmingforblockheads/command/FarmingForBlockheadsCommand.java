package net.blay09.mods.farmingforblockheads.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.blay09.mods.balm.Balm;
import net.blay09.mods.farmingforblockheads.FarmingForBlockheadsConfig;
import net.blay09.mods.farmingforblockheads.mixin.RecipeManagerAccessor;
import net.blay09.mods.farmingforblockheads.recipe.ModRecipes;
import net.blay09.mods.farmingforblockheads.registry.MarketDefaultsRegistry;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

public class FarmingForBlockheadsCommand {

    private static final SuggestionProvider<CommandSourceStack> GROUP_SUGGESTIONS = (context, builder) ->
            SharedSuggestionProvider.suggest(collectKnownGroups(context.getSource()), builder);

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("farmingforblockheads")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("enable")
                        .then(Commands.argument("group", StringArgumentType.word())
                                .suggests(GROUP_SUGGESTIONS)
                                .executes(context -> toggleGroup(context, true))))
                .then(Commands.literal("disable")
                        .then(Commands.argument("group", StringArgumentType.word())
                                .suggests(GROUP_SUGGESTIONS)
                                .executes(context -> toggleGroup(context, false))))
                .then(Commands.literal("list")
                        .executes(FarmingForBlockheadsCommand::listDefaults)));
    }

    private static int toggleGroup(CommandContext<CommandSourceStack> context, boolean enabled) {
        final var group = StringArgumentType.getString(context, "group");
        Balm.config().updateLocalConfig(FarmingForBlockheadsConfig.class, config -> {
            final var includedGroups = new LinkedHashSet<>(config.includedGroups);
            final var excludedGroups = new LinkedHashSet<>(config.excludedGroups);
            if (enabled) {
                excludedGroups.remove(group);
                includedGroups.add(group);
            } else {
                includedGroups.remove(group);
                excludedGroups.add(group);
            }
            config.includedGroups = includedGroups;
            config.excludedGroups = excludedGroups;
        });

        final var translationKey = enabled ? "commands.farmingforblockheads.enable.success" : "commands.farmingforblockheads.disable.success";
        context.getSource().sendSuccess(() -> Component.translatable(translationKey, group), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int listDefaults(CommandContext<CommandSourceStack> context) {
        final var enabledDefaults = collectEnabledDefaults();
        final var disabledDefaults = collectDisabledDefaults();

        context.getSource().sendSuccess(() -> Component.translatable(
                "commands.farmingforblockheads.list.enabled",
                formatGroupList(enabledDefaults, ChatFormatting.GREEN)), false);
        if (!disabledDefaults.isEmpty()) {
            context.getSource().sendSuccess(() -> Component.translatable(
                    "commands.farmingforblockheads.list.disabled",
                    formatGroupList(disabledDefaults, ChatFormatting.RED)), false);
        }

        return Command.SINGLE_SUCCESS;
    }

    private static Component formatGroupList(Set<String> groups, ChatFormatting color) {
        if (groups.isEmpty()) {
            return Component.translatable("commands.farmingforblockheads.list.none").withStyle(ChatFormatting.GRAY);
        }

        return Component.literal(String.join(", ", groups)).withStyle(color);
    }

    private static Set<String> collectEnabledDefaults() {
        final var config = FarmingForBlockheadsConfig.getActive();
        final var includedGroups = config.includedGroups;
        final var excludedGroups = config.excludedGroups;
        final var enabledDefaults = new TreeSet<String>();

        for (final var group : includedGroups) {
            if (!group.equals("default") && !excludedGroups.contains(group)) {
                enabledDefaults.add(group);
            }
        }

        if (includedGroups.contains("default") && !excludedGroups.contains("default")) {
            for (final var group : MarketDefaultsRegistry.INSTANCE.getKnownGroups()) {
                final var marketDefault = MarketDefaultsRegistry.resolveExactDefault(group);
                if (marketDefault.enabledByDefault().orElse(false) && !excludedGroups.contains(group)) {
                    enabledDefaults.add(group);
                }
            }
        }

        return enabledDefaults;
    }

    private static Set<String> collectDisabledDefaults() {
        final var config = FarmingForBlockheadsConfig.getActive();
        final var includedGroups = config.includedGroups;
        final var excludedGroups = config.excludedGroups;
        final var disabledDefaults = new TreeSet<String>();
        final var useDefaultIncludedGroups = includedGroups.contains("default") && !excludedGroups.contains("default");

        for (final var group : excludedGroups) {
            if (isIncludedByGroup(group, includedGroups)) {
                disabledDefaults.add(group);
            } else if (!group.equals("default") && useDefaultIncludedGroups && MarketDefaultsRegistry.resolveDefaults(group).enabledByDefault().orElse(false)) {
                disabledDefaults.add(group);
            }
        }

        return disabledDefaults;
    }

    private static boolean isIncludedByGroup(String group, Set<String> includedGroups) {
        for (final var includedGroup : includedGroups) {
            if (!includedGroup.equals("default") && (group.equals(includedGroup) || group.startsWith(includedGroup + "."))) {
                return true;
            }
        }

        return false;
    }

    private static Set<String> collectKnownGroups(CommandSourceStack source) {
        final var groups = new HashSet<String>();
        groups.add("default");
        groups.addAll(MarketDefaultsRegistry.INSTANCE.getKnownGroups());

        final var recipeManager = source.getServer().getRecipeManager();
        if (recipeManager instanceof RecipeManagerAccessor accessor) {
            for (final var recipeHolder : accessor.getRecipes().byType(ModRecipes.marketRecipe.type())) {
                groups.addAll(MarketDefaultsRegistry.flattenDefaults(recipeHolder.value().getDefaults()));
            }
        }

        return new TreeSet<>(groups);
    }
}
