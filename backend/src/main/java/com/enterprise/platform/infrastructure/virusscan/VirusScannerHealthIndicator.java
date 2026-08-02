package com.enterprise.platform.infrastructure.virusscan;

import com.enterprise.platform.core.config.properties.ClamAvProperties;
import com.enterprise.platform.core.config.properties.VirusScanProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.net.Socket;

@Component("virusScannerHealthIndicator")
public class VirusScannerHealthIndicator implements HealthIndicator {

    private final ClamAvProperties clamAvProperties;
    private final VirusScanProperties virusScanProperties;

    public VirusScannerHealthIndicator(ObjectProvider<ClamAvProperties> clamAvPropertiesProvider, VirusScanProperties virusScanProperties) {
        this.clamAvProperties = clamAvPropertiesProvider.getIfAvailable(() -> new ClamAvProperties("localhost", 3310, 2000, 5000));
        this.virusScanProperties = virusScanProperties;
    }

    @Override
    public Health health() {
        if (!virusScanProperties.enabled()) {
            return Health.up().withDetail("status", "Disabled by configuration").build();
        }

        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(clamAvProperties.host(), clamAvProperties.port()), 1500);
            return Health.up()
                    .withDetail("provider", "ClamAV")
                    .withDetail("host", clamAvProperties.host())
                    .withDetail("port", clamAvProperties.port())
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("provider", "ClamAV")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
