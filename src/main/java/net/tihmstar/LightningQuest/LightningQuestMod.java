package net.tihmstar.LightningQuest;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraft.util.text.StringTextComponent;

import java.util.*;
import java.util.stream.Collectors;

import static net.minecraft.command.arguments.EntityArgument.getPlayers;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("lightningquest")
public class LightningQuestMod
{
    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();

    HashMap<UUID, UUID> playerToSquad = new HashMap<UUID, UUID>();
    HashMap<UUID, Squad> squadUuidMap = new HashMap<UUID, Squad>();


    public LightningQuestMod() {
        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        // Register the enqueueIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
        // Register the processIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);
        // Register the doClientStuff method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event)
    {
        // some preinit code
        LOGGER.info("HELLO FROM PREINIT");
        LOGGER.info("DIRT BLOCK >> {}", Blocks.DIRT.getRegistryName());
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        // do something that can only be done on the client
        LOGGER.info("Got game settings {}", event.getMinecraftSupplier().get().gameSettings);
    }

    private void enqueueIMC(final InterModEnqueueEvent event)
    {
        // some example code to dispatch IMC to another mod
        InterModComms.sendTo("lightningquest", "helloworld", () -> { LOGGER.info("Hello world from the MDK"); return "Hello world";});
    }

    private void processIMC(final InterModProcessEvent event)
    {
        // some example code to receive and process InterModComms from other mods
        LOGGER.info("Got IMC {}", event.getIMCStream().
                map(m->m.getMessageSupplier().get()).
                collect(Collectors.toList()));
    }
    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        // do something when the server starts
        LOGGER.info("HELLO from server starting");
        this.registerCommands(event.getServer().getCommandManager().getDispatcher());
    }

    private void registerCommands(CommandDispatcher<CommandSource> dispatcher) {

        dispatcher.register(Commands.literal("squad")
                .then(
                        Commands.literal("create").then(
                                Commands.argument("name", StringArgumentType.word())
                                        .executes(command -> {
                                            final String name = command.getArgument("name", String.class);
                                            final ServerPlayerEntity player = command.getSource().asPlayer();
                                            LOGGER.info("/squad create command dispatched:\n{}", name);
                                            player.sendStatusMessage(playerCreateSquad(player, name), false);
                                            return 0;
                                        })
                        )
                )
                .then(
                        Commands.literal("invite").then(
                                Commands.argument("players", net.minecraft.command.arguments.EntityArgument.player())
                                        .executes(command -> {
                                            Collection<ServerPlayerEntity> players = getPlayers(command, "players");
                                            final ServerPlayerEntity invitingPlayer = command.getSource().asPlayer();
                                            LOGGER.info("/squad invite command dispatched:\n{}", players);

                                            for (PlayerEntity invitedPlayer: players) {
                                                invitedPlayer.sendStatusMessage(playerInviteToSquad(invitingPlayer, invitedPlayer), false);
                                            }
                                            // TODO: inform player about invitation
                                            return 0;
                                        })
                        )
                )
                .then(
                        Commands.literal("join").then(
                                Commands.argument("name", StringArgumentType.word())
                                        .executes(command -> {
                                            final String name = command.getArgument("name", String.class);
                                            final ServerPlayerEntity player = command.getSource().asPlayer();
                                            LOGGER.info("/squad join command dispatched:\n{}", name);
                                            player.sendStatusMessage(playerJoinSquadByName(player, name), false);
                                            return 0;
                                        })
                        )
                )
                .then(
                        Commands.literal("leave")
                                .executes(command -> {
                                            LOGGER.info("/squad leave command dispatched");
                                            // current player leaves
                                            final ServerPlayerEntity player = command.getSource().asPlayer();
                                            player.sendStatusMessage(playerLeaveSquad(player), false);
                                            return 0;
                                        }

                                )
                )
                .then(
                        Commands.literal("info")
                                .executes(command -> {
                                            LOGGER.info("/squad info command dispatched");
                                            // current player leaves
                                            final ServerPlayerEntity player = command.getSource().asPlayer();
                                            player.sendStatusMessage(playerSquadInfo(player), false);
                                            return 0;
                                        }

                                )
                )
        );
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        LOGGER.info("HELLO from player logged in event! {} ({}) joined.", event.getPlayer().getDisplayName(), event.getPlayer().getUniqueID());
        // initialize squad to null
        playerToSquad.put(event.getPlayer().getUniqueID(), null);
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        LOGGER.info("HELLO from player logged out event! {} joined.", event.getPlayer().getDisplayName());
        // test if player was in squad when leaving and if so, remove player from squad.
        playerLeaveSquad(event.getPlayer());
    }

