package my_app;

import megalodonte.ComputedState;
import megalodonte.ForEachState;
import megalodonte.ListState;
import megalodonte.State;
import megalodonte.base.components.Component;
import megalodonte.base.components.ScreenComponent;
import megalodonte.components.Button;
import megalodonte.components.SpacerVertical;
import megalodonte.components.Text;
import megalodonte.components.layout_components.Column;
import megalodonte.components.layout_components.Container;
import megalodonte.components.layout_components.Row;
import megalodonte.props.ColumnProps;
import megalodonte.props.ContainerProps;
import megalodonte.props.RowProps;
import megalodonte.v2.Show;

import java.io.File;

public class HomeScreen implements ScreenComponent {
    FtpServer ftpServer = new FtpServer();

    State<Boolean> runningFtpServer = State.of(false);
    ComputedState<String> ftpServerText = ComputedState.of(() -> runningFtpServer.get() ? "Running as FTP Server (Sender)" : "Run as FTP Server (Sender)", runningFtpServer);

    State<Boolean> runningFtpClient = State.of(false);
    ComputedState<String> ftpClientText = ComputedState.of(() -> runningFtpClient.get() ? "Running as FTP Client (Receiver)" : "Run as FTP Client (Receiver)", runningFtpClient);

    State<Boolean> hasError = State.of(false);
    State<String> errorText = State.of(null);

    ListState<File> filesReceived = ListState.ofEmpty();

    @Override
    public Component render() {
        ForEachState<File, Text> forEachState = ForEachState.of(
                filesReceived,
                it -> new Text(it.getName())
        );

        return new Container(new ContainerProps().paddingAll(20))
                .children(
                        new Row(new RowProps().spacingOf(10)).children(
                                new Button(ftpServerText).onClick(()-> runningFtpServer.set(true)),
                                new Button(ftpClientText).onClick(this::handleRunAsFtpClient)
                        ),
                        Show.when(hasError, ()-> new Text(errorText)),
                        new SpacerVertical(10),
                        Show.when(runningFtpServer, ()->
                            new Column().children(
                                    Components.form(),
                                    new SpacerVertical(10),
                                    Components.dragFileUi(filesReceived)
                                    )
                        ),
                        new Column(new ColumnProps().centerHorizontally()).items(forEachState)
                );
    }

    void handleRunAsFtpClient() {
        hasError.set(false);
        errorText.set(null);

        if (!runningFtpClient.get()) {
            try {
                ftpServer.startWithDefaultCredentials();
                runningFtpClient.set(true);
            } catch (Exception e) {
                hasError.set(true);
                errorText.set(e.getMessage());
                throw new RuntimeException(e);
            }
        } else {
            ftpServer.stopServer();
            runningFtpClient.set(false);
        }
    }
}
