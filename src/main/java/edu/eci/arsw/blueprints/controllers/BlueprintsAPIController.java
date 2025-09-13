
package edu.eci.arsw.blueprints.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

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

    @Operation(summary = "Obtener todos los blueprints", description = "Retorna todos los blueprints registrados.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Todos los blueprints obtenidos exitosamente")
    })
    @GetMapping
    public ResponseEntity<BaseApiResponse<Set<Blueprint>>> getAll() {
        return ResponseEntity.ok(responseBuilder.success(services.getAllBlueprints(), "All blueprints successfully fetched"));
    }

    // GET /blueprints/{author}

    @Operation(summary = "Obtener blueprints por autor", description = "Retorna todos los blueprints de un autor específico.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Blueprints encontrados por autor"),
        @ApiResponse(responseCode = "404", description = "No se encontraron blueprints para el autor")
    })
    @GetMapping("/{author}")
    public ResponseEntity<BaseApiResponse<?>> byAuthor(@PathVariable String author) {
        try {
            return ResponseEntity.ok(responseBuilder.success(services.getBlueprintsByAuthor(author),"Blueprint was found by author: "+ author));
        } catch (BlueprintNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseBuilder.notFound(Map.of("error", e.getMessage()), "Could not find any blueprint by author: "+ author));
        }
    }

    // GET /blueprints/{author}/{bpname}

    @Operation(summary = "Obtener blueprint por autor y nombre", description = "Retorna un blueprint específico dado el autor y el nombre.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Blueprint encontrado"),
        @ApiResponse(responseCode = "404", description = "No se encontró el blueprint")
    })
    @GetMapping("/{author}/{bpname}")
    public ResponseEntity<BaseApiResponse<?>> byAuthorAndName(@PathVariable String author, @PathVariable String bpname) {
        try {
            return ResponseEntity.ok(responseBuilder.success(services.getBlueprint(author, bpname),"Blueprint was found by author: " + author + " and name: "+ bpname));
        } catch (BlueprintNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseBuilder.notFound(Map.of("error", e.getMessage()), "Could not find any blueprint by author: "+ author+ " and name:" +bpname));
        }
    }

    // POST /blueprints

    @Operation(summary = "Crear un nuevo blueprint", description = "Crea un nuevo blueprint con los datos proporcionados.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Blueprint creado exitosamente"),
        @ApiResponse(responseCode = "403", description = "No se pudo crear el blueprint")
    })
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

    @Operation(summary = "Agregar punto a un blueprint", description = "Agrega un punto a un blueprint existente.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "202", description = "Punto agregado exitosamente"),
        @ApiResponse(responseCode = "404", description = "No se encontró el blueprint para agregar el punto")
    })
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
