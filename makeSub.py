import os
import shutil
import sys
import zipfile

def validate_inputs(args):
    if len(args) != 3:
        raise ValueError("Please provide exactly two inputs: (1) the path to your maven project directory and the destination directory path. All path must be absolute.")

    source_dir = args[1]
    dest_dir = args[2]

    if not os.path.exists(source_dir):
        raise FileNotFoundError(f"Maven project directory '{source_dir}' does not exist. Please create it first.")

    if not os.path.exists(dest_dir):
        raise FileNotFoundError(f"Destination directory '{dest_dir}' does not exist. Please create it first.")

    return source_dir, dest_dir

def copy_and_zip_maven_project(source_dir, dest_dir):
    src_main_java = os.path.join(source_dir, 'src', 'main', 'java')
    src_test = os.path.join(source_dir, 'src', 'test','java')

    dest_src = os.path.join(dest_dir, 'src')
    dest_test = os.path.join(dest_dir, 'test')

    if os.path.exists(src_main_java):
        shutil.copytree(src_main_java, dest_src, dirs_exist_ok=True)
        print(f"Copied {src_main_java} to {dest_src}")
    else:
        print(f"Source path '{src_main_java}' does not exist. Skipping...")

    if os.path.exists(src_test):
        shutil.copytree(src_test, dest_test, dirs_exist_ok=True)
        print(f"Copied {src_test} to {dest_test}")
    else:
        print(f"Source path '{src_test}' does not exist. Skipping...")

    zip_path = os.path.join(dest_dir, 'project.zip')
    with zipfile.ZipFile(zip_path, 'w', zipfile.ZIP_DEFLATED) as zipf:
        for root, _, files in os.walk(dest_src):
            for file in files:
                file_path = os.path.join(root, file)
                arcname = os.path.relpath(file_path, dest_dir)
                zipf.write(file_path, arcname)
        for root, _, files in os.walk(dest_test):
            for file in files:
                file_path = os.path.join(root, file)
                arcname = os.path.relpath(file_path, dest_dir)
                zipf.write(file_path, arcname)

    print(f"Created zip file at {zip_path}")

def main():
    try:
        source_dir, dest_dir = validate_inputs(sys.argv)
        copy_and_zip_maven_project(source_dir, dest_dir)
    except Exception as e:
        print(f"Error: {e}")

if __name__ == "__main__":
    main()
