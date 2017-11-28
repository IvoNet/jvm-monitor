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

package nl.ivonet;

import nl.ivonet.boundary.Monitor;

/**
 * @author Ivo Woltring
 */
public class Main {


    public static void main(final String[] args) {
        if (args.length == 0) {
            Monitor.printJvms();
            System.exit(1);
        }
        final Monitor monitor = new Monitor(args[0]);

        monitor.gc();

        System.out.println("heapMemoryUsage = " + monitor.heapMemoryUsage());
        System.out.println("nonHeapMemoryUsage = " + monitor.nonHeapMemoryUsage());
        System.out.println("processCpuTime = " + monitor.cpu());
        System.out.println("classLoading = " + monitor.classLoading());
        System.out.println("monitor.runtime() = " + monitor.runtime());

//        System.out.println("{\"ts\":\"" + System.currentTimeMillis() + "\",\"hpmax\":\"" + cd.get("max") + "\",\"hpused\":\"" + cd.get("used") + "\",\"cpu\":\"" + osMbean + "\"}");
        monitor.detach();
    }
}
