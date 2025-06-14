import DNS.ConfigLoader;
import DNS.DnsServer;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {

        ConfigLoader config = new ConfigLoader("src/config.properties");
        DnsServer server = new DnsServer(config);
        server.StartServer();
        server.run();
    }
}