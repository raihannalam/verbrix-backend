package com.verbrix.repository;

import com.verbrix.model.business.booking.Relationship;
import com.verbrix.model.business.payment.Transaction;
import com.verbrix.model.enums.PaymentStatus;
import com.verbrix.model.enums.RelationshipStatus;
import com.verbrix.model.enums.TransactionPurpose;
import com.verbrix.model.rbac.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface RelationshipRepository extends JpaRepository<Relationship, Long> {

    boolean existsByClientAndInterpreter(User client, User interpreter);

    List<Relationship> findAllByInterpreterAndStatus(
            User interpreter,
            RelationshipStatus status
    );

    boolean existsByClientAndInterpreterAndStatus(User interpreterUser, User client, RelationshipStatus status);

    List<Relationship> findAllByClientAndStatus(User user, RelationshipStatus relationshipStatus);

    List<Relationship> findAllByInterpreter(User interpreter);

    List<Relationship> findAllByInterpreterAndStatusIn(User interpreter, Collection<RelationshipStatus> statuses);

    List<Relationship> findAllByClientAndStatusIn(User client, Collection<RelationshipStatus> statuses);


    @Query("SELECT r FROM Relationship r " +
            "LEFT JOIN FETCH r.client " +
            "LEFT JOIN FETCH r.interpreter " +
            "WHERE r.id = :id")
    Optional<Relationship> findRelationshipWithUsers(@Param("id") Long id);

    boolean existsByClientAndInterpreterAndStatusIn(User client, User interpreter, Collection<RelationshipStatus> statuses);

}
