/*
 * Copyright 2023-present HiveMQ GmbH
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.hivemq.edge.adapters.helloworld;

import com.hivemq.adapter.sdk.api.ProtocolAdapterInformation;
import com.hivemq.adapter.sdk.api.model.*;
import com.hivemq.adapter.sdk.api.polling.batch.BatchPollingInput;
import com.hivemq.adapter.sdk.api.polling.batch.BatchPollingOutput;
import com.hivemq.adapter.sdk.api.polling.batch.BatchPollingProtocolAdapter;
import com.hivemq.adapter.sdk.api.state.ProtocolAdapterState;
import com.hivemq.edge.adapters.helloworld.config.HelloWorldAdapterConfig;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.Console;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;


public class HelloWorldPollingProtocolAdapter implements BatchPollingProtocolAdapter {

    private final @NotNull HelloWorldAdapterConfig adapterConfig;
    private final @NotNull ProtocolAdapterInformation adapterInformation;
    private final @NotNull ProtocolAdapterState protocolAdapterState;
    private final @NotNull String adapterId;
    private @NotNull Socket socket;
    private @NotNull PrintWriter writer;
    private @NotNull BufferedReader reader;

    public HelloWorldPollingProtocolAdapter(
            final @NotNull ProtocolAdapterInformation adapterInformation,
            final @NotNull ProtocolAdapterInput<HelloWorldAdapterConfig> input) {
        this.adapterId = input.getAdapterId();
        this.adapterInformation = adapterInformation;
        this.adapterConfig = input.getConfig();
        this.protocolAdapterState = input.getProtocolAdapterState();
    }

    @Override
    public @NotNull String getId() {
        return adapterId;
    }

    @Override
    public void start(
            final @NotNull ProtocolAdapterStartInput input,
            final @NotNull ProtocolAdapterStartOutput output) {
        // any setup which should be done before the adapter starts polling comes here.
        try {
            socket = new Socket(adapterConfig.getIp(), adapterConfig.getPort());
            writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            protocolAdapterState.setConnectionStatus(ProtocolAdapterState.ConnectionStatus.CONNECTED);
            output.startedSuccessfully();
        } catch (final Exception e) {
            output.failStart(e, null);
        }
    }

    @Override
    public void stop(
            final @NotNull ProtocolAdapterStopInput protocolAdapterStopInput,
            final @NotNull ProtocolAdapterStopOutput protocolAdapterStopOutput) {
        try {
        reader.close();
        writer.close();
        socket.close();
        } catch (final Exception e) {
             protocolAdapterStopOutput.stoppedSuccessfully();
        }
        protocolAdapterStopOutput.stoppedSuccessfully();
    }

    @Override
    public @NotNull ProtocolAdapterInformation getProtocolAdapterInformation() {
        return adapterInformation;
    }

    @Override
    public void poll(
            final @NotNull BatchPollingInput pollingInput,
            final @NotNull BatchPollingOutput pollingOutput) {
        // here the sampling must be done. F.e. sending a http request

        try {
            writer.print("SI\r\n");
            writer.flush();

            String response = reader.readLine();

            pollingOutput.addDataPoint("Weight", response);
            pollingOutput.finish();
        } catch (final Exception e) {
             pollingOutput.addDataPoint("Weight", "fail");
            pollingOutput.finish();
        }

        
    }

    @Override
    public int getPollingIntervalMillis() {
        return adapterConfig.getPollingIntervalMillis();
    }

    @Override
    public int getMaxPollingErrorsBeforeRemoval() {
        return adapterConfig.getMaxPollingErrorsBeforeRemoval();
    }
}
