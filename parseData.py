import os
import random
import shutil
import pandas as pd

# ---------------------------------------------------------------------
# Global configuration
# ---------------------------------------------------------------------

LOG_ERRORS = "log_errors.txt"

# Manual exception dictionary for specific class/file mappings
MANUAL_EXCEPTIONS = {
    "org.lnicholls.galleon.apps.email.Email$3": "Email.java",
    # Add more manual exceptions here as needed
}


# ---------------------------------------------------------------------
# Utility functions
# ---------------------------------------------------------------------

def write_log(message: str) -> None:
    """Write error or progress messages to the log file and print to console."""
    with open(LOG_ERRORS, "a", encoding="utf-8") as log:
        log.write(message + "\n")
    print(message)


def load_spreadsheets(filepath: str) -> dict[str, pd.DataFrame]:
    """
    Load all four Excel sheets and filter rows where severity > 1.
    
    Args:
        filepath: Path to the Excel file.
    
    Returns:
        A dictionary mapping smell types to filtered DataFrames.
    """
    sheets = pd.read_excel(filepath, sheet_name=None, engine="openpyxl")
    return {smell_type: df[df["severity"] > 1] for smell_type, df in sheets.items()}


def find_class(base_dir: str, project_name: str, package: str, complextype: str) -> str | None:
    """
    Locate a Java class file within the Qualitas Corpus structure.

    Handles:
        - Manual exception mappings
        - Nested/inner classes (using '$' separator)
        - Partial project name resolution (e.g., 'antlr-2.7.2')

    Args:
        base_dir: Base path to the Qualitas Corpus Systems folder.
        project_name: Name of the project (as in the dataset).
        package: Java package name.
        complextype: Fully qualified class or inner class name.

    Returns:
        The absolute path to the class file if found, otherwise None.
    """
    project_root = os.path.join(base_dir, project_name.split('-')[0], project_name)

    if not os.path.exists(project_root):
        write_log(f"[ERROR] Project directory '{project_name}' not found.")
        return None

    # Use manual exception if defined, otherwise infer the filename
    filename = MANUAL_EXCEPTIONS.get(
        complextype, complextype.split('.')[0].split('$')[0] + ".java"
    )
    package_path = package.replace('.', os.sep)

    # 1) Try finding inside the expected package path
    for root, _, files in os.walk(project_root):
        if root.endswith(package_path) and filename in files:
            return os.path.join(root, filename)

    # 2) If not found, search the entire project
    for root, _, files in os.walk(project_root):
        if filename in files:
            return os.path.join(root, filename)

    write_log(
        f"[ERROR] Class '{filename}' not found in project '{project_name}' "
        f"(expected package: {package})"
    )
    return None


def select_random_sample(
    filtered_data: dict[str, pd.DataFrame], base_dir: str, max_files: int = 20
) -> dict[str, list[tuple[str, str]]]:
    """
    Select up to `max_files` random Java files for each code smell type.

    Args:
        filtered_data: Dictionary of smell types to filtered DataFrames.
        base_dir: Base directory where projects are stored.
        max_files: Maximum number of samples per smell type.

    Returns:
        A dictionary mapping each smell type to a list of (project, filepath) tuples.
    """
    selected_files = {}

    for smell_type, df in filtered_data.items():
        write_log(f"\n--> Selecting files for code smell type: {smell_type}")
        valid_paths = []

        for _, row in df.iterrows():
            path = find_class(base_dir, row["project"], row["package"], row["complextype"])
            if path:
                valid_paths.append((row["project"], path))

        # Randomly sample up to `max_files` entries
        selected_files[smell_type] = random.sample(valid_paths, min(max_files, len(valid_paths)))

    return selected_files


def copy_files_to_destination(selected_files: dict[str, list[tuple[str, str]]], destination: str) -> None:
    """
    Copy selected Java files into destination folders by code smell type.
    
    Args:
        selected_files: Mapping from smell type to list of (project, filepath) tuples.
        destination: Root folder where the organized copies will be saved.
    """
    os.makedirs(destination, exist_ok=True)

    for smell_type, files in selected_files.items():
        smell_folder = os.path.join(destination, smell_type.replace(" ", "_"))
        os.makedirs(smell_folder, exist_ok=True)

        for project_name, source_path in files:
            filename = os.path.basename(source_path)
            new_filename = f"{project_name}_{filename}"
            dest_path = os.path.join(smell_folder, new_filename)

            shutil.copy2(source_path, dest_path)  # Preserve metadata
            print(f"Copied: {source_path} → {dest_path}")


# ---------------------------------------------------------------------
# Main pipeline
# ---------------------------------------------------------------------

def main(filepath: str, base_dir: str, destination_dir: str) -> None:
    """Run the complete data extraction and copy process."""
    # Reset previous log
    open(LOG_ERRORS, "w").close()

    filtered_data = load_spreadsheets(filepath)
    sampled_files = select_random_sample(filtered_data, base_dir)
    copy_files_to_destination(sampled_files, destination_dir)

    print("\nAll files were successfully copied!")


# ---------------------------------------------------------------------
# Script entry point
# ---------------------------------------------------------------------

if __name__ == "__main__":
    FILEPATH = "Dataset.xlsx"
    BASE_DIR = "QualitasCorpus-20130901r/Systems"
    DEST_DIR = "selected_files"

    main(FILEPATH, BASE_DIR, DEST_DIR)
