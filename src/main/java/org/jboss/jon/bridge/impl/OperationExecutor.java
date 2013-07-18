package org.jboss.jon.bridge.impl;

import java.util.List;
import java.util.Map;

import org.jboss.jon.bridge.ToCanonical;
import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.domain.configuration.PropertySimple;
import org.rhq.core.domain.criteria.ResourceOperationHistoryCriteria;
import org.rhq.core.domain.operation.JobId;
import org.rhq.core.domain.operation.OperationRequestStatus;
import org.rhq.core.domain.operation.ResourceOperationHistory;
import org.rhq.core.domain.operation.bean.ResourceOperationSchedule;
import org.rhq.core.domain.resource.Resource;
import org.rhq.enterprise.clientapi.RemoteClient;
import org.rhq.enterprise.server.operation.OperationManagerRemote;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class OperationExecutor {

  public static List<org.jboss.jon.bridge.model.Resource> operation(boolean sync, List<Resource> resources, String operation, String command, long timeoutInMs, RemoteClient rc){
    Configuration config=new Configuration();
    if (command!=null && command.length()>0)
      config.put(new PropertySimple("command", command));
    
    OperationManagerRemote omr=rc.getOperationManager();
    Map<Integer,org.jboss.jon.bridge.model.Resource> result=Maps.newHashMap();
    
    for(Resource resource:resources){
      int delay=0;
      int repeatInterval=0;
      int repeatCount=0;
      int timout=600;
      System.out.println("Executing '"+operation+"':'"+command+"' against resource "+ resource.getId()+ "(type:'"+resource.getResourceType()+")");
      // scheduleResourceOperation params = id, operation, delay, repeatInterval, repeat count, timeoutInMs, config, description
      ResourceOperationSchedule sch=omr.scheduleResourceOperation(rc.getSubject(), resource.getId(), operation/*"executePromptCommand"*/, delay, repeatInterval, repeatCount, timout, config, "attempting to force avail");
      result.put(resource.getId(), ToCanonical.toCanonical(resource));
      result.get(resource.getId()).setOperationJobId(sch.getJobId().getJobName() + ":"+sch.getJobId().getJobGroup());
    }
    if (!sync)
      return Lists.newArrayList(result.values());
    
    // sync... did they succeed?
    for(Resource resource:resources){
      ResourceOperationHistoryCriteria criteria=new ResourceOperationHistoryCriteria();
      String[] job=result.get(resource.getId()).getOperationJobId().split(":");
      criteria.addFilterJobId(new JobId(job[0], job[1]));
      
      OperationRequestStatus status=OperationRequestStatus.INPROGRESS;
      long theTimeout=System.currentTimeMillis()+timeoutInMs;
      while(status==OperationRequestStatus.INPROGRESS && System.currentTimeMillis()<theTimeout){
        List<ResourceOperationHistory> history=omr.findResourceOperationHistoriesByCriteria(rc.getSubject(), criteria);
        if (history.size()==1){
          status=history.get(0).getStatus();
          result.get(resource.getId()).setOperationStatus(status.name().toLowerCase());
        }
        try{Thread.sleep(500);}catch(Exception sink){}
      }
      if (status==OperationRequestStatus.INPROGRESS)
        result.get(resource.getId()).setOperationStatus("timeout");
    }
    return Lists.newArrayList(result.values());   
  }

  
}
