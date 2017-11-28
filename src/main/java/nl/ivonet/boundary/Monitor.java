/*
 * Copyright 2017 Ivo Woltring <WebMaster@ivonet.nl>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.ivonet.boundary;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import lombok.extern.slf4j.Slf4j;
import nl.ivonet.model.ClassLoading;
import nl.ivonet.model.Cpu;
import nl.ivonet.model.Memory;
import nl.ivonet.model.Runtime;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Properties;

/**
 * @author Ivo Woltring
 */
@Slf4j
public class Monitor {

    private static final String JMX_LOCAL_CONNECTOR_ADDRESS = "com.sun.management.jmxremote.localConnectorAddress";
    private final VirtualMachine virtualMachine;
    private final MBeanServerConnection mBeanServerConnection;


    public Monitor(final String pid) {
        try {
            this.virtualMachine = VirtualMachine.attach(pid);
        } catch (final AttachNotSupportedException | IOException e) {
            throw new IllegalStateException("Could not attach to vm.", e);
        }
        final String jmxUrl = getJmxUrl();
        final JMXServiceURL jmxServiceURL;
        try {
            jmxServiceURL = new JMXServiceURL(jmxUrl);
        } catch (final MalformedURLException e) {
            throw new IllegalStateException("Quit due to errors in JMXServiceURL", e);
        }
        try {
            final JMXConnector connector = JMXConnectorFactory.newJMXConnector(jmxServiceURL, null);
            connector.connect();
            this.mBeanServerConnection = connector.getMBeanServerConnection();
        } catch (final IOException e) {
            throw new IllegalStateException("Quit due to errors in the jmx connection", e);
        }
    }

    public static void printJvms() {
        final List<VirtualMachineDescriptor> vms = VirtualMachine.list();
        System.err.println("No parameter provided. Please provide a PID...");
        vms.forEach(virtualMachineDescriptor -> System.err.println(String.format("JVM pid = %s, Name = %s", virtualMachineDescriptor.id(), virtualMachineDescriptor.displayName())));
    }


    public void gc() {
        try {
            this.mBeanServerConnection.invoke(new ObjectName(ManagementFactory.MEMORY_MXBEAN_NAME), "gc", null, null);
        } catch (InstanceNotFoundException | MBeanException | ReflectionException | IOException | MalformedObjectNameException e) {
            throw new IllegalStateException("Could not invoke Garbage Collection.", e);
        }
    }

    public Memory heapMemoryUsage() {
        return new Memory((CompositeData) getAttribute(ManagementFactory.MEMORY_MXBEAN_NAME, "HeapMemoryUsage"));
    }

    public Memory nonHeapMemoryUsage() {
        return new Memory((CompositeData) getAttribute(ManagementFactory.MEMORY_MXBEAN_NAME, "NonHeapMemoryUsage"));
    }

    public Cpu cpu() {
        return new Cpu(processCpuTime(), processCpuLoad(), systemCpuLoad(), systemLoadAverage(), availableProcessors());
    }

    public ClassLoading classLoading() {
        return new ClassLoading(classLoadingVerbose(), totalLoadedClassCount(), loadedClassCount(), unloadedClassCount());
    }

    public Runtime runtime() {
        return new Runtime(rtName(),
                rtClassPath(),
                rtStartTime(),
                rtSystemProperties(),
                rtBootClassPathSupported(),
                rtVmName(),
                rtVmVendor(),
                rtVmVersion(),
                rtLibraryPath(),
                rtBootClassPath(),
                rtUptime(),
                rtManagementSpecVersion(),
                rtSpecName(),
                rtSpecVendor(),
                rtSpecVersion(),
                rtInputArguments());
    }

    private String rtName() {
        return (String) getAttribute(ManagementFactory.RUNTIME_MXBEAN_NAME, "Name");
    }

    private boolean rtBootClassPathSupported() {
        return (boolean) getAttribute(ManagementFactory.RUNTIME_MXBEAN_NAME, "BootClassPathSupported");
    }

    private String rtVmName() {
        return (String) getAttribute(ManagementFactory.RUNTIME_MXBEAN_NAME, "VmName");
    }

    private String rtLibraryPath() {
        return (String) getAttribute(ManagementFactory.RUNTIME_MXBEAN_NAME, "LibraryPath");
    }

    private String rtVmVendor() {
        return (String) getAttribute(ManagementFactory.RUNTIME_MXBEAN_NAME, "VmVendor");
    }

    private String rtVmVersion() {
        return (String) getAttribute(ManagementFactory.RUNTIME_MXBEAN_NAME, "VmVersion");
    }

