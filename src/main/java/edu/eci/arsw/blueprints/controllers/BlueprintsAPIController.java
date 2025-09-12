package edu.eci.arsw.blueprints.controllers;

import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import edu.eci.arsw.blueprints.model.dto.BaseApiResponse;
import edu.eci.arsw.blueprints.persistence.BlueprintNotFoundException;
import edu.eci.arsw.blueprints.persistence.BlueprintPersistenceException;
import edu.eci.arsw.blueprints.services.BlueprintsServices;
import edu.eci.arsw.blueprints.utils.ApiResponseBuilder;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/blueprints")
public class BlueprintsAPIController {

    private final BlueprintsServices services;
    
    private final ApiResponseBuilder responseBuilder;

    public BlueprintsAPIController(BlueprintsServices services, ApiResponseBuilder responseBuilder) {
        this.services = services;
        this.responseBuilder = responseBuilder;
    }

    // GET /blueprints
    @GetMapping
    public ResponseEntity<BaseApiResponse<Set<Blueprint>>> getAll() {
        return ResponseEntity.ok(responseBuilder.success(services.getAllBlueprints(), "All blueprints successfully fetched"));
    }

    // GET /blueprints/{author}
    @GetMapping("/{author}")
    public ResponseEntity<BaseApiResponse<?>> byAuthor(@PathVariable String author) {
        try {
            return ResponseEntity.ok(responseBuilder.success(services.getBlueprintsByAuthor(author),"Blueprint was found by author: "+ author));
        } catch (BlueprintNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseBuilder.notFound(Map.of("error", e.getMessage()), "Could not find any blueprint by author: "+ author));
        }
    }

    // GET /blueprints/{author}/{bpname}
    @GetMapping("/{author}/{bpname}")
    public ResponseEntity<BaseApiResponse<?>> byAuthorAndName(@PathVariable String author, @PathVariable String bpname) {
        try {
            return ResponseEntity.ok(responseBuilder.success(services.getBlueprint(author, bpname),"Blueprint was found by author: " + author + " and name: "+ bpname));
        } catch (BlueprintNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseBuilder.notFound(Map.of("error", e.getMessage()), "Could not find any blueprint by author: "+ author+ " and name:" +bpname));
        }
    }

    // POST /blueprints
    @PostMapping
    public ResponseEntity<BaseApiResponse<?>> add(@Valid @RequestBody NewBlueprintRequest req) {
        try {
            Blueprint bp = new Blueprint(req.author(), req.name(), req.points());
            services.addNewBlueprint(bp);
            return ResponseEntity.status(HttpStatus.CREATED).body(responseBuilder.created(null,"Blueprint was created"));
        } catch (BlueprintPersistenceException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(responseBuilder.forbidden(Map.of("error", e.getMessage()),"Could not create blueprint"));
        }
    }

    // PUT /blueprints/{author}/{bpname}/points
    @PutMapping("/{author}/{bpname}/points")
    public ResponseEntity<BaseApiResponse<?>> addPoint(@PathVariable String author, @PathVariable String bpname,
            @RequestBody Point p) {
        try {
            services.addPoint(author, bpname, p.x(), p.y());
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(responseBuilder.accepted(null,"Point was added succesfully"));
        } catch (BlueprintNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseBuilder.notFound(Map.of("error", e.getMessage()), "Could not find name and author to add point"));
        }
    }

    public record NewBlueprintRequest(
            @NotBlank String author,
            @NotBlank String name,
            @Valid java.util.List<Point> points) {
    }
}
