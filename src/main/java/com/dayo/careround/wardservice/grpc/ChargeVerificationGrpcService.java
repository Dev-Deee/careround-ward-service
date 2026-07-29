package com.dayo.careround.wardservice.grpc;

import com.dayo.careround.wardservice.data.models.enums.ChargeAssignmentStatus;
import com.dayo.careround.wardservice.data.repositories.ChargeAssignmentRepository;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.UUID;

@GrpcService
@RequiredArgsConstructor
public class ChargeVerificationGrpcService extends ChargeVerificationServiceGrpc.ChargeVerificationServiceImplBase {

    private final ChargeAssignmentRepository chargeAssignmentRepository;

    @Override
    public void isActiveChargeNurse(ChargeVerificationRequest request, StreamObserver<ChargeVerificationResponse> responseObserver) {
        UUID wardId = UUID.fromString(request.getWardId());
        UUID nurseId = UUID.fromString(request.getNurseId());

        boolean isCharge = chargeAssignmentRepository
                .findByWardIdAndStatus(wardId, ChargeAssignmentStatus.ACTIVE)
                .map(ca -> ca.getNurseId().equals(nurseId))
                .orElse(false);

        responseObserver.onNext(ChargeVerificationResponse.newBuilder().setIsActiveChargeNurse(isCharge).build());
        responseObserver.onCompleted();
    }
}