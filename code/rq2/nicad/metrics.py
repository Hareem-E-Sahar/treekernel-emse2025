from typing import Dict, List, Tuple
from collections import defaultdict
import operator


class RecallCalculator:
    def __init__(self, ref_clone_map, nicad_clone_map):
        self.ref_clone_map = ref_clone_map
        self.nicad_clone_map = nicad_clone_map

    def calculate_recall(self):
        true_positives = 0
        false_negatives = 0
        for key in self.nicad_clone_map:
            ref_clones   = self.ref_clone_map.get(key, set())
            nicad_clones = self.nicad_clone_map.get(key, set())
            true_positives  += self.count_intersection(ref_clones, nicad_clones)
            false_negatives += self.count_difference(ref_clones, nicad_clones)
        print(f"TP: {true_positives}, FN: {false_negatives}")
       
        recall = true_positives / (true_positives + false_negatives) if (true_positives + false_negatives) > 0 else 0.0
        return recall

    @staticmethod
    def count_intersection(set1, set2):
        intersection = set1.intersection(set2)
        return len(intersection)

    @staticmethod
    def count_difference(set1, set2):
        difference = set1.difference(set2)
        return len(difference)

class PrecisionAtKCalculator:
    def __init__(self, nicad_clones, ref_clones, sampled_files):
        self.nicad_clone_map = nicad_clones
        self.reference_clones = ref_clones
        self.sampled_files = sampled_files
        num_entries = len(self.nicad_clone_map)
        total_clones = sum(len(v) for v in self.nicad_clone_map.values())
        print(f"NiCad Clone Map - Number of Entries: {num_entries}, Total Clones: {total_clones}")
       
        num_entries2 = len(self.reference_clones)
        total_clones2 = sum(len(v) for v in self.reference_clones.values())
        print(f"Reference Clone Map - Number of Entries: {num_entries2}, Total Clones: {total_clones2}")
        
    def calculate_precision(self, query: str, ground_truth: List[str], k: int) -> float:
        clones = sorted(self.nicad_clone_map.get(query, []))
        relevant_items = 0
        examined_items = 0
        print("Nicad clones:",clones) #nicad clones are very few. Why?
        for clone in clones:
            if examined_items >= k:
                break
            if clone in ground_truth:
                relevant_items += 1
            examined_items     += 1
        precision_at_k = relevant_items / float(k) if k > 0 else 0
        return precision_at_k

    def calculate_mean_precision(self, k: int) -> float:
        total_precision = 0
        queries_count = 0
        #queries_to_evaluate = self.sampled_files if self.sampled_files else sorted(self.reference_clones.keys())
        for query in sorted(self.reference_clones.keys()):
            if query in self.sampled_files: #just added this check for RQ2
                print("\n\nQuery:",query)
                #ground_truth = self.reference_clones.get(query, [])
                ground_truth = set(self.reference_clones.get(query, []))
                print("Ground Truth:",ground_truth)
                precision_at_k = self.calculate_precision(query, ground_truth, k)
                total_precision += precision_at_k
                queries_count += 1
        average_precision_at_k = total_precision / queries_count if queries_count > 0 else 0
        return average_precision_at_k

class MRRCalculator:
    def __init__(self, nicad_clones, ref_clones, sampled_files):
        self.nicad_clones = nicad_clones
        self.reference_clones = ref_clones
        self.sampled_files = sampled_files

    def calculate_mrr(self, query: str, ground_truth: List[str]) -> float:
        clones = sorted(self.nicad_clones.get(query, []))
        rank = 0
        # Finding the rank of the first relevant item from the ground truth
        for found_clone in clones:
            rank += 1
            if found_clone in ground_truth:
                return 1.0 / rank  # Reciprocal rank of the first relevant item
        return 0  # No relevant scores found

    def calculate_overall_mrr(self) -> float:
        total_mrr   = 0
        query_count = 0
        for query in sorted(self.reference_clones.keys()):
            if query in self.sampled_files:  #just added this check for RQ2
                #ground_truth = self.reference_clones.get(query, [])
                ground_truth = set(self.reference_clones.get(query, []))
                mrr = self.calculate_mrr(query, ground_truth)
                if mrr > 0:
                    total_mrr += mrr
                    query_count += 1
        return total_mrr / query_count if query_count > 0 else 0

class MAPCalculator:
    def __init__(self, nicad_clones, ref_clones, sampled_files):
        self.nicad_clones = nicad_clones
        self.reference_clones = ref_clones
        self.sampled_files = sampled_files

    def calculate_average_precision(self, query: str, ground_truth: List[str]) -> float:
        if query not in self.nicad_clones or not ground_truth:
            return 0

        clones = sorted(self.nicad_clones.get(query, []))
        sum_precision = 0
        relevant_count = 0
        rank = 0
        for found_clone in clones:
            rank += 1
            if found_clone in ground_truth:
                relevant_count += 1
                sum_precision += relevant_count / rank
        return sum_precision / len(ground_truth) if relevant_count > 0 else 0

    def calculate_overall_map(self) -> float:
        total_ap = 0
        query_count = 0
        for query in sorted(self.reference_clones.keys()):
            if query in self.sampled_files: #just added this check for RQ2
                #ground_truth = self.reference_clones.get(query, [])
                ground_truth = set(self.reference_clones.get(query, []))
                ap = self.calculate_average_precision(query, ground_truth)
                if ap > 0:
                    total_ap += ap
                    query_count += 1
        return total_ap / query_count if query_count > 0 else 0

'''
def test_recall():
    ref_clone_map = {
        "file1": {"file2", "file3"},
        "file4": {"file5"},
    }
    nicad_clone_map = {
        "file1": {"file2", "file4"},
        "file4": {"file6"},
    }
    recall = calculate_recall(ref_clone_map, nicad_clone_map)
    print(f"Recall: {recall}")

def test_precision():
    nicad_clones = defaultdict(list, {
        "file1": ["file2", "file4", "file5"],
        "file4": ["file1", "file6"]
    })
    ref_clones = defaultdict(list, {
        "file1": ["file2", "file3"],
        "file4": ["file5"]
    })

    calculator = PrecisionCalculator(nicad_clones, ref_clones)
    precision = calculator.calculate_mean_precision(2)
    print(f"Mean Precision: {precision}")

def test_MRR():
    nicad_clones = defaultdict(list, {
        "file1": ["file2", "file4", "file5"],
        "file4": ["file1", "file6"]
    })
    ref_clones = defaultdict(list, {
        "file1": ["file2", "file3"],
        "file4": ["file5"]
    })

    calculator = MRRCalculator(nicad_clones, ref_clones)
    mrr = calculator.calculate_overall_mrr()
    print(f"Mean Reciprocal Rank: {mrr}")

def test_MAPCalculator():
    nicad_clones = defaultdict(list, {
        "file1": ["file2", "file4", "file5"],
        "file4": ["file1", "file6"]
    })
    ref_clones = defaultdict(list, {
        "file1": ["file2", "file3"],
        "file4": ["file5"]
    })

    calculator = MAPCalculator(nicad_clones, ref_clones)
    mean_average_precision = calculator.calculate_overall_map()
    print(f"Mean Average Precision: {mean_average_precision}")    
'''
