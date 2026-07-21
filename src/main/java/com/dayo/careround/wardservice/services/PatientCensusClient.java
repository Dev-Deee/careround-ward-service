package com.dayo.careround.wardservice.services;

import com.dayo.careround.patientservice.grpc.PatientCensusServiceGrpc;
import com.dayo.careround.patientservice.grpc.WardCensusRequest;
import com.dayo.careround.patientservice.grpc.WardCensusResponse;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PatientCensusClient {

    @GrpcClient("patient-service")
    private PatientCensusServiceGrpc.PatientCensusServiceBlockingStub censusStub;

    public int getAdmittedCount(UUID wardId) {
        WardCensusRequest request = WardCensusRequest.newBuilder()
                .setWardId(wardId.toString())
                .build();

        WardCensusResponse response = censusStub.getAdmittedCount(request);
        return response.getAdmittedCount();
    }
}