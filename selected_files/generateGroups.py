import os
import re
import random
import shutil
from pathlib import Path


def find_file_pairs(directory):
    """
    Find pairs of Java files ending with _A.java and _B.java.
    Returns a list of tuples: [(file_A_path, file_B_path), ...]
    """
    pairs = []
    files = [f for f in os.listdir(directory) if f.endswith(".java")]

    grouped = {}
    for f in files:
        base = re.sub(r'_(A|B)\.java$', '', f)
        grouped.setdefault(base, []).append(f)

    for base, group in grouped.items():
        file_a = next((f for f in group if f.endswith("_A.java")), None)
        file_b = next((f for f in group if f.endswith("_B.java")), None)
        if file_a and file_b:
            pairs.append((os.path.join(directory, file_a), os.path.join(directory, file_b)))
    return pairs


def create_groups(directories, output_base_dir, total_groups=10, pairs_per_group=5):
    """
    Creates groups of unique A/B file pairs distributed among directories.
    Each pair will appear in only one group.
    File names are preserved (no random renaming).
    """
    # Collect all pairs from all directories
    all_pairs = []
    for directory in directories:
        dir_pairs = find_file_pairs(directory)
        all_pairs.extend(dir_pairs)

    if not all_pairs:
        print("[ERROR] No valid A/B pairs found.")
        return

    print(f"Found {len(all_pairs)} total A/B pairs across all directories.")

    # Shuffle pairs to randomize distribution
    random.shuffle(all_pairs)

    # Calculate total required pairs
    total_required = total_groups * pairs_per_group
    if len(all_pairs) < total_required:
        print(f"[WARNING] Only {len(all_pairs)} pairs available, "
              f"but {total_required} requested. Groups will be smaller.")
        total_groups = len(all_pairs) // pairs_per_group or 1

    # Clean and recreate output directory
    if os.path.exists(output_base_dir):
        shutil.rmtree(output_base_dir)
    os.makedirs(output_base_dir, exist_ok=True)

    index = 0
    for group_id in range(1, total_groups + 1):
        group_dir = os.path.join(output_base_dir, f"group_{group_id}")
        os.makedirs(group_dir, exist_ok=True)

        log_lines = [
            f"# Group {group_id} Log\n",
            "## Selected Pairs\n",
            "| Directory | File A | File B |\n",
            "|------------|--------|--------|\n"
        ]

        group_pairs = all_pairs[index:index + pairs_per_group]
        index += pairs_per_group

        for file_a, file_b in group_pairs:
            shutil.copy(file_a, os.path.join(group_dir, Path(file_a).name))
            shutil.copy(file_b, os.path.join(group_dir, Path(file_b).name))

            log_lines.append(
                f"| {Path(file_a).parent.name} "
                f"| {Path(file_a).name} "
                f"| {Path(file_b).name} |"
            )

        # Write Markdown log for the group
        log_file = os.path.join(output_base_dir, f"group_{group_id}_log.md")
        with open(log_file, "w", encoding="utf-8") as f:
            f.write("\n".join(log_lines))

        print(f"Group {group_id} created with {len(group_pairs)} unique pairs.")

    print("\n✅ All groups created successfully without duplication or renaming.")


def main():
    """Main function — create non-overlapping groups from sanitized Java pairs."""
    random.seed(42)  # Optional for reproducibility

    directories = [
        "sanitized_java/data-class",
        "sanitized_java/feature-envy",
        "sanitized_java/god-class",
        "sanitized_java/long-method",
    ]

    output_base_dir = "output_groups"

    create_groups(
        directories=directories,
        output_base_dir=output_base_dir,
        total_groups=10,
        pairs_per_group=5
    )


if __name__ == "__main__":
    main()
