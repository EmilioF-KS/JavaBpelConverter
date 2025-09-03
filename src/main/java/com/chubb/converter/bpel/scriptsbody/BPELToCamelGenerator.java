package com.chubb.converter.bpel.scriptsbody;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.main.Main;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import com.chubb.location.library.*;
import com.chubb.location.library.interfaces.SoapClientInterface;

public class BPELToCamelGenerator {

	private static final String COMPLETE_PATH = "C:\\CHUBB\\LocationServices\\LOC2X3\\"; 

    public static void main(String[] args) throws Exception {
    	System.out.println("-> BPELToCamelGenerator.main");
    	
    	String bpelFile = "AddressStandardizationLOC2X3Process.bpel";
        String bpelFilePath = COMPLETE_PATH + bpelFile;
    	List<String> invokes = BPELParser.extractInvokes(bpelFilePath);
        //System.out.println("-> BPELToCamelGenerator invokes : " + invokes);
        
        String wsOriginalName = BPELParser.extractWsName(bpelFilePath);
        System.out.println("---> wsOriginalName : " + wsOriginalName);
        String wsName = "com.chubb.location.library." + wsOriginalName + "Client";
        System.out.println("---> wsName : " + wsName);
        
        String requestName = BPELParser.extractWsRequestName(bpelFilePath, wsOriginalName);
        System.out.println("---> requestName : " + requestName);
        
        String outputName = BPELParser.extractOutputsName(bpelFilePath);
        System.out.println("---> outputName : " + outputName);
        
        try {
            // Load the class by name
            Class<?> clazz = Class.forName(wsName);

            // Create a new instance (assumes a no-arg constructor)
            Object instance = clazz.getDeclaredConstructor().newInstance();
            System.out.println("---> instance : " + instance);
            
            //String xsdClass = "../" + wsOriginalName + "/" + requestName + ".xsd";
            String xsdClass = BpelUtils.getBpelLocationFormat(wsOriginalName, requestName);
            System.out.println("----> xsdClass : " + xsdClass);
            String namespaceToClass = BPELParser.extractXsdName(bpelFilePath, xsdClass);
            System.out.println("----> namespaceToClass : " + namespaceToClass);
                        
            String fullyQualifiedClass = BpelUtils.getBpelFullQualifiedPojo(namespaceToClass, requestName);
            System.out.println("----> fullyQualifiedClass : " + fullyQualifiedClass);
            
            Class<?> clazzReq = Class.forName(fullyQualifiedClass);

            // Create a new instance (assumes a no-arg constructor)
            Object instanceReq = clazzReq.getDeclaredConstructor().newInstance();
            System.out.println("---> instanceReq : " + instanceReq);
            
            String xsdClassOutput = BpelUtils.getBpelLocationFormat(wsOriginalName, outputName);
            System.out.println("-----> xsdClassOutput : " + xsdClassOutput);
            String namespaceToOutputClass = BPELParser.extractXsdName(bpelFilePath, xsdClassOutput);
            System.out.println("-----> namespaceToOutputClass : " + namespaceToOutputClass);
            String fullyQualifiedOutputClass = BpelUtils.getBpelFullQualifiedPojo(namespaceToOutputClass, outputName);
            System.out.println("----> fullyQualifiedOutputClass : " + fullyQualifiedOutputClass);
            
            Class<?> clazzRes = Class.forName(fullyQualifiedOutputClass);

            // Create a new instance (assumes a no-arg constructor)
            Object instanceRes = clazzRes.getDeclaredConstructor().newInstance();
            System.out.println("---> instanceRes : " + instanceRes);

            if (instance instanceof SoapClientInterface) {
            	instanceRes = ((SoapClientInterface) instance).invokeService(instanceReq);
            	System.out.println("---> final instanceRes : " + instanceRes);
            }

        } catch (ClassNotFoundException |
                 InstantiationException |
                 IllegalAccessException |
                 NoSuchMethodException |
                 InvocationTargetException e) {
            e.printStackTrace();
        }

        
        AddressStandardizationLOC2X3BClient addressStandardizationLOC2X3BClient = new AddressStandardizationLOC2X3BClient();
        //addressStandardizationLOC2X3BClient.invokeService();
        
        Main main = new Main();
        main.configure().addRoutesBuilder(new RouteBuilder() {
            @Override
            public void configure() {
            	System.out.println("-> BPELToCamelGenerator.configure");
            	
                for (String invoke : invokes) {
                	System.out.println("-> invoke : " + invoke);
                    String[] parts = invoke.split("\\.");
                    System.out.println("-> parts : " + parts);
                    
                    String partner = parts[0];
                    String operation = parts[1];
                    
                    System.out.println("--> timer://" + operation + "?repeatCount=1");
                    System.out.println("--> setBody : " + "Calling " + operation + " on " + partner);
                    System.out.println("--> to : " + "log:" + operation);

                    from("timer://" + operation + "?repeatCount=1")
                        .setBody(constant("Calling " + operation + " on " + partner))
                        .to("log:" + operation);
                }
            }
        });

        //main.run();
    }
}
