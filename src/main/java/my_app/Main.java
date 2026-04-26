package my_app;

import megalodonte.ListenerManager;
import megalodonte.application.Context;
import megalodonte.application.MegalodonteApp;
import my_app.hotreload.HotReload;

import java.util.Set;

public class Main {

    static void main() {
        MegalodonteApp.run(context -> {
            final var stage = context.javafxStage();
            stage.setTitle("Ftp file pusher");

            context.useView(new HomeScreen().render());

            MegalodonteApp.onShutdown(() -> {
                System.out.println("Clicked on X - close application");
                ListenerManager.disposeAll();
            });
        });
    }
}