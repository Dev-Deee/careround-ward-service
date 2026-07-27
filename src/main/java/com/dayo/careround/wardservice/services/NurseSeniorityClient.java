package com.dayo.careround.wardservice.services;

import com.dayo.careround.userservice.grpc.NurseCandidate;
import com.dayo.careround.userservice.grpc.NurseSeniorityServiceGrpc;
import com.dayo.careround.userservice.grpc.SeniorityRequest;
import com.dayo.careround.userservice.grpc.SeniorityResponse;
import com.dayo.careround.wardservice.data.models.Attendance;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class NurseSeniorityClient {

    @GrpcClient("user-service")
    private NurseSeniorityServiceGrpc.NurseSeniorityServiceBlockingStub seniorityStub;

    public UUID getMostSeniorNurseId(List<Attendance> candidates) {
        List<NurseCandidate> grpcCandidates = candidates.stream()
                .map(a -> NurseCandidate.newBuilder()
                        .setNurseId(a.getStaffId().toString())
                        .setClockInEpochSeconds(a.getClockInAt().getEpochSecond())
                        .build())
                .toList();

        SeniorityRequest request = SeniorityRequest.newBuilder()
                .addAllCandidates(grpcCandidates)
                .build();

        SeniorityResponse response = seniorityStub.getMostSeniorNurse(request);
        return UUID.fromString(response.getNurseId());
    }
}