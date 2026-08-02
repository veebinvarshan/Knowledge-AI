package com.enterprise.platform.modules.virusscan.provider;

public interface VirusScannerProvider {
    String getName();
    VirusScanner getScanner();
}
