package my_app;

import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import megalodonte.ListState;
import megalodonte.State;
import megalodonte.base.components.Component;
import megalodonte.components.Text;
import megalodonte.components.inputs.Input;
import megalodonte.components.layout_components.Column;
import megalodonte.components.layout_components.Container;
import megalodonte.components.layout_components.Row;
import megalodonte.props.ColumnProps;
import megalodonte.props.InputProps;
import megalodonte.props.RowProps;
import megalodonte.props.TextProps;

import java.io.File;
import java.util.List;

public class Components {

    public static Component form(FtpFormState state) {
        return new Column(new ColumnProps().spacingOf(10))
                .c_child(new Text("FTP Configuration", new TextProps().fontSize(17)))
                .c_child(new Row(new RowProps().spacingOf(10))
                        .children(
                                InputColumn("Server",   state.host()),
                                InputColumn("Port",     state.port()),
                                InputColumn("Username", state.username()),
                                InputColumn("Password", state.password())
                        )
                );
    }

    private static Column InputColumn(String label, State<String> inputState) {
        return new Column()
                .children(
                        new Text(label, new TextProps().fontSize(14)),
                        new Input(inputState, new InputProps().fontSize(17)
                                .placeHolder(label).borderWidth(1).borderColor("gray"))
                );
    }


    /*
    setOnDragOver → permite o drop
    setOnDragDropped → recebe os arquivos
    */
    public  static Component dragFileUi(ListState<File> filesReceived){
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
}
