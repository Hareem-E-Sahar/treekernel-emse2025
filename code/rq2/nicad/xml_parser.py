import xml.etree.ElementTree as ET
from collections import defaultdict

def list_clones_nicad_found():
    # Define the XML file path
    xmlfile = "/home/hareem/4874-NiCad-6.2/NiCad-6.2/systems/11_functions-blind-clones/11_functions-blind-clones-0.30.xml"

    # Parse the XML file
    tree = ET.parse(xmlfile)
    root = tree.getroot()

    # Initialize a dictionary to hold the clone relationships
    ref_clone_map = defaultdict(set)

    # Iterate through 'clone' elements
    for clone in root.findall('clone'):
        # Get 'source' elements under 'clone'
        sources = clone.findall('source')

        # Extract information for the first 'source' element
        dir_file_name1 = sources[0].attrib['file']
        start1 = sources[0].attrib['startline']
        end1 = sources[0].attrib['endline']
        fn1 = dir_file_name1.split('/')[-1]
        clone_file1 = f"{fn1.split('.')[0]}_{start1}_{end1}.java"

        # Extract information for the second 'source' element
        dir_file_name2 = sources[1].attrib['file']
        start2 = sources[1].attrib['startline']
        end2 = sources[1].attrib['endline']
        fn2 = dir_file_name2.split('/')[-1]
        clone_file2 = f"{fn2.split('.')[0]}_{start2}_{end2}.java"

        # Add the clones to the dictionary
        ref_clone_map[clone_file1].add(clone_file2)
        ref_clone_map[clone_file2].add(clone_file1)

    return ref_clone_map

if __name__ == "__main__":
    ref_clone_map = list_clones_nicad_found()
    for key, value in ref_clone_map.items():
        print(f"{key}: {value}")

