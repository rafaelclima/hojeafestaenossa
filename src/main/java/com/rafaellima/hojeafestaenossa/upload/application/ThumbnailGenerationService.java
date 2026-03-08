package com.rafaellima.hojeafestaenossa.upload.application;

import java.io.File;
import java.io.IOException;

import org.springframework.stereotype.Service;

import net.coobird.thumbnailator.Thumbnails;

@Service
public class ThumbnailGenerationService {

    public File execute(File original) throws IOException {
        // Cria um arquivo temporário para o thumbnail
        File thumbnail = File.createTempFile("thumb-", ".jpg");

        Thumbnails.of(original)
                .size(300, 300) // Tamanho ideal para listas de moderação
                .outputQuality(0.8)
                .toFile(thumbnail);

        return thumbnail;
    }
}
