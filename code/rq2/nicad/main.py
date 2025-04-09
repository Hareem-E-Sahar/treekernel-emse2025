from clone import *
from metrics import *
import time, os
home_dir = os.path.expanduser("~")
def typewise_evaluation(seed):
    nicad_clones_file  = os.path.join(home_dir,"treekernel-emse2025","data","BigCloneEval","bigclone_eval_results_for_nicad","clonesfile_all_minsize6")
    ref_clones_file   =  os.path.join(home_dir,"treekernel-emse2025","data","TestH2Database","bigclonedb_clones_alldir_8584153.txt")

    clone_type = "T1"

    if clone_type == "T1":
    	ref_clones_file = os.path.join(home_dir,"treekernel-emse2025","data","BigCloneEval","bigclone_groundtruth_v3","T1-clones-selected-columns.txt")

    elif clone_type=="T2":
    	ref_clones_file = os.path.join(home_dir,"treekernel-emse2025","data","BigCloneEval","bigclone_groundtruth_v3","T2-clones-selected-columns.txt")


    #elif clone_type=="ST3":
    #    ref_clones_file = os.path.join(home_dir,"treekernel-emse2025","data","BigCloneEval","bigclone_groundtruth_v3","bigclonedb_clones_ST3_simline0.7-0.9.txt")

    #elif clone_type=="VST3":
    #    ref_clones_file = os.path.join(home_dir,"treekernel-emse2025","data","BigCloneEval","bigclone_groundtruth_v3","bigclonedb_clones_VST3_simline0.9.txt"

    elif clone_type=="T3":
        ref_clones_file = os.path.join(home_dir,"treekernel-emse2025","data","BigCloneEval","bigclone_groundtruth_v3","ST3-VST3-clones-simtoken-selected-columns.txt")


    metrics_file      = os.path.join(home_dir,"treekernel-emse2025","results","RQ2","metrics_nicad_typewise.csv")
    code_dir          = os.path.join(home_dir,"treekernel-emse2025","data","BigCloneEval","ijadataset","bcb_reduced","0")
    ref_clone_map     = clone_file_lookup(code_dir,ref_clones_file)
    nicad_clone_map   = clone_file_lookup(code_dir,nicad_clones_file)
    print(f"Ref clone map {len(ref_clone_map)} -- nicad clones {len(nicad_clone_map)}")


    #just to make sure all approaches are evaluated on same sample
    sampled_fpath = os.path.join(home_dir,"treekernel-emse2025","results","RQ2",f"final_sample_RQ2_{clone_type}_seed_{seed}.txt")
    print(sampled_fpath)

    sampled_files = load_sampled_files(sampled_fpath)

    calculator    = RecallCalculator(ref_clone_map, nicad_clone_map)
    recall        = calculator.calculate_recall()

    K = 5
    prec_calculator = PrecisionAtKCalculator(nicad_clone_map, ref_clone_map, sampled_files)
    precision_5     = prec_calculator.calculate_mean_precision(K)
    precision_5     = round(precision_5, 3)

    K = 10
    prec_calculator = PrecisionAtKCalculator(nicad_clone_map, ref_clone_map, sampled_files)
    precision_10    = prec_calculator.calculate_mean_precision(K)
    precision_10    = round(precision_10, 3)


    mrr_calculator = MRRCalculator(nicad_clone_map, ref_clone_map, sampled_files)
    mrr            = mrr_calculator.calculate_overall_mrr()
    mrr            = round(mrr,3)

    map_calculator = MAPCalculator(nicad_clone_map, ref_clone_map, sampled_files)
    map = map_calculator.calculate_overall_map()
    map = round(map,3)


    print(f"Precision@5:{precision_5}, Precision@10:{precision_10}, MRR:{mrr}, MAP:{map}")
    with open(metrics_file, "a") as f1:
        f1.write(f"{clone_type}, {precision_5}, {precision_10}, {mrr}, {map}, {recall},{seed}\n")

def print_map(my_map):
    for key, value in my_map.items():
        print(f"{key}: {value}")

def load_sampled_files(sample_file_path):
    """Load sampled files into a set from a file where each line is a file path."""
    with open(sample_file_path, 'r') as file:
        return {os.path.basename(line.strip()) for line in file if line.strip()}
def main():
    start = time.time()
    all_seeds = [ 6251,9080,8241,8828,2084,1375,2802,3501,3389]
    for seed in all_seeds:
    	typewise_evaluation(seed)
    end = time.time()
    print(end - start)
if __name__ == "__main__":
    main()
