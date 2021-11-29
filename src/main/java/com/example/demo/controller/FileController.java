package com.example.demo.controller;

import com.example.demo.domain.dto.ResponseFile;
import com.example.demo.domain.dto.ResponseMessage;
import com.example.demo.domain.model.File;
import com.example.demo.repository.FileRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/files")
public class FileController {

    @Autowired
    private FileRepository fileRepository;

    @PostMapping
    @Operation(
            summary = "Finds a person",
            description = "Finds a person by their Id.",
            responses = {
                    @ApiResponse(
                            description = "Success",
                            responseCode = "200",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseFile.class))
                    ),
                    @ApiResponse(description = "Not found", responseCode = "404", content = @Content),
                    @ApiResponse(description = "Internal error", responseCode = "500", content = @Content)
            }
    )
    public ResponseEntity<ResponseFile> upload(@RequestPart(name = "file") MultipartFile uploadedFile) {
        try {
            File file = new File();
            file.contenttype = uploadedFile.getContentType();
            file.data = uploadedFile.getBytes();

            File savedFile = fileRepository.save(file);

            ResponseFile responseFile = new ResponseFile(savedFile.fileid, savedFile.contenttype);

            return ResponseEntity.ok().body(responseFile);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }


    @GetMapping("/{id}")
    public ResponseEntity<?> getFile(@PathVariable UUID id) {
        File file = fileRepository.findById(id).orElse(null);

        if (file == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ResponseMessage.message("File not found"));

        return ResponseEntity.ok()
                .contentType(MediaType.valueOf(file.contenttype))
                .contentLength(file.data.length)
                .body(file.data);
    }

    @GetMapping
    public ResponseEntity<?> getAll(){
        return ResponseEntity.ok().body(fileRepository.findBy());
    }


    @GetMapping("/web")
    public String hack() {
        return "<form method='POST' enctype='multipart/form-data' action='/files/' style='display:flex;'>" +
                "<input id='file' type='file' name='file' style='display:none' onchange='preview.src=window.URL.createObjectURL(event.target.files[0])'>" +
                "<label for='file' style='border:1px dashed #999'><img id='preview' style='width:64px;max-height:64px;object-fit:contain;border:none'></label>" +
                "<input type='submit' style='background:#0096f7;color: white;border: 0;border-radius: 3px;padding: 8px;' value='Upload'>" +
                "</form><div style='display:flex;flex-wrap:wrap;gap:1em;'>" + fileRepository.getFileIds().stream().map(id -> "<img src='/files/"+id+"' style='width:12em;height:12em;object-fit:contain'>").collect(Collectors.joining()) + "</div>";
    }
}
