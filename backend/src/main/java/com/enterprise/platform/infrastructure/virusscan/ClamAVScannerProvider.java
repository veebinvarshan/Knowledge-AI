package com.enterprise.platform.infrastructure.virusscan;

import com.enterprise.platform.core.config.properties.ClamAvProperties;
import com.enterprise.platform.modules.virusscan.domain.ScanJobStatus;
import com.enterprise.platform.modules.virusscan.provider.VirusScanResult;
import com.enterprise.platform.modules.virusscan.provider.VirusScanner;
import com.enterprise.platform.modules.virusscan.provider.VirusScannerProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

public class ClamAVScannerProvider implements VirusScanner, VirusScannerProvider {

    private static final Logger log = LoggerFactory.getLogger(ClamAVScannerProvider.class);

    private final ClamAvProperties properties;

    public ClamAVScannerProvider(ClamAvProperties properties) {
        this.properties = properties;
    }

    @Override
    public String getName() {
        return "CLAMAV";
    }

    @Override
    public VirusScanner getScanner() {
        return this;
    }

    @Override
    public VirusScanResult scan(InputStream inputStream) throws Exception {
        long start = System.currentTimeMillis();
        long totalBytes = 0;

        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(properties.host(), properties.port()), properties.connectionTimeoutMs());
            socket.setSoTimeout(properties.readTimeoutMs());

            try (OutputStream out = socket.getOutputStream();
                 InputStream in = socket.getInputStream()) {

                // Send INSTREAM command
                out.write("nINSTREAM\n".getBytes(StandardCharsets.UTF_8));
                out.flush();

                byte[] buffer = new byte[8192];
                int read;
                while ((read = inputStream.read(buffer)) != -1) {
                    // Write chunk size as 4-byte big-endian integer
                    out.write(ByteBuffer.allocate(4).putInt(read).array());
                    // Write chunk bytes
                    out.write(buffer, 0, read);
                    totalBytes += read;
                }

                // Write empty chunk size to signal EOF to clamd
                out.write(new byte[]{0, 0, 0, 0});
                out.flush();

                // Read scan response line
                BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
                String response = reader.readLine();

                long duration = System.currentTimeMillis() - start;

                if (response == null) {
                    throw new IOException("Empty response received from ClamAV");
                }

                response = response.trim();
                log.debug("ClamAV socket response: {}", response);

                if (response.endsWith("OK")) {
                    return new VirusScanResult(ScanJobStatus.CLEAN, "ClamAV", "1.0", null, Instant.now(), duration, totalBytes);
                } else if (response.contains("FOUND")) {
                    String signature = extractSignature(response);
                    return new VirusScanResult(ScanJobStatus.INFECTED, "ClamAV", "1.0", signature, Instant.now(), duration, totalBytes);
                } else {
                    throw new IOException("ClamAV scan failed with unexpected response: " + response);
                }
            }
        }
    }

    private String extractSignature(String response) {
        String prefix = "stream:";
        String suffix = "FOUND";
        int start = response.indexOf(prefix);
        int end = response.lastIndexOf(suffix);
        if (start != -1 && end != -1 && end > start + prefix.length()) {
            return response.substring(start + prefix.length(), end).trim();
        }
        return "Unknown-Signature";
    }
}
