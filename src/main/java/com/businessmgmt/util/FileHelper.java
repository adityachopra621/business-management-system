package com.businessmgmt.util;

import com.businessmgmt.exception.BusinessException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Utility class for file I/O operations.
 * Provides methods for serializing/deserializing objects and reading/writing text files.
 */
public class FileHelper {

    private static final Logger logger = LoggerFactory.getLogger(FileHelper.class);

    /**
     * Serializes an object to a file using ObjectOutputStream.
     * The object must implement {@link Serializable}.
     *
     * @param object   the object to serialize
     * @param filePath the destination file path
     * @param <T>      the object type
     */
    public static <T extends Serializable> void serializeObject(T object, String filePath) {
        logger.info("Serializing object to " + filePath);

        ensureParentDirectoryExists(filePath);

        try (FileOutputStream fos = new FileOutputStream(filePath);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(object);
            logger.info("Object serialized successfully to " + filePath);
        } catch (IOException e) {
            logger.severe("Failed to serialize object: " + e.getMessage());
            throw new BusinessException("Failed to serialize object to file: " + e.getMessage());
        }
    }

    /**
     * Deserializes an object from a file using ObjectInputStream.
     *
     * @param filePath the source file path
     * @param type     the expected class type
     * @param <T>      the object type
     * @return the deserialized object
     */
    @SuppressWarnings("unchecked")
    public static <T extends Serializable> T deserializeObject(String filePath, Class<T> type) {
        logger.info("Deserializing object from " + filePath);

        try (FileInputStream fis = new FileInputStream(filePath);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            Object obj = ois.readObject();
            if (!type.isInstance(obj)) {
                throw new BusinessException("Deserialized object is not of expected type: " + type.getName());
            }
            logger.info("Object deserialized successfully from " + filePath);
            return (T) obj;
        } catch (IOException | ClassNotFoundException e) {
            logger.severe("Failed to deserialize object: " + e.getMessage());
            throw new BusinessException("Failed to deserialize object from file: " + e.getMessage());
        }
    }

    /**
     * Writes text content to a file. Creates the file if it doesn't exist,
     * overwrites if it does.
     *
     * @param content  the text content to write
     * @param filePath the destination file path
     */
    public static void writeTextFile(String content, String filePath) {
        logger.info("Writing text file: " + filePath);

        ensureParentDirectoryExists(filePath);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(content);
            logger.info("Text file written successfully: " + filePath);
        } catch (IOException e) {
            logger.severe("Failed to write text file: " + e.getMessage());
            throw new BusinessException("Failed to write text file: " + e.getMessage());
        }
    }

    /**
     * Reads the entire content of a text file as a string.
     *
     * @param filePath the source file path
     * @return the file content
     */
    public static String readTextFile(String filePath) {
        logger.info("Reading text file: " + filePath);

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                if (sb.length() > 0) {
                    sb.append(System.lineSeparator());
                }
                sb.append(line);
            }
            logger.info("Text file read successfully: " + filePath);
            return sb.toString();
        } catch (IOException e) {
            logger.severe("Failed to read text file: " + e.getMessage());
            throw new BusinessException("Failed to read text file: " + e.getMessage());
        }
    }

    /**
     * Reads all lines from a text file.
     *
     * @param filePath the source file path
     * @return list of lines
     */
    public static List<String> readLines(String filePath) {
        logger.info("Reading lines from: " + filePath);

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            List<String> lines = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            logger.info("Read " + lines.size() + " lines from " + filePath);
            return lines;
        } catch (IOException e) {
            logger.severe("Failed to read lines: " + e.getMessage());
            throw new BusinessException("Failed to read lines from file: " + e.getMessage());
        }
    }

    /**
     * Appends text content to a file. Creates the file if it doesn't exist.
     *
     * @param content  the text content to append
     * @param filePath the destination file path
     */
    public static void appendToFile(String content, String filePath) {
        logger.info("Appending to file: " + filePath);

        ensureParentDirectoryExists(filePath);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
            writer.write(content);
            logger.info("Content appended successfully to " + filePath);
        } catch (IOException e) {
            logger.severe("Failed to append to file: " + e.getMessage());
            throw new BusinessException("Failed to append to file: " + e.getMessage());
        }
    }

    /**
     * Checks whether a file exists at the given path.
     *
     * @param filePath the file path to check
     * @return true if the file exists
     */
    public static boolean fileExists(String filePath) {
        return Files.exists(Paths.get(filePath));
    }

    /**
     * Deletes a file at the given path.
     *
     * @param filePath the file path to delete
     * @return true if the file was deleted
     */
    public static boolean deleteFile(String filePath) {
        logger.info("Deleting file: " + filePath);
        try {
            boolean deleted = Files.deleteIfExists(Paths.get(filePath));
            if (deleted) {
                logger.info("File deleted: " + filePath);
            } else {
                logger.warning("File not found for deletion: " + filePath);
            }
            return deleted;
        } catch (IOException e) {
            logger.severe("Failed to delete file: " + e.getMessage());
            throw new BusinessException("Failed to delete file: " + e.getMessage());
        }
    }

    /**
     * Ensures the parent directory of the given file path exists, creating it if necessary.
     */
    private static void ensureParentDirectoryExists(String filePath) {
        Path parent = Paths.get(filePath).getParent();
        if (parent != null && !Files.exists(parent)) {
            try {
                Files.createDirectories(parent);
                logger.info("Created directory: " + parent);
            } catch (IOException e) {
                logger.severe("Failed to create directory: " + e.getMessage());
                throw new BusinessException("Failed to create directory: " + e.getMessage());
            }
        }
    }
}