    @SubscribeEvent
    public void onPlayerDeath(LivingDeathEvent event) {
        LOGGER.info("Entity of type {} died! :( sad", event.getEntityLiving().getType());
        if (event.getEntityLiving().getType().equals(EntityType.PLAYER)) {
            PlayerEntity player = (PlayerEntity) event.getEntityLiving();
            UUID squadUUID = playerToSquad.get(player.getUniqueID());
            if (squadUUID == null) {
                return;
            }
            Squad squad = squadUuidMap.get(squadUUID);
            squad.killAllPlayers();
            LOGGER.info("Killed all player of squad {}! :( very sad", squad.squadName);
        }
    }

    private StringTextComponent playerCreateSquad(PlayerEntity player, String name) {
        UUID oldsquaduuid = playerToSquad.get(player);
        if (oldsquaduuid != null) {
            Squad oldSquad = squadUuidMap.get(oldsquaduuid);
            return new StringTextComponent(String.format("You need to leave your old squad before you can join a new one.\n{} won't forget your betrayal!",oldSquad.squadName));
        }

        for (Squad existingSquad: squadUuidMap.values()) {
            if (existingSquad.squadName.equals(name)) {
                // TODO: handle error and tell user that squad name is already in use
                LOGGER.info("Player {} tried to create a squad {}. A squad by that name already exists!", player.getName().getString(), name);
                return new StringTextComponent(String.format("Squad {} already exists, but they don't want you to be part of it!",name));
            }
        }
        Squad squad = new Squad(name);
        UUID squadUUID = UUID.randomUUID();
        squadUuidMap.put(squadUUID, squad);
        playerToSquad.put(player.getUniqueID(), squadUUID);
        squad.join(player);
        LOGGER.info("Player {} created squad {}.", player.getName().getString(), name);
        return new StringTextComponent(String.format("You successfully created squad {}!\nHold the burden of carrying a bunch of Idiots around",name));
    }

    private StringTextComponent playerJoinSquadByName(PlayerEntity player, String name) {
        UUID oldsquaduuid = playerToSquad.get(player);
        if (oldsquaduuid != null) {
            Squad oldSquad = squadUuidMap.get(oldsquaduuid);
            return new StringTextComponent(String.format("You need to leave your old squad before you can join a new one.\n{} won't forget your betrayal!",oldSquad.squadName));
        }

        for (Map.Entry<UUID, Squad> entry: squadUuidMap.entrySet()) {
            if (entry.getValue().squadName.equals(name)) {
                entry.getValue().join(player);
                playerToSquad.put(player.getUniqueID(), entry.getKey());
                LOGGER.info("Player {} joins squad {}.", player.getName().getString(), name);
                return new StringTextComponent(String.format("You successfully joined {}!\nLet's hope you aren't just ballast for them",name));
            }
        }
        return new StringTextComponent(String.format("Squad {} does not exist, or doesn't want you to be part of it",name));
    }

    private StringTextComponent playerLeaveSquad(PlayerEntity player) {
        StringTextComponent ret = new StringTextComponent("You do not belong to a squad :(\nYou are already alone");
        UUID squadUUID = playerToSquad.get(player.getUniqueID());
        if (squadUUID != null) {
            Squad squad = squadUuidMap.get(squadUUID);
            squad.leave(player);
            playerToSquad.remove(player.getUniqueID());

            LOGGER.info("Player {} left squad {}.", player.getName().getString(), squad.squadName);
            ret = new StringTextComponent(String.format("You left the squad {}\nGood luck on your own", squad.squadName));
            if (squad.getNumberOfPlayers() == 0) {
                // delete empty squad
                LOGGER.info("Deleting empty squad {}.", squad.squadName);
                squadUuidMap.remove(squadUUID);
            }
        }
        return ret;
    }

    private StringTextComponent playerInviteToSquad(PlayerEntity invitingPlayer, PlayerEntity invitedPlayer) {
        UUID squadUUID = playerToSquad.get(invitingPlayer.getUniqueID());
        Squad squad = squadUuidMap.get(squadUUID);
        squad.invite(invitedPlayer);
        LOGGER.info("Player {} invited to squad {}.", invitedPlayer.getName().getString(), squad.squadName);
        return new StringTextComponent(String.format("You were invited to the squad {}\nIs it worth joining?",squad.squadName));
    }

    private StringTextComponent playerSquadInfo(PlayerEntity player) {
        StringTextComponent infostr = new StringTextComponent("You do not belong to a squad :(\nGo find some friends");
        UUID squadUUID = playerToSquad.get(player.getUniqueID());
        if (squadUUID != null) {
            Squad squad = squadUuidMap.get(squadUUID);
            infostr = new StringTextComponent("You are member of " + squad.squadName + " which has " + String.format("%d members",squad.getNumberOfPlayers()));
        }
        return infostr;
    }

    // You can use EventBusSubscriber to automatically subscribe events on the contained class (this is subscribing to the MOD
    // Event bus for receiving Registry Events)
    @Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {
            // register a new block here
            LOGGER.info("HELLO from Register Block");
        }
    }
}
