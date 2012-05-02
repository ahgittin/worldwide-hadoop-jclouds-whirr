package demo.jclouds;

import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.jclouds.Constants;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.ComputeServiceContextFactory;
import org.jclouds.compute.RunNodesException;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.Template;
import org.jclouds.compute.domain.TemplateBuilder;
import org.jclouds.logging.slf4j.config.SLF4JLoggingModule;
import org.jclouds.sshj.config.SshjSshClientModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;

/** creates machines in a given cloud using jclouds
 * <p>
 * requires:
 * -Djclouds.aws-ec2.identity=ABC
 * -Djclouds.aws-ec2.credential=d3F 
 * (or comparable for other cloud)
 */
public class CreateMachines {

    public static final Logger log = LoggerFactory.getLogger(CreateMachines.class);

    public static final String DEFAULT_PROVIDER = "aws-ec2";

    public static ComputeServiceContextFactory computeServiceFactory = new ComputeServiceContextFactory();
    private static Map<Properties,ComputeService> cachedComputeServices = new ConcurrentHashMap<Properties,ComputeService> ();

    public static String getProperty(String key) {
        Properties properties = System.getProperties();
        if (!properties.containsKey(key))
            throw new IllegalStateException("Missing required value for '"+key+"'");
        return (String)properties.get(key);
    }

    public static ComputeService getComputeService(String provider) {
        return getComputeService(provider, null);
    }
    public static ComputeService getComputeService(String provider, Map propertiesSupplied) {
        Properties properties = new Properties();
        if (propertiesSupplied!=null) properties.putAll(properties);

        properties.setProperty(Constants.PROPERTY_IDENTITY, getProperty("jclouds."+provider+".identity"));
        properties.setProperty(Constants.PROPERTY_CREDENTIAL, getProperty("jclouds."+provider+".credential"));
        properties.setProperty(Constants.PROPERTY_TRUST_ALL_CERTS, Boolean.toString(true));
        properties.setProperty(Constants.PROPERTY_RELAX_HOSTNAME, Boolean.toString(true));

        // cache the services for re-use
        ComputeService result = cachedComputeServices.get(properties);
        if (result!=null) {
            log.debug("jclouds ComputeService cache hit for compute service, for "+properties);
            return result;
        }
        log.debug("jclouds ComputeService cache miss for compute service, creating, for "+properties);

        Iterable<Module> modules = ImmutableSet.<Module> of(new SshjSshClientModule(), new SLF4JLoggingModule());

        // make new service
        ComputeService computeService = computeServiceFactory
                .createContext(provider, modules, properties)
                .getComputeService();

        synchronized (cachedComputeServices) {
            result = cachedComputeServices.get(properties);
            if (result!=null) {
                log.debug("jclouds ComputeService cache recovery for compute service, for "+properties);
                // race - keep the old one, discard the new one
                computeService.getContext().close();
                return result;
            }
            log.debug("jclouds ComputeService created "+computeService+", adding to cache, for "+properties);
            cachedComputeServices.put(properties, computeService);
        }
        return computeService;
    }

    public Set<? extends NodeMetadata> createMachines(String provider, String location, int count) throws RunNodesException {
        
        ComputeService computeService = getComputeService(provider);
        TemplateBuilder templateBuilder = computeService.templateBuilder();
        if (location!=null) templateBuilder.locationId(location);
        
        Template template = templateBuilder.build();
        template.getOptions().inboundPorts(22, 80, 8020, 8021, 9000, 9001, 50030, 50070);
        
        String groupId = "worldwide-hadoop-demo-"+System.getProperty("user.name");
        
        Set<? extends NodeMetadata> nodes = computeService.createNodesInGroup(groupId, count, template);
        
        return nodes;
    }

    public static void info(String message) {
        log.info(message);
//        System.out.println(message);
    }
    
    public static void main(String[] args) throws Exception {
        CreateMachines machines = new CreateMachines();
        Set<? extends NodeMetadata> nodes = machines.createMachines(DEFAULT_PROVIDER, null, 2);
        
        
        info("Using key:\n"+nodes.iterator().next().getCredentials().getPrivateKey()+"\n");
        info("Created machines:");
        for (NodeMetadata node: nodes) {
            info("* "+node.getCredentials().getUser()+" @ "+node.getPublicAddresses());
        }
    }
    
}
