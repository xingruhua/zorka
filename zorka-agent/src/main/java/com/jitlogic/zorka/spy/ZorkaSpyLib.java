package com.jitlogic.zorka.spy;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.management.Attribute;

import com.jitlogic.zorka.agent.JmxObject;
import com.jitlogic.zorka.agent.ZorkaBshAgent;
import com.jitlogic.zorka.agent.ZorkaLib;
import com.jitlogic.zorka.mbeans.MethodCallStatisticImpl;
import com.jitlogic.zorka.mbeans.MethodCallStats;
import com.jitlogic.zorka.mbeans.ZorkaMappedMBean;
import com.jitlogic.zorka.util.ZorkaLogger;

public class ZorkaSpyLib {
	
	
	private static final ZorkaLogger log = ZorkaLogger.getLogger(ZorkaSpyLib.class);
	
	
	private ZorkaBshAgent agent;
	private ZorkaSpy spy;
	private ZorkaLib lib;
	
	
	public ZorkaSpyLib(ZorkaBshAgent agent) {
		this.agent = agent;
		this.lib = agent.getZorkaLib();
		spy = new ZorkaSpy();
	}
	
	
	private MethodCallStats getStats(String beanName, String attrName) {
		Object obj = lib.jmx("java", beanName, attrName);
		
		if (obj == null) {
			obj = new MethodCallStats();
			JmxObject jmxobj = (JmxObject)lib.jmx("java", beanName);
			try {
				if (jmxobj != null) {
					jmxobj.getConn().setAttribute(jmxobj.getName(), new Attribute(attrName, obj));
				} else {
					ZorkaMappedMBean bean = lib.mbean("java", beanName);
					bean.setAttribute(new Attribute(attrName, obj));
				}
			} catch (Exception e) {
				log.error("Error setting attribute '" + attrName + "' for '" + beanName, e);
				return null;
			}
		}
		
		if (! (obj instanceof MethodCallStats)) {
			log.error("Attribute '" + attrName + "' of '" + beanName + "' is not MethodCallStats object.");
		}
		
		return (MethodCallStats)obj;
	}

	
	public void simple(String className, String methodName, String beanName, String attrName) {
		MethodCallStats mcs = getStats(beanName, attrName);
		MethodCallStatisticImpl st = mcs.getMethodCallStat(methodName);
		DataCollector collector = new SingleMethodDataCollector(st);
		MethodTemplate mt = new MethodTemplate(className, methodName, null, collector);
		spy.addTemplate(mt);
	}
	
	
	public void byArgs(String className, String methodName, String beanName, String attrName, int arg1) {
		byArgv(className, methodName, beanName, attrName, arg1);
	}
	
	
	public void byArgs(String className, String methodName, String beanName, String attrName, int arg1, int arg2) {
		byArgv(className, methodName, beanName, attrName, arg1, arg2);
	}
	
	
	public void byArg(String className, String methodName, String beanName, String attrName, int arg1, int arg2, int arg3) {
		byArgv(className, methodName, beanName, attrName, arg1, arg2, arg3);
	}
	
	
	public void byArg(String className, String methodName, String beanName, String attrName, int arg1, int arg2, int arg3, int arg4) {
		byArgv(className, methodName, beanName, attrName, arg1, arg2, arg3, arg4);
	}

	
	public void byArg(String className, String methodName, String beanName, String attrName, int arg1, int arg2, int arg3, int arg4, int arg5) {
		byArgv(className, methodName, beanName, attrName, arg1, arg2, arg3, arg4, arg5);
	}
	
	
	private void byArgv(String className, String methodName, String beanName, String attrName, int...args) {
		MethodCallStats mcs = getStats(beanName, attrName);
		DataCollector collector = new MultiMethodDataCollector(mcs, ".", args);
		MethodTemplate mt = new MethodTemplate(className, methodName, null, collector);
		spy.addTemplate(mt);
	}
	
	
	public ZorkaSpy getSpy() {
		return spy;
	}
}