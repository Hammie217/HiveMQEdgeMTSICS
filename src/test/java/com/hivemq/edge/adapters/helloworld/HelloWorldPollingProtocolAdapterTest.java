package com.hivemq.edge.adapters.helloworld;

import com.hivemq.adapter.sdk.api.model.ProtocolAdapterInput;
import com.hivemq.adapter.sdk.api.polling.batch.BatchPollingInput;
import com.hivemq.edge.adapters.helloworld.config.HelloWorldAdapterConfig;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HelloWorldPollingProtocolAdapterTest {

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
            HelloWorldAdapterConfig config = mock(HelloWorldAdapterConfig.class);
            when(config.getIp()).thenReturn("localhost");
            when(config.getPort()).thenReturn(port);
            when(config.getPollingIntervalMillis()).thenReturn(1000);
            when(config.getMaxPollingErrorsBeforeRemoval()).thenReturn(5);

            ProtocolAdapterInput<HelloWorldAdapterConfig> adapterInput = mock(ProtocolAdapterInput.class);
            when(adapterInput.getConfig()).thenReturn(config);
            when(adapterInput.getAdapterId()).thenReturn("test-id");
            when(adapterInput.getProtocolAdapterState()).thenReturn(mock());

            BatchPollingInput pollingInput = mock(BatchPollingInput.class);

            // Output collector
            TestPollingOutput pollingOutput = new TestPollingOutput();

            // Adapter under test
            HelloWorldPollingProtocolAdapter adapter = new HelloWorldPollingProtocolAdapter(
                    new HelloWorldProtocolAdapterInformation(), adapterInput);

            adapter.start(null, mock());

            adapter.poll(pollingInput, pollingOutput);

            Object value = pollingOutput.getDataPoints().get("Weight");

            assertNotNull(value);
            assertTrue(value instanceof String);
            assertEquals("S S     5.400 kg", value);
        }
    }
}
