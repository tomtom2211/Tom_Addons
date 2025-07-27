package tomtom2211.modid;

import com.google.gson.Gson;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.hypixel.api.HypixelAPI;
import net.hypixel.api.http.HypixelHttpClient;
import net.hypixel.api.reactor.ReactorHttpClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;
import static tomtom2211.modid.mojangAPI.formatUuid;

public class TomsAddonsClient implements ClientModInitializer {
    static String apikey;
    @Override
    public void onInitializeClient() {
        MinecraftClient mcClient = MinecraftClient.getInstance(); //Get player's instance
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, commandRegistryAccess) ->{
            dispatcher.register( //Create /setAPIKey command
                    literal("setApiKey")
                    .then(argument("key",StringArgumentType.string())
                    .executes(context -> {
                        apikey = StringArgumentType.getString(context,"key");
                        if(mcClient.player!=null){
                        mcClient.player.sendMessage(Text.of("API key is: " + apikey), false);
                        }
                        return 1;
                    })));
                dispatcher.register( //Create /status command
                        literal("status")
                .then(argument("nickname", StringArgumentType.string())
                        .executes(context -> {
                            if(apikey!=null) {
                                final String API_KEY = apikey;
                                HypixelHttpClient client = new ReactorHttpClient(UUID.fromString(API_KEY));
                                HypixelAPI hypixelAPI = new HypixelAPI(client); //Create new hypixelApi client
                                final String nickname = StringArgumentType.getString(context, "nickname");
                                final String url = "https://api.mojang.com/users/profiles/minecraft/" + nickname;
                                try {
                                    HttpClient client2 = HttpClient.newHttpClient();
                                    HttpRequest request = HttpRequest.newBuilder()
                                            .uri(new URI(url))
                                            .GET()
                                            .build();
                                    HttpResponse<String> response = client2.send(request, HttpResponse.BodyHandlers.ofString());
                                    String jsonString = response.body();

                                    Gson gson = new Gson();
                                    mojangAPI player = gson.fromJson(jsonString, mojangAPI.class);

                                    if (player != null) {
                                        final String uuid = player.getId();
                                        hypixelAPI.getStatus(UUID.fromString(formatUuid(uuid)))
                                                .thenAccept(result -> {
                                                    final boolean status = result.getSession().isOnline(); //Returns true/false if the player is / is not online
                                                    if (status) {
                                                        final String currently = "Online";
                                                        if (!result.getSession().getServerType().getName().isBlank() && !result.getSession().getMode().isBlank()) {
                                                            final String game = result.getSession().getServerType().getName();
                                                            final String lobby = result.getSession().getMode();
                                                            context.getSource().sendFeedback(Text.literal("%s is currently %s on %s in %s".formatted(nickname, currently, game, lobby)));
                                                        } else {
                                                            context.getSource().sendFeedback(Text.literal("%s is currently %s".formatted(nickname, currently)));
                                                        }
                                                    } else {
                                                        final String currently = "Offline";
                                                        context.getSource().sendFeedback(Text.literal("%s is currently %s".formatted(nickname, currently)));
                                                    }
                                                });
                                    } else if (mcClient.player != null) {
                                        mcClient.player.sendMessage(Text.of("Player was not found!"), false);
                                    }

                                } catch (URISyntaxException | IOException | InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            else if (mcClient.player!=null){
                                mcClient.player.sendMessage(Text.of("You have to set your API key first! Use /setApiKey <key>!"),false);
                            }
                            return 1;
                        })));
        });
    }
}
