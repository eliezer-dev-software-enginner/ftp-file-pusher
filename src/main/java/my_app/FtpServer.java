package my_app;

import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.SaltedPasswordEncryptor;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.WritePermission;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Servidor FTP simples usando Apache FtpServer.
 * ---------------------------------------------------------
 *
 * Uso:
 *   java FtpServer <pasta_destino> [porta] [usuario] [senha]
 *
 * Exemplos:
 *   java FtpServer /home/user/ftp_files
 *   java FtpServer C:\ftp_uploads 2121 admin 1234
 */
public class FtpServer {

    // Valores padrão
    private static final int    DEFAULT_PORT     = 21;
    private static final String DEFAULT_USER     = "win";
    private static final String DEFAULT_PASSWORD = "win";

    private static final String DEFAULT_FOLDER =
            System.getProperty("user.home") + "/Documents/ftp_uploads";

    private String folder;
    private org.apache.ftpserver.FtpServer server;

    public FtpServer() {
        this.folder = DEFAULT_FOLDER;
    }

    public FtpServer(String folder){
        this.folder = folder;
    }

    public void connectWithCredentials(String username, String password) throws FtpException{
        // ── 2. Garante que a pasta de destino existe ───────────────────────────
        File pastaArquivos = new File(this.folder);
        if (!pastaArquivos.exists()) {
            if (pastaArquivos.mkdirs()) {
                System.out.println("[INFO] Pasta criada: " + pastaArquivos.getAbsolutePath());
            } else {
                System.err.println("[ERRO] Não foi possível criar a pasta: " + this.folder);
                System.exit(1);
            }
        }

        // ── 3. Configura o gerenciador de usuários ─────────────────────────────
        PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
        userManagerFactory.setPasswordEncryptor(new SaltedPasswordEncryptor());

        // Cria o usuário com permissão de escrita na pasta de destino
        BaseUser ftpUser = new BaseUser();
        ftpUser.setName(username);
        ftpUser.setPassword(password);
        ftpUser.setHomeDirectory(pastaArquivos.getAbsolutePath());
        ftpUser.setEnabled(true);

        // Permissão de leitura e ESCRITA (necessário para receber arquivos)
        List<Authority> authorities = new ArrayList<>();
        authorities.add(new WritePermission());
        ftpUser.setAuthorities(authorities);

        var userManager = userManagerFactory.createUserManager();
        userManager.save(ftpUser);

        // ── 4. Configura o listener (porta e modo passivo) ─────────────────────
        ListenerFactory listenerFactory = new ListenerFactory();
        listenerFactory.setPort(DEFAULT_PORT);

        // Modo passivo: define faixa de portas para transferência de dados.
        // Ajuste conforme necessário para seu firewall.
        // listenerFactory.setDataConnectionConfiguration(
        //     new DataConnectionConfigurationFactory().createDataConnectionConfiguration()
        // );

        // ── 5. Monta e inicia o servidor ───────────────────────────────────────
        FtpServerFactory serverFactory = new FtpServerFactory();
        serverFactory.setUserManager(userManager);
        serverFactory.addListener("default", listenerFactory.createListener());

        var server = serverFactory.createServer();
        this.server = server;
        server.start();

        System.out.println("[OK] Servidor FTP rodando na porta " + DEFAULT_PORT);
        System.out.println("[OK] Arquivos recebidos serão salvos em: " + pastaArquivos.getAbsolutePath());

        // ── 6. Mantém o processo vivo e trata desligamento gracioso ───────────
        Runtime.getRuntime().addShutdownHook(new Thread(this::stopServer));
    }

    public void stopServer() {
        System.out.println("\n[INFO] Encerrando servidor FTP...");
        server.stop();
        System.out.println("[INFO] Servidor encerrado.");
    }

    public void startWithDefaultCredentials() throws FtpException {
        this.connectWithCredentials(DEFAULT_USER, DEFAULT_PASSWORD);
    }

    public static void main() throws FtpException {

       var server = new FtpServer("ftp_uploads");
        server.startWithDefaultCredentials();
    }
}