    private String rtClassPath() {
        return (String) getAttribute(ManagementFactory.RUNTIME_MXBEAN_NAME, "ClassPath");
    }

    private String rtSpecVersion() {
        return (String) getAttribute(ManagementFactory.RUNTIME_MXBEAN_NAME, "SpecVersion");
    }

    private String rtSpecVendor() {
        return (String) getAttribute(ManagementFactory.RUNTIME_MXBEAN_NAME, "SpecVendor");
    }

    private String rtSpecName() {
        return (String) getAttribute(ManagementFactory.RUNTIME_MXBEAN_NAME, "SpecName");
    }

    private long rtUptime() {
        return (long) getAttribute(ManagementFactory.RUNTIME_MXBEAN_NAME, "Uptime");
    }

    private String rtManagementSpecVersion() {
        return (String) getAttribute(ManagementFactory.RUNTIME_MXBEAN_NAME, "ManagementSpecVersion");
    }

    private String rtBootClassPath() {
        return (String) getAttribute(ManagementFactory.RUNTIME_MXBEAN_NAME, "BootClassPath");
    }

    private String[] rtInputArguments() {
        //noinspection unchecked
        return (String[]) getAttribute(ManagementFactory.RUNTIME_MXBEAN_NAME, "InputArguments");
    }

    private long rtStartTime() {
        return (long) getAttribute(ManagementFactory.RUNTIME_MXBEAN_NAME, "StartTime");
    }

    private TabularData rtSystemProperties() {
        return (TabularData) getAttribute(ManagementFactory.RUNTIME_MXBEAN_NAME, "SystemProperties");
    }

    public void detach() {
        if (this.virtualMachine != null) {
            try {
                this.virtualMachine.detach();
            } catch (final IOException e) {
                throw new IllegalStateException(e);
            }
        }
    }


    private long unloadedClassCount() {
        return (long) getAttribute(ManagementFactory.CLASS_LOADING_MXBEAN_NAME, "UnloadedClassCount");
    }

    private int loadedClassCount() {
        return (int) getAttribute(ManagementFactory.CLASS_LOADING_MXBEAN_NAME, "LoadedClassCount");
    }

    private long totalLoadedClassCount() {
        return (long) getAttribute(ManagementFactory.CLASS_LOADING_MXBEAN_NAME, "TotalLoadedClassCount");
    }

    private boolean classLoadingVerbose() {
        return (boolean) getAttribute(ManagementFactory.CLASS_LOADING_MXBEAN_NAME, "Verbose");
    }

    private long processCpuTime() {
        return (long) getAttribute(ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME, "ProcessCpuTime");
    }

    private double processCpuLoad() {
        return (double) getAttribute(ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME, "ProcessCpuLoad");
    }

    private double systemCpuLoad() {
        return (double) getAttribute(ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME, "SystemCpuLoad");
    }

    private double systemLoadAverage() {
        return (double) getAttribute(ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME, "SystemLoadAverage");
    }

    private int availableProcessors() {
        return (int) getAttribute(ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME, "AvailableProcessors");
    }

    private String getJmxUrl() {
        String jmxUrl = readAgentProperty(JMX_LOCAL_CONNECTOR_ADDRESS);
        if (jmxUrl == null) {
            loadMangementAgent();
            jmxUrl = readAgentProperty(JMX_LOCAL_CONNECTOR_ADDRESS);
        }
        return jmxUrl;
    }

    private String readAgentProperty(final String propertyName) {
        try {
            return this.virtualMachine.getAgentProperties().getProperty(propertyName);
        } catch (final IOException e) {
            throw new IllegalStateException("Reading agent property failed", e);
        }
    }

    private void loadMangementAgent() {
        try {
            this.virtualMachine.loadAgent(readSystemProperty("java.home") + "/lib/management-agent.jar");
        } catch (final IOException | AgentLoadException | AgentInitializationException e) {
            throw new IllegalStateException("Could not load the management agent.", e);
        }
    }

    private String readSystemProperty(final String propertyName) {
        try {
            final Properties systemProperties = this.virtualMachine.getSystemProperties();
            return systemProperties.getProperty(propertyName);
        } catch (final IOException e) {
            throw new IllegalStateException("Reading system property failed", e);
        }
    }

    private Object getAttribute(final String objectName, final String attribute) {
        try {
            return this.mBeanServerConnection.getAttribute(new ObjectName(objectName), attribute);
        } catch (MBeanException | MalformedObjectNameException | IOException | ReflectionException | InstanceNotFoundException | AttributeNotFoundException e) {
            throw new IllegalStateException(String.format("Could not retrieve attribute %s from object %s", attribute, objectName));
        }
    }
}
