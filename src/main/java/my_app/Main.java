package my_app;

import megalodonte.ListenerManager;
import megalodonte.application.Context;
import megalodonte.application.MegalodonteApp;
import my_app.hotreload.HotReload;

import java.util.Set;

public class Main {

    static HotReload hotReload;
    static boolean devMode = "true".equals(System.getenv("DEV_MODE"));

    static void main() {
        MegalodonteApp.run(context -> {
            final var stage = context.javafxStage();
            stage.setTitle("Ftp file pusher");

            initialize(context);
        }, ev->{
            if(ev == MegalodonteApp.Event.CloseRequest){
                System.out.println("Clicked on X - close application");
                ListenerManager.disposeAll();
            }
        });
    }

    //mandatory for hotreload
    public static void initialize(Context context) {
        context.useView(HomeScreen.class);

        if (devMode) {
            hotReload = new HotReload()
                    .sourcePath("src/main/java")
                    .classesPath("build/classes/java/main")
                    .resourcesPath("src/main/resources")
                    .implementationClassName("my_app.hotreload.Reloader")
                    .screenClassName("my_app.HomeScreen")
                    .reloadContext(context)
                    .classesToExclude(Set.of(
                            "my_app.Main",
                            "my_app.hotreload.Reloader",
                            "my_app.hotreload.HotReload",
                            "my_app.hotreload.HotReloadClassLoader"
                    ));
            hotReload.start();
        }
    }
}