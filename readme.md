# Hello World Protocol Adapter

This repository is meant to reduce the friction of implementing your own protocol adapter by providing a functioning frame. 

## Quick Start to implement your own polling protocol adapter
In the following, we will create an simple Protocol Adapter that polls data from an endpoint.

1. Check the setup of the project: Run the gradle task `shadowJar` via the IDE or via command line in the projects root: `./gradlew shadowJar`. 
There should be a jar named `hivemq-hello-world-protocol-adapter-<version>-all.jar` in the `build/libs/`. 
This way you can make sure that the setup of the project on your machine works before changing things and potentially breaking it doing so.
2. Create a very simple implementation of your protocol adapter by editing `MTSICSPollingProtocolAdapter`. The `poll()` method is the key method, which contains the actual code that is called regularly by HiveMQ Edge to gather the data. 
Data points can be added on the method's output parameter via `addDataPoint()`. 
When all data points are added, signal this with the `finish()` on the output object. 
The values of the data points are then parsed into json. For native types, no further work is needed. If your value is a complex type with several fields, you need to tell HiveMQ Edge how to parse it into json. 
This is done using the Jackson framework and its annotations.
After (re)starting HiveMQ Edge, it should be visible in the protocol adapters section in the UI. You can create your adapter and test if it is working by subscribing to the mqtt topic you selected when creating your adapter in the ui.

3. Most likely the default config of the `MTSICSProtocolAdapter` is not enough. You can add further config in the `MTSICSAdapterConfig` class which uses the Jackson framework for annotation and conversion of the config of the adapter. 

4. In the `MTSICSProtocolAdapterInformation` you can adapt the information on your protocol adapter, e.g. a custom name, description or logo for your protocol adapter. 
This will be displayed in the UI. If you change the protocolId, this change also must be done in the config.xml of HiveMQ Edge, as this is the mapping between the configuration and the adapter.
5. Rename your classes and the project name
6. Create your adapter.jar the same way as in `1.`
7. Add your adapter to the modules folder of your HiveMQ Edge installation
8. Start Egde and browse the UI to start your adapter.


## How adapters are loaded

The adapters are loaded via Java's service loading mechanism. This means the exposed service class needs to be declared
in the file `src/main/resources/META-INF/services/com.hivemq.edge.modules.api.adapters.ProtocolAdapterFactory`. The file
name is the interface class and the content of file is the full-qualified class name of the implementation,
e.g. `com.hivemq.edge.adapters.MTSICS.MTSICSProtocolAdapterFactory`. This is correctly set up in this repository and IDE should reflect renamings of the implementation correctly. Nevertheless a sanity check that the content of this file is correctly set to your implementation makes sense.

## How to compile an adapter jar

The shadow plugin to create a jar with all needed dependencies is set up in this repository. The jar can be created via the gradle task `shadowJar`. The task can be executed via an IDE with gradle integration or by executing the task from the command line from the root folder of this project: `./gradlew shadowJar`. The jar is located after successful execution in `build/libs/`. 
Per default the jar file will be named `hivemq-hello-world-protocol-adapter-<version>-all.jar`. 

## Important classes

### ProtocolAdapterFactory (MTSICSProtocolAdapterFactory)

This class is loaded by HiveMQ Edge via the service loader framework of Java. It supplies Edge with Information on the adapter, config and how the actual adapter instance is constructed.

### AdapterInformation (MTSICSProtocolAdapterInformation)

This class contains all information on the adapter like name, protocol, description and much more. The java doc of each method explains its purpose.

### Protocol Adapter (MTSICSProtocolAdapter)

This class contains the actual implementation of the protocol adapter. The most interesting method is the poll() method, in which the actual data gathering is done. 


### AdapterConfig (MTSICSAdapterConfig)

The config class uses the Jackson framework to define the fields of the config. This way, the config will automatically be parsed from the input from the ui.

## How to change the adapter icon

In order to change the icon for the adapter in the ui, you need to place the new image
in `src/main/resources/httpd/images/`and adapt the icon path in the class that extends `AdapterInformation`, which
is `MTSICSProtocolAdapterInformation` in this project:

```
public class MTSICSProtocolAdapterInformation extends AbstractProtocolAdapterInformation {
    ...

    @Override
    public @NotNull String getLogoUrl() {
        return "/images/MTSICS.png";
    }
    
    ...
}
```
