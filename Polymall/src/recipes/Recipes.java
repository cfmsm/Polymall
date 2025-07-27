package recipes;

import java.io.IOException;
import java.nio.file.*;
import static org.polymall.install.Installer.*;
import static commons.polymall.api.CommonUtil.*;
public class Recipes {
    private static final String DEFAULT_RECIPES_URL = "https://raw.githubusercontent.com/cfmsm/Polymall/main/downloads/recipes.cfg";

    public static void main(String[] args) {
        ensureRecipesFile();
        print("Polymall successfully booted");
        loadRecipe();
    }

    public static void loadRecipe() {
        command();
    }

    private static void ensureRecipesFile() {
        Path recipesPath = Paths.get(System.getProperty("user.home"), "Polymall", "recipes.cfg");

        if (!Files.exists(recipesPath)) {
            try {
                print("No recipes.cfg found. Downloading default from: " + DEFAULT_RECIPES_URL);
                Files.createDirectories(recipesPath.getParent());
                downloadFile(DEFAULT_RECIPES_URL, recipesPath.toString());
            } catch (IOException e) {
            }
        }
    }
}
