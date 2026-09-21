package com.staffcore33.ats.tag;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @GetMapping
    public List<TagResponse> list(@RequestParam(required = false) String search) {
        return tagService.list(search);
    }

    @GetMapping("/{id}")
    public TagResponse get(@PathVariable Long id) {
        return tagService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TagResponse create(@Valid @RequestBody TagRequest request) {
        return tagService.create(request);
    }

    @PutMapping("/{id}")
    public TagResponse update(@PathVariable Long id, @Valid @RequestBody TagRequest request) {
        return tagService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        tagService.delete(id);
    }

    @GetMapping("/candidates/{candidateId}")
    public List<TagResponse> listForCandidate(@PathVariable Long candidateId) {
        return tagService.listForCandidate(candidateId);
    }

    @PostMapping("/candidates/{candidateId}")
    public List<TagResponse> assign(@PathVariable Long candidateId, @RequestBody AssignTagsRequest request) {
        return tagService.assignToCandidate(candidateId, request);
    }

    @DeleteMapping("/candidates/{candidateId}/{tagId}")
    public List<TagResponse> remove(@PathVariable Long candidateId, @PathVariable Long tagId) {
        return tagService.removeFromCandidate(candidateId, tagId);
    }
}
