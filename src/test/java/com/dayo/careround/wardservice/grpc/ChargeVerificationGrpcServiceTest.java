package com.dayo.careround.wardservice.grpc;

import com.dayo.careround.wardservice.data.models.ChargeAssignment;
import com.dayo.careround.wardservice.data.models.enums.ChargeAssignmentStatus;
import com.dayo.careround.wardservice.data.repositories.ChargeAssignmentRepository;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ChargeVerificationGrpcServiceTest {

    private final ChargeAssignmentRepository repository = mock(ChargeAssignmentRepository.class);
    private final ChargeVerificationGrpcService service = new ChargeVerificationGrpcService(repository);

    private static final class Recorder<T> implements StreamObserver<T> {
        final List<T> values = new ArrayList<>();
        Throwable error;
        boolean completed;
        public void onNext(T value) { values.add(value); }
        public void onError(Throwable t) { error = t; }
        public void onCompleted() { completed = true; }
    }

    @Test
    void returnsTheActiveChargeHolder() {
        UUID ward = UUID.randomUUID(), nurse = UUID.randomUUID();
        ChargeAssignment ca = new ChargeAssignment();
        ca.setNurseId(nurse);
        when(repository.findByWardIdAndStatus(ward, ChargeAssignmentStatus.ACTIVE)).thenReturn(Optional.of(ca));

        Recorder<ActiveChargeNurseResponse> out = new Recorder<>();
        service.getActiveChargeNurse(ActiveChargeNurseRequest.newBuilder().setWardId(ward.toString()).build(), out);

        assertTrue(out.completed);
        assertTrue(out.values.get(0).getFound());
        assertEquals(nurse.toString(), out.values.get(0).getNurseId());
    }

    @Test
    void notFoundWhenNobodyHoldsCharge() {
        UUID ward = UUID.randomUUID();
        when(repository.findByWardIdAndStatus(ward, ChargeAssignmentStatus.ACTIVE)).thenReturn(Optional.empty());

        Recorder<ActiveChargeNurseResponse> out = new Recorder<>();
        service.getActiveChargeNurse(ActiveChargeNurseRequest.newBuilder().setWardId(ward.toString()).build(), out);

        assertFalse(out.values.get(0).getFound());
        assertEquals("", out.values.get(0).getNurseId());
    }

    @Test
    void malformedWardIdIsInvalidArgument() {
        Recorder<ActiveChargeNurseResponse> out = new Recorder<>();
        service.getActiveChargeNurse(ActiveChargeNurseRequest.newBuilder().setWardId("nope").build(), out);

        assertEquals(Status.Code.INVALID_ARGUMENT, ((StatusRuntimeException) out.error).getStatus().getCode());
        assertTrue(out.values.isEmpty());
    }
}
