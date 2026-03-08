package com.rafaellima.hojeafestaenossa.upload.application;

import java.io.File;
import java.io.IOException;

import org.springframework.stereotype.Service;

import net.coobird.thumbnailator.Thumbnails;

@Service
public class ImageCompressionService {

    public File execute(File original) throws IOException {
        // Cria um arquivo temporário para a versão comprimida
        File compressed = File.createTempFile("compressed-", ".jpg");

        Thumbnails.of(original)
                .size(1920, 1080) // Redimensiona para Full HD (mantendo proporção)
                .outputQuality(0.75) // Aplica compressão
                .toFile(compressed);

        return compressed;
    }
}
