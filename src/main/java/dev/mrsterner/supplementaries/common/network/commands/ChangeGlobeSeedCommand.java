package net.mehvahdjukaar.supplementaries.common.network.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.mehvahdjukaar.supplementaries.common.world.data.GlobeData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;

import java.util.Random;

public class ChangeGlobeSeedCommand implements Command<CommandSourceStack> {

    private static final ChangeGlobeSeedCommand CMD = new ChangeGlobeSeedCommand();
    private static final Random rand = new Random();

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher) {
        return Commands.literal("newseed")
                .requires(cs -> cs.hasPermission(2))
                .executes(CMD);
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerWorld level = context.getSource().getWorld();
        GlobeData newData = new GlobeData(rand.nextLong());
        GlobeData.set(level, newData);

        newData.sendToClient(level);
        context.getSource().sendSuccess(new TranslatableComponent("message.supplementaries.command.globe_changed"), false);
        return 0;
    }
}
