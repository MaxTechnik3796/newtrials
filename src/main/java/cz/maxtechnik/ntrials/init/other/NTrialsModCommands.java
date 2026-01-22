package cz.maxtechnik.ntrials.init.other;

import cz.maxtechnik.ntrials.NTrialsMod;
import cz.maxtechnik.ntrials.NTrialsModCommonConfig;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Objects;
@Mod.EventBusSubscriber
public class NTrialsModCommands{
	@SubscribeEvent
	public static void registerCommand(RegisterCommandsEvent event){
		event.getDispatcher().register(Commands.literal("ntrials_config_reload").requires(s->s.hasPermission(4)).executes(arguments->{
			MutableComponent message=Component.literal("");
			message.append(Component.literal("Configuration re-loaded!"));
			NTrialsMod.sendMessageToPlayer(Objects.requireNonNull(arguments.getSource().getPlayer()),message);
			NTrialsModCommonConfig.load();
			return 0;
		}));
	}
}
