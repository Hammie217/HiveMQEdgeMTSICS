/*
 * Copyright 2023-present HiveMQ GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hivemq.edge.adapters.MTSICS;

import com.hivemq.adapter.sdk.api.model.ProtocolAdapterInput;
import com.hivemq.adapter.sdk.api.polling.batch.BatchPollingInput;
import com.hivemq.edge.adapters.MTSICS.config.MTSICSAdapterConfig;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.List;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MTSICSPollingProtocolAdapterTest {

@Test
void test_poll_whenTcpServerResponds_thenResponseIsCapturedInOutput() throws Exception {
    // Set up a mock TCP server
    try (ServerSocket serverSocket = new ServerSocket(0)) {
        int port = serverSocket.getLocalPort();

        // Start the mock server in a separate thread
        Executors.newSingleThreadExecutor().submit(() -> {
            try (Socket clientSocket = serverSocket.accept();
                 BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                String request = in.readLine();
                if ("SI".equals(request)) {
                    out.println("S S     5.400 kg");
                } else {
                    out.println("Unexpected Command");
                }
            } catch (IOException e) {
                // Ignored for test
            }
        });

        // Mocks
        MTSICSAdapterConfig config = mock(MTSICSAdapterConfig.class);
        when(config.getIp()).thenReturn("localhost");
        when(config.getPort()).thenReturn(port);
        when(config.getPollingIntervalMillis()).thenReturn(1000);
        when(config.getMaxPollingErrorsBeforeRemoval()).thenReturn(5);

        ProtocolAdapterInput<MTSICSAdapterConfig> adapterInput = mock(ProtocolAdapterInput.class);
        when(adapterInput.getConfig()).thenReturn(config);
        when(adapterInput.getAdapterId()).thenReturn("test-id");
        when(adapterInput.getProtocolAdapterState()).thenReturn(mock());

        // Mock tag and its definition
        var tagDefinition = mock(com.hivemq.edge.adapters.MTSICS.tag.SICSTagDefinition.class);
        when(tagDefinition.getCommand()).thenReturn("SI");

        var sicsTag = mock(com.hivemq.edge.adapters.MTSICS.tag.SICSTag.class);
        when(sicsTag.getDefinition()).thenReturn(tagDefinition);
        when(sicsTag.getName()).thenReturn("Weight");

        when(adapterInput.getTags()).thenReturn(List.of(sicsTag));

        BatchPollingInput pollingInput = mock(BatchPollingInput.class);

        // Output collector
        TestPollingOutput pollingOutput = new TestPollingOutput();

        // Adapter under test
        MTSICSPollingProtocolAdapter adapter = new MTSICSPollingProtocolAdapter(
                new MTSICSProtocolAdapterInformation(), adapterInput);

        adapter.start(null, mock());

        adapter.poll(pollingInput, pollingOutput);

        Object value = pollingOutput.getDataPoints().get("Weight");

        assertNotNull(value);
        assertTrue(value instanceof String);
        assertEquals("S S     5.400 kg", value);
    }
}}
