package demo.whirr;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.whirr.Cluster;
import org.apache.whirr.ClusterController;
import org.apache.whirr.ClusterControllerFactory;
import org.apache.whirr.ClusterSpec;

public class WhirrHadoop {

    protected ClusterController controller = null;
    protected ClusterSpec clusterSpec = null;
    protected Cluster cluster = null;

    public static final URL RECIPE = WhirrHadoop.class.getClassLoader().getResource("whirr-hadoop.properties");
    
    public void run() throws ConfigurationException, IOException, InterruptedException {
        PropertiesConfiguration config = new PropertiesConfiguration();
        for (Map.Entry entry: System.getenv().entrySet())
            config.setProperty((String)entry.getKey(), (String)entry.getValue());
        config.load(RECIPE.openStream());
        
        clusterSpec = new ClusterSpec(config);
        
        controller = new ClusterControllerFactory().create(null);
        cluster = controller.launchCluster(clusterSpec);
    }
    
    public static void main(String[] args) throws Exception {
        WhirrHadoop wh = new WhirrHadoop();
        wh.run();
        
        System.out.println("Your cluster is ready.");
    }

}
