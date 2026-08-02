package com.enterprise.platform.modules.virusscan.provider;

import java.io.InputStream;

public interface VirusScanner {
    VirusScanResult scan(InputStream inputStream) throws Exception;
}
