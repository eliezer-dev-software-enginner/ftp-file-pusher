package my_app;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class FtpClient {

    public record FtpConfig(String host, int port, String username, String password) {}

    public static void sendFiles(FtpConfig config, List<File> files) throws IOException {
        FTPClient client = new FTPClient();

        try {
            client.connect(config.host(), config.port());
            boolean loggedIn = client.login(config.username(), config.password());
            if (!loggedIn) {
                throw new IOException("Falha no login FTP: credenciais inválidas.");
            }

            client.enterLocalPassiveMode();
            client.setFileType(FTP.BINARY_FILE_TYPE);

            for (File file : files) {
                try (FileInputStream fis = new FileInputStream(file)) {
                    boolean sent = client.storeFile(file.getName(), fis);
                    if (!sent) {
                        throw new IOException("Falha ao enviar arquivo: " + file.getName());
                    }
                    System.out.println("[OK] Enviado: " + file.getName());
                }
            }

        } finally {
            if (client.isConnected()) {
                client.logout();
                client.disconnect();
            }
        }
    }
}