import os

base = "src/main/java/com/example/scrill"

structure = {
    "controller/PlayerController.java":     "controller",
    "controller/LibraryController.java":    "controller",
    "controller/ProfileController.java":    "controller",
    "controller/NavigationController.java": "controller",
    "controller/SettingsController.java":   "controller",
    "ui/DashboardBuilder.java":             "ui",
    "ui/ModalManager.java":                 "ui",
    "ui/SidebarController.java":            "ui",
    "util/SpotifyHelper.java":              "util",
    "util/StatsManager.java":               "util",
    "util/ThemeManager.java":               "util",
}

for path, package_suffix in structure.items():
    full_path = os.path.join(base, path)
    os.makedirs(os.path.dirname(full_path), exist_ok=True)

    if not os.path.exists(full_path):
        class_name = os.path.basename(path).replace(".java", "")
        package = f"com.example.scrill.{package_suffix}"

        with open(full_path, "w", encoding="utf-8") as f:
            f.write(f"package {package};\n\npublic class {class_name} {{\n\n}}\n")
        print(f"✅ Создан: {full_path}")
    else:
        print(f"⚠️  Уже существует: {full_path}")

print("\nГотово!")