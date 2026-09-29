package com.academy.lms.audit;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service public class AuditService { private final AuditLogRepository logs; public AuditService(AuditLogRepository logs){this.logs=logs;}
  @Transactional public void record(UUID actor,String action,String type,Object id,String ip){logs.save(new AuditLog(actor,action,type,id==null?null:id.toString(),ip));}
}
