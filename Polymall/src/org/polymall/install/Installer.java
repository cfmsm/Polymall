package org.polymall.install;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import static commons.polymall.api.CommonUtil.*;

public class Installer {

    public static void install(String cfgUrl) {
        try {
            Path tempDir = Paths.get("temp");
            Files.createDirectories(tempDir);

            String fileName = cfgUrl.substring(cfgUrl.lastIndexOf('/') + 1);
            Path cfgPath = tempDir.resolve(fileName);

            logDownload(cfgUrl);
            downloadFile(cfgUrl, cfgPath.toString());

            Map<String, List<String>> config = parseCfg(cfgPath);
            String dirKey = config.getOrDefault("dir", List.of("downloads")).get(0); // Get first if multiple

            Path saveDir = resolveUserDirectory(dirKey);
            Files.createDirectories(saveDir);

            List<String> links;

            // Step 3: Gather download links
            links = config.getOrDefault("link", new ArrayList<>());



            if (links.isEmpty()) {
                print("No 'link =' entries found in " + cfgPath.getFileName());
                finish(2);
                return;
            }


            // Step 4: Download all files
            for (String url : links) {
                String file = url.substring(url.lastIndexOf('/') + 1);
                Path savePath = saveDir.resolve(file);
                logDownload(url);
                downloadFile(url, savePath.toString());
            }

            // Step 5: Clean up
            Files.deleteIfExists(cfgPath);
            finish(0);

        } catch (Exception e) {
            print("Error during install: " + e.getMessage());
            finish(2);
        }
    }


    public static void downloadFile(String urlString, String outputPath) throws IOException {
        try (InputStream in = new URL(urlString).openStream()) {
            Files.copy(in, Paths.get(outputPath), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static Map<String, List<String>> parseCfg(Path path) throws IOException {
        Map<String, List<String>> map = new HashMap<>();
        List<String> lines = Files.readAllLines(path);
        for (String line : lines) {
            line = line.replace("\r", "").trim();  // ← normalize line endings
            if (line.isEmpty() || line.startsWith("#")) continue;
            String[] parts = line.split("=", 2);
            if (parts.length == 2) {
                String key = parts[0].trim().replace("\uFEFF", "");
                String value = parts[1].trim();
                map.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
            }
        }

        return map;
    }


    private static Path resolveUserDirectory(String dir) {
        String home = System.getProperty("user.home");

        // Allow dynamic path with user.home
        if (dir.startsWith("user.home")) {
            String subPath = dir.replace("user.home", "").replace("+", "").trim();
            return Paths.get(home + File.separator + subPath);
        }

        return switch (dir.toLowerCase()) {
            case "desktop" -> Paths.get(home, "Desktop");
            case "downloads" -> Paths.get(home, "Downloads");
            case "documents" -> Paths.get(home, "Documents");
            default -> Paths.get(dir); // Raw path (can be absolute or relative)
        };
    }


    public static void command() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            String input = scanner.nextLine().trim();

            if (input.startsWith("polymall install ")) {
                String cmd = input.substring("polymall install ".length()).trim();
                try {
                    Map<String, String> recipes = getRecipes();
                    if (!recipes.containsKey(cmd)) {
                        print("No recipe found for: " + cmd);
                        continue;
                    }

                    String recipeUrl = recipes.get(cmd);

                    // Special case: if updating recipe list itself
                    if (cmd.equalsIgnoreCase("update")) {
                        Path target = Paths.get(System.getProperty("user.home"), "Polymall", "recipes.cfg");

                        logDownload(recipeUrl);
                        downloadFile(recipeUrl, target.toString());
                        print("Recipes updated.");
                        continue;
                    }

                    install(recipeUrl);

                } catch (IOException e) {
                }
            } else {
                print("Command not recognized.");
            }
        }
    }

    private static Map<String, String> getRecipes() throws IOException {
        Path userRecipes = Paths.get(System.getProperty("user.home"), "Polymall", "recipes.cfg");
        Map<String, String> recipes = new HashMap<>();
        if (Files.exists(userRecipes)) {
            List<String> lines = Files.readAllLines(userRecipes);
            for (String line : lines) {
                if (line.trim().isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    recipes.put(parts[0].trim(), parts[1].trim());
                }
                else {
                    print("Not found");
                }
            }
        }
        return recipes;
    }
}