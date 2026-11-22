package tech.atani.client.utility.discord;

import net.arikia.dev.drpc.DiscordEventHandlers;
import net.arikia.dev.drpc.DiscordRPC;
import net.arikia.dev.drpc.DiscordRichPresence;
import tech.atani.client.loader.Modification;
import tech.atani.client.utility.interfaces.Methods;

/**
 * غلاف آمن لـ Discord Rich Presence.
 *
 * مكتبة discord-rpc تحتوي natives لـ windows/linux(osx x86_64) فقط ولا يوجد
 * أي native لأندرويد arm64 (أجهزة FCL/Pojav). أي استدعاء مباشر كان سيرمي
 * UnsatisfiedLinkError داخل Modification.start() ويكرش العميل عند الإقلاع.
 * الآن: أول فشل يعطّل الـ Presence بصمت ويُكمل العميل عمله بشكل طبيعي.
 */
public class DiscordRP implements Methods {

    private static boolean available = true;
    private static boolean initialized = false;

    private static void disable(String where, Throwable t) {
        if (available) {
            available = false;
            System.out.println("[Atani] Discord Rich Presence disabled (" + where + "): " + t);
        }
    }

    public static void startup() {
        if (!available) return;
        try {
            DiscordEventHandlers handlers = new DiscordEventHandlers.Builder().setReadyEventHandler((user) -> {
                System.out.println("Welcome " + user.username + "!");
            }).build();
            DiscordRPC.discordInitialize("1141582938127998988", handlers, true);
            initialized = true;
        } catch (Throwable t) {
            disable("startup", t);
        }
    }

    public static void create() {
        if (!available || !initialized) return;
        try {
            final DiscordRichPresence rich = new DiscordRichPresence.Builder("Logging in...")
                    .setBigImage("icon", getText())
                    .build();
            DiscordRPC.discordUpdatePresence(rich);
        } catch (Throwable t) {
            disable("create", t);
        }
    }


    public static void update(final String details, final String state) {
        if (!available || !initialized) return;
        new Thread(() -> {
            try {
                final DiscordRichPresence rich = new DiscordRichPresence.Builder(state)
                        .setBigImage("icon", getText())
                        .setDetails(details)
                        .setStartTimestamps(Modification.INSTANCE.initTime)
                        .build();
                DiscordRPC.discordUpdatePresence(rich);
            } catch (Throwable t) {
                disable("update", t);
            }
        }).start();
    }

    public static void update(final String state) {
        if (!available || !initialized) return;
        new Thread(() -> {
            try {
                final DiscordRichPresence rich = new DiscordRichPresence.Builder(state)
                        .setBigImage("icon", getText())
                        .setStartTimestamps(Modification.INSTANCE.initTime)
                        .build();
                DiscordRPC.discordUpdatePresence(rich);
            } catch (Throwable t) {
                disable("update", t);
            }
        }).start();
    }

    public static void end() {
        if (!available || !initialized) return;
        try {
            DiscordRPC.discordShutdown();
        } catch (Throwable t) {
            disable("end", t);
        }
    }

    public final static String getText() {
        return CLIENT_NAME + " " + CLIENT_VERSION;
    }

    public static boolean isAvailable() {
        return available;
    }

}
