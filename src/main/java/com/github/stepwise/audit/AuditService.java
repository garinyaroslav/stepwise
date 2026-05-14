package com.github.stepwise.audit;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class AuditService {

    @PersistenceContext
    private EntityManager em;

    @Transactional(readOnly = true)
    public <T> List<AuditEntry<T>> getHistory(Class<T> entityClass, Long id) {
        AuditReader reader = AuditReaderFactory.get(em);
        List<Number> revisions = reader.getRevisions(entityClass, id);

        return revisions.stream().map(rev -> {
            T snapshot = reader.find(entityClass, id, rev);
            AuditRevisionEntity info = reader.findRevision(
                    AuditRevisionEntity.class, rev);

            return new AuditEntry<>(
                    rev.longValue(),
                    info.getRevisionDate().toInstant(),
                    info.getUsername(),
                    info.getIpAddress(),
                    snapshot);
        }).toList();
    }

    @Transactional(readOnly = true)
    public <T> T getEntityAtDate(Class<T> entityClass, Long id, Instant at) {
        AuditReader reader = AuditReaderFactory.get(em);
        return reader.find(entityClass, id, Date.from(at));
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public <T> List<T> getRecentChanges(Class<T> entityClass) {
        AuditReader reader = AuditReaderFactory.get(em);
        return reader.createQuery()
                .forRevisionsOfEntity(entityClass, true, true)
                .addOrder(AuditEntity.revisionNumber().desc())
                .setMaxResults(50)
                .getResultList();
    }
}
