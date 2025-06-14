    package DNS;

    import javax.imageio.IIOException;
    import java.io.FileInputStream;
    import java.io.FileNotFoundException;
    import java.io.IOException;
    import java.util.*;
    import java.util.Properties;
    import java.util.HashMap;

    public class ConfigLoader {
        private final Properties   props = new Properties();
        private final Map<String, String> dnsRecords = new HashMap<>();
        private int port ;
        private int threadPoolSize = 10;
        private  String path;

        public  ConfigLoader(String filepath){
            try {
                FileInputStream in = new FileInputStream(filepath);
                this.path = filepath;
                try{
                props.load(in);
                props.stringPropertyNames().forEach( key ->{
                    String value =props.getProperty(key);
                    if(key.equals("port")){
                        this.port = Integer.parseInt(value);
                    }
                    else if(key.equals("threadPoolSize")){
                        this.threadPoolSize = Integer.parseInt(value);
                    }
                    else{
                            dnsRecords.put(key,value);
                    }
                });
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } catch (FileNotFoundException  e) {
                throw new RuntimeException(e);
            }
        }

        public  int getPort(){
            return port;
        }

        public Map<String, String> getDnsRecords() {
            return dnsRecords;
        }
        public int getThreadPoolSize(){
            return threadPoolSize;
        }
        public String getFilePath(){
            return path;
        }
        public  static void   main(String[] args){

        }
    }
