package my_app;

import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import megalodonte.ComputedState;
import megalodonte.ForEachState;
import megalodonte.ListState;
import megalodonte.State;
import megalodonte.base.components.Component;
import megalodonte.base.components.ScreenComponent;
import megalodonte.components.Button;
import megalodonte.components.SpacerHorizontal;
import megalodonte.components.SpacerVertical;
import megalodonte.components.Text;
import megalodonte.components.layout_components.Column;
import megalodonte.components.layout_components.Container;
import megalodonte.components.layout_components.Row;
import megalodonte.props.ColumnProps;
import megalodonte.props.RowProps;
import megalodonte.v2.Show;

import java.io.File;
import java.util.List;

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

        return new Container()
                .children(
                        new Row(new RowProps().spacingOf(10).paddingAll(20)).children(
                                new Button(ftpServerText).onClick(()-> runningFtpServer.set(true)),
                                new Button(ftpClientText).onClick(this::handleRunAsFtpClient)
                        ),
                        Show.when(hasError, ()-> new Text(errorText)),
                        new SpacerVertical(10),
                        Show.when(runningFtpServer, ()-> dragFileUi()),
                        new Column(new ColumnProps().centerHorizontally()).items(forEachState)
                );
    }

    /*
    setOnDragOver → permite o drop
    setOnDragDropped → recebe os arquivos
     */
    Component dragFileUi(){
        Label text = new Label("Arraste arquivos aqui");
        StackPane layout = new StackPane(text);

        layout.setOnDragOver(event -> {
            if (event.getGestureSource() != layout
                    && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(
                        javafx.scene.input.TransferMode.COPY
                );
            }
            event.consume();
        });

        layout.setOnDragDropped(event -> {
            var dragboard = event.getDragboard();
            boolean success = false;

            if (dragboard.hasFiles()) {
                List<File> files = dragboard.getFiles();
                filesReceived.set(files);
                success = true;
            }

            event.setDropCompleted(success);
            event.consume();
        });

        return new Container()
                .children(Component.CreateFromJavaFxNode(layout));
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
