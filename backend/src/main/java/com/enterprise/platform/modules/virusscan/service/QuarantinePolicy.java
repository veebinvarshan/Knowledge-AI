package com.enterprise.platform.modules.virusscan.service;

import com.enterprise.platform.modules.virusscan.domain.ScanJob;
import com.enterprise.platform.modules.virusscan.provider.VirusScanResult;

public interface QuarantinePolicy {
    void execute(ScanJob scanJob, VirusScanResult result);
}
