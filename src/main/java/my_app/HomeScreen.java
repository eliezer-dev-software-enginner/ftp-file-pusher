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

    State<Boolean> sending = State.of(false);
    State<String> sendStatus = State.of(null);

    // States do formulário (precisam ser acessíveis aqui também)
    State<String> ftpServerHost = new State<>("192.168.3.104");
    State<String> ftpPort       = new State<>("2221");
    State<String> ftpUsername   = new State<>("android");
    State<String> ftpPassword   = new State<>("android");

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
                        Show.when(runningFtpServer, () ->
                                new Column().children(
                                        Components.form(formState),
                                        new SpacerVertical(10),
                                        Components.dragFileUi(filesReceived),
                                        new SpacerVertical(10),
                                        new Button("Enviar arquivos").onClick(this::handleSendFiles),
                                        Show.when(sending, () -> new Text("Enviando...")),
                                        Show.when(sendStatus, s -> s != null, () -> new Text(sendStatus))
                                )
                        ),
                        new Column(new ColumnProps().centerHorizontally()).items(forEachState)
                );
    }

    void handleSendFiles() {
        if (filesReceived.get().isEmpty()) {
            hasError.set(true);
            errorText.set("Nenhum arquivo selecionado.");
            return;
        }

        sending.set(true);
        hasError.set(false);
        sendStatus.set(null);

        // FTP é bloqueante — roda em background thread
        Thread.ofVirtual().start(() -> {
            try {
                var config = new FtpClient.FtpConfig(
                        ftpServerHost.get(),
                        Integer.parseInt(ftpPort.get()),
                        ftpUsername.get(),
                        ftpPassword.get()
                );
                FtpClient.sendFiles(config, filesReceived.get());

                javafx.application.Platform.runLater(() -> {
                    sendStatus.set("Arquivos enviados com sucesso!");
                    sending.set(false);
                });

            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    hasError.set(true);
                    errorText.set("Erro ao enviar: " + e.getMessage());
                    sending.set(false);
                });
            }
        });
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
