package my_app;

import megalodonte.State;
import megalodonte.base.components.Component;
import megalodonte.base.components.ScreenComponent;
import megalodonte.components.Button;
import megalodonte.components.layout_components.Column;

public class HomeScreen implements ScreenComponent {
    State<String> connectionStatus = State.of("Start server");
    FtpServer ftpServer = new FtpServer();

    @Override
    public Component render() {
        return new Column()
                .children(
                        new Button(connectionStatus).onClick(()-> {
                            String currentConnection = connectionStatus.get();

                            if(currentConnection.equals("Start server")){
                                    try {
                                        ftpServer.startWithDefaultCredentials();
                                        connectionStatus.set("Server started");
                                    } catch (Exception e) {
                                        throw new RuntimeException(e);
                                    }
                            }else{
                                ftpServer.stopServer();
                                connectionStatus.set("Start server");
                            }
                        } )
                );
    }
}
