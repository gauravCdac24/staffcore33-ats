package com.staffcore33.ats.tag;

import com.staffcore33.ats.audit.AuditService;
import com.staffcore33.ats.candidate.CandidateService;
import com.staffcore33.ats.common.BadRequestException;
import com.staffcore33.ats.common.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;
    private final CandidateService candidateService;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<TagResponse> list(String search) {
        List<Tag> tags = (search == null || search.isBlank())
                ? tagRepository.findAll()
                : tagRepository.search(search.trim());
        return tags.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public TagResponse get(Long id) {
        return toResponse(find(id));
    }

    @Transactional
    public TagResponse create(TagRequest request) {
        String name = request.getName().trim();
        if (tagRepository.existsByNameIgnoreCase(name)) {
            throw new BadRequestException("Tag already exists");
        }
        Tag tag = tagRepository.save(Tag.builder().name(name).build());
        auditService.log("TAG_CREATED", "TAG", tag.getId(), tag.getName());
        return toResponse(tag);
    }

    @Transactional
    public TagResponse update(Long id, TagRequest request) {
        Tag tag = find(id);
        String name = request.getName().trim();
        tagRepository.findByNameIgnoreCase(name).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new BadRequestException("Tag already exists");
            }
        });
        tag.setName(name);
        auditService.log("TAG_UPDATED", "TAG", tag.getId(), tag.getName());
        return toResponse(tag);
    }

    @Transactional
    public void delete(Long id) {
        Tag tag = find(id);
        tagRepository.delete(tag);
        auditService.log("TAG_DELETED", "TAG", id, tag.getName());
    }

    @Transactional
    public List<TagResponse> assignToCandidate(Long candidateId, AssignTagsRequest request) {
        candidateService.findActive(candidateId);
        List<Tag> assigned = new ArrayList<>();
        if (request.getTagIds() != null) {
            for (Long tagId : request.getTagIds()) {
                Tag tag = find(tagId);
                tagRepository.assignCandidateTag(candidateId, tagId);
                assigned.add(tag);
            }
        }
        if (request.getTagNames() != null) {
            for (String tagName : request.getTagNames()) {
                if (tagName == null || tagName.isBlank()) {
                    continue;
                }
                Tag tag = tagRepository.findByNameIgnoreCase(tagName.trim())
                        .orElseGet(() -> tagRepository.save(Tag.builder().name(tagName.trim()).build()));
                tagRepository.assignCandidateTag(candidateId, tag.getId());
                assigned.add(tag);
            }
        }
        auditService.log("TAGS_ASSIGNED", "CANDIDATE", candidateId, "tags assigned");
        return tagRepository.findByCandidateId(candidateId).stream().map(this::toResponse).toList();
    }

    @Transactional
    public List<TagResponse> removeFromCandidate(Long candidateId, Long tagId) {
        candidateService.findActive(candidateId);
        find(tagId);
        tagRepository.removeCandidateTag(candidateId, tagId);
        auditService.log("TAG_REMOVED", "CANDIDATE", candidateId, "tagId=" + tagId);
        return tagRepository.findByCandidateId(candidateId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<TagResponse> listForCandidate(Long candidateId) {
        candidateService.findActive(candidateId);
        return tagRepository.findByCandidateId(candidateId).stream().map(this::toResponse).toList();
    }

    private Tag find(Long id) {
        return tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found"));
    }

    private TagResponse toResponse(Tag tag) {
        return TagResponse.builder()
                .id(tag.getId())
                .name(tag.getName())
                .createdAt(tag.getCreatedAt())
                .build();
    }
}
