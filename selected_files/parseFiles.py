import os
import re
import random


def read_file(file_path):
    """Reads a file and returns its contents as a string."""
    try:
        with open(file_path, 'r', encoding='utf-8') as file:
            return file.read()
    except Exception as e:
        print(f"Error reading file {file_path}: {e}")
        return ""


def sanitize_java_code(java_code):
    """
    Removes comments, package/import lines, and blank lines.
    Returns sanitized Java source code.
    """
    # Remove multi-line comments (including Javadoc)
    java_code = re.sub(r'/\*.*?\*/', '', java_code, flags=re.DOTALL)
    # Remove single-line comments
    java_code = re.sub(r'//.*', '', java_code)
    # Remove import statements
    java_code = re.sub(r'^\s*import\s+.*?;', '', java_code, flags=re.MULTILINE)
    # Remove package declarations
    java_code = re.sub(r'^\s*package\s+.*?;', '', java_code, flags=re.MULTILINE)
    # Remove empty lines and redundant spacing
    java_code = re.sub(r'^\s*$', '', java_code, flags=re.MULTILINE)
    java_code = re.sub(r'\n\s*\n', '\n', java_code)
    return java_code.strip()


def find_matching_files(dir1, dir2):
    """Finds pairs of Java files with matching names across two directories."""
    files_dir1 = {f for f in os.listdir(dir1) if f.endswith(".java")}
    files_dir2 = {f for f in os.listdir(dir2) if f.endswith(".java")}
    return [(os.path.join(dir1, f), os.path.join(dir2, f)) for f in files_dir1.intersection(files_dir2)]


def save_sanitized_java(content, version_label, base_name, output_dir):
    """Saves sanitized Java content to a file with version suffix (_A or _B)."""
    os.makedirs(output_dir, exist_ok=True)
    output_path = os.path.join(output_dir, f"{base_name}_{version_label}.java")
    try:
        with open(output_path, 'w', encoding='utf-8') as java_file:
            java_file.write(content)
        print(f"Sanitized Java file saved: {output_path}")
    except Exception as e:
        print(f"Error saving {version_label} file: {e}")
    return output_path


def main():
    # Directories containing code smell data
    dirs = [
        "dataset-data-class",
        "dataset-feature-envy",
        "dataset-god-class",
        "dataset-long-method",
    ]

    for dir1 in dirs:
        dir2 = f"{dir1}-r"

        # Output folder
        output_dir = os.path.join("sanitized_java", os.path.basename(dir1))
        os.makedirs(output_dir, exist_ok=True)

        # Markdown log file
        log_filename = f"{os.path.basename(dir1)}.md"
        log_path = os.path.join("sanitized_java", log_filename)

        with open(log_path, 'w', encoding='utf-8') as log_file:
            log_file.write("# Sanitized Java Generation Log\n\n")
            log_file.write("| File (A) | File (B) | Version A | Version B |\n")
            log_file.write("|-----------|-----------|------------|------------|\n")

        # Process file pairs
        for file1_path, file2_path in find_matching_files(dir1, dir2):
            content1 = sanitize_java_code(read_file(file1_path))
            content2 = sanitize_java_code(read_file(file2_path))

            # Randomly assign which is version A and B
            if random.choice([0, 1]):
                content_a, content_b = content1, content2
                dir_a, dir_b = dir1, dir2
            else:
                content_a, content_b = content2, content1
                dir_a, dir_b = dir2, dir1

            base_name = os.path.splitext(os.path.basename(file1_path))[0]
            relative_dir = os.path.dirname(os.path.relpath(file1_path, dir1))
            output_subdir = os.path.join(output_dir, relative_dir)
            os.makedirs(output_subdir, exist_ok=True)

            # Save each version separately
            path_a = save_sanitized_java(content_a, "A", base_name, output_subdir)
            path_b = save_sanitized_java(content_b, "B", base_name, output_subdir)

            # Register in log
            log_entry = (
                f"| {os.path.basename(path_a)} "
                f"| {os.path.basename(path_b)} "
                f"| {os.path.basename(dir_a)} "
                f"| {os.path.basename(dir_b)} |\n"
            )
            with open(log_path, 'a', encoding='utf-8') as log_file:
                log_file.write(log_entry)


if __name__ == "__main__":
    main()
