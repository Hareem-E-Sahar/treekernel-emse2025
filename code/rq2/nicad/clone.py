import csv,os
from collections import defaultdict

def get_file_name(file_name, start_line, end_line):
    base_name = file_name.split('/')[-1].split('.')[0]
    a = file_name.rsplit('.', 1)[0]
    return f"{base_name}_{start_line}_{end_line}.java"


def lookup_clone_pairs(code_dir, lookup_file):
    #This is same as clone_file_lookup except that for each key there is a list, so duplicates can occur.
    ref_clones = {}
    unique_files = []
    with open(lookup_file, 'r') as ds_lookup:
        reader = csv.reader(ds_lookup)
        if "selected-columns" in os.path.basename(lookup_file).lower():  # Change 'header' to your specific keyword
            next(reader)  # Skip the header row if applicable

        for line_array in reader:
            try:
                file_name   = line_array[1]
                start_line1 = line_array[2]
                end_line1   = line_array[3]
                file_clone  = line_array[5]
                start_line2 = line_array[6]
                end_line2   = line_array[7]

                f1 = os.path.join(code_dir, line_array[0], file_name)
                f2 = os.path.join(code_dir, line_array[4], file_clone)

                if os.path.exists(f1) and os.path.exists(f2):
                    str_file_name  = f"{file_name.rsplit('.', 1)[0]}_{start_line1}_{end_line1}"
                    str_clone_name = f"{file_clone.rsplit('.', 1)[0]}_{start_line2}_{end_line2}"
                    ref_clones.setdefault(f"{str_file_name}.java", []).append(f"{str_clone_name}.java")
                    ref_clones.setdefault(f"{str_clone_name}.java", []).append(f"{str_file_name}.java")
                    unique_files.append(str_file_name)
                    unique_files.append(str_clone_name)
            except IndexError as e:
                print(e)
    unique_set = set(unique_files)
    print(f"Unique function string extracted: {len(unique_set)}")
    return ref_clones


def clone_file_lookup(code_dir, lookup_file):
    ref_clone_map = defaultdict(set)
    with open(lookup_file, 'r') as ds_lookup:
        reader = csv.reader(ds_lookup)
        for line in reader:
            try:
                f1 = os.path.join(code_dir, line[0], line[1])
                f2 = os.path.join(code_dir, line[4], line[5])

                #if os.path.exists(f1) and os.path.exists(f2):
                clone_file1 = get_file_name(line[1], line[2], line[3])
                clone_file2 = get_file_name(line[5], line[6], line[7])
                ref_clone_map[clone_file1].add(clone_file2)
                ref_clone_map[clone_file2].add(clone_file1)
            except IndexError as e:
                print(f"Error processing line: {line}\n{e}")
    return ref_clone_map


'''
if __name__ == "__main__":

    home_dir = os.path.expanduser("~")
    code_dir  = os.path.join(home_dir,"treekernel-emse2025","data","BigCloneEval","ijadataset","bcb_reduced","0")

    lookup_file   =  os.path.join(home_dir,"treekernel-emse2025","data","TestH2Database","bigclonedb_clones_alldir_8584153.txt")


    ref_clone_map2 = lookup_clone_pairs(code_dir, lookup_file)
    for key, value in ref_clone_map2.items():
    	print(key)
    	if key=="865946_463_501.java":
        	print(f"{key}: {value}")
'''
