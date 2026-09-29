package com.academy.lms.common.audit;

import com.academy.lms.common.api.PageResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/audit-logs")
@PreAuthorize("hasRole('ADMIN')")
public class AuditLogController {
  private final AuditLogRepository logs;

  public AuditLogController(AuditLogRepository logs) { this.logs = logs; }

  @GetMapping
  public PageResponse<AuditLogResponse> list(@RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "50") int size) {
    var pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100));
    return PageResponse.from(logs.findAllByOrderByCreatedAtDesc(pageable)
        .map(AuditLogResponse::from));
  }
}
