package net.tihmstar.LightningQuest;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
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

import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;

import java.util.*;
import java.util.stream.Collectors;

import static net.minecraft.command.arguments.EntityArgument.getPlayers;
import static net.minecraft.entity.EntityType.LIGHTNING_BOLT;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("lightningquest")
public class LightningQuest
{
    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();

    private static HashMap<UUID, UUID> playerToSquad = new HashMap<UUID, UUID>();
    private static HashMap<UUID, Squad> squadUuidMap = new HashMap<UUID, Squad>();

    private static MinecraftServer gServer = null;

    private boolean massKillingInProgress = false;

    public LightningQuest() {
        /*
        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        // Register the enqueueIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
        // Register the processIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);
        // Register the doClientStuff method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);
        */
        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    /*
    private void setup(final FMLCommonSetupEvent event)
    {
        // some preinit code
        //LOGGER.info("HELLO FROM PREINIT");
        //LOGGER.info("DIRT BLOCK >> {}", Blocks.DIRT.getRegistryName());
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        // do something that can only be done on the client
        //LOGGER.info("Got game settings {}", event.getMinecraftSupplier().get().gameSettings);
    }

    private void enqueueIMC(final InterModEnqueueEvent event)
    {
        // some example code to dispatch IMC to another mod
        InterModComms.sendTo("lightningquest", "helloworld", () -> { //LOGGER.info("Hello world from the MDK"); return "Hello world";});
    }

    private void processIMC(final InterModProcessEvent event)
    {
        // some example code to receive and process InterModComms from other mods
        //LOGGER.info("Got IMC {}", event.getIMCStream().
                map(m->m.getMessageSupplier().get()).
                collect(Collectors.toList()));
    }
    */

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        // do something when the server starts
        //LOGGER.info("HELLO from server starting");
        gServer = event.getServer();

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
                                            //LOGGER.info("/squad create command dispatched:\n{}", name);
                                            playerCreateSquad(player, name);
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
                                            //LOGGER.info("/squad invite command dispatched:\n{}", players);

                                            for (PlayerEntity invitedPlayer: players) {
                                                playerInviteToSquad(invitingPlayer, invitedPlayer);
                                            }
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
                                            //LOGGER.info("/squad join command dispatched:\n{}", name);
                                            playerJoinSquadByName(player, name);
                                            return 0;
                                        })
                        )
                )
                .then(
                        Commands.literal("leave")
                                .executes(command -> {
                                            //LOGGER.info("/squad leave command dispatched");
                                            // current player leaves
                                            final ServerPlayerEntity player = command.getSource().asPlayer();
                                            playerLeaveSquad(player);
                                            return 0;
                                        }

                                )
                )
                .then(
                        Commands.literal("info")
                                .executes(command -> {
                                            //LOGGER.info("/squad info command dispatched");
                                            // current player squad info
                                            final ServerPlayerEntity player = command.getSource().asPlayer();
                                            playerSquadInfo(player);
                                            return 0;
                                        }

                                )
                )
                .then(
                        Commands.literal("listInvites")
                                .executes(command -> {
                                            //LOGGER.info("/squad listInvites command dispatched");
                                            // list all squad invites of current player
                                            final ServerPlayerEntity player = command.getSource().asPlayer();
                                            ArrayList<String> pendingInvitesForPlayer = new ArrayList<>();
                                            for (Squad squad: squadUuidMap.values()) {
                                                if (squad.getInvites().contains(player.getUniqueID())){
                                                    // player was invited to join this squad
                                                    pendingInvitesForPlayer.add(squad.squadName);
                                                }
                                            }
                                            if(pendingInvitesForPlayer.isEmpty()) {
                                                player.sendStatusMessage(new StringTextComponent("You have no pending invitations. It seems like you have 0 friends."), false);
                                            }else{
                                                player.sendStatusMessage(new StringTextComponent(String.format("You have pending invitations to join the following squads:%s", String.join(", ", pendingInvitesForPlayer))), false);
                                            }
                                            return 0;
                                        }

                                )
                )
        );
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        //LOGGER.info("HELLO from player logged in event! {} ({}) joined.", event.getPlayer().getName().getString(), event.getPlayer().getUniqueID());
        Squad squad = getSquadForPlayer(event.getPlayer());
        if (squad != null){
            ++squad.onlineSquadPlayers;
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        //LOGGER.info("HELLO from player logged out event! {} joined.", event.getPlayer().getName().getString());
        Squad squad = getSquadForPlayer(event.getPlayer());
        if (squad != null){
            --squad.onlineSquadPlayers;
        }
    }

    @SubscribeEvent
    public void onPlayerDeath(LivingDeathEvent event) {
        //LOGGER.info("Entity of type {} died! :( sad", event.getEntityLiving().getType());
        LivingEntity deadEntitiy = event.getEntityLiving();
        if (deadEntitiy != null && deadEntitiy.getType().equals(EntityType.PLAYER)) {
            PlayerEntity player = (PlayerEntity) deadEntitiy;

            Squad squad = getSquadForPlayer(player);
            if(squad != null){
                killSquad(squad);
                //LOGGER.info("Killed all player of squad {}! :( very sad", squad.squadName);
            }
        }
    }

    /*
    @SubscribeEvent
    public void onPlayerBreakSpeed(BreakSpeed event) {
        Squad squad = getSquadForPlayer(event.getPlayer());
        if (squad != null) {
            float multiplier = squad.getDamageMultiplier();
            event.setNewSpeed(event.getOriginalSpeed() * multiplier);
        }else{
            event.setNewSpeed(0);
        }
    }
    */

    @SubscribeEvent
    public void onPlayerLivingHurtEvent(LivingHurtEvent event) {
        DamageSource dmgsrc = event.getSource();
        Entity entity = dmgsrc.getTrueSource();
        if (!(entity instanceof PlayerEntity)){
            return;
        }
        PlayerEntity sourcePlayer = ((PlayerEntity)entity);
        Squad squad = getSquadForPlayer(sourcePlayer);
        if (squad != null) {
            float multiplier = squad.getDamageMultiplier();
            event.setAmount(event.getAmount() * multiplier);
        }else{
            event.setAmount(0);
        }
    }


    private void killSquad(Squad squad){
        List<UUID> players = squad.getSquadMembers();
        if (massKillingInProgress) {
            return;
        }
        massKillingInProgress = true;
        for (UUID pl: players ){
            ServerPlayerEntity currentPlayer = (ServerPlayerEntity)getPlayerByUUID(pl);
            if (currentPlayer == null) {
                continue;
            }
            ServerWorld currentWorld = currentPlayer.getServerWorld();
            LightningBoltEntity currentPlayerBolt = new LightningBoltEntity(LIGHTNING_BOLT, currentWorld);
            currentPlayerBolt.setPosition(currentPlayer.getPosX(), currentPlayer.getPosY(), currentPlayer.getPosZ());
            //currentWorld.addEntity(currentPlayerBolt);
            currentWorld.summonEntity(currentPlayerBolt);
            currentPlayer.attackEntityFrom(new SquadDamageSource(), Float.MAX_VALUE);
        }
        massKillingInProgress = false;
    }

    private void teleportPlayer(ServerPlayerEntity playerReq, ServerPlayerEntity playerDest){
        ServerWorld requesterWorld = playerReq.getServerWorld();
        ServerWorld destWorld = playerDest.getServerWorld();
        if(requesterWorld!= destWorld){
            String error = "";
            LOGGER.debug("Cannot Teleport Player " + playerReq.getName()+ " to " + playerDest.getName() +"; Worlds aren't identical!");
            return;
        }
        playerReq.teleport(destWorld, playerDest.getPosX(), playerDest.getPosY(), playerDest.getPosZ(), playerDest.cameraYaw, playerDest.rotationPitch);
    }

    private PlayerEntity getPlayerByUUID(UUID playerUUID){
        return gServer.getPlayerList().getPlayerByUUID(playerUUID);
    }

    private Squad getSquadForPlayer(PlayerEntity player){
        UUID squaduuid = playerToSquad.get(player.getUniqueID());
        if (squaduuid != null){
            Squad squad = squadUuidMap.get(squaduuid);
            return squad;
        }
        return null;
    }

    private void playerCreateSquad(PlayerEntity player, String name) {
        Squad oldSquad = getSquadForPlayer(player);
        if (oldSquad != null) {
            String s = "You need to leave your old squad before you can join a new one.\n";
            s += TextFormatting.GREEN + oldSquad.squadName + TextFormatting.RESET;
            s+= " won't forget your betrayal!";

            StringTextComponent msg = new StringTextComponent(s);
            player.sendStatusMessage(msg, false);
            return;
        }

        for (Squad existingSquad: squadUuidMap.values()) {
            if (existingSquad.squadName.equals(name)) {
                //LOGGER.info("Player {} tried to create a squad {}. A squad by that name already exists!", player.getName().getString(), name);

                String s = "Squad ";
                s+= TextFormatting.GREEN + name + TextFormatting.RESET;
                s+= " already exists, but they don't want you to be part of it!";

                StringTextComponent msg = new StringTextComponent(s);
                player.sendStatusMessage(msg, false);
                return;
            }
        }
        Squad squad = new Squad(name);
        UUID squadUUID = UUID.randomUUID();
        squadUuidMap.put(squadUUID, squad);
        playerToSquad.put(player.getUniqueID(), squadUUID);
        squad.join(player.getUniqueID());
        //LOGGER.info("Player {} created squad {}.", player.getName().getString(), name);

        String s = "You successfully created squad ";
        s += TextFormatting.GREEN + name + TextFormatting.RESET;
        s += "!\nHold the burden of carrying a bunch of idiots";

        StringTextComponent msg = new StringTextComponent(s);
        player.sendStatusMessage(msg, false);
    }

    private void playerJoinSquadByName(PlayerEntity player, String name) {
        Squad oldSquad = getSquadForPlayer(player);
        if (oldSquad != null) {
            String s = "You need to leave your old squad before you can join a new one.\n";
            s += TextFormatting.GREEN + oldSquad.squadName + TextFormatting.RESET;
            s+= " won't forget your betrayal!";
            StringTextComponent msg = new StringTextComponent(s);
            player.sendStatusMessage(msg, false);
            return;
        }

        for (Map.Entry<UUID, Squad> entry: squadUuidMap.entrySet()) {
            Squad squad = entry.getValue();
            if (squad.squadName.equals(name)) {
                for (UUID spuuid : squad.getSquadMembers()){
                    PlayerEntity sqp = getPlayerByUUID(spuuid);
                    if (sqp != null){
                        String s = "An idiot called ";
                        s += TextFormatting.RED + player.getName().getString() + TextFormatting.RESET;
                        s += " joined your squad";

                        StringTextComponent smsg = new StringTextComponent(s);
                        sqp.sendStatusMessage(smsg, false);
                    }
                }

                squad.join(player.getUniqueID());
                playerToSquad.put(player.getUniqueID(), entry.getKey());
                //LOGGER.info("Player {} joins squad {}.", player.getName().getString(), name);

                String s = "You successfully joined ";
                s += TextFormatting.GREEN + name + TextFormatting.RESET;
                s += "!\nLet's hope you aren't just ballast for them";

                StringTextComponent msg = new StringTextComponent(s);
                player.sendStatusMessage(msg, false);
                return;
            }
        }

        String s = "Squad ";
        s += TextFormatting.GREEN + name + TextFormatting.RESET;
        s += " does not exist, or doesn't want you to be part of it";

        StringTextComponent msg = new StringTextComponent(s);
        player.sendStatusMessage(msg, false);
    }

    private void playerLeaveSquad(PlayerEntity player) {
        UUID squadUUID = playerToSquad.get(player.getUniqueID());
        if (squadUUID == null) {
            StringTextComponent msg = new StringTextComponent("You do not belong to a squad :(\nYou are already alone");
            player.sendStatusMessage(msg, false);
            return;
        }

        Squad squad = squadUuidMap.get(squadUUID);
        squad.leave(player.getUniqueID());
        playerToSquad.remove(player.getUniqueID());

        //LOGGER.info("Player {} left squad {}.", player.getName().getString(), squad.squadName);

        String s = "You left the squad ";
        s += TextFormatting.GREEN + squad.squadName + TextFormatting.RESET;
        s += "\nGood luck on your own";

        StringTextComponent msg = new StringTextComponent(s);
        player.sendStatusMessage(msg, false);

        if (squad.getNumberOfPlayers() == 0) {
            // delete empty squad
            //LOGGER.info("Deleting empty squad {}.", squad.squadName);
            squadUuidMap.remove(squadUUID);
        }
    }

    private void playerInviteToSquad(PlayerEntity invitingPlayer, PlayerEntity invitedPlayer) {
        Squad squad = getSquadForPlayer(invitingPlayer);
        if (squad == null){
            StringTextComponent errmsg = new StringTextComponent("Error 500: You don't seem to have a squad");
            invitingPlayer.sendStatusMessage(errmsg, false);
            return;
        }
        squad.invite(invitedPlayer.getUniqueID());
        //LOGGER.info("Player {} invited to squad {}.", invitedPlayer.getName().getString(), squad.squadName);

        {
            String s = TextFormatting.RED + invitingPlayer.getName().getString() + TextFormatting.RESET;
            s += " invited you to the squad ";
            s += TextFormatting.GREEN + squad.squadName + TextFormatting.RESET;
            s += "\nIs it even worth joining?";
            invitedPlayer.sendStatusMessage(new StringTextComponent(s), false);
        }

        {
            String s = "You successfully invited ";
            s += TextFormatting.RED + invitedPlayer.getName().getString() + TextFormatting.RESET;
            s += " to your squad.\nThis wasn't your brightest idea";
            invitingPlayer.sendStatusMessage(new StringTextComponent(s), false);
        }
    }

    private void playerSquadInfo(PlayerEntity player) {
        Squad squad = getSquadForPlayer(player);
        if (squad == null) {
            StringTextComponent infostr = new StringTextComponent("You do not belong to a squad :(\nGo find some friends");
            player.sendStatusMessage(infostr, false);
            return;
        }

        String reply = "You are member of ";
        reply += TextFormatting.GREEN + squad.squadName + TextFormatting.RESET;
        reply += " which has ";
        reply += TextFormatting.BLUE + String.format("%d",squad.getNumberOfPlayers()) + TextFormatting.RESET;
        reply += " members:";

        for (UUID playerUUID : squad.getSquadMembers()){
            PlayerEntity squadplayer = getPlayerByUUID(playerUUID);
            if (squadplayer != null){
                reply += "\n" + TextFormatting.RED + squadplayer.getName().getString() + TextFormatting.RESET;
            }
        }
        reply += "\nYour ";
        reply += TextFormatting.GOLD + "AttackDamage" + TextFormatting.RESET;
        reply += " and ";
        reply += TextFormatting.GOLD + "BreakSpeed" + TextFormatting.RESET;
        reply += " is currently multiplied by ";
        reply += TextFormatting.BLUE + String.format("%.2f",squad.getDamageMultiplier()) + TextFormatting.RESET;



        StringTextComponent infostr = new StringTextComponent(reply);
        player.sendStatusMessage(infostr, false);
    }

    /*
    // You can use EventBusSubscriber to automatically subscribe events on the contained class (this is subscribing to the MOD
    // Event bus for receiving Registry Events)
    @Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {
            // register a new block here
            //LOGGER.info("HELLO from Register Block");
        }
    }
    */
}
