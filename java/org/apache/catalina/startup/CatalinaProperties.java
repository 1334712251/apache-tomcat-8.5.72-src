/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.catalina.startup;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;


/**
 * Utility class to read the bootstrap Catalina configuration.
 *
 * @author Remy Maucherat
 */
public class CatalinaProperties {

    private static final Log log = LogFactory.getLog(CatalinaProperties.class);

    private static Properties properties = null;


    static {
        loadProperties();
    }


    /**
     * @param name The property name
     * @return specified property value
     */
    public static String getProperty(String name) {
        return properties.getProperty(name);
    }


    /**
     *
     * 将catalina.properties里的属性设置为系统属性
     *
     * Load properties.
     */
    private static void loadProperties() {

        InputStream is = null;
        try {
            String configUrl = System.getProperty("catalina.config");
            //System.out.println("uuuuuuuuuuu------>"+configUrl);
            //uuuuuuuuuuu------>null
            if (configUrl != null) {
                is = (new URL(configUrl)).openStream();
            }
        } catch (Throwable t) {
            handleThrowable(t);
        }

        ////为空，执行这里
        if (is == null) {
            try {
                //这里读取JVM设置的参数配置的地址home目录
                File home = new File(Bootstrap.getCatalinaBase());
                File conf = new File(home, "conf");
                File propsFile = new File(conf, "catalina.properties");
//                String path = propsFile.getPath();
//                System.out.println("oooooooooooooo_______>>"+path);
                //oooooooooooooo_______>>F:\IdeaProjects\apache-tomcat-8.5.72-src\home\conf\catalina.properties
                is = new FileInputStream(propsFile);
            } catch (Throwable t) {
                handleThrowable(t);
            }
        }

        if (is == null) {
            try {
                is = CatalinaProperties.class.getResourceAsStream
                    ("/org/apache/catalina/startup/catalina.properties");
            } catch (Throwable t) {
                handleThrowable(t);
            }
        }

        //在执行这里，不为空
        if (is != null) {
            try {
                //创建一个Properties对象
                properties = new Properties();
                //加载里面的属性
                properties.load(is);
            } catch (Throwable t) {
                handleThrowable(t);
                log.warn(t);
            } finally {
                try {
                    is.close();
                } catch (IOException ioe) {
                    log.warn("Could not close catalina.properties", ioe);
                }
            }
        }

        if ((is == null)) {
            // Do something
            log.warn("Failed to load catalina.properties");
            // That's fine - we have reasonable defaults.
            properties = new Properties();
        }

        // Register the properties as system properties
        //将properties的属性注册为系统属性
        Enumeration<?> enumeration = properties.propertyNames();
        while (enumeration.hasMoreElements()) {
            String name = (String) enumeration.nextElement();
            String value = properties.getProperty(name);
            if (value != null) {
                System.setProperty(name, value);
            }
        }
    }


    // Copied from ExceptionUtils since that class is not visible during start
    private static void handleThrowable(Throwable t) {
        if (t instanceof ThreadDeath) {
            throw (ThreadDeath) t;
        }
        if (t instanceof VirtualMachineError) {
            throw (VirtualMachineError) t;
        }
        // All other instances of Throwable will be silently swallowed
    }
}
